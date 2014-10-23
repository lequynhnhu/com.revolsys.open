/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.39
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.ogr;

public class FieldDefn {
  protected static long getCPtr(final FieldDefn obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }

  protected static long getCPtrAndDisown(final FieldDefn obj) {
    if (obj != null) {
      obj.swigCMemOwn = false;
    }
    return getCPtr(obj);
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public FieldDefn() {
    this(ogrJNI.new_FieldDefn__SWIG_2(), true);
  }

  protected FieldDefn(final long cPtr, final boolean cMemoryOwn) {
    if (cPtr == 0) {
      throw new RuntimeException();
    }
    this.swigCMemOwn = cMemoryOwn;
    this.swigCPtr = cPtr;
  }

  public FieldDefn(final String name_null_ok) {
    this(ogrJNI.new_FieldDefn__SWIG_1(name_null_ok), true);
  }

  public FieldDefn(final String name_null_ok, final int field_type) {
    this(ogrJNI.new_FieldDefn__SWIG_0(name_null_ok, field_type), true);
  }

  /* Ensure that the GC doesn't collect any parent instance set from Java */
  protected void addReference(final Object reference) {
  }

  public synchronized void delete() {
    if (this.swigCPtr != 0 && this.swigCMemOwn) {
      this.swigCMemOwn = false;
      ogrJNI.delete_FieldDefn(this.swigCPtr);
    }
    this.swigCPtr = 0;
  }

  @Override
  public boolean equals(final Object obj) {
    boolean equal = false;
    if (obj instanceof FieldDefn) {
      equal = ((FieldDefn)obj).swigCPtr == this.swigCPtr;
    }
    return equal;
  }

  @Override
  protected void finalize() {
    delete();
  }

  public int GetFieldType() {
    return ogrJNI.FieldDefn_GetFieldType(this.swigCPtr, this);
  }

  public String GetFieldTypeName(final int type) {
    return ogrJNI.FieldDefn_GetFieldTypeName(this.swigCPtr, this, type);
  }

  public int GetJustify() {
    return ogrJNI.FieldDefn_GetJustify(this.swigCPtr, this);
  }

  public String GetName() {
    return ogrJNI.FieldDefn_GetName(this.swigCPtr, this);
  }

  public String GetNameRef() {
    return ogrJNI.FieldDefn_GetNameRef(this.swigCPtr, this);
  }

  public int GetPrecision() {
    return ogrJNI.FieldDefn_GetPrecision(this.swigCPtr, this);
  }

  public String GetTypeName() {
    return ogrJNI.FieldDefn_GetTypeName(this.swigCPtr, this);
  }

  public int GetWidth() {
    return ogrJNI.FieldDefn_GetWidth(this.swigCPtr, this);
  }

  @Override
  public int hashCode() {
    return (int)this.swigCPtr;
  }

  public int IsIgnored() {
    return ogrJNI.FieldDefn_IsIgnored(this.swigCPtr, this);
  }

  public void SetIgnored(final int bIgnored) {
    ogrJNI.FieldDefn_SetIgnored(this.swigCPtr, this, bIgnored);
  }

  public void SetJustify(final int justify) {
    ogrJNI.FieldDefn_SetJustify(this.swigCPtr, this, justify);
  }

  public void SetName(final String name) {
    ogrJNI.FieldDefn_SetName(this.swigCPtr, this, name);
  }

  public void SetPrecision(final int precision) {
    ogrJNI.FieldDefn_SetPrecision(this.swigCPtr, this, precision);
  }

  public void SetType(final int type) {
    ogrJNI.FieldDefn_SetType(this.swigCPtr, this, type);
  }

  public void SetWidth(final int width) {
    ogrJNI.FieldDefn_SetWidth(this.swigCPtr, this, width);
  }

}