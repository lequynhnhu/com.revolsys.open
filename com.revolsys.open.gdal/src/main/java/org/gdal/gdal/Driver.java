/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.gdal;

import java.util.Vector;

import org.gdal.gdalconst.gdalconstConstants;

public class Driver extends MajorObject {
  protected static long getCPtr(final Driver obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }

  private static Vector StringArrayToVector(final String[] options)
  {
    if (options == null) {
      return null;
    }
    final Vector v = new Vector();
    for (final String option : options) {
      v.addElement(option);
    }
    return v;
  }

  private long swigCPtr;

  protected Driver(final long cPtr, final boolean cMemoryOwn) {
    super(gdalJNI.Driver_SWIGUpcast(cPtr), cMemoryOwn);
    this.swigCPtr = cPtr;
  }


  public int CopyFiles(final String newName, final String oldName) {
    return gdalJNI.Driver_CopyFiles(this.swigCPtr, this, newName, oldName);
  }

  public Dataset Create(final String utf8_path, final int xsize, final int ysize) {
    final long cPtr = gdalJNI.Driver_Create__SWIG_3(this.swigCPtr, this, utf8_path, xsize, ysize);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset Create(final String utf8_path, final int xsize, final int ysize, final int bands) {
    final long cPtr = gdalJNI.Driver_Create__SWIG_2(this.swigCPtr, this, utf8_path, xsize, ysize, bands);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset Create(final String utf8_path, final int xsize, final int ysize, final int bands, final int eType) {
    final long cPtr = gdalJNI.Driver_Create__SWIG_1(this.swigCPtr, this, utf8_path, xsize, ysize, bands, eType);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset Create(final String utf8_path, final int xsize, final int ysize, final int bands, final int eType, final java.util.Vector options) {
    final long cPtr = gdalJNI.Driver_Create__SWIG_0(this.swigCPtr, this, utf8_path, xsize, ysize, bands, eType, options);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset Create(final String name, final int xsize, final int ysize, final int bands, final int eType, final String[] options) {
    return Create(name, xsize, ysize, bands, eType, StringArrayToVector(options));
  }


  public Dataset Create(final String name, final int xsize, final int ysize, final int bands, final String[] options) {
    return Create(name, xsize, ysize, bands, gdalconstConstants.GDT_Byte, StringArrayToVector(options));
  }

  public Dataset CreateCopy(final String utf8_path, final Dataset src) {
    final long cPtr = gdalJNI.Driver_CreateCopy__SWIG_4(this.swigCPtr, this, utf8_path, Dataset.getCPtr(src), src);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset CreateCopy(final String utf8_path, final Dataset src, final int strict) {
    final long cPtr = gdalJNI.Driver_CreateCopy__SWIG_3(this.swigCPtr, this, utf8_path, Dataset.getCPtr(src), src, strict);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset CreateCopy(final String utf8_path, final Dataset src, final int strict, final java.util.Vector options) {
    final long cPtr = gdalJNI.Driver_CreateCopy__SWIG_2(this.swigCPtr, this, utf8_path, Dataset.getCPtr(src), src, strict, options);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset CreateCopy(final String utf8_path, final Dataset src, final int strict, final java.util.Vector options, final ProgressCallback callback) {
    final long cPtr = gdalJNI.Driver_CreateCopy__SWIG_0(this.swigCPtr, this, utf8_path, Dataset.getCPtr(src), src, strict, options, callback);
    return cPtr == 0 ? null : new Dataset(cPtr, true);
  }

  public Dataset CreateCopy(final String name, final Dataset src, final int strict, final String[] options) {
    return CreateCopy(name, src, strict, StringArrayToVector(options), null);
  }

  public Dataset CreateCopy(final String name, final Dataset src, final String[] options) {
    return CreateCopy(name, src, 1, StringArrayToVector(options), null);
  }

  public Dataset CreateCopy(final String name, final Dataset src, final Vector options) {
    return CreateCopy(name, src, 1, options, null);
  }

  @Override
  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        throw new UnsupportedOperationException("C++ destructor does not have public access");
      }
      this.swigCPtr = 0;
    }
    super.delete();
  }

  public int Delete(final String utf8_path) {
    return gdalJNI.Driver_Delete(this.swigCPtr, this, utf8_path);
  }

  public void Deregister() {
    gdalJNI.Driver_Deregister(this.swigCPtr, this);
  }

  public String getHelpTopic() {
    return gdalJNI.Driver_HelpTopic_get(this.swigCPtr, this);
  }

  public String getLongName() {
    return gdalJNI.Driver_LongName_get(this.swigCPtr, this);
  }

  public String getShortName() {
    return gdalJNI.Driver_ShortName_get(this.swigCPtr, this);
  }

  public int Register() {
    return gdalJNI.Driver_Register(this.swigCPtr, this);
  }

  public int Rename(final String newName, final String oldName) {
    return gdalJNI.Driver_Rename(this.swigCPtr, this, newName, oldName);
  }

}
