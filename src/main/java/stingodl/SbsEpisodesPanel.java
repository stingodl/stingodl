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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class SbsEpisodesPanel extends VBox {

    static final Logger LOGGER = Logger.getLogger(SbsEpisodesPanel.class.getName());
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    List<SCheckBox> cbs = new ArrayList<>();

    StackPane cardPanel = new StackPane();
    VBox dateSortedPanel = new VBox();
    VBox nameSortedPanel = new VBox();

    public SbsEpisodesPanel(Status status, Panels panels, SbsSeries series) { // Programs
        super();

        HBox topRow = new HBox(8);
        topRow.setPadding(new Insets(5));
        Label seriesLabel = new Label("Download all episodes as they become available");
        topRow.getChildren().add(seriesLabel);
        SCheckBox seriesCb = new SCheckBox();
        seriesCb.network = Network.SBS;
        seriesCb.href = series.seriesKey;
        // seriesId can be null, which causes heaps of problems
        if (seriesCb.href == null) {
            seriesCb.setDisable(true);
        } else {
            seriesCb.setSelected(status.seriesSelection.isSelected(Network.SBS, series.seriesKey));
            seriesCb.setOnAction(actionEvent -> {
                if (seriesCb.isSelected()) {
                    status.seriesSelection.add(Network.SBS, seriesCb.href, series.series);
                } else {
                    status.seriesSelection.remove(Network.SBS, seriesCb.href);
                }
                status.saveSeriesSelection();
                for (SCheckBox cb : cbs) {
                    if (seriesCb.isSelected()) {
                        cb.setSelected(false);
                        status.selectedEpMap.remove(new EpisodeKey(cb.network, cb.href));
                        cb.setDisable(true);
                    } else {
                        cb.setDisable(false);
                    }
                }
                LOGGER.fine("Selected eps " + status.selectedEpMap);
            });
        }
        topRow.getChildren().add(seriesCb);
        this.getChildren().add(topRow);

        LOGGER.fine("Ready to start episodes");
        List<SbsEpisode> sorted = new ArrayList<SbsEpisode>(series.episodes.values());
        Collections.sort(sorted);
        simpleLayout(status, panels, null, sorted, seriesCb);
    }

    private void simpleLayout(Status status, Panels panels, SbsHref href, List<SbsEpisode> sorted, SCheckBox seriesCb) {
        for (SbsEpisode ep: sorted) {
            LOGGER.fine("Episode: " + ep.title);
            String title = ep.title;
            if ((title == null) || (title.trim().equals(""))) {
                title = ep.series;
            }
            SCheckBox cb = new SCheckBox();
            cb.network = Network.SBS;
            cb.href = ep.id;
            cbs.add(cb);
            SButton episodeButton = new SButton(title);
            episodeButton.network = Network.SBS;
            episodeButton.href = ep.id;
            SelectedEpisode se = new SelectedEpisode(Network.SBS, ep.id);
            se.title = ep.title;
            se.description = ep.description;
            se.countryOfOrigin = ep.countryOfOrigin;
            se.language = ep.language;
            se.duration = (ep.duration / 60) + " min";
            se.expiry = sdf.format(new Date(ep.expirationDate));
            se.thumbnailUrl = ep.thumbnailUrl;
            episodeButton.setOnAction(e -> {
                EpisodePanel epPanel = new EpisodePanel(se, status);
                panels.scroll.setContent(epPanel);
            });
            if (status.episodeHistory.contains(Network.SBS.historyHref(ep.id))) {
                episodeButton.setTextFill(Color.GRAY);
            }
            EpisodeKey key = new EpisodeKey(Network.SBS, ep.id);
            if (seriesCb == null) {
                cb.setSelected(status.selectedEpMap.containsKey(key));
            } else {
                if (seriesCb.isSelected()) { // download series as available ie no individual selection
                    cb.setDisable(true);
                } else {
                    cb.setSelected(status.selectedEpMap.containsKey(key));
                }
            }
            VisualEpisode ve = status.visualEpMap.get(key);
            if (ve == null) {
                ve = new VisualEpisode();
                status.visualEpMap.put(key, ve);
            }
            ve.setWidgets(episodeButton, cb);
            Label dateLabel = null;
            if (SbsHref.filmsByExpiryDate.equals(href)) {
                dateLabel = new Label(se.expiry);
            } else {
                if (ep.availableDate != null) {
                    dateLabel = new Label(ep.availableDate.substring(0, 10));
                }
            }
            dateLabel.setMinWidth(80);
            LOGGER.fine("Episode: " + episodeButton.href);
            cb.setOnAction(actionEvent -> {
                LOGGER.fine("Episode CB action " + cb.isSelected());
                SelectedEpisode cbSe = new SelectedEpisode(cb.network, cb.href);
                if (cb.isSelected()) {
                    status.selectedEpMap.put(cbSe.key, se);
                } else {
                    status.selectedEpMap.remove(cbSe.key);
                }
            });
            HBox row = new HBox(8);
            row.setPadding(new Insets(3));
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(episodeButton, Priority.ALWAYS);
            episodeButton.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().addAll(episodeButton, dateLabel, cb);
            this.getChildren().add(row);
        }
    }

    public SbsEpisodesPanel(Status status, Panels panels, SbsHref href, String key) {
        super();
        List<SbsEpisode> eps = null;
        if (SbsHref.filmsAtoZ.equals(href)) {
            eps = status.sbsEpisodes.filmsAtoZMap.get(key);
            if (eps != null) {
                Collections.sort(eps, new NameComp());
                simpleLayout(status, panels, href, eps, null);
            }
        } else if (SbsHref.filmsByPublishDate.equals(href)) {
            eps = status.sbsEpisodes.filmsPubMap.get(key);
            if (eps != null) {
                Collections.sort(eps);
                simpleLayout(status, panels, href, eps, null);
            }
        } else if (SbsHref.filmsByExpiryDate.equals(href)) {
            eps = status.sbsEpisodes.filmsExpMap.get(key);
            if (eps != null) {
                Collections.sort(eps, new ExpireComp());
                simpleLayout(status, panels, href, eps, null);
            }
        }
    }

    public SbsEpisodesPanel(Status status, Panels panels, SbsGenre genre) { // Films
        super();
        List<SbsEpisode> dateSorted = genre.getDateSortedEpisodes();
        List<SbsEpisode> nameSorted = genre.getNameSortedEpisodes();
        layoutPanel(status, panels, dateSortedPanel, dateSorted, true);
        layoutPanel(status, panels, nameSortedPanel, nameSorted, false);
        nameSortedPanel.setVisible(false);
        cardPanel.getChildren().add(dateSortedPanel);
        cardPanel.getChildren().add(nameSortedPanel);
        Button nameButton = new Button("Sort by Name");
        Button dateButton = new Button("Sort by Date");
        HBox.setMargin(nameButton, new Insets(6,4,6,4));
        HBox.setMargin(dateButton, new Insets(6,4,6,4));
        nameButton.setOnAction(actionEvent -> {
            nameButton.setDisable(true);
            dateButton.setDisable(false);
            dateSortedPanel.setVisible(false);
            nameSortedPanel.setVisible(true);
        });
        dateButton.setDisable(true);
        dateButton.setOnAction(actionEvent -> {
            dateButton.setDisable(true);
            nameButton.setDisable(false);
            nameSortedPanel.setVisible(false);
            dateSortedPanel.setVisible(true);
        });
        HBox buttonRow = new HBox();
        buttonRow.getChildren().add(nameButton);
        Label filler = new Label();
        filler.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(filler, Priority.ALWAYS);
        buttonRow.getChildren().add(filler);
        buttonRow.getChildren().add(dateButton);
        this.getChildren().add(buttonRow);
        this.getChildren().add(cardPanel);
    }

    private void layoutPanel(Status status, Panels panels, VBox epPanel, List<SbsEpisode> list, boolean dateSorted) {
        for (SbsEpisode ep: list) {
            LOGGER.fine("Episode: " + ep.title);
            String title = ep.title;
            if ((title == null) || (title.trim().equals(""))) {
                title = ep.series;
            }
            SCheckBox cb = new SCheckBox();
            cb.network = Network.SBS;
            cb.href = ep.id;
            SButton episodeButton = new SButton(title);
            episodeButton.network = Network.SBS;
            episodeButton.href = ep.id;
            SelectedEpisode se = new SelectedEpisode(Network.SBS, episodeButton.href);
            se.title = ep.title;
            se.description = ep.description;
            se.countryOfOrigin = ep.countryOfOrigin;
            se.language = ep.language;
            se.duration = (ep.duration / 60) + " min";
            se.expiry = sdf.format(new Date(ep.expirationDate));
            se.thumbnailUrl = ep.thumbnailUrl;
            episodeButton.setOnAction(e -> {
                panels.scroll.setContent(new EpisodePanel(se, status));
            });
            if (status.episodeHistory.contains(Network.SBS.historyHref(ep.id))) {
                episodeButton.setTextFill(Color.GRAY);
            }
            EpisodeKey key = new EpisodeKey(cb.network, cb.href);
            boolean selected = status.selectedEpMap.containsKey(key);
            cb.setSelected(selected);
            if (selected) {
                VisualEpisode ve = status.visualEpMap.get(key);
                if (ve == null) {
                    ve = new VisualEpisode();
                    status.visualEpMap.put(key, ve);
                }
                ve.setWidgets(episodeButton, cb, dateSorted);
            }
            Label pubLabel = null;
            if (ep.availableDate == null) {
                pubLabel = new Label(ep.pubDate.substring(0, 10));
            } else {
                pubLabel = new Label(ep.availableDate.substring(0, 10));
            }
            LOGGER.fine("Episode: " + episodeButton.href);
            cb.setOnAction(actionEvent -> {
                LOGGER.fine("Episode CB action " + cb.isSelected());
                SelectedEpisode cbSe = new SelectedEpisode(cb.network, cb.href);
                VisualEpisode ve = status.visualEpMap.get(cbSe.key);
                if (ve == null) {
                    ve = new VisualEpisode();
                    status.visualEpMap.put(cbSe.key, ve);
                }
                ve.setWidgets(episodeButton, cb);
                if (cb.isSelected()) {
                    status.selectedEpMap.put(cbSe.key, se);
                    status.visualEpMap.put(cbSe.key, ve);
                } else {
                    status.selectedEpMap.remove(cbSe.key);
                    status.visualEpMap.remove(cbSe.key);
                }
            });
            HBox row = new HBox(8);
            row.setPadding(new Insets(3));
            episodeButton.setMaxWidth(Double.MAX_VALUE);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(episodeButton, Priority.ALWAYS);
            pubLabel.setMinWidth(80);
            row.getChildren().addAll(episodeButton,pubLabel,cb);
            epPanel.getChildren().add(row);
        }
    }

    public static class NameComp implements Comparator<SbsEpisode> {
        @Override
        public int compare(SbsEpisode o1, SbsEpisode o2) {
            return o1.title.compareTo(o2.title);
        }
    }

    public static class ExpireComp implements Comparator<SbsEpisode> {
        @Override
        public int compare(SbsEpisode o1, SbsEpisode o2) {
            return (int)(o1.expirationDate - o2.expirationDate);
        }
    }
}
