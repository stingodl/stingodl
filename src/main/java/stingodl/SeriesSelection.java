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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeriesSelection implements Cloneable {
    public List<SelectedSeries> series;

    public synchronized void clean() {
        List<SelectedSeries> removeThese = new ArrayList<>();
        for (SelectedSeries s: series) {
            if (s.href == null) {
                removeThese.add(s);
            }
        }
        for (SelectedSeries s: removeThese) {
            series.remove(s);
        }
    }

    public synchronized boolean add(Network network, String href) {
        if (href == null) {
            return false;
        }
        SelectedSeries newSeries = new SelectedSeries(network, href);
        for (SelectedSeries s: series) {
            if (s.equals(newSeries)) {
                return false;
            }
        }
        series.add(newSeries);
        return true;
    }

    public synchronized boolean remove(Network network, String href) {
        SelectedSeries oldSeries = new SelectedSeries(network, href);
        for (SelectedSeries s: series) {
            if (s.equals(oldSeries)) {
                series.remove(s);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isSelected(Network network, String href) {
        SelectedSeries selSeries = new SelectedSeries(network, href);
        for (SelectedSeries s: series) {
            if (s.equals(selSeries)) {
                return true;
            }
        }
        return false;
    }

    public synchronized List<SelectedSeries> sort() {
        ArrayList<SelectedSeries> clone = new ArrayList<>(series);
        Collections.sort(clone);
        return clone;
    }

    @Override
    public synchronized String toString() {
        if (series == null) {
            return "";
        } else {
            return series.toString();
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Object clone() throws CloneNotSupportedException {
        SeriesSelection sel = new SeriesSelection();
        if (series != null) {
            sel.series = (List<SelectedSeries>)((ArrayList<SelectedSeries>)series).clone();
        }
        return sel;
    }
}
