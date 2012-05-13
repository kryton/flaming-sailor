package com.zilbo.flamingSailor.TE.model;

import org.apache.commons.lang3.StringUtils;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
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
public class TextLine extends Component {

    public TextLine(long id, Component tp) {
        super(id);
        addChild(tp);
    }


    public int size() {
        return pieces.size();
    }


    @Override
    public String toString() {
        return this.getText();

    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        TextPiece prev = null;
        for (Component component : this.getChildren()) {
            if (component instanceof TextPiece) {
                TextPiece p = (TextPiece) component;
                if (prev != null) {
                    if (!prev.isNextTo(p)) {
                        sb.append(' ');
                    }
                }
                sb.append(p.getText());
                prev = p;
            }
        }
        return sb.toString();
    }

    @Override
    public double density() {
        if (width() == 0) {
            return 1.0;
        }
        String text=this.getText();
        text = text.replaceAll("[^\\w]","");
        return (1.0) * (text.length()) / this.width();
    }

    @Override
    public double height() {
        double height = 0.0;
        double length = 0.0;
        for (Component c : getChildren()) {
            height += c.height() * c.getText().length();
            length += c.getText().length();
        }
        if (length == 0.0) {
            return super.height();
        }
        return height / length;
    }

    boolean isHeading = false;

    public boolean isHeading() {
        return this.isHeading;
    }
    public void setIsHeading(boolean flag) {
        isHeading = flag;
    }

    double lineIsRegularProbability = 0.0;

    protected void calcLineHeightProbability(
            double highestFreqSize,
            double minFontSize,
            double maxFontSize,
            Map<String, Map<Integer, Double>> normalizedFontCounts,
            Map<String, Double> normalizedFonts,
            Map<Integer, Double> normalizedSizes,
            double textLength) {
        if (textLength == 0) {
            lineIsRegularProbability= 0;
            return;
        }
        Double probability = 0.0;
        for (Component c : getChildren()) {
            if (c instanceof TextPiece) {
                TextPiece tp = (TextPiece) c;
                /*
                Map<Double, Double> fontSizes = normalizedFontCounts.get(tp.getFontName());
                if (fontSizes != null) {
                    Double TPprob = fontSizes.get(tp.getHeight());
                    if (TPprob != null) {
                        probability += TPprob * tp.getText().length();
                    }
                }
                */
                Double TPprob = normalizedFonts.get(tp.getFontName());
                Double sizeProb = normalizedSizes.get((int)Math.round(tp.getHeight()));
                if ( sizeProb!=null) {
                    if (TPprob!=null) {
                        TPprob *= sizeProb;
                    } else {
                        TPprob=sizeProb;
                    }
                }
                if (TPprob != null) {
                    probability += TPprob * tp.getText().length();
                }
            }
        }
        probability /= textLength;
        double heightDiff = 1 - Math.abs((this.height() - highestFreqSize) / (maxFontSize - minFontSize));
        lineIsRegularProbability = probability * heightDiff;
    }

    public void categorizeLine(double highestFreqSize,
                               double minFontSize,
                               double maxFontSize,
                               Map<String, Map<Integer, Double>> normalizedFontCounts,
                               Map<String, Double> normalizedFonts,
                               Map<Integer, Double> normalizedSizes,
                               double avgLeft,
                               double avgRight,
                               double charDensity,
                               double linesPerPage) {
        String text = getText();

        calcLineHeightProbability(
                highestFreqSize,
                minFontSize, maxFontSize,
                normalizedFontCounts,
                normalizedFonts,
                normalizedSizes,
                text.length());


        if (text.toUpperCase().equals(text)) {
            lineIsRegularProbability *= 0.90;
        }

        if (!text.matches(".*[A-Za-z].*")) {
            // headings need some text in there.
            lineIsRegularProbability *= 0.8;
        }

        // for regular lines we would be expecting a p() of over 40%
        /*
        double thisHeight = this.height();
        if (lineIsRegularProbability < 0.3 && Math.round(thisHeight) >= highestFreqSize) {
            isHeading = true;
        }
        */
    }

    public double getLineIsRegularProbability() {
        return lineIsRegularProbability;
    }

    @Override
    public void dumpChildren(PrintStream out, int level) {

        StringBuilder sb = new StringBuilder();

        sb.append(StringUtils.repeat("..", level));
        sb.append(getClass().getSimpleName());
        if (isHeading()) {
            sb.append("      (H) ");
        }
        if (sb.length() < 20) {
            sb.append(StringUtils.repeat(' ', 20 - sb.length()));
        }
        sb.append(' ');

        sb.append(getRectangleDebug()).append(" ");


        sb.append(getText().replace("\n", "\n" + StringUtils.repeat(' ', 43)));

        String text;
        if (sb.length() > 256) {
            text = sb.substring(0, 256 - 4) + " ...";
        } else {
            text = sb.toString();
        }
        out.println(text);
        for (Component component : getChildren()) {
            component.dumpChildren(out, level + 1);
        }
    }
}
