package com.revolsys.gis.data.model.property;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.LineString;

public class DirectionalAttributesTest {

  private static final Logger LOG = LoggerFactory.getLogger(DirectionalAttributesTest.class);

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory();

  private static final String DIRECTIONAL = "DIRECTIONAL";

  private static final String LEFT = "LEFT";

  private static final String RIGHT = "RIGHT";

  private static final String START_LEFT = "START_LEFT";

  private static final String START_RIGHT = "START_RIGHT";

  private static final String END_LEFT = "END_LEFT";

  private static final String END_RIGHT = "END_RIGHT";

  private static final String START_LEFT_TURN = "START_LEFT_TURN";

  private static final String START_RIGHT_TURN = "START_RIGHT_TURN";

  private static final String END_LEFT_TURN = "END_LEFT_TURN";

  private static final String END_RIGHT_TURN = "END_RIGHT_TURN";

  private static final String START = "START";

  private static final String END = "END";

  private static final String BACKWARDS = "Backwards";

  private static final String FORWARDS = "Forwards";

  private static final String EITHER = "Either";

  private static final String OTHER = "Other";

  private static DataObjectMetaDataImpl TABLE;

  private static Map<String, String> DIRECTIONAL_VALUES = new LinkedHashMap<String, String>();

  private final Coordinates MERGE_COORDINATES = new DoubleCoordinates(10, 20);

  private final LineString LINE1 = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 0, 0, 10, 20));

  private final LineString MERGED_LINE = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 0, 0, 10, 20, 20, 30));

  private final LineString REVERSE_MERGED_LINE = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 20, 30, 10, 20, 0, 0));

  private final LineString REVERSE_LINE1 = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 10, 20, 0, 0));

  private final LineString LINE2 = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 10, 20, 20, 30));

  private final LineString LINE3 = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 11, 20, 20, 30));

  private final LineString REVERSE_LINE2 = GEOMETRY_FACTORY.createLineString(new DoubleCoordinatesList(
    2, 20, 30, 10, 20));

  private static DirectionalAttributes DIRECTIONAL_ATTRIBUTES;
  static {
    DIRECTIONAL_VALUES.put(FORWARDS, BACKWARDS);
    DIRECTIONAL_VALUES.put(BACKWARDS, FORWARDS);
    DIRECTIONAL_VALUES.put(EITHER, EITHER);

    TABLE = new DataObjectMetaDataImpl("Directional");
    TABLE.addAttribute(DIRECTIONAL, DataTypes.STRING, false);
    TABLE.addAttribute(LEFT, DataTypes.BOOLEAN, false);
    TABLE.addAttribute(RIGHT, DataTypes.BOOLEAN, false);
    TABLE.addAttribute(START, DataTypes.BOOLEAN, false);
    TABLE.addAttribute(END, DataTypes.BOOLEAN, false);
    TABLE.addAttribute(START_LEFT, DataTypes.STRING, false);
    TABLE.addAttribute(START_RIGHT, DataTypes.STRING, false);
    TABLE.addAttribute(END_LEFT, DataTypes.STRING, false);
    TABLE.addAttribute(END_RIGHT, DataTypes.STRING, false);
    TABLE.addAttribute(START_LEFT_TURN, DataTypes.STRING, false);
    TABLE.addAttribute(START_RIGHT_TURN, DataTypes.STRING, false);
    TABLE.addAttribute(END_LEFT_TURN, DataTypes.STRING, false);
    TABLE.addAttribute(END_RIGHT_TURN, DataTypes.STRING, false);
    TABLE.addAttribute("LINE", DataTypes.LINE_STRING, true);

    DIRECTIONAL_ATTRIBUTES = DirectionalAttributes.getProperty(TABLE);
    DIRECTIONAL_ATTRIBUTES.addDirectionalAttributeValues(DIRECTIONAL,
      DIRECTIONAL_VALUES);
    DIRECTIONAL_ATTRIBUTES.addSideAttributePair(LEFT, RIGHT);
    DIRECTIONAL_ATTRIBUTES.addEndAttributePair(START, END);
    DIRECTIONAL_ATTRIBUTES.addEndAndSideAttributePairs(START_LEFT, START_RIGHT,
      END_LEFT, END_RIGHT);
    DIRECTIONAL_ATTRIBUTES.addEndTurnAttributePairs(START_LEFT_TURN,
      START_RIGHT_TURN, END_LEFT_TURN, END_RIGHT_TURN);
  }

  private void assertAttributeEquals(final String message,
    final DataObject reverse, final String attributeName,
    final Object expectedValue) {
    final Object value = reverse.getValue(attributeName);
    Assert.assertEquals(message, expectedValue, value);
  }

  private void assertDirectionalAttributesEqual(final LineString line1,
    final String value1, final LineString line2, final String value2) {
    final DataObject object1 = createObject(line1, DIRECTIONAL, value1);
    final DataObject object2 = createObject(line2, DIRECTIONAL, value2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    Assert.assertTrue(
      "Directional attribute equal " + value1 + " != " + value2, equals);
  }

  private void assertDirectionalAttributesMerge(final LineString line1,
    final String value1, final LineString line2, final String value2,
    final LineString expectedMergedLine, final String expectedValue) {
    final DataObject object1 = createObject(line1, DIRECTIONAL, value1);
    final DataObject object2 = createObject(line2, DIRECTIONAL, value2);
    final DataObject mergedObject = DirectionalAttributes.mergeLongest(object1,
      object2);

    assertAttributeEquals("Directional attribute merge ", mergedObject,
      DIRECTIONAL, expectedValue);

    final LineString mergedLine = mergedObject.getGeometryValue();
    Assert.assertTrue("Directional attribute merge line " + expectedMergedLine
      + " != " + mergedLine,
      LineStringUtil.equalsExact2d(expectedMergedLine, mergedLine));
  }

  private void assertDirectionalAttributesNotCanMerge(final LineString line1,
    final String value1, final LineString line2, final String value2) {
    final DataObject object1 = createObject(line1, DIRECTIONAL, value1);
    final DataObject object2 = createObject(line2, DIRECTIONAL, value2);
    final boolean canMerge = DirectionalAttributes.canMergeObjects(
      this.MERGE_COORDINATES, object1, object2);
    Assert.assertFalse("Directional attribute not can merge " + value1 + " == "
        + value2, canMerge);
  }

  private void assertDirectionalAttributesNotEqual(final LineString line1,
    final String value1, final LineString line2, final String value2) {
    final DataObject object1 = createObject(line1, DIRECTIONAL, value1);
    final DataObject object2 = createObject(line2, DIRECTIONAL, value2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    Assert.assertFalse("Directional attribute not equal" + value1 + " == "
        + value2, equals);
  }

  private void assertDirectionalAttributesReverse(final String value,
    final String expectedReverseValue) {
    final DataObject object = createObject(this.LINE1, DIRECTIONAL, value);
    final DataObject reverse = reverse(object);
    final LineString reverseLine = reverse.getGeometryValue();
    Assert.assertTrue("Reverse line", this.REVERSE_LINE1.equals(reverseLine));

    assertAttributeEquals("Directional reverse attribute", reverse,
      DIRECTIONAL, expectedReverseValue);
  }

  private void assertEndAndSideAttributesEqual(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final String endLeftValue1, final String endRightValue1,
    final LineString line2, final String startLeftValue2,
    final String startRightValue2, final String endLeftValue2,
    final String endRightValue2) {
    final DataObject object1 = createObject(line1, START_LEFT, startLeftValue1,
      START_RIGHT, startRightValue1, END_LEFT, endLeftValue1, END_RIGHT,
      endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT, startLeftValue2,
      START_RIGHT, startRightValue2, END_LEFT, endLeftValue2, END_RIGHT,
      endRightValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    if (!equals) {
      LOG.error(object1.toString());
      LOG.error(object2.toString());
    }
    Assert.assertTrue("End and side attribute equal", equals);
  }

  private void assertEndAndSideAttributesMerge(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final Object endLeftValue1, final Object endRightValue1,
    final LineString line2, final Object startLeftValue2,
    final Object startRightValue2, final String endLeftValue2,
    final String endRightValue2, final LineString expectedMergedLine,
    final String expectedStartLeftValue, final String expectedStartRightValue,
    final String expectedEndLeftValue, final String expectedEndRightValue) {
    final DataObject object1 = createObject(line1, START_LEFT, startLeftValue1,
      START_RIGHT, startRightValue1, END_LEFT, endLeftValue1, END_RIGHT,
      endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT, startLeftValue2,
      START_RIGHT, startRightValue2, END_LEFT, endLeftValue2, END_RIGHT,
      endRightValue2);
    DataObject mergedObject = null;
    try {
      final boolean canMerge = DirectionalAttributes.canMergeObjects(
        this.MERGE_COORDINATES, object1, object2);
      Assert.assertTrue("End & Side attribute can't merge", canMerge);

      mergedObject = DirectionalAttributes.mergeLongest(object1, object2);

      assertAttributeEquals("End & Side attribute merge start left",
        mergedObject, START_LEFT, expectedStartLeftValue);
      assertAttributeEquals("End & Side attribute merge start right",
        mergedObject, START_RIGHT, expectedStartRightValue);
      assertAttributeEquals("End & Side attribute merge end left",
        mergedObject, END_LEFT, expectedEndLeftValue);
      assertAttributeEquals("End & Side attribute merge end right",
        mergedObject, END_RIGHT, expectedEndRightValue);

      final LineString mergedLine = mergedObject.getGeometryValue();
      Assert.assertTrue("End attribute merge line",
        LineStringUtil.equalsExact2d(expectedMergedLine, mergedLine));
    } catch (final AssertionFailedError e) {
      LOG.error(e.getMessage());
      LOG.error(expectedMergedLine.toString());
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      if (mergedObject != null) {
        LOG.error(mergedObject.toString());
      }
      throw e;
    }
  }

  private void assertEndAndSideAttributesNotCanMerge(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final Object endLeftValue1, final Object endRightValue1,
    final LineString line2, final Object startLeftValue2,
    final Object startRightValue2, final String endLeftValue2,
    final String endRightValue2) {
    final DataObject object1 = createObject(line1, START_LEFT, startLeftValue1,
      START_RIGHT, startRightValue1, END_LEFT, endLeftValue1, END_RIGHT,
      endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT, startLeftValue2,
      START_RIGHT, startRightValue2, END_LEFT, endLeftValue2, END_RIGHT,
      endRightValue2);
    final boolean canMerge = DirectionalAttributes.canMergeObjects(
      this.MERGE_COORDINATES, object1, object2);
    try {
      Assert.assertFalse("Side attribute not can't merge", canMerge);
    } catch (final AssertionFailedError e) {
      LOG.error(e.getMessage());
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      throw e;
    }
  }

  private void assertEndAndSideAttributesNotEqual(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final String endLeftValue1, final String endRightValue1,
    final LineString line2, final String startLeftValue2,
    final String startRightValue2, final String endLeftValue2,
    final String endRightValue2) {
    final DataObject object1 = createObject(line1, START_LEFT, startLeftValue1,
      START_RIGHT, startRightValue1, END_LEFT, endLeftValue1, END_RIGHT,
      endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT, startLeftValue2,
      START_RIGHT, startRightValue2, END_LEFT, endLeftValue2, END_RIGHT,
      endRightValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    if (equals) {
      LOG.error(object1.toString());
      LOG.error(object2.toString());
    }
    Assert.assertFalse("End and side attribute not equal", equals);
  }

  private void assertEndAndSideAttributesReverse(final String startLeftValue,
    final String startRightValue, final String endLeftValue,
    final String endRightValue) {
    final DataObject object = createObject(this.LINE1, START_LEFT,
      startLeftValue, START_RIGHT, startRightValue, END_LEFT, endLeftValue,
      END_RIGHT, endRightValue);
    final DataObject reverse = reverse(object);
    final LineString reverseLine = reverse.getGeometryValue();
    Assert.assertTrue("Reverse line", this.REVERSE_LINE1.equals(reverseLine));

    assertAttributeEquals("End reverse attribute (startLeft->endRight)",
      reverse, END_RIGHT, startLeftValue);
    assertAttributeEquals("End reverse attribute (startRight->endLeft)",
      reverse, END_LEFT, startRightValue);
    assertAttributeEquals("End reverse attribute (endLeft->startRight)",
      reverse, START_RIGHT, endLeftValue);
    assertAttributeEquals("End reverse attribute (endRight->startLeft)",
      reverse, START_LEFT, endRightValue);
  }

  private void assertEndAttributesEqual(final LineString line1,
    final Boolean startValue1, final Boolean endValue1, final LineString line2,
    final Boolean startValue2, final Boolean endValue2) {
    final DataObject object1 = createObject(line1, START, startValue1, END,
      endValue1);
    final DataObject object2 = createObject(line2, START, startValue2, END,
      endValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    Assert.assertTrue("End attribute equal " + startValue1 + " != "
        + startValue2 + " &  " + endValue1 + " != " + endValue2, equals);
  }

  private void assertEndAttributesMerge(final LineString line1,
    final Boolean startValue1, final Boolean endValue1, final LineString line2,
    final Boolean startValue2, final Boolean endValue2,
    final LineString expectedMergedLine, final Boolean expectedStartValue,
    final Boolean expectedEndValue) {
    final DataObject object1 = createObject(line1, START, startValue1, END,
      endValue1);
    final DataObject object2 = createObject(line2, START, startValue2, END,
      endValue2);
    final boolean canMerge = DirectionalAttributes.canMergeObjects(
      this.MERGE_COORDINATES, object1, object2);
    Assert.assertTrue("End attribute can merge  " + startValue1 + " != "
        + startValue2 + " &  " + endValue1 + " != " + endValue2, canMerge);
    final DataObject mergedObject = DirectionalAttributes.mergeLongest(object1,
      object2);

    assertAttributeEquals("End attribute merge start", mergedObject, START,
      expectedStartValue);
    assertAttributeEquals("End attribute merge end", mergedObject, END,
      expectedEndValue);

    final LineString mergedLine = mergedObject.getGeometryValue();
    Assert.assertTrue("End attribute merge line " + expectedMergedLine + " != "
        + mergedLine,
        LineStringUtil.equalsExact2d(expectedMergedLine, mergedLine));
  }

  private void assertEndAttributesNotCanMerge(final LineString line1,
    final Boolean startValue1, final Boolean endValue1, final LineString line2,
    final Boolean startValue2, final Boolean endValue2) {
    final DataObject object1 = createObject(line1, START, startValue1, END,
      endValue1);
    final DataObject object2 = createObject(line2, START, startValue2, END,
      endValue2);
    final boolean canMerge = DirectionalAttributes.canMergeObjects(
      this.MERGE_COORDINATES, object1, object2);
    Assert.assertFalse("Side attribute not can merge  " + startValue1 + " == "
        + startValue2 + " ||  " + endValue1 + " == " + endValue2, canMerge);
  }

  private void assertEndAttributesNotEqual(final LineString line1,
    final Boolean startValue1, final Boolean endValue1, final LineString line2,
    final Boolean startValue2, final Boolean endValue2) {
    final DataObject object1 = createObject(line1, START, startValue1, END,
      endValue1);
    final DataObject object2 = createObject(line2, START, startValue2, END,
      endValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    Assert.assertFalse("Side attribute not equal " + startValue1 + " == "
        + startValue2 + " &  " + endValue1 + " == " + endValue2, equals);
  }

  private void assertEndAttributesReverse(final Boolean startValue,
    final Boolean endValue) {
    final DataObject object = createObject(this.LINE1, START, startValue, END,
      endValue);
    final DataObject reverse = reverse(object);
    final LineString reverseLine = reverse.getGeometryValue();
    Assert.assertTrue("Reverse line", this.REVERSE_LINE1.equals(reverseLine));

    assertAttributeEquals("Side reverse attribute (start->end)", reverse, END,
      startValue);
    assertAttributeEquals("Side reverse attribute (end->start)", reverse,
      START, endValue);
  }

  private void assertEndTurnAttributesEqual(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final String endLeftValue1, final String endRightValue1,
    final LineString line2, final String startLeftValue2,
    final String startRightValue2, final String endLeftValue2,
    final String endRightValue2) {
    final DataObject object1 = createObject(line1, START_LEFT_TURN,
      startLeftValue1, START_RIGHT_TURN, startRightValue1, END_LEFT_TURN,
      endLeftValue1, END_RIGHT_TURN, endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT_TURN,
      startLeftValue2, START_RIGHT_TURN, startRightValue2, END_LEFT_TURN,
      endLeftValue2, END_RIGHT_TURN, endRightValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    if (!equals) {
      LOG.error(object1.toString());
      LOG.error(object2.toString());
    }
    Assert.assertTrue("End turn attribute equal", equals);
  }

  private void assertEndTurnAttributesMerge(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final Object endLeftValue1, final Object endRightValue1,
    final LineString line2, final Object startLeftValue2,
    final Object startRightValue2, final String endLeftValue2,
    final String endRightValue2, final LineString expectedMergedLine,
    final String expectedStartLeftValue, final String expectedStartRightValue,
    final String expectedEndLeftValue, final String expectedEndRightValue) {
    final DataObject object1 = createObject(line1, START_LEFT_TURN,
      startLeftValue1, START_RIGHT_TURN, startRightValue1, END_LEFT_TURN,
      endLeftValue1, END_RIGHT_TURN, endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT_TURN,
      startLeftValue2, START_RIGHT_TURN, startRightValue2, END_LEFT_TURN,
      endLeftValue2, END_RIGHT_TURN, endRightValue2);
    DataObject mergedObject = null;
    try {
      final boolean canMerge = DirectionalAttributes.canMergeObjects(
        this.MERGE_COORDINATES, object1, object2);
      Assert.assertTrue("End & Side attribute can't merge", canMerge);

      mergedObject = DirectionalAttributes.mergeLongest(object1, object2);

      assertAttributeEquals("End turn attribute merge start left",
        mergedObject, START_LEFT_TURN, expectedStartLeftValue);
      assertAttributeEquals("End turn attribute merge start right",
        mergedObject, START_RIGHT_TURN, expectedStartRightValue);
      assertAttributeEquals("End turn attribute merge end left", mergedObject,
        END_LEFT_TURN, expectedEndLeftValue);
      assertAttributeEquals("End turn attribute merge end right", mergedObject,
        END_RIGHT_TURN, expectedEndRightValue);

      final LineString mergedLine = mergedObject.getGeometryValue();
      Assert.assertTrue("End turn attribute merge line",
        LineStringUtil.equalsExact2d(expectedMergedLine, mergedLine));
    } catch (final AssertionFailedError e) {
      LOG.error(e.getMessage());
      LOG.error(expectedMergedLine.toString());
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      if (mergedObject != null) {
        LOG.error(mergedObject.toString());
      }
      throw e;
    }
  }

  private void assertEndTurnAttributesNotCanMerge(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final Object endLeftValue1, final Object endRightValue1,
    final LineString line2, final Object startLeftValue2,
    final Object startRightValue2, final String endLeftValue2,
    final String endRightValue2) {
    final DataObject object1 = createObject(line1, START_LEFT_TURN,
      startLeftValue1, START_RIGHT_TURN, startRightValue1, END_LEFT_TURN,
      endLeftValue1, END_RIGHT_TURN, endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT_TURN,
      startLeftValue2, START_RIGHT_TURN, startRightValue2, END_LEFT_TURN,
      endLeftValue2, END_RIGHT_TURN, endRightValue2);
    final boolean canMerge = DirectionalAttributes.canMergeObjects(
      this.MERGE_COORDINATES, object1, object2);
    try {
      Assert.assertFalse("End turn attribute not can't merge", canMerge);
    } catch (final AssertionFailedError e) {
      LOG.error(e.getMessage());
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      throw e;
    }
  }

  private void assertEndTurnAttributesNotEqual(final LineString line1,
    final String startLeftValue1, final String startRightValue1,
    final String endLeftValue1, final String endRightValue1,
    final LineString line2, final String startLeftValue2,
    final String startRightValue2, final String endLeftValue2,
    final String endRightValue2) {
    final DataObject object1 = createObject(line1, START_LEFT_TURN,
      startLeftValue1, START_RIGHT_TURN, startRightValue1, END_LEFT_TURN,
      endLeftValue1, END_RIGHT_TURN, endRightValue1);
    final DataObject object2 = createObject(line2, START_LEFT, startLeftValue2,
      START_RIGHT_TURN, startRightValue2, END_LEFT_TURN, endLeftValue2,
      END_RIGHT_TURN, endRightValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    if (equals) {
      LOG.error(object1.toString());
      LOG.error(object2.toString());
    }
    Assert.assertFalse("End turn attribute not equal", equals);
  }

  private void assertEndTurnAttributesReverse(final String startLeftValue,
    final String startRightValue, final String endLeftValue,
    final String endRightValue) {
    final DataObject object = createObject(this.LINE1, START_LEFT_TURN,
      startLeftValue, START_RIGHT_TURN, startRightValue, END_LEFT_TURN,
      endLeftValue, END_RIGHT_TURN, endRightValue);
    final DataObject reverse = reverse(object);
    final LineString reverseLine = reverse.getGeometryValue();
    Assert.assertTrue("Reverse line", this.REVERSE_LINE1.equals(reverseLine));

    assertAttributeEquals(
      "End turn reverse attribute (endRight->startRightValue)", reverse,
      END_RIGHT_TURN, startRightValue);
    assertAttributeEquals(
      "End turn reverse attribute (endLeft->startLeftValue)", reverse,
      END_LEFT_TURN, startLeftValue);
    assertAttributeEquals(
      "End turn reverse attribute (startRightValue->endRight)", reverse,
      START_RIGHT_TURN, endRightValue);
    assertAttributeEquals(
      "End turn reverse attribute (startLeftValue->endLeft)", reverse,
      START_LEFT_TURN, endLeftValue);
  }

  private void assertSideAttributesEqual(final LineString line1,
    final Boolean startValue1, final Boolean endValue1, final LineString line2,
    final Boolean startValue2, final Boolean endValue2) {
    final DataObject object1 = createObject(line1, START, startValue1, END,
      endValue1);
    final DataObject object2 = createObject(line2, START, startValue2, END,
      endValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    Assert.assertTrue("Side attribute equal " + startValue1 + " != "
        + startValue2 + " &  " + endValue1 + " != " + endValue2, equals);
  }

  private void assertSideAttributesMerge(final LineString line1,
    final Boolean leftValue1, final Boolean endValue1, final LineString line2,
    final Boolean leftValue2, final Boolean endValue2,
    final LineString expectedMergedLine, final Boolean expectedLeftValue,
    final Boolean expectedRightValue) {
    final DataObject object1 = createObject(line1, LEFT, leftValue1, RIGHT,
      endValue1);
    final DataObject object2 = createObject(line2, LEFT, leftValue2, RIGHT,
      endValue2);
    final DataObject mergedObject = DirectionalAttributes.mergeLongest(object1,
      object2);

    assertAttributeEquals("Side attribute merge left", mergedObject, LEFT,
      expectedLeftValue);
    assertAttributeEquals("Side attribute merge right", mergedObject, RIGHT,
      expectedRightValue);

    final LineString mergedLine = mergedObject.getGeometryValue();
    Assert.assertTrue("Side attribute merge line",
      LineStringUtil.equalsExact2d(expectedMergedLine, mergedLine));
  }

  private void assertSideAttributesNotCanMerge(final LineString line1,
    final Boolean leftValue1, final Boolean endValue1, final LineString line2,
    final Boolean leftValue2, final Boolean endValue2) {
    final DataObject object1 = createObject(line1, LEFT, leftValue1, RIGHT,
      endValue1);
    final DataObject object2 = createObject(line2, LEFT, leftValue2, RIGHT,
      endValue2);
    final boolean canMerge = DirectionalAttributes.canMergeObjects(
      this.MERGE_COORDINATES, object1, object2);
    try {
      Assert.assertFalse("Side attribute not can't merge", canMerge);
    } catch (final AssertionFailedError e) {
      LOG.error(e.getMessage());
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      throw e;
    }
  }

  private void assertSideAttributesNotEqual(final LineString line1,
    final Boolean leftValue1, final Boolean endValue1, final LineString line2,
    final Boolean leftValue2, final Boolean endValue2) {
    final DataObject object1 = createObject(line1, LEFT, leftValue1, RIGHT,
      endValue1);
    final DataObject object2 = createObject(line2, LEFT, leftValue2, RIGHT,
      endValue2);
    final boolean equals = DirectionalAttributes.equalsObjects(object1, object2);
    Assert.assertFalse("Side attribute not equal " + leftValue1 + " == "
        + leftValue2 + " &  " + endValue1 + " == " + endValue2, equals);
  }

  private void assertSideAttributesReverse(final Boolean leftValue,
    final Boolean rightValue) {
    final DataObject object = createObject(this.LINE1, LEFT, leftValue, RIGHT,
      rightValue);
    final DataObject reverse = reverse(object);
    final LineString reverseLine = reverse.getGeometryValue();
    Assert.assertTrue("Reverse line", this.REVERSE_LINE1.equals(reverseLine));

    assertAttributeEquals("Side reverse attribute (left->right)", reverse,
      RIGHT, leftValue);
    assertAttributeEquals("Side reverse attribute (right->left)", reverse,
      LEFT, rightValue);
  }

  private DataObject createObject(final LineString line) {
    final DataObject object = new ArrayDataObject(TABLE);
    object.setGeometryValue(line);
    return object;
  }

  private DataObject createObject(final LineString line,
    final String attributeName, final Object value) {
    final DataObject object = createObject(line);
    object.setValue(attributeName, value);
    return object;
  }

  private DataObject createObject(final LineString line, final String name1,
    final Object value1, final String name2, final Object value2) {
    final DataObject object = createObject(line);
    object.setValue(name1, value1);
    object.setValue(name2, value2);
    return object;
  }

  private DataObject createObject(final LineString line, final String name1,
    final Object value1, final String name2, final Object value2,
    final String name3, final Object value3, final String name4,
    final Object value4) {
    final DataObject object = createObject(line);
    object.setValue(name1, value1);
    object.setValue(name2, value2);
    object.setValue(name3, value3);
    object.setValue(name4, value4);
    return object;
  }

  public String getReverseValue(final String value) {
    final String reverseValue = DIRECTIONAL_VALUES.get(value);
    if (reverseValue == null) {
      return value;
    } else {
      return reverseValue;
    }
  }

  protected DataObject reverse(final DataObject object) {
    return DIRECTIONAL_ATTRIBUTES.getReverse(object);
  }

  @Test
  public void testDirectionalAttributesEqual() {
    for (final String value : Arrays.asList(null, FORWARDS, BACKWARDS, EITHER,
      OTHER)) {
      final String reverseValue = getReverseValue(value);
      assertDirectionalAttributesEqual(this.LINE1, value, this.LINE1, value);
      assertDirectionalAttributesEqual(this.LINE1, value, this.REVERSE_LINE1,
        reverseValue);
      assertDirectionalAttributesEqual(this.LINE2, value, this.LINE2, value);
      assertDirectionalAttributesEqual(this.LINE2, value, this.REVERSE_LINE2,
        reverseValue);
    }

    for (final String value : Arrays.asList(FORWARDS, BACKWARDS)) {
      assertDirectionalAttributesNotEqual(this.LINE1, value,
        this.REVERSE_LINE1, value);
    }
    assertDirectionalAttributesNotEqual(this.LINE1, EITHER, this.REVERSE_LINE1,
      OTHER);
    assertDirectionalAttributesNotEqual(this.LINE1, null, this.LINE2, null);
  }

  @Test
  public void testDirectionalAttributesMerge() {
    for (final String value : Arrays.asList(null, FORWARDS, BACKWARDS, EITHER,
      OTHER)) {
      final String reverseValue = getReverseValue(value);
      assertDirectionalAttributesMerge(this.LINE1, value, this.LINE2, value,
        this.MERGED_LINE, value);
      assertDirectionalAttributesMerge(this.LINE2, value, this.LINE1, value,
        this.MERGED_LINE, value);
      assertDirectionalAttributesMerge(this.REVERSE_LINE1, value,
        this.REVERSE_LINE2, value, this.REVERSE_MERGED_LINE, value);
      assertDirectionalAttributesMerge(this.REVERSE_LINE2, value,
        this.REVERSE_LINE1, value, this.REVERSE_MERGED_LINE, value);
      assertDirectionalAttributesMerge(this.LINE1, value, this.REVERSE_LINE2,
        reverseValue, this.MERGED_LINE, value);
      assertDirectionalAttributesMerge(this.REVERSE_LINE2, value, this.LINE1,
        reverseValue, this.MERGED_LINE, reverseValue);
      assertDirectionalAttributesMerge(this.LINE2, value, this.REVERSE_LINE1,
        reverseValue, this.REVERSE_MERGED_LINE, reverseValue);
      assertDirectionalAttributesMerge(this.REVERSE_LINE1, value, this.LINE2,
        reverseValue, this.REVERSE_MERGED_LINE, value);
    }
    for (final String value : Arrays.asList(FORWARDS, BACKWARDS)) {
      assertDirectionalAttributesNotCanMerge(this.LINE1, value,
        this.REVERSE_LINE2, value);
      assertDirectionalAttributesNotCanMerge(this.REVERSE_LINE2, value,
        this.LINE1, value);
      assertDirectionalAttributesNotCanMerge(this.LINE2, value,
        this.REVERSE_LINE1, value);
      assertDirectionalAttributesNotCanMerge(this.REVERSE_LINE1, value,
        this.LINE2, value);
    }
    assertDirectionalAttributesNotCanMerge(this.LINE1, null, this.LINE1, null);
    assertDirectionalAttributesNotCanMerge(this.LINE1, null, this.LINE3, null);
  }

  @Test
  public void testDirectionalAttributesReverse() {
    for (final String value : Arrays.asList(null, FORWARDS, BACKWARDS, EITHER,
      OTHER)) {
      final String reverseValue = getReverseValue(value);
      assertDirectionalAttributesReverse(value, reverseValue);
    }
  }

  @Test
  public void testEndAndSideAttributesEqual() {
    for (final String startLeftValue : Arrays.asList("A", "B", "C", "D", null)) {
      for (final String startRightValue : Arrays.asList("B", "C", "D", null,
          "A")) {
        for (final String endLeftValue : Arrays.asList("C", "D", null, "A", "B")) {
          for (final String endRightValue : Arrays.asList("D", null, "A", "B",
              "C")) {
            assertEndAndSideAttributesEqual(this.LINE1, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.LINE1,
              startLeftValue, startRightValue, endLeftValue, endRightValue);
            assertEndAndSideAttributesEqual(this.LINE1, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.REVERSE_LINE1,
              endRightValue, endLeftValue, startRightValue, startLeftValue);
            assertEndAndSideAttributesEqual(this.LINE2, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.LINE2,
              startLeftValue, startRightValue, endLeftValue, endRightValue);
            assertEndAndSideAttributesEqual(this.LINE2, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.REVERSE_LINE2,
              endRightValue, endLeftValue, startRightValue, startLeftValue);
          }
        }
      }
    }

    for (final String startLeftValue : Arrays.asList("A", null)) {
      for (final String startRightValue : Arrays.asList("B", null)) {
        for (final String endLeftValue : Arrays.asList("C", null)) {
          for (final String endRightValue : Arrays.asList("D")) {
            assertEndAndSideAttributesNotEqual(this.LINE1, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.REVERSE_LINE1,
              startLeftValue, startRightValue, endLeftValue, endRightValue);
          }
        }
      }
    }
    assertEndAndSideAttributesNotEqual(this.LINE1, null, null, null, null,
      this.LINE2, null, null, null, null);
  }

  @Test
  public void testEndAndSideAttributesMerge() {
    for (final String startLeftValue : Arrays.asList("A", "B", "C", "D", null)) {
      for (final String startRightValue : Arrays.asList("B", "C", "D", null,
          "A")) {
        for (final String endLeftValue : Arrays.asList("C", "D", null, "A", "B")) {
          for (final String endRightValue : Arrays.asList("D", null, "A", "B",
              "C")) {
            assertEndAndSideAttributesMerge(this.LINE1, startLeftValue,
              startRightValue, null, null, this.LINE2, null, null,
              endLeftValue, endRightValue, this.MERGED_LINE, startLeftValue,
              startRightValue, endLeftValue, endRightValue);
            assertEndAndSideAttributesMerge(this.LINE1, startLeftValue,
              startRightValue, null, null, this.REVERSE_LINE2, endRightValue,
              endLeftValue, null, null, this.MERGED_LINE, startLeftValue,
              startRightValue, endLeftValue, endRightValue);
            assertEndAndSideAttributesMerge(this.REVERSE_LINE1, null, null,
              startLeftValue, startRightValue, this.LINE2, null, null,
              endRightValue, endLeftValue, this.REVERSE_MERGED_LINE,
              endLeftValue, endRightValue, startLeftValue, startRightValue);
            assertEndAndSideAttributesMerge(this.REVERSE_LINE1, null, null,
              startLeftValue, startRightValue, this.REVERSE_LINE2,
              endLeftValue, endRightValue, null, null,
              this.REVERSE_MERGED_LINE, endLeftValue, endRightValue,
              startLeftValue, startRightValue);
          }
        }
      }
    }
    final String startLeftValue = "A";
    final String startRightValue = "B";
    final String endLeftValue = "C";
    final String endRightValue = "D";

    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.LINE2, startLeftValue, null,
      endLeftValue, endRightValue);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.LINE2, null, startRightValue,
      endLeftValue, endRightValue);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.REVERSE_LINE2, endLeftValue, null,
      startLeftValue, startRightValue);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.REVERSE_LINE2, null, endRightValue,
      startLeftValue, startRightValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.LINE2, startLeftValue, null,
      endLeftValue, endRightValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.LINE2, null, startRightValue,
      endLeftValue, endRightValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, startLeftValue, startRightValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, null, startRightValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, startLeftValue, null);

    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, endLeftValue, null, this.LINE2, null, null,
      endLeftValue, endRightValue);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, endRightValue, this.LINE2, null, null,
      endLeftValue, endRightValue);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, endLeftValue, null, this.REVERSE_LINE2, endLeftValue,
      endRightValue, null, null);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, endRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, null, null);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, endLeftValue,
      endRightValue, startLeftValue, startRightValue, this.LINE2, null, null,
      null, endLeftValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, endLeftValue,
      null, startLeftValue, startRightValue, this.LINE2, null, null, null,
      endLeftValue);
    assertEndAndSideAttributesNotCanMerge(this.REVERSE_LINE1, endLeftValue,
      null, startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      null, null, null);

    assertEndAndSideAttributesNotCanMerge(this.LINE1, null, null, null, null,
      this.LINE1, null, null, null, null);
    assertEndAndSideAttributesNotCanMerge(this.LINE1, null, null, null, null,
      this.LINE3, null, null, null, null);
  }

  @Test
  public void testEndAndSideAttributesReverse() {
    for (final String startLeftValue : Arrays.asList(null, "A", "B", "C", "D")) {
      for (final String startRightValue : Arrays.asList(null, "A", "B", "C",
          "D")) {
        for (final String endLeftValue : Arrays.asList(null, "A", "B", "C", "D")) {
          for (final String endRightValue : Arrays.asList(null, "A", "B", "C",
              "D")) {
            assertEndAndSideAttributesReverse(startLeftValue, startRightValue,
              endLeftValue, endRightValue);
          }
        }
      }
    }
  }

  @Test
  public void testEndAttributesEqual() {
    for (final Boolean startValue : Arrays.asList(null, false, true)) {
      for (final Boolean endValue : Arrays.asList(null, false, true)) {
        assertEndAttributesEqual(this.LINE1, startValue, endValue, this.LINE1,
          startValue, endValue);
        assertEndAttributesEqual(this.LINE1, startValue, endValue,
          this.REVERSE_LINE1, endValue, startValue);
        assertEndAttributesEqual(this.LINE2, startValue, endValue, this.LINE2,
          startValue, endValue);
        assertEndAttributesEqual(this.LINE2, startValue, endValue,
          this.REVERSE_LINE2, endValue, startValue);
      }
    }

    for (final Boolean startValue : Arrays.asList(false, true)) {
      final Boolean endValue = !startValue;
      assertEndAttributesNotEqual(this.LINE1, startValue, endValue,
        this.REVERSE_LINE1, startValue, endValue);
    }
    assertEndAttributesNotEqual(this.LINE1, null, null, this.LINE2, null, null);
  }

  @Test
  public void testEndAttributesMerge() {
    for (final Boolean startValue : Arrays.asList(true, false, null)) {
      for (final Boolean endValue : Arrays.asList(false, true, null)) {
        assertEndAttributesMerge(this.LINE1, startValue, null, this.LINE2,
          null, endValue, this.MERGED_LINE, startValue, endValue);
        assertEndAttributesMerge(this.LINE1, startValue, null,
          this.REVERSE_LINE2, endValue, null, this.MERGED_LINE, startValue,
          endValue);
        assertEndAttributesMerge(this.REVERSE_LINE1, null, startValue,
          this.LINE2, null, endValue, this.REVERSE_MERGED_LINE, endValue,
          startValue);
        assertEndAttributesMerge(this.REVERSE_LINE1, null, startValue,
          this.REVERSE_LINE2, endValue, null, this.REVERSE_MERGED_LINE,
          endValue, startValue);
      }
    }
    for (final Boolean startValue : Arrays.asList(false, true)) {
      final Boolean endValue = !startValue;
      assertEndAttributesNotCanMerge(this.LINE1, startValue, null, this.LINE2,
        startValue, endValue);
      assertEndAttributesNotCanMerge(this.LINE1, startValue, null,
        this.REVERSE_LINE2, endValue, startValue);
      assertEndAttributesNotCanMerge(this.REVERSE_LINE1, null, startValue,
        this.LINE2, startValue, endValue);
      assertEndAttributesNotCanMerge(this.REVERSE_LINE1, null, startValue,
        this.REVERSE_LINE2, endValue, startValue);

      assertEndAttributesNotCanMerge(this.LINE1, startValue, endValue,
        this.LINE2, null, endValue);
      assertEndAttributesNotCanMerge(this.LINE1, startValue, endValue,
        this.REVERSE_LINE2, endValue, null);
      assertEndAttributesNotCanMerge(this.REVERSE_LINE1, endValue, startValue,
        this.LINE2, null, endValue);
      assertEndAttributesNotCanMerge(this.REVERSE_LINE1, endValue, startValue,
        this.REVERSE_LINE2, endValue, null);
    }
    assertEndAttributesNotCanMerge(this.LINE1, null, null, this.LINE1, null,
      null);
    assertEndAttributesNotCanMerge(this.LINE1, null, null, this.LINE3, null,
      null);
  }

  @Test
  public void testEndAttributesReverse() {
    for (final Boolean startValue : Arrays.asList(null, false, true)) {
      for (final Boolean endValue : Arrays.asList(null, false, true)) {
        assertEndAttributesReverse(startValue, endValue);
      }
    }
  }

  @Test
  public void testEndTurnAttributesEqual() {
    for (final String startLeftValue : Arrays.asList("A", "B", "C", "D", null)) {
      for (final String startRightValue : Arrays.asList("B", "C", "D", null,
          "A")) {
        for (final String endLeftValue : Arrays.asList("C", "D", null, "A", "B")) {
          for (final String endRightValue : Arrays.asList("D", null, "A", "B",
              "C")) {
            assertEndTurnAttributesEqual(this.LINE1, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.LINE1,
              startLeftValue, startRightValue, endLeftValue, endRightValue);
            assertEndTurnAttributesEqual(this.LINE1, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.REVERSE_LINE1,
              endLeftValue, endRightValue, startLeftValue, startRightValue);
            assertEndTurnAttributesEqual(this.LINE2, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.LINE2,
              startLeftValue, startRightValue, endLeftValue, endRightValue);
            assertEndTurnAttributesEqual(this.LINE2, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.REVERSE_LINE2,
              endLeftValue, endRightValue, startLeftValue, startRightValue);
          }
        }
      }
    }

    for (final String startLeftValue : Arrays.asList("A", null)) {
      for (final String startRightValue : Arrays.asList("B", null)) {
        for (final String endLeftValue : Arrays.asList("C", null)) {
          for (final String endRightValue : Arrays.asList("D")) {
            assertEndTurnAttributesNotEqual(this.LINE1, startLeftValue,
              startRightValue, endLeftValue, endRightValue, this.REVERSE_LINE1,
              startLeftValue, startRightValue, endLeftValue, endRightValue);
          }
        }
      }
    }
    assertEndTurnAttributesNotEqual(this.LINE1, null, null, null, null,
      this.LINE2, null, null, null, null);
  }

  @Test
  public void testEndTurnAttributesMerge() {
    for (final String startLeftValue : Arrays.asList("A", "B", "C", "D", null)) {
      for (final String startRightValue : Arrays.asList("B", "C", "D", null,
          "A")) {
        for (final String endLeftValue : Arrays.asList("C", "D", null, "A", "B")) {
          for (final String endRightValue : Arrays.asList("D", null, "A", "B",
              "C")) {
            assertEndTurnAttributesMerge(this.LINE1, startLeftValue,
              startRightValue, null, null, this.LINE2, null, null,
              endLeftValue, endRightValue, this.MERGED_LINE, startLeftValue,
              startRightValue, endLeftValue, endRightValue);
            assertEndTurnAttributesMerge(this.LINE1, startLeftValue,
              startRightValue, null, null, this.REVERSE_LINE2, endLeftValue,
              endRightValue, null, null, this.MERGED_LINE, startLeftValue,
              startRightValue, endLeftValue, endRightValue);
            assertEndTurnAttributesMerge(this.REVERSE_LINE1, null, null,
              startLeftValue, startRightValue, this.LINE2, null, null,
              endLeftValue, endRightValue, this.REVERSE_MERGED_LINE,
              endLeftValue, endRightValue, startLeftValue, startRightValue);
            assertEndTurnAttributesMerge(this.REVERSE_LINE1, null, null,
              startLeftValue, startRightValue, this.REVERSE_LINE2,
              endLeftValue, endRightValue, null, null,
              this.REVERSE_MERGED_LINE, endLeftValue, endRightValue,
              startLeftValue, startRightValue);
          }
        }
      }
    }
    final String startLeftValue = "A";
    final String startRightValue = "B";
    final String endLeftValue = "C";
    final String endRightValue = "D";

    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.LINE2, startLeftValue, null,
      endLeftValue, endRightValue);
    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.LINE2, null, startRightValue,
      endLeftValue, endRightValue);
    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.REVERSE_LINE2, endLeftValue, null,
      startLeftValue, startRightValue);
    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, null, this.REVERSE_LINE2, null, endRightValue,
      startLeftValue, startRightValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.LINE2, startLeftValue, null,
      endLeftValue, endRightValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.LINE2, null, startRightValue,
      endLeftValue, endRightValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, startLeftValue, startRightValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, null, startRightValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, null, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, startLeftValue, null);

    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, endLeftValue, null, this.LINE2, null, null,
      endLeftValue, endRightValue);
    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, endRightValue, this.LINE2, null, null,
      endLeftValue, endRightValue);
    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, endLeftValue, null, this.REVERSE_LINE2, endLeftValue,
      endRightValue, null, null);
    assertEndTurnAttributesNotCanMerge(this.LINE1, startLeftValue,
      startRightValue, null, endRightValue, this.REVERSE_LINE2, endLeftValue,
      endRightValue, null, null);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, endLeftValue,
      endRightValue, startLeftValue, startRightValue, this.LINE2, null, null,
      null, endLeftValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, endLeftValue, null,
      startLeftValue, startRightValue, this.LINE2, null, null, null,
      endLeftValue);
    assertEndTurnAttributesNotCanMerge(this.REVERSE_LINE1, endLeftValue, null,
      startLeftValue, startRightValue, this.REVERSE_LINE2, endLeftValue, null,
      null, null);

    assertEndTurnAttributesNotCanMerge(this.LINE1, null, null, null, null,
      this.LINE1, null, null, null, null);
    assertEndTurnAttributesNotCanMerge(this.LINE1, null, null, null, null,
      this.LINE3, null, null, null, null);
  }

  @Test
  public void testEndTurnAttributesReverse() {
    for (final String startLeftValue : Arrays.asList(null, "A", "B", "C", "D")) {
      for (final String startRightValue : Arrays.asList(null, "A", "B", "C",
          "D")) {
        for (final String endLeftValue : Arrays.asList(null, "A", "B", "C", "D")) {
          for (final String endRightValue : Arrays.asList(null, "A", "B", "C",
              "D")) {
            assertEndTurnAttributesReverse(startLeftValue, startRightValue,
              endLeftValue, endRightValue);
          }
        }
      }
    }
  }

  @Test
  public void testSideAttributesEqual() {
    for (final Boolean leftValue : Arrays.asList(null, false, true)) {
      for (final Boolean endValue : Arrays.asList(null, false, true)) {
        assertSideAttributesEqual(this.LINE1, leftValue, endValue, this.LINE1,
          leftValue, endValue);
        assertSideAttributesEqual(this.LINE1, leftValue, endValue,
          this.REVERSE_LINE1, endValue, leftValue);
        assertSideAttributesEqual(this.LINE2, leftValue, endValue, this.LINE2,
          leftValue, endValue);
        assertSideAttributesEqual(this.LINE2, leftValue, endValue,
          this.REVERSE_LINE2, endValue, leftValue);
      }
    }

    for (final Boolean leftValue : Arrays.asList(false, true)) {
      final Boolean endValue = !leftValue;
      assertSideAttributesNotEqual(this.LINE1, leftValue, endValue,
        this.REVERSE_LINE1, leftValue, endValue);
    }
    assertSideAttributesNotEqual(this.LINE1, null, null, this.LINE2, null, null);
  }

  @Test
  public void testSideAttributesMerge() {
    for (final Boolean leftValue : Arrays.asList(null, false, true)) {
      for (final Boolean endValue : Arrays.asList(null, false, true)) {
        assertSideAttributesMerge(this.LINE1, leftValue, endValue, this.LINE2,
          leftValue, endValue, this.MERGED_LINE, leftValue, endValue);
        assertSideAttributesMerge(this.LINE2, leftValue, endValue, this.LINE1,
          leftValue, endValue, this.MERGED_LINE, leftValue, endValue);
        assertSideAttributesMerge(this.REVERSE_LINE1, endValue, leftValue,
          this.REVERSE_LINE2, endValue, leftValue, this.REVERSE_MERGED_LINE,
          endValue, leftValue);
        assertSideAttributesMerge(this.REVERSE_LINE2, endValue, leftValue,
          this.REVERSE_LINE1, endValue, leftValue, this.REVERSE_MERGED_LINE,
          endValue, leftValue);
        assertSideAttributesMerge(this.LINE1, leftValue, endValue,
          this.REVERSE_LINE2, endValue, leftValue, this.MERGED_LINE, leftValue,
          endValue);
        assertSideAttributesMerge(this.REVERSE_LINE2, endValue, leftValue,
          this.LINE1, leftValue, endValue, this.MERGED_LINE, leftValue,
          endValue);
        assertSideAttributesMerge(this.LINE2, leftValue, endValue,
          this.REVERSE_LINE1, endValue, leftValue, this.REVERSE_MERGED_LINE,
          endValue, leftValue);
        assertSideAttributesMerge(this.REVERSE_LINE1, endValue, leftValue,
          this.LINE2, leftValue, endValue, this.REVERSE_MERGED_LINE, endValue,
          leftValue);
      }
    }
    for (final Boolean leftValue : Arrays.asList(false, true)) {
      final Boolean endValue = !leftValue;
      assertSideAttributesNotCanMerge(this.LINE1, leftValue, endValue,
        this.REVERSE_LINE2, leftValue, endValue);
      assertSideAttributesNotCanMerge(this.REVERSE_LINE2, leftValue, endValue,
        this.LINE1, leftValue, endValue);
      assertSideAttributesNotCanMerge(this.LINE2, leftValue, endValue,
        this.REVERSE_LINE1, leftValue, endValue);
      assertSideAttributesNotCanMerge(this.REVERSE_LINE1, leftValue, endValue,
        this.LINE2, leftValue, endValue);
    }
    assertSideAttributesNotCanMerge(this.LINE1, null, null, this.LINE3, null,
      null);
    assertSideAttributesNotCanMerge(this.LINE1, null, null, this.LINE1, null,
      null);
  }

  @Test
  public void testSideAttributesReverse() {
    for (final Boolean leftValue : Arrays.asList(null, false, true)) {
      for (final Boolean rightValue : Arrays.asList(null, false, true)) {
        assertSideAttributesReverse(leftValue, rightValue);
      }
    }
  }
}
