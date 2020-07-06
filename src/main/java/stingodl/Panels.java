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

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.logging.Logger;

public class Panels {

    static final Logger LOGGER = Logger.getLogger(Panels.class.getName());

    public ScrollPane scroll;
    public SButton networks;
    public SButton network;
    public boolean networkEnabled;
    public SButton category;
    public boolean categoryEnabled;
    public SButton series;
    public boolean seriesEnabled;
    public ProgressBar sbsUpdate = new ProgressBar();
    public ProgressBar progress;
    public Label currentName;
    public Label currentStatus;
    public Button downloadSelectedButton;
    public Button downloadAbcSeriesButton;
    public Button downloadSbsSeriesButton;
    public VBox abcButtons = new VBox();
    public Button abcUpdate = new Button("Update");
    public HBox abcBox = new HBox(abcButtons, abcUpdate);
    public VBox sbsButtons = new VBox();
    public HBox sbsBox = new HBox(sbsButtons);
    public Button sbsQuickUpdate = new Button("Quick Update");
    public Button sbsFullUpdate = new Button("Full Update");

    public Panels() {
        sbsUpdate.setMinWidth(200);
    }

    public void enableNetwork(boolean enable) {
        network.setDisable(!enable);
        networkEnabled = enable;
    }

    public void enableCategory(boolean enable) {
        category.setDisable(!enable);
        categoryEnabled = enable;
    }

    public void enableSeries(boolean enable) {
        series.setDisable(!enable);
        seriesEnabled = enable;
    }

    public void disable() {
        networks.setDisable(true);
        network.setDisable(true);
        category.setDisable(true);
        series.setDisable(true);
        scroll.setDisable(true);
    }

    public void enable() {
        networks.setDisable(false);
        network.setDisable(!networkEnabled);
        category.setDisable(!categoryEnabled);
        series.setDisable(!seriesEnabled);
        scroll.setDisable(false);
    }

    public Pane constructSbsPane(Status status, String href) {
        if (SbsHref.programs.name().equals(href)) { // List program genres
            return new SbsProgramsPanel(status, this);
        } else if (SbsHref.films.name().equals(href)) { // List film genres
            return new SbsFilmsPanel(status, this);
        } else if (href.startsWith(SbsHref.progGenre.name() + "/")) { // List program series
            SbsGenre genre = status.sbsEpisodes.programsMap.get(
                    href.substring(SbsHref.progGenre.name().length() + 1));
            return new SbsSeriesPanel(status, this, genre);
        } else if (href.equals(SbsHref.filmsAtoZ.name())) {
            return new SbsFilmsAtoZPanel(status, this);
        } else if (href.startsWith(SbsHref.filmsAtoZ.name() + "/")) {
            String letter = href.substring(SbsHref.filmsAtoZ.name().length() + 1);
            return new SbsEpisodesPanel(status, this, SbsHref.filmsAtoZ, letter);
        } else if (href.equals(SbsHref.filmsByPublishDate.name())) {
            return new SbsFilmsByPubPanel(status, this);
        } else if (href.startsWith(SbsHref.filmsByPublishDate.name() + "/")) {
            String month = href.substring(SbsHref.filmsByPublishDate.name().length() + 1);
            return new SbsEpisodesPanel(status, this, SbsHref.filmsByPublishDate, month);
        } else if (href.equals(SbsHref.filmsByExpiryDate.name())) {
            return new SbsFilmsByExpiryPanel(status, this);
        } else if (href.startsWith(SbsHref.filmsByExpiryDate.name() + "/")) {
            String month = href.substring(SbsHref.filmsByExpiryDate.name().length() + 1);
            return new SbsEpisodesPanel(status, this, SbsHref.filmsByExpiryDate, month);
        } else if (href.startsWith(SbsHref.filmGenre.name() + "/")) {
            SbsGenre genre = status.sbsEpisodes.filmsMap.get(
                    href.substring(SbsHref.filmGenre.name().length() + 1));
            if (genre != null) {
                return new SbsEpisodesPanel(status, this, genre);
            }
        } else if (href.startsWith(SbsHref.series.name() + "/")) {
            SbsSeries series = status.sbsEpisodes.seriesMap.get(
                    href.substring(SbsHref.series.name().length() + 1));
            return new SbsEpisodesPanel(status, this, series);
        }
        return null;
    }
}
