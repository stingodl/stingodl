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

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.net.URI;
import java.util.logging.Logger;

public class AbcCategoriesTask extends Task<AbcCategoriesPanel> {

    static final Logger LOGGER = Logger.getLogger(AbcCategoriesTask.class.getName());

    Panels panels;
    Status status;
    SButton button;

    public AbcCategoriesTask(Status status, Panels panels, SButton button) {
        this.panels = panels;
        this.status = status;
        this.button = button;
    }

    @Override
    protected AbcCategoriesPanel call() throws Exception {
        LOGGER.fine("Starting ABC cat");
        URI uri = new URI("https://iview.abc.net.au/api/category");
        AbcCategories abcCats = JsonConstructiveParser.parse(status.httpInput.getReader(uri), AbcCategories.class);
        abcCats.toCamelCase();
        return new AbcCategoriesPanel(status, panels, abcCats);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        button.pane = getValue();
        Platform.runLater(() -> {
            panels.network.setText(button.getText());
            panels.network.network = Network.ABC;
            panels.network.href = button.href;
            panels.network.pane = button.pane;
            panels.networkEnabled = true;
            panels.scroll.setContent(button.pane);
            panels.enable();
        });
    }

    @Override
    protected void failed() {
        super.failed();
    }
}
