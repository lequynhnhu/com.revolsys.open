package com.revolsys.swing.map.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.revolsys.data.io.RecordReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.raster.GeoReferencedImageFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.node.layer.LayerGroupTreeNode;
import com.revolsys.util.CollectionUtil;

public class AddFileLayerAction extends AbstractAction {

  public static FileNameExtensionFilter createFileFilter(
    final String description, final Collection<String> fileExtensions) {
    final String[] array = fileExtensions.toArray(new String[0]);
    return new FileNameExtensionFilter(description, array);
  }

  public static void getFileFilters(final List<FileFilter> fileFilters,
    final Set<String> allExtensions,
    final Class<? extends IoFactory> factoryClass) {
    final Map<String, FileFilter> filtersByName = new TreeMap<>();
    final Set<IoFactory> factories = IoFactoryRegistry.getInstance()
        .getFactories(factoryClass);
    for (final IoFactory factory : factories) {
      final List<String> fileExtensions = factory.getFileExtensions();
      String description = factory.getName();
      description += " (" + CollectionUtil.toString(fileExtensions) + ")";
      final FileNameExtensionFilter filter = createFileFilter(description,
        fileExtensions);
      filtersByName.put(description, filter);
      allExtensions.addAll(fileExtensions);
    }
    fileFilters.addAll(filtersByName.values());
  }

  private static final long serialVersionUID = 1L;

  public AddFileLayerAction() {
    putValue(NAME, "Open File Layer");
    putValue(SMALL_ICON, Icons.getIcon("page_add"));
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    Window window;
    if (source instanceof Component) {
      final Component component = (Component)source;
      window = SwingUtilities.getWindowAncestor(component);
    } else {
      window = null;
    }

    final JFileChooser fileChooser = SwingUtil.createFileChooser(getClass(),
        "currentDirectory");
    fileChooser.setMultiSelectionEnabled(true);

    final List<FileFilter> imageFileFilters = new ArrayList<FileFilter>();
    final Set<String> allImageExtensions = new TreeSet<String>();
    getFileFilters(imageFileFilters, allImageExtensions,
      GeoReferencedImageFactory.class);

    final List<FileFilter> recordFileFilters = new ArrayList<FileFilter>();
    final Set<String> allRecordExtensions = new TreeSet<String>();
    getFileFilters(recordFileFilters, allRecordExtensions,
      RecordReaderFactory.class);

    final Set<String> allExtensions = new TreeSet<String>();
    allExtensions.addAll(allRecordExtensions);
    allExtensions.addAll(allImageExtensions);
    final FileNameExtensionFilter allFilter = createFileFilter(
      "All Supported Files", allExtensions);
    fileChooser.addChoosableFileFilter(allFilter);

    fileChooser.addChoosableFileFilter(createFileFilter(
      "All Vector/Record Files", allRecordExtensions));

    fileChooser.addChoosableFileFilter(createFileFilter("All Image Files",
      allImageExtensions));

    for (final FileFilter fileFilter : recordFileFilters) {
      fileChooser.addChoosableFileFilter(fileFilter);
    }

    for (final FileFilter fileFilter : imageFileFilters) {
      fileChooser.addChoosableFileFilter(fileFilter);
    }

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(allFilter);

    final int status = fileChooser.showDialog(window, "Open Files");
    if (status == JFileChooser.APPROVE_OPTION) {
      final Object menuSource = MenuFactory.getMenuSource();
      final LayerGroup layerGroup;
      if (menuSource instanceof LayerGroupTreeNode) {
        final LayerGroupTreeNode node = (LayerGroupTreeNode)menuSource;
        layerGroup = node.getGroup();
      } else if (menuSource instanceof LayerGroup) {
        layerGroup = (LayerGroup)menuSource;
      } else {
        layerGroup = Project.get();
      }
      for (final File file : fileChooser.getSelectedFiles()) {
        Invoke.background("Open file: " + FileUtil.getCanonicalPath(file),
          layerGroup, "openFile", file);
      }
    }
    SwingUtil.saveFileChooserDirectory(getClass(), "currentDirectory",
      fileChooser);
  }
}
