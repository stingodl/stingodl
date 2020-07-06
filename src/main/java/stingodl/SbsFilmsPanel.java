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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SbsFilmsPanel extends VBox {

    Status status;
    Panels panels;

    public SbsFilmsPanel(Status status, Panels panels) {
        super();
        this.status = status;
        this.panels = panels;
        List<SbsGenre> genres = new ArrayList<>(status.sbsEpisodes.filmsMap.values());
        Collections.sort(genres);

        SButton aToZButton = new SButton("Films A to Z");
        Font sys = aToZButton.getFont();
        aToZButton.setFont(Font.font(sys.getFamily(), FontWeight.NORMAL, 16.0));
        aToZButton.network = Network.SBS;
        aToZButton.href = SbsHref.filmsAtoZ.name();
        aToZButton.setOnAction(actionEvent -> {
            panels.category.setText("Films A to Z");
            panels.category.network = aToZButton.network;
            panels.category.href = aToZButton.href;
            panels.enableCategory(true);
            panels.scroll.setContent(panels.constructSbsPane(status, aToZButton.href));
        });
        VBox.setMargin(aToZButton, new Insets(3));
        aToZButton.setAlignment(Pos.CENTER_LEFT);
        aToZButton.setMaxWidth(Double.MAX_VALUE);
        this.getChildren().add(aToZButton);

        SButton pubButton = new SButton("Films by published date");
        pubButton.setFont(Font.font(sys.getFamily(), FontWeight.NORMAL, 16.0));
        pubButton.network = Network.SBS;
        pubButton.href = SbsHref.filmsByPublishDate.name();
        pubButton.setOnAction(actionEvent -> {
            panels.category.setText("Films by published date");
            panels.category.network = pubButton.network;
            panels.category.href = pubButton.href;
            panels.enableCategory(true);
            panels.scroll.setContent(panels.constructSbsPane(status, pubButton.href));
        });
        VBox.setMargin(pubButton, new Insets(3));
        pubButton.setAlignment(Pos.CENTER_LEFT);
        pubButton.setMaxWidth(Double.MAX_VALUE);
        this.getChildren().add(pubButton);

        SButton expireButton = new SButton("Films by expiry date");
        expireButton.setFont(Font.font(sys.getFamily(), FontWeight.NORMAL, 16.0));
        expireButton.network = Network.SBS;
        expireButton.href = SbsHref.filmsByExpiryDate.name();
        expireButton.setOnAction(actionEvent -> {
            panels.category.setText("Films by expiry date");
            panels.category.network = expireButton.network;
            panels.category.href = expireButton.href;
            panels.enableCategory(true);
            panels.scroll.setContent(panels.constructSbsPane(status, expireButton.href));
        });
        VBox.setMargin(expireButton, new Insets(3));
        expireButton.setAlignment(Pos.CENTER_LEFT);
        expireButton.setMaxWidth(Double.MAX_VALUE);
        this.getChildren().add(expireButton);

        for (SbsGenre genre: genres) {
            SButton genreButton = new SButton(genre.genre);
            genreButton.network = Network.SBS;
            genreButton.href = SbsHref.filmGenre + "/" + genre.genre;
            genreButton.setOnAction(actionEvent -> {
                    panels.category.setText(genre.genre);
                    panels.category.network = genreButton.network;
                    panels.category.href = genreButton.href;
                    panels.enableCategory(true);
                    panels.scroll.setContent(panels.constructSbsPane(status, genreButton.href));
            });
            VBox.setMargin(genreButton, new Insets(3));
            genreButton.setAlignment(Pos.CENTER_LEFT);
            genreButton.setMaxWidth(Double.MAX_VALUE);
            this.getChildren().add(genreButton);
        }
    }
}
