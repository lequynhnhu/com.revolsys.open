/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.ogr;

public class StyleTable {
  protected static long getCPtr(final StyleTable obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }
  protected static long getCPtrAndDisown(final StyleTable obj) {
    if (obj != null)
    {
      obj.swigCMemOwn= false;
    }
    return getCPtr(obj);
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public StyleTable() {
    this(ogrJNI.new_StyleTable(), true);
  }

  protected StyleTable(final long cPtr, final boolean cMemoryOwn) {
    if (cPtr == 0) {
      throw new RuntimeException();
    }
    this.swigCMemOwn = cMemoryOwn;
    this.swigCPtr = cPtr;
  }

  /* Ensure that the GC doesn't collect any parent instance set from Java */
  protected void addReference(final Object reference) {
  }

  public int AddStyle(final String pszName, final String pszStyleString) {
    return ogrJNI.StyleTable_AddStyle(this.swigCPtr, this, pszName, pszStyleString);
  }

  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        ogrJNI.delete_StyleTable(this.swigCPtr);
      }
      this.swigCPtr = 0;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    boolean equal = false;
    if (obj instanceof StyleTable) {
      equal = ((StyleTable)obj).swigCPtr == this.swigCPtr;
    }
    return equal;
  }


  @Override
  protected void finalize() {
    delete();
  }

  public String Find(final String pszName) {
    return ogrJNI.StyleTable_Find(this.swigCPtr, this, pszName);
  }

  public String GetLastStyleName() {
    return ogrJNI.StyleTable_GetLastStyleName(this.swigCPtr, this);
  }

  public String GetNextStyle() {
    return ogrJNI.StyleTable_GetNextStyle(this.swigCPtr, this);
  }

  @Override
  public int hashCode() {
    return (int)this.swigCPtr;
  }

  public int LoadStyleTable(final String utf8_path) {
    return ogrJNI.StyleTable_LoadStyleTable(this.swigCPtr, this, utf8_path);
  }

  public void ResetStyleStringReading() {
    ogrJNI.StyleTable_ResetStyleStringReading(this.swigCPtr, this);
  }

  public int SaveStyleTable(final String utf8_path) {
    return ogrJNI.StyleTable_SaveStyleTable(this.swigCPtr, this, utf8_path);
  }

}
