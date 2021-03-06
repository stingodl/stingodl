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
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SbsFilmsByExpiryPanel extends VBox {

    Status status;
    Panels panels;

    public SbsFilmsByExpiryPanel(Status status, Panels panels) {
        super();
        this.status = status;
        this.panels = panels;

        List<String> labels = new ArrayList<>(status.sbsEpisodes.filmsExpMap.keySet());
        Collections.sort(labels);
        for (String s: labels) {
            SButton expButton = new SButton(s);
            expButton.network = Network.SBS;
            expButton.href = SbsHref.filmsByExpiryDate + "/" + s;
            expButton.setOnAction(actionEvent -> {
                    panels.series.setText(expButton.getText());
                    panels.series.network = expButton.network;
                    panels.series.href = expButton.href;
                    panels.enableSeries(true);
                    panels.scroll.setContent(panels.constructSbsPane(status, expButton.href));
            });
            VBox.setMargin(expButton, new Insets(3));
            expButton.setAlignment(Pos.CENTER_LEFT);
            expButton.setMaxWidth(Double.MAX_VALUE);
            this.getChildren().add(expButton);
        }
    }
}
