/*******************************************************************************
 * Copyright (c) 2020 StingoDL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package stingodl;

import java.util.*;
import java.util.logging.Logger;

public class SbsSeries implements Comparable<SbsSeries> {
    public String series;
    public String genre;
//    public String seriesId;
    public String seriesKey;
    public String latestDate = "1970-01-01 00:00:00";
    public Map<String, SbsEpisode> episodes = new HashMap<>();

    static final Logger LOGGER = Logger.getLogger(SbsSeries.class.getName());

    @Override
    public int compareTo(SbsSeries o) {
        int date = this.latestDate.compareTo(o.latestDate);
        if (date == 0) {
            return this.series.compareTo(o.series);
        } else {
            return -date;
        }
    }

    /*
        Determine the seriesId and series name from the list of episodes
     */
    public void discoverMeta() {
//        Set<String> seriesIdSet = new HashSet<>();
        Set<String> seriesNameSet = new HashSet<>();
//        Set<String> seriesUniqueSet = new HashSet<>();
        for (SbsEpisode ep: episodes.values()) {
//            if (ep.seriesId != null) {
//                seriesIdSet.add(ep.seriesId);
//            }
            if (ep.series != null) {
                seriesNameSet.add(ep.series);
            }
//            if (ep.seriesUnique != null) {
//                seriesUniqueSet.add(ep.seriesUnique);
//            }
        }
//        if (seriesIdSet.size() == 1) {
//            seriesId = seriesIdSet.toArray(new String[1])[0];
//        } else if (seriesIdSet.size() > 1) {
//            LOGGER.severe("Series contains multiple SeriesId");
//        }
        if (seriesNameSet.size() == 1) {
            series = seriesNameSet.toArray(new String[1])[0];
//        } else { // no names or multiple
//            if (seriesUniqueSet.size() == 1) {
//                series = formName(seriesUniqueSet.toArray(new String[1])[0]);
//            }
        }
        if (series == null) {
            if (episodes.size() == 1) { // no series name or unique series
                series = episodes.values().toArray(new SbsEpisode[1])[0].title;
            } else if (seriesNameSet.size() > 1) {
                series = seriesNameSet.toArray(new String[seriesNameSet.size()])[0];
 //           } else if (seriesUniqueSet.size() > 1) {
 //               series = formName(seriesUniqueSet.toArray(new String[seriesUniqueSet.size()])[0]);
            } else {
                series = episodes.values().toArray(new SbsEpisode[1])[0].title;
            }
        }
        for (SbsEpisode ep: episodes.values()) {
            ep.series = series;
        }
    }

    public String formName(String lower) {
        StringBuilder buf = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(lower);
        boolean start = true;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("s") && (!start)) {
                buf.append("'");
                buf.append(token);
                continue;
            }
            if (start) {
                start = false;
            } else {
                buf.append(' ');
            }
            char[] cap = token.toCharArray();
            cap[0] = Character.toUpperCase(cap[0]);
            for (char c: cap) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public String toString() {
        return "Series: " + seriesKey + " " + series;
    }
}
