package com.zilbo.flamingSailor.TE;

import com.zilbo.flamingSailor.TE.model.*;
import junit.framework.TestCase;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ianholsman
 * Date: 8/5/12
 * Time: 12:16 PM
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
public class TestSort extends TestCase {
    public void testIsNextTo() {
        TextPiece p1 = new TextPiece(1);
        p1.setGeom(451, 756, 5, 6);//new Point2D.Double(451, 756), new Point2D.Double(456, 762));
        p1.setText("th");
        //   p1.setXScale(6.5f);
        p1.setWidthOfSpace(2.64f);
        TextPiece p2 = new TextPiece(2);
        p2.setGeom(459, 758, 64, 10);//new Point2D.Double(459, 758), new Point2D.Double(523, 768));
        p2.setText("Floor, Toronto,");
        p2.setWidthOfSpace(4.0f);
        //  p2.setXScale(10.0f);
        assertFalse(p1.isNextTo(p2));
        TextPiece p3 = new TextPiece(3);
        p3.setGeom(45, 758, 406, 10);// new Point2D.Double(45, 758), new Point2D.Double(451, 768));
        p3.setText("asked to contact the Corporate Secretary by mail at 199 Bay Street, Commerce Court West, 44");
        //  p3.setXScale(10.0f);
        p3.setWidthOfSpace(4.0f);
        assertTrue(p3.isNextTo(p1));
        TextLine l1 = new TextLine(4, p3);
        l1.addChild(p1);
        l1.addChild(p2);
        assertEquals("text should be sorted correctly and spaces added", "asked to contact the Corporate Secretary by mail at 199 Bay Street, Commerce Court West, 44th Floor, Toronto,", l1.getText());
        assertEquals("Boundry of line should match components",
                GeomUtil.getRectangleDebug(new Rectangle2D.Double(45, 756, 478, 12)),//new Line2D.Double(new Point2D.Double(45, 756), new Point2D.Double(523, 768))),
                (l1.getRectangleDebug()));

    }

    /**
     * the following exhibits a 'yet unfixed' problem with Collections.sort.
     * we have worked around it by using SortedList
     *
     * @throws IOException
     */
    public void testSort() throws IOException {
        String filename = "../test/lineSortTest2.pdf";
        PDFParser parser = new PDFParser();
        List<TextPage> pages = parser.getTextPages(new File(filename), TextPage.MINIMUM_FONT_HEIGHT);
    }

    /**
     * The following tests line ordering. at the bottom of this file is a small 'th' between 44 & floor.
     *
     * @throws IOException
     */
    public void testSort2() throws IOException {
        String fileName = "../test/lineSortTest.pdf";
        PDFParser parser = new PDFParser();
        List<TextPage> pages = parser.getTextPages(new File(fileName), TextPage.MINIMUM_FONT_HEIGHT);
        assertEquals(1, pages.size());
        TextPage page1 = pages.get(0);
        for (Component c : page1.getComponents()) {
            String text = c.getText();
            if (text.contains("44th Floor")) {
                return;
            }
            if (text.contains("44 th Floor")) {
                assertTrue(false);
            }
            if (text.contains("44thFloor")) {
                assertTrue(false);
            }
        }
        assertTrue("expected '44th Floor'", page1.getText().contains("44th Floor"));
    }


}
