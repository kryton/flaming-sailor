package com.zilbo.flamingSailor.TE;

import com.zilbo.flamingSailor.TE.model.PDLink;
import com.zilbo.flamingSailor.TE.model.TextPage;
import com.zilbo.flamingSailor.TE.model.TextPiece;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class PDFParser extends PDFTextStripper {
    private static final Logger logger = Logger.getLogger(PDFParser.class);
    private List<TextPage> textPageList;
    private int m_currentPageNo = 0;
    private float minHeight;
    TextPage currentPage;
    PDDocument document;

    Map<String, Map<Integer, Double>> normalizedFontCounts;
    Map<Integer, Double> normalizedSizes;
    Map<String, Double> normalizedFonts;

    Integer highestFreqSize;
    PDDocumentCatalog catalog;
    List allpages;
    StringWriter outString;
    private String fileName;   // for debugging purposes;
    double docAvgLeft = 0.0;
    double docAvgRight = 0.0;
    long docLineCount = 0;
    Double docCharDensity = 0.0;
    double linesPerPage = 0.0;

    /**
     * Constructor
     *
     * @throws java.io.IOException
     */
    public PDFParser() throws IOException {
        super();
    }

    /**
     * get a TextPage out of the PDF, ignoring characters smaller than minHeight.
     *
     * @param pdfFile   the File to extract it out of
     * @param minHeight minimum height to ignore
     * @return a Page
     */

    public List<TextPage> getTextPages(File pdfFile, float minHeight) {
        fileName = pdfFile.getName();
        outString = new StringWriter();
        this.minHeight = minHeight;
        this.textPageList = new ArrayList<>();
        Map<String, Map<Integer, Long>> fontCounts = new HashMap<>();
        document = null;
        try {
            document = PDDocument.load(pdfFile);
            catalog = document.getDocumentCatalog();
            allpages = catalog.getAllPages();

            this.writeText(document, outString);
            outString.close();
            outString = null;
            // document.close();
        } catch (IOException e) {
            logger.error("I/O Error:" + pdfFile.getName(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                    document = null;
                } catch (IOException e) {
                    logger.error("I/O error closing file:" + pdfFile.getName(), e);
                }
            }
        }

        // the page is currently a set of lines with text pieces.
        // next steps
        // 1. remove header/footer boilerplate
        // 2. get font stats
        // 3. construct higher order components
        //
        TextPage.removeBoilerplate(textPageList, TextPage.LEVENSHTEIN_DISTANCE);

        for (TextPage page : textPageList) {
            double avgLeft = page.getAvgLeft();
            double avgRight = page.getAvgRight();
            long lineCount = page.getLineCount();
            Double charDensity = page.getCharDensity();

            if (lineCount > 0) {
                docAvgLeft += avgLeft * lineCount;
                docAvgRight += avgRight * lineCount;
                docCharDensity += charDensity * lineCount;
                docLineCount += lineCount;
            }
            Map<String, Map<Integer, Long>> pageFontCounts = page.getFontCounts();
            for (Map.Entry<String, Map<Integer, Long>> e : pageFontCounts.entrySet()) {
                Map<Integer, Long> fontTally = fontCounts.get(e.getKey());
                if (fontTally == null) {
                    fontTally = new HashMap<>();

                }
                for (Map.Entry<Integer, Long> pageFontTally : e.getValue().entrySet()) {
                    Long tally = fontTally.get(pageFontTally.getKey());
                    if (tally == null) {
                        fontTally.put(pageFontTally.getKey(), pageFontTally.getValue());
                    } else {
                        fontTally.put(pageFontTally.getKey(), tally + pageFontTally.getValue());
                    }
                }
                fontCounts.put(e.getKey(), fontTally);
            }
        }

        docAvgLeft /= docLineCount;
        docAvgRight /= docLineCount;
        docCharDensity /= docLineCount;
        linesPerPage = docLineCount / textPageList.size();
        normalizeFontCounts(fontCounts);


        for (TextPage page : textPageList) {
            page.constructPageComponents(highestFreqSize,
                    this.minFontSize, this.maxFontSize,
                    normalizedFontCounts, normalizedFonts, normalizedSizes,
                    docAvgLeft, docAvgRight, docCharDensity, linesPerPage);
        }

        return textPageList;
    }

    int minFontSize = 99999;
    int maxFontSize = 0;

    public int getMinFontSize() {
        return minFontSize;
    }

    public int getMaxFontSize() {
        return maxFontSize;
    }

    protected void normalizeFontCounts(Map<String, Map<Integer, Long>> fontCounts) {
        this.normalizedFontCounts = new HashMap<>();
        this.normalizedSizes = new HashMap<>();
        this.normalizedFonts = new HashMap<>();
        double total = 0.0;
        long maxFreq = 0;
        highestFreqSize = 0;
        minFontSize = 999999;
        maxFontSize = 0;
        // double sum = 0.0;
        for (Map.Entry<String, Map<Integer, Long>> e : fontCounts.entrySet()) {
            // unknown fonts are usually used in diagrams/or other wierd things. so ignore em
            if (e.getKey().equals("UNKNOWN")) {
                continue;
            }
            Map<Integer, Long> sizeCount = e.getValue();
            for (Map.Entry<Integer, Long> l : sizeCount.entrySet()) {
                total += l.getValue();
                if (l.getValue() > maxFreq) {
                    maxFreq = l.getValue();
                    highestFreqSize = l.getKey();
                }

                maxFontSize = Math.max(maxFontSize, l.getKey());
                minFontSize = Math.min(minFontSize, l.getKey());

                // sum += l.getKey() * l.getValue();
            }
        }
        //  highestFreqSize = sum / total;
        for (Map.Entry<String, Map<Integer, Long>> e : fontCounts.entrySet()) {
            if (e.getKey().equals("UNKNOWN")) {
                continue;
            }
            Map<Integer, Double> nE = new HashMap<>();
            Map<Integer, Long> sizeCount = e.getValue();
            Double fontP = 0.0;
            for (Map.Entry<Integer, Long> i : sizeCount.entrySet()) {
                Double normalized = 1.0 * i.getValue() / total;
                fontP += i.getValue();
                nE.put(i.getKey(), normalized);
                Double sizeScore = normalizedSizes.get(i.getKey());
                if (sizeScore == null) {
                    sizeScore = normalized;
                } else {
                    sizeScore += normalized;
                }
                normalizedSizes.put(i.getKey(), sizeScore);
            }
            normalizedFonts.put(e.getKey(), fontP / total);
            normalizedFontCounts.put(e.getKey(), nE);
        }
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        m_currentPageNo++;
        currentPage = new TextPage(m_currentPageNo, this.minHeight);
    }

    @Override
    protected void endPage(PDPage page) throws IOException {
        super.endPage(page);
        int pieceID = 0;
        Map<String, Map<Integer, Long>> fontCounts = new HashMap<>();
        List<TextPiece> wordsOfThisPage = new ArrayList<>();

        for (List<TextPosition> aCharactersByArticle : charactersByArticle) {
            //   int len = aCharactersByArticle.size();
            for (TextPosition t : aCharactersByArticle) {
                // copy information
                TextPiece w = new TextPiece(pieceID++);
                PDFont font = t.getFont();
                PDFontDescriptor fontDescriptor = font.getFontDescriptor();

                w.setFontDescriptor(fontDescriptor);
                if (fontDescriptor == null) {
                    w.setFontName("UNKNOWN");
                } else {
                    w.setFontName(fontDescriptor.getFontName());
                }

                /*
                * 100: a simple step to fix the font size to the normal range, for those documents in unknown codes that PDFBox can not process now
                */
                if (t.getFontSize() < 0.3 && t.getYScale() <= 1.0) {
                    w.setFontSize(t.getFontSize() * 100);
                    w.setHeight(Math.max(t.getYScale(), t.getFontSize()) * 100);
                    w.setXScale(t.getXScale());
                    w.setYScale(t.getYScale());
                } else {
                    if (t.getYScale() < 0.3 && t.getFontSize() <= 1.0) {
                        w.setYScale(t.getYScale() * 100);
                        w.setXScale(t.getXScale() * 100);
                        w.setHeight(Math.max(t.getYScale() * 100, t.getFontSize()));
                    } else {
                        w.setFontSize(t.getFontSize());
                        w.setHeight(Math.max(t.getYScale(), t.getFontSize()));
                        w.setXScale(t.getXScale());
                        w.setYScale(t.getYScale());
                    }
                }

                Map<Integer, Long> counts = fontCounts.get(w.getFontName());
                if (counts == null) {
                    counts = new HashMap<>();
                    fontCounts.put(w.getFontName(), counts);
                }
                Long count = counts.get((int) Math.round(w.getHeight()));
                if (count == null) {
                    count = 1L;
                } else {
                    count += 1L;
                }
                counts.put((int) Math.round(w.getHeight()), count);

                w.setWidth(Math.abs(t.getWidth()));
                w.setGeom(t.getX(), t.getY(), w.getWidth(), w.getHeight());

                w.setText(t.getCharacter());

                w.setWidthOfSpace(t.getWidthOfSpace());
                wordsOfThisPage.add(w);
            }
        }
        currentPage.processPage(wordsOfThisPage, fontCounts);
        currentPage.setText(outString.getBuffer().toString());
        outString.getBuffer().setLength(0);
        List<PDAnnotation> annotations = page.getAnnotations();

        for (PDAnnotation annotation : annotations) {
            if (annotation instanceof PDAnnotationLink) {
                PDAnnotationLink l = (PDAnnotationLink) annotation;
                PDRectangle rect = l.getRectangle();
                PDDestination dest = l.getDestination();
                if (dest instanceof PDPageXYZDestination) {
                    PDPageXYZDestination xyzDestination = (PDPageXYZDestination) dest;
                    PDPage pageDest = ((PDPageXYZDestination) dest).getPage();

                    if (rect != null) {
                        if (xyzDestination.getPageNumber() < 0) {
                            int pageNumber = allpages.indexOf(pageDest) + 1;
                            Rectangle2D hotbox = new Rectangle2D.Double(rect.getLowerLeftX(), rect.getLowerLeftY(),
                                    (rect.getUpperRightX() - rect.getLowerLeftX()), (rect.getUpperRightY() - rect.getLowerLeftY()));
                            Point2D toPoint = new Point2D.Double(xyzDestination.getLeft(), xyzDestination.getTop());
                            currentPage.addLink(new PDLink(hotbox, pageNumber, toPoint));
                        }
                    }
                }
            }
        }

        /*
         The following code is REALLY raw.
         initial testing seemed to show memory leaks, and was REALLY slow.

        PDResources r = page.getResources();
        Map<String, PDXObjectImage> images = r.getImages();
        for (Map.Entry<String, PDXObjectImage> e : images.entrySet()) {
            BufferedImage bi = null;
            try {

                //   currentPage.addImage(bi);

                //    (e.getValue()).write2file("/tmp/II" + e.getKey());
                if (e.getValue() instanceof PDJpeg) {
                    PDJpeg jpg = (PDJpeg) e.getValue();
                    bi = jpg.getRGBImage();
                    ColorSpace cs = bi.getColorModel().getColorSpace();
                    File jpgFile = new File("/tmp/II" + e.getKey() + ".jpg");

                    if (cs instanceof ColorSpaceCMYK) {

                        logger.info("Ignoring image with CMYK color space");
                    } else {
                       // ImageIO.write(bi, "jpg", jpgFile);
                        jpg.write2file("/tmp/II"+ e.getKey());
                    }

                } else {
                    (e.getValue()).write2file("/tmp/II" + e.getKey());
                }
            } catch (Exception ee) {
                logger.info("can't read image ;-(", ee);
            }

        }
        */

        textPageList.add(currentPage);
        currentPage = null;
    }

    public static BufferedImage makeCompatible(BufferedImage img) throws IOException {
        // Allocate the new image
        BufferedImage dstImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        // Check if the ColorSpace is RGB and the TransferType is BYTE.
        // Otherwise this fast method does not work as expected
        ColorModel cm = img.getColorModel();
        if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB && img.getRaster().getTransferType() == DataBuffer.TYPE_BYTE) {
            //Allocate arrays
            int len = img.getWidth() * img.getHeight();
            byte[] src = new byte[len * 3];
            int[] dst = new int[len];

            // Read the src image data into the array
            img.getRaster().getDataElements(0, 0, img.getWidth(), img.getHeight(), src);

            // Convert to INT_RGB
            int j = 0;
            for (int i = 0; i < len; i++) {
                dst[i] = (((int) src[j++] & 0xFF) << 16) |
                        (((int) src[j++] & 0xFF) << 8) |
                        (((int) src[j++] & 0xFF));
            }

            // Set the dst image data
            dstImage.getRaster().setDataElements(0, 0, img.getWidth(), img.getHeight(), dst);

            return dstImage;
        }

        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(img, dstImage);

        return dstImage;
    }

    public double getDocAvgLeft() {
        return docAvgLeft;
    }

    public double getDocAvgRight() {
        return docAvgRight;
    }

    public long getDocLineCount() {
        return docLineCount;
    }

    public Double getDocCharDensity() {
        return docCharDensity;
    }

    public double getLinesPerPage() {
        return linesPerPage;
    }

    public Map<String, Map<Integer, Double>> getNormalizedFontCounts() {
        return normalizedFontCounts;
    }

    public Map<Integer, Double> getNormalizedSizes() {
        return normalizedSizes;
    }

    public Map<String, Double> getNormalizedFonts() {
        return normalizedFonts;
    }
}
