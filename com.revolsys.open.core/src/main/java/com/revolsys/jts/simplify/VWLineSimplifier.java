package com.revolsys.jts.simplify;

import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Triangle;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * Simplifies a linestring (sequence of points) using the
 * Visvalingam-Whyatt algorithm.
 * The Visvalingam-Whyatt algorithm simplifies geometry
 * by removing vertices while trying to minimize the area changed.
 *
 * @version 1.7
 */
class VWLineSimplifier {
  static class VWVertex {
    public static VWLineSimplifier.VWVertex buildLine(final LineString pts) {
      VWLineSimplifier.VWVertex first = null;
      VWLineSimplifier.VWVertex prev = null;
      for (int i = 0; i < pts.getVertexCount(); i++) {
        final VWLineSimplifier.VWVertex v = new VWVertex(pts.getPoint(i));
        if (first == null) {
          first = v;
        }
        v.setPrev(prev);
        if (prev != null) {
          prev.setNext(v);
          prev.updateArea();
        }
        prev = v;
      }
      return first;
    }

    public static double MAX_AREA = Double.MAX_VALUE;

    private final Point pt;

    private VWLineSimplifier.VWVertex prev;

    private VWLineSimplifier.VWVertex next;

    private double area = MAX_AREA;

    private boolean isLive = true;

    public VWVertex(final Point pt) {
      this.pt = pt;
    }

    public double getArea() {
      return this.area;
    }

    public Point[] getCoordinates() {
      final CoordinateList coords = new CoordinateList();
      VWLineSimplifier.VWVertex curr = this;
      do {
        coords.add(curr.pt, false);
        curr = curr.next;
      } while (curr != null);
      return coords.toCoordinateArray();
    }

    public boolean isLive() {
      return this.isLive;
    }

    public VWLineSimplifier.VWVertex remove() {
      final VWLineSimplifier.VWVertex tmpPrev = this.prev;
      final VWLineSimplifier.VWVertex tmpNext = this.next;
      VWLineSimplifier.VWVertex result = null;
      if (this.prev != null) {
        this.prev.setNext(tmpNext);
        this.prev.updateArea();
        result = this.prev;
      }
      if (this.next != null) {
        this.next.setPrev(tmpPrev);
        this.next.updateArea();
        if (result == null) {
          result = this.next;
        }
      }
      this.isLive = false;
      return result;
    }

    public void setNext(final VWLineSimplifier.VWVertex next) {
      this.next = next;
    }

    public void setPrev(final VWLineSimplifier.VWVertex prev) {
      this.prev = prev;
    }

    public void updateArea() {
      if (this.prev == null || this.next == null) {
        this.area = MAX_AREA;
        return;
      }
      this.area = Math.abs(Triangle.area(this.prev.pt, this.pt, this.next.pt));
    }
  }

  public static Point[] simplify(final LineString coords,
    final double distanceTolerance) {
    final VWLineSimplifier simp = new VWLineSimplifier(coords,
      distanceTolerance);
    return simp.simplify();
  }

  private final LineString pts;

  private final double tolerance;

  public VWLineSimplifier(final LineString coords, final double distanceTolerance) {
    this.pts = coords;
    this.tolerance = distanceTolerance * distanceTolerance;
  }

  public Point[] simplify() {
    final VWLineSimplifier.VWVertex vwLine = VWVertex.buildLine(this.pts);
    double minArea = this.tolerance;
    do {
      minArea = simplifyVertex(vwLine);
    } while (minArea < this.tolerance);
    final Point[] simp = vwLine.getCoordinates();
    // ensure computed value is a valid line
    if (simp.length < 2) {
      return new Point[] {
        simp[0], new PointDouble(simp[0])
      };
    }
    return simp;
  }

  private double simplifyVertex(final VWLineSimplifier.VWVertex vwLine) {
    /**
     * Scan vertices in line and remove the one with smallest effective area.
     */
    // TODO: use an appropriate data structure to optimize finding the smallest
    // area vertex
    VWLineSimplifier.VWVertex curr = vwLine;
    double minArea = curr.getArea();
    VWLineSimplifier.VWVertex minVertex = null;
    while (curr != null) {
      final double area = curr.getArea();
      if (area < minArea) {
        minArea = area;
        minVertex = curr;
      }
      curr = curr.next;
    }
    if (minVertex != null && minArea < this.tolerance) {
      minVertex.remove();
    }
    if (!vwLine.isLive()) {
      return -1;
    }
    return minArea;
  }
}
