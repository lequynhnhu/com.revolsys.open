package com.revolsys.gis.oracle.esri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.data.io.RecordStoreExtension;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.gis.oracle.io.OracleRecordStore;
import com.revolsys.io.Path;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.Property;

public class ArcSdeStGeometryRecordStoreExtension implements
  RecordStoreExtension {

  public ArcSdeStGeometryRecordStoreExtension() {
  }

  @Override
  public void initialize(final RecordStore recordStore,
    final Map<String, Object> connectionProperties) {
    final OracleRecordStore oracleRecordStore = (OracleRecordStore)recordStore;
    final JdbcAttributeAdder stGeometryAttributeAdder = new ArcSdeStGeometryAttributeAdder(
      oracleRecordStore);
    oracleRecordStore.addAttributeAdder("ST_GEOMETRY", stGeometryAttributeAdder);
    oracleRecordStore.addAttributeAdder("SDE.ST_GEOMETRY",
      stGeometryAttributeAdder);
  }

  @Override
  public boolean isEnabled(final RecordStore recordStore) {
    return ArcSdeConstants.isSdeAvailable(recordStore);
  }

  private void loadColumnProperties(final RecordStoreSchema schema,
    final String schemaName, final Connection connection) throws SQLException {
    final String sql = "SELECT GC.F_TABLE_NAME, GC.F_GEOMETRY_COLUMN, GC.SRID, GC.GEOMETRY_TYPE, GC.COORD_DIMENSION, SG.GEOMETRY_TYPE GEOMETRY_DATA_TYPE FROM SDE.GEOMETRY_COLUMNS GC LEFT OUTER JOIN SDE.ST_GEOMETRY_COLUMNS SG ON GC.F_TABLE_SCHEMA = SG.OWNER AND GC.F_TABLE_NAME = SG.TABLE_NAME WHERE GC.F_TABLE_SCHEMA = ?";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.setString(1, schemaName);
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          final String tableName = resultSet.getString(1);
          final String columnName = resultSet.getString(2);

          final String typePath = Path.toPath(schemaName, tableName);

          final int esriSrid = resultSet.getInt(3);
          JdbcAttributeAdder.setColumnProperty(schema, typePath, columnName,
            ArcSdeConstants.ESRI_SRID_PROPERTY, esriSrid);

          int axisCount = resultSet.getInt(5);
          axisCount = Math.max(axisCount, 2);
          JdbcAttributeAdder.setColumnProperty(schema, typePath, columnName,
            JdbcAttributeAdder.NUM_AXIS, axisCount);

          final ArcSdeSpatialReference spatialReference = ArcSdeSpatialReferenceCache.getSpatialReference(
            schema, esriSrid);
          JdbcAttributeAdder.setColumnProperty(schema, typePath, columnName,
            ArcSdeConstants.SPATIAL_REFERENCE, spatialReference);

          GeometryFactory geometryFactory = JdbcAttributeAdder.getColumnProperty(
            schema, typePath, columnName, JdbcAttributeAdder.GEOMETRY_FACTORY);
          int srid = spatialReference.getSrid();
          final double scaleXy = spatialReference.getXyScale();
          final double scaleZ = spatialReference.getZScale();
          if (srid <= 0) {
            srid = geometryFactory.getSrid();
          }
          axisCount = Math.min(axisCount, 3);
          geometryFactory = GeometryFactory.fixed(srid, axisCount, scaleXy,
            scaleZ);

          JdbcAttributeAdder.setColumnProperty(schema, typePath, columnName,
            JdbcAttributeAdder.GEOMETRY_FACTORY, geometryFactory);

          final int geometryType = resultSet.getInt(4);
          JdbcAttributeAdder.setColumnProperty(schema, typePath, columnName,
            JdbcAttributeAdder.GEOMETRY_TYPE,
            ArcSdeConstants.getGeometryDataType(geometryType));

          String geometryColumnType = resultSet.getString(6);
          if (!Property.hasValue(geometryColumnType)) {
            geometryColumnType = ArcSdeConstants.SDEBINARY;
          }
          JdbcAttributeAdder.setColumnProperty(schema, typePath, columnName,
            ArcSdeConstants.GEOMETRY_COLUMN_TYPE, geometryColumnType);
        }
      } finally {
        JdbcUtils.close(resultSet);
      }
    } finally {
      JdbcUtils.close(statement);
    }
  }

  private void loadTableProperties(final Connection connection,
    final RecordStoreSchema schema, final String schemaName) {
    final String sql = "SELECT registration_id, table_name, rowid_column FROM sde.table_registry WHERE owner = ?";
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(sql);
      statement.setString(1, schemaName);
      resultSet = statement.executeQuery();
      while (resultSet.next()) {
        final String tableName = resultSet.getString(2);
        final String typePath = Path.toPath(schemaName, tableName)
          .toUpperCase();

        final int registrationId = resultSet.getInt(1);
        JdbcAttributeAdder.setTableProperty(schema, typePath,
          ArcSdeConstants.REGISTRATION_ID, registrationId);

        final String rowidColumn = resultSet.getString(3);
        JdbcAttributeAdder.setTableProperty(schema, typePath,
          ArcSdeConstants.ROWID_COLUMN, rowidColumn);
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Unable to load rowid columns for " + schemaName, e);
    } finally {
      JdbcUtils.close(statement, resultSet);
    }
  }

  @Override
  public void postProcess(final RecordStoreSchema schema) {
    final String schemaName = schema.getName();
    for (final RecordDefinition recordDefinition : schema.getRecordDefinitions()) {
      final String typePath = recordDefinition.getPath();
      final Integer registrationId = JdbcAttributeAdder.getTableProperty(
        schema, typePath, ArcSdeConstants.REGISTRATION_ID);
      final String rowIdColumn = JdbcAttributeAdder.getTableProperty(schema,
        typePath, ArcSdeConstants.ROWID_COLUMN);
      if (registrationId != null && rowIdColumn != null) {
        ArcSdeObjectIdJdbcAttribute.replaceAttribute(schemaName,
          recordDefinition, registrationId, rowIdColumn);
      }
    }
  }

  @Override
  public void preProcess(final RecordStoreSchema schema) {
    final RecordStore recordStore = schema.getRecordStore();
    final OracleRecordStore oracleRecordStore = (OracleRecordStore)recordStore;
    try {
      try (
        final Connection connection = oracleRecordStore.getJdbcConnection()) {
        final String schemaName = oracleRecordStore.getDatabaseSchemaName(schema);
        loadTableProperties(connection, schema, schemaName);
        loadColumnProperties(schema, schemaName, connection);
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Unable to get ArcSDE metadata for schema " + schema.getName(), e);
    }
  }
}
