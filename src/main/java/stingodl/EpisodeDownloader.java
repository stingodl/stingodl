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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
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
                if (abc.episodes != null) {
                    for (AbcEpisode ep: abc.episodes) {
                        ep.parseTitle();
                    }
                    Collections.sort(abc.episodes);
                }
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
                            if (status.config.oneEpisodePerSeries) {
                                break;
                            }
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
        M3u8.addAbcDetails(se, status);
        if (se.m3u8Url != null) {
            M3u8.findAbcStreams(se, status);
            if (!isCancelled() && (se.streams != null) && (!se.streams.isEmpty())) {
                LOGGER.fine("ABC episode " + se.title);
                downloadEpisode(se, status.config.maxResulotion);
            }
        }
    }

    private int downloadSbsSeries(SelectedSeries s) {
        SbsSeries series = status.sbsEpisodes.seriesMap.get(s.href);
        int downloadCount = 0;
        if (series != null) {
            List<SbsEpisode> eps = new ArrayList<>(series.episodes.values());
            Collections.sort(eps, Comparator.reverseOrder()); // earliest first
            for (SbsEpisode ep: eps) {
                if (!isCancelled()) {
                    if (!status.episodeHistory.contains(Network.SBS.historyHref(ep.id))) {
                        SelectedEpisode se = new SelectedEpisode(Network.SBS, ep.id);
                        downloadSbsEpisode(se);
                        downloadCount++;
                        if (status.config.oneEpisodePerSeries) {
                            break;
                        }
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
        M3u8.findSbsStreams(se, status);
        if (!isCancelled() && (se.streams != null) && (!se.streams.isEmpty())) {
            LOGGER.fine("SBS episode " + se.title);
            downloadEpisode(se, status.config.maxResulotion);
        }
    }

    public void downloadEpisode(SelectedEpisode se, int maxResulotion) {
        M3u8.StreamInf best = M3u8.getResolutionUpTo(maxResulotion, true, se.streams);
        if (best == null) {
            LOGGER.severe("Requested resolution not available: " + maxResulotion + " " + se);
            throw new RuntimeException("Requested resolution not available");
        }
        HlsSegments segments = M3u8.getSegments(se, best.url, status.httpInput);
        System.out.println("Key URI " + segments.keyURI);
        TsPacketCache cache = new TsPacketCache();
        int segCount = segments.segList.size();
        LOGGER.fine(best + " * segments: " + segCount);
        File outDir = new File((status.config.downloadDir == null) ?
                System.getProperty("user.home") : status.config.downloadDir);
        outFile = new File(outDir, fixFileName(se.title) + ".mp4");
        LOGGER.fine("Download file: " + outFile);
        List<String> args = new ArrayList<>();
        args.add(status.config.ffmpegCommand);
        args.add("-y");
        args.add("-i");
        args.add("-");
        if (se.subtitle != null) {
            args.add("-i");
            args.add(se.subtitle);
            args.add("-c:s");
            args.add("mov_text");
            args.add("-metadata:s:s:0");
            args.add("language=eng");
            args.add("-tag:s:s:0");
            args.add("tx3g");
        }
        if (status.config.encodeHEVC && !best.isHEVC) {
            args.add("-c:v");
            args.add("libx265");
            args.add("-tag:v");
            args.add("hvc1");
            args.add("-crf");
            args.add(Integer.toString(status.config.encodeHevcCrf));
        } else {
            args.add("-c:v");
            args.add("copy");
        }
        args.add("-c:a");
        args.add("copy");
        args.add(outFile.toString());
        LOGGER.fine(args.toString());
        ProcessBuilder pb = new ProcessBuilder(args);
//        pb.redirectErrorStream(true);
        if (isCancelled()) {
            return;
        } else {
            updateTitle(se.title);
            updateProgress(0, segCount);
            updateValue(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(7), null)));
        }

        try {
            InputStream keyStream = status.httpInput.getInputStream(new URI(segments.keyURI));
            byte[] key = keyStream.readAllBytes();
            p = pb.start();
            OutputStream stdin = p.getOutputStream();
            for (int i = 0; i < segCount; i++) {
                URI uri = new URI(segments.segList.get(i));
                TsDecryptInputStream tsdis = new TsDecryptInputStream(
                        status.httpInput.getInputStream(uri), key, i + segments.startSeq);
                tsdis.seekSyncByte(TsPacket.SYNC_BYTE);
                TsPacket tsPacket = cache.getTsPacket();
                int size = tsPacket.loadData(tsdis);
                if (size == 0) { // no data in stream
                    throw new IOException("No data in stream " + segments.segList.get(i));
                }
                TsPacket head = tsPacket;
                TsPacket tail = tsPacket;
                tsPacket = cache.getTsPacket();
                size = tsPacket.loadData(tsdis);
                while (size > 0) {
                    tail.next = tsPacket;
                    tail = tsPacket;
                    tsPacket = cache.getTsPacket();
                    size = tsPacket.loadData(tsdis);
                }
                cache.returnPacketList(tsPacket);
                // Segment composed of valid TS packets, now write to ffmpeg stdin
                TsPacket tsp = head;
                while (tsp != null) {
                    stdin.write(tsp.data);
                    tsp = tsp.next;
                }
                cache.returnPacketList(head);
                updateProgress(i + 1, segCount);
            }
            stdin.flush();
            stdin.close();
            /*
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
             */
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Process failed", e);
            throw new RuntimeException("FFmpeg failed", e);
        }

//        int procStatus = -1;
        int procStatus = 0;
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
            status.episodeHistory.add(se.key.network.historyHref(se.key.href));
            status.saveHistory();
        } else {
            LOGGER.severe("Download of " + se.title + " failed. FFmpeg status = " + procStatus);
            throw new RuntimeException("FFmpeg process ended abnormally");
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
