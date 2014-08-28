package com.revolsys.gis.desktop.print;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.swing.ImageIcon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.Invoke;

public class Print extends I18nAction {
  /**
   *
   */
  private static final long serialVersionUID = 8194892040166851551L;

  private static final ImageIcon ICON = SilkIconLoader.getIcon("printer");

  private PrintService printService;

  public Print() {
    super(ICON);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Project project = Project.get();
    final Viewport2D viewport = MapPanel.get(project).getViewport();

    final PrinterJob job = PrinterJob.getPrinterJob();

    final PageFormat format = job.defaultPage();
    format.setOrientation(PageFormat.PORTRAIT);
    final Paper paper = format.getPaper();
    paper.setImageableArea(29, 29, format.getWidth() - 58,
      format.getHeight() - 58);
    format.setPaper(paper);
    if (this.printService != null) {
      try {
        job.setPrintService(this.printService);
      } catch (final PrinterException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    final BoundingBox boundingBox = viewport.getBoundingBox();
    final MapPageable pageable = new MapPageable(project, boundingBox, format,
      20000, 300, 200);
    job.setPageable(pageable);
    final boolean doPrint = job.printDialog();
    if (doPrint) {
      this.printService = job.getPrintService();
      Invoke.background("Print", job, "print");
    }

  }
}