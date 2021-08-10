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

public class Config {
    public static int HEVC_DEFAULT_CRF = 28;
    public static int MAX_CRF = 51;
    public String downloadDir;
    public long sbsQuickUpdateMinutes = 120;
    public long sbsFullUpdateHours = 23;
    public boolean oneEpisodePerSeries = true;
//    public boolean encodeHEVC = false;
    public int maxResulotion = Integer.MAX_VALUE;
    public int encodeHevcCrf = HEVC_DEFAULT_CRF;
    public transient String ffmpegVersion;
    public transient String ffmpegText;
    public transient String ffmpegCommand;
    public transient String ffmpegCommandType;
    public transient String os;
    public transient String osExe;
    public transient String version = "";

    @Override
    public String toString() {
        return "Config: Download " + downloadDir;
    }
}
