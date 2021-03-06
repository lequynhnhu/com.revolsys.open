package com.revolsys.io.esri.gdb.xml.model;

import java.util.concurrent.atomic.AtomicInteger;

public class DEDataset extends DataElement {
  private final AtomicInteger DSID = new AtomicInteger(0);

  private String datasetType;

  private int dsid = this.DSID.incrementAndGet();

  private boolean versioned;

  private boolean canVersion;

  private String configurationKeyword = "";

  public String getConfigurationKeyword() {
    return this.configurationKeyword;
  }

  public String getDatasetType() {
    return this.datasetType;
  }

  public int getDSID() {
    return this.dsid;
  }

  public boolean isCanVersion() {
    return this.canVersion;
  }

  public boolean isVersioned() {
    return this.versioned;
  }

  public void setCanVersion(final boolean canVersion) {
    this.canVersion = canVersion;
  }

  public void setConfigurationKeyword(final String configurationKeyword) {
    this.configurationKeyword = configurationKeyword;
  }

  public void setDatasetType(final String datasetType) {
    this.datasetType = datasetType;
  }

  public void setDSID(final int dsid) {
    this.dsid = dsid;
  }

  public void setVersioned(final boolean versioned) {
    this.versioned = versioned;
  }

}
