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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class NetworksPanel extends VBox {

    SButton abcCat = new SButton("ABC by Category");
    SButton abcCh = new SButton("ABC by Channel");
    SButton sbsProg = new SButton("SBS Programs");
    SButton sbsFilm = new SButton("SBS Films");

    public NetworksPanel(Status status, Panels panels) {
        super();
        VBox.setMargin(abcCat, new Insets(4));
        abcCat.setAlignment(Pos.CENTER_LEFT);
        abcCat.setMaxWidth(Double.MAX_VALUE);
        abcCat.network = Network.ABC;
        abcCat.setOnAction(actionEvent -> {
            if (abcCat.pane == null) {
                panels.disable();
                status.searchExecutor.execute(new AbcCategoriesTask(status, panels, abcCat));
            } else {
                panels.network.setText(abcCat.getText());
                panels.network.pane = abcCat.pane;
                panels.network.network = abcCat.network;
                panels.scroll.setContent(abcCat.pane);
                panels.enableNetwork(true);
            }
        });
        VBox.setMargin(abcCh, new Insets(4));
        abcCh.setAlignment(Pos.CENTER_LEFT);
        abcCh.setMaxWidth(Double.MAX_VALUE);
        abcCh.network = Network.ABC;
        abcCh.setOnAction(actionEvent -> {
            if (abcCh.pane == null) {
                panels.disable();
                status.searchExecutor.execute(new AbcChannelsTask(status, panels, abcCh));
            } else {
                panels.network.setText(abcCh.getText());
                panels.network.pane = abcCh.pane;
                panels.network.network = abcCat.network;
                panels.scroll.setContent(abcCh.pane);
                panels.enableNetwork(true);
            }
        });
        panels.abcBox.setAlignment(Pos.CENTER_LEFT);
        panels.abcButtons.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(panels.abcButtons, Priority.ALWAYS);
        panels.abcButtons.getChildren().addAll(abcCat, abcCh);
        panels.abcUpdate.setOnAction(actionEvent -> {
            abcCat.pane = null;
            abcCh.pane = null;
        });
        HBox.setMargin(panels.abcUpdate, new Insets(4));

        VBox.setMargin(sbsProg, new Insets(4));
        sbsProg.setAlignment(Pos.CENTER_LEFT);
        sbsProg.setMaxWidth(Double.MAX_VALUE);
        sbsProg.network = Network.SBS;
        sbsProg.href = SbsHref.programs.name();
        sbsProg.setOnAction(actionEvent ->  {
            panels.network.setText(sbsProg.getText());
            panels.network.network = sbsProg.network;
            panels.network.href = sbsProg.href;
            panels.network.pane = null;
            panels.scroll.setContent(panels.constructSbsPane(status, sbsProg.href));
            panels.enableNetwork(true);
        });
        VBox.setMargin(sbsFilm, new Insets(4));
        sbsFilm.setAlignment(Pos.CENTER_LEFT);
        sbsFilm.setMaxWidth(Double.MAX_VALUE);
        sbsFilm.network = Network.SBS;
        sbsFilm.href = SbsHref.films.name();
        sbsFilm.setOnAction(actionEvent ->  {
            panels.network.setText(sbsFilm.getText());
            panels.network.network = sbsFilm.network;
            panels.network.href = sbsFilm.href;
            panels.network.pane = null;
            panels.scroll.setContent(panels.constructSbsPane(status, sbsFilm.href));
            panels.enableNetwork(true);
        });
        panels.sbsBox.setAlignment(Pos.CENTER_LEFT);
        panels.sbsButtons.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(panels.sbsButtons, Priority.ALWAYS);
        panels.sbsButtons.getChildren().addAll(sbsProg, sbsFilm);
        HBox.setMargin(panels.sbsUpdate, new Insets(4));
        panels.sbsQuickUpdate.setOnAction(actionEvent -> {
            status.sbsTask = new SbsTask(status, panels, true);
            status.sbsExecutor.execute(status.sbsTask);
        });
        panels.sbsFullUpdate.setOnAction(actionEvent -> {
            status.sbsTask = new SbsTask(status, panels, false);
            status.sbsExecutor.execute(status.sbsTask);
        });
        HBox.setMargin(panels.sbsQuickUpdate, new Insets(4));
        HBox.setMargin(panels.sbsFullUpdate, new Insets(4));
        panels.sbsBox.getChildren().addAll(panels.sbsQuickUpdate, panels.sbsFullUpdate);
        getChildren().addAll(panels.abcBox, panels.sbsBox);
    }
}
