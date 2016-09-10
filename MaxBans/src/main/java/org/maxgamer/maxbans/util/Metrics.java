package org.maxgamer.maxbans.util;

import java.util.LinkedHashSet;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import org.bukkit.plugin.PluginDescriptionFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Proxy;
import java.net.URL;
import org.bukkit.configuration.InvalidConfigurationException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import java.io.IOException;
import java.util.UUID;
import java.util.Collections;
import java.util.HashSet;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.Set;
import org.bukkit.plugin.Plugin;

public class Metrics
{
    /*private static final int REVISION = 6;
    private static final String BASE_URL = "http://mcstats.org";
    private static final String REPORT_URL = "/report/%s";
    private static final String CUSTOM_DATA_SEPARATOR = "~~";
    private static final int PING_INTERVAL = 10;*/
    private final Plugin plugin;
    private final Set<Graph> graphs;
    private final Graph defaultGraph;
    private final YamlConfiguration configuration;
    private final File configurationFile;
    private final String guid;
    private final boolean debug;
    private final Object optOutLock;
    private volatile BukkitTask task;
    
    public Metrics(final Plugin plugin) throws IOException {
        super();
        this.graphs = Collections.synchronizedSet(new HashSet<Graph>());
        this.defaultGraph = new Graph("Default");
        this.optOutLock = new Object();
        this.task = null;
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
        this.configurationFile = this.getConfigFile();
        (this.configuration = YamlConfiguration.loadConfiguration(this.configurationFile)).addDefault("opt-out", (Object)false);
        this.configuration.addDefault("guid", (Object)UUID.randomUUID().toString());
        this.configuration.addDefault("debug", (Object)false);
        if (this.configuration.get("guid", (Object)null) == null) {
            this.configuration.options().header("http://mcstats.org").copyDefaults(true);
            this.configuration.save(this.configurationFile);
        }
        this.guid = this.configuration.getString("guid");
        this.debug = this.configuration.getBoolean("debug", false);
    }
    
    public Graph createGraph(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Graph name cannot be null");
        }
        final Graph graph = new Graph(name);
        this.graphs.add(graph);
        return graph;
    }
    
    public void addGraph(final Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        this.graphs.add(graph);
    }
    
    public void addCustomData(final Plotter plotter) {
        if (plotter == null) {
            throw new IllegalArgumentException("Plotter cannot be null");
        }
        this.defaultGraph.addPlotter(plotter);
        this.graphs.add(this.defaultGraph);
    }
    
    public boolean start() {
        synchronized (this.optOutLock) {
            if (this.isOptOut()) {
                // monitorexit(this.optOutLock)
                return false;
            }
            if (this.task != null) {
                // monitorexit(this.optOutLock)
                return true;
            }
            this.task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, (Runnable)new Runnable() {
                private boolean firstPost = true;
                
                public void run() {
                    try {
                        synchronized (Metrics.this.optOutLock) {
                            if (Metrics.this.isOptOut() && Metrics.this.task != null) {
                                Metrics.this.task.cancel();
                                Metrics.access$2(Metrics.this, null);
                                for (final Graph graph : Metrics.this.graphs) {
                                    graph.onOptOut();
                                }
                            }
                        }
                        // monitorexit(Metrics.access$0(this.this$0))
                        Metrics.this.postPlugin(!this.firstPost);
                        this.firstPost = false;
                    }
                    catch (IOException e) {
                        if (Metrics.this.debug) {
                            Bukkit.getLogger().log(Level.INFO, "[Metrics] " + e.getMessage());
                        }
                    }
                }
            }, 0L, 12000L);
            // monitorexit(this.optOutLock)
            return true;
        }
    }
    
    public boolean isOptOut() {
        synchronized (this.optOutLock) {
            try {
                this.configuration.load(this.getConfigFile());
            }
            catch (IOException ex) {
                if (this.debug) {
                    Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
                }
                // monitorexit(this.optOutLock)
                return true;
            }
            catch (InvalidConfigurationException ex2) {
                if (this.debug) {
                    Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex2.getMessage());
                }
                // monitorexit(this.optOutLock)
                return true;
            }
            // monitorexit(this.optOutLock)
            return this.configuration.getBoolean("opt-out", false);
        }
    }
    
    public void enable() throws IOException {
        synchronized (this.optOutLock) {
            if (this.isOptOut()) {
                this.configuration.set("opt-out", (Object)false);
                this.configuration.save(this.configurationFile);
            }
            if (this.task == null) {
                this.start();
            }
        }
        // monitorexit(this.optOutLock)
    }
    
    public void disable() throws IOException {
        synchronized (this.optOutLock) {
            if (!this.isOptOut()) {
                this.configuration.set("opt-out", (Object)true);
                this.configuration.save(this.configurationFile);
            }
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }
        }
        // monitorexit(this.optOutLock)
    }
    
    public File getConfigFile() {
        final File pluginsFolder = this.plugin.getDataFolder().getParentFile();
        return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
    }
    
    private void postPlugin(final boolean isPing) throws IOException {
        final PluginDescriptionFile description = this.plugin.getDescription();
        final String pluginName = description.getName();
        final boolean onlineMode = Bukkit.getServer().getOnlineMode();
        final String pluginVersion = description.getVersion();
        final String serverVersion = Bukkit.getVersion();
        final int playersOnline = Bukkit.getServer().getOnlinePlayers().size();
        final StringBuilder data = new StringBuilder();
        data.append(encode("guid")).append('=').append(encode(this.guid));
        encodeDataPair(data, "version", pluginVersion);
        encodeDataPair(data, "server", serverVersion);
        encodeDataPair(data, "players", Integer.toString(playersOnline));
        encodeDataPair(data, "revision", String.valueOf(6));
        final String osname = System.getProperty("os.name");
        String osarch = System.getProperty("os.arch");
        final String osversion = System.getProperty("os.version");
        final String java_version = System.getProperty("java.version");
        final int coreCount = Runtime.getRuntime().availableProcessors();
        if (osarch.equals("amd64")) {
            osarch = "x86_64";
        }
        encodeDataPair(data, "osname", osname);
        encodeDataPair(data, "osarch", osarch);
        encodeDataPair(data, "osversion", osversion);
        encodeDataPair(data, "cores", Integer.toString(coreCount));
        encodeDataPair(data, "online-mode", Boolean.toString(onlineMode));
        encodeDataPair(data, "java_version", java_version);
        if (isPing) {
            encodeDataPair(data, "ping", "true");
        }
        synchronized (this.graphs) {
            for (final Graph graph : this.graphs) {
                for (final Plotter plotter : graph.getPlotters()) {
                    final String key = String.format("C%s%s%s%s", "~~", graph.getName(), "~~", plotter.getColumnName());
                    final String value = Integer.toString(plotter.getValue());
                    encodeDataPair(data, key, value);
                }
            }
        }
        // monitorexit(this.graphs)
        final URL url = new URL("http://mcstats.org" + String.format("/report/%s", encode(pluginName)));
        URLConnection connection;
        if (this.isMineshafterPresent()) {
            connection = url.openConnection(Proxy.NO_PROXY);
        }
        else {
            connection = url.openConnection();
        }
        connection.setDoOutput(true);
        final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data.toString());
        writer.flush();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final String response = reader.readLine();
        writer.close();
        reader.close();
        if (response == null || response.startsWith("ERR")) {
            throw new IOException(response);
        }
        if (response.contains("OK This is your first update this hour")) {
            synchronized (this.graphs) {
                for (final Graph graph2 : this.graphs) {
                    for (final Plotter plotter2 : graph2.getPlotters()) {
                        plotter2.reset();
                    }
                }
            }
            // monitorexit(this.graphs)
        }
    }
    
    private boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
        buffer.append('&').append(encode(key)).append('=').append(encode(value));
    }
    
    private static String encode(final String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }
    
    static /* synthetic */ void access$2(final Metrics metrics, final BukkitTask task) {
        metrics.task = task;
    }
    
    public static class Graph
    {
        private final String name;
        private final Set<Plotter> plotters;
        
        private Graph(final String name) {
            super();
            this.plotters = new LinkedHashSet<Plotter>();
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void addPlotter(final Plotter plotter) {
            this.plotters.add(plotter);
        }
        
        public void removePlotter(final Plotter plotter) {
            this.plotters.remove(plotter);
        }
        
        public Set<Plotter> getPlotters() {
            return Collections.unmodifiableSet((Set<? extends Plotter>)this.plotters);
        }
        
        public int hashCode() {
            return this.name.hashCode();
        }
        
        public boolean equals(final Object object) {
            if (!(object instanceof Graph)) {
                return false;
            }
            final Graph graph = (Graph)object;
            return graph.name.equals(this.name);
        }
        
        protected void onOptOut() {
        }
    }
    
    public abstract static class Plotter
    {
        private final String name;
        
        public Plotter() {
            this("Default");
        }
        
        public Plotter(final String name) {
            super();
            this.name = name;
        }
        
        public abstract int getValue();
        
        public String getColumnName() {
            return this.name;
        }
        
        public void reset() {
        }
        
        public int hashCode() {
            return this.getColumnName().hashCode();
        }
        
        public boolean equals(final Object object) {
            if (!(object instanceof Plotter)) {
                return false;
            }
            final Plotter plotter = (Plotter)object;
            return plotter.name.equals(this.name) && plotter.getValue() == this.getValue();
        }
    }
}
