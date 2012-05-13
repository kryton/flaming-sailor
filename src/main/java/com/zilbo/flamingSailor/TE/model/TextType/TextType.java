package com.zilbo.flamingSailor.TE.model.TextType;

import com.zilbo.flamingSailor.TE.model.Component;
import com.zilbo.flamingSailor.TE.model.NamesMerge;
import com.zilbo.flamingSailor.TE.model.TextPiece;
import org.apache.log4j.Logger;

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
public abstract class TextType {
    private static final Logger logger = Logger.getLogger(TextType.class);
    protected String categorizedText;

    protected TextType(String text) {
        this.categorizedText = text;
    }

    public abstract String getType();

    public boolean isNumber() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isText() {
        return false;
    }

    public boolean isYear() {
        return false;
    }

    public boolean isUnknown() {
        return false;
    }

    public static boolean matchesType(String text) {
        return false;
    }

    public String getCategorizedText() {
        return categorizedText;
    }

    public static TextType categorize(TextPiece tp) {
        if (!tp.getType().isUnknown()) {
            logger.info("recat?");
        }
        String text = tp.getText();
        text = text.trim();
        if (text.isEmpty()) {
            return new Empty();
        }

        if (Year.matchesType(text)) {
            return new Year(text);
        }
        if (Percent.matchesType(text)) {
            return new Percent(text);
        }

        //footnotes are a bitch
        text = text.replaceAll("\\(\\d\\)", "").trim();

        if (Nil.matchesType(text)) {
            return new Nil();
        }

        if (Number.matchesType(text)) {
            return new Number(text);
        }


        text = text.replaceAll("(\\s\\.)+", "");
        text = text.replaceAll("\\.\\.\\.(\\.)+", "");
        // intellij doesn't like this reg.
        String footNotePat = "\\(\\d";
        footNotePat += "{1,3}+\\)";
        text = text.replaceAll(footNotePat, "");
        text = text.trim();
        if (text.isEmpty()) {
            return new Empty();
        }

        if (tp.getType().isUnknown()) {
            return new Text(text);
        } else {
            return tp.getType();
        }
    }

    @Override
    public String toString() {
        return this.getType();
    }
}
