package com.zilbo.flamingSailor.TE.model;

import junit.framework.TestCase;

/**
 * User: ianholsman
 * Date: 16/5/12
 * Time: 8:59 PM
 * <p/>
 * <p/>
 * COPYRIGHT  (c)  2012 Zilbo.com.
 * All Rights Reserved.
 * <p/>
 * PROPRIETARY - INTERNAL Zilbo.com USE ONLY
 * This document contains proprietary information that shall be
 * distributed, routed, or made available only within Zilbo.com,
 * except with written permission of Zilbo.com.
 */
public class TestSplit extends TestCase {
    public void testSplitMiddle() {
        MultiPartBlock m = new MultiPartBlock(1);
        TextLine toBeSplit = new TextLine(4, new TextPiece(5));
        m.addChild(new TextLine(2, new TextPiece(3)));
        m.addChild(toBeSplit);
        m.addChild(new TextLine(6, new TextPiece(7)));
        MultiPartBlock newOne = m.splitComponent(99, toBeSplit);
        assertEquals("Existing one should be 1", 1, m.size());
        assertEquals("new one should be 2", 2, newOne.size());
        assertEquals("Existing Child = 2", new Long(2L), m.getChildren().get(0).getID());
        assertEquals("new one Child 0 = 4", new Long(4L), newOne.getChildren().get(0).getID());
        assertEquals("new one Child 1 = 6", new Long(6L), newOne.getChildren().get(1).getID());
    }

    public void testSplitTop() {
        MultiPartBlock m = new MultiPartBlock(1);
        TextLine toBeSplit = new TextLine(4, new TextPiece(5));
        m.addChild(toBeSplit);
        m.addChild(new TextLine(2, new TextPiece(3)));
        m.addChild(new TextLine(6, new TextPiece(7)));
        MultiPartBlock newOne = m.splitComponent(99, toBeSplit);

        assertEquals("Existing one should be 0", 0, m.size());
        assertEquals("new one should be 3", 3, newOne.size());
        assertEquals("new one child 0 = 4", new Long(4L), newOne.getChildren().get(0).getID());
        assertEquals("new one Child 1 = 2", new Long(2L), newOne.getChildren().get(1).getID());
        assertEquals("new one Child 2 = 6", new Long(6L), newOne.getChildren().get(2).getID());
    }

    public void testSplitBottom() {
        MultiPartBlock m = new MultiPartBlock(1);
        TextLine toBeSplit = new TextLine(4, new TextPiece(5));

        m.addChild(new TextLine(2, new TextPiece(3)));
        m.addChild(new TextLine(6, new TextPiece(7)));
        m.addChild(toBeSplit);
        MultiPartBlock newOne = m.splitComponent(99, toBeSplit);

        assertEquals("Existing one should be 2", 2, m.size());
        assertEquals("new one should be 1", 1, newOne.size());

        assertEquals("existing one Child 1 = 2", new Long(2L), m.getChildren().get(0).getID());
        assertEquals("existing one Child 2 = 6", new Long(6L), m.getChildren().get(1).getID());
        assertEquals("new one child 0 = 4", new Long(4L), newOne.getChildren().get(0).getID());
    }
}
