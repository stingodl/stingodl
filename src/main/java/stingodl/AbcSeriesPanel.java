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
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AbcSeriesPanel extends VBox {

    static final Logger LOGGER = Logger.getLogger(AbcSeriesPanel.class.getName());

    AbcCategory catDetail;
    StackPane cardPanel = new StackPane();
    VBox dateSortedPanel = new VBox();
    VBox nameSortedPanel = new VBox();
    Map<String, SButton[]> buttonMap = new HashMap<>();

    public AbcSeriesPanel(Status status, Panels panels, AbcCategory catDetail) {
        super();
        this.catDetail = catDetail;
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
        });
        nameDateRow.getChildren().add(nameButton);
        dateButton.setDisable(true);
        dateButton.setOnAction(actionEvent -> {
            dateSortedPanel.setVisible(true);
            nameSortedPanel.setVisible(false);
            dateButton.setDisable(true);
            nameButton.setDisable(false);
        });
        Label pad = new Label();
        pad.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pad, Priority.ALWAYS);
        nameDateRow.getChildren().add(pad);
        nameDateRow.getChildren().add(dateButton);
        this.getChildren().add(nameDateRow);
        layoutPanel(status, panels, dateSortedPanel, catDetail.dateOrdered);
        layoutPanel(status, panels, nameSortedPanel, catDetail.nameOrdered);
        nameSortedPanel.setVisible(false);
        cardPanel.getChildren().addAll(dateSortedPanel, nameSortedPanel);
        this.getChildren().add(cardPanel);
    }

    private void layoutPanel(Status status, Panels panels, VBox seriesPanel, List<AbcSeriesSummary> list) {
        for (AbcSeriesSummary series: list) {
            String href = series.href.substring(0, series.href.lastIndexOf('/')).replace("programs", "series");
            SButton[] buttons = buttonMap.get(href);
            if (buttons == null) {
                buttons = new SButton[2];
                buttonMap.put(href, buttons);
            }
            SButton seriesButton = new SButton(series.seriesTitle);
            if (buttons[0] == null) {
                buttons[0] = seriesButton;
            } else {
                buttons[1] = seriesButton;
            }
            seriesButton.network = Network.ABC;
            seriesButton.href = href;
            seriesButton.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(seriesButton, Priority.ALWAYS);
            seriesButton.setOnAction(actionEvent -> {
                if (seriesButton.pane == null) {
                    panels.disable();
                    status.searchExecutor.execute(new AbcEpisodesTask(status, panels, buttonMap.get(seriesButton.href)));
                } else {
                    panels.series.setText(series.seriesTitle);
                    panels.series.network = Network.ABC;
                    panels.series.href = seriesButton.href;
                    panels.series.pane = seriesButton.pane;
                    panels.scroll.setContent(seriesButton.pane);
                    panels.enableSeries(true);
                }
            });
            Label dateLabel = new Label(series.pubDate.substring(0,10));
            dateLabel.setMinWidth(80);
            HBox row = new HBox(8);
            row.setPadding(new Insets(3));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(seriesButton, dateLabel);
            seriesPanel.getChildren().add(row);
        }
    }
}
