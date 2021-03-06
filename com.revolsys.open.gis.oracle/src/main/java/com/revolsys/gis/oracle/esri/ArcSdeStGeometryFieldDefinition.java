package com.revolsys.gis.oracle.esri;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.types.DataType;
import com.revolsys.jdbc.attribute.JdbcFieldDefinition;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class ArcSdeStGeometryFieldDefinition extends JdbcFieldDefinition {

  public static List<List<Geometry>> getParts(final Geometry geometry,
    final boolean clockwise) {
    final List<List<Geometry>> partsList = new ArrayList<>();
    if (geometry != null) {
      for (final Geometry part : geometry.geometries()) {
        if (!part.isEmpty()) {
          if (part instanceof Point) {
            final Point point = (Point)part;
            partsList.add(Collections.<Geometry> singletonList(point));
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            partsList.add(Collections.<Geometry> singletonList(line));
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<Geometry> ringList = new ArrayList<>();

            boolean partClockwise = clockwise;
            for (LinearRing ring : polygon.rings()) {
              final boolean ringClockwise = ring.isClockwise();
              if (ringClockwise != partClockwise) {
                ring = ring.reverse();
              }
              ringList.add(ring);
              if (partClockwise == clockwise) {
                partClockwise = !clockwise;
              }
            }
            partsList.add(ringList);
          }
        }
      }
    }
    return partsList;
  }

  private final int dimension;

  private final ArcSdeSpatialReference spatialReference;

  private final GeometryFactory geometryFactory;

  public ArcSdeStGeometryFieldDefinition(final String dbName, final String name,
    final DataType type, final boolean required, final String description,
    final Map<String, Object> properties,
    final ArcSdeSpatialReference spatialReference, final int dimension) {
    super(dbName, name, type, -1, 0, 0, required, description, properties);
    this.spatialReference = spatialReference;
    final GeometryFactory factory = spatialReference.getGeometryFactory();
    this.geometryFactory = GeometryFactory.fixed(factory.getSrid(), dimension,
      factory.getScaleXY(), factory.getScaleZ());
    this.dimension = dimension;
    setProperty(FieldProperties.GEOMETRY_FACTORY, this.geometryFactory);
  }

  @Override
  public void addColumnName(final StringBuilder sql, final String tablePrefix) {
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.ENTITY, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.NUMPTS, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.POINTS");
  }

  @Override
  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append("SDE.ST_GEOMETRY(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
  }

  @Override
  public ArcSdeStGeometryFieldDefinition clone() {
    return new ArcSdeStGeometryFieldDefinition(getDbName(), getName(), getType(),
      isRequired(), getDescription(), getProperties(), this.spatialReference,
      this.dimension);
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final int geometryType = resultSet.getInt(columnIndex);
    if (!resultSet.wasNull()) {
      final int numPoints = resultSet.getInt(columnIndex + 1);
      final Blob blob = resultSet.getBlob(columnIndex + 2);
      final InputStream pointsIn = new BufferedInputStream(
        blob.getBinaryStream(), 32000);

      final Double xOffset = this.spatialReference.getXOffset();
      final Double yOffset = this.spatialReference.getYOffset();
      final Double xyScale = this.spatialReference.getXyScale();
      final Double zScale = this.spatialReference.getZScale();
      final Double zOffset = this.spatialReference.getZOffset();
      final Double mScale = this.spatialReference.getMScale();
      final Double mOffset = this.spatialReference.getMOffset();

      final GeometryFactory geometryFactory = this.spatialReference.getGeometryFactory();
      final Geometry geometry = PackedCoordinateUtil.getGeometry(pointsIn,
        geometryFactory, geometryType, numPoints, xOffset, yOffset, xyScale,
        zOffset, zScale, mOffset, mScale);
      object.setValue(getIndex(), geometry);
    }
    return columnIndex + 3;
  }

  public void setFloat(final PreparedStatement statement, int index,
    final Double value, final Number defaultValue) throws SQLException {
    if (value == null || Double.isInfinite(value) || Double.isNaN(value)) {
      if (defaultValue == null) {
        statement.setNull(index++, Types.FLOAT);
      } else {
        statement.setFloat(index, defaultValue.floatValue());
      }
    } else {
      statement.setFloat(index, value.floatValue());
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, Object value) throws SQLException {
    int index = parameterIndex;

    if (value instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)value;
      value = boundingBox.convert(this.geometryFactory).toPolygon(1);
    }
    if (value instanceof Geometry) {
      Geometry geometry = (Geometry)value;
      geometry = geometry.copy(this.geometryFactory);

      final int sdeSrid = this.spatialReference.getEsriSrid();
      final Double xOffset = this.spatialReference.getXOffset();
      final Double yOffset = this.spatialReference.getYOffset();
      final Double xyScale = this.spatialReference.getXyScale();
      final Double zScale = this.spatialReference.getZScale();
      final Double zOffset = this.spatialReference.getZOffset();
      final Double mScale = this.spatialReference.getMScale();
      final Double mOffset = this.spatialReference.getMOffset();

      final BoundingBox envelope = geometry.getBoundingBox();

      final double minX = envelope.getMinX();
      final double minY = envelope.getMinY();
      final double maxX = envelope.getMaxX();
      final double maxY = envelope.getMaxY();
      final double area = geometry.getArea();
      final double length = geometry.getLength();

      final boolean hasZ = this.dimension > 2 && zOffset != null
          && zScale != null;
      final boolean hasM = this.dimension > 3 && mOffset != null
          && mScale != null;

      int numPoints = 0;
      byte[] data;

      final List<List<Geometry>> parts = getParts(geometry, false);
      final int entityType = ArcSdeConstants.getStGeometryType(geometry);
      numPoints = PackedCoordinateUtil.getNumPoints(parts);
      data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale,
        hasZ, zOffset, zScale, hasM, mScale, mOffset, parts);

      statement.setInt(index++, entityType);
      statement.setInt(index++, numPoints);
      setFloat(statement, index++, minX, 0);
      setFloat(statement, index++, minY, 0);
      setFloat(statement, index++, maxX, 0);
      setFloat(statement, index++, maxY, 0);
      if (hasZ) {
        final double minZ = envelope.getMin(2);
        final double maxZ = envelope.getMax(2);
        setFloat(statement, index++, minZ, 0);
        setFloat(statement, index++, maxZ, 0);
      } else {
        statement.setNull(index++, Types.FLOAT);
        statement.setNull(index++, Types.FLOAT);
      }
      if (hasM) {
        final double minM = envelope.getMin(3);
        final double maxM = envelope.getMax(3);
        setFloat(statement, index++, minM, 0);
        setFloat(statement, index++, maxM, 0);
      } else {
        statement.setNull(index++, Types.FLOAT);
        statement.setNull(index++, Types.FLOAT);
      }
      statement.setFloat(index++, (float)area);
      statement.setFloat(index++, (float)length);
      statement.setInt(index++, sdeSrid);
      statement.setBytes(index++, data);
    } else {
      throw new IllegalArgumentException("Geometry cannot be null");
    }
    return index;
  }
}
