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

import javafx.scene.image.Image;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Status {

    static final Logger LOGGER = Logger.getLogger(Status.class.getName());

    public HttpInput httpInput = new HttpInput();
    public Map<EpisodeKey, SelectedEpisode> selectedEpMap = Collections.synchronizedMap(new HashMap<>());
    public Map<EpisodeKey, VisualEpisode> visualEpMap = new HashMap<>();
    public EpisodeHistory episodeHistory;
    public SeriesSelection seriesSelection;
    public File dataDir;
    public Config config;
    public SbsEpisodes sbsEpisodes = new SbsEpisodes();
    public boolean foundSbsEpisodesOnDisk = false;
    public ExecutorService sbsExecutor = Executors.newSingleThreadExecutor();
    public ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    public ExecutorService downloadExecutor = Executors.newSingleThreadExecutor();
    public SbsTask sbsTask;

    public Status() {
        dataDir = new File(System.getProperty("user.home") + "/.stingodl");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public void loadHistory() {
        File history = new File(dataDir, "history.json");
        if (history.isFile()) {
            try {
                FileReader fileReader = new FileReader(history);
                episodeHistory = JsonConstructiveParser.parse(fileReader, EpisodeHistory.class);
                fileReader.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,"Load history failed", e);
            }
            episodeHistory.populateSet();
            LOGGER.fine("Loaded episode history");
        } else {
            episodeHistory = new EpisodeHistory();
        }
    }

    public void loadSeriesSelection() {
        File series = new File(dataDir, "series.json");
        if (series.isFile()) {
            try {
                FileReader fileReader = new FileReader(series);
                seriesSelection = JsonConstructiveParser.parse(fileReader, SeriesSelection.class);
                fileReader.close();
                seriesSelection.clean();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,"Load series failed", e);
                e.printStackTrace();
            }
            LOGGER.fine("Load series selection " + seriesSelection);
        } else {
            seriesSelection = new SeriesSelection();
            seriesSelection.series = new ArrayList<>();
        }
    }

    public void loadSbsEpisodes() {
        File sbsEps = new File(dataDir, "sbsEpisodes.json");
        if (sbsEps.isFile()) {
            try {
                FileReader fileReader = new FileReader(sbsEps);
                sbsEpisodes = JsonConstructiveParser.parse(fileReader, SbsEpisodes.class);
                fileReader.close();
                foundSbsEpisodesOnDisk = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,"Load SBS episodes failed", e);
                e.printStackTrace();
            }
            LOGGER.fine("Loaded SBS episodes " + sbsEpisodes.episodes.size());
        } else {
            sbsEpisodes = new SbsEpisodes();
            sbsEpisodes.episodes = new ArrayList<>();
        }
        sbsEpisodes.createInitialMaps();
    }

    public void loadConfig() {
        File conf = new File(dataDir, "config.json");
        if (conf.isFile()) {
            try {
                FileReader fileReader = new FileReader(conf);
                config = JsonConstructiveParser.parse(fileReader, Config.class);
                fileReader.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,"Load config failed", e);
            }
            LOGGER.fine("Load config " + config);
        } else {
            config = new Config();
            File home = new File(System.getProperty("user.home"));
            File videos = new File(home, "Videos");
            File movies = new File(home, "Movies");
            if (videos.isDirectory()) {
                config.downloadDir = videos.toString();
            } else if (movies.isDirectory()) {
                config.downloadDir = movies.toString();
            } else {
                config.downloadDir = home.toString();
            }
        }
        Logger rootLogger = Logger.getLogger("stingodl");
        try {
            File logDir = new File(config.downloadDir);
            File logFile = new File(logDir, "stingodl.log");
            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);
            rootLogger.addHandler(fileHandler);
        } catch (IOException ioe) {} //what else can we do here

        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (osName.toLowerCase().startsWith("windows")) {
            if (osArch.endsWith("64")) {
                config.os = "win64";
                config.osExe = ".exe";
            } else if (osArch.endsWith("86")) {
                config.os = "win32";
                config.osExe = ".exe";
            } else {
                config.os = "other";
                config.osExe = ".exe";
            }
        } else if (osName.toLowerCase().startsWith("mac")) {
            if (osArch.endsWith("64")) {
                config.os = "macos64";
                config.osExe = "";
            } else {
                config.os = "other";
                config.osExe = "";
            }
        } else {
            config.os = "other";
            config.osExe = "";
        }

        config.ffmpegText = testFFmpeg();
    }

    public void saveHistory() {
        searchExecutor.submit(new SaveHistory(episodeHistory.copyForSave()));
    }

    public void saveSeriesSelection() {
        searchExecutor.submit(new SaveSeriesSelection(seriesSelection));
    }

    public void saveSbsEpisodes() {
        File sbsEps = new File(dataDir, "sbsEpisodes.json");
        sbsEpisodes.lastFullUpdate = System.currentTimeMillis();
        try {
            JsonObjectSerialiser.toJson(sbsEpisodes, sbsEps);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Save SBS episodes failed", e);
        }
    }

    public void saveConfig() {
        File conf = new File(dataDir, "config.json");
        try {
            JsonObjectSerialiser.toJson(config, conf);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Save config failed", e);
        }
    }

    public String testFFmpeg() {
        config.ffmpegCommandType = "null";
        config.ffmpegVersion = null;

        if ("macos64".equals(config.os) || "win64".equals(config.os)) {
            File pkg = new File(System.getProperty("java.home") + System.getProperty("file.separator") + "ffmpeg" + config.osExe);
            if (pkg.isFile()) {
                config.ffmpegVersion = processFFmpeg(pkg.toString());
                if (config.ffmpegVersion != null) {
                    config.ffmpegCommandType = "pkg";
                    return "StingoDL package version " + config.ffmpegVersion;
                }
            }
        }

        config.ffmpegVersion = processFFmpeg("ffmpeg");
        if (config.ffmpegVersion != null) {
            config.ffmpegCommandType = "global";
            return "Installed global version " + config.ffmpegVersion;
        }

        return "FFmpeg not found";
    }

    public String processFFmpeg(String file) {
        if (file == null) {
            return null;
        }
        String version = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(file,"-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String out = stdOut.readLine();
            StringTokenizer st = new StringTokenizer(out);
            String first = st.nextToken();
            String second = st.nextToken();
            String third = st.nextToken();
            if ("ffmpeg".equals(first) && "version".equals(second)) {
                version = third;
            }
            while (out != null) {
                out = stdOut.readLine();
            }
            p.waitFor();
            config.ffmpegCommand = file;
        } catch (Exception e) {
            LOGGER.log(Level.FINE,"Test FFmpeg failed",e);
        }
        return version;
    }

    public boolean isSbsRecent() {
        if ((sbsEpisodes == null) || (config == null)) {
            return false;
        } else {
            return (System.currentTimeMillis() - sbsEpisodes.lastFullUpdate) <
                    (config.sbsFullUpdateHours * 60 * 60 * 1000);
        }
    }

    public boolean isSbsVeryRecent() {
        if ((sbsEpisodes == null) || (config == null)) {
            return false;
        } else {
            return (System.currentTimeMillis() - sbsEpisodes.lastFullUpdate) <
                    (config.sbsQuickUpdateMinutes * 60 * 1000);
        }
    }

    public void pruneSbsSeries() {
        List<SelectedSeries> current = new ArrayList<>(seriesSelection.series);
        boolean seriesAltered = false;
        for (SelectedSeries s: current) {
            if (s.network.equals("sbs")) {
                SbsSeries series = sbsEpisodes.seriesMap.get(s.href);
                if (series == null) {
                    seriesSelection.remove(Network.SBS, s.href);
                    seriesAltered = true;
                    LOGGER.fine("Removing selected SBS series: " + s.name + " (no longer available)");
                } else if (s.name == null) {
                    s.name = series.series;
                    seriesAltered = true;
                }
            }
        }
        if (seriesAltered) {
            saveSeriesSelection();
        }
    }

    public class SaveHistory implements Runnable {
        EpisodeHistory eh;
        public SaveHistory(EpisodeHistory eh) {
            this.eh = eh;
        }
        @Override
        public void run() {
            File historyFile = new File(dataDir, "history.json");
            try {
                JsonObjectSerialiser.toJson(eh, historyFile);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,"Save history failed", e);
            }
        }
    }

    public class SaveSeriesSelection implements Runnable {
        SeriesSelection series;
        public SaveSeriesSelection(SeriesSelection series) {
            try {
                SaveSeriesSelection.this.series = (SeriesSelection)series.clone();
            } catch (CloneNotSupportedException cnse) {}
        }
        @Override
        public void run() {
            File series = new File(dataDir, "series.json");
            try {
                JsonObjectSerialiser.toJson(seriesSelection, series);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,"Save series failed", e);
            }
        }
    }
}
