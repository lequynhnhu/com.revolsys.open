package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.NodeQuadTree;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public class NodeLessThanDistanceOfCoordinatesVisitor<T> implements
  Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(
    Graph<T> graph,
    Coordinates point,
    double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Visitor<Node<T>> visitor = new NodeWithinDistanceOfCoordinateVisitor<T>(
      point, maxDistance, results);
    final Envelope envelope = new BoundingBox(point);
    envelope.expandBy(maxDistance);
    NodeQuadTree<T> nodeIndex = graph.getNodeIndex();
    nodeIndex.visit(envelope, visitor);
    final List<Node<T>> nodes = results.getList();
    Collections.sort(nodes);
    return nodes;
  }

  private final Coordinates coordinates;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeLessThanDistanceOfCoordinatesVisitor(
    final Coordinates coordinates, final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.coordinates = coordinates;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  public boolean visit(final Node<T> node) {
    final double distance = this.coordinates.distance(node);
    if (distance < maxDistance) {
      matchVisitor.visit(node);
    }
    return true;
  }

}