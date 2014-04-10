/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class CoordinateSequencesTest extends TestCase {

  private static CoordinatesList createTestSequence(
    final CoordinateSequenceFactory csFactory, final int size, final int dim) {
    final CoordinatesList cs = csFactory.create(size, dim);
    // initialize with a data signature where coords look like [1, 10, 100, ...]
    for (int i = 0; i < size; i++) {
      for (int d = 0; d < dim; d++) {
        cs.setOrdinate(i, d, i * Math.pow(10, d));
      }
    }
    return cs;
  }

  public static void main(final String args[]) {
    TestRunner.run(CoordinateSequencesTest.class);
  }

  private final PrecisionModel precisionModel = new PrecisionModel();

  private final GeometryFactory geometryFactory = new GeometryFactory(
    this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public CoordinateSequencesTest(final String name) {
    super(name);
  }

  public void testCopyToLargerDim() throws Exception {
    final DoubleCoordinatesListFactory csFactory = new DoubleCoordinatesListFactory();
    final CoordinatesList cs2D = createTestSequence(csFactory, 10, 2);
    final CoordinatesList cs3D = csFactory.create(10, 3);
    CoordinateSequences.copy(cs2D, 0, cs3D, 0, cs3D.size());
    assertTrue(CoordinateSequences.isEqual(cs2D, cs3D));
  }

  public void testCopyToSmallerDim() throws Exception {
    final DoubleCoordinatesListFactory csFactory = new DoubleCoordinatesListFactory();
    final CoordinatesList cs3D = createTestSequence(csFactory, 10, 3);
    final CoordinatesList cs2D = csFactory.create(10, 2);
    CoordinateSequences.copy(cs3D, 0, cs2D, 0, cs2D.size());
    assertTrue(CoordinateSequences.isEqual(cs2D, cs3D));
  }

}
