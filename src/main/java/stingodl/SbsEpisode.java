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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class SbsEpisode implements Comparable<SbsEpisode> {
    public String id;
    public String series;
//    public String seriesUnique;
//    public String seriesId;
    public String seriesKey;
    public boolean film = false;
    public List<String> genres;
    public String title;
    public String description;
    public String countryOfOrigin;
    public String language;
    public String pubDate;
    public String availableDate;
    public String thumbnailUrl;
    public long expirationDate;
    public int seriesNumber = 0;
    public int episodeNumber = 0;
    public int duration;

    static final Logger LOGGER = Logger.getLogger(SbsEpisode.class.getName());
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SbsEpisode(){}

    public SbsEpisode(SbsEntry entry){
        title = entry.title;
        description = entry.description;
        countryOfOrigin = entry.pl1$countryOfOrigin;
        language = entry.pl1$language;
        thumbnailUrl = entry.plmedia$defaultThumbnailUrl;
        int lastSlash = entry.id.lastIndexOf('/');
        id = entry.id.substring(lastSlash + 1);
        expirationDate = entry.media$expirationDate;
        try {
            pubDate = sdf.format(new Date(entry.pubDate));
            availableDate = sdf.format(new Date(entry.media$availableDate));
        } catch (Exception e) {
            LOGGER.fine("Entry date invalid");
        }
        for (SbsMediaContent mc: entry.media$content) {
            if ("video".equals(mc.plfile$contentType)) {
                duration = mc.plfile$duration;
                break;
            }
        }
        for (SbsCategory cat: entry.media$categories) {
            if ("Genre".equals(cat.media$scheme)) {
                if ("Film".equals(cat.media$name)) {
                    film = true;
                } else if ("Short Film".equals(cat.media$name)) {
                    film = true;
                    addGenre(cat.media$name);
                } else {
                    if ("Factual".equals(cat.media$name)) {
                        addGenre("Documentary");
                    } else {
                        addGenre(cat.media$name);
                    }
                }
            } else if (cat.media$name.startsWith("Film/")) {
                film = true;
                String genre = cat.media$name.substring(5);
                if (genre.equals("Documentary Feature")) {
                    addGenre("Documentary");
                } else {
                    addGenre(genre);
                }
            }
        }

        parseNumbers(entry.title);
        if (film && (seriesNumber > 0)) { // not really a film
//            System.out.println(entry.title + " * " + genres + " * " + entry.pl1$seriesId + " * " + seriesNumber + " * " + episodeNumber);
            film = false;
        }

        if (!film) {
//            String seriesId = null;
//            if ((entry.pl1$seriesId != null) && (entry.pl1$seriesId.trim().length() > 0)) {
//                seriesId = entry.pl1$seriesId;
//            }
            if ((entry.pl1$programName != null) && (entry.pl1$programName.trim().length() > 0)) {
                series = entry.pl1$programName;
            }
//            if (seriesId != null) {
//                seriesKey = seriesId;
            if (series != null) {
                seriesKey = series;
            } else {
                seriesKey = title;
            }
        }
    }

    private void addGenre(String genre) {
        if (genres == null) {
            genres = new ArrayList<>();
            genres.add(genre);
        } else {
            if (!genres.contains(genre)) {
                genres.add(genre);
            }
        }
    }

    public void parseNumbers(String title) {
        StringTokenizer st = new StringTokenizer(title, " -");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ((token.length() > 1) && (token.charAt(0) == 'S') && Character.isDigit(token.charAt(1))) {
                try {
                    seriesNumber = Integer.parseInt(token.substring(1));
                } catch (NumberFormatException nfe) {
                }
            } else if ((token.length() > 2) && (token.charAt(0) == 'E') && (token.charAt(1) == 'p')
                    && Character.isDigit(token.charAt(2))) {
                try {
                    episodeNumber = Integer.parseInt(token.substring(2));
                } catch (NumberFormatException nfe) {
                }
            }
        }
    }

    @Override
    public int compareTo(SbsEpisode o) {
            if ((this.series != null) && (o.series != null) &&
                    (this.seriesNumber > 0) && (o.seriesNumber > 0) &&
                    (this.episodeNumber > 0) && (o.episodeNumber > 0)) {
                // numbered series
                int s = this.series.compareTo(o.series);
                if (s == 0) {
                    int sn = o.seriesNumber - this.seriesNumber;
                    if (sn == 0) {
                        return o.episodeNumber - this.episodeNumber;
                    } else {
                        return sn;
                    }
                } else {
                    return 0 - s;
                }
            } else { // non numbered series
//                int date = this.pubDate.compareTo(o.pubDate);
                int date = this.availableDate.compareTo(o.availableDate);
                if (date == 0) {
                    int t = this.title.compareTo(o.title);
                    if (t == 0) {
                        return t;
                    } else {
                        return 0 - t;
                    }
                } else {
                    return 0 - date;
                }
            }
    }

    public boolean isCurrent() {
        return (expirationDate > System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return title + " " + genres;
    }

    @Override
    public int hashCode() {
        return (int)Long.parseLong(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SbsEpisode) {
            SbsEpisode that = (SbsEpisode)obj;
            return this.id.equals(that.id);
        } else {
            return false;
        }
    }
}
