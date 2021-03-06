/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class Guid {
  protected static long getCPtr(final Guid obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public Guid() {
    this(EsriFileGdbJNI.new_Guid(), true);
  }

  protected Guid(final long cPtr, final boolean cMemoryOwn) {
    this.swigCMemOwn = cMemoryOwn;
    this.swigCPtr = cPtr;
  }

  public void Create() {
    EsriFileGdbJNI.Guid_Create(this.swigCPtr, this);
  }

  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        EsriFileGdbJNI.delete_Guid(this.swigCPtr);
      }
      this.swigCPtr = 0;
    }
  }

  public boolean equal(final Guid other) {
    return EsriFileGdbJNI.Guid_equal(this.swigCPtr, this, Guid.getCPtr(other),
      other);
  }

  @Override
  protected void finalize() {
    delete();
  }

  public int FromString(final String guidString) {
    return EsriFileGdbJNI.Guid_FromString(this.swigCPtr, this, guidString);
  }

  public boolean notEqual(final Guid other) {
    return EsriFileGdbJNI.Guid_notEqual(this.swigCPtr, this,
      Guid.getCPtr(other), other);
  }

  public void SetNull() {
    EsriFileGdbJNI.Guid_SetNull(this.swigCPtr, this);
  }

  @Override
  public String toString() {
    return EsriFileGdbJNI.Guid_toString(this.swigCPtr, this);
  }

}
