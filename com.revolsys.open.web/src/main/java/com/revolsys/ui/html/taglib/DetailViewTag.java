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
package com.revolsys.ui.html.taglib;

import java.io.Writer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.revolsys.ui.html.serializer.LabelValueListSerializer;
import com.revolsys.ui.html.view.DetailView;
import com.revolsys.util.JavaBeanUtil;

public class DetailViewTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 5852237847322159567L;

  private String name;

  private String modelClass;

  private LabelValueListSerializer model;

  public int doStartTag() throws JspException {
    if (name != null) {
      try {
        Object object = pageContext.findAttribute(name);
        if (object != null) {
          Writer out = pageContext.getOut();
          JavaBeanUtil.setProperty(model, "object", object);
          DetailView view = new DetailView(model);
          view.serialize(out);
        }
      } catch (Throwable t) {
        throw new JspException(t);
      }
    }
    return SKIP_BODY;
  }

  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getModelClass() {
    return modelClass;
  }

  public void setModelClass(final String modelClass) {
    this.modelClass = modelClass;
    try {
      Class klass = Class.forName(modelClass);
      model = (LabelValueListSerializer)klass.newInstance();
    } catch (Throwable t) {
      throw new IllegalArgumentException("Unable to create class " + modelClass);
    }
  }
}