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
package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlUtil;

public class ErrorField extends Field {
  private String message;

  /**
   * @param name
   * @param required
   */
  public ErrorField(final String name, final boolean required) {
    super(name, true);
  }

  @Override
  public boolean hasValue() {
    return false;
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "alert");
    out.write(this.message);
    out.endTag(HtmlUtil.DIV);
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
