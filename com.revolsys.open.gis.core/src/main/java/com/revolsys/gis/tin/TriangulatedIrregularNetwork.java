package com.revolsys.gis.tin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.Circle;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.Triangle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class TriangulatedIrregularNetwork {
  private static final int[] OPPOSITE_INDEXES = {
    2, 1, 0
  };

  private final Quadtree circumCircleIndex = new Quadtree();

  private final GeometryFactory geometryFactory;

  private Quadtree triangleIndex = new Quadtree();

  public TriangulatedIrregularNetwork(final BoundingBox boundingBox) {
    this(boundingBox.getGeometryFactory(), boundingBox);
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final BoundingBox envelope) {
    this.geometryFactory = geometryFactory;
    final Coordinates c1 = new DoubleCoordinates(envelope.getMinX(),
      envelope.getMinY(), 0);
    final Coordinates c2 = new DoubleCoordinates(envelope.getMaxX(),
      envelope.getMinY(), 0);
    final Coordinates c3 = new DoubleCoordinates(envelope.getMaxX(),
      envelope.getMaxY(), 0);
    final Coordinates c4 = new DoubleCoordinates(envelope.getMinX(),
      envelope.getMaxY(), 0);
    addTriangle(Triangle.createClockwiseTriangle(c1, c2, c3));
    addTriangle(Triangle.createClockwiseTriangle(c1, c3, c4));
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final Envelope envelope) {
    this.geometryFactory = geometryFactory;
    final Coordinates c1 = new DoubleCoordinates(envelope.getMinX(),
      envelope.getMinY(), 0);
    final Coordinates c2 = new DoubleCoordinates(envelope.getMaxX(),
      envelope.getMinY(), 0);
    final Coordinates c3 = new DoubleCoordinates(envelope.getMaxX(),
      envelope.getMaxY(), 0);
    final Coordinates c4 = new DoubleCoordinates(envelope.getMinX(),
      envelope.getMaxY(), 0);
    addTriangle(Triangle.createClockwiseTriangle(c1, c2, c3));
    addTriangle(Triangle.createClockwiseTriangle(c1, c3, c4));
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final Polygon polygon) {
    this.geometryFactory = geometryFactory;
    final CoordinatesList coords = CoordinatesListUtil.get(polygon.getExteriorRing());
    addTriangle(Triangle.createClockwiseTriangle(coords.get(0), coords.get(1),
      coords.get(2)));
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final Triangle triangle) {
    this.geometryFactory = geometryFactory;

    addTriangle(triangle);
  }

  private void addBreaklineIntersect(final Triangle triangle,
    final Coordinates intersectCoord) {
    Coordinates previousCoord = triangle.get(0);
    for (int i = 1; i < 3; i++) {
      final Coordinates triCorner = triangle.get(i);
      if (!triCorner.equals2d(intersectCoord)
        && !previousCoord.equals2d(intersectCoord)) {
        final double distance = new LineSegment(previousCoord, triCorner).distance(intersectCoord);
        if (distance == 0) {
          final Coordinates nextCoordinates = triangle.get((i + 1) % 3);
          replaceTriangle(
            triangle,
            new Triangle[] {
              Triangle.createClockwiseTriangle(intersectCoord, triCorner,
                nextCoordinates),
              Triangle.createClockwiseTriangle(intersectCoord, nextCoordinates,
                previousCoord)
            });
        }
      }
      previousCoord = triCorner;
    }
  }

  private void addBreaklineIntersect(final Triangle triangle,
    final LineSegment intersectLine) {
    final Coordinates lc0 = intersectLine.get(0);
    final Coordinates lc1 = intersectLine.get(1);
    double startCornerDistance = Double.MAX_VALUE;
    double startEdgeDistance = Double.MAX_VALUE;
    double endEdgeDistance = Double.MAX_VALUE;
    double endCornerDistance = Double.MAX_VALUE;
    int startClosestCorner = -1;
    int endClosestCorner = -1;
    int startClosestEdge = -1;
    int endClosestEdge = -1;
    for (int i = 0; i < 3; i++) {
      final Coordinates corner = triangle.get(i);
      final Coordinates nextCorner = triangle.get((i + 1) % 3);

      final double startCorner = corner.distance(lc0);
      if (startClosestCorner == -1 || startCorner < startCornerDistance) {
        startClosestCorner = i;
        startCornerDistance = startCorner;
      }

      final double endCorner = corner.distance(lc1);
      if (endClosestCorner == -1 || endCorner < endCornerDistance) {
        endClosestCorner = i;
        endCornerDistance = endCorner;
      }

      final LineSegment edge = new LineSegment(corner, nextCorner);
      final double startEdge = edge.distance(lc0);
      if (startClosestEdge == -1 || startEdge < startEdgeDistance) {
        startClosestEdge = i;
        startEdgeDistance = startEdge;
      }

      final double endEdge = edge.distance(lc1);
      if (endClosestEdge == -1 || endEdge < endEdgeDistance) {
        endClosestEdge = i;
        endEdgeDistance = endEdge;
      }
    }
    // Start of algorithm

    if (startCornerDistance < 0.01) {
      // Touching Start corner
      if (endCornerDistance < 0.01) {
        // Touching two corners
        final Triangle newTriangle = Triangle.createClockwiseTriangle(lc0, lc1,
          getOtherCoordinates(triangle, startClosestCorner, endClosestCorner));
        replaceTriangle(triangle, newTriangle);
      } else {
        // Touching start corner
        final double edgeDistance = endEdgeDistance;
        addTriangleTouchingOneCorner(triangle, lc0, lc1, startClosestCorner,
          endClosestEdge, edgeDistance);
      }
    } else if (endCornerDistance < 0.01) {
      // Touching end corner
      final double edgeDistance = startEdgeDistance;
      addTriangleTouchingOneCorner(triangle, lc1, lc0, endClosestCorner,
        startClosestEdge, edgeDistance);
    } else if (startEdgeDistance < 0.01) {
      if (endEdgeDistance < 0.01) {
        addTriangleTouchingTwoEdges(triangle, lc0, lc1, startClosestEdge,
          endClosestEdge);
      } else {
        addTriangleTouchingOneEdge(triangle, lc0, lc1, startClosestEdge);
      }
    } else if (endEdgeDistance < 0.01) {
      addTriangleTouchingOneEdge(triangle, lc1, lc0, endClosestEdge);

    } else {
      if (startCornerDistance <= endCornerDistance) {
        addContainedLine(triangle, startClosestCorner, lc0, lc1);
      } else {
        addContainedLine(triangle, endClosestCorner, lc1, lc0);
      }

    }
  }

  private void addBreaklineItersect(final Triangle triangle,
    final LineSegment breakline, final LineSegment intersectLine) {
    final Coordinates lc0 = intersectLine.get(0);
    final Coordinates lc1 = intersectLine.get(0);
    breakline.setElevationOnPoint(geometryFactory, lc0);
    breakline.setElevationOnPoint(geometryFactory, lc1);
    final LineSegment lineSegment = new LineSegment(lc0, lc1);
    addBreaklineIntersect(triangle, lineSegment);
  }

  /**
   * Split a triangle where the line segment i0 -> i1 is fully contained in the
   * triangle. Creates 3 new triangles.
   * 
   * @param triangle
   * @param coordinates The coordinates of the triangle.
   * @param index The index of the closest corner to i0.
   * @param l0 The start coordinate of the line.
   * @param l1 The end coordinate of the line.
   */
  private void addContainedLine(final Triangle triangle, final int index,
    final Coordinates l0, final Coordinates l1) {
    final Coordinates t0 = triangle.get(index);
    final Coordinates t1 = triangle.get((index + 1) % 3);
    final Coordinates t2 = triangle.get((index + 2) % 3);

    final int c0i0i1Orientation = CoordinatesUtil.orientationIndex(t0, l0, l1);
    if (c0i0i1Orientation == CGAlgorithms.COLLINEAR) {
      addTrianglesContained(triangle, t0, t1, t2, l0, l1);

    } else if (c0i0i1Orientation == CGAlgorithms.CLOCKWISE) {
      final double angleCornerLine = CoordinatesUtil.angle(t0, l0, l1);
      final double angleCornerLineCorner = CoordinatesUtil.angle(t0, l0, t2);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      } else {
        addTrianglesContained(triangle, t1, t2, t0, l0, l1);
      }

    } else {
      final double angleCornerLine = CoordinatesUtil.angle(t0, l0, l1);
      final double angleCornerLineCorner = CoordinatesUtil.angle(t0, l0, t1);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t1, t2, t0, l1, l0);
      } else {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      }
    }
  }

  private void addTrangleCornerAndEdgeTouch(final Triangle triangle,
    final Coordinates cPrevious, final Coordinates c, final Coordinates cNext,
    final Coordinates cOpposite) {
    replaceTriangle(
      triangle,
      new Triangle[] {
        Triangle.createClockwiseTriangle(cPrevious, c, cOpposite),
        Triangle.createClockwiseTriangle(c, cNext, cOpposite)
      });
  }

  private void addTriangle(final Triangle triangle) {
    final Circle circle = triangle.getCircumcircle();
    circumCircleIndex.insert(circle.getEnvelopeInternal(), triangle);
  }

  private void addTriangleCorderEdge(final Triangle triangle,
    final Coordinates lc0, final Coordinates lc1, final int startCorner,
    final int startEdge) {
    final Coordinates cNext = triangle.get((startCorner + 1) % 3);
    final Coordinates cPrevious = triangle.get((startCorner + 2) % 3);
    if (startEdge == startCorner) {
      addTrangleCornerAndEdgeTouch(triangle, lc0, lc1, cNext, cPrevious);
    } else if (startEdge == (startCorner + 1) % 3) {
      addTrangleCornerAndEdgeTouch(triangle, cPrevious, lc1, cNext, lc0);
    } else {
      addTrangleCornerAndEdgeTouch(triangle, lc0, lc1, cPrevious, cNext);
    }
  }

  /**
   * Add the triangles where the line is fully contained in the triangle. There
   * will be 5 triangles created. The triangle coordinate t0 will be part of two
   * triangles, the other two triangle coordinates will be part of 3 triangles.
   * l1 must not be closer than l0 to t0.
   * 
   * @param triangle TODO
   * @param t0 The first triangle coordinate.
   * @param t1 The second triangle coordinate.
   * @param t2 The third triangle coordinate.
   * @param l0 The first line coordinate.
   * @param l1 The second line coordinate.
   */
  private void addTrianglesContained(final Triangle triangle,
    final Coordinates t0, final Coordinates t1, final Coordinates t2,
    final Coordinates l0, final Coordinates l1) {
    replaceTriangle(
      triangle,
      new Triangle[] {
        Triangle.createClockwiseTriangle(t0, t1, l0),
        Triangle.createClockwiseTriangle(l0, t1, l1),
        Triangle.createClockwiseTriangle(l1, t1, t2),
        Triangle.createClockwiseTriangle(l0, l1, t2),
        Triangle.createClockwiseTriangle(t0, l0, t2)
      });
  }

  private void addTriangleStartCornerEndInside(final Triangle triangle,
    final int cornerIndex, final Coordinates cCorner, final Coordinates cInside) {
    final Coordinates cNext = triangle.get((cornerIndex + 1) % 3);
    final Coordinates cPrevious = triangle.get((cornerIndex + 2) % 3);
    replaceTriangle(
      triangle,
      new Triangle[] {
        Triangle.createClockwiseTriangle(cCorner, cNext, cInside),
        Triangle.createClockwiseTriangle(cInside, cNext, cPrevious),
        Triangle.createClockwiseTriangle(cInside, cPrevious, cCorner)
      });
  }

  private void addTriangleTouchingOneCorner(final Triangle triangle,
    final Coordinates lc0, final Coordinates lc1, final int startCorner,
    final int endEdge, final double endEdgeDistance) {
    if (endEdgeDistance < 1) {
      addTriangleCorderEdge(triangle, lc0, lc1, startCorner, endEdge);
    } else {
      addTriangleStartCornerEndInside(triangle, startCorner, lc0, lc1);
    }
  }

  private void addTriangleTouchingOneEdge(final Triangle triangle,
    final Coordinates lc0, final Coordinates lc1, final int edgeIndex) {
    final Coordinates cPrevious = triangle.get((edgeIndex) % 3);
    final Coordinates cNext = triangle.get((edgeIndex + 1) % 3);
    final Coordinates cOpposite = triangle.get((edgeIndex + 2) % 3);
    if (CoordinatesUtil.orientationIndex(cPrevious, lc0, lc1) == CGAlgorithms.COLLINEAR) {
      replaceTriangle(
        triangle,
        new Triangle[] {
          Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
          Triangle.createClockwiseTriangle(cOpposite, lc0, lc1),
          Triangle.createClockwiseTriangle(cOpposite, lc1, cNext),
          Triangle.createClockwiseTriangle(lc0, lc1, cNext)
        });
    } else {
      replaceTriangle(
        triangle,
        new Triangle[] {
          Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
          Triangle.createClockwiseTriangle(cNext, lc0, lc1),
          Triangle.createClockwiseTriangle(cNext, lc1, cOpposite),
          Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite)
        });
    }
  }

  private void addTriangleTouchingTwoEdges(final Triangle triangle,
    final Coordinates lc0, final Coordinates lc1, final int startEdge,
    final int endEdge) {
    final Coordinates cPrevious = triangle.get(startEdge);
    final Coordinates cNext = triangle.get((startEdge + 1) % 3);
    final Coordinates cOpposite = triangle.get((startEdge + 2) % 3);
    if (startEdge == endEdge) {
      if (cPrevious.distance(lc0) < cPrevious.distance(lc1)) {
        replaceTriangle(
          triangle,
          new Triangle[] {
            Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
            Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
            Triangle.createClockwiseTriangle(lc1, cNext, cOpposite),
          });
      } else {
        replaceTriangle(
          triangle,
          new Triangle[] {
            Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite),
            Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
            Triangle.createClockwiseTriangle(lc0, cNext, cOpposite)
          });
      }
    } else if (endEdge == ((startEdge + 1) % 3)) {
      replaceTriangle(
        triangle,
        new Triangle[] {
          Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, cNext, lc1)
        });
    } else {
      replaceTriangle(
        triangle,
        new Triangle[] {
          Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
          Triangle.createClockwiseTriangle(lc0, cNext, lc1),
          Triangle.createClockwiseTriangle(lc1, cNext, cOpposite)
        });
    }
  }

  @SuppressWarnings("unchecked")
  public void buildIndex() {
    triangleIndex = new Quadtree();
    for (final Triangle triangle : (List<Triangle>)circumCircleIndex.queryAll()) {
      triangleIndex.insert(triangle.getEnvelopeInternal(), triangle);
    }
  }

  public Circle getCircle(final Polygon polygon) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(polygon.getExteriorRing());
    final Coordinates a = coordinates.get(0);
    final Coordinates b = coordinates.get(1);
    final Coordinates c = coordinates.get(2);
    final double angleB = CoordinatesUtil.angle(a, b, c);

    final double radius = a.distance(c) / Math.sin(angleB) * 0.5;
    final Coordinates coordinate = Triangle.circumCentre(coordinates);
    return new Circle(coordinate, radius);
  }

  public double getElevation(final Coordinates coordinate) {
    final List<Triangle> triangles = getTriangles(coordinate);
    for (final Triangle triangle : triangles) {
      final Coordinates t0 = triangle.getP0();
      final Coordinates t1 = triangle.getP1();
      final Coordinates t2 = triangle.getP2();
      Coordinates closestCorner = t0;
      LineSegment oppositeEdge = new LineSegment(t1, t2);
      double closestDistance = coordinate.distance(closestCorner);
      final double t1Distance = coordinate.distance(t1);
      if (closestDistance > t1Distance) {
        closestCorner = t1;
        oppositeEdge = new LineSegment(t2, t0);
        closestDistance = t1Distance;
      }
      if (closestDistance > coordinate.distance(t2)) {
        closestCorner = t2;
        oppositeEdge = new LineSegment(t0, t1);
      }
      LineSegment segment = new LineSegment(closestCorner, coordinate).extend(
        0, t0.distance(t1) + t1.distance(t2) + t0.distance(t2));
      final Coordinates intersectCoordinates = oppositeEdge.intersection(segment);
      if (intersectCoordinates != null) {
        segment = new LineSegment(t0, intersectCoordinates);
        return segment.getElevation(coordinate);
      }
    }
    return Double.NaN;
  }

  private Coordinates getOtherCoordinates(final CoordinatesList coords,
    final int i1, final int i2) {
    final int index = getOtherIndex(i1, i2);
    return coords.get(index);
  }

  /**
   * Get the index of the corner or a triangle opposite corners i1 -> i2. i1 and
   * i2 must have different values in the range 0..2.
   * 
   * @param i1
   * @param i2
   * @return
   */
  private int getOtherIndex(final int i1, final int i2) {
    return OPPOSITE_INDEXES[i1 + i2 - 1];
  }

  @SuppressWarnings("unchecked")
  public List<Triangle> getTriangles() {
    return triangleIndex.queryAll();

  }

  public List<Triangle> getTriangles(final Coordinates coordinate) {
    final Envelope envelope = new BoundingBox(coordinate);
    final List<Triangle> triangles = new ArrayList<Triangle>();
    triangleIndex.query(envelope, new ItemVisitor() {
      public void visitItem(final Object object) {
        final Triangle triangle = (Triangle)object;
        if (triangle.contains(coordinate)) {
          triangles.add(triangle);
        }
      }
    });
    return triangles;
  }

  public List<Triangle> getTriangles(final Envelope envelope) {
    final List<Triangle> triangles = new ArrayList<Triangle>();
    triangleIndex.query(envelope, new ItemVisitor() {
      public void visitItem(final Object object) {
        final Triangle triangle = (Triangle)object;
        final Envelope triangleEnvelope = triangle.getEnvelopeInternal();
        if (triangleEnvelope.intersects(envelope)) {
          triangles.add(triangle);
        }
      }
    });
    return triangles;
  }

  public List<Triangle> getTriangles(final LineSegment segment) {
    final Envelope envelope = new BoundingBox(segment.get(0), segment.get(0));
    return getTriangles(envelope);
  }

  public void insertEdge(final LineSegment breakline) {
    final List<Triangle> triangles = getTriangles(breakline);
    for (final Triangle triangle : triangles) {
      final LineSegment intersection = triangle.intersection(breakline);
      if (intersection != null) {
        final double length = intersection.getLength();
        if (length < 0.01) {
          addBreaklineIntersect(triangle, intersection.get(0));
        } else {
          addBreaklineItersect(triangle, breakline, intersection);
        }
      }
    }
  }

  public void insertEdge(final LineString breakline) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(breakline);
    Coordinates previousCoordinates = coordinates.get(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinates coordinate = coordinates.get(i);
      final LineSegment segment = new LineSegment(previousCoordinates,
        coordinate);
      insertEdge(segment);
      previousCoordinates = coordinate;
    }
  }

  @SuppressWarnings("unchecked")
  public void insertNode(final Coordinates coordinate) {
    final List<Triangle> triangles = getTrianglesCircumcircleIntersections(coordinate);
    if (!triangles.isEmpty()) {
      final TreeSet<Coordinates> exterior = new TreeSet<Coordinates>(
        new Comparator<Coordinates>() {
          public int compare(final Coordinates c1, final Coordinates c2) {
            final double angleC1 = coordinate.angle2d(c1);
            final double angleC2 = coordinate.angle2d(c2);
            if (angleC1 < angleC2) {
              return 1;
            } else if (angleC1 > angleC2) {
              return -1;
            } else {
              return 0;
            }
          }
        });
      for (final Triangle triangle : triangles) {
        final Circle circle = triangle.getCircumcircle();
        if (circle.contains(coordinate)) {
          removeTriangle(triangle);
          if (!coordinate.equals2d(triangle.getP0())) {
            exterior.add(triangle.getP0());
          }
          if (!coordinate.equals2d(triangle.getP1())) {
            exterior.add(triangle.getP1());
          }
          if (!coordinate.equals2d(triangle.getP2())) {
            exterior.add(triangle.getP2());
          }
        }
      }

      if (!exterior.isEmpty()) {
        Coordinates previousCorner = exterior.last();
        for (final Coordinates corner : exterior) {
          addTriangle(new Triangle(coordinate, previousCorner, corner));
          previousCorner = corner;
        }
      }
    }
  }

  public List<Triangle> getTrianglesCircumcircleIntersections(
    final Coordinates point) {
    final Envelope envelope = new BoundingBox(point);
    final List<Triangle> triangles = circumCircleIndex.query(envelope);
    for (Iterator<Triangle> iterator = triangles.iterator(); iterator.hasNext();) {
      Triangle triangle = iterator.next();
      if (!triangle.intersectsCircumCircle(point)) {
        iterator.remove();
      }
    }
    return triangles;
  }

  public void insertNode(final Point point) {
    final Coordinates coordinate = CoordinatesUtil.get(point);
    insertNode(coordinate);
  }

  public void insertNodes(final LineString nodes) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(nodes);
    for (int i = 0; i < coordinates.size(); i++) {
      final Coordinates coordinate = coordinates.get(i);
      insertNode(coordinate);
    }
  }

  private void removeTriangle(final Triangle triangle) {
    circumCircleIndex.remove(triangle.getCircumcircle().getEnvelopeInternal(),
      triangle);
    // triangleIndex.remove(triangle.getEnvelopeInternal(), triangle);
  }

  private void replaceTriangle(final Triangle triangle,
    final Triangle newTriangle) {
    removeTriangle(triangle);
    addTriangle(newTriangle);
  }

  private void replaceTriangle(final Triangle triangle,
    final Triangle[] newTriangles) {
    removeTriangle(triangle);
    for (final Triangle newTriangle : newTriangles) {
      addTriangle(newTriangle);
    }
  }
}