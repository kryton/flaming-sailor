package com.zilbo.flamingSailor.TE.model;

import com.zilbo.flamingSailor.TE.PDFParser;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ianholsman
 * Date: 18/5/12
 * Time: 8:42 PM
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
public class TestConstructLines extends TestCase {
    public void testSubScript() {
        TextPage page = new TextPage(1, TextPage.MINIMUM_FONT_HEIGHT);
        List<TextPiece> wordsOfThisPage = new ArrayList<>();
        TextPiece p1 = new TextPiece(1);
        p1.setGeom(222, 514, 393 - 222, 526 - 514);
        p1.setHeight(12f);
        p1.setText("D.R. Duncan & Associates Ltd.");
        p1.setWidthOfSpace(3.336f);
        p1.setYScale(12.0f);
        wordsOfThisPage.add(p1);
        p1 = new TextPiece(2);
        p1.setText(" ");
        p1.setYScale(12.0f);
        p1.setXScale(12.0f);
        p1.setHeight(12f);
        p1.setWidthOfSpace(3.336f);
        p1.setGeom(306, 528, 309 - 306, 540 - 528);
        wordsOfThisPage.add(p1);

        p1 = new TextPiece(4);
        p1.setText("February 17");
        p1.setYScale(10.9799f);
        p1.setXScale(10.9799f);
        p1.setHeight(12f);
        p1.setWidthOfSpace(3.052f);
        p1.setGeom(258, 541, 317 - 258, 552 - 541);
        wordsOfThisPage.add(p1);
        p1 = new TextPiece(3);
        p1.setText("th");
        p1.setYScale(7.019f);
        p1.setXScale(7.019f);
        p1.setHeight(7.0199f);
        p1.setWidthOfSpace(1.9515f);
        p1.setGeom(317, 536, 323 - 317, 543 - 536);
        wordsOfThisPage.add(p1);
        p1 = new TextPiece(5);
        p1.setText(", 2012");
        p1.setHeight(12f);
        p1.setYScale(10.9799f);
        p1.setXScale(10.9799f);
        p1.setWidthOfSpace(3.052f);
        p1.setGeom(323, 541, 354 - 323, 552 - 541);
        wordsOfThisPage.add(p1);
        Map<String, Map<Integer, Long>> fontCounts = new HashMap<>();

        page.processPage(wordsOfThisPage, fontCounts);
        List<TextLine> lines = page.getLines();
        assertEquals("There should be 2 lines", 2, lines.size());
        TextLine  line= lines.get(1);
        assertEquals("3 pieces in last line",3,line.size());

    }

    /**
     * The following tests line ordering as well. particularly subscripts like 'th' are put on the correct line
     *
     * @throws java.io.IOException
     */
    public void testSubScriptPositioning_1() throws IOException {
        String fileName = "../test/testTH_1.pdf";
        PDFParser parser = new PDFParser();
        List<TextPage> pages = parser.getTextPages(new File(fileName), TextPage.MINIMUM_FONT_HEIGHT);
        assertEquals(1, pages.size());
        TextPage page1 = pages.get(0);
        assertEquals("17 components",17,page1.getComponents().size());
        Component c = page1.getComponents().get(16);
        assertEquals("5 bits",5,c.getChildren().size());
        Component cx = c.getChildren().get(4);
        String text = cx.getText();
        assertEquals("expected 'February 17th, 2012'","February 17th, 2012", text);
    }
}
