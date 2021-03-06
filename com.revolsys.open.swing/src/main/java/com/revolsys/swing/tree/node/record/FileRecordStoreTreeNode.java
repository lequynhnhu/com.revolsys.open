package com.revolsys.swing.tree.node.record;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.revolsys.data.io.RecordStoreConnectionMapProxy;
import com.revolsys.data.io.RecordStoreProxy;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.file.FileTreeNode;
import com.revolsys.util.Property;

public class FileRecordStoreTreeNode extends FileTreeNode implements
RecordStoreProxy, RecordStoreConnectionMapProxy {
  private static final MenuFactory MENU = new MenuFactory("File Record Store");

  static {
    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh",
      "arrow_refresh", NODE_EXISTS, "refresh");
    MENU.addMenuItem("default", refresh);

    MENU.addMenuItem("default", TreeNodeRunnable.createAction(
      "Add Record Store Connection", "link_add", NODE_EXISTS,
        "addRecordStoreConnection"));
  }

  public FileRecordStoreTreeNode(final File file) {
    super(file);
    setType("Record Store");
    setName(FileUtil.getFileName(file));
    setIcon(FileTreeNode.ICON_FILE_DATABASE);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public void addRecordStoreConnection() {
    final File file = getUserData();
    final String fileName = FileUtil.getBaseName(file);

    final ValueField panel = new ValueField();
    panel.setTitle("Add Record Store Connection");
    SwingUtil.setTitledBorder(panel, "Record Store Connection");

    SwingUtil.addLabel(panel, "File");
    final JLabel fileLabel = new JLabel(file.getAbsolutePath());
    panel.add(fileLabel);

    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);
    nameField.setText(fileName);

    SwingUtil.addLabel(panel, "Record Store Connections");

    final List<RecordStoreConnectionRegistry> registries = new ArrayList<>();
    for (final RecordStoreConnectionRegistry registry : RecordStoreConnectionManager.get()
        .getVisibleConnectionRegistries()) {
      if (!registry.isReadOnly()) {
        registries.add(registry);
      }
    }
    final JComboBox registryField = new JComboBox(
      new Vector<RecordStoreConnectionRegistry>(registries));

    panel.add(registryField);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final RecordStoreConnectionRegistry registry = (RecordStoreConnectionRegistry)registryField.getSelectedItem();
      String connectionName = nameField.getText();
      if (!Property.hasValue(connectionName)) {
        connectionName = fileName;
      }
      final String baseConnectionName = connectionName;
      int i = 0;
      while (registry.getConnection(connectionName) != null) {
        connectionName = baseConnectionName + i;
        i++;
      }
      final Map<String, Object> connection = getRecordStoreConnectionMap();
      final Map<String, Object> config = new HashMap<String, Object>();
      config.put("name", connectionName);
      config.put("connection", connection);
      registry.createConnection(config);
    }
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final RecordStore recordStore = getRecordStore();
    return RecordStoreConnectionTreeNode.getChildren(
      getRecordStoreConnectionMap(), recordStore);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getRecordStore() {
    final File file = getUserData();
    return (V)RecordStoreConnectionManager.getRecordStore(file);
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    final BaseTreeNode parent = getParent();
    final File file = getUserData();
    final URL url = FileTreeNode.getUrl(parent, file);

    return Collections.<String, Object> singletonMap("url", url.toString());
  }

  @Override
  public boolean isAllowsChildren() {
    return true;
  }

}
