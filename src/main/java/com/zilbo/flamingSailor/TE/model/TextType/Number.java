package com.zilbo.flamingSailor.TE.model.TextType;

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
public class Number extends TextType {
    static Pattern numberMatcher = Pattern.compile("^\\d+(,\\d\\d\\d)*$");
    static Pattern numberMatcherCur = Pattern.compile("^\\$\\s*\\d+(,\\d\\d\\d)*$");
    static Pattern numberMatcher2 = Pattern.compile("^\\d+(\\s\\d\\d\\d)*$");
    static Pattern numberMatcher3 = Pattern.compile("^\\d+(,\\d\\d\\d)*(\\.\\d+)*$");

    @Override
    public String getType() {
        return "number";
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    private static String parseText(String text) {
        if (text.equals("-0-")) {
            return "0";
        } else {
            return text;
        }
    }

    protected Number(String text) {
        super(parseText(text));
    }

    public static boolean matchesType(String text) {
        if (text.equals("-0-")) {
            return true;
        }
        return numberMatcher.matcher(text).matches() ||
                numberMatcherCur.matcher(text).matches() ||
                numberMatcher2.matcher(text).matches() ||
                numberMatcher3.matcher(text).matches();
    }

}
