package com.zilbo.flamingSailor.TE.model;

import com.zilbo.flamingSailor.TE.PDFParser;
import junit.framework.TestCase;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: ianholsman
 * Date: 11/5/12
 * Time: 10:43 PM
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
public class TestBoundingBox extends TestCase {
    public static final String report = "../test/testBB.pdf";

    PDFParser parser;
    List<TextPage> pages;

    public TestBoundingBox() throws IOException {
        parser = new PDFParser();
        pages = parser.getTextPages(new File(report), TextPage.MINIMUM_FONT_HEIGHT);
    }

    public void testFindByArea() {
        TextPage page = pages.get(116);
        Rectangle2D bottomOfPage = new Rectangle2D.Double(107,168, 342, 317);//new Point2D.Double(107, 168), new Point2D.Double(449, 485));
        assertEquals("page should have 2 components", 2, page.getComponents().size());
        Component x = page.getComponents().get(1);
        assertTrue("The box should contain the footer of the page", x.isContainedBy(bottomOfPage));

        List<Component> parts = page.findByGeom(bottomOfPage);
        assertEquals("There should be 1 component found", 1, parts.size());
    }


    public void testBoilerPlateRemoval() {
        int testPage = 28;
        TextPage page;
        page = pages.get(testPage);

        assertEquals("Page has 36 lines ", 36, page.getLineSize());
        Component header = page.getHeader();
        assertEquals("header should have no children", 0, header.getChildren().size());
        Component footer = page.getFooter();
        assertEquals("footer should have 3", 3, footer.getChildren().size());
        assertEquals("top footer line should be: China Coal Corporation 10-4889", "China Coal Corporation 10-4889 ", footer.getChildren().get(0).getText());


    }

    public void testBoilerPlateRemoval2() throws IOException {
        String file2 = "../test/testBoilerplate.pdf";
        PDFParser parser2 = new PDFParser();
        List<TextPage> pages2 = parser2.getTextPages(new File(file2), TextPage.MINIMUM_FONT_HEIGHT);
        assertEquals("145 pages", 145, pages2.size());
        TextPage p2 = pages2.get(13);
        assertEquals("1 line header", 1, p2.getHeader().getChildren().size());
        assertEquals("2 line footer", 2, p2.getFooter().getChildren().size());
        p2 = pages2.get(80);
        assertEquals("1 line header", 1, p2.getHeader().getChildren().size());
        assertEquals("1 line footer", 1, p2.getFooter().getChildren().size());
    }

}
