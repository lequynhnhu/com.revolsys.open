package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import org.apache.commons.collections4.set.MapBackedSet;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.revolsys.beans.EventsEnabledState;
import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.filter.RecordGeometryBoundingBoxIntersectsFilter;
import com.revolsys.data.filter.RecordGeometryDistanceFilter;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.io.ListRecordReader;
import com.revolsys.data.io.RecordIo;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.RecordQuadTree;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.gpx.GpxWriter;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.spring.ByteArrayResource;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.RecordReaderTransferable;
import com.revolsys.swing.dnd.transferable.StringTransferable;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.FieldNamesSetPanel;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.form.SnapLayersPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.component.MergeRecordsDialog;
import com.revolsys.swing.map.layer.record.renderer.AbstractMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.QueryFilterField;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.RecordDefinitionTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordSaveErrorTableModel;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.tree.MenuSourcePropertyEnableCheck;
import com.revolsys.swing.tree.MenuSourceRunnable;
import com.revolsys.swing.tree.node.record.RecordStoreTableTreeNode;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Label;
import com.revolsys.util.Maps;
import com.revolsys.util.Property;

public abstract class AbstractRecordLayer extends AbstractLayer implements RecordFactory,
  AddGeometryCompleteAction {

  public static void addVisibleLayers(final List<AbstractRecordLayer> layers, final LayerGroup group) {
    if (group.isExists() && group.isVisible()) {
      for (final Layer layer : group) {
        if (layer instanceof LayerGroup) {
          final LayerGroup layerGroup = (LayerGroup)layer;
          addVisibleLayers(layers, layerGroup);
        } else if (layer instanceof AbstractRecordLayer) {
          if (layer.isExists() && layer.isVisible()) {
            final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
            layers.add(recordLayer);
          }
        }
      }
    }
  }

  public static LayerRecord getAndRemoveSame(final Collection<? extends LayerRecord> records,
    final LayerRecord record) {
    for (final Iterator<? extends LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
      final LayerRecord queryRecord = iterator.next();
      if (queryRecord.isSame(record)) {
        iterator.remove();
        return queryRecord;
      }
    }
    return null;
  }

  public static List<AbstractRecordLayer> getVisibleLayers(final LayerGroup group) {
    final List<AbstractRecordLayer> layers = new ArrayList<AbstractRecordLayer>();
    addVisibleLayers(layers, group);
    return layers;
  }

  public static final String ALL = "All";

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  static {
    final Class<AbstractRecordLayer> clazz = AbstractRecordLayer.class;
    final MenuFactory menu = MenuFactory.getMenu(clazz);
    menu.setName("Layer");
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");
    menu.addGroup(3, "dnd");

    final EnableCheck exists = new MenuSourcePropertyEnableCheck("exists");

    menu.addMenuItem(clazz, "ViewRecords");

    final EnableCheck hasSelectedRecords = new MenuSourcePropertyEnableCheck("hasSelectedRecords");
    final EnableCheck hasGeometry = new MenuSourcePropertyEnableCheck("hasGeometry");
    menu.addMenuItem("zoom", MenuSourceRunnable.createAction("Zoom to Selected",
      "magnifier_zoom_selected", new AndEnableCheck(exists, hasGeometry, hasSelectedRecords),
      "zoomToSelected"));

    final EnableCheck editable = new MenuSourcePropertyEnableCheck("editable");
    final EnableCheck readonly = new MenuSourcePropertyEnableCheck("readOnly", false);
    final EnableCheck hasChanges = new MenuSourcePropertyEnableCheck("hasChanges");
    final EnableCheck canAdd = new MenuSourcePropertyEnableCheck("canAddRecords");
    final EnableCheck canDelete = new MenuSourcePropertyEnableCheck("canDeleteRecords");
    final EnableCheck canMergeRecords = new MenuSourcePropertyEnableCheck("canMergeRecords");
    final EnableCheck canPaste = new MenuSourcePropertyEnableCheck("canPaste");

    menu.addCheckboxMenuItem("edit",
      MenuSourceRunnable.createAction("Editable", "pencil", readonly, "toggleEditable"), editable);

    menu.addMenuItem("edit",
      MenuSourceRunnable.createAction("Save Changes", "table_save", hasChanges, "saveChanges"));

    menu.addMenuItem(
      "edit",
      MenuSourceRunnable.createAction("Cancel Changes", "table_cancel", hasChanges, "cancelChanges"));

    menu.addMenuItem("edit",
      MenuSourceRunnable.createAction("Add New Record", "table_row_insert", canAdd, "addNewRecord"));

    menu.addMenuItem("edit", MenuSourceRunnable.createAction("Delete Selected Records",
      "table_row_delete", new AndEnableCheck(hasSelectedRecords, canDelete),
      "deleteSelectedRecords"));

    menu.addMenuItem("edit", MenuSourceRunnable.createAction("Merge Selected Records",
      "shape_group", canMergeRecords, "mergeSelectedRecords"));

    menu.addMenuItem("dnd", MenuSourceRunnable.createAction("Copy Selected Records", "page_copy",
      hasSelectedRecords, "copySelectedRecords"));

    menu.addMenuItem("dnd", MenuSourceRunnable.createAction("Paste New Records", "paste_plain",
      new AndEnableCheck(canAdd, canPaste), "pasteRecords"));

    menu.addMenuItem("layer", 0, MenuSourceRunnable.createAction("Layer Style", "palette",
      new AndEnableCheck(exists, hasGeometry), "showProperties", "Style"));

    // menu.addMenuItem("edit", 0, MenuSourceRunnable.createAction(
    // "Export Records", "disk", new AndEnableCheck(exists, hasSelectedRecords),
    // "exportRecords"));
  }

  private BoundingBox boundingBox = new BoundingBoxDoubleGf();

  private final Label cacheIdDeleted = new Label("deleted");

  private final Label cacheIdHighlighted = new Label("highlighted");

  private final Label cacheIdIndex = new Label("index");

  private final Label cacheIdModified = new Label("modified");

  private final Label cacheIdNew = new Label("new");

  private final Label cacheIdSelected = new Label("selected");

  private Map<Label, Collection<LayerRecord>> cacheIdToRecordMap = new HashMap<>();

  private boolean canAddRecords = true;

  private boolean canDeleteRecords = true;

  private boolean canEditRecords = true;

  private List<String> fieldNames = Collections.emptyList();

  private Object editSync;

  private List<Record> formRecords = new LinkedList<>();

  private List<Component> formComponents = new LinkedList<>();

  private List<Window> formWindows = new LinkedList<>();

  private RecordQuadTree index = new RecordQuadTree();

  private Set<AbstractProxyLayerRecord> proxyRecords = MapBackedSet.mapBackedSet(new WeakHashMap<AbstractProxyLayerRecord, Object>());

  private Query query = new Query();

  private RecordDefinition recordDefinition;

  private RecordQuadTree selectedRecordsIndex;

  private boolean snapToAllLayers = true;

  private boolean useFieldTitles = false;

  private Set<String> userReadOnlyFieldNames = new LinkedHashSet<>();

  private List<String> fieldNamesSetNames = new ArrayList<>();

  private Map<String, List<String>> fieldNamesSets = new HashMap<>();

  private String fieldNamesSetName = ALL;

  public AbstractRecordLayer() {
    this("");
  }

  public AbstractRecordLayer(final Map<String, ? extends Object> properties) {
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
    if (!properties.containsKey("style")) {
      final GeometryStyleRenderer renderer = getRenderer();
      renderer.setStyle(GeometryStyle.createStyle());
    }
    setProperties(properties);
    if (this.fieldNamesSets.isEmpty()) {
      setFieldNamesSets(null);
    }
  }

  public AbstractRecordLayer(final RecordDefinition recordDefinition) {
    this(recordDefinition.getName());
    setRecordDefinition(recordDefinition);
  }

  public AbstractRecordLayer(final String name) {
    this(name, GeometryFactory.floating3(4326));
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
  }

  public AbstractRecordLayer(final String name, final GeometryFactory geometryFactory) {
    super(name);
    setFieldNamesSets(null);
    setGeometryFactory(geometryFactory);
  }

  @Override
  public void addComplete(final AbstractOverlay overlay, final Geometry geometry) {
    if (geometry != null) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String geometryFieldName = recordDefinition.getGeometryFieldName();
      final Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put(geometryFieldName, geometry);
      showAddForm(parameters);
    }
  }

  protected void addHighlightedRecord(final LayerRecord record) {
    addRecordToCache(this.cacheIdHighlighted, record);
  }

  public void addHighlightedRecords(final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      for (final LayerRecord record : records) {
        addHighlightedRecord(record);
      }
      cleanCachedRecords();
    }
    fireHighlighted();
  }

  public void addHighlightedRecords(final LayerRecord... records) {
    addHighlightedRecords(Arrays.asList(records));
  }

  protected void addModifiedRecord(final LayerRecord record) {
    addRecordToCache(this.cacheIdModified, record);
    cleanCachedRecords();
  }

  public void addNewRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField == null) {
      showAddForm(null);
    } else {
      final MapPanel map = MapPanel.get(this);
      if (map != null) {
        final EditGeometryOverlay addGeometryOverlay = map.getMapOverlay(EditGeometryOverlay.class);
        synchronized (addGeometryOverlay) {
          clearSelectedRecords();
          addGeometryOverlay.addRecord(this, this);
        }
      }
    }
  }

  protected void addProxyRecord(final AbstractProxyLayerRecord record) {
    this.proxyRecords.add(record);
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerRecord> List<V> addRecordsToCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      final List<V> results = new ArrayList<>();
      for (final LayerRecord record : records) {
        final LayerRecord cachedRecord = addRecordToCache(cacheId, record);
        results.add((V)cachedRecord);
      }
      cleanCachedRecords();
      return results;
    }
  }

  public LayerRecord addRecordToCache(final Label cacheId, final LayerRecord record) {
    if (isLayerRecord(record)) {
      if (record.getState() == RecordState.Deleted && !isDeleted(record)) {
        return record;
      } else {
        synchronized (getSync()) {
          Collection<LayerRecord> cachedRecords = this.cacheIdToRecordMap.get(cacheId);
          if (cachedRecords == null) {
            cachedRecords = new ArrayList<>();
            this.cacheIdToRecordMap.put(cacheId, cachedRecords);
          }
          if (!cachedRecords.contains(record)) {
            cachedRecords.add(record);
          }
          return record;
        }
      }
    }
    return record;
  }

  @Override
  public int addRenderer(final LayerRenderer<?> child, final int index) {
    final AbstractRecordLayerRenderer oldRenderer = getRenderer();
    AbstractMultipleRenderer rendererGroup;
    if (oldRenderer instanceof AbstractMultipleRenderer) {
      rendererGroup = (AbstractMultipleRenderer)oldRenderer;
    } else {
      rendererGroup = new MultipleRenderer(oldRenderer.getLayer(), null);
      rendererGroup.addRenderer(oldRenderer);
      setRenderer(rendererGroup);
    }
    if (index == 0) {
      rendererGroup.addRenderer(0, (AbstractRecordLayerRenderer)child);
      return 0;
    } else {
      rendererGroup.addRenderer((AbstractRecordLayerRenderer)child);
      return rendererGroup.getRenderers().size() - 1;
    }
  }

  protected void addSelectedRecord(final LayerRecord record) {
    addRecordToCache(this.cacheIdSelected, record);
    clearSelectedRecordsIndex();
  }

  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = query(boundingBox);
      for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
        final LayerRecord layerRecord = iterator.next();
        if (!isVisible(layerRecord) || internalIsDeleted(layerRecord)) {
          iterator.remove();
        }
      }
      addSelectedRecords(records);
      if (isHasSelectedRecords()) {
        showRecordsTable(RecordLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void addSelectedRecords(final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      for (final LayerRecord record : records) {
        addSelectedRecord(record);
      }
    }
    fireSelected();
  }

  public void addSelectedRecords(final LayerRecord... records) {
    addSelectedRecords(Arrays.asList(records));
  }

  public void addToIndex(final Collection<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      addToIndex(record);
    }
  }

  public void addToIndex(final LayerRecord record) {
    if (record != null) {
      final Geometry geometry = record.getGeometryValue();
      if (geometry != null && !geometry.isEmpty()) {
        final RecordQuadTree index = getIndex();
        addRecordToCache(this.cacheIdIndex, record);
        index.insert(record);
      }
    }
  }

  public void addUserReadOnlyFieldNames(final Collection<String> userReadOnlyFieldNames) {
    if (userReadOnlyFieldNames != null) {
      this.userReadOnlyFieldNames.addAll(userReadOnlyFieldNames);
    }
  }

  public void cancelChanges() {
    synchronized (this.getEditSync()) {
      boolean cancelled = true;
      try (
        EventsEnabledState eventsEnabled = EventsEnabledState.disabled(this)) {
        cancelled &= internalCancelChanges(this.cacheIdNew, getNewRecords());
        cancelled &= internalCancelChanges(this.cacheIdDeleted, getDeletedRecords());
        cancelled &= internalCancelChanges(this.cacheIdModified, getModifiedRecords());
        clearSelectedRecordsIndex();
        cleanCachedRecords();
      } finally {
        fireRecordsChanged();
      }
      if (!cancelled) {
        JOptionPane.showMessageDialog(MapPanel.get(this),
          "<html><p>There was an error cancelling changes for one or more records.</p>" + "<p>"
            + getPath() + "</p>" + "<p>Check the logging panel for details.</html>",
          "Error Cancelling Changes", JOptionPane.ERROR_MESSAGE);
      }

    }
  }

  public boolean canPasteRecordGeometry(final LayerRecord record) {
    final Geometry geometry = getPasteRecordGeometry(record, false);

    return geometry != null;
  }

  protected void cleanCachedRecords() {
    System.gc();
  }

  public void clearCachedRecords(final Label cacheId) {
    synchronized (getSync()) {
      this.cacheIdToRecordMap.remove(cacheId);
    }
  }

  public void clearSelectedRecords() {
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdSelected);
      clearCachedRecords(this.cacheIdHighlighted);
      clearSelectedRecordsIndex();
    }
    fireSelected();
  }

  protected void clearSelectedRecordsIndex() {
    this.selectedRecordsIndex = null;
  }

  @Override
  public AbstractRecordLayer clone() {
    final AbstractRecordLayer clone = (AbstractRecordLayer)super.clone();
    clone.cacheIdToRecordMap = new HashMap<>();
    clone.fieldNames = new ArrayList<>(this.fieldNames);
    clone.fieldNamesSetNames = new ArrayList<>(this.fieldNamesSetNames);
    clone.fieldNamesSets = new HashMap<>(this.fieldNamesSets);
    clone.formRecords = new LinkedList<>();
    clone.formComponents = new LinkedList<>();
    clone.formWindows = new LinkedList<>();
    clone.index = new RecordQuadTree();
    clone.selectedRecordsIndex = null;
    clone.proxyRecords = MapBackedSet.mapBackedSet(new WeakHashMap<AbstractProxyLayerRecord, Object>());
    clone.query = this.query.clone();
    clone.sync = new Object();
    clone.editSync = new Object();
    clone.userReadOnlyFieldNames = new LinkedHashSet<>(this.userReadOnlyFieldNames);

    return clone;
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerRecord> V copyRecord(final V record) {
    final LayerRecord copy = createRecord(record);
    return (V)copy;
  }

  public void copyRecordGeometry(final LayerRecord record) {
    final Geometry geometry = record.getGeometryValue();
    if (geometry != null) {
      final StringTransferable transferable = new StringTransferable(DataFlavor.stringFlavor,
        geometry.toString());
      ClipboardUtil.setContents(transferable);
    }
  }

  public void copyRecordsToClipboard(final List<LayerRecord> records) {
    if (!records.isEmpty()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final List<Record> copies = new ArrayList<Record>();
      for (final LayerRecord record : records) {
        copies.add(new ArrayRecord(record));
      }
      final RecordReader reader = new ListRecordReader(recordDefinition, copies);
      final RecordReaderTransferable transferable = new RecordReaderTransferable(reader);
      ClipboardUtil.setContents(transferable);
    }
  }

  public void copySelectedRecords() {
    final List<LayerRecord> selectedRecords = getSelectedRecords();
    copyRecordsToClipboard(selectedRecords);
  }

  protected LayerRecordForm createDefaultForm(final LayerRecord record) {
    return new LayerRecordForm(this, record);
  }

  public LayerRecordForm createForm(final LayerRecord record) {
    final String formFactoryExpression = getProperty(FORM_FACTORY_EXPRESSION);
    if (Property.hasValue(formFactoryExpression)) {
      try {
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Expression expression = parser.parseExpression(formFactoryExpression);
        final EvaluationContext context = new StandardEvaluationContext(this);
        context.setVariable("object", record);
        return expression.getValue(context, LayerRecordForm.class);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to create form for " + this, e);
        return null;
      }
    } else {
      return createDefaultForm(record);
    }
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.createPropertiesPanel();
    createPropertiesPanelFields(propertiesPanel);
    createPropertiesPanelFieldNamesSet(propertiesPanel);
    createPropertiesPanelStyle(propertiesPanel);
    createPropertiesPanelSnapping(propertiesPanel);
    return propertiesPanel;
  }

  protected void createPropertiesPanelFieldNamesSet(final TabbedValuePanel propertiesPanel) {
    final FieldNamesSetPanel panel = new FieldNamesSetPanel(this);
    propertiesPanel.addTab("Field Sets", panel);
  }

  protected void createPropertiesPanelFields(final TabbedValuePanel propertiesPanel) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final BaseJTable fieldTable = RecordDefinitionTableModel.createTable(recordDefinition);

    final BasePanel fieldPanel = new BasePanel(new BorderLayout());
    fieldPanel.setPreferredSize(new Dimension(500, 400));
    final JScrollPane fieldScroll = new JScrollPane(fieldTable);
    fieldPanel.add(fieldScroll, BorderLayout.CENTER);
    propertiesPanel.addTab("Fields", fieldPanel);
  }

  protected void createPropertiesPanelSnapping(final TabbedValuePanel propertiesPanel) {
    final SnapLayersPanel panel = new SnapLayersPanel(this);
    propertiesPanel.addTab("Snapping", panel);
  }

  protected void createPropertiesPanelStyle(final TabbedValuePanel propertiesPanel) {
    if (getRenderer() != null) {
      final LayerStylePanel stylePanel = new LayerStylePanel(this);
      propertiesPanel.addTab("Style", stylePanel);
    }
  }

  @Override
  protected BasePanel createPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = super.createPropertiesTabGeneral(tabPanel);
    createPropertiesTabGeneralPanelFilter(generalPanel);
    return generalPanel;
  }

  protected ValueField createPropertiesTabGeneralPanelFilter(final BasePanel parent) {

    final ValueField filterPanel = new ValueField(this);
    SwingUtil.setTitledBorder(filterPanel, "Filter");

    final QueryFilterField field = new QueryFilterField(this, "where", getWhere());
    SwingUtil.addLabel(filterPanel, "Filter");
    filterPanel.add(field);
    Property.addListener(field, "where", getBeanPropertyListener());

    GroupLayoutUtil.makeColumns(filterPanel, 2, true);

    parent.add(filterPanel);
    return filterPanel;
  }

  public UndoableEdit createPropertyEdit(final LayerRecord record, final String propertyName,
    final Object oldValue, final Object newValue) {
    return new SetObjectProperty(record, propertyName, oldValue, newValue);
  }

  public LayerRecord createRecord() {
    final Map<String, Object> values = Collections.emptyMap();
    return createRecord(values);
  }

  public LayerRecord createRecord(final Map<String, Object> values) {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {
      final LayerRecord record = createRecord(getRecordDefinition());
      record.setState(RecordState.Initalizing);
      try {
        if (values != null && !values.isEmpty()) {
          record.setValues(values);
          record.setIdValue(null);
        }
      } finally {
        record.setState(RecordState.New);
      }
      addRecordToCache(this.cacheIdNew, record);
      fireRecordInserted(record);
      return record;
    } else {
      return null;
    }
  }

  @Override
  public LayerRecord createRecord(final RecordDefinition recordDefinition) {
    if (recordDefinition.equals(getRecordDefinition())) {
      return new ArrayLayerRecord(this);
    } else {
      throw new IllegalArgumentException("Cannot create records for " + recordDefinition);
    }
  }

  public RecordLayerTablePanel createTablePanel() {
    final RecordLayerTable table = RecordLayerTableModel.createTable(this);
    if (table == null) {
      return null;
    } else {
      return new RecordLayerTablePanel(this, table);
    }
  }

  @Override
  protected Component createTableViewComponent() {
    return createTablePanel();
  }

  @Override
  public void delete() {
    super.delete();
    for (final Window window : this.formWindows) {
      if (window != null) {
        Invoke.later(window, "dispose");
      }
    }
    for (final Component form : this.formComponents) {
      if (form != null) {
        if (form instanceof LayerRecordForm) {
          final LayerRecordForm recordForm = (LayerRecordForm)form;
          Invoke.later(recordForm, "destroy");
        }
      }
    }
    this.fieldNamesSetNames.clear();
    this.fieldNamesSets.clear();
    this.formRecords.clear();
    this.formComponents.clear();
    this.formWindows.clear();
    this.index.clear();
    this.cacheIdToRecordMap.clear();
    this.selectedRecordsIndex = null;
  }

  public void deleteRecord(final LayerRecord record) {
    if (isCanDeleteRecords()) {
      doDeleteRecord(record);
      fireRecordDeleted(record);
    }
  }

  public void deleteRecords(final Collection<? extends LayerRecord> records) {
    try (
      EventsEnabledState eventsEnabled = EventsEnabledState.disabled(this)) {
      if (isCanDeleteRecords()) {
        synchronized (this.getEditSync()) {
          unSelectRecords(records);
          for (final LayerRecord record : records) {
            doDeleteRecord(record);
          }
        }
      } else {
        synchronized (this.getEditSync()) {
          for (final LayerRecord record : records) {
            if (removeRecordFromCache(this.cacheIdNew, record)) {
              removeRecordFromCache(record);
              record.setState(RecordState.Deleted);
            }
          }
        }
      }
    }
    fireRecordsChanged();
  }

  public void deleteRecords(final LayerRecord... records) {
    deleteRecords(Arrays.asList(records));
  }

  public void deleteSelectedRecords() {
    final List<LayerRecord> selectedRecords = getSelectedRecords();
    deleteRecords(selectedRecords);
  }

  protected void doDeleteRecord(final LayerRecord record) {
    if (isLayerRecord(record)) {
      removeFromIndex(record);
      removeRecordFromCache(record);

      addRecordToCache(this.cacheIdDeleted, record);

      record.setState(RecordState.Deleted);
      clearSelectedRecordsIndex();
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected List<LayerRecord> doQuery(final BoundingBox boundingBox) {
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    if (boundingBox.isEmpty() || width == 0 || height == 0) {
      return Collections.emptyList();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      final List<LayerRecord> records = (List)getIndex().queryIntersects(convertedBoundingBox);
      return records;
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public List<LayerRecord> doQuery(Geometry geometry, final double distance) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    geometry = geometry.convert(geometryFactory);
    final RecordQuadTree index = getIndex();
    return (List)index.queryDistance(geometry, distance);
  }

  protected abstract List<LayerRecord> doQuery(final Query query);

  protected List<LayerRecord> doQueryBackground(final BoundingBox boundingBox) {
    return doQuery(boundingBox);
  }

  @Override
  protected void doRefresh() {
    setIndexRecords(null);
    fireRecordsChanged();
  }

  @Override
  protected boolean doSaveChanges() {
    throw new UnsupportedOperationException();
  }

  protected boolean doSaveChanges(final LayerRecord record) {
    return false;
  }

  protected boolean doSaveChanges(final RecordSaveErrorTableModel errors) {
    if (isExists()) {
      boolean saved = true;
      try {
        saved &= doSaveChanges(errors, getDeletedRecords());
        saved &= doSaveChanges(errors, getModifiedRecords());
        saved &= doSaveChanges(errors, getNewRecords());
      } finally {
        fireRecordsChanged();
      }
      return saved;
    } else {
      return false;
    }
  }

  private boolean doSaveChanges(final RecordSaveErrorTableModel errors,
    final Collection<LayerRecord> records) {
    boolean saved = true;
    for (final LayerRecord record : new ArrayList<>(records)) {
      try {
        if (!internalSaveChanges(errors, record)) {
          errors.addRecord(record, "Unknown error");
          saved = false;
        }
      } catch (final Throwable t) {
        errors.addRecord(record, t);
      }
    }
    return saved;
  }

  public void exportRecords() {
    final List<LayerRecord> records = getSelectedRecords();
    if (!records.isEmpty()) {
      try {
        try (
          GpxWriter writer = new GpxWriter(new File("/Users/paustin/Desktop/" + getName() + ".gpx"))) {
          for (final LayerRecord record : records) {
            writer.write(record);
          }
        }
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> List<V> filterQueryResults(final List<V> results,
    final Filter<Map<String, Object>> filter) {
    final Collection<LayerRecord> modifiedRecords = getModifiedRecords();
    for (final ListIterator<V> iterator = results.listIterator(); iterator.hasNext();) {
      final LayerRecord record = iterator.next();
      if (internalIsDeleted(record)) {
        iterator.remove();
      } else {
        final V modifiedRecord = (V)getAndRemoveSame(modifiedRecords, record);
        if (modifiedRecord != null) {
          if (Condition.accept(filter, modifiedRecord)) {
            iterator.set(modifiedRecord);
          } else {
            iterator.remove();
          }
        }
      }
    }
    for (final LayerRecord record : modifiedRecords) {
      if (Condition.accept(filter, record)) {
        results.add((V)record);
      }
    }
    for (final LayerRecord record : getNewRecords()) {
      if (Condition.accept(filter, record)) {
        results.add((V)record);
      }
    }
    return results;
  }

  protected <V extends LayerRecord> List<V> filterQueryResults(final List<V> results,
    final Query query) {
    final Condition filter = query.getWhereCondition();
    return filterQueryResults(results, filter);
  }

  protected void fireHighlighted() {
    final int highlightedCount = getHighlightedCount();
    final boolean highlighted = highlightedCount > 0;
    firePropertyChange("hasHighlightedRecords", !highlighted, highlighted);
    firePropertyChange("highlightedCount", -1, highlightedCount);
  }

  public void fireRecordDeleted(final LayerRecord record) {
    firePropertyChange("recordDeleted", null, record);
  }

  protected void fireRecordInserted(final LayerRecord record) {
    firePropertyChange("recordInserted", null, record);
  }

  protected void fireRecordsChanged() {
    firePropertyChange("recordsChanged", false, true);
  }

  protected void fireRecordUpdated(final LayerRecord record) {
    firePropertyChange("recordUpdated", null, record);
  }

  protected void fireSelected() {
    final int selectionCount = getSelectionCount();
    final boolean selected = selectionCount > 0;
    firePropertyChange("hasSelectedRecords", !selected, selected);
    firePropertyChange("selectionCount", -1, selectionCount);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerRecord> V getCachedRecord(final Identifier identifier) {
    return (V)getRecordById(identifier);
  }

  public int getCachedRecordCount(final Label cacheId) {
    final Collection<LayerRecord> cachedRecords = this.cacheIdToRecordMap.get(cacheId);
    if (cachedRecords == null) {
      return 0;
    } else {
      return cachedRecords.size();
    }
  }

  public List<LayerRecord> getCachedRecords(final Label cacheId) {
    synchronized (getSync()) {
      final List<LayerRecord> records = new ArrayList<>();
      final Collection<LayerRecord> cachedRecords = this.cacheIdToRecordMap.get(cacheId);
      if (cachedRecords != null) {
        records.addAll(cachedRecords);
      }
      return records;
    }
  }

  protected final Label getCacheIdDeleted() {
    return this.cacheIdDeleted;
  }

  protected final Label getCacheIdHighlighted() {
    return this.cacheIdHighlighted;
  }

  public Label getCacheIdIndex() {
    return this.cacheIdIndex;
  }

  protected final Label getCacheIdModified() {
    return this.cacheIdModified;
  }

  protected final Label getCacheIdNew() {
    return this.cacheIdNew;
  }

  protected final Label getCacheIdSelected() {
    return this.cacheIdSelected;
  }

  public int getChangeCount() {
    int changeCount = 0;
    synchronized (getSync()) {
      changeCount += getNewRecordCount();
      changeCount += getModifiedRecordCount();
      changeCount += getDeletedRecordCount();
    }
    return changeCount;
  }

  public List<LayerRecord> getChanges() {
    synchronized (getSync()) {
      final List<LayerRecord> records = new ArrayList<LayerRecord>();
      records.addAll(getNewRecords());
      records.addAll(getModifiedRecords());
      records.addAll(getDeletedRecords());
      return records;
    }
  }

  @Override
  public Collection<Class<?>> getChildClasses() {
    return Collections.<Class<?>> singleton(AbstractRecordLayerRenderer.class);
  }

  public CoordinateSystem getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getCoordinateSystem();
    }
  }

  public int getDeletedRecordCount() {
    return getCachedRecordCount(this.cacheIdDeleted);
  }

  public Collection<LayerRecord> getDeletedRecords() {
    return getCachedRecords(this.cacheIdDeleted);
  }

  public synchronized Object getEditSync() {
    if (this.editSync == null) {
      this.editSync = new Object();
    }
    return this.editSync;
  }

  public List<String> getFieldNames() {
    return new ArrayList<>(this.fieldNames);
  }

  public List<String> getFieldNamesSet() {
    return getFieldNamesSet(this.fieldNamesSetName);
  }

  public List<String> getFieldNamesSet(final String fieldNamesSetName) {
    if (Property.hasValue(fieldNamesSetName)) {
      List<String> fieldNames = this.fieldNamesSets.get(fieldNamesSetName.toUpperCase());
      if (Property.hasValue(fieldNames)) {
        fieldNames = new ArrayList<>(fieldNames);
        if (Property.hasValue(this.fieldNames)) {
          fieldNames.retainAll(this.fieldNames);
        }
        return fieldNames;
      }
    }
    return getFieldNames();
  }

  public String getFieldNamesSetName() {
    return this.fieldNamesSetName;
  }

  public List<String> getFieldNamesSetNames() {
    return new ArrayList<>(this.fieldNamesSetNames);
  }

  public Map<String, List<String>> getFieldNamesSets() {
    final Map<String, List<String>> fieldNamesSets = new LinkedHashMap<>();
    for (final String fieldNamesSetName : getFieldNamesSetNames()) {
      final List<String> fieldNames = getFieldNamesSet(fieldNamesSetName);
      fieldNamesSets.put(fieldNamesSetName, fieldNames);
    }
    return fieldNamesSets;
  }

  public String getFieldTitle(final String fieldName) {
    if (isUseFieldTitles()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      return recordDefinition.getFieldTitle(fieldName);
    } else {
      return fieldName;
    }
  }

  public String getGeometryFieldName() {
    if (this.recordDefinition == null) {
      return "";
    } else {
      return getRecordDefinition().getGeometryFieldName();
    }
  }

  public DataType getGeometryType() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField == null) {
        return null;
      } else {
        return geometryField.getType();
      }
    }
  }

  public BoundingBox getHighlightedBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    BoundingBox boundingBox = geometryFactory.boundingBox();
    for (final Record record : getHighlightedRecords()) {
      final Geometry geometry = record.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  public int getHighlightedCount() {
    return getCachedRecordCount(this.cacheIdHighlighted);
  }

  public Collection<LayerRecord> getHighlightedRecords() {
    return getCachedRecords(this.cacheIdHighlighted);
  }

  public String getIdFieldName() {
    return getRecordDefinition().getIdFieldName();
  }

  protected Collection<String> getIgnorePasteFields() {
    Collection<String> ignorePasteFields = getProperty("ignorePasteFields");
    if (ignorePasteFields == null) {
      ignorePasteFields = Collections.emptySet();
    }
    return ignorePasteFields;
  }

  public RecordQuadTree getIndex() {
    return this.index;
  }

  public List<LayerRecord> getMergeableSelectedRecords() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return new ArrayList<>();
    } else {
      final List<LayerRecord> selectedRecords = getSelectedRecords();
      for (final ListIterator<LayerRecord> iterator = selectedRecords.listIterator(); iterator.hasNext();) {
        final LayerRecord record = iterator.next();
        if (record == null || record.isDeleted()) {
          iterator.remove();
        } else {
          Geometry geometry = record.getGeometryValue();
          if (geometry == null || geometry.isEmpty()) {
            iterator.remove();
          } else {
            geometry = geometryFactory.geometry(LineString.class, geometry);
            if (!(geometry instanceof LineString)) {
              iterator.remove();
            }
          }
        }
      }
      return selectedRecords;
    }
  }

  /**
   * Get a record containing the values of the two records if they can be
   * merged. The new record is not a layer data object so would need to be
   * added, likewise the old records are not removed so they would need to be
   * deleted.
   *
   * @param point
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecord(final Point point, final Record record1, final Record record2) {
    if (record1 == record2) {
      return record1;
    } else {
      final String sourceIdFieldName = getIdFieldName();
      final Object id1 = record1.getValue(sourceIdFieldName);
      final Object id2 = record2.getValue(sourceIdFieldName);
      int compare = 0;
      if (id1 == null) {
        if (id2 != null) {
          compare = 1;
        }
      } else if (id2 == null) {
        compare = -1;
      } else {
        compare = CompareUtil.compare(id1, id2);
      }
      if (compare == 0) {
        final Geometry geometry1 = record1.getGeometryValue();
        final Geometry geometry2 = record2.getGeometryValue();
        final double length1 = geometry1.getLength();
        final double length2 = geometry2.getLength();
        if (length1 > length2) {
          compare = -1;
        } else {
          compare = 1;
        }
      }
      if (compare > 0) {
        return getMergedRecord(point, record2, record1);
      } else {
        final DirectionalAttributes property = DirectionalAttributes.getProperty(getRecordDefinition());
        final Map<String, Object> newValues = property.getMergedMap(point, record1, record2);
        newValues.remove(getIdFieldName());
        return new ArrayRecord(getRecordDefinition(), newValues);
      }
    }
  }

  public int getModifiedRecordCount() {
    return getCachedRecordCount(this.cacheIdModified);
  }

  public Collection<LayerRecord> getModifiedRecords() {
    return getCachedRecords(this.cacheIdModified);
  }

  public int getNewRecordCount() {
    return getCachedRecordCount(this.cacheIdNew);
  }

  public List<LayerRecord> getNewRecords() {
    return getCachedRecords(this.cacheIdNew);
  }

  protected Geometry getPasteRecordGeometry(final LayerRecord record, final boolean alert) {
    try {
      if (record == null) {
        return null;
      } else {
        final RecordDefinition recordDefinition = getRecordDefinition();
        final FieldDefinition geometryField = recordDefinition.getGeometryField();
        if (geometryField != null) {
          final MapPanel parentComponent = MapPanel.get(getProject());
          Geometry geometry = null;
          DataType geometryDataType = null;
          Class<?> layerGeometryClass = null;
          final GeometryFactory geometryFactory = getGeometryFactory();
          geometryDataType = geometryField.getType();
          layerGeometryClass = geometryDataType.getJavaClass();
          RecordReader reader = ClipboardUtil.getContents(RecordReaderTransferable.DATA_OBJECT_READER_FLAVOR);
          if (reader == null) {
            final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
            if (Property.hasValue(string)) {
              try {
                geometry = geometryFactory.geometry(string);
                geometry = geometryFactory.geometry(layerGeometryClass, geometry);
                if (geometry != null) {
                  return geometry;
                }
              } catch (final Throwable e) {
              }
              final Resource resource = new ByteArrayResource("t.csv", string);
              reader = RecordIo.recordReader(resource);
            } else {
              return null;
            }
          }
          if (reader != null) {
            try {

              for (final Record sourceRecord : reader) {
                if (geometry == null) {
                  final Geometry sourceGeometry = sourceRecord.getGeometryValue();
                  if (sourceGeometry == null) {
                    if (alert) {
                      JOptionPane.showMessageDialog(parentComponent,
                        "Clipboard does not contain a record with a geometry.", "Paste Geometry",
                        JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                  }
                  geometry = geometryFactory.geometry(layerGeometryClass, sourceGeometry);
                  if (geometry == null) {
                    if (alert) {
                      JOptionPane.showMessageDialog(parentComponent,
                        "Clipboard should contain a record with a " + geometryDataType + " not a "
                          + sourceGeometry.getGeometryType() + ".", "Paste Geometry",
                        JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                  }
                } else {
                  if (alert) {
                    JOptionPane.showMessageDialog(parentComponent,
                      "Clipboard contains more than one record. Copy a single record.",
                      "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                  }
                  return null;
                }
              }
            } finally {
              FileUtil.closeSilent(reader);
            }
            if (geometry == null) {
              if (alert) {
                JOptionPane.showMessageDialog(parentComponent,
                  "Clipboard does not contain a record with a geometry.", "Paste Geometry",
                  JOptionPane.ERROR_MESSAGE);
              }
            } else if (geometry.isEmpty()) {
              if (alert) {
                JOptionPane.showMessageDialog(parentComponent,
                  "Clipboard contains an empty geometry.", "Paste Geometry",
                  JOptionPane.ERROR_MESSAGE);
              }
              return null;
            } else {
              return geometry;
            }
          }
        }
        return null;
      }
    } catch (final Throwable t) {
      return null;
    }
  }

  public List<AbstractProxyLayerRecord> getProxyRecords() {
    return new ArrayList<>(this.proxyRecords);
  }

  public Query getQuery() {
    return this.query.clone();
  }

  public LayerRecord getRecord(final int row) {
    throw new UnsupportedOperationException();
  }

  public LayerRecord getRecordById(final Identifier id) {
    return null;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public List<LayerRecord> getRecords() {
    throw new UnsupportedOperationException();
  }

  public RecordStore getRecordStore() {
    return getRecordDefinition().getRecordStore();
  }

  public int getRowCount() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Query query = new Query(recordDefinition);
    return getRowCount(query);
  }

  public int getRowCount(final Query query) {
    LoggerFactory.getLogger(getClass()).error("Get row count not implemented");
    return 0;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (final Record record : getSelectedRecords()) {
      final Geometry geometry = record.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  public List<LayerRecord> getSelectedRecords() {
    return getCachedRecords(this.cacheIdSelected);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public List<LayerRecord> getSelectedRecords(final BoundingBox boundingBox) {
    final RecordQuadTree index = getSelectedRecordsIndex();
    return (List)index.queryIntersects(boundingBox);
  }

  protected RecordQuadTree getSelectedRecordsIndex() {
    if (this.selectedRecordsIndex == null) {
      final List<LayerRecord> selectedRecords = getSelectedRecords();
      final RecordQuadTree index = new RecordQuadTree(getProject().getGeometryFactory(),
        selectedRecords);
      this.selectedRecordsIndex = index;
    }
    return this.selectedRecordsIndex;
  }

  public int getSelectionCount() {
    return getCachedRecordCount(this.cacheIdSelected);
  }

  public Collection<String> getSnapLayerPaths() {
    return getProperty("snapLayers", Collections.<String> emptyList());
  }

  public String getTypePath() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getPath();
    }
  }

  public Collection<String> getUserReadOnlyFieldNames() {
    return Collections.unmodifiableSet(this.userReadOnlyFieldNames);
  }

  public String getWhere() {
    return this.query.getWhere();
  }

  public Condition getWhereCondition() {
    return this.query.getWhereCondition();
  }

  public boolean hasFieldNamesSet(final String fieldNamesSetName) {
    if (Property.hasValue(fieldNamesSetName)) {
      final List<String> fieldNames = this.fieldNamesSets.get(fieldNamesSetName.toUpperCase());
      if (Property.hasValue(fieldNames)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasGeometryAttribute() {
    return getRecordDefinition().getGeometryField() != null;
  }

  protected boolean hasPermission(final String permission) {
    if (this.recordDefinition == null) {
      return true;
    } else {
      final Collection<String> permissions = this.recordDefinition.getProperty("permissions");
      if (permissions == null) {
        return true;
      } else {
        final boolean hasPermission = permissions.contains(permission);
        return hasPermission;
      }
    }
  }

  /**
   * Cancel changes for one of the lists of changes {@link #deletedRecords},
   * {@link #newRecords}, {@link #modifiedRecords}.
   * @param cacheId
   *
   * @param records
   */
  private boolean internalCancelChanges(final Label cacheId, final Collection<LayerRecord> records) {
    boolean cancelled = true;
    for (final LayerRecord record : records) {
      removeFromIndex(record);
      try {
        removeRecordFromCache(cacheId, record);
        if (cacheId == this.cacheIdNew) {
          removeRecordFromCache(record);
          record.setState(RecordState.Deleted);
        } else {
          internalCancelChanges(record);
          addToIndex(record);
        }
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Unable to cancel changes.\n" + record, e);
        cancelled = false;
      }
    }
    return cancelled;
  }

  /**
   * Revert the values of the record to the last values loaded from the database
   *
   * @param record
   */
  protected void internalCancelChanges(final LayerRecord record) {
    if (record != null) {
      record.cancelChanges();
    }
  }

  protected boolean internalIsDeleted(final LayerRecord record) {
    return isRecordCached(this.cacheIdDeleted, record);
  }

  /**
   * Revert the values of the record to the last values loaded from the database
   *
   * @param record
   */
  protected LayerRecord internalPostSaveChanges(final LayerRecord record) {
    if (record != null) {
      record.clearChanges();
      return record;
    }
    return null;
  }

  protected boolean internalSaveChanges(final RecordSaveErrorTableModel errors,
    final LayerRecord record) {
    final RecordState originalState = record.getState();
    final boolean saved = doSaveChanges(record);
    if (saved) {
      postSaveChanges(originalState, record);
    }
    return saved;
  }

  public boolean isCanAddRecords() {
    return !super.isReadOnly() && isEditable() && this.canAddRecords && hasPermission("INSERT");
  }

  public boolean isCanDeleteRecords() {
    return !super.isReadOnly() && isEditable() && this.canDeleteRecords && hasPermission("DELETE");
  }

  public boolean isCanEditRecords() {
    return !super.isReadOnly() && isEditable() && this.canEditRecords && hasPermission("UPDATE");
  }

  public boolean isCanMergeRecords() {
    if (isCanAddRecords()) {
      if (isCanDeleteRecords()) {
        final DataType geometryType = getGeometryType();
        if (DataTypes.POINT.equals(geometryType)) {
          return false;
        } else if (DataTypes.MULTI_POINT.equals(geometryType)) {
          return false;
        } else if (DataTypes.POLYGON.equals(geometryType)) {
          return false;
        } else if (DataTypes.MULTI_POLYGON.equals(geometryType)) {
          return false;
        }
        if (getMergeableSelectedRecords().size() > 1) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isCanPaste() {
    if (isExists()) {
      if (ClipboardUtil.isDataFlavorAvailable(RecordReaderTransferable.DATA_OBJECT_READER_FLAVOR)) {
        return true;
      } else {
        if (ClipboardUtil.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
          final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
          if (Property.hasValue(string)) {
            int lineIndex = string.indexOf('\n');
            if (lineIndex == -1) {
              lineIndex = string.indexOf('\r');
            }
            if (lineIndex != -1) {
              final String line = string.substring(0, lineIndex).trim();
              String fieldName;
              final int tabIndex = line.indexOf('\t');
              if (tabIndex != -1) {
                fieldName = line.substring(0, tabIndex);

              } else {
                final int commaIndex = line.indexOf(',');
                if (commaIndex != -1) {
                  fieldName = line.substring(0, commaIndex);
                } else {
                  fieldName = line;
                }
              }
              if (fieldName.startsWith("\"")) {
                fieldName = fieldName.substring(1);
              }
              if (fieldName.endsWith("\"")) {
                fieldName = fieldName.substring(0, fieldName.length() - 1);
              }
              if (getRecordDefinition().hasField(fieldName)) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isClonable() {
    return true;
  }

  public boolean isDeleted(final LayerRecord record) {
    return internalIsDeleted(record);
  }

  public boolean isEmpty() {
    return getRowCount() + getNewRecordCount() <= 0;
  }

  public boolean isFieldUserReadOnly(final String fieldName) {
    return getUserReadOnlyFieldNames().contains(fieldName);
  }

  public boolean isHasCachedRecords(final Label cacheId) {
    return getCachedRecordCount(cacheId) > 0;
  }

  @Override
  public boolean isHasChanges() {
    if (isEditable()) {
      synchronized (this.getEditSync()) {
        if (isHasCachedRecords(this.cacheIdNew)) {
          return true;
        } else if (isHasCachedRecords(this.cacheIdModified)) {
          return true;
        } else if (isHasCachedRecords(this.cacheIdDeleted)) {
          return true;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean isHasGeometry() {
    return getGeometryFieldName() != null;
  }

  @Override
  public boolean isHasSelectedRecords() {
    return isHasCachedRecords(this.cacheIdSelected);
  }

  public boolean isHidden(final LayerRecord record) {
    if (isCanDeleteRecords() && isDeleted(record)) {
      return true;
    } else if (isSelectable() && isSelected(record)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isHighlighted(final LayerRecord record) {

    return isRecordCached(this.cacheIdHighlighted, record);
  }

  public boolean isLayerRecord(final Record record) {
    if (record == null) {
      return false;
    } else if (record.getRecordDefinition() == getRecordDefinition()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isModified(final LayerRecord record) {
    return isRecordCached(this.cacheIdModified, record);
  }

  public boolean isNew(final LayerRecord record) {
    return isRecordCached(this.cacheIdNew, record);
  }

  protected boolean isPostSaveRemoveCacheId(final Label cacheId) {
    if (cacheId == this.cacheIdDeleted || cacheId == this.cacheIdNew
      || cacheId == this.cacheIdModified) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isReadOnly() {
    if (super.isReadOnly()) {
      return true;
    } else {
      if (this.canAddRecords && hasPermission("INSERT")) {
        return false;
      } else if (this.canDeleteRecords && hasPermission("DELETE")) {
        return false;
      } else if (this.canEditRecords && hasPermission("UPDATE")) {
        return false;
      } else {
        return true;
      }
    }
  }

  public boolean isRecordCached(final Label cacheId, final LayerRecord record) {
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        final Collection<LayerRecord> cachedRecords = this.cacheIdToRecordMap.get(cacheId);
        if (cachedRecords != null) {
          return cachedRecords.contains(record);
        }
      }
    }
    return false;
  }

  public boolean isSelected(final LayerRecord record) {
    return isRecordCached(this.cacheIdSelected, record);
  }

  public boolean isSnapToAllLayers() {
    return this.snapToAllLayers;
  }

  public boolean isUseFieldTitles() {
    return this.useFieldTitles;
  }

  public boolean isVisible(final LayerRecord record) {
    if (isExists() && isVisible()) {
      final AbstractRecordLayerRenderer renderer = getRenderer();
      if (renderer == null || renderer.isVisible(record)) {
        return true;
      }
    }
    return false;
  }

  public void mergeSelectedRecords() {
    if (isCanMergeRecords()) {
      Invoke.later(MergeRecordsDialog.class, "showDialog", this);
    }
  }

  public void pasteRecordGeometry(final LayerRecord record) {
    final Geometry geometry = getPasteRecordGeometry(record, true);
    if (geometry != null) {
      record.setGeometryValue(geometry);
    }
  }

  public List<LayerRecord> pasteRecords() {
    final List<LayerRecord> newRecords = new ArrayList<>();
    try (
      EventsEnabledState eventsEnabled = EventsEnabledState.disabled(this)) {
      RecordReader reader = ClipboardUtil.getContents(RecordReaderTransferable.DATA_OBJECT_READER_FLAVOR);
      if (reader == null) {
        final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
        if (Property.hasValue(string)) {
          if (string.contains("\t")) {
            final Resource tsvResource = new ByteArrayResource("t.tsv", string);
            reader = RecordIo.recordReader(tsvResource);
          } else {
            final Resource csvResource = new ByteArrayResource("t.csv", string);
            reader = RecordIo.recordReader(csvResource);
          }
        }
      }
      final List<Record> rejectedRecords = new ArrayList<Record>();
      if (reader != null) {
        final RecordDefinition recordDefinition = getRecordDefinition();
        final FieldDefinition geometryField = recordDefinition.getGeometryField();
        DataType geometryDataType = null;
        Class<?> layerGeometryClass = null;
        final GeometryFactory geometryFactory = getGeometryFactory();
        if (geometryField != null) {
          geometryDataType = geometryField.getType();
          layerGeometryClass = geometryDataType.getJavaClass();
        }
        final Collection<String> ignorePasteFields = getIgnorePasteFields();
        for (final Record sourceRecord : reader) {
          final Map<String, Object> newValues = new LinkedHashMap<String, Object>(sourceRecord);

          Geometry sourceGeometry = sourceRecord.getGeometryValue();
          for (final Iterator<String> iterator = newValues.keySet().iterator(); iterator.hasNext();) {
            final String fieldName = iterator.next();
            final FieldDefinition attribute = recordDefinition.getField(fieldName);
            if (attribute == null) {
              iterator.remove();
            } else if (ignorePasteFields != null) {
              if (ignorePasteFields.contains(attribute.getName())) {
                iterator.remove();
              }
            }
          }
          if (geometryDataType != null) {
            if (sourceGeometry == null) {
              final Object value = sourceRecord.getValue(geometryField.getName());
              sourceGeometry = StringConverterRegistry.toObject(Geometry.class, value);
            }
            final Geometry geometry = geometryFactory.geometry(layerGeometryClass, sourceGeometry);
            if (geometry == null) {
              newValues.clear();
            } else {
              final String geometryFieldName = geometryField.getName();
              newValues.put(geometryFieldName, geometry);
            }
          }
          LayerRecord newRecord = null;
          if (newValues.isEmpty()) {
            rejectedRecords.add(sourceRecord);
          } else {
            newRecord = createRecord(newValues);
          }
          if (newRecord == null) {
            rejectedRecords.add(sourceRecord);
          } else {
            recordPasted(newRecord);
            newRecords.add(newRecord);
          }
        }
      }
      saveChanges(newRecords);
      if (!newRecords.isEmpty()) {
        zoomToRecords(newRecords);
        showRecordsTable(RecordLayerTableModel.MODE_EDITS);
      }
    }
    firePropertyChange("recordsInserted", null, newRecords);
    addSelectedRecords(newRecords);
    return newRecords;
  }

  protected void postSaveChanges(final RecordState originalState, final LayerRecord record) {
    postSaveDeletedRecord(record);
    postSaveModifiedRecord(record);
    postSaveNewRecord(record);
  }

  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted;
    synchronized (getSync()) {
      deleted = removeRecordFromCache(this.cacheIdDeleted, record);
    }
    if (deleted) {
      removeRecordFromCache(record);
      removeFromIndex(record);
      return true;
    } else {
      return false;
    }
  }

  protected boolean postSaveModifiedRecord(final LayerRecord record) {
    synchronized (getSync()) {
      if (removeRecordFromCache(this.cacheIdModified, record)) {
        record.postSaveModified();
        return true;
      } else {
        return false;
      }
    }
  }

  protected boolean postSaveNewRecord(final LayerRecord record) {
    synchronized (getSync()) {
      final boolean selected = isSelected(record);
      final boolean highlighted = isHighlighted(record);
      if (removeRecordFromCache(this.cacheIdNew, record)) {
        removeRecordFromCache(record);
        record.postSaveNew();
        addToIndex(record);
        setSelectedHighlighted(record, selected, highlighted);
        return true;
      }
    }
    return false;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    if (isExists()) {
      final Object source = event.getSource();
      final String propertyName = event.getPropertyName();
      if (!"qaMessagesUpdated".equals(propertyName)) {
        if (source instanceof LayerRecord) {
          final LayerRecord record = (LayerRecord)source;
          if (record.getLayer() == this) {
            if (EqualsRegistry.equal(propertyName, getGeometryFieldName())) {
              final Geometry oldGeometry = (Geometry)event.getOldValue();
              updateSpatialIndex(record, oldGeometry);
              clearSelectedRecordsIndex();
            }
          }
        }
      }
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public final List<LayerRecord> query(BoundingBox boundingBox) {
    if (hasGeometryAttribute()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
      final List<LayerRecord> results = doQuery(boundingBox);
      final Filter filter = new RecordGeometryBoundingBoxIntersectsFilter(boundingBox);
      return filterQueryResults(results, filter);
    } else {
      return Collections.emptyList();
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public List<LayerRecord> query(final Geometry geometry, final double maxDistance) {
    if (hasGeometryAttribute()) {
      final List<LayerRecord> results = doQuery(geometry, maxDistance);
      final Filter filter = new RecordGeometryDistanceFilter(geometry, maxDistance);
      return filterQueryResults(results, filter);
    } else {
      return Collections.emptyList();
    }
  }

  public List<LayerRecord> query(final Query query) {
    final List<LayerRecord> results = doQuery(query);
    final Condition condition = query.getWhereCondition();
    // TODO sorting
    return filterQueryResults(results, condition);
  }

  public final List<LayerRecord> queryBackground(BoundingBox boundingBox) {
    if (hasGeometryAttribute()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
      final List<LayerRecord> results = doQueryBackground(boundingBox);
      return results;
    } else {
      return Collections.emptyList();
    }
  }

  protected void recordPasted(final LayerRecord newRecord) {
  }

  protected void removeForm(final LayerRecord record) {
    if (record != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final int index = this.formRecords.indexOf(record);
        if (index != -1) {
          this.formRecords.remove(index);
          final Component form = this.formComponents.remove(index);
          if (form != null) {
            if (form instanceof LayerRecordForm) {
              final LayerRecordForm recordForm = (LayerRecordForm)form;
              recordForm.destroy();
            }
          }
          final Window window = this.formWindows.remove(index);
          if (window != null) {
            window.dispose();
          }
        }

        cleanCachedRecords();
      } else {
        Invoke.later(this, "removeForm", record);
      }
    }
  }

  public void removeForms(final Collection<LayerRecord> records) {
    if (records != null && !records.isEmpty()) {
      if (SwingUtilities.isEventDispatchThread()) {
        for (final LayerRecord record : records) {
          removeForm(record);
        }
      } else {
        Invoke.later(this, "removeForms", records);
      }
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public boolean removeFromIndex(final BoundingBox boundingBox, final LayerRecord record) {
    boolean removed = false;
    final RecordQuadTree index = getIndex();
    final List<LayerRecord> records = (List)index.query(boundingBox);
    for (final LayerRecord indexRecord : records) {
      if (indexRecord.isSame(record)) {
        index.remove(indexRecord);
        removed = true;
      }
    }
    return removed;
  }

  public void removeFromIndex(final Collection<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      removeFromIndex(record);
    }
  }

  public void removeFromIndex(final LayerRecord record) {
    final Geometry geometry = record.getGeometryValue();
    if (geometry != null && !geometry.isEmpty()) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      removeFromIndex(boundingBox, record);
      removeRecordFromCache(this.cacheIdIndex, record);
    }
  }

  protected void removeHighlightedRecord(final LayerRecord record) {
    removeRecordFromCache(this.cacheIdHighlighted, record);
  }

  public void removeProxyRecord(final AbstractProxyLayerRecord proxyRecord) {
    this.proxyRecords.remove(proxyRecord);
  }

  public boolean removeRecordFromCache(final Label cacheId, final LayerRecord record) {
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        return Maps.removeFromCollection(this.cacheIdToRecordMap, cacheId, record);
      }
    }
    return false;
  }

  public boolean removeRecordFromCache(final LayerRecord record) {
    boolean removed = false;
    synchronized (getSync()) {
      if (isLayerRecord(record)) {
        for (final Label cacheId : new ArrayList<>(this.cacheIdToRecordMap.keySet())) {
          removed |= removeRecordFromCache(cacheId, record);
        }
      }
    }
    return removed;
  }

  public int removeRecordsFromCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      int count = 0;
      for (final LayerRecord record : records) {
        if (removeRecordFromCache(cacheId, record)) {
          count++;
        }
      }
      cleanCachedRecords();
      return count;
    }
  }

  protected void removeSelectedRecord(final LayerRecord record) {
    removeRecordFromCache(this.cacheIdSelected, record);
    removeHighlightedRecord(record);
    clearSelectedRecordsIndex();
  }

  public void replaceValues(final LayerRecord record, final Map<String, Object> values) {
    record.setValues(values);
  }

  public void revertChanges(final LayerRecord record) {
    synchronized (getSync()) {
      if (isLayerRecord(record)) {
        final boolean selected = isSelected(record);
        final boolean highlighted = isHighlighted(record);
        postSaveModifiedRecord(record);
        if (removeRecordFromCache(this.cacheIdDeleted, record)) {
          record.setState(RecordState.Persisted);
        }
        removeRecordFromCache(record);
        setSelectedHighlighted(record, selected, highlighted);
        cleanCachedRecords();
      }
    }
  }

  @Override
  public boolean saveChanges() {
    synchronized (this.getEditSync()) {
      boolean allSaved = true;
      if (isHasChanges()) {
        final RecordSaveErrorTableModel errors = new RecordSaveErrorTableModel(this);
        try (
          EventsEnabledState eventsEnabled = EventsEnabledState.disabled(this)) {
          doSaveChanges(errors);
        } finally {
          cleanCachedRecords();
          fireRecordsChanged();
          allSaved = errors.showErrorDialog();
        }
      }
      return allSaved;
    }
  }

  public final boolean saveChanges(final Collection<? extends LayerRecord> records) {
    synchronized (this.getEditSync()) {
      boolean allSaved;
      final RecordSaveErrorTableModel errors = new RecordSaveErrorTableModel(this);
      try (
        EventsEnabledState eventsEnabled = EventsEnabledState.disabled(this)) {
        for (final LayerRecord record : records) {
          try {
            if (isLayerRecord(record)) {
              if (!internalSaveChanges(errors, record)) {
                errors.addRecord(record, "Unknown error");
              }
            }
          } catch (final Throwable t) {
            errors.addRecord(record, t);
          }
        }
        cleanCachedRecords();
        fireRecordsChanged();
      } finally {
        allSaved = errors.showErrorDialog();
      }
      return allSaved;
    }
  }

  public final boolean saveChanges(final LayerRecord... records) {
    final List<LayerRecord> list = Arrays.asList(records);
    return saveChanges(list);
  }

  public final boolean saveChanges(final LayerRecord record) {
    synchronized (this.getEditSync()) {
      boolean allSaved;
      final RecordSaveErrorTableModel errors = new RecordSaveErrorTableModel(this);
      try (
        EventsEnabledState eventsEnabled = EventsEnabledState.disabled(this)) {
        try {
          final boolean saved = internalSaveChanges(errors, record);
          if (!saved) {
            errors.addRecord(record, "Unknown error");
          }
        } catch (final Throwable t) {
          errors.addRecord(record, t);
        }
        cleanCachedRecords();
        fireRecordUpdated(record);
      } finally {
        allSaved = errors.showErrorDialog();
      }
      return allSaved;
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCanAddRecords(final boolean canAddRecords) {
    this.canAddRecords = canAddRecords;
    firePropertyChange("canAddRecords", !isCanAddRecords(), isCanAddRecords());
  }

  public void setCanDeleteRecords(final boolean canDeleteRecords) {
    this.canDeleteRecords = canDeleteRecords;
    firePropertyChange("canDeleteRecords", !isCanDeleteRecords(), isCanDeleteRecords());
  }

  public void setCanEditRecords(final boolean canEditRecords) {
    this.canEditRecords = canEditRecords;
    firePropertyChange("canEditRecords", !isCanEditRecords(), isCanEditRecords());
  }

  @Override
  public void setEditable(final boolean editable) {
    if (SwingUtilities.isEventDispatchThread()) {
      Invoke.background("Set editable", this, "setEditable", editable);
    } else {
      synchronized (this.getEditSync()) {
        if (editable == false) {
          firePropertyChange("preEditable", false, true);
          if (isHasChanges()) {
            final Integer result = InvokeMethodCallable.invokeAndWait(
              JOptionPane.class,
              "showConfirmDialog",
              JOptionPane.getRootFrame(),
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
              if (!saveChanges()) {
                return;
              }
            } else if (result == JOptionPane.NO_OPTION) {
              cancelChanges();
            } else {
              // Don't allow state change if cancelled
              return;
            }

          }
        }
        super.setEditable(editable);
        setCanAddRecords(this.canAddRecords);
        setCanDeleteRecords(this.canDeleteRecords);
        setCanEditRecords(this.canEditRecords);
      }
    }
  }

  public void setFieldNamesSetName(final String fieldNamesSetName) {
    final String oldValue = this.fieldNamesSetName;
    if (Property.hasValue(fieldNamesSetName)) {
      this.fieldNamesSetName = fieldNamesSetName;
    } else {
      this.fieldNamesSetName = ALL;
    }
    firePropertyChange("fieldNamesSetName", oldValue, this.fieldNamesSetName);
  }

  public void setFieldNamesSets(final Map<String, List<String>> fieldNamesSets) {
    final List<String> allFieldNames = this.fieldNames;
    this.fieldNamesSetNames.clear();
    this.fieldNamesSetNames.add("All");
    this.fieldNamesSets.clear();
    if (fieldNamesSets != null) {
      for (final Entry<String, List<String>> entry : fieldNamesSets.entrySet()) {
        final String name = entry.getKey();
        if (Property.hasValue(name)) {
          final String upperName = name.toUpperCase();
          final Collection<String> names = entry.getValue();
          if (Property.hasValue(names)) {
            final Set<String> fieldNames = new LinkedHashSet<>(names);
            if (ALL.equalsIgnoreCase(name)) {
              if (Property.hasValue(allFieldNames)) {
                fieldNames.addAll(allFieldNames);
              }
            } else {
              boolean found = false;
              for (final String name2 : this.fieldNamesSetNames) {
                if (name.equalsIgnoreCase(name2)) {
                  found = true;
                  LoggerFactory.getLogger(getClass()).error(
                    "Duplicate field set name " + name + "=" + name2 + " for layer " + getPath());
                }
              }
              if (!found) {
                this.fieldNamesSetNames.add(name);
              }
            }
            if (Property.hasValue(allFieldNames)) {
              fieldNames.retainAll(allFieldNames);
            }
            this.fieldNamesSets.put(upperName, new ArrayList<>(fieldNames));
          }
        }
      }
    }
    getFieldNamesSet(ALL);
    firePropertyChange("fieldNamesSets", null, this.fieldNamesSets);
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
    if (geometryFactory != null && this.boundingBox.isEmpty()) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        this.boundingBox = coordinateSystem.getAreaBoundingBox();
      }
    }
  }

  public void setHighlightedRecords(final Collection<LayerRecord> highlightedRecords) {
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdHighlighted);
      addHighlightedRecords(highlightedRecords);
    }
  }

  protected void setIndexRecords(final List<LayerRecord> records) {
    synchronized (getSync()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final RecordQuadTree index = new RecordQuadTree(geometryFactory);
      final Label cacheIdIndex = getCacheIdIndex();
      clearCachedRecords(cacheIdIndex);
      if (records != null) {
        for (final Record record : records) {
          final LayerRecord cacheRecord = addRecordToCache(cacheIdIndex, (LayerRecord)record);
          if (!cacheRecord.isDeleted()) {
            index.insert(cacheRecord);
          }
        }
      }
      cleanCachedRecords();
      final List<LayerRecord> newRecords = getNewRecords();
      index.insert(newRecords);
      this.index = index;
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("style".equals(name)) {
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> style = (Map<String, Object>)value;
        final LayerRenderer<AbstractRecordLayer> renderer = AbstractRecordLayerRenderer.getRenderer(
          this, style);
        if (renderer != null) {
          setRenderer(renderer);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  public void setQuery(Query query) {
    final Query oldQuery = this.query;
    if (query == null) {
      query = this.query.clone();
      query.setWhereCondition(query.getWhereCondition());
    }
    this.query = query;
    firePropertyChange("query", oldQuery, this.query);
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    if (recordDefinition != null) {
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      String geometryType = null;
      GeometryFactory geometryFactory;
      if (geometryField == null) {
        geometryFactory = null;
        setVisible(false);
        setSelectSupported(false);
        setRenderer(null);
      } else {
        geometryFactory = recordDefinition.getGeometryFactory();
        geometryType = geometryField.getType().toString();
      }
      setGeometryFactory(geometryFactory);
      final Icon icon = RecordStoreTableTreeNode.getIcon(geometryType);
      setIcon(icon);
      this.fieldNames = recordDefinition.getFieldNames();
      List<String> allFieldNames = this.fieldNamesSets.get(ALL.toUpperCase());
      if (Property.hasValue(allFieldNames)) {
        final Set<String> mergedFieldNames = new LinkedHashSet<>(allFieldNames);
        mergedFieldNames.addAll(this.fieldNames);
        mergedFieldNames.retainAll(this.fieldNames);
        allFieldNames = new ArrayList<>(mergedFieldNames);
      } else {
        allFieldNames = new ArrayList<>(this.fieldNames);
      }
      this.fieldNamesSets.put(ALL.toUpperCase(), allFieldNames);
      this.query.setRecordDefinition(recordDefinition);
    }

  }

  protected void setSelectedHighlighted(final LayerRecord record, final boolean selected,
    final boolean highlighted) {
    if (selected) {
      addSelectedRecord(record);
      if (highlighted) {
        addHighlightedRecord(record);
      }
    }
  }

  public void setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = query(boundingBox);
      for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
        final LayerRecord layerRecord = iterator.next();
        if (!isVisible(layerRecord) || internalIsDeleted(layerRecord)) {
          iterator.remove();
        }
      }
      setSelectedRecords(records);
      if (isHasSelectedRecords()) {
        showRecordsTable(RecordLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void setSelectedRecords(final Collection<LayerRecord> selectedRecords) {
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdSelected);
      for (final LayerRecord record : selectedRecords) {
        addSelectedRecord(record);
      }
      for (final LayerRecord record : getHighlightedRecords()) {
        if (!isSelected(record)) {
          removeHighlightedRecord(record);
        }
      }
    }
    fireSelected();
  }

  public void setSelectedRecords(final LayerRecord... selectedRecords) {
    setSelectedRecords(Arrays.asList(selectedRecords));
  }

  public void setSelectedRecordsById(final Object id) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (idFieldName == null) {
        clearSelectedRecords();
      } else {
        final Query query = Query.equal(recordDefinition, idFieldName, id);
        final List<LayerRecord> records = query(query);
        setSelectedRecords(records);
      }
    }
  }

  public void setSnapLayerPaths(final Collection<String> snapLayerPaths) {
    if (snapLayerPaths == null || snapLayerPaths.isEmpty()) {
      removeProperty("snapLayers");
    } else {
      setProperty("snapLayers", new TreeSet<String>(snapLayerPaths));
    }
  }

  public void setSnapToAllLayers(final boolean snapToAllLayers) {
    this.snapToAllLayers = snapToAllLayers;
  }

  public void setUseFieldTitles(final boolean useFieldTitles) {
    this.useFieldTitles = useFieldTitles;
  }

  public void setUserReadOnlyFieldNames(final Collection<String> userReadOnlyFieldNames) {
    this.userReadOnlyFieldNames = new LinkedHashSet<String>(userReadOnlyFieldNames);
  }

  public void setWhere(final String where) {
    final Query query = this.query.clone();
    query.setWhere(where);
    setQuery(query);
  }

  public void setWhereCondition(final Condition whereCondition) {
    final Query query = this.query.clone();
    query.setWhereCondition(whereCondition);
    setQuery(query);
  }

  public LayerRecord showAddForm(final Map<String, Object> parameters) {
    if (isCanAddRecords()) {
      final LayerRecord newRecord = createRecord(parameters);
      final LayerRecordForm form = createForm(newRecord);
      if (form == null) {
        return null;
      } else {
        try {
          form.setAddRecord(newRecord);
          if (form.showAddDialog()) {
            return form.getAddRecord();
          } else {
            return null;
          }
        } finally {
          form.setAddRecord(null);
        }
      }
    } else {
      final Window window = SwingUtil.getActiveWindow();
      JOptionPane.showMessageDialog(window, "Adding records is not enabled for the " + getPath()
        + " layer. If possible make the layer editable", "Cannot Add Record",
        JOptionPane.ERROR_MESSAGE);
      return null;
    }

  }

  @SuppressWarnings("unchecked")
  public <V extends JComponent> V showForm(final LayerRecord record) {
    if (record == null || record.isDeleted()) {
      return null;
    } else {
      if (SwingUtilities.isEventDispatchThread()) {
        final int index = this.formRecords.indexOf(record);
        Window window;
        if (index == -1) {
          window = null;
        } else {
          window = this.formWindows.get(index);
        }
        if (window == null) {
          final Component form = createForm(record);
          final Identifier id = record.getIdentifier();
          if (form == null) {
            return null;
          } else {
            String title;
            if (record.getState() == RecordState.New) {
              title = "Add NEW " + getName();
            } else if (isCanEditRecords()) {
              title = "Edit " + getName();
              if (id != null) {
                title += " #" + id;
              }
            } else {
              title = "View " + getName();
              if (id != null) {
                title += " #" + id;
              }
              if (form instanceof LayerRecordForm) {
                final LayerRecordForm recordForm = (LayerRecordForm)form;
                recordForm.setEditable(false);
              }
            }
            final Window parent = SwingUtil.getActiveWindow();
            window = new BaseDialog(parent, title);
            window.add(form);
            window.pack();
            if (form instanceof LayerRecordForm) {
              final LayerRecordForm recordForm = (LayerRecordForm)form;
              window.addWindowListener(recordForm);
            }
            SwingUtil.autoAdjustPosition(window);
            this.formRecords.add(record);
            this.formComponents.add(form);
            this.formWindows.add(window);
            window.addWindowListener(new WindowAdapter() {

              @Override
              public void windowClosing(final WindowEvent e) {
                removeForm(record);
              }
            });
            SwingUtil.setVisible(window, true);

            window.requestFocus();
            return (V)form;
          }
        } else {
          SwingUtil.setVisible(window, true);

          window.requestFocus();
          final Component component = window.getComponent(0);
          if (component instanceof JScrollPane) {
            final JScrollPane scrollPane = (JScrollPane)component;
            return (V)scrollPane.getComponent(0);
          }
          return null;
        }
      } else {
        Invoke.later(this, "showForm", record);
        return null;
      }
    }
  }

  public void showRecordsTable() {
    showRecordsTable(RecordLayerTableModel.MODE_ALL);
  }

  public void showRecordsTable(String fieldFilterMode) {
    if (SwingUtilities.isEventDispatchThread()) {
      final RecordLayerTablePanel panel = showTableView();
      panel.setFieldFilterMode(fieldFilterMode);
    } else {
      if (!Property.hasValue(fieldFilterMode)) {
        fieldFilterMode = RecordLayerTableModel.MODE_ALL;
      }
      Invoke.later(this, "showRecordsTable", fieldFilterMode);
    }
  }

  public List<LayerRecord> splitRecord(final LayerRecord record, final CloseLocation mouseLocation) {

    final Geometry geometry = mouseLocation.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final int[] vertexId = mouseLocation.getVertexId();
      final Point point = mouseLocation.getPoint();
      final Point convertedPoint = (Point)point.copy(getGeometryFactory());
      final LineString line1;
      final LineString line2;

      final int vertexCount = line.getVertexCount();
      if (vertexId == null) {
        final int vertexIndex = mouseLocation.getSegmentId()[0];
        line1 = line.subLine(null, 0, vertexIndex + 1, convertedPoint);
        line2 = line.subLine(convertedPoint, vertexIndex + 1, vertexCount - vertexIndex - 1, null);
      } else {
        final int pointIndex = vertexId[0];
        if (pointIndex == 0) {
          return Collections.singletonList(record);
        } else if (vertexCount - pointIndex < 2) {
          return Collections.singletonList(record);
        } else {
          line1 = line.subLine(pointIndex + 1);
          line2 = line.subLine(null, pointIndex, vertexCount - pointIndex, null);
        }

      }
      if (line1 == null || line2 == null) {
        return Collections.singletonList(record);
      }

      return splitRecord(record, line, convertedPoint, line1, line2);
    }
    return Arrays.asList(record);
  }

  /** Perform the actual split. */
  protected List<LayerRecord> splitRecord(final LayerRecord record, final LineString line,
    final Point point, final LineString line1, final LineString line2) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record);

    final LayerRecord record1 = copyRecord(record);
    final LayerRecord record2 = copyRecord(record);
    record1.setGeometryValue(line1);
    record2.setGeometryValue(line2);

    property.setSplitAttributes(line, point, record1);
    property.setSplitAttributes(line, point, record2);
    deleteRecord(record);

    saveChanges(record, record1, record2);

    addSelectedRecords(record1, record2);
    return Arrays.asList(record1, record2);
  }

  public List<LayerRecord> splitRecord(final LayerRecord record, final Point point) {
    final LineString line = record.getGeometryValue();
    final List<LineString> lines = line.split(point);
    if (lines.size() == 2) {
      final LineString line1 = lines.get(0);
      final LineString line2 = lines.get(1);
      return splitRecord(record, line, point, line1, line2);
    } else {
      return Collections.singletonList(record);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (!super.isReadOnly()) {
      MapSerializerUtil.add(map, "canAddRecords", this.canAddRecords);
      MapSerializerUtil.add(map, "canDeleteRecords", this.canDeleteRecords);
      MapSerializerUtil.add(map, "canEditRecords", this.canEditRecords);
      MapSerializerUtil.add(map, "snapToAllLayers", this.snapToAllLayers);
    }
    MapSerializerUtil.add(map, "fieldNamesSetName", this.fieldNamesSetName, ALL);
    MapSerializerUtil.add(map, "fieldNamesSets", getFieldNamesSets());
    MapSerializerUtil.add(map, "useFieldTitles", this.useFieldTitles);
    return map;
  }

  public void unHighlightRecords(final Collection<? extends LayerRecord> records) {
    removeRecordsFromCache(this.cacheIdHighlighted, records);
    fireHighlighted();
  }

  public void unHighlightRecords(final LayerRecord... records) {
    unHighlightRecords(Arrays.asList(records));
  }

  public void unSelectRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = query(boundingBox);
      for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
        final LayerRecord record = iterator.next();
        if (!isVisible(record) || internalIsDeleted(record)) {
          iterator.remove();
        }
      }
      unSelectRecords(records);
      if (isHasSelectedRecords()) {
        showRecordsTable(RecordLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void unSelectRecords(final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      for (final LayerRecord record : records) {
        removeSelectedRecord(record);
      }
      unHighlightRecords(records);
    }
    fireSelected();
  }

  public void unSelectRecords(final LayerRecord... records) {
    unSelectRecords(Arrays.asList(records));
  }

  protected void updateRecordState(final LayerRecord record) {
    final RecordState state = record.getState();
    if (state == RecordState.Modified) {
      addModifiedRecord(record);
    } else if (state == RecordState.Persisted) {
      postSaveModifiedRecord(record);
    }
  }

  protected void updateSpatialIndex(final LayerRecord record, final Geometry oldGeometry) {
    if (oldGeometry != null) {
      final BoundingBox oldBoundingBox = oldGeometry.getBoundingBox();
      if (removeFromIndex(oldBoundingBox, record)) {
        addToIndex(record);
      }
    }

  }

  public void zoomToBoundingBox(BoundingBox boundingBox) {
    if (!BoundingBoxUtil.isEmpty(boundingBox)) {
      final Project project = getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
      boundingBox = boundingBox.expandPercent(0.1);
      project.setViewBoundingBox(boundingBox);
    }
  }

  public void zoomToGeometry(final Geometry geometry) {
    if (geometry != null) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      zoomToBoundingBox(boundingBox);
    }
  }

  public void zoomToHighlighted() {
    final BoundingBox boundingBox = getHighlightedBoundingBox();
    zoomToBoundingBox(boundingBox);
  }

  public void zoomToRecord(final Record record) {
    final Geometry geometry = record.getGeometryValue();
    zoomToGeometry(geometry);
  }

  public void zoomToRecords(final List<? extends LayerRecord> records) {
    BoundingBox boundingBox = new BoundingBoxDoubleGf();
    for (final Record record : records) {
      boundingBox = boundingBox.expandToInclude(record);
    }
    zoomToBoundingBox(boundingBox);
  }

  public void zoomToSelected() {
    final BoundingBox selectedBoundingBox = getSelectedBoundingBox();
    zoomToBoundingBox(selectedBoundingBox);
  }
}
