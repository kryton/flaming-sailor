import com.zilbo.flamingSailor.TE.PDFParser;
import com.zilbo.flamingSailor.TE.model.TextPage;

import java.io.File;
import java.io.IOException;
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
public class pdf2Text {
    public static void main(String args[]) throws IOException {
        String filename;
        //filename= "../test/lineSortTest.pdf";

        filename = "../test/linedTableTest.pdf";
        filename = "/data/sedar/2012-03-01/01867105-00000001-00021840-C@#SEDAR#DOWNLOAD#UrEnergy#20120301TechReport-PDF.pdf";
        //filename = "../test/imageTest.pdf";
        if (args.length > 1) {
            filename = args[0];
        }
        PDFParser parser = new PDFParser();
        List<TextPage> pages = parser.getTextPages(new File(filename), TextPage.MINIMUM_FONT_HEIGHT);
        for (TextPage p : pages) {
            p.dumpChildren(System.out);
        }
    }

}
