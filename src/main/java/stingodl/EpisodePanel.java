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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EpisodePanel extends VBox {

    static final Logger LOGGER = Logger.getLogger(EpisodePanel.class.getName());

    SelectedEpisode se;
    Status status;
    Label title = new Label();
    Label desc = new Label();
    ImageView thumbnail = null;
    Label originLabel = new Label("  Origion:");
    Label origin = new Label();
    Label languageLabel = new Label("  Language:");
    Label language = new Label();
    Label expiryLabel = new Label("  Expires:");
    Label expiry = new Label();
    Label durationLabel = new Label("Duration:");
    Label duration = new Label();
    HBox details = new HBox(8);
    Label downloadLabel = new Label("Select episode for download");
    CheckBox downloadCb = new CheckBox();
    Label downloadedLabel = new Label("     Mark episode as downloaded");
    CheckBox downloadedCb = new CheckBox();
    HBox cbs = new HBox(8);
    Button preview = new Button("Preview Video");
    Button play = new Button();
    boolean playState = true;
    MediaPlayer player = null;
    MediaView view = null;
    Slider slider = new Slider();
    int changedSlider = 0;
    boolean changing = false;
    double newSlider = 0.0;

    public EpisodePanel(SelectedEpisode se, Status status) {
        this.se = se;
        this.status = status;
        // this weird hack is necessary to get the scroll pane to size properly
        desc.heightProperty().addListener((observableValue, number, t1) -> desc.setPrefHeight(t1.doubleValue()));
        this.setAlignment(Pos.TOP_CENTER);
        Font sys = title.getFont();
        title.setFont(Font.font(sys.getFamily(), FontWeight.NORMAL, 18.0));
        VBox.setMargin(desc, new Insets(8));
        desc.setWrapText(true);
        if (Network.ABC.equals(se.key.network)) {
            M3u8.addAbcDetails(se, status);
        }
        title.setText(se.title);
        desc.setText(se.description);
        if (se.thumbnailUrl != null) {
            thumbnail = new ImageView(new Image(se.thumbnailUrl, 640, 360, true, true, true));
            VBox.setMargin(thumbnail, new Insets(8,0,0,0));
        }
        this.getChildren().addAll(title, desc);
        expiry.setText(se.expiry.substring(0,10));
        duration.setText(se.duration);
        details.getChildren().addAll(durationLabel, duration, expiryLabel, expiry);
        if ((se.countryOfOrigin != null) || (se.language != null)) {
            origin.setText(se.countryOfOrigin);
            language.setText(se.language);
            details.getChildren().addAll(originLabel, origin, languageLabel, language);
        }
        VBox.setMargin(details, new Insets(0, 8, 0, 8));
        this.getChildren().add(details);
        VisualEpisode ve = status.visualEpMap.get(se.key);
        if (ve != null) {
            if (ve.isSelectedDisabled()) {
                downloadCb.setDisable(true);
            } else {
                downloadCb.setSelected(status.selectedEpMap.containsKey(se.key));
                downloadCb.setOnAction(actionEvent -> {
                    if (downloadCb.isSelected()) {
                        status.selectedEpMap.put(se.key, se);
                        ve.setSelected(true);
                    } else {
                        status.selectedEpMap.remove(se.key);
                        ve.setSelected(false);
                    }
                });
            }
        }
        String epHistory = se.key.network.historyHref(se.key.href);
        downloadedCb.setSelected(status.episodeHistory.contains(epHistory));
        downloadedCb.setOnAction(actionEvent -> {
            if (downloadedCb.isSelected()) {
                status.episodeHistory.add(epHistory);
                if (ve != null) {
                    ve.setGrayed(true);
                }
            } else {
                status.episodeHistory.remove(epHistory);
               if (ve != null) {
                    ve.setGrayed(false);
                }
            }
            status.saveHistory();
        });
        cbs.getChildren().addAll(downloadLabel, downloadCb, downloadedLabel, downloadedCb);
        VBox.setMargin(cbs, new Insets(6, 8,8, 8));
        this.getChildren().add(cbs);
        if (status.config.os.equals("macos64") && Network.SBS.equals(se.key.network)) {
            // everything else does not work, including ABC on Mac OS for now
            this.getChildren().add(preview);
            preview.setOnAction(e -> {
                if (thumbnail != null) {
                    this.getChildren().remove(thumbnail);
                }
                if (Network.ABC.equals(se.key.network)) {
                    M3u8.findAbcStreams(se, status);
                } else if (Network.SBS.equals(se.key.network)) {
                    M3u8.findSbsStreams(se, status);
                }
                buildPreview(se);
            });
        }
        if (thumbnail != null) {
            this.getChildren().add(thumbnail);
        }
   }

    public void stopVideo() {
        if (player != null) {
            player.stop();
        }
    }

    private void buildPreview(SelectedEpisode se) {
        if (se.streams == null) {
            return;
        }
        String url = M3u8.getResolutionUpTo(360, false, se.streams).url;
        LOGGER.fine("Preview URL: " + url);
        if (url != null) {
            try {
                Media media = new Media(url);
                player = new MediaPlayer(media);
                view = new MediaView(player);
                view.setFitHeight(360);
                view.setFitWidth(640);
                view.setOnError(mediaErrorEvent -> {
                    LOGGER.log(Level.SEVERE, "MediaView Error", mediaErrorEvent.getMediaError());
                });
                player.setOnReady(() -> {
                    slider.setMax(0.0);
                    slider.setMax(player.getTotalDuration().toSeconds());
                    slider.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> {
                        changing = t1;
                        if (!changing) {
                            newSlider = changedSlider; //record the new slider position
                            player.seek(Duration.seconds(newSlider));
                        }
                    });
                    slider.valueProperty().addListener((observableValue, number, t1) -> {
                        if (changing) {
                            changedSlider = t1.intValue();
                        } else {
                            double change = Math.abs(t1.doubleValue() - number.doubleValue());
                            if (change > 2.0) { // This is more than the normal 'currentTime' change
                                newSlider = t1.intValue();
                                player.seek(Duration.seconds(t1.intValue()));
                            }
                        }
                    });
                    player.currentTimeProperty().addListener((observableValue, duration, t1) -> {
                        if (!changing) { // do nothing while the slider is changing
                            if (newSlider == 0.0) { // no seek is pending
                                slider.setValue(t1.toSeconds());
                            } else { // a seek is pending
                                double currentSeconds = t1.toSeconds();
                                double change = Math.abs(currentSeconds - newSlider);
                                if (change < 1.0) { // the seek has been actioned
                                    newSlider = 0.0;
                                    slider.setValue(t1.toSeconds());
                                } // the seek has not been actioned yet, so do nothing
                            }
                        }
                    });
                });
                play.setText("Play");
                play.setOnAction(e -> {
                    if (player != null) {
                        if (playState) {
                            player.play();
                            play.setText("Pause");
                        } else {
                            player.pause();
                            play.setText("Play");
                        }
                        playState = !playState;
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Preview failed", e);
            }
            HBox playControl = new HBox(8);
            playControl.setAlignment(Pos.CENTER);
            HBox.setHgrow(slider, Priority.ALWAYS);
            playControl.getChildren().addAll(play, slider);
            VBox.setMargin(playControl, new Insets(8));
            this.getChildren().remove(preview);
            this.getChildren().addAll(playControl, view);
        }
    }
    @SuppressWarnings("unchecked")
    private void printStack(Throwable e) {
        System.out.println("*");
        System.out.println(e.getMessage());
        StackTraceElement[] stes = e.getStackTrace();
        for (StackTraceElement ste: stes) {
            System.out.println(ste);
        }
        if (e instanceof Exception) {
            Exception ex = (Exception)e;
            if (ex.getCause() != null) {
                printStack(ex.getCause());
            }
        }
    }
}
