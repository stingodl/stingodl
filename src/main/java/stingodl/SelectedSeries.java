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

import java.time.LocalDate;
import java.util.List;

public class SelectedSeries implements Comparable<SelectedSeries> {
    public String network;
    public String href;
    public String date;

    public SelectedSeries() {
    }

    public SelectedSeries(Network network, String href) {
        this.network = network.name().toLowerCase();
        this.href = href;
        this.date = LocalDate.now().toString();
    }

    @Override
    public String toString() {
        return network + "/" + href;
    }

    @Override
    public int hashCode() {
        return network.hashCode() + href.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SelectedSeries) {
            SelectedSeries that = (SelectedSeries)obj;
            return this.network.equals(that.network) && this.href.equals(that.href);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(SelectedSeries o) {
        return o.date.compareTo(this.date);
    }

    public static String listToString(List<SelectedSeries> list) {
        String ls = System.getProperty("line.separator");
        StringBuilder buf = new StringBuilder();
        buf.append("Selected Series (");
        buf.append(list.size());
        buf.append(")");
        for (SelectedSeries s: list) {
            buf.append(ls);
            buf.append(s.toString());
        }
        return buf.toString();
    }
}
