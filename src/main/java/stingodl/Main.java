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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {

    static {
        LogManager.getLogManager().reset();
        System.setProperty("java.util.logging.SimpleFormatter.format","%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %3$s %4$s: %5$s%n %6$s");
        Logger rootLogger = Logger.getLogger("stingodl");
        rootLogger.setLevel(Level.FINE);
    }
    static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage stage) throws Exception{
        stage.setTitle("StingoDL");
        Image icon16 = new Image(this.getClass().getResource("/icon_16.png").toString(),true);
        Image icon32 = new Image(this.getClass().getResource("/icon_32.png").toString(),true);
        Image icon64 = new Image(this.getClass().getResource("/icon_64.png").toString(),true);
        stage.getIcons().addAll(icon16, icon32, icon64);
        MainPanel mainPanel = new MainPanel(stage);
        stage.setScene(new Scene(mainPanel, 710, 710));
        stage.show();
        mainPanel.checkFfmpeg();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Level level = null;
            try {
                level = Level.parse(args[0]);
            } catch (IllegalArgumentException iae) {}
            if (level != null) {
                ConsoleHandler console = new ConsoleHandler();
                console.setLevel(level);
                Logger root = Logger.getLogger("stingodl");
                root.setLevel(level);
                root.addHandler(console);
            }
        }
        launch(args);
    }
}
