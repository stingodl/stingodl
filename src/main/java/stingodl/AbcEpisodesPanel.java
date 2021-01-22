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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AbcEpisodesPanel extends VBox {

    static final Logger LOGGER = Logger.getLogger(AbcEpisodesPanel.class.getName());

    List<SCheckBox> cbs = new ArrayList<>();

    public AbcEpisodesPanel(Status status, Panels panels, AbcSeries series, String seriesHref) {
        super();
        HBox topRow = new HBox(8);
        topRow.setPadding(new Insets(5));
        Label seriesLabel = new Label("Download all episodes as they become available");
        topRow.getChildren().add(seriesLabel);
        SCheckBox seriesCb = new SCheckBox();
        seriesCb.network = Network.ABC;
        seriesCb.href = seriesHref;
        seriesCb.setSelected(status.seriesSelection.isSelected(Network.ABC, seriesHref));
        topRow.getChildren().add(seriesCb);
        seriesCb.setOnAction(actionEvent -> {
            if (seriesCb.isSelected()) {
                status.seriesSelection.add(Network.ABC, seriesHref);
            } else {
                status.seriesSelection.remove(Network.ABC, seriesHref);
            }
            status.saveSeriesSelection();
            for (SCheckBox cb: cbs) {
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
        this.getChildren().add(topRow);

        LOGGER.fine("Ready to start episodes");
        for (AbcEpisode ep: series.episodes) {
            LOGGER.fine("Episode: " + ep.title);
            String title = ep.title;
            if ((title == null) || (title.trim().equals(""))) {
                title = ep.seriesTitle;
            }
            SCheckBox cb = new SCheckBox();
            cb.network = Network.ABC;
            cb.href = ep.href;
            cb.setDisable(seriesCb.isSelected());
            cb.setSelected(status.selectedEpMap.containsKey(new EpisodeKey(Network.ABC, ep.href)));
            cbs.add(cb);
            String date = "";
            if (ep.pubDate != null) {
                int dateEnd = ep.pubDate.indexOf(' ');
                date = ep.pubDate.substring(0, dateEnd);
            }
            Label pubLabel = new Label(date);
            pubLabel.setMinWidth(80);
            SButton episodeButton = new SButton(title);
            episodeButton.network = Network.ABC;
            episodeButton.href = ep.href;
            episodeButton.setOnAction(e -> {
                panels.scroll.setContent(new EpisodePanel(new SelectedEpisode(Network.ABC, ep.href), status));
            });
            SelectedEpisode se = new SelectedEpisode(cb.network, cb.href);
            VisualEpisode ve = new VisualEpisode();
            ve.setWidgets(episodeButton, cb);
            status.visualEpMap.put(se.key, ve);
            LOGGER.fine("Episode: " + episodeButton.href);
            cb.setOnAction(actionEvent -> {
                LOGGER.fine("Episode CB action " + cb.isSelected());
                if (cb.isSelected()) {
                    status.selectedEpMap.put(se.key, se);
                } else {
                    status.selectedEpMap.remove(se.key);
                }
            });
            if (status.episodeHistory.contains(Network.ABC.historyHref(ep.href))) {
                episodeButton.setTextFill(Color.GRAY);
            }
            HBox row = new HBox(8);
            row.setPadding(new Insets(3));
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(episodeButton, Priority.ALWAYS);
            episodeButton.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().addAll(episodeButton, pubLabel, cb);
            this.getChildren().add(row);
        }
    }
}
