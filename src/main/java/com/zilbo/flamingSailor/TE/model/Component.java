package com.zilbo.flamingSailor.TE.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
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
public abstract class Component implements Comparable<Component> {
    private static final Logger logger = Logger.getLogger(Component.class);
    Rectangle2D geom;
    Long id;
    List<Component> pieces;


    public String getText() {
        int end = this.getChildren().size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < end; i++) {
            if (i != 0) {
                sb.append('\n');
            }
            sb.append(this.getChildren().get(i).getText());

        }
        return sb.toString();
    }

    public boolean isEmpty() {
        for (Component c : pieces) {
            if (!c.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasChildren() {
        return this.pieces.size() > 0;
    }

    // TODO make this shallow!
    public List<Component> getChildren() {
        List<Component> c = new ArrayList<>();
        c.addAll(pieces);
        return c;
    }

    /**
     * remove a child component.
     * this makes no effort to fix up geographic boundaries, and will fail silently if it doesn't exist.
     * this also doesn't do a depth-wide search for the component.
     *
     * @param c the child to remove
     */
    public void removeChild(Component c) {
        this.pieces.remove(c);
        adjustGeom();
    }



    /**
     * recalculate the geometery of the piece, based on the children of it.
     */
    protected void adjustGeom() {
        geom = new Rectangle2D.Double();
        if (pieces.size() > 0) {
            geom = pieces.get(0).getGeom().getBounds2D();
            for (int i = 1; i < pieces.size(); i++) {
                geom = geom.createUnion(pieces.get(i).getGeom());
            }
        }
    }

    public int size() {
        return this.getChildren().size();
    }

    public Long getID() {
        return this.id;
    }

    protected Component(Long id) {
        this.id = id;
        this.geom = new Rectangle2D.Double();
        pieces = new ArrayList<>();
    }

    public void addChild(Component child) {

        if (pieces.size() == 0) {
            Rectangle2D childRect = child.getGeom();
            geom = new Rectangle2D.Double();
            geom.setRect(childRect);
            //  geom = new Rectangle2D.Double(childRect.getMinX(), childRect.getMinY(), childRect.getMaxX(), childRect.getMaxY());
        } else {
            expandBoundry(child.getGeom());
        }
        pieces.add(child);

    }

    public void addChildAtTop(Component child) {
        if (pieces.size() == 0) {
            Rectangle2D childRect = child.getGeom();
            geom = new Rectangle2D.Double();
            geom.setRect(childRect);
            // geom = new Rectangle2D.Double(childRect.getMinX(), childRect.getMinY(), childRect.getMaxX(), childRect.getMaxY());
        } else {
            expandBoundry(child.getGeom());
        }
        pieces.add(0, child);

    }

    public void merge(Component mergeThis) {
        expandBoundry(mergeThis.getGeom());

        for (Component tl : mergeThis.getChildren()) {
            this.addChild(tl);
        }
    }


    public Rectangle2D getGeom() {
        return this.geom;
    }

    /**
     * @return distance between 2 X points (the width)
     */
    public double width() {
        return getGeom().getWidth();
    }

    /**
     * @return height of the component
     */

    public double height() {
        return getGeom().getHeight();

    }

    @Override
    /**
     * j
     * Sorts text pieces by position by comparing the location of this text piece
     *  with a given text piece t
     * @param t
     *       the text piece to compare with
     * @return
     *       the comparison result
     *            -1: this text piece is located in the left-side or the top of another text piece t
     *             1: this text piece is located in the right-side or the below of another text piece t
     *             0: this text piece is located in the same location as another text piece t
     */
    public int compareTo(Component t) {
        //   Line2D g1 = this.getGeom();
        //  Line2D g2 = t.getGeom();
        // int ret;
        /*
        if (g1.getP1() == g2.getP1() && g1.getP2() == g2.getP2()) {
            ret = 0;
            //   logger.info(ret + "\t" + GeomUtil.getRectangleDebug(g1) + "\t" + GeomUtil.getRectangleDebug(g2));
            return ret;
        }
        */

        if (onSameLine(t)) {
            //ret = new Long(Math.round(y1 / 10)).compareTo(Math.round(y2 / 10));
            //if (ret == 0) {
            //if ( Math.abs(y1-y2) <2) {
            Double x1 = getGeom().getMinX();
            return x1.compareTo(t.getGeom().getMinX());

        } else {
            Double y1 = getGeom().getMinY();
            // double y2 = g2.getY1();

            return y1.compareTo(t.getGeom().getMinY());
        }
    }

    public static class left_comparator implements Comparator<Component> {

        public int compare(Component t1, Component t2) {

            double result = t1.getGeom().getMinX() - t2.getGeom().getMaxX();

            if (result > 0) {
                return 1;
            }
            if (result < 0) {
                return -1;
            }
            return 0;
        }
    }

    public static class top_comparator implements Comparator<Component> {

        public int compare(Component t1, Component t2) {
            if (t1.onSameLine(t2)) {
                return 0;
            }
            double result = t1.getGeom().getMinY() - t2.getGeom().getMaxY();

            if (result > 0) {
                return 1;
            }
            if (result < 0) {
                return -1;
            }
            return 0;
        }
    }

    public static class topleft_comparator implements Comparator<Component> {

        public int compare(Component t1, Component t2) {
            return t1.compareTo(t2);
        }
    }

    public String getRectangleDebug() {
        return GeomUtil.getRectangleDebug(geom);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + "{" +
                getRectangleDebug()
                + "\n");
        for (Component p : getChildren()) {
            sb.append("\t\t").append(p.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }


    public double density() {
        double density = 0.0;
        for (Component c : getChildren()) {

            density += c.density();
        }
        return density / (1.0 * getChildren().size());
    }

    public void expandBoundry(Rectangle2D geom2) {

        geom = geom.createUnion(geom2);
        /*
        Point2D point1;
        Point2D point2;
        double x;
        double y;

        if (geom.getX1() > geom2.getX1()) {
            x = geom2.getX1();
        } else {
            x = geom.getX1();
        }
        if (geom.getY1() > geom2.getY1()) {
            y = geom2.getY1();
        } else {
            y = geom.getY1();
        }
        point1 = new Point2D.Double(x, y);

        if (geom.getX2() < geom2.getX2()) {
            x = geom2.getX2();
        } else {
            x = geom.getX2();
        }
        if (geom.getY2() < geom2.getY2()) {
            y = geom2.getY2();
        } else {
            y = geom.getY2();
        }
        point2 = new Point2D.Double(x, y);
        return new Rectangle2D.Double(point1, point2);
        */
    }

    /**
     * Is 'l2' inside of 'l1'
     *
     * @param l1 box1
     * @return true if this component is inside the box
     */
    public boolean isContainedBy(Rectangle2D l1) {

        return l1.contains(geom);
        /*
        if (l1.getX1() <= geom.getX1() && l1.getX2() >= geom.getX2()) {
            if (l1.getY1() <= geom.getY1() && l1.getY2() >= geom.getY2()) {
                return true;
            }
        }
        return false;
        */

    }

    public List<Component> findByGeom(Rectangle2D box) {
        List<Component> ret = new ArrayList<>();
        if (isContainedBy(box)) {
            ret.add(this);
            return ret;
        }
        for (Component c : getChildren()) {
            ret.addAll(c.findByGeom(box));
        }
        return ret;
    }

    /**
     * if the bottom of the lines rectangle is lower than the textpiece's top, then it belongs here
     *
     * @param c the piece to compare against
     * @return true if they are
     */


    public boolean onSameLine(Component c) {

        return onSameLine(c.getGeom());
    }

    public boolean onSameLine(Rectangle2D geom) {

        Double y1 = this.geom.getMinY();
        Double y3 = this.geom.getMaxY();
        Double y2 = geom.getMinY();
        Double y4 = geom.getMaxY();

        double yAveT = (y2 + y4) / 2;
        //   double diff = (y4-y2)/4;
        if ((y1 <= (yAveT)) && ((yAveT) < y3)) {
            return true;
        }
        yAveT = (y1 + y3) / 2;
        return (y2 <= yAveT) && (yAveT < y4);

    }

    public void dumpChildren(PrintStream out, int level) {

        StringBuilder sb = new StringBuilder();

        sb.append(StringUtils.repeat("..", level));
        sb.append(getClass().getSimpleName());
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

    boolean isHeading = false;

    public boolean isHeading() {
        return this.isHeading;
    }

    public void setIsHeading(boolean flag) {
        isHeading = flag;
    }

    public boolean isMultiLine() {
        return false;
    }

}
