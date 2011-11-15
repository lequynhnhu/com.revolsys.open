package com.revolsys.io.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.io.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.io.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.util.CollectionUtil;

public class EsriXmlDataObjectMetaDataUtil implements
  EsriGeodatabaseXmlConstants {
  private static final String DE_TABLE_PROPERTY = EsriXmlDataObjectMetaDataUtil.class
    + ".DETable";

  public static final EsriGeodatabaseXmlFieldTypeRegistry FIELD_TYPES = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  private static Field addField(final DETable table, final Attribute attribute) {
    final String fieldName = attribute.getName();
    final DataType dataType = attribute.getType();
    final EsriGeodatabaseXmlFieldType fieldType = FIELD_TYPES.getFieldType(dataType);
    if (fieldType == null) {
      throw new RuntimeException("Data type not supported " + dataType);
    } else {
      final Field field = new Field();
      field.setName(fieldName);
      field.setType(fieldType.getEsriFieldType());
      field.setIsNullable(!attribute.isRequired());
      field.setRequired(attribute.isRequired());
      int length = fieldType.getFixedLength();
      if (length < 0) {
        length = attribute.getLength();
      }
      field.setLength(length);
      final int precision;
      if (fieldType.isUsePrecision()) {
        precision = attribute.getLength();
      } else {
        precision = 0;
      }
      field.setPrecision(precision);
      final int scale = attribute.getScale();
      field.setScale(scale);
      table.addField(field);
      return field;
    }
  }

  private static void addGeometryField(final GeometryType shapeType,
    final DETable table, final Attribute attribute) {
    final Field field = addField(table, attribute);
    final DEFeatureClass featureClass = (DEFeatureClass)table;
    final SpatialReference spatialReference = featureClass.getSpatialReference();
    final GeometryDef geometryDef = new GeometryDef(shapeType, spatialReference);
    field.setGeometryDef(geometryDef);

    table.addIndex(field, false, "FDO_GEOMETRY");
  }

  private static void addObjectIdField(final DETable table) {
    final Field field = new Field();
    field.setName("OBJECTID");
    field.setType(FieldType.esriFieldTypeOID);
    field.setIsNullable(false);
    field.setLength(4);
    field.setRequired(true);
    field.setEditable(false);
    table.addField(field);

    table.addIndex(field, true, "FDO_OBJECTID");
  }

  public static DEFeatureDataset createDEFeatureDataset(
    final String schemaName, final SpatialReference spatialReference) {
    final DEFeatureDataset dataset = new DEFeatureDataset();
    String name;
    final int slashIndex = schemaName.lastIndexOf('\\');
    if (slashIndex == -1) {
      name = schemaName;
    } else {
      name = schemaName.substring(slashIndex + 1);
    }
    dataset.setCatalogPath("\\" + schemaName);
    dataset.setName(name);

    final EnvelopeN envelope = new EnvelopeN(spatialReference);
    dataset.setExtent(envelope);
    dataset.setSpatialReference(spatialReference);
    return dataset;
  }

  public static List<DEFeatureDataset> createDEFeatureDatasets(
    final DETable table) {
    final String parentPath = table.getParentCatalogPath();
    if (parentPath.equals("\\")) {
      return Collections.emptyList();
    } else if (table instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)table;
      final String schemaName = parentPath.substring(1);
      final SpatialReference spatialReference = featureClass.getSpatialReference();
      return createDEFeatureDatasets(schemaName, spatialReference);
    } else {
      throw new IllegalArgumentException("Expected a "
        + DEFeatureClass.class.getName());
    }
  }

  public static List<DEFeatureDataset> createDEFeatureDatasets(
    final String schemaName, final SpatialReference spatialReference) {
    final List<DEFeatureDataset> datasets = new ArrayList<DEFeatureDataset>();
    String path = "";
    for (final String name : schemaName.split("\\\\")) {
      path += name;
      final DEFeatureDataset dataset = createDEFeatureDataset(path,
        spatialReference);
      datasets.add(dataset);
      path += "\\";
    }
    return datasets;
  }

  public static DETable createDETable(final DataObjectMetaData metaData,
    final SpatialReference spatialReference) {
    final QName typeName = metaData.getName();
    final String schemaName = typeName.getNamespaceURI();
    String schemaPath;
    if (schemaName.length() == 0) {
      schemaPath = "";
    } else {
      schemaPath = "\\" + schemaName;
    }
    return createDETable(schemaPath, metaData, spatialReference);
  }

  public static DETable createDETable(final String schemaPath,
    final DataObjectMetaData metaData, final SpatialReference spatialReference) {
    DETable table;
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    boolean hasGeometry = false;
    DataType geometryDataType = null;
    GeometryType shapeType = null;
    if (geometryAttribute != null) {
      geometryDataType = geometryAttribute.getType();
      if (FIELD_TYPES.getFieldType(geometryDataType) != null) {
        hasGeometry = true;
        // TODO Z,m
        if (geometryDataType.equals(DataTypes.POINT)) {
          shapeType = GeometryType.esriGeometryPoint;
        } else if (geometryDataType.equals(DataTypes.MULTI_POINT)) {
          shapeType = GeometryType.esriGeometryMultipoint;
        } else if (geometryDataType.equals(DataTypes.LINE_STRING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(DataTypes.MULTI_LINE_STRING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(DataTypes.POLYGON)) {
          shapeType = GeometryType.esriGeometryPolygon;
        } else if (geometryDataType.equals(DataTypes.MULTI_POLYGON)) {
          shapeType = GeometryType.esriGeometryMultiPatch;
        } else {
          throw new IllegalArgumentException("Unable to detect geometry type");
        }
      }
    }

    final QName typeName = metaData.getName();
    final String name = typeName.getLocalPart();
    if (hasGeometry) {
      final DEFeatureClass featureClass = new DEFeatureClass();
      table = featureClass;
      featureClass.setShapeType(shapeType);
      featureClass.setShapeFieldName(geometryAttribute.getName());
      final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      featureClass.setSpatialReference(spatialReference);
      featureClass.setHasZ(geometryFactory.hasM());
      featureClass.setHasZ(geometryFactory.hasZ());
      final EnvelopeN envelope = new EnvelopeN(spatialReference);
      featureClass.setExtent(envelope);

    } else {
      table = new DETable();
    }

    table.setCatalogPath(schemaPath + "\\" + name);
    table.setName(name);
    table.setHasOID(true);
    table.setOIDFieldName("OBJECTID");

    addObjectIdField(table);
    final Attribute idAttribute = metaData.getIdAttribute();
    for (final Attribute attribute : metaData.getAttributes()) {
      if (attribute == geometryAttribute) {
        addGeometryField(shapeType, table, attribute);
      } else {
        final String attributeName = attribute.getName();
        if (!attributeName.equals("OBJECTID")) {
          final Field field = addField(table, attribute);
          if (idAttribute == attribute) {
            table.addIndex(field, true, attributeName + "_PK");
          }
        }
      }
    }
    table.setAliasName(name);
    return table;
  }

  public static DETable getDETable(final DataObjectMetaData metaData,
    final SpatialReference spatialReference) {
    DETable table = metaData.getProperty(DE_TABLE_PROPERTY);
    if (table == null) {
      table = createDETable(metaData, spatialReference);
    }
    return table;
  }

  public static DataObjectMetaData getMetaData(final String schemaName,
    final CodedValueDomain domain) {
    final String tableName = domain.getName();
    final QName typeName = new QName(schemaName, tableName);
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(typeName);
    final FieldType fieldType = domain.getFieldType();
    final DataType dataType = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE.getDataType(fieldType);
    metaData.addAttribute(tableName + "_ID", dataType, true);
    metaData.addAttribute("VALUE", DataTypes.STRING, 255, true);
    metaData.setIdAttributeIndex(0);
    return metaData;
  }

  /**
   * Get a metaData instance for the table definition excluding any ESRI
   * specific fields.
   * 
   * @param schemaName
   * @param deTable
   * @return
   */
  public static DataObjectMetaData getMetaData(final String schemaName,
    final DETable deTable) {
    return getMetaData(schemaName, deTable, true);
  }

  public static DataObjectMetaData getMetaData(final String schemaName,
    final DETable deTable, final boolean ignoreEsriFields) {
    final String tableName = deTable.getName();
    final QName typeName = new QName(schemaName, tableName);
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(typeName);
    final List<String> ignoreFieldNames = new ArrayList<String>();
    if (ignoreEsriFields) {
      ignoreFieldNames.add(deTable.getOIDFieldName());

      if (deTable instanceof DEFeatureClass) {
        final DEFeatureClass featureClass = (DEFeatureClass)deTable;
        ignoreFieldNames.add(featureClass.getLengthFieldName());
        ignoreFieldNames.add(featureClass.getAreaFieldName());
      }
    }
    for (final Field field : deTable.getFields()) {
      final String fieldName = field.getName();
      if (!ignoreFieldNames.contains(fieldName)) {
        addField(metaData, deTable, tableName, field, fieldName);
      }
    }
    for (Index index : deTable.getIndexes()) {
      String indexName = index.getName();
      if (indexName.endsWith("_PK")) {
        final List<Field> indexFields = index.getFields();
        final Field indexField = CollectionUtil.get(indexFields,0);
        final String idName = indexField.getName();
        metaData.setIdAttributeName(idName);
      }
    }
    if (deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final String shapeFieldName = featureClass.getShapeFieldName();
      metaData.setGeometryAttributeName(shapeFieldName);
      final SpatialReference spatialReference = featureClass.getSpatialReference();
      GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      if (featureClass.isHasM()) {
        geometryFactory = new GeometryFactory(geometryFactory, 4);
      } else if (featureClass.isHasZ()) {
        geometryFactory = new GeometryFactory(geometryFactory, 3);
      }
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        geometryFactory);
    }

    return metaData;
  }

  private static void addField(final DataObjectMetaDataImpl metaData,
    final DETable deTable, final String tableName, final Field field,
    final String fieldName) {
    final FieldType fieldType = field.getType();
    int precision = field.getPrecision();
    final DataType dataType;
    if (fieldType == FieldType.esriFieldTypeGeometry
      && deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final GeometryType shapeType = featureClass.getShapeType();
      switch (shapeType) {
        case esriGeometryPoint:
          dataType = DataTypes.POINT;
        break;
        case esriGeometryMultipoint:
          dataType = DataTypes.MULTI_POINT;
        break;
        case esriGeometryPolyline:
          dataType = DataTypes.MULTI_LINE_STRING;
        break;
        case esriGeometryPolygon:
          dataType = DataTypes.POLYGON;
        break;

        default:
          throw new RuntimeException("Unknown geometry type");
      }

    } else if (precision > 0
      && (fieldType.equals(FieldType.esriFieldTypeSingle) || fieldType.equals(FieldType.esriFieldTypeDouble))) {
      dataType = DataTypes.DECIMAL;
    } else {
      dataType = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE.getDataType(fieldType);
    }
    final int scale = field.getScale();
    int length = field.getLength();
    if (precision != 0) {
      length = precision;
    }
    final Boolean required = !field.isIsNullable()
      || field.getRequired() == Boolean.TRUE;
    final Attribute attribute = new Attribute(fieldName, dataType, length,
      scale, required);

    metaData.addAttribute(attribute);
    if (fieldName.equals(tableName + "_ID")) {
      metaData.setIdAttributeName(fieldName);
    }
  }

  public static List<DataObject> getValues(DataObjectMetaData metaData,
    CodedValueDomain domain) {
    List<DataObject> values = new ArrayList<DataObject>();
    for (CodedValue codedValue : domain.getCodedValues()) {
      DataObject value = new ArrayDataObject(metaData);
      value.setIdValue(codedValue.getCode());
      value.setValue("VALUE", codedValue.getName());
      values.add(value);
    }
    return values;
  }
}