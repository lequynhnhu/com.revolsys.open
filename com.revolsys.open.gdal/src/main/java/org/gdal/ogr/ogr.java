/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.ogr;

import org.gdal.osr.SpatialReference;

public class ogr implements ogrConstants {
  public static Geometry ApproximateArcAngles(final double dfCenterX, final double dfCenterY, final double dfZ, final double dfPrimaryRadius, final double dfSecondaryAxis, final double dfRotation, final double dfStartAngle, final double dfEndAngle, final double dfMaxAngleStepSizeDegrees) {
    final long cPtr = ogrJNI.ApproximateArcAngles(dfCenterX, dfCenterY, dfZ, dfPrimaryRadius, dfSecondaryAxis, dfRotation, dfStartAngle, dfEndAngle, dfMaxAngleStepSizeDegrees);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry BuildPolygonFromEdges(final Geometry hLineCollection) {
    final long cPtr = ogrJNI.BuildPolygonFromEdges__SWIG_3(Geometry.getCPtr(hLineCollection), hLineCollection);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry BuildPolygonFromEdges(final Geometry hLineCollection, final int bBestEffort) {
    final long cPtr = ogrJNI.BuildPolygonFromEdges__SWIG_2(Geometry.getCPtr(hLineCollection), hLineCollection, bBestEffort);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry BuildPolygonFromEdges(final Geometry hLineCollection, final int bBestEffort, final int bAutoClose) {
    final long cPtr = ogrJNI.BuildPolygonFromEdges__SWIG_1(Geometry.getCPtr(hLineCollection), hLineCollection, bBestEffort, bAutoClose);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry BuildPolygonFromEdges(final Geometry hLineCollection, final int bBestEffort, final int bAutoClose, final double dfTolerance) {
    final long cPtr = ogrJNI.BuildPolygonFromEdges__SWIG_0(Geometry.getCPtr(hLineCollection), hLineCollection, bBestEffort, bAutoClose, dfTolerance);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry CreateGeometryFromGML(final String input_string) {
    final long cPtr = ogrJNI.CreateGeometryFromGML(input_string);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry CreateGeometryFromJson(final String input_string) {
    final long cPtr = ogrJNI.CreateGeometryFromJson(input_string);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry CreateGeometryFromWkb(final byte[] nLen) {
    final long cPtr = ogrJNI.CreateGeometryFromWkb__SWIG_1(nLen);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry CreateGeometryFromWkb(final byte[] nLen, final SpatialReference reference) {
    final long cPtr = ogrJNI.CreateGeometryFromWkb__SWIG_0(nLen, SpatialReference.getCPtr(reference), reference);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry CreateGeometryFromWkt(final String val) {
    final long cPtr = ogrJNI.CreateGeometryFromWkt__SWIG_1(val);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry CreateGeometryFromWkt(final String val, final SpatialReference reference) {
    final long cPtr = ogrJNI.CreateGeometryFromWkt__SWIG_0(val, SpatialReference.getCPtr(reference), reference);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static void DontUseExceptions() {
    ogrJNI.DontUseExceptions();
  }

  public static Geometry ForceToLineString(final Geometry geom_in) {
    final long cPtr = ogrJNI.ForceToLineString(Geometry.getCPtr(geom_in), geom_in);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry ForceToMultiLineString(final Geometry geom_in) {
    final long cPtr = ogrJNI.ForceToMultiLineString(Geometry.getCPtr(geom_in), geom_in);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry ForceToMultiPoint(final Geometry geom_in) {
    final long cPtr = ogrJNI.ForceToMultiPoint(Geometry.getCPtr(geom_in), geom_in);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry ForceToMultiPolygon(final Geometry geom_in) {
    final long cPtr = ogrJNI.ForceToMultiPolygon(Geometry.getCPtr(geom_in), geom_in);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static Geometry ForceToPolygon(final Geometry geom_in) {
    final long cPtr = ogrJNI.ForceToPolygon(Geometry.getCPtr(geom_in), geom_in);
    return cPtr == 0 ? null : new Geometry(cPtr, true);
  }

  public static java.util.Vector GeneralCmdLineProcessor(final java.util.Vector papszArgv) {
    return ogrJNI.GeneralCmdLineProcessor__SWIG_1(papszArgv);
  }

  public static java.util.Vector GeneralCmdLineProcessor(final java.util.Vector papszArgv, final int nOptions) {
    return ogrJNI.GeneralCmdLineProcessor__SWIG_0(papszArgv, nOptions);
  }

  public static String[] GeneralCmdLineProcessor(final String[] args)
  {
    return GeneralCmdLineProcessor(args, 0);
  }

  public static String[] GeneralCmdLineProcessor(String[] args, final int nOptions)
  {
    java.util.Vector vArgs = new java.util.Vector();
    int i;
    for(i=0;i<args.length;i++) {
      vArgs.addElement(args[i]);
    }

    vArgs = GeneralCmdLineProcessor(vArgs, nOptions);
    final java.util.Enumeration eArgs = vArgs.elements();
    args = new String[vArgs.size()];
    i = 0;
    while(eArgs.hasMoreElements())
    {
      final String arg = (String)eArgs.nextElement();
      args[i++] = arg;
    }

    return args;
  }

  public static String GeometryTypeToName(final int eType) {
    return ogrJNI.GeometryTypeToName(eType);
  }

  public static Driver GetDriver(final int driver_number) {
    final long cPtr = ogrJNI.GetDriver(driver_number);
    return cPtr == 0 ? null : new Driver(cPtr, false);
  }

  public static Driver GetDriverByName(final String name) {
    final long cPtr = ogrJNI.GetDriverByName(name);
    return cPtr == 0 ? null : new Driver(cPtr, false);
  }

  public static int GetDriverCount() {
    return ogrJNI.GetDriverCount();
  }

  public static String GetFieldTypeName(final int type) {
    return ogrJNI.GetFieldTypeName(type);
  }

  public static DataSource GetOpenDS(final int ds_number) {
    final long cPtr = ogrJNI.GetOpenDS(ds_number);
    return cPtr == 0 ? null : new DataSource(cPtr, false);
  }

  public static int GetOpenDSCount() {
    return ogrJNI.GetOpenDSCount();
  }

  public static DataSource Open(final String utf8_path) {
    final long cPtr = ogrJNI.Open__SWIG_1(utf8_path);
    return cPtr == 0 ? null : new DataSource(cPtr, true);
  }

  public static DataSource Open(final String filename, final boolean update)
  {
    return Open(filename, update?1:0);
  }

  public static DataSource Open(final String utf8_path, final int update) {
    final long cPtr = ogrJNI.Open__SWIG_0(utf8_path, update);
    return cPtr == 0 ? null : new DataSource(cPtr, true);
  }

  public static DataSource OpenShared(final String utf8_path) {
    final long cPtr = ogrJNI.OpenShared__SWIG_1(utf8_path);
    return cPtr == 0 ? null : new DataSource(cPtr, true);
  }

  public static DataSource OpenShared(final String utf8_path, final int update) {
    final long cPtr = ogrJNI.OpenShared__SWIG_0(utf8_path, update);
    return cPtr == 0 ? null : new DataSource(cPtr, true);
  }



  public static void RegisterAll() {
    ogrJNI.RegisterAll();
  }

  public static int SetGenerate_DB2_V72_BYTE_ORDER(final int bGenerate_DB2_V72_BYTE_ORDER) {
    return ogrJNI.SetGenerate_DB2_V72_BYTE_ORDER(bGenerate_DB2_V72_BYTE_ORDER);
  }

  public static void UseExceptions() {
    ogrJNI.UseExceptions();
  }

  /* Uninstanciable class */
  private ogr()
  {
  }

}
