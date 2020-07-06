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
import java.util.Comparator;
import java.util.List;

public class AbcCategory {
    public String title;
    public List<AbcCategoryDay> index;
    public transient List<AbcSeriesSummary> nameOrdered = new ArrayList<>();
    public transient List<AbcSeriesSummary> dateOrdered = new ArrayList<>();

    public void orderLists() {
        if (index != null) {
            for (AbcCategoryDay day: index) {
                if (day.episodes != null) {
                    for (AbcSeriesSummary ep: day.episodes) {
                        if ("0".equals(ep.livestream) && (ep.seriesTitle != null) && (ep.pubDate != null)) {
                            dateOrdered.add(ep);
                            nameOrdered.add(ep);
                        }
                    }
                }
            }
        }
        Collections.sort(nameOrdered, new NameComparator());
        Collections.sort(dateOrdered, new DateComparator());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("AbcCategory: ");
        buf.append(title);
        return buf.toString();
    }

    public static class NameComparator implements Comparator<AbcSeriesSummary> {
        @Override
        public int compare(AbcSeriesSummary o1, AbcSeriesSummary o2) {
            return o1.seriesTitle.compareTo(o2.seriesTitle);
        }
    }

    public static class DateComparator implements Comparator<AbcSeriesSummary> {
        @Override
        public int compare(AbcSeriesSummary o1, AbcSeriesSummary o2) {
            return -o1.pubDate.compareTo(o2.pubDate);
        }
    }
}
