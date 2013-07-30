package com.revolsys.swing.map.layer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.CollectionUtil;

public class Project extends LayerGroup {

  private static WeakReference<Project> project = new WeakReference<Project>(
    null);

  public static Project get() {
    return Project.project.get();
  }

  public static void set(final Project project) {
    Project.project = new WeakReference<Project>(project);
  }

  private Resource resource;

  private LayerGroup baseMapLayers = new LayerGroup("Base Maps");

  private BoundingBox viewBoundingBox = new BoundingBox();

  private DataObjectStoreConnectionRegistry dataStores;

  private FolderConnectionRegistry folderConnections;

  public Project() {
    this("Project");
    setGeometryFactory(GeometryFactory.WORLD_MERCATOR);
  }

  public Project(final String name) {
    super(name);
    baseMapLayers.setLayerGroup(this);
    set(this);
  }

  private void addChangedLayers(final LayerGroup group,
    final List<Layer> layersWithChanges) {
    for (final Layer layer : group) {
      if (layer instanceof LayerGroup) {
        final LayerGroup subGroup = (LayerGroup)layer;
        addChangedLayers(subGroup, layersWithChanges);
      } else if (layer.isHasChanges()) {
        layersWithChanges.add(layer);
      }
    }

  }

  @Override
  public void delete() {
    super.delete();
    this.baseMapLayers = null;
    this.viewBoundingBox = null;
  }

  public DataObjectStoreConnectionRegistry getDataStores() {
    return dataStores;
  }

  public FolderConnectionRegistry getFolderConnections() {
    return folderConnections;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    if (name.equals("Base Maps")) {
      return (V)baseMapLayers;
    } else {
      return (V)super.getLayer(name);
    }
  }

  @Override
  public Project getProject() {
    return this;
  }

  public BoundingBox getViewBoundingBox() {
    return viewBoundingBox;
  }

  protected void readBaseMapsLayers(final Resource resource) {
    final Resource baseMapsResource = SpringUtil.getResource(resource,
      "Base Maps");
    final Resource layerGroupResource = SpringUtil.getResource(
      baseMapsResource, "rgLayerGroup.rgobject");
    if (layerGroupResource.exists()) {
      final Resource oldResource = SpringUtil.setBaseResource(baseMapsResource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        baseMapLayers.loadLayers(properties);
        if (!baseMapLayers.isEmpty()) {
          baseMapLayers.get(0).setVisible(true);
        }
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  protected void readLayers(final Resource resource) {
    final Resource layerGroupResource = SpringUtil.getResource(resource,
      "rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error(
        "File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        loadLayers(properties);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to read: " + layerGroupResource, e);
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  public void readProject(final Resource resource) {
    this.resource = resource;
    final Resource dataStoresDirectory = SpringUtil.getResource(resource,
      "Data Stores");
    final DataObjectStoreConnectionManager dataObjectStoreConnectionManager = DataObjectStoreConnectionManager.get();
    final DataObjectStoreConnectionRegistry dataStores = dataObjectStoreConnectionManager.addConnectionRegistry(
      "Project", dataStoresDirectory);
    setDataStores(dataStores);

    final Resource folderConnectionsDirectory = SpringUtil.getResource(
      resource, "Folder Connections");
    this.folderConnections = FolderConnectionManager.get()
      .addConnectionRegistry("Project", folderConnectionsDirectory);

    final Resource layersDir = SpringUtil.getResource(resource, "Layers");
    readLayers(layersDir);

    readBaseMapsLayers(resource);
  }

  public boolean saveChangesWithPrompt() {
    final List<Layer> layersWithChanges = new ArrayList<Layer>();
    addChangedLayers(this, layersWithChanges);

    if (layersWithChanges.isEmpty()) {
      return true;
    } else {
      final MapPanel mapPanel = MapPanel.get(this);
      final JLabel message = new JLabel(
        "<html><body><p><b>The following layers have un-saved changes.</b></p>"
          + "<p><b>Do you want to save the changes before continuing?</b></p><ul><li>"
          + CollectionUtil.toString("</li>\n<li>", layersWithChanges)
          + "</li></ul></body></html>");

      final int option = JOptionPane.showConfirmDialog(mapPanel, message,
        "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.CANCEL_OPTION) {
        return false;
      } else if (option == JOptionPane.NO_OPTION) {
        return true;
      } else {
        for (final Iterator<Layer> iterator = layersWithChanges.iterator(); iterator.hasNext();) {
          final Layer layer = iterator.next();
          if (layer.saveChanges()) {
            iterator.remove();
          }
        }
        if (layersWithChanges.isEmpty()) {
          return true;
        } else {
          final JLabel message2 = new JLabel(
            "<html><body><p><b>The following layers could not be saved.</b></p>"
              + "<p><b>Do you want to ignore these changes and continue?</b></p><ul><li>"
              + CollectionUtil.toString("</li>\n<li>", layersWithChanges)
              + "</li></ul></body></html>");

          final int option2 = JOptionPane.showConfirmDialog(mapPanel, message2,
            "Ignore Changes", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
          if (option2 == JOptionPane.CANCEL_OPTION) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  public void saveProject() {
    if (resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)resource;
      final File directory = fileResource.getFile();
      if (directory.isDirectory()) {
        final File layersDirectory = new File(directory, "Layers");
        FileUtil.deleteDirectory(layersDirectory, false);
        layersDirectory.mkdir();
        saveLayerGroup(layersDirectory);
      }
    }
  }

  public void setDataStores(final DataObjectStoreConnectionRegistry dataStores) {
    this.dataStores = dataStores;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
  }

  public void setViewBoundingBox(BoundingBox viewBoundingBox) {

    if (!viewBoundingBox.isNull()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox oldValue = this.viewBoundingBox;
      if (viewBoundingBox.getWidth() == 0) {
        if (geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
          viewBoundingBox = viewBoundingBox.expand(0.000009 * 20, 0);
        } else {
          viewBoundingBox = viewBoundingBox.expand(20, 0);
        }
      }
      if (viewBoundingBox.getHeight() == 0) {
        if (geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
          viewBoundingBox = viewBoundingBox.expand(0, 0.000009 * 20);
        } else {
          viewBoundingBox = viewBoundingBox.expand(0, 20);
        }
      }
      this.viewBoundingBox = viewBoundingBox;
      firePropertyChange("viewBoundingBox", oldValue, viewBoundingBox);
    }
  }

}
