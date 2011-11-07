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

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;
import com.revolsys.xml.io.XmlWriter;

public class HiddenField extends Field {
  private String inputValue = "";

  private boolean fixedValue = false;

  /**
   * @param name
   * @param required
   */
  public HiddenField(final String name, final boolean required) {
    super(name, required);
  }

  public HiddenField(final String name, final String value) {
    super(name, false);
    this.inputValue = value;
    fixedValue = true;
  }

  public HiddenField(final String name, final Object value) {
    this(name, value.toString());
  }

  public String getInputValue() {
    return inputValue;
  }

  public boolean hasValue() {
    return inputValue != null && !inputValue.equals("");
  }

  public void initialize(final Form form, final HttpServletRequest request) {
    if (!fixedValue) {
      inputValue = request.getParameter(getName());
      if (inputValue == null) {
        setValue(getInitialValue(request));
        if (getValue() != null) {
          inputValue = getValue().toString();
        }
      }
    }
  }

  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    }
    if (valid) {
      setValue(inputValue);
    }
    return valid;
  }

  public void serializeElement(final XmlWriter out) {
    HtmlUtil.serializeHiddenInput(out, getName(), inputValue);
  }
}