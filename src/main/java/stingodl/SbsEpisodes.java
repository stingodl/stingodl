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

import javafx.scene.control.Label;

import java.text.SimpleDateFormat;
import java.util.*;

public class SbsEpisodes {

    static final SimpleDateFormat veryShortDate = new SimpleDateFormat("yyyy-MM");

    long lastFullUpdate;
    List<SbsEpisode> episodes;
    transient Map<String, SbsEpisode> newEpisodeMap = new HashMap<>();
    transient Map<String, SbsEpisode> episodeMap = new HashMap<>();
    transient Map<String, SbsSeries> seriesMap = new HashMap<>();
    transient Map<String, SbsGenre> programsMap = new HashMap<>();
    transient Map<String, SbsGenre> filmsMap = new HashMap<>();
    transient Map<String, List<SbsEpisode>> filmsAtoZMap = new HashMap<>();
    transient Map<String, List<SbsEpisode>> filmsPubMap = new HashMap<>();
    transient Map<String, List<SbsEpisode>> filmsExpMap = new HashMap<>();

    public SbsEpisodes() {
    }

    public void createInitialMaps() {
        if (episodes == null) {
            episodes = new ArrayList<>();
        } else {
            for (SbsEpisode ep: episodes) {
                if (ep.isCurrent()) {
                    addToMaps(ep);
                }
            }
        }
    }

    public void createFinalMaps() {
        episodeMap = new HashMap<>();
        seriesMap = new HashMap<>();
        programsMap = new HashMap<>();
        filmsMap = new HashMap<>();
        filmsAtoZMap = new HashMap<>();
        filmsPubMap = new HashMap<>();
        filmsExpMap = new HashMap<>();
        for (SbsEpisode ep: newEpisodeMap.values()) {
            addToMaps(ep);
        }
        episodes = new ArrayList<>(newEpisodeMap.values());
    }

    private void updateGenres(SbsEpisode ep) {
        if (ep.film) {
            String letter = ep.title.substring(0,1).toUpperCase();
            if (letter.charAt(0) < 'A') {
                letter = "0-9";
            }
            List<SbsEpisode> letterList = filmsAtoZMap.get(letter);
            if (letterList == null) {
                letterList = new ArrayList<>();
                filmsAtoZMap.put(letter, letterList);
            }
            letterList.add(ep);

            String pub = null;
            if (ep.availableDate != null) {
                pub = ep.availableDate.substring(0, 7);
                List<SbsEpisode> pubList = filmsPubMap.get(pub);
                if (pubList == null) {
                    pubList = new ArrayList<>();
                    filmsPubMap.put(pub, pubList);
                }
                pubList.add(ep);
            }

            String exp = null;
            if (ep.expirationDate > 0L) {
                exp = veryShortDate.format(new Date(ep.expirationDate));
                List<SbsEpisode> expList = filmsExpMap.get(exp);
                if (expList == null) {
                    expList = new ArrayList<>();
                    filmsExpMap.put(exp, expList);
                }
                expList.add(ep);
            }
        }
        if (ep.genres != null) {
            if (ep.film) {
                for (String genre : ep.genres) {
                    SbsGenre filmGenre = filmsMap.get(genre);
                    if (filmGenre == null) {
                        filmGenre = new SbsGenre(genre, ep.film);
                        filmsMap.put(genre, filmGenre);
                    }
                    filmGenre.addEpisode(ep);
                }
            } else {
                for (String genre : ep.genres) {
                    SbsGenre progGenre = programsMap.get(genre);
                    if (progGenre == null) {
                        progGenre = new SbsGenre(genre, ep.film);
                        programsMap.put(genre, progGenre);
                    }
                    progGenre.addEpisode(ep);
                }
            }
        }
    }

    public void addSearch(SbsSearch sbsSearch) {
        for (SbsEntry entry : sbsSearch.entries) {
            if ("Live Stream".equals(entry.pl1$useType) || (entry.title == null) || ("".equals(entry.title.trim()))) {
            } else {
                SbsEpisode ep = new SbsEpisode(entry);
                if (!newEpisodeMap.containsKey(ep.id)) { // check for duplicate episode
                    newEpisodeMap.put(ep.id, ep);
                }
                if (!episodeMap.containsKey(ep.id)) { // check for duplicate episode
                    addToMaps(ep);
                }
            }
        }
    }

    private void addToMaps(SbsEpisode ep) {
        episodeMap.put(ep.id, ep);
        updateGenres(ep);
        SbsSeries sbsSeries = seriesMap.get(ep.seriesKey);
        if (sbsSeries == null) {
            sbsSeries = new SbsSeries();
            sbsSeries.seriesKey = ep.seriesKey;
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
