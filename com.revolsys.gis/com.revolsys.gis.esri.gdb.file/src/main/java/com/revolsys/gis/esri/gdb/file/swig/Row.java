/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class Row {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Row(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Row obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Row(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int SetNull(String field) {
    return EsriFileGdbJNI.Row_SetNull(swigCPtr, this, field);
  }

  public int GetGeometry(ShapeBuffer shapeBuffer) {
    return EsriFileGdbJNI.Row_GetGeometry(swigCPtr, this, ShapeBuffer.getCPtr(shapeBuffer), shapeBuffer);
  }

  public int SetGeometry(ShapeBuffer shapeBuffer) {
    return EsriFileGdbJNI.Row_SetGeometry(swigCPtr, this, ShapeBuffer.getCPtr(shapeBuffer), shapeBuffer);
  }

  public int SetShort(String field, short value) {
    return EsriFileGdbJNI.Row_SetShort(swigCPtr, this, field, value);
  }

  public int SetInteger(String field, int value) {
    return EsriFileGdbJNI.Row_SetInteger(swigCPtr, this, field, value);
  }

  public int SetFloat(String field, float value) {
    return EsriFileGdbJNI.Row_SetFloat(swigCPtr, this, field, value);
  }

  public int SetDouble(String field, double value) {
    return EsriFileGdbJNI.Row_SetDouble(swigCPtr, this, field, value);
  }

  public int SetDate(String field, SWIGTYPE_p_tm value) {
    return EsriFileGdbJNI.Row_SetDate(swigCPtr, this, field, SWIGTYPE_p_tm.getCPtr(value));
  }

  public int SetString(String field, String value) {
    return EsriFileGdbJNI.Row_SetString(swigCPtr, this, field, value);
  }

  public int SetGUID(String field, Guid value) {
    return EsriFileGdbJNI.Row_SetGUID(swigCPtr, this, field, Guid.getCPtr(value), value);
  }

  public int SetXML(String field, String value) {
    return EsriFileGdbJNI.Row_SetXML(swigCPtr, this, field, value);
  }

  public int SetRaster(String field, Raster raster) {
    return EsriFileGdbJNI.Row_SetRaster(swigCPtr, this, field, Raster.getCPtr(raster), raster);
  }

  public int GetBinary(String field, ByteArray binaryBuf) {
    return EsriFileGdbJNI.Row_GetBinary(swigCPtr, this, field, ByteArray.getCPtr(binaryBuf), binaryBuf);
  }

  public int SetBinary(String field, ByteArray binaryBuf) {
    return EsriFileGdbJNI.Row_SetBinary(swigCPtr, this, field, ByteArray.getCPtr(binaryBuf), binaryBuf);
  }

  public int GetFieldInformation(FieldInfo fieldInfo) {
    return EsriFileGdbJNI.Row_GetFieldInformation(swigCPtr, this, FieldInfo.getCPtr(fieldInfo), fieldInfo);
  }

  public Row() {
    this(EsriFileGdbJNI.new_Row(), true);
  }

  public boolean isNull(String name) {
    return EsriFileGdbJNI.Row_isNull(swigCPtr, this, name);
  }

  public SWIGTYPE_p_tm getDate(String name) {
    return new SWIGTYPE_p_tm(EsriFileGdbJNI.Row_getDate(swigCPtr, this, name), true);
  }

  public double getDouble(String name) {
    return EsriFileGdbJNI.Row_getDouble(swigCPtr, this, name);
  }

  public float getFloat(String name) {
    return EsriFileGdbJNI.Row_getFloat(swigCPtr, this, name);
  }

  public Guid getGuid(String name) {
    return new Guid(EsriFileGdbJNI.Row_getGuid(swigCPtr, this, name), true);
  }

  public int getOid() {
    return EsriFileGdbJNI.Row_getOid(swigCPtr, this);
  }

  public short getShort(String name) {
    return EsriFileGdbJNI.Row_getShort(swigCPtr, this, name);
  }

  public int getInteger(String name) {
    return EsriFileGdbJNI.Row_getInteger(swigCPtr, this, name);
  }

  public String getString(String name) {
    return EsriFileGdbJNI.Row_getString(swigCPtr, this, name);
  }

  public String getXML(String name) {
    return EsriFileGdbJNI.Row_getXML(swigCPtr, this, name);
  }

  public ShapeBuffer getGeometry() {
    long cPtr = EsriFileGdbJNI.Row_getGeometry(swigCPtr, this);
    return (cPtr == 0) ? null : new ShapeBuffer(cPtr, false);
  }

}
