package com.revolsys.swing.builder;


public class StringUiBuilder extends AbstractUiBuilder {

  @Override
  public void appendHtml(final StringBuffer s, final Object object) {
    if (object != null) {
      s.append(escapeHTML(object.toString(), false, false));
    }
  }
}
