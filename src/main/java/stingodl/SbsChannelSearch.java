/*******************************************************************************
 * Copyright (c) 2021 StingoDL.
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

import javafx.application.Platform;

import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SbsChannelSearch extends Thread {

    static final Logger LOGGER = Logger.getLogger(SbsChannelSearch.class.getName());
    SbsTask sbsTask;
    String channel;

    public SbsChannelSearch(SbsTask sbsTask, String channel) {
        this.sbsTask = sbsTask;
        this.channel = channel;
    }

    @Override
    public void run() {
        int offset = 0;
        int entries = 0;
        long start = System.currentTimeMillis();
        do {
            try {
                if (sbsTask.isCancelled()) {
                    return;
                } else {
                    SbsSearch search = JsonConstructiveParser.parse(sbsTask.status.httpInput.getReader(getURI(channel, offset)), SbsSearch.class);
                    if (sbsTask.isCancelled() || (search == null)) {
                        return;
                    }
                    search.searchOffset = offset;
                    if (search.entries == null) {
                        search.entries = new ArrayList<>();
                    }
                    if (search.entries.size() > 0) {
                        Platform.runLater(() -> {
                            sbsTask.status.sbsEpisodes.addSearch(search);
                            sbsTask.updateProgressBar(search.entries.size());
                        });
                    }
                    LOGGER.fine("SbsSearch " + channel + " Offset " + offset + " Size " + search.entries.size() + " time " + (System.currentTimeMillis() - start));
                    offset += SbsTask.INCREMENT;
                    entries = search.entries.size();
                }
            } catch (InterruptedException ie) {
                LOGGER.log(Level.SEVERE, "SBS search cancelled: " + channel);
                entries = 0;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "SBS search failed: " + channel, ex);
                entries = 0;
                sbsTask.cancel();
            }
        } while (entries > 495); // allow for the odd case where the backend does not find all episodes asked for
    }

    public static URI getURI(String channel, int offset) throws Exception {
        URI uri = new URI("https",
                "www.sbs.com.au",
                "/api/video_feed/f/Bgtm9B/sbs-section-programs/",
                "byCategories=Channel/" + channel + "&range=" + (offset + 1) + "-" + (offset + SbsTask.INCREMENT),
                null);
        return uri;
    }
}
