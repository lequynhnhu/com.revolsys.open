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

import java.util.Collection;
import java.util.List;

import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.TopologyException;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Indexing is used to improve performance.
 * In the most common use case, validation stops after a single 
 * non-noded intersection is detected, 
 * but the class can be requested to detect all intersections
 * by using the {@link #setFindAllIntersections(boolean)} method.
 * <p>
 * The validator does not check for a-b-a topology collapse situations.
 * <p> 
 * The validator does not check for endpoint-interior vertex intersections.
 * This should not be a problem, since the JTS noders should be
 * able to compute intersections between vertices correctly.
 * <p>
 * The client may either test the {@link #isValid()} condition, 
 * or request that a suitable {@link TopologyException} be thrown.
 *
 * @version 1.7
 */
public class FastNodingValidator {
  private final LineIntersector li = new RobustLineIntersector();

  private final Collection segStrings;

  private boolean findAllIntersections = false;

  private InteriorIntersectionFinder segInt = null;

  private boolean isValid = true;

  /**
   * Creates a new noding validator for a given set of linework.
   * 
   * @param segStrings a collection of {@link SegmentString}s
   */
  public FastNodingValidator(final Collection segStrings) {
    this.segStrings = segStrings;
  }

  private void checkInteriorIntersections() {
    /**
     * MD - It may even be reliable to simply check whether 
     * end segments (of SegmentStrings) have an interior intersection,
     * since noding should have split any true interior intersections already.
     */
    isValid = true;
    segInt = new InteriorIntersectionFinder(li);
    segInt.setFindAllIntersections(findAllIntersections);
    final MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(segInt);
    noder.computeNodes(segStrings);
    if (segInt.hasIntersection()) {
      isValid = false;
      return;
    }
  }

  /**
   * Checks for an intersection and throws
   * a TopologyException if one is found.
   *
   * @throws TopologyException if an intersection is found
   */
  public void checkValid() {
    execute();
    if (!isValid) {
      final String errorMessage = getErrorMessage();
      final Point interiorIntersection = segInt.getInteriorIntersection();
      throw new TopologyException(errorMessage, interiorIntersection);
    }
  }

  private void execute() {
    if (segInt != null) {
      return;
    }
    checkInteriorIntersections();
  }

  /**
   * Returns an error message indicating the segments containing
   * the intersection.
   * 
   * @return an error message documenting the intersection location
   */
  public String getErrorMessage() {
    if (isValid) {
      return "no intersections found";
    }

    final Point[] intSegs = segInt.getIntersectionSegments();
    return "found non-noded intersection between "
      + WktWriter.lineString(intSegs[0], intSegs[1]) + " and "
      + WktWriter.lineString(intSegs[2], intSegs[3]);
  }

  public List getIntersections() {
    return segInt.getIntersections();
  }

  /**
   * Checks for an intersection and 
   * reports if one is found.
   * 
   * @return true if the arrangement contains an interior intersection
   */
  public boolean isValid() {
    execute();
    return isValid;
  }

  public void setFindAllIntersections(final boolean findAllIntersections) {
    this.findAllIntersections = findAllIntersections;
  }

}