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

public class AbcCategoriesPanel extends VBox {

    public AbcCategoriesPanel(Status status, Panels panels, AbcCategories categories) {
        super();

        for (AbcCategorySummary cat: categories.categories) {
            SButton catButton = new SButton(cat.title);
            VBox.setMargin(catButton, new Insets(4));
            catButton.network = Network.ABC;
            catButton.href = cat.href;
            catButton.setAlignment(Pos.CENTER_LEFT);
            catButton.setMaxWidth(Double.MAX_VALUE);
            catButton.setOnAction(actionEvent -> {
                if (catButton.pane == null) {
                    panels.disable();
                    status.searchExecutor.execute(new AbcSeriesTask(status, panels, catButton));
                } else {
                    panels.category.setText(cat.title);
                    panels.category.network = Network.ABC;
                    panels.category.href = catButton.href;
                    panels.category.pane = catButton.pane;
                    panels.scroll.setContent(catButton.pane);
                    panels.enableCategory(true);
                }
            });
            this.getChildren().add(catButton);
        }
    }
}
