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

import javafx.scene.paint.Color;

public class VisualEpisode {
    private SButton dateOrderButton;
    private SButton nameOrderButton;
    private SCheckBox dateOrderCb;
    private SCheckBox nameOrderCb;

    public void setWidgets(SButton button, SCheckBox cb) {
        dateOrderButton = button;
        dateOrderCb = cb;
    }

    public void setWidgets(SButton button, SCheckBox cb, boolean dateSorted) {
        if (dateSorted) {
            dateOrderButton = button;
            dateOrderCb = cb;
        } else {
            nameOrderButton = button;
            nameOrderCb = cb;
        }
    }

    public boolean isSelectedDisabled() {
        return dateOrderCb.isDisable(); // use isDisable() to be specific about this node
    }

    public void setSelected(boolean selected) {
        dateOrderCb.setSelected(selected);
        if (nameOrderCb != null) {
            nameOrderCb.setSelected(selected);
        }
    }

    public void setGrayed(boolean grayed) {
        if (grayed) {
            dateOrderButton.setTextFill(Color.GRAY);
            if (nameOrderButton != null) {
                nameOrderButton.setTextFill(Color.GRAY);
            }
        } else {
            dateOrderButton.setTextFill(Color.BLACK);
            if (nameOrderButton != null) {
                nameOrderButton.setTextFill(Color.BLACK);
            }
        }
    }

    public void downloadComplete() {
        dateOrderButton.setTextFill(Color.GRAY);
        dateOrderCb.setSelected(false);
        if (nameOrderButton != null) {
            nameOrderButton.setTextFill(Color.GRAY);
            nameOrderCb.setSelected(false);
        }
    }

    @Override
    public String toString() {
        return "VisualEpisode: " + dateOrderButton.getText();
    }
}
