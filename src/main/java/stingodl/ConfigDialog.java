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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigDialog extends VBox {

    Window window;
    Status status;
    Label ffmpegLabel = new Label("FFmpeg Version:");
    Label ffmpegLocation = new Label();
    Label downloadLabel = new Label("Download location:");
    Button downloadSelect = new Button("Select");
    Label downloadLocation = new Label();
    Label sbsVeryRecentMinutesLabel =
            new Label("Minutes after a full SBS update to suppress a quick update:");
    TextField sbsVeryRecentText = new TextField();
    Label sbsRecentHoursLabel =
            new Label("Hours after a full SBS update to suppress another full update:");
    TextField sbsRecentText = new TextField();
    Label linuxText = new Label("Linux users should install ffmpeg through their package manager.");
    Button uninstall = new Button("Delete all configuration and history.");
    CheckBox logCb = new CheckBox();
    Label logLabel = new Label("Turn on detailed logging");

    public ConfigDialog(Window window, Status status) {
        this.window = window;
        this.status = status;
        Font sys = ffmpegLabel.getFont();
        Font bigger = Font.font(sys.getFamily(), FontWeight.NORMAL, 16.0);
        ffmpegLabel.setFont(bigger);
        status.config.ffmpegText = status.testFFmpeg();
        ffmpegLocation.setText(status.config.ffmpegText);
        downloadLabel.setFont(bigger);
        if (status.config.downloadDir == null) {
            downloadLocation.setText(System.getProperty("user.home"));
        } else {
            downloadLocation.setText(status.config.downloadDir);
        }

        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(ffmpegLabel, new Insets(10, 0, 0, 10));
        row1.getChildren().add(ffmpegLabel);
        HBox.setMargin(ffmpegLocation, new Insets(10, 0, 0, 10));
        row1.getChildren().add(ffmpegLocation);
        getChildren().add(row1);

        HBox row2 = new HBox();
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(downloadLabel, new Insets(10, 0, 0, 10));
        row2.getChildren().add(downloadLabel);
        HBox.setMargin(downloadLocation, new Insets(10, 0, 0, 10));
        row2.getChildren().add(downloadLocation);
        HBox.setMargin(downloadSelect, new Insets(10, 0, 0, 20));
        row2.getChildren().add(downloadSelect);
        downloadSelect.setOnAction(actionEvent -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File f = chooser.showDialog(window);
            if (f != null) {
                status.config.downloadDir = f.toString();
                downloadLocation.setText(status.config.downloadDir);
                status.saveConfig();
                }
        });
        getChildren().add(row2);

        sbsVeryRecentText.setPrefColumnCount(5);
        sbsVeryRecentText.setText(Long.toString(status.config.sbsQuickUpdateMinutes));
        sbsVeryRecentText.setTextFormatter(new TextFormatter<>(c -> {
            if (c.isContentChange()) {
                if (c.getControlNewText().length() == 0) {
                    return c;
                }
                try {
                    Integer.parseInt(c.getControlNewText());
                    return c;
                } catch (NumberFormatException e) {
                }
                return null;

            }
            return c;
        }));
        sbsVeryRecentText.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) { //lost focus
                status.config.sbsQuickUpdateMinutes = 0;
                if (sbsVeryRecentText.getText().length() > 0) {
                    status.config.sbsQuickUpdateMinutes = Long.parseLong(sbsVeryRecentText.getText());
                }
                status.saveConfig();
            }
        });
        sbsRecentText.setPrefColumnCount(5);
        sbsRecentText.setText(Long.toString(status.config.sbsFullUpdateHours));
        sbsRecentText.setTextFormatter(new TextFormatter<>(c -> {
            if (c.isContentChange()) {
                if (c.getControlNewText().length() == 0) {
                    return c;
                }
                try {
                    Integer.parseInt(c.getControlNewText());
                    return c;
                } catch (NumberFormatException e) {
                }
                return null;

            }
            return c;
        }));
        sbsRecentText.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) { //lost focus
                status.config.sbsFullUpdateHours = 0;
                if (sbsRecentText.getText().length() > 0) {
                    status.config.sbsFullUpdateHours = Long.parseLong(sbsRecentText.getText());
                }
                status.saveConfig();
            }
        });

        HBox row3 = new HBox();
        row3.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(sbsVeryRecentMinutesLabel, new Insets(15, 0, 0, 10));
        HBox.setMargin(sbsVeryRecentText, new Insets(15, 0, 0, 10));
        row3.getChildren().addAll(sbsVeryRecentMinutesLabel, sbsVeryRecentText);
        getChildren().add(row3);

        HBox row4 = new HBox();
        row4.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(sbsRecentHoursLabel, new Insets(15, 0, 0, 10));
        HBox.setMargin(sbsRecentText, new Insets(15, 0, 0, 10));
        row4.getChildren().addAll(sbsRecentHoursLabel, sbsRecentText);
        getChildren().add(row4);

        if (status.config.os.equals("other")) {
            HBox rowLinux = new HBox();
            rowLinux.setAlignment(Pos.CENTER_LEFT);
            HBox.setMargin(linuxText, new Insets(10, 0, 0, 10));
            rowLinux.getChildren().add(linuxText);
            getChildren().add(rowLinux);
        }

        HBox row5 = new HBox();
        row5.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(logCb, new Insets(15, 0, 0, 10));
        logCb.setOnAction(actionEvent ->  {
            configureLogging();
        });
        row5.getChildren().add(logCb);
        HBox.setMargin(logLabel, new Insets(15, 0, 0, 10));
        row5.getChildren().add(logLabel);
        getChildren().add(row5);

        HBox row6 = new HBox();
        row6.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(uninstall, new Insets(25, 0, 0, 10));
        row6.getChildren().add(uninstall);
        uninstall.setOnAction(actionEvent -> {
            Dialog<ButtonType> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setContentText("This will remove configuration and all history,\n" +
                    "and cannot be undone.\nDo you wish to continue?");
            d.initOwner(window);
            d.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean userFiles = false;
                    File[] configFiles = status.dataDir.listFiles();
                    for (File f: configFiles) {
                        if (f.getName().endsWith(".json")) {
                            f.delete();
                        } else {
                            userFiles = true;
                        }
                    }
                    if (!userFiles) {
                        status.dataDir.delete();
                    }
                    System.exit(0);
                }
            });
        });
        getChildren().add(row6);
    }

    public void configureLogging() {
        Logger rootLogger = Logger.getLogger("stingodl");
        FileHandler fileHandler = null;
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler: handlers) {
            if (handler instanceof FileHandler) {
                fileHandler = (FileHandler)handler;
                break;
            }
        }
        if (logCb.isSelected()) {
            if (fileHandler != null) {
                fileHandler.setLevel(Level.FINE);
            }
        } else {
            if (fileHandler != null) {
                fileHandler.setLevel(Level.INFO);
            }
        }
    }
}
