package com.revolsys.io.gml;

import java.io.Writer;

import javax.xml.namespace.QName;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataProperties;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.PathUtil;
import com.revolsys.io.gml.type.GmlFieldType;
import com.revolsys.io.gml.type.GmlFieldTypeRegistry;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.Property;

public class GmlDataObjectWriter extends AbstractWriter<DataObject> implements
GmlConstants {
  public static final void srsName(final XmlWriter out,
    final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final int csId = coordinateSystem.getId();
    out.attribute(SRS_NAME, "EPSG:" + csId);
  }

  private final GmlFieldTypeRegistry fieldTypes = GmlFieldTypeRegistry.INSTANCE;

  private GeometryFactory geometryFactory;

  private final DataObjectMetaData metaData;

  private boolean opened;

  private final XmlWriter out;

  private QName qualifiedName;

  private final String namespaceUri;

  private boolean writeNulls;

  public GmlDataObjectWriter(final DataObjectMetaData metaData, final Writer out) {
    this.metaData = metaData;
    this.out = new XmlWriter(out);
    this.qualifiedName = metaData.getProperty(DataObjectMetaDataProperties.QUALIFIED_NAME);
    if (this.qualifiedName == null) {
      this.qualifiedName = new QName(metaData.getTypeName());
    }
    this.namespaceUri = this.qualifiedName.getNamespaceURI();
    this.out.setPrefix(this.qualifiedName);
  }

  private void box(final GeometryFactory geometryFactory,
    final BoundingBox areaBoundingBox) {
    this.out.startTag(BOX);
    srsName(this.out, geometryFactory);
    this.out.startTag(COORDINATES);
    this.out.text(areaBoundingBox.getMinX());
    this.out.text(",");
    this.out.text(areaBoundingBox.getMinY());
    this.out.text(" ");
    this.out.text(areaBoundingBox.getMaxX());
    this.out.text(",");
    this.out.text(areaBoundingBox.getMaxY());
    this.out.endTag(COORDINATES);
    this.out.endTag(BOX);
  }

  @Override
  public void close() {
    if (!this.opened) {
      writeHeader();
    }

    writeFooter();
    this.out.close();
  }

  private void envelope(final GeometryFactory geometryFactory,
    final BoundingBox areaBoundingBox) {
    this.out.startTag(ENVELOPE);
    srsName(this.out, geometryFactory);
    this.out.element(LOWER_CORNER, areaBoundingBox.getMinX() + " "
      + areaBoundingBox.getMinY());
    this.out.element(UPPER_CORNER, areaBoundingBox.getMaxX() + " "
      + areaBoundingBox.getMaxY());
    this.out.endTag(ENVELOPE);
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  public boolean isWriteNulls() {
    return this.writeNulls;
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.GEOMETRY_FACTORY)) {
      this.geometryFactory = (GeometryFactory)value;
    } else if (IoConstants.WRITE_NULLS_PROPERTY.equals(name)) {
      this.writeNulls = BooleanStringConverter.isTrue(value);
    }
    super.setProperty(name, value);
  }

  @Override
  public void write(final DataObject object) {
    if (!this.opened) {
      writeHeader();
    }
    this.out.startTag(FEATURE_MEMBER);
    final DataObjectMetaData metaData = object.getMetaData();
    QName qualifiedName = metaData.getProperty(DataObjectMetaDataProperties.QUALIFIED_NAME);
    if (qualifiedName == null) {
      final String typeName = metaData.getPath();
      final String path = PathUtil.getPath(typeName);
      final String name = PathUtil.getName(typeName);
      qualifiedName = new QName(path, name);
      metaData.setProperty(DataObjectMetaDataProperties.QUALIFIED_NAME,
        qualifiedName);
    }
    this.out.startTag(qualifiedName);

    for (final Attribute attribute : metaData.getAttributes()) {
      final String attributeName = attribute.getName();
      final Object value = object.getValue(attributeName);
      if (Property.hasValue(value) || this.writeNulls) {
        this.out.startTag(this.namespaceUri, attributeName);
        final DataType type = attribute.getType();
        final GmlFieldType fieldType = this.fieldTypes.getFieldType(type);
        fieldType.writeValue(this.out, value);
        this.out.endTag();
      }
    }

    this.out.endTag(qualifiedName);
    this.out.endTag(FEATURE_MEMBER);
  }

  public void writeFooter() {
    this.out.endTag(FEATURE_COLLECTION);
    this.out.endDocument();
  }

  private void writeHeader() {
    this.opened = true;
    this.out.startDocument("UTF-8", "1.0");

    this.out.startTag(FEATURE_COLLECTION);
    if (this.geometryFactory != null) {
      this.out.startTag(BOUNDED_BY);
      box(this.geometryFactory, this.geometryFactory.getCoordinateSystem()
        .getAreaBoundingBox());
      this.out.endTag(BOUNDED_BY);
    }
  }

}
