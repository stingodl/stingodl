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

public class SbsFilmsByPubPanel extends VBox {

    Status status;
    Panels panels;

    public SbsFilmsByPubPanel(Status status, Panels panels) {
        super();
        this.status = status;
        this.panels = panels;

        List<String> labels = new ArrayList<>(status.sbsEpisodes.filmsPubMap.keySet());
        Collections.sort(labels, new ReverseDateComp());
        for (String s: labels) {
            SButton pubButton = new SButton(s);
            pubButton.network = Network.SBS;
            pubButton.href = SbsHref.filmsByPublishDate + "/" + s;
            pubButton.setOnAction(actionEvent -> {
                    panels.series.setText(pubButton.getText());
                    panels.series.network = pubButton.network;
                    panels.series.href = pubButton.href;
                    panels.enableSeries(true);
                    panels.scroll.setContent(panels.constructSbsPane(status, pubButton.href));
            });
            VBox.setMargin(pubButton, new Insets(3));
            pubButton.setAlignment(Pos.CENTER_LEFT);
            pubButton.setMaxWidth(Double.MAX_VALUE);
            this.getChildren().add(pubButton);
        }
    }

    public static class ReverseDateComp implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    }
}
