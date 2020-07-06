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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does all the real work of managing the episode download. It finds candidate
 * episodes, finds the best HLS URI, finds the authentication token, adds the token to the
 * HLS URI, uses the HLS URI to determine the number of segments that will be downloaded
 * (for the purpose of progress monitoring), configures a native process to run FFmpeg taking
 * input from the HLS URI and writing output to a .mp4 file. The FFmpeg configuration
 * is a simple repackaging with no codec manipulation of any kind.
 */
public class EpisodeDownloader extends Task<Background> {

    static final Logger LOGGER = Logger.getLogger(EpisodeDownloader.class.getName());

    NumberFormat nf = new DecimalFormat("##0.0");
    Status status;
    EpisodeDownloadType type;
    List<SelectedSeries> selectedSeries;
    Process p;
    File outFile;

    public EpisodeDownloader(Status status, EpisodeDownloadType type) {
        this.status = status;
        this.type = type;
        updateProgress(0,1);
        updateValue(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(7), null)));
    }

    public Background call() {
        LOGGER.fine("Starting episode download: " + type + " " + status.config);
        if (status.config.ffmpegVersion == null) {
            updateMessage(" FFmpeg not found ");
            return new Background(new BackgroundFill(Color.RED, new CornerRadii(7), null));
        }
        int downloadCount = 1;
        if (EpisodeDownloadType.ABC_SERIES.equals(type) || EpisodeDownloadType.SBS_SERIES.equals(type)) {
            while (downloadCount > 0) {
                downloadCount = 0;
                selectedSeries = status.seriesSelection.sort(); // this is a synchronized method
                LOGGER.fine(SelectedSeries.listToString(selectedSeries));
                for (SelectedSeries s : selectedSeries) {
                    if (!isCancelled()) {
                        if (EpisodeDownloadType.ABC_SERIES.equals(type) && s.network.equals("abc")) {
                            downloadCount += downloadAbcSeries(s);
                        } else if (EpisodeDownloadType.SBS_SERIES.equals(type) && s.network.equals("sbs")){
                            downloadCount += downloadSbsSeries(s);
                        }
                    }
                }
            }
        } else if (EpisodeDownloadType.SELECTED_EPISODE.equals(type)) { //Selected Episodes
            while (downloadCount > 0) {
                downloadCount = 0;
                List<SelectedEpisode> selectedEpisodes = null;
                synchronized (status.selectedEpMap) {
                    selectedEpisodes = new ArrayList<>(status.selectedEpMap.values());
                }
                LOGGER.fine("Selected: " + selectedEpisodes);
                for (SelectedEpisode se : selectedEpisodes) {
                    if (!isCancelled()) {
                        if (se.key.network.equals(Network.ABC)) {
                            downloadAbcEpisode(se);
                            status.selectedEpMap.remove(se.key);
                            downloadCount++;
                        } else if (se.key.network.equals(Network.SBS)) {
                            downloadSbsEpisode(se);
                            status.selectedEpMap.remove(se.key);
                            downloadCount++;
                        }
                    }
                }
            }
        }
        return new Background(new BackgroundFill(Color.GREEN, new CornerRadii(7), null));
    }

    private int downloadAbcSeries(SelectedSeries s) {
        int downloadCount = 0;
        URI uri = null;
        String uriStr = "https://iview.abc.net.au/api/" + s.href;
        AbcSeries abc = null;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException se) {
            LOGGER.log(Level.SEVERE, "ABC URI Invalid: " + uriStr, se);
        }
        if (uri != null) {
            try {
                abc = JsonConstructiveParser.parse(status.httpInput.getReader(uri), AbcSeries.class);
            } catch (Exception ioe) {
                LOGGER.log(Level.SEVERE, "Series request failed: " + uriStr, ioe);
            }
        }
        if (!isCancelled() && (abc != null)) {
            LOGGER.fine(abc.toString());
            if (("error".equals(abc.status) && (abc.code == 404)) || (abc.episodes == null)) {
                // Series no longer available
                status.seriesSelection.remove(Network.ABC, s.href);
                LOGGER.fine(status.seriesSelection.toString());
                status.saveSeriesSelection();
            } else {
                for (AbcEpisode ep : abc.episodes) {
                    if (!isCancelled()) {
                        if (!status.episodeHistory.contains(Network.ABC.historyHref(ep.href))) {
                            SelectedEpisode se = new SelectedEpisode(Network.ABC, ep.href);
                            downloadAbcEpisode(se);
                            downloadCount++;
                        }
                    }
                }
            }
        }
        LOGGER.fine(s.toString() + " " + downloadCount);
        return downloadCount;
    }

    private void downloadAbcEpisode(SelectedEpisode se) {
        if (isCancelled()) {
            return;
        }
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
                LOGGER.log(Level.SEVERE, "Series request failed: " + uriStr, ioe);
            }
        }
        if (!isCancelled() && (ep != null) && (ep.playlist != null)) {
            if (ep.title == null) {
                se.title = ep.seriesTitle;
            } else {
                se.title = ep.seriesTitle + " " + ep.title;
            }
            se.description = ep.description;
            LOGGER.fine("ABC episode " + ep.title);
            for (AbcStreams st : ep.playlist) {
                if (st.type.equals("program")) {
                    String plus = st.hls_plus;
                    LOGGER.fine("HLS URL " + plus);
                    plus = status.abcAuth.fixUrl(plus);
                    LOGGER.fine("Fixed URL " + plus);
                    se.streams = M3u8.getStreamInfs(plus, status.httpInput);
                    String best = status.abcAuth.appendTokenOnly(M3u8.getResolutionUpTo(Integer.MAX_VALUE, true, se.streams));
                    int segCount = M3u8.getSegmentCount(best, status.httpInput);
                    LOGGER.fine(best + " " + segCount);
                    File outDir = new File((status.config.downloadDir == null) ?
                            System.getProperty("user.home") : status.config.downloadDir);
                    outFile = new File(outDir, fixFileName(se.title) + ".mp4");
                    LOGGER.fine("Download file: " + outFile);
                    String captions = null;
                    if (st.captions != null) {
                        captions = st.captions.src_vtt;
                    }
                    ProcessBuilder pb = null;
                    if (captions == null) {
                        pb = new ProcessBuilder(status.config.ffmpegCommand, "-y", "-i", best, "-c", "copy", outFile.toString());
                    } else {
                        pb = new ProcessBuilder(status.config.ffmpegCommand, "-y", "-i", best, "-i", captions,
                                "-c", "copy", "-c:s", "mov_text", "-metadata:s:s:0", "language=eng", "-tag:s:s:0", "tx3g", outFile.toString());
                    }
                    pb.redirectErrorStream(true);
                    if (isCancelled()) {
                        return;
                    } else {
                        updateTitle(se.title);
                        updateProgress(0, segCount);
                        updateValue(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(7), null)));
                    }
                    try {
                        p = pb.start();
                        BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line = outReader.readLine();
                        while (line != null) {
                            if (isCancelled()) {
                                kill();
                                return;
                            }
                            int start = line.indexOf("/segment");
                            if (start > 0) {
                                start = start + 8;
                                int end = line.indexOf('_', start);
                                int seg = Integer.parseInt(line.substring(start, end));
                                updateProgress(seg, segCount);
                            }
                            line = outReader.readLine();
                        }
                    } catch (IOException ioe) {
                        LOGGER.log(Level.SEVERE, "Process status read failed", ioe);
                    }
                    int procStatus = -1;
                    try {
                        if (p != null) {
                            procStatus = p.waitFor();
                        }
                    } catch (InterruptedException ie) {}
                    p = null;
                    if (procStatus == 0) {
                        updateMessage(" Complete ");
                        updateValue(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(7), null)));
                        VisualEpisode ve = status.visualEpMap.get(se.key);
                        if (ve != null) {
                            Platform.runLater(() ->  {
                                ve.downloadComplete();
                            });
                        }
                        status.episodeHistory.add(Network.ABC.historyHref(se.key.href));
                        status.saveHistory();
                    } else {
                        LOGGER.severe("Download of " + se.title + " failed. FFmpeg status = " + procStatus);
                        throw new RuntimeException("FFmpeg process ended abnormally");
                    }
                }
            }
        }
    }

    private int downloadSbsSeries(SelectedSeries s) {
        SbsSeries series = status.sbsEpisodes.seriesMap.get(s.href);
        int downloadCount = 0;
        if (series != null) {
            for (SbsEpisode ep: series.episodes.values()) {
                if (!isCancelled()) {
                    if (!status.episodeHistory.contains(Network.SBS.historyHref(ep.id))) {
                        SelectedEpisode se = new SelectedEpisode(Network.SBS, ep.id);
                        downloadSbsEpisode(se);
                        downloadCount++;
                    }
                }
            }
        }
        return downloadCount;
    }

    private void downloadSbsEpisode(SelectedEpisode se) {
        if (isCancelled()) {
            return;
        }
        URI uri = null;
        String uriStr = "https://www.sbs.com.au/ondemand/video/single/" + se.key.href;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException urise) {
            LOGGER.log(Level.SEVERE, "SBS URI Invalid: " + uriStr, urise);
        }
        SbsPlayerParams params = null;
        String m3u8 = null;
        String subtitle = null;
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
                    se.description = n.getAttributes().getNamedItem("abstract").getTextContent();
                    // find subtitles
                    list = doc.getElementsByTagName("textstream");
                    for (int i = 0; i < list.getLength(); i++) {
                        Element sub = (Element)list.item(i);
                        String lang = sub.getAttribute("lang");
                        String type = sub.getAttribute("type");
                        if ("en".equals(lang) && "text/srt".equals(type)) {
                            subtitle = sub.getAttribute("src");
                            break;
                        }
                    }
                }
            } catch (Exception ioe) {
                LOGGER.log(Level.SEVERE, "Series request failed: " + uriStr, ioe);
            }
        }
        if (!isCancelled() && (m3u8 != null)) {
            LOGGER.fine("SBS episode " + se.title);
            List<M3u8.StreamInf> streamList = M3u8.getStreamInfs(m3u8, status.httpInput);
            String best = M3u8.getResolutionUpTo(Integer.MAX_VALUE, true, streamList);
            int segCount = M3u8.getSegmentCount(best, status.httpInput);
            LOGGER.fine(best + " " + segCount);
            File outDir = new File((status.config.downloadDir == null) ?
                    System.getProperty("user.home") : status.config.downloadDir);
            outFile = new File(outDir, fixFileName(se.title) + ".mp4");
            LOGGER.fine("Download file: " + outFile);
            ProcessBuilder pb = null;
            if (subtitle == null) {
                pb = new ProcessBuilder(status.config.ffmpegCommand, "-y", "-i", best, "-c", "copy", outFile.toString());
            } else {
                pb = new ProcessBuilder(status.config.ffmpegCommand, "-y", "-i", best, "-i", subtitle,
                        "-c", "copy", "-c:s", "mov_text", "-metadata:s:s:0", "language=eng", "-tag:s:s:0", "tx3g", outFile.toString());
            }
            pb.redirectErrorStream(true);
            if (isCancelled()) {
                return;
            } else {
                updateTitle(se.title);
                updateProgress(0, segCount);
                updateValue(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(7), null)));
            }
            try {
                p = pb.start();
                BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = outReader.readLine();
                while (line != null) {
                    if (isCancelled()) {
                        kill();
                        return;
                    }
                    int start = line.indexOf("/segment");
                    if (start > 0) {
                        start = start + 8;
                        int end = line.indexOf('_', start);
                        int seg = Integer.parseInt(line.substring(start, end));
                        updateProgress(seg, segCount);
                    }
                    line = outReader.readLine();
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Process status read failed", ioe);
            }
            int procStatus = -1;
            try {
                if (p != null) {
                    procStatus = p.waitFor();
                }
            } catch (InterruptedException ie) {
            }
            p = null;
            if (procStatus == 0) {
                updateMessage(" Complete ");
                updateValue(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(7), null)));
                VisualEpisode ve = status.visualEpMap.get(se.key);
                if (ve != null) {
                    Platform.runLater(() -> {
                        ve.downloadComplete();
                    });
                }
                status.episodeHistory.add(Network.SBS.historyHref(se.key.href));
                status.saveHistory();
            } else {
                LOGGER.severe("Download of " + se.title + " failed. FFmpeg status = " + procStatus);
                throw new RuntimeException("FFmpeg process ended abnormally");
            }
        }
    }

    public String fixFileName(String in) {
        return in.replace('/','-').replace('\\','-').replace(':','-');
    }

    private void kill() {
        if (p != null) {
            p.destroyForcibly();
            LOGGER.fine("Destroy!");
        }
        if (outFile != null) {
            outFile.delete();
        }
    }

    @Override
    protected void updateProgress(long v, long v1) {
        updateMessage(" " + nf.format(v * 100.0 / (double)v1) + " ");
        super.updateProgress(v, v1);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage(" Complete ");
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage(" Cancelled ");
        updateValue(new Background(new BackgroundFill(Color.RED, new CornerRadii(7), null)));
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage(" Failed ");
        updateValue(new Background(new BackgroundFill(Color.RED, new CornerRadii(7), null)));
        kill();
    }
}
