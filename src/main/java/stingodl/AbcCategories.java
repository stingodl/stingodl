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

import java.util.List;
import java.util.StringTokenizer;

public class AbcCategories {
    public List<AbcCategorySummary> categories;

    public void toCamelCase() {
        for (AbcCategorySummary cat: categories) {
            StringTokenizer tokenizer = new StringTokenizer(cat.title);
            StringBuilder buf = new StringBuilder();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                char[] chars = token.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (i == 0) {
                        chars[i] = Character.toUpperCase(chars[i]);
                    } else {
                        chars[i] = Character.toLowerCase(chars[i]);
                    }
                }
                buf.append(chars);
                buf.append(' ');
            }
            cat.title = buf.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof AbcCategories)) {
            AbcCategories that = (AbcCategories) obj;
            if ((this.categories == null) || (that.categories == null)) {
                return false;
            }
            if (this.categories.size() != that.categories.size()) {
                return false;
            }
            for (int i = 0; i < this.categories.size(); i++) {
                if (!this.categories.get(i).equals(that.categories.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return categories == null ? "" : categories.toString();
    }
}
