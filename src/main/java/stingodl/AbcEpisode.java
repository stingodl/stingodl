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

import java.util.StringTokenizer;

public class AbcEpisode implements Comparable<AbcEpisode> {
    public String seriesTitle;
    public String title;
    public String pubDate;
    public String href;
    public int series;
    public int episode;
    public void parseTitle() {
        if (title != null) {
            StringTokenizer st = new StringTokenizer(title);
            String[] token = new String[4];
            for (int i = 0; i < 4; i++) {
                if (st.hasMoreTokens()) {
                    token[i] = st.nextToken();
                } else {
                    token[i] = "";
                }
            }
            if ("series".equalsIgnoreCase(token[0])) {
                try {
                    series = Integer.parseInt(token[1]);
                } catch (NumberFormatException nfe) {}
            }
            if ("ep".equalsIgnoreCase(token[2])) {
                try {
                    episode = Integer.parseInt(token[3]);
                } catch (NumberFormatException nfe) {}
            }
        }
    }
    public String toString() {
        return seriesTitle + " " + title + " " + series + " " + episode + " " + href + " " + pubDate;
    }

    @Override
    public int compareTo(AbcEpisode that) {
        if (this.series == that.series) {
            if (this.episode == that.episode) {
                if ((this.pubDate == null) || (that.pubDate == null)) {
                    return 0;
                } else {
                    return this.pubDate.compareTo(that.pubDate);
                }
            } else {
                return this.episode - that.episode;
            }
        } else {
            return this.series - that.series;
        }
    }
}
