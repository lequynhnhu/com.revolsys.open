package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.Function;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.Value;
import com.revolsys.swing.map.list.DataObjectListCellRenderer;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;

public class DataStoreSearchTextField extends JXSearchField implements
  DocumentListener, KeyListener, MouseListener, FocusListener,
  ListDataListener, ItemSelectable, Field {
  private static final long serialVersionUID = 1L;

  private final String displayAttributeName;

  private final JXList list;

  private final JPopupMenu menu = new JPopupMenu();

  private final DataStoreQueryListModel listModel;

  public DataObject selectedItem;

  private String errorMessage;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public DataStoreSearchTextField(final DataObjectMetaData metaData,
    final String displayAttributeName) {
    this(metaData.getDataObjectStore(), displayAttributeName, new Query(
      metaData, Conditions.equal(Function.upper(displayAttributeName),
        new Value(null))), new Query(metaData, Conditions.likeUpper(
      displayAttributeName, "")));
  }

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String displayAttributeName, final List<Query> queries) {
    this.displayAttributeName = displayAttributeName;

    final Document document = getDocument();
    document.addDocumentListener(this);
    addFocusListener(this);
    addKeyListener(this);
    addMouseListener(this);

    this.listModel = new DataStoreQueryListModel(dataStore,
      displayAttributeName, queries);
    this.list = new JXList(this.listModel);
    this.list.setCellRenderer(new DataObjectListCellRenderer(
      displayAttributeName));
    this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.list.setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));
    this.list.addMouseListener(this);
    this.listModel.addListDataListener(this);

    this.menu.add(this.list);
    this.menu.setFocusable(false);
    this.menu.setBorderPainted(true);
    this.menu.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(Color.DARK_GRAY),
      BorderFactory.createEmptyBorder(1, 2, 1, 2)));

    setEditable(true);
    setSearchMode(SearchMode.REGULAR);
    PopupMenu.getPopupMenuFactory(this);
    this.undoManager.addKeyMap(this);
  }

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String displayAttributeName, final Query... queries) {
    this(dataStore, displayAttributeName, Arrays.asList(queries));

  }

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String typeName, final String displayAttributeName) {
    this(dataStore, displayAttributeName, new Query(typeName, Conditions.equal(
      Function.upper(displayAttributeName), new Value(null))), new Query(
      typeName, Conditions.likeUpper(displayAttributeName, "")));
  }

  @Override
  public void addItemListener(final ItemListener l) {
    this.listenerList.add(ItemListener.class, l);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    search();
  }

  @Override
  public void contentsChanged(final ListDataEvent e) {
    intervalAdded(e);
  }

  protected void fireItemStateChanged(final ItemEvent e) {
    final Object[] listeners = this.listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener)listeners[i + 1]).itemStateChanged(e);
      }
    }
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    showMenu();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    this.menu.setVisible(false);
  }

  @Override
  public String getFieldName() {
    return this.displayAttributeName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.errorMessage;
  }

  @Override
  public <T> T getFieldValue() {
    if (this.selectedItem == null) {
      return null;
    } else {
      return this.selectedItem.getIdValue();
    }
  }

  public ItemListener[] getItemListeners() {
    return this.listenerList.getListeners(ItemListener.class);
  }

  public DataObject getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  public Object[] getSelectedObjects() {
    final DataObject selectedItem = getSelectedItem();
    if (selectedItem == null) {
      return null;
    } else {
      return new Object[] {
        selectedItem
      };
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    search();
  }

  @Override
  public void intervalAdded(final ListDataEvent e) {
    this.list.getSelectionModel().clearSelection();
  }

  @Override
  public void intervalRemoved(final ListDataEvent e) {
    intervalAdded(e);
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  public boolean isTextSameAsSelected() {
    if (this.selectedItem == null) {
      return false;
    } else {
      final String text = getText();
      if (StringUtils.hasText(text)) {
        final String value = this.selectedItem.getString(this.displayAttributeName);
        return text.equalsIgnoreCase(value);
      } else {
        return false;
      }
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    int increment = 1;
    final int size = this.listModel.getSize();
    int selectedIndex = this.list.getSelectedIndex();
    switch (keyCode) {
      case KeyEvent.VK_UP:
        increment = -1;
      case KeyEvent.VK_DOWN:
        if (selectedIndex >= size) {
          selectedIndex = -1;
        }
        selectedIndex += increment;
        if (selectedIndex < 0) {
          selectedIndex = 0;
        } else if (selectedIndex >= size) {
          selectedIndex = size - 1;
        }
        this.list.setSelectedIndex(selectedIndex);
        e.consume();
      break;
      case KeyEvent.VK_ENTER:
        if (size > 0) {
          if (selectedIndex >= 0 && selectedIndex < size) {
            final DataObject selectedItem = this.listModel.getElementAt(selectedIndex);
            final String text = selectedItem.getString(this.displayAttributeName);
            if (!text.equals(this.getText())) {
              this.selectedItem = selectedItem;
              setText(text);
              e.consume();
              return;
            }
          }
        }
        if (this.listModel.getSelectedItem() != null) {
          final JButton findButton = getFindButton();
          findButton.doClick();
          setText("");
        }
      break;
      default:
      break;
    }
    showMenu();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (e.getSource() == this.list) {
      final Point point = e.getPoint();
      final int index = this.list.locationToIndex(point);
      if (index > -1) {
        this.list.setSelectedIndex(index);
        final DataObject value = this.listModel.getElementAt(index);
        if (value != null) {
          final String label = value.getString(this.displayAttributeName);
          setText(label);
        }
        requestFocus();
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (e.getSource() == this) {
      showMenu();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @Override
  public void removeItemListener(final ItemListener l) {
    this.listenerList.remove(ItemListener.class, l);
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    search();
  }

  protected void search() {
    if (this.selectedItem != null) {
      final DataObject oldValue = this.selectedItem;
      this.selectedItem = null;
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
        oldValue, ItemEvent.DESELECTED));
    }
    final String text = getText();
    this.listModel.setSearchText(text);
    showMenu();

    this.selectedItem = this.listModel.getSelectedItem();
    if (this.selectedItem != null) {
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
        this.selectedItem, ItemEvent.SELECTED));
    }
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color color) {
    setForeground(color);
    setSelectedTextColor(color);
    setBackground(ColorUtil.setAlpha(color, 50));
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    // TODO Auto-generated method stub

  }

  public void setMaxResults(final int maxResults) {
    this.listModel.setMaxResults(maxResults);
  }

  @Override
  public void setText(final String text) {
    super.setText(text);
    search();
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(this.errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  private void showMenu() {
    final List<DataObject> objects = this.listModel.getObjects();
    if (objects.isEmpty()) {
      this.menu.setVisible(false);
    } else {
      this.menu.setVisible(true);
      this.menu.show(this, 0, this.getHeight());
      this.menu.pack();
      if (objects.size() == 1) {
        this.list.setSelectedIndex(0);
      } else {
        int i = 0;
        for (final DataObject object : objects) {
          final String value = object.getString(this.displayAttributeName);
          final String text = getText();
          if (value.equals(text)) {
            this.list.setSelectedIndex(i);
          }
          i++;
        }
      }
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

}
