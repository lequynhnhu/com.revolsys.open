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
package com.revolsys.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;

/**
 * Extracts the components of a given type from a {@link Geometry}.
 *
 * @version 1.7
 */
public class GeometryExtracter {

  /**
   * Extracts the components of type <tt>clz</tt> from a {@link Geometry}
   * and returns them in a {@link List}.
   * 
   * @param geom the geometry from which to extract
   */
  public static List<Geometry> extract(final Geometry geom, final Class<?> clz) {
    return extract(geom, clz, new ArrayList<Geometry>());
  }

  /**
   * Extracts the components of type <tt>clz</tt> from a {@link Geometry}
   * and adds them to the provided {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @param list the list to add the extracted elements to
   */
  public static List<Geometry> extract(final Geometry geometry,
    final Class<?> clz, final List<Geometry> list) {
    for (final Geometry part : geometry.geometries()) {
      if (clz.isAssignableFrom(part.getClass())) {
        list.add(part);
      }
    }

    return list;
  }

}
