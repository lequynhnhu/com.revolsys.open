package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JComponent;

import org.slf4j.LoggerFactory;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.listener.BeanPropertyListener;
import com.revolsys.swing.map.layer.menu.SetLayerScaleMenu;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractLayer extends AbstractObjectWithProperties
  implements Layer, PropertyChangeListener {
  private static final AtomicLong ID_GEN = new AtomicLong();

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractLayer.class);
    menu.addMenuItemTitleIcon("zoom", "Zoom to Layer", "magnifier",
      LayerUtil.class, "zoomToLayer");
    menu.addComponentFactory("scale", new SetLayerScaleMenu(true));
    menu.addComponentFactory("scale", new SetLayerScaleMenu(false));

    menu.addMenuItemTitleIcon("layer", "Delete Layer", "delete",
      LayerUtil.class, "deleteLayer");
    menu.addMenuItemTitleIcon("layer", "Layer Properties", "information",
      LayerUtil.class, "showProperties");
  }

  protected PropertyChangeListener beanPropertyListener = new BeanPropertyListener(
    this);

  private boolean editable = false;

  private LayerGroup layerGroup;

  private String name;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private GeometryFactory geometryFactory;

  private boolean readOnly;

  private boolean selectable = false;

  private boolean selectSupported = true;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private boolean visible = true;

  private boolean queryable;

  private boolean querySupported;

  private final long id = ID_GEN.incrementAndGet();

  private LayerRenderer<?> renderer;

  public AbstractLayer() {
  }

  public AbstractLayer(final String name) {
    this.name = name;
  }

  public AbstractLayer(final String name,
    final Map<String, ? extends Object> properties) {
    this.name = name;
    setProperties(properties);
  }

  public ValueField addPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final ValueField panel = new ValueField(this);
    tabPanel.addTab("General", panel);

    final JComponent nameField = SwingUtil.addField(panel, this, "name");
    if (nameField instanceof Field) {
      nameField.addPropertyChangeListener("name", beanPropertyListener);
    }

    GroupLayoutUtil.makeColumns(panel, 2);
    return panel;
  }

  @Override
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public int compareTo(final Layer layer) {
    return getName().compareTo(layer.getName());
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel tabPanel = new TabbedValuePanel("Layer " + this
      + " Properties", this);
    addPropertiesTabGeneral(tabPanel);
    return tabPanel;
  }

  @Override
  public void delete() {
    firePropertyChange("deleted", false, true);
    if (layerGroup != null) {
      layerGroup.remove(this);
    }
    this.geometryFactory = null;
    this.layerGroup = null;
    this.propertyChangeSupport = null;
    this.renderer = null;
  }

  protected boolean doSaveChanges() {
    return true;
  }

  protected void fireIndexedPropertyChange(final String propertyName,
    final int index, final Object oldValue, final Object newValue) {
    if (propertyChangeSupport != null) {
      propertyChangeSupport.fireIndexedPropertyChange(propertyName, index,
        oldValue, newValue);
    }
  }

  protected void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return new BoundingBox();
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (visible || !visibleLayersOnly) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return new BoundingBox(geometryFactory);
    } else {
      return getBoundingBox();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (geometryFactory == null && layerGroup != null) {
      return layerGroup.getGeometryFactory();
    } else {
      return geometryFactory;
    }
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public LayerGroup getLayerGroup() {
    return layerGroup;
  }

  @Override
  public long getMaximumScale() {
    return maximumScale;
  }

  @Override
  public long getMinimumScale() {
    return minimumScale;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getPath() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup == null) {
      return "/";
    } else {
      final String path = layerGroup.getPath();
      if ("/".equals(path)) {
        return "/" + getName();
      } else {
        return path + "/" + getName();
      }
    }
  }

  @Override
  public Project getProject() {
    if (layerGroup == null) {
      return null;
    } else {
      return layerGroup.getProject();
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <L extends LayerRenderer<? extends Layer>> L getRenderer() {
    return (L)renderer;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBox(geometryFactory);
  }

  @Override
  public boolean isEditable() {
    return editable;
  }

  @Override
  public boolean isEditable(final double scale) {
    return isVisible(scale) && isEditable();
  }

  @Override
  public boolean isHasChanges() {
    return false;
  }

  @Override
  public boolean isQueryable() {
    return querySupported && queryable;
  }

  @Override
  public boolean isQuerySupported() {
    return querySupported;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public boolean isSelectable() {
    return isSelectSupported() && isVisible() && selectable;
  }

  @Override
  public boolean isSelectable(final double scale) {
    return isVisible(scale) && isSelectable();
  }

  @Override
  public boolean isSelectSupported() {
    return selectSupported;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public boolean isVisible(final double scale) {
    if (isVisible()) {
      final long longScale = (long)scale;
      if (getMinimumScale() >= longScale && longScale >= getMaximumScale()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  @Override
  public void refresh() {
  }

  @Override
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    if (propertyChangeSupport != null) {
      propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  @Override
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    if (propertyChangeSupport != null) {
      propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
  }

  @Override
  public boolean saveChanges() {
    if (isHasChanges()) {
      return doSaveChanges();
    }
    return true;
  }

  @Override
  public void setEditable(final boolean editable) {
    final boolean old = isEditable();
    this.editable = editable;
    firePropertyChange("editable", old, isEditable());
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != this.geometryFactory) {
      final GeometryFactory old = this.geometryFactory;
      this.geometryFactory = geometryFactory;
      firePropertyChange("geometryFactory", old, this.geometryFactory);
    }
  }

  @Override
  public void setLayerGroup(final LayerGroup layerGroup) {
    if (this.layerGroup != layerGroup) {
      final LayerGroup old = this.layerGroup;
      if (old != null) {
        removePropertyChangeListener(old);
      }
      this.layerGroup = layerGroup;
      addPropertyChangeListener(layerGroup);
      firePropertyChange("layerGroup", old, layerGroup);
    }
  }

  @Override
  public void setMaximumScale(final long maxScale) {
    this.maximumScale = maxScale;
  }

  @Override
  public void setMinimumScale(final long minScale) {
    this.minimumScale = minScale;
  }

  @Override
  public void setName(final String name) {
    if (!EqualsRegistry.equal(name, this.name)) {
      propertyChangeSupport.firePropertyChange("name", this.name, name);
      this.name = name;
    }
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties == null || !this.getProperties().equals(properties)) {
      propertyChangeSupport.firePropertyChange("properties",
        this.getProperties(), properties);
      super.setProperties(properties);
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    // TODO see if we can get the JavaBeanUtil set property to work with
    // conversions
    if (name.equals("type")) {
    } else if (name.equals("minimumScale")) {
      setMinimumScale(((Number)value).longValue());
    } else if (name.equals("maximumScale")) {
      setMaximumScale(((Number)value).longValue());
    } else {
      final Object oldValue = getProperty(name);
      if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
        final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(
          this, "property", oldValue, value, name);
        propertyChangeSupport.firePropertyChange(event);

        super.setProperty(name, value);
        try {
          JavaBeanUtil.setProperty(this, name, value);
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to set property:" + name, e);
        }
      }
    }
  }

  @Override
  public void setQueryable(final boolean queryable) {
    this.queryable = queryable;
  }

  protected void setQuerySupported(final boolean querySupported) {
    this.querySupported = querySupported;
  }

  @Override
  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRenderer(final LayerRenderer<? extends Layer> renderer) {
    if (this.renderer != null) {
      this.renderer.getPropertyChangeSupport().removePropertyChangeListener(
        this);
    }
    this.renderer = renderer;
    if (renderer != null) {
      renderer.getPropertyChangeSupport().addPropertyChangeListener(this);
    }
  }

  @Override
  public void setSelectable(final boolean selectable) {
    final boolean oldValue = this.selectable;
    this.selectable = selectable;
    propertyChangeSupport.firePropertyChange("selectable", oldValue, selectable);
  }

  public void setSelectSupported(final boolean selectSupported) {
    this.selectSupported = selectSupported;
  }

  @Override
  public void setVisible(final boolean visible) {
    final boolean oldVisible = this.visible;
    this.visible = visible;
    propertyChangeSupport.firePropertyChange("visible", oldVisible, visible);
  }

  @Override
  public String toString() {
    return getName();
  }
}
