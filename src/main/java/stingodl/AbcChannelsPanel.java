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
import javafx.scene.layout.VBox;

public class AbcChannelsPanel extends VBox {

    public AbcChannelsPanel(Status status, Panels panels, AbcChannels channels) {
        super();

        for (AbcChannelSummary ch: channels.channels) {
            SButton chButton = new SButton(ch.title);
            VBox.setMargin(chButton, new Insets(4));
            chButton.network = Network.ABC;
            chButton.href = ch.href;
            chButton.setAlignment(Pos.CENTER_LEFT);
            chButton.setMaxWidth(Double.MAX_VALUE);
            chButton.setOnAction(actionEvent -> {
                if (chButton.pane == null) {
                    panels.disable();
                    status.searchExecutor.execute(new AbcSeriesTask(status, panels, chButton));
                } else {
                    panels.category.setText(ch.title);
                    panels.category.pane = chButton.pane;
                    panels.category.network = Network.ABC;
                    panels.category.href = chButton.href;
                    panels.scroll.setContent(chButton.pane);
                    panels.enableCategory(true);
               }
            });
            this.getChildren().add(chButton);
        }
    }
}
