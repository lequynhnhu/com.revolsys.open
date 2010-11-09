package com.revolsys.spring;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;

public class NonExistingResource extends AbstractResource {

  public NonExistingResource() {
   }

  public String getDescription() {
    return "resource which does not exist";
  }

  public InputStream getInputStream()
    throws IOException {
    return null;
  }

  @Override
  public boolean exists() {
    return false;
  }
}