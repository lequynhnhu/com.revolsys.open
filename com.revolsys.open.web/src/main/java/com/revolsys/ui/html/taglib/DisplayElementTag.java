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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.web.servlet.HttpServletLogUtil;

public class DisplayElementTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 7616198383718213550L;

  private static final Logger log = LoggerFactory.getLogger(DisplayElementTag.class);

  private String name;

  private boolean useNamespaces = true;

  public int doStartTag() throws JspException {
    if (name != null) {
      HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
      try {
        Element element = (Element)request.getAttribute(name);
        if (element != null) {
          Writer out = pageContext.getOut();
          element.serialize(out, useNamespaces);
        }
      } catch (Throwable t) {
        HttpServletLogUtil.logRequestException(log, request, t);
        throw new JspException(t.getMessage(), t);
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

  public boolean isUseNamespaces() {
    return useNamespaces;
  }

  public void setUseNamespaces(final boolean useNamespaces) {
    this.useNamespaces = useNamespaces;
  }
}