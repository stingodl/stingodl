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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.logging.Logger;

public class MainPanel extends VBox {

    static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    Stage mainWindow;
    ScrollPane scroll = new ScrollPane();
    Label currentName = new Label(" ");
    Label currentStatus = new Label();
    ProgressBar currentProgress = new ProgressBar();
    Button downloadSelectedButton = new Button("Download Selected Episodes");
    Button downloadAbcSeriesButton = new Button("ABC Series");
    Button downloadSbsSeriesButton = new Button("SBS Series");
    Button cancelButton = new Button("Cancel");
    Button menuButton;
    SButton networks = new SButton("Networks");
    SButton network = new SButton();
    SButton category = new SButton();
    SButton series = new SButton();
    Panels panels = new Panels();
    Status status = new Status();
    EpisodeDownloader epDownloader;

    public MainPanel(Stage window) {
        super();
        mainWindow = window;
        window.setOnCloseRequest(event -> {
            status.downloadExecutor.shutdown();
            status.searchExecutor.shutdown();
            status.sbsExecutor.shutdown();
            if (epDownloader != null) {
                epDownloader.cancel();
            }
            if (status.sbsTask != null) {
                status.sbsTask.cancel();
            }
        });
        panels.scroll = scroll;
        panels.networks = networks;
        panels.network = network;
        panels.category = category;
        panels.series = series;
        panels.progress = currentProgress;
        panels.currentName = currentName;
        panels.currentStatus = currentStatus;
        panels.downloadSelectedButton = downloadSelectedButton;
        panels.downloadAbcSeriesButton = downloadAbcSeriesButton;
        panels.downloadSbsSeriesButton = downloadSbsSeriesButton;
        status.loadConfig();
        status.loadSbsEpisodes();
        if (!status.isSbsVeryRecent()) {
            status.sbsTask = new SbsTask(status, panels, status.isSbsRecent());
            status.sbsExecutor.execute(status.sbsTask);
        }
        menuButton = new Button(null, new ImageView(new Image("/menu-button.png", true)));
        status.loadHistory();
        status.loadSeriesSelection();

        HBox row1 = new HBox(8);
        row1.setPadding(new Insets(10,5,5,5));
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(downloadSelectedButton);
        downloadSelectedButton.setOnAction(actionEvent -> {
            epDownloader = new EpisodeDownloader(status, EpisodeDownloadType.SELECTED_EPISODE);
            configureEpisodeDownloader();
            status.downloadExecutor.execute(epDownloader);
        });
        row1.getChildren().add(downloadAbcSeriesButton);
        downloadAbcSeriesButton.setOnAction(actionEvent -> {
            epDownloader = new EpisodeDownloader(status, EpisodeDownloadType.ABC_SERIES);
            configureEpisodeDownloader();
            status.downloadExecutor.execute(epDownloader);
        });
        row1.getChildren().add(downloadSbsSeriesButton);
        downloadSbsSeriesButton.setOnAction(actionEvent -> {
            epDownloader = new EpisodeDownloader(status, EpisodeDownloadType.SBS_SERIES);
            configureEpisodeDownloader();
            status.downloadExecutor.execute(epDownloader);
        });
        row1.getChildren().add(cancelButton);
        cancelButton.setOnAction(actionEvent -> {
            if (epDownloader != null) {
                epDownloader.cancel();
            }
        });
        Label padding = new Label();
        padding.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(padding, Priority.ALWAYS);
        row1.getChildren().add(padding);
        row1.getChildren().add(menuButton);
        menuButton.setOnAction(actionEvent -> {
            buildConfig().showAndWait();
        });
        this.getChildren().add(row1);

        HBox row2 = new HBox(8);
        row2.setPadding(new Insets(5));
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(currentName);
        currentProgress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(currentProgress, Priority.ALWAYS);
        currentStatus.setTextFill(Color.WHITE);
        row2.getChildren().add(currentStatus);
        currentProgress.setProgress(0.0);
        row2.getChildren().add(currentProgress);
        this.getChildren().add(row2);

        HBox row3 = new HBox(8);
        row3.setPadding(new Insets(5,5,10,5));
        row3.setAlignment(Pos.CENTER_LEFT);
        networks.pane = new NetworksPanel(status, panels);
        networks.setOnAction(actionEvent -> {
            network.setText("");
            panels.enableNetwork(false);
            category.setText("");
            panels.enableCategory(false);
            series.setText("");
            panels.enableSeries(false);
            if (scroll.getContent() instanceof EpisodePanel) {
                EpisodePanel ep = (EpisodePanel)scroll.getContent();
                ep.stopVideo();
            }
            scroll.setContent(networks.pane);
        });
        row3.getChildren().add(networks);
        network.setDisable(true);
        network.setOnAction(actionEvent -> {
            category.setText("");
            panels.enableCategory(false);
            series.setText("");
            panels.enableSeries(false);
            if (scroll.getContent() instanceof EpisodePanel) {
                EpisodePanel ep = (EpisodePanel)scroll.getContent();
                ep.stopVideo();
            }
            if (Network.SBS.equals(network.network)) {
                scroll.setContent(panels.constructSbsPane(status, network.href));
            } else {
                scroll.setContent(network.pane);
            }
        });
        row3.getChildren().add(network);
        category.setDisable(true);
        category.setOnAction(actionEvent -> {
            series.setText("");
            panels.enableSeries(false);
            if (scroll.getContent() instanceof EpisodePanel) {
                EpisodePanel ep = (EpisodePanel)scroll.getContent();
                ep.stopVideo();
            }
            if (Network.SBS.equals(category.network)) {
                scroll.setContent(panels.constructSbsPane(status, category.href));
            } else {
                scroll.setContent(category.pane);
            }
        });
        row3.getChildren().add(category);
        series.setDisable(true);
        series.setOnAction(actionEvent -> {
            if (scroll.getContent() instanceof EpisodePanel) {
                EpisodePanel ep = (EpisodePanel)scroll.getContent();
                ep.stopVideo();
            }
            if (Network.SBS.equals(series.network)) {
                scroll.setContent(panels.constructSbsPane(status, series.href));
            } else {
                scroll.setContent(series.pane);
            }
        });
        row3.getChildren().add(series);
        this.getChildren().add(row3);

        this.getChildren().add(scroll);
        scroll.setBorder(Border.EMPTY);
        scroll.setFitToWidth(true);
        scroll.setContent(networks.pane);
    }

    private void configureEpisodeDownloader() {
        downloadSelectedButton.setDisable(true);
        downloadAbcSeriesButton.setDisable(true);
        downloadSbsSeriesButton.setDisable(true);
        epDownloader.setOnSucceeded(workerStateEvent -> {
            downloadSelectedButton.setDisable(false);
            downloadAbcSeriesButton.setDisable(false);
            downloadSbsSeriesButton.setDisable(false);
        });
        epDownloader.setOnFailed(workerStateEvent -> {
            downloadSelectedButton.setDisable(false);
            downloadAbcSeriesButton.setDisable(false);
            downloadSbsSeriesButton.setDisable(false);
        });
        epDownloader.setOnCancelled(workerStateEvent -> {
            downloadSelectedButton.setDisable(false);
            downloadAbcSeriesButton.setDisable(false);
            downloadSbsSeriesButton.setDisable(false);
        });
        currentName.textProperty().bind(epDownloader.titleProperty());
        currentStatus.textProperty().bind(epDownloader.messageProperty());
        currentStatus.backgroundProperty().bind(epDownloader.valueProperty());
        currentProgress.progressProperty().bind(epDownloader.progressProperty());
    }

    public Stage buildConfig() {
        Stage dialog = new Stage();
        dialog.setX(mainWindow.getX() + 5);
        dialog.setY(mainWindow.getY() + 5);
        dialog.setTitle("StingoDL Configuration (v " + AAConstants.VERSION + ")");
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(mainWindow);
        dialog.initStyle(StageStyle.UTILITY);
        ConfigDialog cd = new ConfigDialog(dialog, status);
        dialog.setScene(new Scene(cd, 600, 700));
        return dialog;
    }

    public void checkFfmpeg() {
        if (status.config.ffmpegVersion == null) { // not found
            Alert alert = new Alert(Alert.AlertType.WARNING,
                     "No FFmpeg installation found.\nEpisode download will not function until" +
                            " FFmpeg is installed");
            alert.showAndWait();
        }
    }
}
