/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class ShortValue {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected ShortValue(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ShortValue obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_ShortValue(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setValue(short value) {
    EsriFileGdbJNI.ShortValue_value_set(swigCPtr, this, value);
  }

  public short getValue() {
    return EsriFileGdbJNI.ShortValue_value_get(swigCPtr, this);
  }

  public ShortValue() {
    this(EsriFileGdbJNI.new_ShortValue(), true);
  }

}