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
    static final int INDICITIVE_SIZE = 13000;
    static final String[] CHANNELS = new String[] {"SBS1","NITV","SBS+World+Movies","Web+Exclusive","SBS+VICELAND","SBS+Food"};
    Thread[] thread = new Thread[CHANNELS.length];
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
            if (quickUpdate) {
                SbsSearch search = JsonConstructiveParser.parse(status.httpInput.getReader(getQuickURI()), SbsSearch.class);
                if (isCancelled()) {
                    return null;
                }
                if (search.entries == null) {
                    search.entries = new ArrayList<>();
                }
                Platform.runLater(() -> {
                    status.sbsEpisodes.addSearch(search);
                    panels.sbsButtons.setDisable(false);
                });
                LOGGER.fine("SbsQuickSearch " + search.entries.size() + " time " + (System.currentTimeMillis() - start));
            } else {
                for (int i = 0; i < CHANNELS.length; i++) {
                    String channel = CHANNELS[i];
                    thread[i] = new SbsChannelSearch(this, channel);
                    thread[i].start();
                }
                for (int i = 0; i < thread.length; i++) {
                    thread[i].join();
                }
                panels.sbsButtons.setDisable(false);
                if (!isCancelled()) {
                    status.pruneSbsSeries();
                }
                LOGGER.fine("Sbs episodes " + status.sbsEpisodes.episodes.size());
            }
        } catch (InterruptedException ie) { // cancelled
            for (int i = 0; i < thread.length; i++) {
                if (thread[i] != null) {
                    thread[i].interrupt();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SBS Search Failed", e);
            RuntimeException re = new RuntimeException("SBS episode update failed", e);
        }
        return null;
    }

    public void updateProgressBar(int entries) {
        foundEpisodes += entries;
        updateProgress(foundEpisodes, loadedEpisodes);
    }

    public static URI getQuickURI() throws Exception {
        URI uri = new URI("https",
                "www.sbs.com.au",
                "/api/video_feed/f/Bgtm9B/sbs-section-programs/",
                "range=1-100",
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
        LOGGER.fine("SbsSearch completed: quick = " + quickUpdate);
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        panels.sbsBox.getChildren().remove(panels.sbsUpdate);
        panels.sbsQuickUpdate.setDisable(false);
        panels.sbsFullUpdate.setDisable(false);
        panels.sbsButtons.setDisable(false);
        LOGGER.fine("SbsSearch cancelled");
    }

    @Override
    protected void failed() {
        super.failed();
        panels.sbsBox.getChildren().remove(panels.sbsUpdate);
        panels.sbsQuickUpdate.setDisable(false);
        panels.sbsFullUpdate.setDisable(false);
        panels.sbsButtons.setDisable(false);
        LOGGER.fine("SbsSearch failed");
    }
}
