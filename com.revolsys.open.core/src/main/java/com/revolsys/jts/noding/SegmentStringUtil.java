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

package com.revolsys.jts.noding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.util.LinearComponentExtracter;

/**
 * Utility methods for processing {@link SegmentString}s.
 * 
 * @author Martin Davis
 *
 */
public class SegmentStringUtil {
  /**
   * Extracts all linear components from a given {@link Geometry}
   * to {@link SegmentString}s.
   * The SegmentString data item is set to be the source Geometry.
   * 
   * @param geom the geometry to extract from
   * @return a List of SegmentStrings
   */
  public static List extractSegmentStrings(final Geometry geom) {
    final List segStr = new ArrayList();
    final List lines = LinearComponentExtracter.getLines(geom);
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();
      final Coordinates[] pts = line.getCoordinateArray();
      segStr.add(new NodedSegmentString(pts, geom));
    }
    return segStr;
  }

  public static String toString(final List segStrings) {
    final StringBuffer buf = new StringBuffer();
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString segStr = (SegmentString)i.next();
      buf.append(segStr.toString());
      buf.append("\n");

    }
    return buf.toString();
  }
}