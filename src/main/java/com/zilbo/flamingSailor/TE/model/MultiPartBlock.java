package com.zilbo.flamingSailor.TE.model;

import org.apache.log4j.Logger;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
public class MultiPartBlock extends Component {
    private static final Logger logger = Logger.getLogger(MultiPartBlock.class);

    public MultiPartBlock(long id) {
        super(id);
    }


    /**
     * we are not too interested in notes or lists of things. they aren't tables
     * <p/>
     * this should match something like
     * 1. lorem ipsum
     * lorem ipsum
     * 2. lorem ipsum
     *
     * @return true if it is a note format.
     */
    public boolean isaNote() {
        for (Component l : this.getChildren()) {
            if (l.size() > 2) {
                return false;
            }
            if (l.size() == 2) {
                Component p1 = l.getChildren().get(0);
                String st = p1.getText().trim();
                if (st.length() > 1) {
                    Pattern numPattern = Pattern.compile("^[A-Za-z\\d]+[.]?$");
                    Pattern numPattern2 = Pattern.compile("^\\([A-Za-z\\d]+\\)?$");
                    if (!numPattern.matcher(st).matches()) {
                        if (!numPattern2.matcher(st).matches()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * we sometimes see the following type of paragraph
     * <B>startwords</B> lorem ipsum ...
     * lorem ipsum
     * <p/>
     * for now we just detect 2 Pieces + rest 1 pieces
     * and
     * lorem ipsum
     * <b> startwords</b> lorem ipsum
     * lorem ipsum
     *
     * @return true if it matches
     */
    public boolean isParagraphWithBoldStart() {
        int len = this.size();
        int j = 0;
        if (len > 2) {
            Component p1 = this.getChildren().get(j);
            if (p1.size() != 2) {
                if (p1.size() == 1) {
                    j++;
                    p1 = this.getChildren().get(j);
                    if (p1.size() != 2) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            for (int i = j + 1; i < len; i++) {
                Component pX = this.getChildren().get(i);
                if (pX.size() != 1) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * false start on a table.
     * 2 lines
     * line 1: 1 piece
     * line 2: multiple pieces
     *
     * @return true if it matches
     */
    public boolean is2Lines() {
        if (this.getChildren().size() == 2) {
            if (this.getChildren().get(0).size() == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidTable() {
        return !is2Lines() && !isaNote() && !isParagraphWithBoldStart() && this.getChildren().size() != 1;
    }


    public List<Component> textBelow(Component toMatch, int lineNum) {
        Rectangle2D thisSpot = toMatch.getGeom();
        List<Component> span = new ArrayList<>();
        int i = lineNum + 1;
        while (i < this.getChildren().size()) {
            Component line = this.getChildren().get(i);
            for (Component tp : line.getChildren()) {
                Rectangle2D potG = tp.getGeom();
                if (thisSpot.getMinX() <= potG.getMinX() && (potG.getMinX() <= thisSpot.getMaxX())) {
                    span.add(tp);
                } else {
                    if (thisSpot.getMinX() <= potG.getMaxX() && (potG.getMaxX() <= thisSpot.getMaxX())) {
                        span.add(tp);
                    } else {
                        if (potG.getMinX() <= thisSpot.getMinX() && (thisSpot.getMinX() <= potG.getMaxX())) {
                            span.add(tp);
                        } else {
                            if (potG.getMinX() <= thisSpot.getMaxX() && (potG.getMinX() <= thisSpot.getMaxX())) {
                                span.add(tp);
                            }
                        }
                    }
                }
            }
            if (span.size() > 0) {
                return span;
            }
            i++;
        }
        return span;
    }

    public List<Component> textAbove(Component toMatch, int lineNum) {
        Rectangle2D thisSpot = toMatch.getGeom();
        List<Component> span = new ArrayList<>();
        int i = lineNum - 1;
        while (i > 0) {
            Component line = this.getChildren().get(i);
            for (Component tp : line.getChildren()) {
                Rectangle2D potG = tp.getGeom();
                if (thisSpot.getMinX() <= potG.getMinX() && (potG.getMinX() <= thisSpot.getMaxX())) {
                    span.add(tp);
                } else {
                    if (thisSpot.getMinX() <= potG.getMaxX() && (potG.getMaxX() <= thisSpot.getMaxX())) {
                        span.add(tp);
                    } else {
                        if (potG.getMinX() <= thisSpot.getMinX() && (thisSpot.getMinX() <= potG.getMaxX())) {
                            span.add(tp);
                        } else {
                            if (potG.getMinX() <= thisSpot.getMaxX() && (thisSpot.getMaxX() <= potG.getMaxX())) {
                                span.add(tp);
                            }
                        }
                    }
                }
            }
            if (span.size() > 0) {
                return span;
            }
            i--;
        }
        return span;
    }



    public int linesStartWith(String text) {
        return linesStartWith(text, true);
    }

    public int linesStartWith(String text, boolean matchCase) {
        int count = 0;
        for (Component c : getChildren()) {
            if (matchCase) {
                if (c.getText().startsWith(text)) {
                    count++;
                }
            } else {
                if (c.getText().toLowerCase().startsWith(text)) {
                    count++;
                }
            }
        }
        return count;
    }

}
