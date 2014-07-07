package com.revolsys.gdal;

import org.gdal.gdal.gdal;

public class GdalException extends RuntimeException {
  private final int errorType = gdal.GetLastErrorType();

  private final int errorNumber = gdal.GetLastErrorNo();

  public GdalException() {
    super(gdal.GetLastErrorMsg().trim());
    gdal.ErrorReset();
  }

  public int getErrorNumber() {
    return errorNumber;
  }

  public int getErrorType() {
    return errorType;
  }
}