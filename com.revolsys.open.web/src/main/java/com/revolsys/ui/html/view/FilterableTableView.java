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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.TextAreaField;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.layout.TableBodyLayout;
import com.revolsys.ui.html.serializer.KeySerializerTableSerializer;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.util.HtmlUtil;

public class FilterableTableView extends ElementContainer {
  private String cssClass = "table";

  private final KeySerializerTableSerializer model;

  private String noRecordsMessgae = "No records found";

  private Map<String, Element> searchFields;

  public FilterableTableView(final KeySerializerTableSerializer model,
    final Map<String, Element> searchFields, final String cssClass) {
    this.model = model;
    this.searchFields = searchFields;
    this.cssClass = cssClass;
  }

  public FilterableTableView(final KeySerializerTableSerializer model,
    final String cssClass) {
    this.model = model;
    this.cssClass = cssClass;
  }

  /**
   * @return Returns the noRecordsMessgae.
   */
  public String getNoRecordsMessgae() {
    return this.noRecordsMessgae;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    if (this.searchFields != null) {
      final ElementContainer searchContainer = new ElementContainer(
        new TableBodyLayout("search", this.model.getColumnCount()));
      add(searchContainer);
      for (final KeySerializer serializer : this.model.getSerializers()) {
        final String name = serializer.getName();
        Element element = this.searchFields.get(name);
        if (element == null) {
          element = NbspElement.INSTANCE;
        } else {
          element = element.clone();
          if (element instanceof Field) {
            final Field field = (Field)element;
            field.setRequired(false);
          }
          if (element instanceof TextField) {
            final TextField textField = (TextField)element;
            textField.setSize(1);
          }
          if (element instanceof TextAreaField) {
            final TextAreaField textField = (TextAreaField)element;
            textField.setRows(1);
            textField.setCols(1);
          }
        }
        searchContainer.add(element);
      }
    }
    super.initialize(request);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final int rowCount = this.model.getBodyRowCount();
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, this.cssClass);

    out.startTag(HtmlUtil.TABLE);
    out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
    out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");
    out.attribute(HtmlUtil.ATTR_CLASS, "data");

    serializeHeadings(out);
    serializeFooter(out);
    serializeRows(out);

    out.endTag(HtmlUtil.TABLE);
    if (rowCount == 0) {
      out.startTag(HtmlUtil.I);
      out.text(this.noRecordsMessgae);
      out.endTag(HtmlUtil.I);
    }
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeFooter(final XmlWriter out) {
    final int rowCount = this.model.getFooterRowCount();
    if (rowCount > 0) {
      out.startTag(HtmlUtil.TFOOT);
      for (int row = 0; row < rowCount; row++) {
        serializeFooterRow(out, row, rowCount);
      }
      out.endTag(HtmlUtil.TFOOT);
    }
  }

  protected void serializeFooterRow(
    final XmlWriter out,
    final int row,
    final int rowCount) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlUtil.TR);
    String rowCss = "";
    if (row == 0) {
      rowCss += " firstRow";
    }
    if (row == rowCount - 1) {
      rowCss += " lastRow";
    }
    if (rowCss.length() > 0) {
      out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
    }
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TD);
      String colCssClass = this.model.getFooterCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      this.model.serializeFooterCell(out, row, col);
      out.endTag(HtmlUtil.TD);
    }
    out.endTag(HtmlUtil.TR);
  }

  protected void serializeHeadings(final XmlWriter out) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlUtil.THEAD);
    out.startTag(HtmlUtil.TR);
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TH);
      String colCssClass = this.model.getHeaderCssClass(col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      }
      if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      if (colCssClass.length() > 0) {
        out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      }
      this.model.serializeHeaderCell(out, col);
      out.endTag(HtmlUtil.TH);
    }
    out.endTag(HtmlUtil.TR);
    out.endTag(HtmlUtil.THEAD);
  }

  protected void serializeRow(
    final XmlWriter out,
    final int row,
    final int rowCount) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlUtil.TR);
    String rowCss = "";
    if (row == 0) {
      rowCss += " firstRow";
    }
    if (row == rowCount - 1) {
      rowCss += " lastRow";
    }
    if (row % 2 == 1) {
      rowCss += " even";
    }
    if (rowCss.length() > 0) {
      out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
    }
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TD);
      String colCssClass = this.model.getBodyCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      this.model.serializeBodyCell(out, row, col);
      out.endTag(HtmlUtil.TD);
    }
    out.endTag(HtmlUtil.TR);
  }

  protected void serializeRows(final XmlWriter out) {
    for (final Element element : getElements()) {
      element.serialize(out);
    }

    out.startTag(HtmlUtil.TBODY);
    final int rowCount = this.model.getBodyRowCount();
    for (int row = 0; row < rowCount; row++) {
      serializeRow(out, row, rowCount);
    }
    out.endTag(HtmlUtil.TBODY);
  }

  /**
   * @param noRecordsMessgae The noRecordsMessgae to set.
   */
  public void setNoRecordsMessgae(final String noRecordsMessgae) {
    this.noRecordsMessgae = noRecordsMessgae;
  }

  public void setRows(final List<?> results) {
    this.model.setRows(results);
  }
}
