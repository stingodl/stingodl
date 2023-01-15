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

public class SbsGenre implements Comparable<SbsGenre> {
    public String genre;
    public boolean film = false;
    public Map<String, SbsSeries> seriesMap = new HashMap<>();
    public Map<String, SbsEpisode> episodeMap = new HashMap<>();

    public boolean dateSorted = true;

    public SbsGenre(String genre, boolean film) {
        this.genre = genre;
        this.film = film;
    }

    public void addEpisode(SbsEpisode ep) {
        if (film) {
            episodeMap.put(ep.id, ep);
        } else {
            SbsSeries sbsSeries = seriesMap.get(ep.seriesKey);
            if (sbsSeries == null) {
                sbsSeries = new SbsSeries();
                sbsSeries.seriesKey = ep.seriesKey;
                sbsSeries.genre = genre;
                seriesMap.put(ep.seriesKey, sbsSeries);
            }
            sbsSeries.episodes.put(ep.id, ep);
            sbsSeries.discoverMeta();
            if (ep.availableDate == null) {
                if (ep.pubDate.compareTo(sbsSeries.latestDate) > 0) {
                    sbsSeries.latestDate = ep.pubDate;
                }
            } else {
                if (ep.availableDate.compareTo(sbsSeries.latestDate) > 0) {
                    sbsSeries.latestDate = ep.availableDate;
                }
            }
        }
    }

    public List<SbsSeries> getDateSortedSeries() {
        List<SbsSeries> list = new ArrayList<>(seriesMap.values());
        Collections.sort(list);
        return list;
    }

    public List<SbsSeries> getNameSortedSeries() {
        List<SbsSeries> list = new ArrayList<>(seriesMap.values());
        Collections.sort(list, new SeriesNameComparator());
        return list;
    }

    public List<SbsEpisode> getDateSortedEpisodes() {
        List<SbsEpisode> list = new ArrayList<>(episodeMap.values());
        Collections.sort(list);
        return list;
    }

    public List<SbsEpisode> getNameSortedEpisodes() {
        List<SbsEpisode> list = new ArrayList<>(episodeMap.values());
        Collections.sort(list, new EpisodeNameComparator());
        return list;
    }

    @Override
    public int compareTo(SbsGenre o) {
        return this.genre.compareTo(o.genre);
    }

    @Override
    public String toString() {
        if (film) {
            return "Film: " + genre;
        } else {
            return "Program: " + genre + " " + seriesMap.keySet();
        }
    }

    public static class SeriesNameComparator implements Comparator<SbsSeries> {
        @Override
        public int compare(SbsSeries o1, SbsSeries o2) {
            return o1.series.compareTo(o2.series);
        }
    }

    public static class EpisodeNameComparator implements Comparator<SbsEpisode> {
        @Override
        public int compare(SbsEpisode o1, SbsEpisode o2) {
            return o1.title.compareTo(o2.title);
        }
    }
}
