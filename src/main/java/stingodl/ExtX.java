/*******************************************************************************
 * Copyright (c) 2021 StingoDL.
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

import java.util.HashMap;
import java.util.Map;

public class ExtX {
    String extX;
    Map<String, String> valueMap = new HashMap<>();

    public ExtX(String line) {
        int colon = line.indexOf(':');
        if (colon < 0) { // no value
            return;
        }
        extX = line.substring(0, colon);
        String values = line.substring(colon + 1);
        boolean quoted = false;
        String quotedString = null;
        String key = null;
        int keyStart = 0;
        int valueStart = 0;
        for (int i = 0; i < values.length(); i++) {
            char c = values.charAt(i);
            if (quoted) {
                if (c == '"') { // end of quote
                    quotedString = values.substring(valueStart, i);
                    quoted = false;
                }
            } else {
                if (c == '"') {
                    quoted = true;
                    valueStart = i + 1;
                } else if (c == '=') {
                    key = values.substring(keyStart, i);
                    valueStart = i + 1;
                } else if (c == ',') {
                    if (key == null) {
                        key = "VALUE";
                    }
                    String value = quotedString;
                    quotedString = null;
                    if (value == null) {
                        value = values.substring(valueStart, i);
                    }
                    valueMap.put(key, value);
                    key = null;
                    keyStart = i + 1;
                }
            }
        }
        if (key == null) {
            key = "VALUE";
        }
        String value = quotedString;
        if (value == null) {
            value = values.substring(valueStart);
        }
        valueMap.put(key, value);
//        System.out.println(extX + " * " + valueMap);
    }
}
