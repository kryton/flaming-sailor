package com.zilbo.flamingSailor.TE.model;

import com.scottlogic.util.SortedList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

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
public class TextPage {
    private static final Logger logger = Logger.getLogger(TextPage.class);
    private static final float LINE_HEIGHT_SPACE_BETWEEN_MLB = 2.0f;
    public static final float MINIMUM_FONT_HEIGHT = 6.0f;
    // basically distance should just be the number of digits in the page# (+1) * 2
    // to take into account when even/odd pages have numbers on outside pages.
    public static final int LEVENSHTEIN_DISTANCE = 6;
    // this is kind of low, but we have documents that switch between landscape&portrait
    private static final double template_match_percent = 0.30;

    long componentID;
    float minimumHeight;
    List<TextLine> lines;
    List<TextPiece> pagePieces;
    List<Component> components;
    List<PDLink> PDLinks;
    List<BufferedImage> images;
    int pageNum;
    Component header;
    Component footer;

    protected String text;
    Map<String, Map<Integer, Long>> fontCounts;
    double avgLeft = 0.0;
    double avgRight = 0.0;
    double avgWidth = 0.0;
    long lineCount = 0;
    double charDensity = 0.0;


    public TextPage(int pageNum, float minimumHeight) {
        this.pageNum = pageNum;
        this.minimumHeight = minimumHeight;
        this.PDLinks = new ArrayList<>();
        this.images = new ArrayList<>();
        this.fontCounts = new HashMap<>();
        header = new MultiPartBlock(-1);
        footer = new MultiPartBlock(-2);
    }

    /*
    public void constructPages(double avgSize,
                               Map<String, Map<Double, Double>> normalizedFontCounts,
                               Map<String, Double> normalizedFonts,
                               Map<Double, Double> normalizedSizes) {
        //Collections.sort(pagePieces, new TextPiece.top_comparator());
        //  constructLines();
        constructMLBS();
        constructComponents();
    }
    */

    public void constructPageComponents(double highestFreqSize,
                                        double minFontSize,
                                        double maxFontSize,
                                        Map<String, Map<Integer, Double>> normalizedFontCounts,
                                        Map<String, Double> normalizedFonts,
                                        Map<Integer, Double> normalizedSizes,
                                        double avgLeft,
                                        double avgRight,
                                        double avgWidth,
                                        double charDensity,
                                        double linesPerPage) {

        components = new ArrayList<>();


//        this.components.clear();
        MultiPartBlock currentTable = null;
        boolean first = true;
        double previousEndY = -1;

        for (TextLine l : lines) {
            l.categorizeLine(highestFreqSize, minFontSize, maxFontSize,
                    normalizedFontCounts, normalizedFonts, normalizedSizes, avgLeft, avgRight, avgWidth, charDensity, linesPerPage);

            // for regular lines we would be expecting a p() of over 40%

            double thisHeight = l.height();
            double thisDensity = l.density();
            if (l.getLineIsRegularProbability() < 0.3 && Math.round(thisHeight) >= highestFreqSize) {
                if (thisDensity >= (charDensity - 0.02) || thisHeight - highestFreqSize > 2.0) {
                    // headings usually start at the left
                    if (l.getGeom().getMinX() < (avgLeft + (avgWidth / 10))) {
                        l.setIsHeading(true);
                    }
                    // or they are in the center of the line
                    if (l.width() < avgWidth/3){
                        l.setIsHeading(true);
                    }
                    /*
                            logger.info("Heading:Y\t"+String.format("Density %+3.2f Height: %+3.2f Prob: %3.2f %s",
                                    thisDensity-charDensity,thisHeight-highestFreqSize,l.getLineIsRegularProbability(),
                                    l.getText()));
                        }    else {
                            logger.info("Heading:N\t"+String.format("Density %+3.2f Height: %+3.2f Prob: %3.2f %s",
                                    thisDensity-charDensity,thisHeight-highestFreqSize,l.getLineIsRegularProbability(),
                                    l.getText()));
                    */
                }
            }

            // gaps of 4 lines usually mean tables have ended. possible short page

            if (!first && (l.getGeom().getMinY() - previousEndY) > (4 * l.height())) {
                if (currentTable != null) {
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }

                    } else {
                        components.add(currentTable);
                    }
                    currentTable = null;
                }
            }

            previousEndY = l.getGeom().getMaxY();

            first = false;
            if (l.isHeading()) {
                if (currentTable != null) {
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }

                    } else {
                        components.add(currentTable);
                    }
                    currentTable = null;
                }
                components.add(l);
                continue;
            }
            // a regular line.
            if (l.getLineIsRegularProbability() > 0.4 && l.width() > avgWidth && l.density() > charDensity) {
                if (currentTable != null) {
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }

                    } else {
                        components.add(currentTable);
                    }
                    currentTable = null;
                }
                components.add(l);
                continue;

            }
            // big font is usually a section heading, not in a table
            double height = l.height();
            if (height > (highestFreqSize * 1.25)) {
                if (currentTable != null) {
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }
                        currentTable = null;
                    } else {
                        components.add(currentTable);
                        currentTable = null;
                    }
                }

                components.add(l);
                continue;
            }
            String text = l.getText();
            if (text.toLowerCase().startsWith("notes:") || text.toLowerCase().startsWith("note:")) {
                if (currentTable != null) {
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }
                        currentTable = null;
                    } else {
                        components.add(currentTable);
                        currentTable = null;
                    }
                }
                components.add(l);
                continue;
            }
            /*
            if (text.startsWith("Total")) {
                if (currentTable != null) {
                    currentTable.addChild(l);
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }
                        currentTable = null;
                    } else {
                        components.add(currentTable);
                        currentTable = null;
                    }
                }
                continue;
            }
            */
            // low density means lots of spaces between words.
            if (l.density() < charDensity) {
                if (currentTable == null) {
                    currentTable = new MultiPartBlock(getNextComponentID());
                }
                currentTable.addChild(l);
                continue;
            }

            // long line, regular size font, good density
            if (l.getGeom().getWidth() >= avgWidth) {
                if (currentTable != null) {
                    if (currentTable.size() < 2) {
                        for (Component c : currentTable.getChildren()) {
                            components.add(c);
                        }
                        currentTable = null;
                    } else {
                        components.add(currentTable);
                        currentTable = null;
                    }
                }
                components.add(l);
                continue;
            }

            if (l.getGeom().getMinX() > avgLeft && l.getGeom().getMaxX() < avgRight) {

                if (currentTable == null) {
                    currentTable = new MultiPartBlock(getNextComponentID());
                }
                currentTable.addChild(l);
                continue;
            }
            // just a bit on the right hand side
            if (l.getGeom().getMinX() > avgLeft) {
                if (currentTable == null) {
                    currentTable = new MultiPartBlock(getNextComponentID());
                }
                currentTable.addChild(l);
                continue;
            }
            // if we are here, we have a short line, average density, and slightly larger? fontsize.
            // so just copy what is above us
            if (currentTable != null) {
                currentTable.addChild(l);
            } else {
                components.add(l);
            }
        }
        if (currentTable != null) {
            components.add(currentTable);
            currentTable = null;
        }
        List<Component> currentComponents = new ArrayList<>();
        currentComponents.addAll(components);
        for (Component c : currentComponents) {
            if (c instanceof MultiPartBlock) {
                List<Component> children = c.getChildren();
                // prevent looking at 'list of figures / list of tables'
                if (((MultiPartBlock) c).linesStartWith("figure", false) > (0.5 * children.size())) {
                    continue;
                }
                if (((MultiPartBlock) c).linesStartWith("table", false) > (0.5 * children.size())) {
                    continue;
                }
                boolean seenTableCaption = false;

                int lastSplit = 0;
                boolean haveSplit = false;

                for (int i = 0; i < children.size(); i++) {
                    Component l = children.get(i);
                    String text = l.getText();
                    if (text.contains("Table") || text.contains("Figure") || text.contains("TABLE") || text.contains("FIGURE")) {
                        if (seenTableCaption && (i - lastSplit) > 2) {
                            MultiPartBlock mlb = new MultiPartBlock(getNextComponentID());
                            for (int j = lastSplit; j < i; j++) {
                                mlb.addChild(children.get(j));
                            }
                            lastSplit = i;
                            int index = components.indexOf(c);
                            haveSplit = true;
                            components.add(index, mlb);
                        }
                        seenTableCaption = true;
                    }
                }
                if (haveSplit) {
                    MultiPartBlock mlb = new MultiPartBlock(getNextComponentID());
                    for (int j = lastSplit; j < children.size(); j++) {
                        mlb.addChild(children.get(j));
                    }
                    if (!mlb.isEmpty()) {
                        int index = components.indexOf(c);
                        components.add(index, mlb);
                    }
                    components.remove(c);
                }
            }
        }
    }

    public void processPage(List<TextPiece> pieces, Map<String, Map<Integer, Long>> fontCounts) {

        pagePieces = new SortedList<>(new Component.topleft_comparator());
        this.fontCounts = fontCounts;
        lines = new ArrayList<>();

        this.componentID = 0;
        for (TextPiece bit : pieces) {
            componentID = Math.max(componentID, bit.getID());
        }
        getNextComponentID();

        shrinkPieces(pieces);
        // eventually we want to go back to JDK-lists.. and we will need this then
        //   Collections.sort(pagePieces);
        constructLines();

    }

    protected void calcLineStats() {
        lineCount = lines.size();

        for (TextLine l : lines) {
            avgLeft += l.getGeom().getMinX();
            avgRight += l.getGeom().getMaxX();
            charDensity += l.density();
            avgWidth += l.getGeom().getWidth();
        }
        avgLeft /= lineCount;
        avgRight /= lineCount;
        charDensity /= lineCount;
        avgWidth /= lineCount;

    }

    public void dumpPage(PrintWriter pw) {
        pw.printf("--------\n");
        pw.printf("PAGE:%d\n", pageNum);
        pw.printf("--------\n");
        for (Component c : components) {
            pw.print(c.getText());
            pw.print("\n");
        }

        pw.print('\n');

    }


    protected void shrinkPieces(List<TextPiece> pieces) {
        boolean first = true;

        TextPiece currentPiece = null;
        for (TextPiece bit : pieces) {

//            if ( currentPiece!=null &&currentPiece.getText().contains("TITLE PAGE"))     {
//                logger.info("debug");
//            }
            if (bit.getHeight() < this.minimumHeight) {
                continue;
            }
            if (first) {
                currentPiece = bit;
                first = false;
            } else {
                if (currentPiece.isNextToX(bit)) {
                    // periods like this are in a Table of Contents. we want to split out the page number from the other part of the heading.
                    if (currentPiece.isTOCPart(bit)) {
                        currentPiece.categorize();
                        pagePieces.add(currentPiece);
                        currentPiece = bit;
                    } else {
                        currentPiece.appendX(bit);
                    }
                } else {
                    currentPiece.categorize();
                    pagePieces.add(currentPiece);
                    currentPiece = bit;
                }
            }
        }
        if (!first) {
            currentPiece.categorize();
            pagePieces.add(currentPiece);
        }
    }

    protected void constructLines() {

        TextLine currentLine = null;
        for (TextPiece piece : pagePieces) {
            if (currentLine == null) {
                currentLine = new TextLine(getNextComponentID(), piece);
            } else {
                if (currentLine.onSameLine(piece)) {
                    currentLine.addChild(piece);
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine);
                    }
                    currentLine = new TextLine(getNextComponentID(), piece);
                }
            }
        }
        if (currentLine != null) {
            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }
        }
        calcLineStats();
    }


    public int getPageNum() {
        return pageNum;
    }


    public String getText() {
        int end = this.getComponents().size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < end; i++) {
            if (i != 0) {
                sb.append('\n');
            }
            sb.append(this.getComponents().get(i).getText());

        }
        return sb.toString();
    }

    public void setText(String s) {
        this.text = s;
    }


    @Override
    public String toString() {
        return "TextPage{" +
                "pageNum=" + pageNum +
                //  ",mlbs=\n\t" + multis +
                ",components=\n\t" + components +
                '}';
    }

    public List<Component> getComponents() {
        return this.components;
    }

    public void addLink(PDLink l) {
        this.PDLinks.add(l);
    }

    public void addImage(BufferedImage i) {
        this.images.add(i);
    }

    public void resolveLinks(List<TextPage> pages) {
        for (PDLink link : PDLinks) {
            List<Component> matching = findByGeom(link.geom);
            logger.info("Link from" + GeomUtil.getRectangleDebug(link.geom) + " -> " + link.pageTo + " " + GeomUtil.getPointDebug(link.to) + "-" + matching.size());

        }
    }

    public List<Component> findByGeom(Rectangle2D box) {
        List<Component> ret = new ArrayList<>();
        for (Component c : getComponents()) {
            ret.addAll(c.findByGeom(box));
        }
        return ret;
    }

    //TODO move to 'component' class
    /*
    public void splitComponent(Component old, Component p1, Component p2) {
        int index = this.components.indexOf(old);
        if (index >= 0) {
            components.remove(index);
            components.add(index, p2);
            components.add(index, p1);
        }
        if (old instanceof MultiPartBlock && p1 instanceof MultiPartBlock && p2 instanceof MultiPartBlock) {
            index = this.multis.indexOf(old);
            if (index >= 0) {
                multis.remove(index);
                multis.add(index, (MultiPartBlock) p2);
                multis.add(index, (MultiPartBlock) p1);
            }
        }
        if (old instanceof TextLine && p1 instanceof TextLine && p2 instanceof TextLine) {
            index = this.lines.indexOf(old);
            if (index >= 0) {
                lines.remove(index);
                lines.add(index, (TextLine) p2);
                lines.add(index, (TextLine) p1);
            }
        }
    }
    */

    public void dumpChildren(PrintStream out) {
        out.println("** Page:" + pageNum);
        for (Component component : getComponents()) {
            component.dumpChildren(out, 0);
        }
    }

    private String componentListToString(List<Component> parts) {
        StringBuilder p2 = new StringBuilder();
        for (Component p : parts) {
            p2.append(p.getText());
        }
        return p2.toString();
    }

    private List<Component> findByGeomByLines(Rectangle2D box) {
        List<Component> ret = new ArrayList<>();
        for (TextLine c : lines) {
            if (c.onSameLine(box)) {
                ret.add(c);
            }
        }
        return ret;
    }

    private void adjustFontTally(TextPiece tp) {
        int tpHeight = (int) Math.round(tp.getHeight());
        String fontName = tp.getFontName();
        Map<Integer, Long> fontTally = fontCounts.get(fontName);
        if (fontTally != null) {
            Long tally = fontTally.get(tpHeight);
            if (tally == null) {
                tally = 0L;
            }
            tally -= tp.getText().length();
            if (tally <= 0) {
                fontTally.remove(tpHeight);
            } else {
                fontTally.put(tpHeight, tally);
            }
            if (fontTally.isEmpty()) {
                fontCounts.remove(fontName);
            } else {
                fontCounts.put(fontName, fontTally);
            }
        }
    }

    public final static boolean BOILERPLATE_HEADER = true;
    public final static boolean BOILERPLATE_FOOTER = false;

    /**
     * Try and remove boilerplate text from the page.
     * this was designed to be called just after the pieces have been converted to lines,
     * and before higher order structures (tables/headings) have been determined.
     *
     * @param headerTemplate         potential template text  (in components)
     * @param maxLevenshteinDistance max distance to allow. (to take into account page-numbers)
     * @param headerTemplateString   the template text (in a string)
     * @param boundingBox            the bounding box of the template text
     * @param isHeader               is this at the top of the page (true) or bottom.
     * @param doUpdate               actually modify the header/footer.
     * @return true if matched the boilerplate
     */
    public boolean removeBoilerPlateComponent(Component headerTemplate, int maxLevenshteinDistance,
                                              String headerTemplateString, Rectangle2D boundingBox,
                                              boolean isHeader, boolean doUpdate) {
        List<Component> topC = this.findByGeomByLines(boundingBox);
        if (topC.size() == 0) {
            if (lines.size() < 2) {
                return false;
            }
            if (isHeader) {
                topC.add(this.getTopLine());
            } else {
                topC.add(this.getBottomLine());
            }
        }
        String topCAsString = componentListToString(topC);
        int distance = StringUtils.getLevenshteinDistance(headerTemplateString, topCAsString);

        if (distance <= maxLevenshteinDistance) {
            if (doUpdate) {
                for (Component c : topC) {
                    assert (c instanceof TextLine);
                    if (isHeader) {
                        header.addChild(c);
                    } else {
                        footer.addChildAtTop(c);
                    }
                    for (Component tpC : c.getChildren()) {
                        assert (tpC instanceof TextPiece);
                        adjustFontTally((TextPiece) tpC);
                    }

                    if (c instanceof TextLine) {

                        int index = lines.indexOf(c);
                        if (index >= 0) {
                            lines.remove(index);
                        } else {
                            logger.error("Component not found in lines?");
                        }
                    } else {
                        logger.error("BoilerPlate! need to remove other components than lines:");
                    }
                }
            }
            return true;
        } else {
            //   logger.info(headerTemplateString + "\t" + topCAsString + "\t distance:" + distance);
        }
        return false;
    }

    protected TextLine getTopLine() {
        if (lines.size() > 0) {
            return lines.get(0);
        } else {
            return null;
        }
    }

    protected TextLine getBottomLine() {
        int size = lines.size();
        if (size > 0) {
            return lines.get(size - 1);
        } else {
            return null;
        }
    }

    protected static double removeBoilerplateLine(List<TextPage> pages, TextPage templatePage, int distance,
                                                  boolean headerFooter,
                                                  Set<Integer> pageTemplatePossibles) {
        Component t;
        if (headerFooter) {
            t = templatePage.getTopLine();
        } else {
            t = templatePage.getBottomLine();
        }
        if (t == null) {
            return 0.0;
        }
        Rectangle2D boundry = t.getGeom();
        String templateText = t.getText(); // +'\n';
        int matchedTemplate = 0;
        for (int i = 0; i < pages.size(); i++) {
            TextPage p = pages.get(i);
            if (p.removeBoilerPlateComponent(t, distance, templateText, boundry, headerFooter, false)) {
                matchedTemplate++;
            } else {
                pageTemplatePossibles.add(i);
            }
        }
        double matched = 1.0 * matchedTemplate / pages.size();
        if (matched > template_match_percent) {
            for (TextPage p : pages) {
                p.removeBoilerPlateComponent(t, distance, templateText, boundry, headerFooter, true);
            }
        }
        return matched;
    }


    protected static void removeBoilerplate(List<TextPage> pages, int distance, int pageNum, Set<Integer> pageTemplatePossible) {
        //  this won't work easily on small documents, so just don't do them
        if (pages.size() < 4) {
            return;
        }
        TextPage page = pages.get(pageNum);
        int iterations = 0;
        double matched = 1.0;

        while (matched > template_match_percent && iterations++ < 10) {
            Set<Integer> matchPages;
            if (iterations == 1) {
                matchPages = pageTemplatePossible;
            } else {
                matchPages = new HashSet<>();
            }
            matched = removeBoilerplateLine(pages, page, distance, BOILERPLATE_HEADER, matchPages);
        }
        matched = 1.0;
        iterations = 0;
        while (matched > template_match_percent && iterations++ < 10) {
            Set<Integer> matchPages;
            if (iterations == 1) {
                matchPages = pageTemplatePossible;
            } else {
                matchPages = new HashSet<>();
            }
            matched = removeBoilerplateLine(pages, page, distance, BOILERPLATE_FOOTER, matchPages);
        }

    }

    public static void removeBoilerplate(List<TextPage> pages, int distance) {
        if (pages.size() < 4) {
            return;
        }
        Set<Integer> pageTemplatePossibles = new HashSet<>();
        // pick a random page to start with. ideally it will match alot.
        removeBoilerplate(pages, distance, (int) (Math.round(pages.size()) * 0.33), pageTemplatePossibles);
        if (pageTemplatePossibles.size() == (pages.size() - 1)) {
            pageTemplatePossibles = new HashSet<>();
            removeBoilerplate(pages, distance, (int) (Math.round(pages.size()) * 0.66), pageTemplatePossibles);
        }
        //TODO find a better candidate to get some more boilerplate.
        /*
        List<Integer> workThrough = new ArrayList<>();
        workThrough.addAll(pageTemplatePossibles);
        // work through all the pages that didn't match, as the first choice might not have been a good candidate.
        // 3 iterations should be sufficent
        int iterations = 3;
        while (workThrough.size() > 0 && iterations-- > 0) {
            int page = workThrough.get(0);
            workThrough.remove(0);
            pageTemplatePossibles = new HashSet<>();
            removeBoilerplate(pages, distance, page, pageTemplatePossibles);
            // clean out the list of pages that matched this one (retainAll is a set intersect)
            workThrough.retainAll(pageTemplatePossibles);
        }
        */

    }

    public Component getFooter() {
        return footer;
    }

    public Component getHeader() {
        return header;
    }

    public Map<String, Map<Integer, Long>> getFontCounts() {
        return fontCounts;
    }

    public double getAvgLeft() {
        return avgLeft;
    }

    public double getAvgWidth() {
        return avgWidth;
    }

    public double getAvgRight() {
        return avgRight;
    }

    public double getCharDensity() {
        return charDensity;
    }

    public long getLineCount() {
        return lineCount;
    }

    // used for testcases
    int getLineSize() {
        return this.lines.size();
    }

    public long getNextComponentID() {
        return componentID++;
    }

}
