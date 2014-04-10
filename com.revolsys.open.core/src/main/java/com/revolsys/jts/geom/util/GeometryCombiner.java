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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygonal;

/**
 * Combines {@link Geometry}s
 * to produce a {@link GeometryCollection} of the most appropriate type.
 * Input geometries which are already collections
 * will have their elements extracted first.
 * No validation of the result geometry is performed.
 * (The only case where invalidity is possible is where {@link Polygonal} geometries
 * are combined and result in a self-intersection).
 * 
 * @author mbdavis
 * @see GeometryFactory#buildGeometry
 */
public class GeometryCombiner {
  /**
   * Combines a collection of geometries.
   * 
   * @param geoms the geometries to combine
   * @return the combined geometry
   */
  public static Geometry combine(final Collection geoms) {
    final GeometryCombiner combiner = new GeometryCombiner(geoms);
    return combiner.combine();
  }

  /**
   * Combines two geometries.
   * 
   * @param g0 a geometry to combine
   * @param g1 a geometry to combine
   * @return the combined geometry
   */
  public static Geometry combine(final Geometry g0, final Geometry g1) {
    final GeometryCombiner combiner = new GeometryCombiner(createList(g0, g1));
    return combiner.combine();
  }

  /**
   * Combines three geometries.
   * 
   * @param g0 a geometry to combine
   * @param g1 a geometry to combine
   * @param g2 a geometry to combine
   * @return the combined geometry
   */
  public static Geometry combine(final Geometry g0, final Geometry g1,
    final Geometry g2) {
    final GeometryCombiner combiner = new GeometryCombiner(createList(g0, g1,
      g2));
    return combiner.combine();
  }

  /**
   * Creates a list from two items
   * 
   * @param obj0
   * @param obj1
   * @return a List containing the two items
   */
  private static List createList(final Object obj0, final Object obj1) {
    final List list = new ArrayList();
    list.add(obj0);
    list.add(obj1);
    return list;
  }

  /**
   * Creates a list from two items
   * 
   * @param obj0
   * @param obj1
   * @return a List containing the two items
   */
  private static List createList(final Object obj0, final Object obj1,
    final Object obj2) {
    final List list = new ArrayList();
    list.add(obj0);
    list.add(obj1);
    list.add(obj2);
    return list;
  }

  /**
   * Extracts the GeometryFactory used by the geometries in a collection
   * 
   * @param geoms
   * @return a GeometryFactory
   */
  public static GeometryFactory extractFactory(final Collection geoms) {
    if (geoms.isEmpty()) {
      return null;
    }
    return ((Geometry)geoms.iterator().next()).getGeometryFactory();
  }

  private final GeometryFactory geomFactory;

  private final boolean skipEmpty = false;

  private final Collection inputGeoms;

  /**
   * Creates a new combiner for a collection of geometries
   * 
   * @param geoms the geometries to combine
   */
  public GeometryCombiner(final Collection geoms) {
    geomFactory = extractFactory(geoms);
    this.inputGeoms = geoms;
  }

  /**
   * Computes the combination of the input geometries
   * to produce the most appropriate {@link Geometry} or {@link GeometryCollection}
   * 
   * @return a Geometry which is the combination of the inputs
   */
  public Geometry combine() {
    final List elems = new ArrayList();
    for (final Iterator i = inputGeoms.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      extractElements(g, elems);
    }

    if (elems.size() == 0) {
      if (geomFactory != null) {
        // return an empty GC
        return geomFactory.createGeometryCollection();
      }
      return null;
    }
    // return the "simplest possible" geometry
    return geomFactory.buildGeometry(elems);
  }

  private void extractElements(final Geometry geom, final List elems) {
    if (geom == null) {
      return;
    }

    for (int i = 0; i < geom.getNumGeometries(); i++) {
      final Geometry elemGeom = geom.getGeometry(i);
      if (skipEmpty && elemGeom.isEmpty()) {
        continue;
      }
      elems.add(elemGeom);
    }
  }

}
