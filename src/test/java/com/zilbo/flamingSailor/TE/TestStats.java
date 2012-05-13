package com.zilbo.flamingSailor.TE;

import com.zilbo.flamingSailor.TE.model.Component;
import com.zilbo.flamingSailor.TE.model.MultiPartBlock;
import com.zilbo.flamingSailor.TE.model.TextPage;
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
public class TestStats extends TestCase {
    public static final String report = "../test/testStats.pdf";
    PDFParser parser;
    List<TextPage> pages;

    public TestStats() throws IOException {
        parser = new PDFParser();
        pages = parser.getTextPages(new File(report), TextPage.MINIMUM_FONT_HEIGHT);
    }

    public void testFontStats() {

        //  System.out.println("done");
        assertTrue(!new Double(parser.getDocAvgLeft()).isNaN());
        assertTrue(!new Double(parser.getDocAvgLeft()).isInfinite());
        assertTrue(!new Double(parser.getDocAvgRight()).isNaN());
        assertTrue(!new Double(parser.getDocAvgRight()).isInfinite());
        assertTrue(!parser.getDocCharDensity().isNaN());
        assertTrue(!parser.getDocCharDensity().isInfinite());
    }

    public void testTable1() {
        assertTrue(pages.size() > 12);
        TextPage page = pages.get(11);
        page.constructPageComponents(parser.highestFreqSize,
                parser.getMinFontSize(), parser.getMaxFontSize(),
                parser.getNormalizedFontCounts(), parser.getNormalizedFonts(),
                parser.getNormalizedSizes(), parser.getDocAvgLeft(), parser.getDocAvgRight(), parser.getDocCharDensity(), parser.getLinesPerPage());
        List<Component> components = page.getComponents();
        assertEquals("Should be 2 components on this page", 2, components.size());
        assertTrue(components.get(0) instanceof MultiPartBlock);
        assertTrue(components.get(1) instanceof MultiPartBlock);

    }
}
