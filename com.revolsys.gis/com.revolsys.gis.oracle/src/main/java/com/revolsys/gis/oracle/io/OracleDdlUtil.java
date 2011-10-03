package com.revolsys.gis.oracle.io;

import java.io.PrintWriter;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class OracleDdlUtil {

  public static void createTable(PrintWriter out, DataObjectMetaData metaData) {
    QName typeName = metaData.getName();
    out.println();
    out.print("CREATE TABLE ");
    String schemaName = typeName.getNamespaceURI();
    String tableName = typeName.getLocalPart();
    if (schemaName.length() > 0) {
      out.print(schemaName);
      out.print('.');
    }
    out.print(tableName);
    out.println(" (");
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 1) {
        out.println(",");
      }
      Attribute attribute = metaData.getAttribute(i);
      String name = attribute.getName();
      out.print("  ");
      out.print(name);
      for (int j = name.length(); j < 32; j++) {
        out.print(' ');
      }
      out.print(" : ");
      DataType dataType = attribute.getType();
      if (dataType == DataTypes.BOOLEAN) {
        out.print("NUMBER(1)");
      } else  if (dataType == DataTypes.BYTE) {
        out.print("NUMBER(3)");
      } else  if (dataType == DataTypes.SHORT) {
        out.print("NUMBER(5)");
      } else  if (dataType == DataTypes.INT) {
        out.print("NUMBER(9)");
      } else  if (dataType == DataTypes.LONG) {
        out.print("NUMBER(19)");
      } else  if (dataType == DataTypes.FLOAT) {
        out.print("NUMBER");
      } else  if (dataType == DataTypes.DOUBLE) {
        out.print("NUMBER");
      } else  if (dataType == DataTypes.STRING) {
        out.print("VARCHAR2(");
        out.print(attribute.getLength());
        out.print(")");
      } else  if (dataType == DataTypes.GEOMETRY) {
        out.print("MDSYS.SDO_GEOMETRY");
      }
      if (attribute.isRequired()) {
        out.print(" NOT NULL");
      }
    }
    out.println();
    out.println(");");

  }
}