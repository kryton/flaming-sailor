package com.zilbo.flamingSailor.TE.model;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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
public class NamesMerge {
    private static final Logger logger = Logger.getLogger(NamesMerge.class);
    Map<String, Integer> nameMap;
    Map<String, String> nameRC;
    Set<String> ignored;
    Set<String> otherMap;
    Map<String, Set<String>> surnames;
    Pattern titleMatch = Pattern.compile("(mr|mrs|miss|dr|ms|messrs)\\.?", Pattern.CASE_INSENSITIVE);

    static public void main(String args[]) throws IOException {

        /*
        HashSet<String> ignored = new HashSet<String>();
        ignored.add("toronto");
        ignored.add("guelph");
        ignored.add("calgary");
        ignored.add("jupiter");
        ignored.add("chair");
        HashSet<String> s = new HashSet<String>();
        s.add("charles sirois");
        s.add("richard nesbitt");
        */

        NamesMerge nm = NamesMerge.readinTestNames("TE/test/names_2.txt");

        //      NamesMerge nm2 = NamesMerge.dedup(nm);
        for (Map.Entry<String, Integer> e : nm.getNames().entrySet()) {
            logger.info(e.getKey() + "\t" + e.getValue());
        }
        logger.info("**\nSurnames\n**");
        for (Map.Entry<String, Set<String>> e : nm.surnames.entrySet()) {
            logger.info(e.getKey() + "\t" + e.getValue());
        }
    }

    public static NamesMerge readinTestNames(String fileName) throws IOException {
        return readinTestNames(fileName, null);
    }

    public static NamesMerge readinTestNames(String fileName, Set<String> ignored) throws IOException {

        BufferedReader r = new BufferedReader(new FileReader(fileName));

        NamesMerge nm = new NamesMerge(ignored);

        Map<String, Integer> x = new HashMap<String, Integer>();
        String line = r.readLine();
        while (line != null) {
            String[] p = line.split("\t");
            x.put(p[0], Integer.decode(p[1]));
            line = r.readLine();
        }
        nm.addAll(x);
        return nm;
    }


    public NamesMerge() {
        nameMap = new HashMap<String, Integer>();
        nameRC = new HashMap<String, String>();
        ignored = new HashSet<String>();
        otherMap = new HashSet<String>();
        surnames = new HashMap<String, Set<String>>();
    }

    public NamesMerge(Set<String> toIgnore) {
        nameMap = new HashMap<String, Integer>();
        nameRC = new HashMap<String, String>();
        ignored = new HashSet<String>();
        otherMap = new HashSet<String>();
        surnames = new HashMap<String, Set<String>>();

        if (toIgnore != null) {
            for (String s : toIgnore) {
                ignored.add(s.toLowerCase());
            }
        }
    }

    public NamesMerge(Set<String> toIgnore, Set<String> ignore2) {
        nameMap = new HashMap<String, Integer>();
        ignored = new HashSet<String>();
        surnames = new HashMap<String,  Set<String>>();
        for (String s : toIgnore) {
            ignored.add(s.toLowerCase());
        }
        otherMap = new HashSet<String>();
        for (String s : ignore2) {
            otherMap.add(s.toLowerCase());
        }
    }

    public void add(String name) {
        this.add(name, 1);
    }

    public static NamesMerge dedup(NamesMerge orig) {
        SortedMap<String, Integer> names = new TreeMap<String, Integer>();
        for (Map.Entry<String, Integer> n : orig.nameMap.entrySet()) {
            names.put(n.getKey(), n.getValue());
        }
        NamesMerge nm2 = new NamesMerge(orig.ignored);
        nm2.nameRC = orig.nameRC;
        nm2.surnames = new HashMap<String, Set<String>>(orig.surnames);
        nm2.setOtherMap(orig.otherMap);

        for (Map.Entry<String, Integer> n : names.entrySet()) {
            nm2.add(n.getKey(), n.getValue());
        }

        return nm2;
    }

    public boolean contains(String name) {
        return (this.nameMap.containsKey(name.trim().toLowerCase()));
    }

    public void addAll(Map<String, Integer> list) {
        if (list == null) {
            return;
        }
        for (Map.Entry<String, Integer> n : list.entrySet()) {
            nameRC.put(n.getKey().toLowerCase(), n.getKey());
            this.add(n.getKey().toLowerCase(), n.getValue());
        }
        NamesMerge n = dedup(this);
        this.nameMap = n.nameMap;
    }

    public void add(String name, Integer c) {

        if (ignored.contains(name)) {
            return;
        }
        // ignore strings with just titles
        if (titleMatch.matcher(name.trim()).matches()) {
            return;
        }
        String nPart[] = name.split(" ", 2);
        if (nPart.length > 1) {
            if (titleMatch.matcher(nPart[0]).matches()) {
                if ( !surnames.containsKey(nPart[1])) {
                    surnames.put(nPart[1], new HashSet<String>());
                }
            } else {
                String xnPart[] = name.split(" ");
                String potentialSurname = xnPart[xnPart.length-1];
                Set<String> x = surnames.get(potentialSurname);
                if (x != null) {
                  //  if (x.isEmpty()) {
                        x.add(name);
                        surnames.put(potentialSurname, x);
                  /*
                    } else {
                        if (x.length() > name.length()) {
                            surnames.put(potentialSurname, name);
                        }
                    }
                    */
                } else {
                    // don't just put a surname in here from a regular name, just use 100% mr. FOO ones
               //     surnames.put(potentialSurname,new HashSet<String>());
                }
            }
        }
        Integer count = nameMap.get(name);
        if (count == null || count == 0) {
            String parts[] = name.split(" ");
            if (parts.length < 2) {
                nameMap.put(name, c);
            } else {
                int i = 0;
                int len = parts.length;
                if (parts.length > 2) {
                    if (ignored.contains(parts[0])) {
                        i = 1;
                    }
                    if (ignored.contains(parts[len - 1])) {
                        len = len - 1;
                    }
                }
                if (len > 3) {
                    // find XY XY Z... pattern, adding a 'XY' and leaving the 'Z'
                    if (parts[i].equals(parts[i + 2]) && parts[i + 1].equals(parts[i + 3])) {
                        this.add(parts[i] + " " + parts[i + 1]);
                        i = i + 4;
                    }
                }
                if (len > 5 && i+3<len) {
                    String x = parts[i + 2] + " " + parts[i + 3];
                    if (nameMap.containsKey(x)) {
                        this.add(parts[i] + " " + parts[i + 1]);
                        this.add(parts[i + 2] + " " + parts[i + 3]);
                        i += 4;
                    }
                }

                if (i >= len) {
                    return;
                }
                StringBuilder nameBuilder = new StringBuilder(parts[i]);
                i += 1;
                int startI = i;
                int startLen = len;
                while (i < len) {
                    /*
                    if ( titleMatch.matcher(parts[i]).matches()) {
                        logger.info(nameBuilder);
                    }
                    */
                    if (nameBuilder == null) {
                        if (ignored.contains(parts[i])) {
                            i += 1;
                            continue;
                        } else {
                            nameBuilder = new StringBuilder(parts[i]);
                        }
                    } else {
                        nameBuilder.append(" ").append(parts[i]);
                    }
                    String potentialName = nameBuilder.toString();
                    if (!this.otherMap.contains(potentialName)) {
                        count = nameMap.get(potentialName);
                        if (count != null) {
                            nameMap.put(potentialName, count + c);
                            nameBuilder = null;
                        }
                    } else {
                        nameBuilder = null;
                    }
                    i += 1;
                }

                if (nameBuilder != null) {
                    i = startI;
                    len = startLen;
                    if (len - i > 1) {
                        nameBuilder = new StringBuilder(parts[len - 1]);
                        len -= 1;
                        while (i <= len) {
                            if (nameBuilder == null) {
                                if (ignored.contains(parts[len - 1])) {
                                    len -= 1;
                                    continue;
                                } else {
                                    nameBuilder = new StringBuilder(parts[len - 1]);
                                }
                            } else {
                                StringBuilder sb = new StringBuilder(parts[len - 1]);
                                sb.append(" ").append(nameBuilder);
                                nameBuilder = sb;
                            }
                            String potentialName = nameBuilder.toString();
                            if (!this.otherMap.contains(potentialName)) {
                                count = nameMap.get(potentialName);
                                if (count != null) {
                                    nameMap.put(potentialName, count + c);
                                    nameBuilder = null;
                                }
                            } else {
                                nameBuilder = null;
                            }
                            len -= 1;
                        }
                    }
                    if ( nameBuilder!= null) {
                        String potentialName = nameBuilder.toString();
                        if (!ignored.contains(potentialName)) {
                            nameMap.put(potentialName, c);
                        }
                    }
                }
            }
        } else {
            nameMap.put(name, count + c);
        }
    }

    public Map<String, Integer> getNames() {
        Map<String, Integer> ret = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> e : nameMap.entrySet()) {
            if (nameRC.containsKey(e.getKey())) {
                ret.put(nameRC.get(e.getKey()), e.getValue());
            } else {
                ret.put(e.getKey(), e.getValue());
            }
        }
        return ret;
    }

    public void setOtherMap(Set<String> otherMap) {
        this.otherMap = new HashSet<String>();
        for (String s : otherMap) {
            this.otherMap.add(s.toLowerCase());
        }
    }

    /**
     * returns true if we have seent the surname before
     * @param name the namestring
     * @return     true if we have seen it
     * TODO
     * only returns true/false .. could possibly return the full name, but have to deal with us
     * having multiple different people with same surname (eg jason & phillip dezwirek )
     */
    public boolean surnameMatches(String name) {
        //  Claudio Mannarino;
        String nPart[] = name.split(" ", 2);
        if (nPart.length > 1) {
            if (surnames.containsKey(nPart[1].toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }
}
