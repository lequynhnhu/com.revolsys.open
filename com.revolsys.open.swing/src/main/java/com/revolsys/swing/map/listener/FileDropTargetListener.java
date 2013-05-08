package com.revolsys.swing.map.listener;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.util.LayerUtil;

public class FileDropTargetListener implements DropTargetListener,
  HierarchyListener {
  private static final String ZERO_CHAR_STRING = String.valueOf((char)0);

  private static final Logger LOG = Logger.getLogger(FileDropTargetListener.class);

  private final MapPanel map;

  public FileDropTargetListener(final MapPanel map) {
    this.map = map;
    addDropTarget(map);
  }

  public void addDropTarget(final Component component) {
    if (component.getParent() != null) {
      new DropTarget(component, this);
    }

    component.addHierarchyListener(this);
    if (component.getParent() != null) {
      new DropTarget(component, this);
    }

    if (component instanceof Container) {
      final Container container = (Container)component;

      final Component[] components = container.getComponents();
      for (final Component child : components) {
        addDropTarget(child);
      }
    }
  }

  @Override
  public void dragEnter(final DropTargetDragEvent event) {
    if (isDragOk(event)) {
      event.acceptDrag(DnDConstants.ACTION_COPY);
    } else {
      event.rejectDrag();
    }
  }

  @Override
  public void dragExit(final DropTargetEvent event) {
  }

  @Override
  public void dragOver(final DropTargetDragEvent event) {
  }

  @Override
  @SuppressWarnings("unchecked")
  public void drop(final DropTargetDropEvent event) {
    try {
      final Transferable tr = event.getTransferable();
      List<File> files = null;
      if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        event.acceptDrop(DnDConstants.ACTION_COPY);

        files = (List<File>)tr.getTransferData(DataFlavor.javaFileListFlavor);
      } else {
        final DataFlavor[] flavors = tr.getTransferDataFlavors();
        boolean handled = false;
        for (final DataFlavor flavor : flavors) {
          if (flavor.isRepresentationClassReader()) {
            event.acceptDrop(DnDConstants.ACTION_COPY);

            final BufferedReader reader = new BufferedReader(
              flavor.getReaderForText(tr));
            handled = true;
            files = new ArrayList<File>();
            String fileName = null;
            while ((fileName = reader.readLine()) != null) {
              try {
                // kde seems to append a 0 char to the end of the reader
                if (!ZERO_CHAR_STRING.equals(fileName)) {
                  final File file = new File(new URI(fileName));
                  files.add(file);
                }
              } catch (final URISyntaxException e) {
                LOG.error("Drag and Drop file " + fileName + " not valid", e);
              }
            }
          }
        }
        if (!handled) {
          event.rejectDrop();
          return;
        }
      }
      if (files != null) {
        LayerUtil.openFiles(files);
      }
      event.getDropTargetContext().dropComplete(true);
    } catch (final Throwable e) {
      LOG.error("Unable to drop", e);
    }
  }

  @Override
  public void dropActionChanged(final DropTargetDragEvent event) {
    if (isDragOk(event)) {
      event.acceptDrag(DnDConstants.ACTION_COPY);
    } else {
      event.rejectDrag();
    }
  }

  @Override
  public void hierarchyChanged(final HierarchyEvent event) {
    final Component component = event.getComponent();
    final java.awt.Component parent = component.getParent();
    if (parent == null) {
      component.setDropTarget(null);
    } else {
      new DropTarget(component, this);
    }
  }

  private boolean isDragOk(final DropTargetDragEvent event) {
    final DataFlavor[] flavors = event.getCurrentDataFlavors();
    for (final DataFlavor flavor : flavors) {
      if (flavor.equals(DataFlavor.javaFileListFlavor)
        || flavor.isRepresentationClassReader()) {
        return true;
      }
    }
    return false;
  }
}
