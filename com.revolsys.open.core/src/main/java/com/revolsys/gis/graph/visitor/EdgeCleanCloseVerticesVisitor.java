package com.revolsys.gis.graph.visitor;

import java.util.LinkedHashSet;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.event.CoordinateEventListenerList;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.event.EdgeEvent;
import com.revolsys.gis.graph.event.EdgeEventListenerList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.math.Angle;
import com.revolsys.util.MathUtil;

public class EdgeCleanCloseVerticesVisitor<T> implements Visitor<Edge<T>> {

  private final CoordinateEventListenerList coordinateListeners = new CoordinateEventListenerList();

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<T>();

  private final Graph<T> graph;

  private final double minDistance;

  public EdgeCleanCloseVerticesVisitor(final Graph<T> graph,
    final double minDistance) {
    this.graph = graph;
    this.minDistance = minDistance;
  }

  public EdgeCleanCloseVerticesVisitor(final Graph<T> graph,
    final double minDistance, final Visitor<Edge<T>> visitor) {
    this.graph = graph;
    this.minDistance = minDistance;
  }

  private double getAngle(final Edge<T> edge, final LineString line,
    final int index) {
    if (index + index - 1 < 0 || index + index + 1 >= line.getVertexCount()) {
      return Double.NaN;
    } else {
      final double x1 = line.getCoordinate(index - 1, 0);
      final double y1 = line.getCoordinate(index - 1, 1);
      final double x2 = line.getCoordinate(index, 0);
      final double y2 = line.getCoordinate(index, 1);
      final double x3 = line.getCoordinate(index + 1, 0);
      final double y3 = line.getCoordinate(index + 1, 1);
      return Angle.angle(x1, y1, x2, y2, x3, y3);
    }
  }

  public CoordinateEventListenerList getCoordinateListeners() {
    return this.coordinateListeners;
  }

  public EdgeEventListenerList<T> getEdgeListeners() {
    return this.edgeListeners;
  }

  // TODO look at the angles with the previous and next segments to decide
  // which coordinate to remove. If there is a right angle in a building then
  // it should probably not be removed. This would be confirmed by the angles
  // of the next and previous segments.
  /**
   * Visit the edge performing any required cleanup.
   *
   * @param edge The edge to visit.
   * @return true If further edges should be processed.
   */
  @Override
  public boolean visit(final Edge<T> edge) {
    final String typePath = edge.getTypeName();
    final LineString line = edge.getLine();
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 2) {
      final GeometryFactory geometryFactory = line.getGeometryFactory();
      final LinkedHashSet<Integer> removeIndicies = new LinkedHashSet<Integer>();

      double x1 = line.getX(0);
      double y1 = line.getY(0);
      for (int i = 1; i < vertexCount; i++) {
        final double x2 = line.getX(i);
        final double y2 = line.getY(i);
        final double distance = MathUtil.distance(x1, y1, x2, y2);
        if (distance < this.minDistance) {
          final double previousAngle = getAngle(edge, line, i - 1);
          final double angle = getAngle(edge, line, i);
          final double nextAngle = getAngle(edge, line, i + 1);
          boolean fixed = false;
          if (angle > previousAngle) {
            if (angle > nextAngle) {
              if (angle > Math.toRadians(160)) {
                fixed = true;
              }
            }
          } else if (previousAngle > nextAngle) {

          }
          if (fixed) {
            this.coordinateListeners.coordinateEvent(
              new PointDouble(x2, y2),
              typePath,
              "Short Segment",
              "Fixed",
              distance + " " + Math.toDegrees(previousAngle) + " "
                  + Math.toDegrees(angle) + " " + Math.toDegrees(nextAngle));
          } else {
            this.coordinateListeners.coordinateEvent(
              new PointDouble(x2, y2),
              typePath,
              "Short Segment",
              "Review",
              distance + " " + Math.toDegrees(previousAngle) + " "
                  + Math.toDegrees(angle) + " " + Math.toDegrees(nextAngle));
          }
        }
        x1 = x2;
        y1 = y2;
      }
      if (!removeIndicies.isEmpty()) {
        final int axisCount = line.getAxisCount();
        final double[] newCoordinates = new double[(vertexCount - removeIndicies.size())
                                                   * axisCount];
        int k = 0;
        for (int j = 0; j < vertexCount; j++) {
          if (!removeIndicies.contains(j)) {
            CoordinatesListUtil.setCoordinates(newCoordinates, axisCount, k,
              line, j);
            k++;
          }
        }
        final LineString newLine = geometryFactory.lineString(axisCount,
          newCoordinates);
        final Edge<T> newEdge = this.graph.replaceEdge(edge, newLine);
        this.edgeListeners.edgeEvent(newEdge, "Edge close indicies",
          EdgeEvent.EDGE_CHANGED, null);
      }
    }
    return true;
  }

}
