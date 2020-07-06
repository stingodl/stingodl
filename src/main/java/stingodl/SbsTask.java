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

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SbsTask extends Task<Void> {

    static final Logger LOGGER = Logger.getLogger(SbsTask.class.getName());
    static final int INCREMENT = 500;
    static final int INDICITIVE_SIZE = 11000;
    int offset = 0;
    int loadedEpisodes = 0;
    int foundEpisodes = 0;
    long start;
    Status status;
    Panels panels;
    boolean quickUpdate = false;

    public SbsTask(Status status, Panels panels, boolean quickUpdate) {
        this.status = status;
        this.panels = panels;
        loadedEpisodes = status.sbsEpisodes.episodes.size();
        if (loadedEpisodes == 0) {
            loadedEpisodes = INDICITIVE_SIZE;
        }
        this.quickUpdate = quickUpdate;
        panels.sbsButtons.setDisable(true);
        panels.sbsQuickUpdate.setDisable(true);
        panels.sbsFullUpdate.setDisable(true);
        panels.sbsUpdate.progressProperty().bind(this.progressProperty());
        panels.sbsBox.getChildren().add(1, panels.sbsUpdate);
    }

    @Override
    protected Void call() {
        start = System.currentTimeMillis();
        try {
            int entries = 0;
            do {
                SbsSearch search = JsonConstructiveParser.parse(status.httpInput.getReader(getURI(offset)), SbsSearch.class);
                if (isCancelled()) {
                    return null;
                }
                search.searchOffset = offset;
                if (search.entries == null) {
                    search.entries = new ArrayList<>();
                }
                entries = search.entries.size();
                foundEpisodes += entries;
                updateProgress(foundEpisodes, loadedEpisodes);

                if (offset == 0) { //first 500
                    Platform.runLater(() -> {
                        status.sbsEpisodes.addSearch(search);
                        panels.sbsButtons.setDisable(false);
                    });
                    if (quickUpdate) {
                        break;
                    }
                } else {
                    Platform.runLater(() -> {
                        status.sbsEpisodes.addSearch(search);
                    });
                }

                LOGGER.fine("SbsSearch " + offset + " " + search.entries.size() + " time " + (System.currentTimeMillis() - start));
                offset += INCREMENT;
            } while (entries > 0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SBS Search Failed", e);
            RuntimeException re = new RuntimeException("SBS episode update failed", e);
        }
        return null;
    }

    public static URI getURI(int offset) throws Exception {
        URI uri = new URI("https",
                "www.sbs.com.au",
                "/api/video_feed/f/Bgtm9B/sbs-section-programs/",
                "range=" + (offset + 1) + "-" + (offset + INCREMENT),
                null);
        return uri;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        if (!quickUpdate) {
            status.sbsEpisodes.createFinalMaps();
            status.saveSbsEpisodes();
        }
        panels.sbsBox.getChildren().remove(panels.sbsUpdate);
        panels.sbsQuickUpdate.setDisable(false);
        panels.sbsFullUpdate.setDisable(false);
    }

    @Override
    protected void failed() {
        super.failed();
    }
}
