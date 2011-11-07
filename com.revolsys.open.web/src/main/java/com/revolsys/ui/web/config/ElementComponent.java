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
package com.revolsys.ui.web.config;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.view.Element;

public class ElementComponent extends Component {
  private static final Logger log = Logger.getLogger(ElementComponent.class);

  private String attribute;

  public ElementComponent(final String area, final String name, final String attribute) {
    super(area, name);
    setAttribute(attribute);
  }

  public ElementComponent(final ElementComponent component) {
    super(component);
    setAttribute(component.attribute);
  }

  public Object clone() {
    return new ElementComponent(this);
  }

  public boolean equals(final Object o) {
    if (o instanceof ElementComponent) {
      ElementComponent c = (ElementComponent)o;
      if (equalsWithNull(c.attribute, attribute) && super.equals(o)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  public int hashCode() {
    return super.hashCode() + (attribute.hashCode() << 2);
  }

  public void includeComponent(final PageContext context)
    throws ServletException, IOException {
    Object object = context.findAttribute(attribute);
    if (object instanceof Element) {
      Element element = (Element)object;
      Writer out = context.getOut();
      element.serialize(out);

    }
  }

  private void setAttribute(final String attribute) {
    this.attribute = attribute;
  }
}