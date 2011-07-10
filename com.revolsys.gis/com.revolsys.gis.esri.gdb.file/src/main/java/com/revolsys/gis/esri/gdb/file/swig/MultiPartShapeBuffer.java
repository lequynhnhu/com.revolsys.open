/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class MultiPartShapeBuffer extends ShapeBuffer {
  private long swigCPtr;

  protected MultiPartShapeBuffer(long cPtr, boolean cMemoryOwn) {
    super(EsriFileGdbJNI.SWIGMultiPartShapeBufferUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MultiPartShapeBuffer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_MultiPartShapeBuffer(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public int Setup(ShapeType shapeType, int numParts, int numPoints, int numCurves) {
    return EsriFileGdbJNI.MultiPartShapeBuffer_Setup__SWIG_0(swigCPtr, this, shapeType.swigValue(), numParts, numPoints, numCurves);
  }

  public int Setup(ShapeType shapeType, int numParts, int numPoints) {
    return EsriFileGdbJNI.MultiPartShapeBuffer_Setup__SWIG_1(swigCPtr, this, shapeType.swigValue(), numParts, numPoints);
  }

  public int CalculateExtent() {
    return EsriFileGdbJNI.MultiPartShapeBuffer_CalculateExtent(swigCPtr, this);
  }

  public int PackCurves() {
    return EsriFileGdbJNI.MultiPartShapeBuffer_PackCurves(swigCPtr, this);
  }

  public PointArray getPoints() {
  return new PointArray(EsriFileGdbJNI.MultiPartShapeBuffer_getPoints(swigCPtr, this), false);
}

  public UnsignedCharArray getCurves() {
  return new UnsignedCharArray(EsriFileGdbJNI.MultiPartShapeBuffer_getCurves(swigCPtr, this), false);
}

  public DoubleArray getExtent() {
  return new DoubleArray(EsriFileGdbJNI.MultiPartShapeBuffer_getExtent(swigCPtr, this), false);
}

  public DoubleArray getMExtent() {
  return new DoubleArray(EsriFileGdbJNI.MultiPartShapeBuffer_getMExtent(swigCPtr, this), false);
}

  public DoubleArray getZExtent() {
  return new DoubleArray(EsriFileGdbJNI.MultiPartShapeBuffer_getZExtent(swigCPtr, this), false);
}

  public DoubleArray getZs() {
  return new DoubleArray(EsriFileGdbJNI.MultiPartShapeBuffer_getZs(swigCPtr, this), false);
}

  public DoubleArray getMs() {
  return new DoubleArray(EsriFileGdbJNI.MultiPartShapeBuffer_getMs(swigCPtr, this), false);
}

  public IntArray getIDs() {
  return new IntArray(EsriFileGdbJNI.MultiPartShapeBuffer_getIDs(swigCPtr, this), false);
}

  public IntArray getParts() {
  return new IntArray(EsriFileGdbJNI.MultiPartShapeBuffer_getParts(swigCPtr, this), false);
}

  public int getNumPoints() {
    return EsriFileGdbJNI.MultiPartShapeBuffer_getNumPoints(swigCPtr, this);
  }

  public int getNumParts() {
    return EsriFileGdbJNI.MultiPartShapeBuffer_getNumParts(swigCPtr, this);
  }

  public int getNumCurves() {
    return EsriFileGdbJNI.MultiPartShapeBuffer_getNumCurves(swigCPtr, this);
  }

  public MultiPartShapeBuffer() {
    this(EsriFileGdbJNI.new_MultiPartShapeBuffer(), true);
  }

}
