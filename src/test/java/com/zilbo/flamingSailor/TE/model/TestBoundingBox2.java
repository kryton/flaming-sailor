package com.zilbo.flamingSailor.TE.model;

import com.zilbo.flamingSailor.TE.PDFParser;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

/*
 * Copyright 2012 Zilbo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class TestBoundingBox2 extends TestCase {
    public static final String report = "../test/testBB2.pdf";

    PDFParser parser;
    List<TextPage> pages;
    String dir;

    public TestBoundingBox2() throws IOException {
        parser = new PDFParser();
        pages = parser.getTextPages(new File(report), TextPage.MINIMUM_FONT_HEIGHT);
    }

    public void testBoilerPlateRemoval() {
        int testPage = 28;
        TextPage page;
        page = pages.get(testPage);

        assertEquals("Page has 41 lines ", 41, page.getLineSize());
        Component header = page.getHeader();
        assertEquals("header should have no children", 0, header.getChildren().size());
        Component footer = page.getFooter();
        assertEquals("footer should have 1", 1, footer.getChildren().size());
        assertEquals("footer line should be:", "Protore Geological Services  26   Technical Report Reef Property, December, 2011      Precipitate Gold Inc.  ", footer.getChildren().get(0).getText());
    }

    public void testTable() {
        int testPage = 33;
        TextPage page = pages.get(testPage);
        Component tableC = null;
        for (Component c : page.getComponents()) {
            if (c instanceof MultiPartBlock) {
                tableC = c;
                break;
            }
        }

        assertNotNull("should have found a MLB", tableC);
        assertTrue("Should start with: Permitting and First", tableC.getText().startsWith("Permitting and First"));
        assertTrue("Should end with: 1 500 000", tableC.getText().trim().endsWith("1 500 000"));
        testPage = 34;
        page = pages.get(testPage);
        tableC = null;
        for (Component c : page.getComponents()) {
            if (c instanceof MultiPartBlock) {
                tableC = c;
                break;
            }
        }
        assertNotNull("should have found a MLB", tableC);
        assertTrue("Should start with: Permitting and First", tableC.getText().startsWith("Permitting and First"));
        assertTrue("Should end with: 1 500 000", tableC.getText().trim().endsWith("1 200 000"));
    }

}
