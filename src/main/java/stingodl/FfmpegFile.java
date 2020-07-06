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

public class FfmpegFile implements Comparable<FfmpegFile> {
    public String file;
    int v1 = 0;
    int v2 = 0;
    int v3 = 0;
    public FfmpegFile(String file) {
        this.file = file;
        int firstDash = file.indexOf('-');
        int lastDash = file.indexOf('-',firstDash+1);
        String version = file.substring(firstDash+1,lastDash);
        int firstDot = version.indexOf('.');
        int secondDot = -1;
        if (firstDot > 0) {
            secondDot = version.indexOf('.',firstDot+1);
        }
        try {
            if (firstDot < 0) {
                v1 = Integer.parseInt(version);
            } else {
                v1 = Integer.parseInt(version.substring(0,firstDot));
                if (secondDot < 0) {
                    v2 = Integer.parseInt(version.substring(firstDot+1));
                } else {
                    v2 = Integer.parseInt(version.substring(firstDot+1,secondDot));
                    v3 = Integer.parseInt(version.substring(secondDot+1));
                }
            }
        } catch (NumberFormatException nfe) {}
    }

    @Override
    public int compareTo(FfmpegFile o) {
        int c = o.v1 - this.v1;
        if (c == 0) {
            c = o.v2 - this.v2;
            if (c == 0) {
                c = o.v3 - this.v3;
            }
        }
        return c;
    }

    @Override
    public String toString() {
        return file;
    }
}
