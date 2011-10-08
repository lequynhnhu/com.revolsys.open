package com.revolsys.gis.graph.visitor;

import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.filter.NotFilter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectLog;
import com.revolsys.gis.data.model.filter.GeometryFilter;
import com.revolsys.gis.data.visitor.AbstractVisitor;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.jts.filter.LinearIntersectionFilter;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.LineString;

public class LinearIntersectionNotEqualEdgeLogVisitor extends
  AbstractVisitor<Edge<DataObject>> implements ObjectProcessor<DataObjectGraph> {
  private static final String PROCESSED = LinearIntersectionNotEqualLineEdgeCleanupVisitor.class.getName()
    + ".PROCESSED";

  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  @SuppressWarnings("unchecked")
  public boolean visit(final Edge<DataObject> edge) {
    final DataObject object = edge.getObject();
    final LineString line = edge.getLine();
    if (JtsGeometryUtil.getGeometryProperty(line, PROCESSED) != Boolean.TRUE) {
      final QName typeName = edge.getTypeName();

      final Graph<DataObject> graph = edge.getGraph();

      final AndFilter<Edge<DataObject>> attributeAndGeometryFilter = new AndFilter<Edge<DataObject>>();

      attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<DataObject>(
        typeName));

      final Filter<Edge<DataObject>> filter = getFilter();
      if (filter != null) {
        attributeAndGeometryFilter.addFilter(filter);
      }

      final Filter<DataObject> notEqualLineFilter = new NotFilter<DataObject>(
        new GeometryFilter<LineString>(new EqualFilter<LineString>(line)));

      final GeometryFilter<LineString> linearIntersectionFilter = new GeometryFilter<LineString>(
        new LinearIntersectionFilter(line));

      attributeAndGeometryFilter.addFilter(new EdgeObjectFilter<DataObject>(
        new AndFilter<DataObject>(notEqualLineFilter, linearIntersectionFilter)));

      final List<Edge<DataObject>> intersectingEdges = graph.getEdges(
        attributeAndGeometryFilter, line);

      if (!intersectingEdges.isEmpty()) {
        DataObjectLog.error(getClass(), "Overlapping edge", object);
        JtsGeometryUtil.setGeometryProperty(line, PROCESSED, Boolean.TRUE);
        for (Edge<DataObject> intersectingEdge : intersectingEdges) {
          final DataObject intersectingObject = intersectingEdge.getObject();
          LineString intersectingLine = intersectingObject.getGeometryValue();
          if (JtsGeometryUtil.getGeometryProperty(intersectingLine, PROCESSED) != Boolean.TRUE) {
            JtsGeometryUtil.setGeometryProperty(intersectingLine, PROCESSED,
              Boolean.TRUE);
          }
        }
      }
    }
    return true;
  }
}