package com.revolsys.swing.table.geometry;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.data.types.DataTypes;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.map.form.GeometryCoordinatesPanel;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class GeometryCoordinatesTableModel extends AbstractTableModel {
  public static int[] getEventRowObject(final TablePanel panel) {
    final GeometryCoordinatesTableModel model = panel.getTableModel();
    final int row = TablePanel.getEventRow();
    final int[] object = model.getVertexIndex(row);
    return object;
  }

  public static Map<int[], Vertex> getIndexOfVertices(final Geometry geometry) {
    final Map<int[], Vertex> pointIndexes = new LinkedHashMap<>();
    if (geometry == null || geometry.isEmpty()) {
    } else {
      for (final Vertex vertex : geometry.vertices()) {
        final int[] vertexId = vertex.getVertexId();
        final Vertex clone = vertex.clone();
        pointIndexes.put(vertexId, clone);
      }
    }
    return pointIndexes;
  }

  private static final long serialVersionUID = 1L;

  private List<String> axisNames = Arrays.asList("#", "X", "Y", "Z", "M");

  private Geometry geometry;

  private GeometryFactory geometryFactory;

  private int columnCount = 0;

  private Map<int[], Vertex> vertexIndexMap = Collections.emptyMap();

  private List<int[]> vertexIndices = Collections.emptyList();

  private int axisCount;

  private int numIndexItems;

  private Reference<LayerRecordForm> form;

  private int vertexIndexColumn;

  private int segmentIndexColumn;

  private final GeometryCoordinatesPanel geometryCoordinatesPanel;

  public GeometryCoordinatesTableModel() {
    this(null);
  }

  public GeometryCoordinatesTableModel(
    final GeometryCoordinatesPanel geometryCoordinatesPanel) {
    this.geometryCoordinatesPanel = geometryCoordinatesPanel;

  }

  public int getAxisCount() {
    return this.axisCount;
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex < this.numIndexItems) {
      return Integer.class;
    } else {
      return Double.class;
    }
  }

  @Override
  public int getColumnCount() {
    return this.columnCount;
  }

  @Override
  public String getColumnName(final int column) {
    return this.axisNames.get(column);
  }

  public double getCoordinate(final int rowIndex, final int columnIndex) {
    if (rowIndex < getRowCount()) {
      final int axisIndex = columnIndex - this.numIndexItems;
      if (axisIndex > -1) {
        final Point point = getVertex(rowIndex);
        if (point != null) {
          final double coordinate = point.getCoordinate(axisIndex);
          return coordinate;
        }
      }
    }
    return Double.NaN;
  }

  @Override
  public JComponent getEditorField(final int rowIndex, final int columnIndex,
    final Object value) {
    if (columnIndex < this.numIndexItems) {
      return null;
    } else {
      final int axisIndex = columnIndex - this.numIndexItems;
      final int scale;
      if (this.geometryFactory.isFloating()) {
        scale = 7;
      } else {
        scale = (int)Math.ceil(Math.log10(this.geometryFactory.getScale(axisIndex)));
      }
      final NumberTextField field = new NumberTextField(DataTypes.DOUBLE, 20,
        scale);
      field.setFieldValue(value);
      field.setUndoManager(getForm().getUndoManager());
      return field;
    }
  }

  public LayerRecordForm getForm() {
    return this.form.get();
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public int getNumIndexItems() {
    return this.numIndexItems;
  }

  @Override
  public int getRowCount() {
    return this.vertexIndices.size();
  }

  public int getSegmentIndexColumn() {
    return this.segmentIndexColumn;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (rowIndex < getRowCount()) {
      final int[] vertexIndex = getVertexIndex(rowIndex);
      if (columnIndex < vertexIndex.length) {
        return vertexIndex[columnIndex];
      } else if (columnIndex == this.segmentIndexColumn) {
        return vertexIndex[vertexIndex.length - 1] + " \u2193";
      } else {
        final int axisIndex = columnIndex - this.numIndexItems;
        final Point point = getVertex(rowIndex);
        if (point == null) {
          return "-";
        } else {
          final double coordinate = point.getCoordinate(axisIndex);
          if (Double.isNaN(coordinate)) {
            return "NaN";
          } else if (Double.isInfinite(coordinate)) {
            if (coordinate < 0) {
              return "-Infinity";
            } else {
              return "Infinity";
            }
          } else {
            return coordinate;
          }
        }
      }
    } else {
      return "-";
    }
  }

  private Vertex getVertex(final int rowIndex) {
    final int[] vertexIndex = getVertexIndex(rowIndex);
    if (vertexIndex == null) {
      return null;
    } else {
      return this.vertexIndexMap.get(vertexIndex);
    }
  }

  public int[] getVertexIndex(final int rowIndex) {
    if (rowIndex >= 0 && rowIndex < getRowCount()) {
      return this.vertexIndices.get(rowIndex);
    } else {
      return new int[0];
    }
  }

  public int getVertexIndexColumn() {
    return this.vertexIndexColumn;
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (rowIndex < getRowCount()) {
      if (columnIndex >= this.numIndexItems) {
        return true;
      }
    }
    return false;
  }

  public void setForm(final LayerRecordForm form) {
    this.form = new WeakReference<LayerRecordForm>(form);
  }

  public void setGeometry(final Geometry geometry) {
    final LayerRecordForm form = this.geometryCoordinatesPanel.getForm();
    final LayerRecord record = form.getRecord();
    final Geometry oldGeometry = record.getGeometryValue();

    if (oldGeometry != geometry) {
      final SetObjectProperty setObjectProperty = new SetObjectProperty(record,
        record.getRecordDefinition().getGeometryFieldName(), oldGeometry,
        geometry);
      final UndoManager undoManager = form.getUndoManager();
      undoManager.addEdit(setObjectProperty);
    }
    if (this.geometry != geometry) {
      this.geometry = geometry;
      if (geometry == null) {
        this.geometryFactory = GeometryFactory.floating3();
        this.vertexIndexMap = Collections.emptyMap();
        this.vertexIndices = Collections.emptyList();
      } else {
        this.geometryFactory = geometry.getGeometryFactory();
        this.vertexIndexMap = getIndexOfVertices(geometry);
        this.vertexIndices = new ArrayList<int[]>(this.vertexIndexMap.keySet());
      }
      this.axisCount = this.geometryFactory.getAxisCount();
      this.axisNames = new ArrayList<String>();
      if (geometry instanceof Polygon) {
        this.axisNames.add("R");
      } else if (geometry instanceof MultiPoint) {
        this.axisNames.add("P");
      } else if (geometry instanceof MultiLineString) {
        this.axisNames.add("P");
      } else if (geometry instanceof MultiPolygon) {
        this.axisNames.add("P");
        this.axisNames.add("R");
      } else if (geometry instanceof GeometryCollection) {
        this.axisNames.add("P");
        this.axisNames.add("R");
      } else {
      }
      this.vertexIndexColumn = this.axisNames.size();
      this.axisNames.add("#");
      this.segmentIndexColumn = this.axisNames.size();
      this.axisNames.add("S #");
      this.numIndexItems = this.axisNames.size();
      this.axisNames.add("X");
      this.axisNames.add("Y");
      if (this.axisCount > 2) {
        this.axisNames.add("Z");
      }
      if (this.axisCount > 3) {
        this.axisNames.add("M");
      }
      this.columnCount = this.axisNames.size();
      fireTableStructureChanged();
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    if (Property.hasValue(value)) {
      if (rowIndex < getRowCount()) {
        if (columnIndex >= this.numIndexItems) {
          final int axisIndex = columnIndex - this.numIndexItems;
          final Vertex vertex = getVertex(rowIndex);
          if (vertex != null) {
            final double[] coordinates = vertex.getCoordinates();
            coordinates[axisIndex] = MathUtil.toDouble(value.toString());
            final Point newPoint = this.geometryFactory.point(coordinates);
            if (!newPoint.equalsExact(vertex)) {
              final int[] vertexId = vertex.getVertexId();
              final Geometry newGeometry = this.geometry.moveVertex(newPoint,
                vertexId);
              setGeometry(newGeometry);
            }
          }
        }
      }
    }
  }

  public void setVertexIndexColumn(final int vertexIndexColumn) {
    this.vertexIndexColumn = vertexIndexColumn;
  }
}
