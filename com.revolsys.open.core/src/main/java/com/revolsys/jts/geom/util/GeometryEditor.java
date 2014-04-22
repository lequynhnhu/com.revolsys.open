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

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.util.Assert;

/**
 * A class which supports creating new {@link Geometry}s 
 * which are modifications of existing ones.
 * Geometry objects are intended to be treated as immutable.
 * This class "modifies" Geometrys
 * by traversing them, applying a user-defined
 * {@link GeometryEditorOperation} or {@link CoordinateOperation}  
 * and creating new Geometrys with the same structure but
 * (possibly) modified components.
 * <p>
 * Examples of the kinds of modifications which can be made are:
 * <ul>
 * <li>the values of the coordinates may be changed.
 *     The editor does not check whether changing coordinate values makes the result Geometry invalid
 * <li>the coordinate lists may be changed
 *     (e.g. by adding or deleting coordinates).
 *     The modifed coordinate lists must be consistent with their original parent component
 *     (e.g. a <tt>LinearRing</tt> must always have at least 4 coordinates, and the first and last
 *     coordinate must be equal)
 * <li>components of the original geometry may be deleted
 * (   e.g. holes may be removed from a Polygon, or LineStrings removed from a MultiLineString).
 *     Deletions will be propagated up the component tree appropriately.
 * </ul>
 * All changes must be consistent with the original Geometry's structure
 * (e.g. a <tt>Polygon</tt> cannot be collapsed into a <tt>LineString</tt>).
 * If changing the structure is required, use a {@link GeometryTransformer}.
 * <p>
 * This class supports the case where an edited Geometry needs to
 * be created under a new GeometryFactory, via the {@link #GeometryEditor(GeometryFactory)}
 * constructor.  
 * Examples of situations where this is required is if the geometry is 
 * transformed to a new SRID and/or a new PrecisionModel.
 * <p>
 * The resulting Geometry is not checked for validity.
 * If validity needs to be enforced, the new Geometry's 
 * {@link Geometry#isValid} method should be called.
 *
 * @see GeometryTransformer
 * @see Geometry#isValid
 *
 * @version 1.7
 */
public class GeometryEditor {
  /**
   * A {@link GeometryEditorOperation} which edits the coordinate list of a {@link Geometry}.
   * Operates on Geometry subclasses which contains a single coordinate list.
   */
  public abstract static class CoordinateOperation implements
    GeometryEditorOperation {
    /**
     * Edits the array of {@link Coordinates}s from a {@link Geometry}.
     * <p>
     * If it is desired to preserve the immutability of Geometrys,
     * if the coordinates are changed a new array should be created
     * and returned.
     *
     * @param coordinates the coordinate array to operate on
     * @param geometry the geometry containing the coordinate list
     * @return an edited coordinate array (which may be the same as the input)
     */
    public abstract Coordinates[] edit(Coordinates[] coordinates,
      Geometry geometry);

    @Override
    public final Geometry edit(final Geometry geometry,
      final GeometryFactory factory) {
      if (geometry instanceof LinearRing) {
        return factory.linearRing(edit(geometry.getCoordinateArray(), geometry));
      }

      if (geometry instanceof LineString) {
        return factory.lineString(edit(geometry.getCoordinateArray(), geometry));
      }

      if (geometry instanceof Point) {
        final Coordinates[] newCoordinates = edit(
          geometry.getCoordinateArray(), geometry);

        return factory.point((newCoordinates.length > 0) ? newCoordinates[0]
          : null);
      }

      return geometry;
    }
  }

  /**
   * A {@link GeometryEditorOperation} which edits the {@link CoordinatesList}
   * of a {@link Geometry}.
   * Operates on Geometry subclasses which contains a single coordinate list.
   */
  public abstract static class CoordinateSequenceOperation implements
    GeometryEditorOperation {
    /**
     * Edits a {@link CoordinatesList} from a {@link Geometry}.
     *
     * @param coordseq the coordinate array to operate on
     * @param geometry the geometry containing the coordinate list
     * @return an edited coordinate sequence (which may be the same as the input)
     */
    public abstract CoordinatesList edit(CoordinatesList coordSeq,
      Geometry geometry);

    @Override
    public final Geometry edit(final Geometry geometry,
      final GeometryFactory factory) {
      if (geometry instanceof LinearRing) {
        return factory.linearRing(edit(
          ((LinearRing)geometry).getCoordinatesList(), geometry));
      }

      if (geometry instanceof LineString) {
        return factory.lineString(edit(
          ((LineString)geometry).getCoordinatesList(), geometry));
      }

      if (geometry instanceof Point) {
        return factory.point(edit(((Point)geometry).getCoordinatesList(),
          geometry));
      }

      return geometry;
    }
  }

  /**
   * A interface which specifies an edit operation for Geometries.
   *
   * @version 1.7
   */
  public interface GeometryEditorOperation {
    /**
     * Edits a Geometry by returning a new Geometry with a modification.
     * The returned Geometry might be the same as the Geometry passed in.
     *
     * @param geometry the Geometry to modify
     * @param factory the factory with which to construct the modified Geometry
     * (may be different to the factory of the input geometry)
     * @return a new Geometry which is a modification of the input Geometry
     * @return null if the Geometry is to be deleted completely
     */
    Geometry edit(Geometry geometry, GeometryFactory factory);
  }

  /**
   * A GeometryEditorOperation which does not modify
   * the input geometry.
   * This can be used for simple changes of 
   * GeometryFactory (including PrecisionModel and SRID).
   * 
   * @author mbdavis
   *
   */
  public static class NoOpGeometryOperation implements GeometryEditorOperation {
    @Override
    public Geometry edit(final Geometry geometry, final GeometryFactory factory) {
      return geometry;
    }
  }

  /**
   * The factory used to create the modified Geometry.
   * If <tt>null</tt> the GeometryFactory of the input is used.
   */
  private GeometryFactory factory = null;

  /**
   * Creates a new GeometryEditor object which will create
   * edited {@link Geometry}s with the same {@link GeometryFactory} as the input Geometry.
   */
  public GeometryEditor() {
  }

  /**
   * Creates a new GeometryEditor object which will create
   * edited {@link Geometry}s with the given {@link GeometryFactory}.
   *
   * @param factory the GeometryFactory to create  edited Geometrys with
   */
  public GeometryEditor(final GeometryFactory factory) {
    this.factory = factory;
  }

  /**
   * Edit the input {@link Geometry} with the given edit operation.
   * Clients can create subclasses of {@link GeometryEditorOperation} or
   * {@link CoordinateOperation} to perform required modifications.
   *
   * @param geometry the Geometry to edit
   * @param operation the edit operation to carry out
   * @return a new {@link Geometry} which is the result of the editing (which may be empty)
   */
  public Geometry edit(final Geometry geometry,
    final GeometryEditorOperation operation) {
    // nothing to do
    if (geometry == null) {
      return null;
    }

    // if client did not supply a GeometryFactory, use the one from the input
    // Geometry
    if (factory == null) {
      factory = geometry.getGeometryFactory();
    }

    if (geometry instanceof GeometryCollection) {
      return editGeometryCollection((GeometryCollection)geometry, operation);
    }

    if (geometry instanceof Polygon) {
      return editPolygon((Polygon)geometry, operation);
    }

    if (geometry instanceof Point) {
      return operation.edit(geometry, factory);
    }

    if (geometry instanceof LineString) {
      return operation.edit(geometry, factory);
    }

    Assert.shouldNeverReachHere("Unsupported Geometry class: "
      + geometry.getClass().getName());
    return null;
  }

  private GeometryCollection editGeometryCollection(
    final GeometryCollection collection, final GeometryEditorOperation operation) {
    // first edit the entire collection
    // MD - not sure why this is done - could just check original collection?
    final GeometryCollection collectionForType = (GeometryCollection)operation.edit(
      collection, factory);

    // edit the component geometries
    final ArrayList geometries = new ArrayList();
    for (int i = 0; i < collectionForType.getGeometryCount(); i++) {
      final Geometry geometry = edit(collectionForType.getGeometry(i),
        operation);
      if (geometry == null || geometry.isEmpty()) {
        continue;
      }
      geometries.add(geometry);
    }

    if (collectionForType.getClass() == MultiPoint.class) {
      return factory.createMultiPoint((Point[])geometries.toArray(new Point[] {}));
    }
    if (collectionForType.getClass() == MultiLineString.class) {
      return factory.createMultiLineString((LineString[])geometries.toArray(new LineString[] {}));
    }
    if (collectionForType.getClass() == MultiPolygon.class) {
      return factory.createMultiPolygon((Polygon[])geometries.toArray(new Polygon[] {}));
    }
    return factory.geometryCollection((Geometry[])geometries.toArray(new Geometry[] {}));
  }

  private Polygon editPolygon(final Polygon polygon,
    final GeometryEditorOperation operation) {
    Polygon newPolygon = (Polygon)operation.edit(polygon, factory);
    // create one if needed
    if (newPolygon == null) {
      newPolygon = factory.polygon();
    }
    if (newPolygon.isEmpty()) {
      // RemoveSelectedPlugIn relies on this behaviour. [Jon Aquino]
      return newPolygon;
    }

    final LinearRing shell = (LinearRing)edit(newPolygon.getExteriorRing(),
      operation);
    if (shell == null || shell.isEmpty()) {
      // RemoveSelectedPlugIn relies on this behaviour. [Jon Aquino]
      return factory.polygon();
    }

    final List<LinearRing> rings = new ArrayList<>();
    rings.add(shell);
    for (int i = 0; i < newPolygon.getNumInteriorRing(); i++) {
      final LinearRing hole = (LinearRing)edit(newPolygon.getInteriorRing(i),
        operation);
      if (hole == null || hole.isEmpty()) {
        continue;
      }
      rings.add(hole);
    }

    return factory.polygon(rings);
  }
}
