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
import javafx.scene.layout.Pane;

import java.net.URI;
import java.util.logging.Logger;

public class AbcEpisodesTask extends Task<AbcEpisodesPanel> {

    static final Logger LOGGER = Logger.getLogger(AbcEpisodesTask.class.getName());

    Status status;
    Panels panels;
    SButton[] buttons;

    public AbcEpisodesTask(Status status, Panels panels, SButton[] buttons) {
        this.status = status;
        this.panels = panels;
        this.buttons = buttons;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        Pane episodesPane = getValue();
        for (SButton sb: buttons) {
            sb.pane = episodesPane;
        }
        Platform.runLater(() -> {
            panels.series.setText(buttons[0].getText());
            panels.series.network = Network.ABC;
            panels.series.href = buttons[0].href;
            panels.series.pane = episodesPane;
            panels.scroll.setContent(episodesPane);
            panels.enableSeries(true);
            panels.enable();
        });
    }

    @Override
    protected void failed() {
        super.failed();
    }

    @Override
    protected AbcEpisodesPanel call() throws Exception {
        LOGGER.fine("Starting Epiosodes: " + buttons[0].href);
        URI uri = new URI("https://iview.abc.net.au/api/" + buttons[0].href);
        AbcSeries series = JsonConstructiveParser.parse(status.httpInput.getReader(uri), AbcSeries.class);
        LOGGER.fine("Series: " + series.toString());
        return new AbcEpisodesPanel(status, panels, series, buttons[0].href);
    }
}
