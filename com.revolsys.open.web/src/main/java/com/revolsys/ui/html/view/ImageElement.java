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


import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

/**
 * @author paustin
 * @version 1.0
 */
public class ImageElement extends Element {
  private String src;

  private String alt;

  public ImageElement(final String src) {
    this(src, null);
  }

  /**
   * @param object
   * @param content
   */
  public ImageElement(final String src, final String alt) {
    this.src = src;
    this.alt = alt;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.revolsys.ui.model.Element#serializeElement(com.revolsys.xml.io.XmlWriter)
   */
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.IMG);
    out.attribute(HtmlUtil.ATTR_SRC, src);
    out.attribute(HtmlUtil.ATTR_ALT, alt);
    out.attribute(HtmlUtil.ATTR_TITLE, alt);
    out.endTag();
  }
}