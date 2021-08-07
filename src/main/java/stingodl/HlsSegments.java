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

import java.util.ArrayList;
import java.util.List;

public class HlsSegments {
    SelectedEpisode se;
    String keyMethod;
    String keyURI;
    int startSeq;
    String m3u8Base;
    List<String> segList = new ArrayList<>();

    public HlsSegments(SelectedEpisode se, String m3u8Uri) {
        this.se = se;
        String stripM3u8 = m3u8Uri.substring(0, m3u8Uri.indexOf(".m3u8"));
        m3u8Base = stripM3u8.substring(0, stripM3u8.lastIndexOf('/') + 1);
    }

    public void addExtX(ExtX extX) {
        if ("#EXT-X-KEY".equals(extX.extX)) {
            keyMethod = extX.valueMap.get("METHOD");
            keyURI = extX.valueMap.get("URI");
        } else if ("#EXT-X-MEDIA-SEQUENCE".equals(extX.extX)) {
            startSeq = Integer.parseInt(extX.valueMap.get("VALUE"));
        }
    }

    public void addUri(String uri) {
        if (uri.startsWith("https://") || uri.startsWith("http://")) {
            segList.add(uri);
        } else {
            segList.add(m3u8Base + uri);
        }
    }

    public String toString() {
        return "HLS Segments: Key M " + keyMethod + " Key URI " + keyURI + " Seq Start " + startSeq
                + " Segments " + segList;
    }
}
