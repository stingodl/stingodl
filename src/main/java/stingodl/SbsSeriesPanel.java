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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.logging.Logger;

public class SbsSeriesPanel extends VBox {

    static final Logger LOGGER = Logger.getLogger(SbsSeriesPanel.class.getName());

    SbsGenre genre;
    StackPane cardPanel = new StackPane();
    VBox dateSortedPanel = new VBox();
    VBox nameSortedPanel = new VBox();

    public SbsSeriesPanel(Status status, Panels panels, SbsGenre genre) {
        super();
        this.genre = genre;
        HBox nameDateRow = new HBox();
        nameDateRow.setMaxWidth(Double.MAX_VALUE);
        Button nameButton = new Button("Sort by Name");
        Button dateButton = new Button("Sort by Date");
        HBox.setMargin(nameButton, new Insets(6,4,6,4));
        HBox.setMargin(dateButton, new Insets(6,4,6,4));
        nameButton.setOnAction(actionEvent -> {
            nameSortedPanel.setVisible(true);
            dateSortedPanel.setVisible(false);
            nameButton.setDisable(true);
            dateButton.setDisable(false);
            genre.dateSorted = false;
        });
        nameDateRow.getChildren().add(nameButton);
        dateButton.setOnAction(actionEvent -> {
            dateSortedPanel.setVisible(true);
            nameSortedPanel.setVisible(false);
            dateButton.setDisable(true);
            nameButton.setDisable(false);
            genre.dateSorted = true;
        });
        Label pad = new Label();
        pad.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pad, Priority.ALWAYS);
        nameDateRow.getChildren().add(pad);
        nameDateRow.getChildren().add(dateButton);
        this.getChildren().add(nameDateRow);
        layoutPanel(status, panels, dateSortedPanel, genre.getDateSortedSeries());
        layoutPanel(status, panels, nameSortedPanel, genre.getNameSortedSeries());
        if (genre.dateSorted) {
            dateButton.setDisable(true);
            nameSortedPanel.setVisible(false);
        } else {
            nameButton.setDisable(true);
            dateSortedPanel.setVisible(false);
        }
        cardPanel.getChildren().addAll(dateSortedPanel, nameSortedPanel);
        this.getChildren().add(cardPanel);
    }

    private void layoutPanel(Status status, Panels panels, VBox seriesPanel, List<SbsSeries> list) {
        for (SbsSeries series: list) {
            SButton seriesButton = new SButton(series.series);
            seriesButton.network = Network.SBS;
            seriesButton.href = SbsHref.series + "/" + series.seriesKey;
            seriesButton.setMaxWidth(Double.MAX_VALUE);
            seriesButton.setOnAction(actionEvent -> {
                SbsSeries s = status.sbsEpisodes.seriesMap.get(
                        seriesButton.href.substring(SbsHref.series.name().length() + 1));
                panels.series.setText(s.series);
                panels.enableSeries(true);
                panels.series.network = seriesButton.network;
                panels.series.href = seriesButton.href;
                panels.scroll.setContent(panels.constructSbsPane(status, seriesButton.href));
            });
            Label dateLabel = new Label(series.latestDate.substring(0,10));
            dateLabel.setMinWidth(80);
            HBox row = new HBox(8);
            row.setPadding(new Insets(3));
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(seriesButton, Priority.ALWAYS);
            row.getChildren().addAll(seriesButton, dateLabel);
            seriesPanel.getChildren().add(row);
        }
    }
}
