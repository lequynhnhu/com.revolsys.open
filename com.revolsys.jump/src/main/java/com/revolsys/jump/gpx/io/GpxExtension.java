package com.revolsys.jump.gpx.io;

import org.openjump.core.ui.io.file.FileLayerLoader;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

public class GpxExtension extends Extension {

  public void configure(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    Registry registry = workbenchContext.getRegistry();

    GpxFileLoader loader = new GpxFileLoader(workbenchContext);
    registry.createEntry(FileLayerLoader.KEY, loader);

  }

}