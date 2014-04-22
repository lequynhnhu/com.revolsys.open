/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.operation.IsSimpleOp;
import com.revolsys.jts.operation.valid.IsValidOp;
import com.revolsys.jts.operation.valid.TopologyValidationError;
import com.revolsys.jtstest.testbuilder.model.TestCaseEdit;

/**
 * @version 1.7
 */
public class ValidPanel extends JPanel {
  TestCaseEdit testCase;

  private Coordinates markPoint = null;

  // ===========================================
  JButton btnValidate = new JButton();

  JButton btnSimple = new JButton();

  JTextField txtIsValid = new JTextField();

  JTextArea taInvalidMsg = new JTextArea();

  JLabel jLabel1 = new JLabel();

  JPanel jPanel1 = new JPanel();

  GridBagLayout gridBagLayout2 = new GridBagLayout();

  private transient Vector validPanelListeners;

  GridLayout gridLayout1 = new GridLayout();

  JPanel markPanel = new JPanel();

  JPanel markSquishPanel = new JPanel();

  JPanel jPanel3 = new JPanel();

  JPanel markBtnPanel = new JPanel();

  JTextField txtMark = new JTextField();

  GridBagLayout gridBagLayout1 = new GridBagLayout();

  JLabel markLabel = new JLabel();

  JButton btnClearMark = new JButton();

  JButton btnSetMark = new JButton();

  public ValidPanel() {
    try {
      jbInit();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  public synchronized void addValidPanelListener(final ValidPanelListener l) {
    final Vector v = validPanelListeners == null ? new Vector(2)
      : (Vector)validPanelListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      validPanelListeners = v;
    }
  }

  void btnClearMark_actionPerformed(final ActionEvent e) {
    setMarkPoint(null);
  }

  void btnSetMark_actionPerformed(final ActionEvent e) {
    final String xyStr = txtMark.getText();
    setMarkPoint(parseXY(xyStr));
  }

  void btnSimple_actionPerformed(final ActionEvent e) {
    boolean isSimple = true;
    Coordinates nonSimpleLoc = null;
    if (testCase.getGeometry(0) != null) {
      final IsSimpleOp simpleOp = new IsSimpleOp(testCase.getGeometry(0));
      isSimple = simpleOp.isSimple();
      nonSimpleLoc = simpleOp.getNonSimpleLocation();
    }
    final String msg = isSimple ? "" : "Self-intersection at "
      + WktWriter.point(nonSimpleLoc);
    taInvalidMsg.setText(msg);
    txtIsValid.setText(isSimple ? "Y" : "N");
    setMarkPoint(nonSimpleLoc);
  }

  void btnValidate_actionPerformed(final ActionEvent e) {
    TopologyValidationError err = null;
    if (testCase.getGeometry(0) != null) {
      final IsValidOp validOp = new IsValidOp(testCase.getGeometry(0));
      err = validOp.getValidationError();
    }
    String msg = "";
    boolean isValid = true;
    Coordinates invalidPoint = null;
    if (err != null) {
      isValid = false;
      msg = err.toString();
      invalidPoint = err.getCoordinate();
    }
    taInvalidMsg.setText(msg);
    txtIsValid.setText(isValid ? "Y" : "N");
    setMarkPoint(invalidPoint);
  }

  protected void fireSetHighlightPerformed(final ValidPanelEvent e) {
    if (validPanelListeners != null) {
      final Vector listeners = validPanelListeners;
      final int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ValidPanelListener)listeners.elementAt(i)).setHighlightPerformed(e);
      }
    }
  }

  public Coordinates getMarkPoint() {
    return markPoint;
  }

  void jbInit() throws Exception {
    btnValidate.setText("Valid?");
    btnValidate.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        btnValidate_actionPerformed(e);
      }
    });
    btnSimple.setText("Simple?");
    btnSimple.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        btnSimple_actionPerformed(e);
      }
    });
    this.setLayout(gridLayout1);
    txtIsValid.setBackground(SystemColor.control);
    txtIsValid.setEditable(false);
    txtIsValid.setText("Y");
    txtIsValid.setHorizontalAlignment(SwingConstants.CENTER);
    taInvalidMsg.setPreferredSize(new Dimension(70, 80));
    taInvalidMsg.setLineWrap(true);
    taInvalidMsg.setBorder(BorderFactory.createLoweredBevelBorder());
    taInvalidMsg.setMinimumSize(new Dimension(70, 70));
    taInvalidMsg.setToolTipText("");
    taInvalidMsg.setBackground(SystemColor.inactiveCaptionText);
    taInvalidMsg.setEditable(false);
    taInvalidMsg.setFont(new java.awt.Font("SansSerif", 0, 12));
    jLabel1.setToolTipText("");
    jLabel1.setText("Valid / Simple ");
    jPanel1.setLayout(gridBagLayout2);
    gridLayout1.setRows(2);
    markLabel.setToolTipText("");
    markLabel.setText("Mark Point ( X Y ) ");
    btnClearMark.setToolTipText("");
    btnClearMark.setText("Clear Mark");
    btnClearMark.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        btnClearMark_actionPerformed(e);
      }
    });
    btnSetMark.setToolTipText("");
    btnSetMark.setText("Set Mark");
    btnSetMark.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        btnSetMark_actionPerformed(e);
      }
    });
    jPanel3.add(btnValidate);
    jPanel3.add(btnSimple);
    jPanel1.add(jPanel3, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 5, 10,
        5), 0, 0));
    jPanel1.add(txtIsValid, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 0, 4, 0),
      10, 0));
    jPanel1.add(taInvalidMsg, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 4, 0, 4), 0, 0));
    jPanel1.add(jLabel1, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
      GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 4),
      0, 0));
    this.add(jPanel1, null);
    markPanel.setLayout(new BorderLayout());
    /*
     * markPanel.setLayout(gridBagLayout1); markPanel.add(jLabel2, new
     * GridBagConstraints(0, 0, 1, 1, 1.0, 0.0 ,GridBagConstraints.EAST,
     * GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); //
     * jPanel2.add(jLabel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0 //
     * ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0,
     * 0), 0, 0)); markPanel.add(txtMark, new GridBagConstraints(1, 1, 2, 1,
     * 0.5, 0.0 ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new
     * Insets(0, 0, 0, 20), 0, 0)); // jPanel2.add(txtX, new
     * GridBagConstraints(1, 0, 2, 1, 0.5, 0.0 // ,GridBagConstraints.CENTER,
     * GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 20), 0, 0));
     */

    markBtnPanel.add(btnSetMark);
    markBtnPanel.add(btnClearMark);
    markPanel.add(markLabel, BorderLayout.NORTH);
    markPanel.add(txtMark, BorderLayout.CENTER);
    markPanel.add(markBtnPanel, BorderLayout.SOUTH);

    markSquishPanel.setLayout(new BorderLayout());
    markSquishPanel.add(markPanel, BorderLayout.NORTH);
    /*
     * markPanel.add(btnSetMark, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
     * ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 0, 4,
     * 10), 0, 0)); markPanel.add(btnClearMark, new GridBagConstraints(2, 2, 1,
     * 1, 0.0, 0.0 ,GridBagConstraints.WEST, GridBagConstraints.NONE, new
     * Insets(4, 10, 4, 10), 0, 0));
     */
    this.add(markSquishPanel, null);
  }

  double parseNumber(final String[] xy, final int index) {
    if (xy.length <= index) {
      return 0.0;
    }
    final String s = xy[index];
    try {
      return Double.parseDouble(s);
    } catch (final NumberFormatException ex) {
      // just eat it - not much we can do
    }
    return 0.0;
  }

  Coordinates parseXY(final String xyStr) {
    final String[] xy = xyStr.trim().split("\\s+");
    final double x = parseNumber(xy, 0);
    final double y = parseNumber(xy, 1);
    return new Coordinate(x, y);
  }

  public synchronized void removeValidPanelListener(final ValidPanelListener l) {
    if (validPanelListeners != null && validPanelListeners.contains(l)) {
      final Vector v = (Vector)validPanelListeners.clone();
      v.removeElement(l);
      validPanelListeners = v;
    }
  }

  private void setMarkPoint(final Coordinates coord) {
    markPoint = coord;
    String markText = "";
    if (markPoint != null) {
      markText = " " + coord.getX() + "  " + coord.getY() + " ";
    }
    txtMark.setText(markText);
    fireSetHighlightPerformed(new ValidPanelEvent(this));
  }

  public void setTestCase(final TestCaseEdit testCase) {
    this.testCase = testCase;
  }
}
