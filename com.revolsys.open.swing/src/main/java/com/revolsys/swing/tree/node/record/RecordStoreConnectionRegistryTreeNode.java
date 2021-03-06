package com.revolsys.swing.tree.node.record;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.io.datastore.RecordStoreConnection;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.map.form.RecordStoreConnectionDialog;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.file.FileTreeNode;
import com.revolsys.util.OS;

public class RecordStoreConnectionRegistryTreeNode extends LazyLoadTreeNode
implements PropertyChangeListener {

  private static final MenuFactory MENU = new MenuFactory(
    "Record Store Connections");

  static {
    if (OS.isMac()) {
      MENU.addMenuItem("default", TreeNodeRunnable.createAction(
        "Add Connection", "database_add", "addConnection"));
    }
  }

  public RecordStoreConnectionRegistryTreeNode(
    final RecordStoreConnectionRegistry registry) {
    super(registry);
    setType("Record Store Connections");
    setName(registry.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
  }

  public void addConnection() {
    final RecordStoreConnectionRegistry registry = getRegistry();
    final RecordStoreConnectionDialog dialog = new RecordStoreConnectionDialog(
      registry);
    dialog.setVisible(true);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final RecordStoreConnectionRegistry registry = getRegistry();
    final List<RecordStoreConnection> conections = registry.getConections();
    for (final RecordStoreConnection connection : conections) {
      final RecordStoreConnectionTreeNode child = new RecordStoreConnectionTreeNode(
        connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public void doPropertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == getRegistry()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("connections")) {
        refresh();
      }
    }
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  public Icon getOpenIcon() {
    return FileTreeNode.ICON_FOLDER_LINK_OPEN;
  }

  protected RecordStoreConnectionRegistry getRegistry() {
    final RecordStoreConnectionRegistry registry = getUserData();
    return registry;
  }
}
