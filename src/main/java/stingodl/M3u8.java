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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.PrettyPrint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
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
            if (ep.expireDate == null) {
                se.expiry = "          ";
            } else {
                se.expiry = ep.expireDate.substring(0, 10);
            }
            se.thumbnailUrl = ep.thumbnail;
            LOGGER.fine("ABC episode " + ep);
            for (AbcPlay p : ep.playlist) {
                if (p.type.equals("program")) {
                    String url = p.streams.hls.hd;
                    if (url == null) {
                        url = p.streams.hls.sd;
                    }
                    LOGGER.fine("HLS URL " + url);
                    se.abcAuth = new AbcAuth(se.key.href.substring(se.key.href.lastIndexOf('/') + 1), status.httpInput);
                    se.m3u8Url = se.abcAuth.setQueryToken(url);
                    LOGGER.fine("Auth URL " + se.m3u8Url);
                    if (p.captions != null) {
                        se.subtitle = p.captions.src_vtt;
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
        String uriStr = "https://www.sbs.com.au/api/video_pdkvars/id/" +
                se.key.href.substring(se.key.href.lastIndexOf('/') + 1) +
                "?form=json";
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException urise) {
            LOGGER.log(Level.SEVERE, "SBS URI Invalid: " + uriStr, urise);
        }
        SbsPlayerParams params = null;
        String m3u8 = null;
        if (uri != null) {
            try {
                if (LOGGER.isLoggable(Level.FINE)) {
                    JsonValue val = Json.parse(status.httpInput.getReader(uri));
                    Writer writer = new StringWriter();
                    val.writeTo(writer, PrettyPrint.indentWithSpaces(3));
                    LOGGER.fine("SBS episode JSON: " + writer.toString());
                }
                BufferedReader reader = new BufferedReader(status.httpInput.getReader(uri));
                params = JsonConstructiveParser.parse(reader, SbsPlayerParams.class);
                LOGGER.fine("SBS Player params " + params);
                if (params != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        displayUri(params.releaseUrls.htmlandroid, status);
                    }
                    se.title = params.videoTitle;
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = db.parse(params.releaseUrls.htmlandroid);
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
        String stripM3u8 = uri.substring(0, uri.indexOf(".m3u8"));
        String m3u8Base = stripM3u8.substring(0, stripM3u8.lastIndexOf('/') + 1);
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
                            if (l.startsWith("https://")) {
                                inf.url = l;
                            } else if (l.startsWith("http://")) {
                                inf.url = l;
                            } else {
                                inf.url = m3u8Base + l;
                            }
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

    public static HlsSegments getSegments(SelectedEpisode se, String uri, HttpInput httpInput) {
        HlsSegments segments = new HlsSegments(se, uri);
        try {
            BufferedReader reader = new BufferedReader(httpInput.getReader(new URI(uri)));
            String l = reader.readLine();
            boolean uriNext = false;
            if (l.equals("#EXTM3U")) {
                l = reader.readLine();
                while (l != null) {
                    LOGGER.fine(l);
                    if (l.startsWith("#EXT-X")) {
                        segments.addExtX(new ExtX(l));
                    } else if (l.startsWith("#EXTINF:")) {
                        uriNext = true;
                    } else if (uriNext) {
                        segments.addUri(l);
                        uriNext = false;
                    }
                    l = reader.readLine();
                }
            } else {
                while (l != null) {
                    LOGGER.fine(l);
                    l = reader.readLine();
                }
            }
            reader.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "M3u8 read (segment count) failed: " + uri, e);
        }
        return segments;
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

    private static void displayUri(String uri, Status status) {
        try {
            Reader reader = status.httpInput.getReader(new URI(uri));
            StringBuilder buf = new StringBuilder();
            int c = reader.read();
            while (c >= 0) {
                buf.append((char)c);
                c = reader.read();
            }
            reader.close();
            LOGGER.fine(buf.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
