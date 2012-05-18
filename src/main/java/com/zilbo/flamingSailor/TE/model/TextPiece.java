package com.zilbo.flamingSailor.TE.model;

import com.zilbo.flamingSailor.TE.model.TextType.TextType;
import com.zilbo.flamingSailor.TE.model.TextType.Unknown;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
public final class TextPiece extends Component {
    private static final Logger logger = Logger.getLogger(TextPiece.class);


    TextType type;
    double m_xScale;                  //the X-scale of the text piece
    double m_yScale;                  //the Y-scale of the text piece

   // boolean m_superScriptBeginning = false;      //whether this text piece begins with a superscript
    //   boolean m_sparseLine = true;                 //whether this text piece is a sparse line or not
    // boolean m_onlyPiece_PhysicalLine = true;     //whether this text piece is the only piece in the physical line in document. We need this attribute to label table caption lines....

    double m_width;                               //the width of this text piece
    double m_height;                              //the height of this text piece
    double m_widthOfSpace;                        //the width space in this text piece
    double m_wordSpacing;                         //the space between words in this text piece
    double m_fontSize;                            //the font size of this text piece

    String m_fontName;                           //the font name of this text piece.
    //PDFontDescriptor fontDescriptor;
    String m_text;                               //the text content of this text piece
    //  boolean hadZeroWidth;


    public TextPiece(long id) {
        super(id);

        m_text = "";
        //      hadZeroWidth = false;
        this.type = new Unknown();
    }

    /*
    public PDFontDescriptor getFontDescriptor() {
        return fontDescriptor;
    }
    */
     /*
    public void setFontDescriptor(PDFontDescriptor fontDescriptor) {
        this.fontDescriptor = fontDescriptor;
    }
    */


    /**
     * Gets the font name of a text piece
     *
     * @return the font name
     */
    public String getFontName() {
        return m_fontName;
    }

    /**
     * Gets the font size of a text piece
     *
     * @return the font size
     */
    public double getFontSize() {
        return m_fontSize;
    }

    @Override
    /**
     * Gets the content of a text piece
     *
     * @return the text itself
     */
    public String getText() {
        return m_text;
    }

    /**
     * Gets the width of a text piece
     *
     * @return the width of a text piece
     */
    public double getWidth() {
        return m_width;
    }

    /**
     * Gets the height of a text piece
     *
     * @return the height of a text piece
     */
    public double getHeight() {
        return m_height;
    }

    /**
     * Gets the space width between characters in a text piece
     *
     * @return the space width
     */
    public double getWidthOfSpace() {
        return m_widthOfSpace;
    }

    /**
     * Gets the space size between words
     *
     * @return the space size between words
     */
    public double getWordSpacing() {
        return m_wordSpacing;
    }


    /**
     * Gets the X scale of a text piece
     *
     * @return the X-Scale
     */
    public double getXScale() {
        return m_xScale;
    }


    /**
     * Gets the Y scale of a text piece
     *
     * @return the Y-Scale
     */
    public double getYScale() {
        return m_yScale;
    }

    /**
     * Judges whether the first character of a text piece starting is a superscript
     *
     * @return the superScriptBeginning
     */
    /*
    public boolean isSuperScriptBeginning() {
        return m_superScriptBeginning;
    }
    */

    /**
     * Judges whether a text line is a sparse line or not
     *
     * @return whether it is a sparse line or not
     */
    /*
    public boolean isSparseLine() {
        return m_sparseLine;
    }
    */


    /**
     * Sets the font name of a text piece
     *
     * @param fontName the font name to be set
     */
    public void setFontName(String fontName) {
        m_fontName = fontName;
    }

    /**
     * Sets the font size of a text piece
     *
     * @param fontSize the font size to set
     */
    public void setFontSize(float fontSize) {
        m_fontSize = fontSize;
    }

    /**
     * Sets a text piece starting with a superscript
     *
     * @param superScriptBeginning the superScriptBeginning to set
     */
    /*
    public void setSuperScriptBeginning(boolean superScriptBeginning) {
        m_superScriptBeginning = superScriptBeginning;
    }
    */

    /**
     * Sets a text piece as a sparse line
     *
     * @param sparseLine a boolean value (whether it is a sparse line or not)
     */
    /*
    public void setSparseLine(boolean sparseLine) {
        m_sparseLine = sparseLine;
    }

*/

    /**
     * Sets the text content of a text piece
     *
     * @param text the text to set
     */
    public void setText(String text) {
        //m_text = new String(ModifiedASCIIFoldingFilter.foldToASCII(text.toCharArray(), text.length()));
    //    m_text = text;
        // the following is to deal with non-breaking spaces (Char(160)).
       m_text= text.replace( (char)( 160),' ');

    }

    /**
     * Sets the width of a text piece
     *
     * @param width the width to set
     */
    public void setWidth(float width) {
        m_width = width;
    }

    /**
     * Sets the height of a text piece
     *
     * @param height the height to set
     */
    public void setHeight(float height) {
        m_height = height;
    }

    /**
     * Sets the width of the space between characters in a text piece
     *
     * @param widthOfSpace the widthOfSpace to set
     */
    public void setWidthOfSpace(float widthOfSpace) {
        m_widthOfSpace = widthOfSpace;
    }

    /**
     * Sets the space between words
     *
     * @param wordSpacing the wordSpacing to set
     */
    public void setWordSpacing(float wordSpacing) {
        m_wordSpacing = wordSpacing;
    }


    /**
     * Sets the XScale of a text piece
     *
     * @param scale the xScale of a text piece to set
     */
    public void setXScale(float scale) {
        m_xScale = scale;
    }


    /**
     * Sets the YScale of a text piece
     *
     * @param scale the yScale to set
     */
    public void setYScale(float scale) {
        m_yScale = scale;
    }

    /**
     * For some PDF files, PDFBOX extracted the texts as HTML codes.
     * We have to detect such files and convert the HTML numbers back to real text for later procession
     *
     * @param text the string to be checked
     * @return the boolean result after checking
     */
    /*
    public boolean isHTMLCode(String text) {
        return text.startsWith("c");

    }
    */


    /**
     * Collects all the information of a text piece and stores them in a string. The string will be printed into a middle-result file locally for testing and debugging purpose
     *
     * @return the generated information string for the text piece
     */
    @Override
    public String toString() {
        String format = "Type=[%s] Text=[%s] ";
        return getRectangleDebug() + " " + String.format(
                format,
                this.getType(),
                this.getText());
    }


    /**
     * Is this other piece of text next to (left-right not top-down) the current piece of text
     *
     * @param t the other piece
     * @return true if it is
     */
    public boolean isNextTo(TextPiece t) {
        if (!this.onSameLine(t)) {
            return false;
        }
        // note sometimes two characters have same 'X' position and a width of zero ;-(

        // double dist = Math.abs(this.geom.getMinX() - t.geom.getMinX());   /* this one really shouldn't occur */
        double dist2 = Math.abs(this.getGeom().getMaxX() - t.getGeom().getMinX());
        // sometimes getWidthOfSpace is zero. in this case we explicitly test for it
        double distAllowed = Math.max(1.0,this.getWidthOfSpace()-0.15);
        if (dist2 < (distAllowed)) {
            return true;
        }

        return false;

    }

    /**
     * Is this other piece of text next to (left-right not top-down) the current piece of text and the same font
     *
     * @param t the other piece
     * @return true if it is
     */
    public boolean isNextToX(TextPiece t) {
        // note sometimes two characters have same 'X' position and a width of zero ;-(
        return (this.isNextTo(t) &&
                (t.getFontSize() == this.getFontSize()) &&
                (t.getXScale() == this.getXScale()));

    }

    /**
     * append the passed 'textpiece' to the current one
     *
     * @param bit the piece to append
     */
    public void appendX(TextPiece bit) {
        if (Math.abs(bit.geom.getMinX() - this.geom.getMaxX()) > this.getXScale() * 0.2) {
            this.setText(this.getText() + " " + bit.getText());
        } else {
            this.setText(this.getText() + bit.getText());
        }
        //    this.geom = new Rectangle2D.Double(geom.getP1(), new Point2D.Double(bit.geom.getX2(), geom.getY2()));
        geom = geom.createUnion(bit.getGeom());
        // this.setEndX(bit.getEndX());
        /*
        if (bit.getWidth() == 0) {
            this.hadZeroWidth = true;
        }
        */
    }

    public void setGeom(double x, double y, double w, double h) {
        geom = new Rectangle2D.Double(x, y, w, h);
    }
    /*
    public void setGeom(Point2D p1, Point2D p2) {
        geom = new Rectangle2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    */

    /*
        public boolean hadZeroWidth() {
            return hadZeroWidth;
        }
    */
    public Long getID() {
        return id;
    }

    public TextType getType() {
        return this.type;
    }

    @Override
    public List<Component> getChildren() {
        return new ArrayList<>();
    }

    public String getCategorizedText() {
        return type.getCategorizedText();
    }
    public TextType getCategory() {
        return type;
    }
    public void setCategory(TextType typer) {
        type = typer;
    }

    public void categorize() {
        type = TextType.categorize(this);
    }

    public boolean isTOCPart(TextPiece bit) {
        return (m_text.endsWith(". . ") || (m_text.endsWith(". .") || (m_text.endsWith("... ")) || (m_text.endsWith("....")))
                && !(bit.getText().equals(".") || bit.getText().equals(" ")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextPiece textPiece = (TextPiece) o;

        if (!geom.equals(textPiece.geom)) return false;
        if (!m_text.equals(textPiece.m_text)) return false;
        //    if (!type.equals(textPiece.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = geom.hashCode();
        //     result = 31 * result + type.hashCode();
        result = 31 * result + m_text.hashCode();
        return result;
    }

    public boolean isEmpty() {
        if ( getType().isUnknown()) {
            categorize();
        }
        return getType().isEmpty() ;
    }

    @Override
    public double density() {
        assert false;
        return -1;
    }
}
