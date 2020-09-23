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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class M3u8 {

    static final Logger LOGGER = Logger.getLogger(M3u8.class.getName());

    public static void addAbcDetails(SelectedEpisode se, Status status) {
        URI uri = null;
        String uriStr = "https://iview.abc.net.au/api/" + se.key.href;
        AbcEpisodeDetail ep = null;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException urise) {
            LOGGER.log(Level.SEVERE, "ABC URI Invalid: " + uriStr, urise);
        }
        if (uri != null) {
            try {
                ep = JsonConstructiveParser.parse(status.httpInput.getReader(uri), AbcEpisodeDetail.class);
            } catch (Exception ioe) {
                LOGGER.log(Level.SEVERE, "ABC episode parse failed: " + uriStr, ioe);
            }
        }
        if (ep != null) {
            if (ep.title == null) {
                se.title = ep.seriesTitle;
            } else {
                se.title = ep.seriesTitle + " " + ep.title;
            }
            se.description = ep.description;
            se.duration = (Integer.parseInt(ep.duration) / 60) + " min";
            se.expiry = ep.expireDate.substring(0, 10);
            se.thumbnailUrl = ep.thumbnail;
            LOGGER.fine("ABC episode " + ep.title);
            for (AbcStreams st : ep.playlist) {
                if (st.type.equals("program")) {
                    LOGGER.fine("HLS URL " + st.hls_plus);
                    se.abcAuth = new AbcAuth(se.key.href.substring(se.key.href.lastIndexOf('/') + 1), status.httpInput);
                    se.m3u8Url = se.abcAuth.setQueryToken(st.hls_plus);
                    if (st.captions != null) {
                        se.subtitle = st.captions.src_vtt;
                    }
                }
            }
        }
    }

    public static void findAbcStreams(SelectedEpisode se, Status status) {
        se.streams = getStreamInfs(se.m3u8Url, status.httpInput);
        for (StreamInf inf: se.streams) {
            inf.url = se.abcAuth.appendToken(inf.url);
        }
    }

    public static void findSbsStreams(SelectedEpisode se, Status status) {
        URI uri = null;
        String uriStr = "https://www.sbs.com.au/ondemand/video/single/" +
                se.key.href.substring(se.key.href.lastIndexOf('/') + 1);
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException urise) {
            LOGGER.log(Level.SEVERE, "SBS URI Invalid: " + uriStr, urise);
        }
        SbsPlayerParams params = null;
        String m3u8 = null;
        if (uri != null) {
            try {
                BufferedReader reader = new BufferedReader(status.httpInput.getReader(uri));
                String l = reader.readLine();
                while (l != null) {
                    if (l.trim().startsWith("var playerParams =")) {
                        params = JsonConstructiveParser.parse(l.substring(l.indexOf('{'), l.lastIndexOf('}') + 1), SbsPlayerParams.class);
                        LOGGER.fine(params.toString());
                        break;
                    }
                    l = reader.readLine();
                }
                reader.close();
                if (params != null) {
                    se.title = params.videoTitle;
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = db.parse(params.releaseUrls.html);
                    NodeList list = doc.getElementsByTagName("video");
                    Node n = list.item(0);
                    m3u8 = n.getAttributes().getNamedItem("src").getTextContent();
                    LOGGER.fine("SBS initial m3u8: " + m3u8);
                    se.streams = getStreamInfs(m3u8, status.httpInput);
                    list = doc.getElementsByTagName("textstream");
                    for (int i = 0; i < list.getLength(); i++) {
                        Element sub = (Element)list.item(i);
                        String lang = sub.getAttribute("lang");
                        String type = sub.getAttribute("type");
                        if ("en".equals(lang) && "text/srt".equals(type)) {
                            se.subtitle = sub.getAttribute("src");
                            break;
                        }
                    }
                }
            } catch (Exception ioe) {
                LOGGER.log(Level.SEVERE, "Series request failed: " + uriStr, ioe);
            }
        }
    }

    public static List<StreamInf> getStreamInfs(String uri, HttpInput httpInput) {
        List<StreamInf> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(httpInput.getReader(new URI(uri)));
            String l = reader.readLine();
            StreamInf inf = null;
            if (l.equals("#EXTM3U")) {
                l = reader.readLine();
                while (l != null) {
                    if (l.startsWith("#EXT-X-STREAM-INF:")) {
                        inf = parseStreamInf(l.substring("#EXT-X-STREAM-INF:".length()));
                    } else {
                        if (inf != null) {
                            inf.url = l;
                            list.add(inf);
                            inf = null;
                        }
                    }
                    l = reader.readLine();
                }
            }
            reader.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "M3u8 read failed: " + uri, e);
        }
        return list;
    }

    public static StreamInf getResolutionUpTo(int targetHeight, boolean bandwidthHigh, List<StreamInf> infs) {
        StreamInf bestInf = null;
        int bestHeight = 0;
        int bandwidth = bandwidthHigh ? 0 : Integer.MAX_VALUE;
        for (StreamInf inf: infs) {
            if ((inf.resolutionH > bestHeight) && (inf.resolutionH <= targetHeight)) {
                bestHeight = inf.resolutionH;
                bandwidth = inf.bandwidth;
                bestInf = inf;
            } else if (inf.resolutionH == bestHeight) {
                if (bandwidthHigh) {
                    if (inf.bandwidth > bandwidth) {
                        bandwidth = inf.bandwidth;
                        bestInf = inf;
                    }
                } else {
                    if (inf.bandwidth < bandwidth) {
                        bandwidth = inf.bandwidth;
                        bestInf = inf;
                    }
                }
            }
        }
        LOGGER.fine((bestInf == null) ? "No StreamInf selected" : bestInf.toString());
        return bestInf;
    }

    public static int getSegmentCount(String uri, HttpInput httpInput) {
        int count = 0;
        try {
            BufferedReader reader = new BufferedReader(httpInput.getReader(new URI(uri)));
            String l = reader.readLine();
            int first = 999999;
            int last = 0;
            if (l.equals("#EXTM3U")) {
                l = reader.readLine();
                while (l != null) {
                    if (!l.startsWith("#EXT")) {
                        int start = l.indexOf("/segment") + 8;
                        int end = l.indexOf('_', start);
                        int seg = -1;
                        try {
                            seg = Integer.parseInt(l.substring(start, end));
                        } catch (NumberFormatException nfe) {}
                        if (seg >= 0) {
                            count++;
                            if (seg < first) {
                                first = seg;
                            }
                            if (seg > last) {
                                last = seg;
                            }
                        }
                    }
                    l = reader.readLine();
                }
            }
            reader.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "M3u8 read (segment count) failed: " + uri, e);
        }
        return count;

    }

    public static StreamInf parseStreamInf(String s) {
        LOGGER.fine(s);
        StreamInf inf = new StreamInf();
        String quoted = null;
        boolean inquoted = false;
        int tokenStart = 0;
        String property = null;
        String value = null;

        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                if (inquoted) { // end quoted
                    quoted = s.substring(tokenStart, i);
                    inquoted = false;
                } else { // start quoted
                    tokenStart = i + 1;
                    inquoted = true;
                }
            } else if ((c == '=') && !inquoted) { // end property
                property = s.substring(tokenStart, i);
                tokenStart = i + 1;
            } else if ((c == ',') && !inquoted) { // end value
                if (quoted == null) {
                    value = s.substring(tokenStart, i);
                } else {
                    value = quoted;
                    quoted = null;
                }
                setProperty(inf, property, value);
                property = null;
                value = null;
                tokenStart = i + 1;
            }
        }
        if (quoted == null) {
            value = s.substring(tokenStart, s.length());
        } else {
            value = quoted;
        }
        setProperty(inf, property, value);
        return inf;
    }

    public static void setProperty(StreamInf inf, String property, String value) {
        if ("BANDWIDTH".equals(property)) {
            try {
                inf.bandwidth = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {}
        } else if ("RESOLUTION".equals(property)) {
            int x = value.indexOf('x');
            if (x > 0) {
                try {
                    inf.resolutionW = Integer.parseInt(value.substring(0, x));
                } catch (NumberFormatException nfe) {}
                try {
                    inf.resolutionH = Integer.parseInt(value.substring(x + 1));
                } catch (NumberFormatException nfe) {}
            }
        } else if ("CODECS".equals(property)) {
            inf.codecs = value;
            StringTokenizer st = new StringTokenizer(value,",");
            while(st.hasMoreTokens()) {
                if (st.nextToken().trim().startsWith("hvc")) {
                    inf.isHEVC = true;
                    break;
                }
            }
        } else if ("CLOSED-CAPTIONS".equals(property)) {
            inf.closedCations = value;
        }
    }

    public static class StreamInf {
        int bandwidth;
        int resolutionW;
        int resolutionH;
        String codecs;
        String closedCations;
        String url;
        boolean isHEVC = false;

        @Override
        public String toString() {
            return "StreamInf bw: " + bandwidth +
                    " resW: " + resolutionW + " resH: " + resolutionH +
                    " cod: " + codecs + " cap: " + closedCations;
        }
    }
}
