/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.gdal;

public class Transformer {
  protected static long getCPtr(final Transformer obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }
  protected static long getCPtrAndDisown(final Transformer obj) {
    if (obj != null)
    {
      obj.swigCMemOwn= false;
    }
    return getCPtr(obj);
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public Transformer(final Dataset src, final Dataset dst, final java.util.Vector options) {
    this(gdalJNI.new_Transformer(Dataset.getCPtr(src), src, Dataset.getCPtr(dst), dst, options), true);
  }

  protected Transformer(final long cPtr, final boolean cMemoryOwn) {
    if (cPtr == 0) {
      throw new RuntimeException();
    }
    this.swigCMemOwn = cMemoryOwn;
    this.swigCPtr = cPtr;
  }

  /* Ensure that the GC doesn't collect any parent instance set from Java */
  protected void addReference(final Object reference) {
  }

  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        gdalJNI.delete_Transformer(this.swigCPtr);
      }
      this.swigCPtr = 0;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    boolean equal = false;
    if (obj instanceof Transformer) {
      equal = ((Transformer)obj).swigCPtr == this.swigCPtr;
    }
    return equal;
  }

  @Override
  protected void finalize() {
    delete();
  }


  @Override
  public int hashCode() {
    return (int)this.swigCPtr;
  }

  public int TransformGeolocations(final Band xBand, final Band yBand, final Band zBand) {
    return gdalJNI.Transformer_TransformGeolocations__SWIG_3(this.swigCPtr, this, Band.getCPtr(xBand), xBand, Band.getCPtr(yBand), yBand, Band.getCPtr(zBand), zBand);
  }

  public int TransformGeolocations(final Band xBand, final Band yBand, final Band zBand, final ProgressCallback callback) {
    return gdalJNI.Transformer_TransformGeolocations__SWIG_1(this.swigCPtr, this, Band.getCPtr(xBand), xBand, Band.getCPtr(yBand), yBand, Band.getCPtr(zBand), zBand, callback);
  }

  public int TransformGeolocations(final Band xBand, final Band yBand, final Band zBand, final ProgressCallback callback, final java.util.Vector options) {
    return gdalJNI.Transformer_TransformGeolocations__SWIG_0(this.swigCPtr, this, Band.getCPtr(xBand), xBand, Band.getCPtr(yBand), yBand, Band.getCPtr(zBand), zBand, callback, options);
  }

  public int TransformPoint(final double[] argout, final int bDstToSrc, final double x, final double y) {
    return gdalJNI.Transformer_TransformPoint__SWIG_2(this.swigCPtr, this, argout, bDstToSrc, x, y);
  }

  public int TransformPoint(final double[] argout, final int bDstToSrc, final double x, final double y, final double z) {
    return gdalJNI.Transformer_TransformPoint__SWIG_1(this.swigCPtr, this, argout, bDstToSrc, x, y, z);
  }

  public int TransformPoint(final int bDstToSrc, final double[] inout) {
    return gdalJNI.Transformer_TransformPoint__SWIG_0(this.swigCPtr, this, bDstToSrc, inout);
  }

  public int TransformPoints(final int bDstToSrc, final double[][] nCount, final int[] panSuccess) {
    return gdalJNI.Transformer_TransformPoints(this.swigCPtr, this, bDstToSrc, nCount, panSuccess);
  }

}
