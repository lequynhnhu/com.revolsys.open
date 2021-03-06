package com.revolsys.ui.html.fields;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.DateUtil;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class DateTimeField extends Field {

  private String inputValue;

  public DateTimeField(final String name, final boolean required,
    final Object defaultValue) {
    super(name, required);
    setInitialValue(defaultValue);
    setValue(defaultValue);
    setDefaultInstructions("Enter Date/Time in format yyyy-MM-dd HH:mm:ss.SSS");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.inputValue = request.getParameter(getName());
    if (this.inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        this.inputValue = StringConverterRegistry.toString(Date.class,
          getValue());
      }
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {

      if (valid) {
        try {
          final Date date = DateUtil.getDate(this.inputValue);
          setValue(date);
        } catch (final Throwable e) {
          addValidationError("Invalid Date Time");
          valid = false;

        }
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    if (Property.hasValue(this.inputValue)) {
      out.attribute(HtmlUtil.ATTR_VALUE, this.inputValue);
    }
    out.attribute(HtmlUtil.ATTR_SIZE, 30);
    if (isRequired()) {
      out.attribute(HtmlUtil.ATTR_CLASS, "required");
    }

    out.endTag(HtmlUtil.INPUT);
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      this.inputValue = StringConverterRegistry.toString(value);
    } else {
      this.inputValue = null;
    }
  }

}
