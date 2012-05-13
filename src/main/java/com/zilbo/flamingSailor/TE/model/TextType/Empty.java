package com.zilbo.flamingSailor.TE.model.TextType;

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
public class Empty extends TextType {
    @Override
    public String getType() {
        return "empty";
    }

    public boolean isEmpty() {
        return true;
    }

    public Empty() {
        super("");
    }
}
