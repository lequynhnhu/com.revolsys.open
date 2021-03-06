package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentGroup;
import com.revolsys.util.Property;

public class ToolBar extends JToolBar {
  private static final long serialVersionUID = 1L;

  private final ComponentGroup groups = new ComponentGroup();

  public ToolBar() {
    this(HORIZONTAL);
  }

  public ToolBar(final int orientation) {
    super(orientation);
    setOpaque(true);
    setRollover(false);
    setFloatable(false);
  }

  public JButton addButton(final AbstractAction action) {
    final JButton button = action.createButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, button);
    return button;
  }

  public JButton addButton(final String groupName, final AbstractAction action) {
    final JButton button = action.createButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, groupName, button);
    return button;
  }

  public JButton addButton(final String groupName, final int index,
    final AbstractAction action) {
    final JButton button = action.createButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, groupName, index, button);
    return button;
  }

  public JButton addButton(final String groupName, final int index,
    final String name, final String title, final Icon icon,
    final Object object, final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    return addButton(groupName, index, action);
  }

  public JButton addButton(final String groupName, final String title,
    final Object object, final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(title, object,
      methodName, parameters);
    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, String title,
    final String iconName, final EnableCheck enableCheck, final Object object,
    final String methodName, final Object... parameters) {
    String name = null;
    Icon icon = null;
    if (Property.hasValue(iconName)) {
      icon = Icons.getIcon(iconName);
    } else {
      name = title;
      title = null;
    }

    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      enableCheck, object, methodName, parameters);

    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    return addButton(groupName, action);
  }

  public JButton addButtonTitleIcon(final String groupName, final int index,
    final String title, final String iconName, final Object object,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = Icons.getIcon(iconName);
    return addButton(groupName, index, iconName, title, icon, object,
      methodName, parameters);
  }

  public JButton addButtonTitleIcon(final String groupName, final String title,
    final String iconName, final Object object, final String methodName,
    final Object... parameters) {
    final ImageIcon icon = Icons.getIcon(iconName);
    return addButton(groupName, iconName, title, icon, object, methodName,
      parameters);
  }

  public void addComponent(final Component component) {
    this.groups.addComponent(this, component);
  }

  public void addComponent(final String groupName, final Component component) {
    this.groups.addComponent(this, groupName, component);
  }

  public void addGroup(final String groupName) {
    this.groups.addGroup(groupName);
  }

  public JToggleButton addToggleButton(final String groupName, final int index,
    final String title, final String iconName, final EnableCheck enableCheck,
    final Object object, final String methodName, final Object... parameters) {
    final ImageIcon icon = Icons.getIcon(iconName);
    return addToggleButton(groupName, index, iconName, title, icon,
      enableCheck, object, methodName, parameters);
  }

  public JToggleButton addToggleButton(final String groupName, final int index,
    final String name, final String title, final Icon icon,
    final EnableCheck enableCheck, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);
    action.setEnableCheck(enableCheck);

    final JToggleButton button = action.createToggleButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, groupName, index, button);
    final ButtonGroup buttonGroup = getButtonGroup(groupName);
    buttonGroup.add(button);
    return button;
  }

  public JToggleButton addToggleButtonTitleIcon(final String groupName,
    final int index, final String title, final String iconName,
    final Object object, final String methodName, final Object... parameters) {
    final ImageIcon icon = Icons.getIcon(iconName);
    return addToggleButton(groupName, index, iconName, title, icon, null,
      object, methodName, parameters);
  }

  public void clear() {
    super.removeAll();
    this.groups.clear();
  }

  public ButtonGroup getButtonGroup(final String groupName) {
    return this.groups.getButtonGroup(groupName);
  }

  public List<Component> getGroup(final String groupName) {
    return this.groups.getGroup(groupName);
  }

  public void removeComponent(final String groupName, final int index) {
    this.groups.removeComponent(this, groupName, index);
  }

  public void removeGroup(final String groupName) {
    this.groups.removeGroup(this, groupName);

  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    this.groups.setGroupEnabled(groupName, enabled);
  }

}
