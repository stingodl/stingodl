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

public class AbcSeriesTask extends Task<AbcSeriesPanel> {

    static final Logger LOGGER = Logger.getLogger(AbcSeriesTask.class.getName());

    Status status;
    Panels panels;
    SButton button;

    public AbcSeriesTask(Status status, Panels panels, SButton button) {
        this.status = status;
        this.panels = panels;
        this.button = button;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        button.pane = getValue();
        Platform.runLater(() -> {
            panels.category.setText(button.getText());
            panels.category.network = Network.ABC;
            panels.category.href = button.href;
            panels.category.pane = button.pane;
            panels.scroll.setContent(button.pane);
            panels.categoryEnabled = true;
            panels.enable();
        });
    }

    @Override
    protected void failed() {
        super.failed();
    }

    @Override
    protected AbcSeriesPanel call() throws Exception {
        URI uri = new URI("https://iview.abc.net.au/api/" + button.href);
        AbcCategory catDetail = JsonConstructiveParser.parse(status.httpInput.getReader(uri), AbcCategory.class);
        LOGGER.fine("SeriesWorker: href=" + button.href + " " + catDetail.toString());
        catDetail.orderLists();
        return new AbcSeriesPanel(status, panels, catDetail);
    }

}
