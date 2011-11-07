/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.web.config.Menu;
import com.revolsys.ui.web.config.MenuItem;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.xml.io.XmlWriter;

public class MenuView extends ObjectView {
  private static final Logger log = Logger.getLogger(MenuView.class);
  private String cssClass = "menu";

  private int numLevels = 1;

  private boolean showRoot;

  public void processProperty(final String name, final Object value) {
    String stringValue = (String)value;
    if (name.equals("cssClass")) {
      cssClass = value.toString();
    } else if (name.equals("numLevels")) {
      numLevels = Integer.parseInt(stringValue);
    } else if (name.equals("menuName")) {
      WebUiContext context = WebUiContext.get();
      setObject(context.getMenu(stringValue));
      if (getObject() == null) {
        throw new IllegalArgumentException("Menu " + value + " does not exist");
      }
    } else if (name.equals("showRoot")) {
      showRoot = Boolean.valueOf(stringValue).booleanValue();
    }
  }

  public void serializeElement(final XmlWriter out) {
    Menu menu = (Menu)getObject();
    if (menu != null) {
      List menuItems = new ArrayList();
      for (Iterator items = menu.getItems().iterator(); items.hasNext();) {
        MenuItem menuItem = (MenuItem)items.next();
        if (menuItem.isVisible()) {
          menuItems.add(menuItem);
        }
      }
      if (showRoot || !menuItems.isEmpty()) {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, cssClass);

        if (showRoot) {
          out.startTag(HtmlUtil.DIV);
          out.attribute(HtmlUtil.ATTR_CLASS, "title");
          menuItemLink(out, menu);
          out.endTag(HtmlUtil.DIV);

        }

        menu(out, menuItems, 1);

        out.endTag(HtmlUtil.DIV);
      }
    }
  }

  private void menu(final XmlWriter out, final Collection items, final int level)
    {
    // Collection items = menu.getItems();
    if (items.size() > 0) {
      out.startTag(HtmlUtil.UL);
      for (Iterator menuItemIter = items.iterator(); menuItemIter.hasNext();) {
        MenuItem menuItem = (MenuItem)menuItemIter.next();
        if (menuItem.isVisible()) {
          out.startTag(HtmlUtil.LI);

          String cssClass = menuItem.getProperty("cssClass");
          if (cssClass != null) {
            out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
          }
          menuItemLink(out, menuItem);
          if (level < numLevels && menuItem instanceof Menu) {
            menu(out, ((Menu)menuItem).getItems(), level + 1);
          }
          out.endTag(HtmlUtil.LI);
        }
      }
      out.endTag(HtmlUtil.UL);
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "end");
      out.entityRef("nbsp");
      out.endTag(HtmlUtil.DIV);
    }
  }

  private void menuItemLink(final XmlWriter out, final MenuItem menuItem)
    {

    String uri = menuItem.getUri();
    if (uri != null) {
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, uri);
      out.attribute(HtmlUtil.ATTR_TITLE, menuItem.getTitle());
      out.text(menuItem.getTitle());
      out.endTag(HtmlUtil.A);
    } else {
      out.text(menuItem.getTitle());
    }
  }
}