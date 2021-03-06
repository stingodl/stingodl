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

public class AbcCategorySummary {
    public String title;
    public String href;

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof AbcCategorySummary)) {
            AbcCategorySummary that = (AbcCategorySummary) obj;
            if ((this.title == null) || (this.href == null) || (that.title == null) || (that.href == null)) {
                return false;
            }
            return this.title.equals(that.title) && this.href.equals(that.href);
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("AbcCategorySummary: ");
        buf.append(title);
        buf.append(" (");
        buf.append(href);
        buf.append(')');
        return buf.toString();
    }
}
