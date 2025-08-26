/*******************************************************************************
 * Copyright (c) 2025 StingoDL.
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

public class Subtitle implements Comparable<Subtitle> {
    public int priority = 1;
    public String sortKey;
    public String language;
    public String url;
    public String title;

    public Subtitle(String prioritySubtitle, String sortKey, String language, String url, String title) {
        this.sortKey = sortKey;
        this.language = language;
        this.url = url;
        this.title = title;
        if (sortKey.equals(prioritySubtitle)) {
            priority = 0;
        } else if ("en".equals(prioritySubtitle) && ("en0".equals(sortKey) || "en1".equals(sortKey))) {
            priority = 0;
        }
    }

    @Override
    public int compareTo(Subtitle s) {
        if (priority == s.priority) {
            return sortKey.compareTo(s.sortKey);
        } else {
            return priority - s.priority;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Subtitle) {
            return this.sortKey.equals(((Subtitle) o).sortKey);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return sortKey.hashCode();
    }

    @Override
    public String toString() {
        return "Subtitle: " + sortKey + " " + priority;
    }
}
