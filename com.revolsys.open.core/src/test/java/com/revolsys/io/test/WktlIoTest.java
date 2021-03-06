package com.revolsys.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WktlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("WKT Geometry");
    IoTestSuite.addGeometryTestSuites(suite, "WKT", IoTestSuite.class,
      "doWriteReadTest", "wkt");
    return suite;
  }
}
