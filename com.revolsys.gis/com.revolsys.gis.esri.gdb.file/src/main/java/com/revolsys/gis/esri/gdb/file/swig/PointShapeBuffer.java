/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class PointShapeBuffer extends ShapeBuffer {
  private long swigCPtr;

  protected PointShapeBuffer(long cPtr, boolean cMemoryOwn) {
    super(EsriFileGdbJNI.SWIGPointShapeBufferUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(PointShapeBuffer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_PointShapeBuffer(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public int Setup(ShapeType shapeType) {
    return EsriFileGdbJNI.PointShapeBuffer_Setup(swigCPtr, this, shapeType.swigValue());
  }

  public double getM() {
    return EsriFileGdbJNI.PointShapeBuffer_getM(swigCPtr, this);
  }

  public double getZ() {
    return EsriFileGdbJNI.PointShapeBuffer_getZ(swigCPtr, this);
  }

  public Point getPoint() {
    return new Point(EsriFileGdbJNI.PointShapeBuffer_getPoint(swigCPtr, this), true);
  }

  public int getID() {
    return EsriFileGdbJNI.PointShapeBuffer_getID(swigCPtr, this);
  }

  public PointShapeBuffer() {
    this(EsriFileGdbJNI.new_PointShapeBuffer(), true);
  }

}
