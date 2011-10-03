/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class EnumRows {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected EnumRows(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(EnumRows obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      EsriFileGdbJNI.delete_EnumRows(swigCPtr);
    }
    swigCPtr = 0;
  }

  public void Close() {
    EsriFileGdbJNI.EnumRows_Close(swigCPtr, this);
  }

  public int GetFieldInformation(FieldInfo fieldInfo) {
    return EsriFileGdbJNI.EnumRows_GetFieldInformation(swigCPtr, this, FieldInfo.getCPtr(fieldInfo), fieldInfo);
  }

  public EnumRows() {
    this(EsriFileGdbJNI.new_EnumRows(), true);
  }

  public Row next() {
    long cPtr = EsriFileGdbJNI.EnumRows_next(swigCPtr, this);
    return (cPtr == 0) ? null : new Row(cPtr, true);
  }

}