package com.revolsys.swing.map.form;

import javax.swing.JComponent;

import org.apache.commons.beanutils.ConstructorUtils;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public class DataObjectLayerFormFactory {
  public static final String FORM_CLASS_NAME = "formClassName";

  public static JComponent createFormComponent(DataObjectLayer layer,
    DataObject object) {
    String formClassName = layer.getProperty(FORM_CLASS_NAME);
    LayerDataObjectForm form = null;
    if (StringUtils.hasText(formClassName)) {
      try {
        Class<?> formClass = Class.forName(formClassName);
        Object[] args = {
          layer
        };
        form = (LayerDataObjectForm)ConstructorUtils.invokeConstructor(
          formClass, args);
      } catch (Throwable e) {
        LoggerFactory.getLogger(DataObjectLayerFormFactory.class).error(
          "Unable to create form " + formClassName, e);
      }
    }
    if (form == null) {
      form = new LayerDataObjectForm(layer);
    }
    form.setObject(object);
    return form;
  }
}