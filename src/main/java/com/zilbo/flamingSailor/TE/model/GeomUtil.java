package com.zilbo.flamingSailor.TE.model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
public class GeomUtil {
    public static String getRectangleDebug(Rectangle2D geom) {
        String format = "(%3.0f, %3.0f)/(%3.0f,%3.0f)";
        return String.format(format,
                geom.getMinX(),geom.getMinY(),geom.getMaxX(),geom.getMaxY());
    }

    public static String getPointDebug(Point2D point) {
        String format = "(%3.0f, %3.0f)";
        return String.format(format,
                point.getX(), point.getY());
    }

}
