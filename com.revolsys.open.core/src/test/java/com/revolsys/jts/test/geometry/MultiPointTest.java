package com.revolsys.jts.test.geometry;

import org.junit.Test;

public class MultiPointTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "MultiPoint.csv");
  }
}
