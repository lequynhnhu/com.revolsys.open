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
package com.revolsys.jts.operation.buffer.validate;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * Validates that the result of a buffer operation
 * is geometrically correct, within a computed tolerance.
 * <p>
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 * <p>
 * This test may be (much) more expensive than the original
 * buffer computation.
 *
 * @author Martin Davis
 */
public class BufferResultValidator {
  public static boolean isValid(final Geometry g, final double distance,
    final Geometry result) {
    final BufferResultValidator validator = new BufferResultValidator(g,
      distance, result);
    if (validator.isValid()) {
      return true;
    }
    return false;
  }

  /**
   * Checks whether the geometry buffer is valid,
   * and returns an error message if not.
   *
   * @param g
   * @param distance
   * @param result
   * @return an appropriate error message
   * or null if the buffer is valid
   */
  public static String isValidMsg(final Geometry g, final double distance,
    final Geometry result) {
    final BufferResultValidator validator = new BufferResultValidator(g,
      distance, result);
    if (!validator.isValid()) {
      return validator.getErrorMessage();
    }
    return null;
  }

  private static boolean VERBOSE = false;

  /**
   * Maximum allowable fraction of buffer distance the
   * actual distance can differ by.
   * 1% sometimes causes an error - 1.2% should be safe.
   */
  private static final double MAX_ENV_DIFF_FRAC = .012;

  private final Geometry input;

  private final double distance;

  private final Geometry result;

  private boolean isValid = true;

  private String errorMsg = null;

  private Point errorLocation = null;

  private Geometry errorIndicator = null;

  public BufferResultValidator(final Geometry input, final double distance,
    final Geometry result) {
    this.input = input;
    this.distance = distance;
    this.result = result;
  }

  private void checkArea() {
    final double inputArea = this.input.getArea();
    final double resultArea = this.result.getArea();

    if (this.distance > 0.0 && inputArea > resultArea) {
      this.isValid = false;
      this.errorMsg = "Area of positive buffer is smaller than input";
      this.errorIndicator = this.result;
    }
    if (this.distance < 0.0 && inputArea < resultArea) {
      this.isValid = false;
      this.errorMsg = "Area of negative buffer is larger than input";
      this.errorIndicator = this.result;
    }
    report("Area");
  }

  private void checkDistance() {
    final BufferDistanceValidator distValid = new BufferDistanceValidator(
      this.input, this.distance, this.result);
    if (!distValid.isValid()) {
      this.isValid = false;
      this.errorMsg = distValid.getErrorMessage();
      this.errorLocation = distValid.getErrorLocation();
      this.errorIndicator = distValid.getErrorIndicator();
    }
    report("Distance");
  }

  private void checkEnvelope() {
    if (this.distance < 0.0) {
      return;
    }

    double padding = this.distance * MAX_ENV_DIFF_FRAC;
    if (padding == 0.0) {
      padding = 0.001;
    }

    final BoundingBox expectedEnv = this.input.getBoundingBox().expand(this.distance);

    final BoundingBox bufEnv = this.result.getBoundingBox().expand(padding);

    if (!bufEnv.covers(expectedEnv)) {
      this.isValid = false;
      this.errorMsg = "Buffer envelope is incorrect";
      final GeometryFactory r = this.input.getGeometryFactory();
      this.errorIndicator = bufEnv.toGeometry();
    }
    report("BoundingBoxDoubleGf");
  }

  private void checkExpectedEmpty() {
    // can't check areal features
    if (this.input.getDimension() >= 2) {
      return;
    }
    // can't check positive distances
    if (this.distance > 0.0) {
      return;
    }

    // at this point can expect an empty result
    if (!this.result.isEmpty()) {
      this.isValid = false;
      this.errorMsg = "Result is non-empty";
      this.errorIndicator = this.result;
    }
    report("ExpectedEmpty");
  }

  private void checkPolygonal() {
    if (!(this.result instanceof Polygon || this.result instanceof MultiPolygon)) {
      this.isValid = false;
    }
    this.errorMsg = "Result is not polygonal";
    this.errorIndicator = this.result;
    report("Polygonal");
  }

  /**
   * Gets a geometry which indicates the location and nature of a validation failure.
   * <p>
   * If the failure is due to the buffer curve being too far or too close
   * to the input, the indicator is a line segment showing the location and size
   * of the discrepancy.
   *
   * @return a geometric error indicator
   * or null if no error was found
   */
  public Geometry getErrorIndicator() {
    return this.errorIndicator;
  }

  public Point getErrorLocation() {
    return this.errorLocation;
  }

  public String getErrorMessage() {
    return this.errorMsg;
  }

  public boolean isValid() {
    checkPolygonal();
    if (!this.isValid) {
      return this.isValid;
    }
    checkExpectedEmpty();
    if (!this.isValid) {
      return this.isValid;
    }
    checkEnvelope();
    if (!this.isValid) {
      return this.isValid;
    }
    checkArea();
    if (!this.isValid) {
      return this.isValid;
    }
    checkDistance();
    return this.isValid;
  }

  private void report(final String checkName) {
    if (!VERBOSE) {
      return;
    }
    System.out.println("Check " + checkName + ": "
        + (this.isValid ? "passed" : "FAILED"));
  }
}
