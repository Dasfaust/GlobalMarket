package com.survivorserver.GlobalMarket;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.Chat.ChatComponent;
import com.survivorserver.GlobalMarket.Command.MarketCommand;
import com.survivorserver.GlobalMarket.Interface.Handler;
import com.survivorserver.GlobalMarket.Legacy.Importer;
import com.survivorserver.GlobalMarket.Lib.ItemIndex;
import com.survivorserver.GlobalMarket.Lib.PacketManager;
import com.survivorserver.GlobalMarket.Lib.Updater;
import com.survivorserver.GlobalMarket.Lib.Updater.UpdateResult;
import com.survivorserver.GlobalMarket.SQL.AsyncDatabase;
import com.survivorserver.GlobalMarket.SQL.Database;
import com.survivorserver.GlobalMarket.SQL.StorageMethod;
import com.survivorserver.GlobalMarket.Tasks.CleanTask;
import com.survivorserver.GlobalMarket.Tasks.ExpireTask;
import com.survivorserver.GlobalMarket.Tasks.Queue;

public class Market extends JavaPlugin implements Listener {

    public Logger log;
    private ArrayList<Integer> tasks;
    static Market market;
    private ConfigHandler config;
    private InterfaceHandler interfaceHandler;
    private MarketCore core;
    private InterfaceListener listener;
    private Economy econ;
    private Permission perms;
    private LocaleHandler locale;
    private Map<String, String> searching;
    private MarketCommand cmd;
    private HistoryHandler history;
    private AsyncDatabase asyncDb;
    public String infiniteSeller;
    private MarketStorage storage;
    private Map<String, String[]> worldLinks;
    private PacketManager packet;
    private ItemIndex items;
    private ChatComponent chat;
    private Updater updater;
    private boolean mcpcp = false;
    String prefix;

    public void onEnable() {
        log = getLogger();
        tasks = new ArrayList<Integer>();
        market = this;
        reloadConfig();
        getConfig().options().header("GlobalMarket config: " + getDescription().getVersion());
        getConfig().addDefault("storage.type", StorageMethod.SQLITE.toString());
        getConfig().addDefault("storage.mysql_user", "root");
        getConfig().addDefault("storage.mysql_pass", "password");
        getConfig().addDefault("storage.mysql_database", "market");
        getConfig().addDefault("storage.mysql_address", "localhost");
        getConfig().addDefault("storage.mysql_port", 3306);
        getConfig().addDefault("multiworld.enable", false);
        getConfig().addDefault("multiworld.links.world", Arrays.asList(new String[]{"world_nether", "world_the_end"}));
        getConfig().addDefault("limits.default.cut", 0.05);
        getConfig().addDefault("limits.default.max_price", 0.0);
        getConfig().addDefault("limits.default.max_item_prices.air.dmg", 0);
        getConfig().addDefault("limits.default.max_item_prices.air.price", 50.0);
        getConfig().addDefault("limits.default.creation_fee", 0.05);
        getConfig().addDefault("limits.default.max_listings", 0);
        getConfig().addDefault("limits.default.expire_time", 0);
        getConfig().addDefault("limits.default.queue_trade_time", 0);
        getConfig().addDefault("limits.default.queue_mail_time", 0);
        getConfig().addDefault("limits.default.allow_creative", true);
        getConfig().addDefault("limits.default.max_mail", 0);
        getConfig().addDefault("queue.queue_mail_on_buy", true);
        getConfig().addDefault("queue.queue_on_cancel", true);
        getConfig().addDefault("infinite.seller", "Server");
        getConfig().addDefault("infinite.account", "");
        getConfig().addDefault("blacklist.as_whitelist", false);
        getConfig().addDefault("blacklist.custom_names", false);
        getConfig().addDefault("blacklist.item_name", Arrays.asList(new String[]{"Transaction Log", "Market History"}));
        getConfig().addDefault("blacklist.item_id.0", 0);
        getConfig().addDefault("blacklist.enchant_id", Arrays.asList(new String[0]));
        getConfig().addDefault("blacklist.lore", Arrays.asList(new String[0]));
        getConfig().addDefault("blacklist.use_with_mail", false);
        getConfig().addDefault("automatic_payments", false);
        getConfig().addDefault("enable_history", true);
        getConfig().addDefault("announce_new_listings", true);
        getConfig().addDefault("stall_radius", 0);
        getConfig().addDefault("mailbox_radius", 0);
        getConfig().addDefault("new_mail_notification", true);
        getConfig().addDefault("new_mail_notification_delay", 10);
        getConfig().addDefault("enable_metrics", true);
        getConfig().addDefault("notify_on_update", true);

        getConfig().options().copyDefaults(true);
        saveConfig();

        File langFile = new File(getDataFolder().getAbsolutePath() + File.separator + "en_US.lang");
        if (!langFile.exists()) {
            saveResource("en_US.lang", true);
        }
        items = new ItemIndex(this);

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        } else {
            log.severe("Vault has no hooked economy plugin, disabling");
            this.setEnabled(false);
            return;
        }
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permsProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permsProvider != null) {
            perms = permsProvider.getProvider();
        } else {
            log.warning("You do not have a Vault-enabled permissions plugin. Defaulting to default player limits under limits.default in config.yml.");
        }
        boolean plib = false;
        try {
            Class.forName("com.comphenix.protocol.ProtocolManager");
            plib = true;
        } catch(Exception ignored) {}
        if (plib) {
            if (Material.getMaterial("LOG_2") != null) {
                packet = new PacketManager(this);
            } else {
                log.info("ProtocolLib was found but GM only supports ProtocolLib for 1.7 and above.");
            }
        }
        try {
            Class.forName("me.dasfaust.GlobalMarket.MarketCompanion");
            log.info("Market Forge mod detected!");
            mcpcp = true;
        } catch(Exception ignored) {}
        config = new ConfigHandler(this);
        locale = new LocaleHandler(config);
        prefix = locale.get("cmd.prefix");
        cmd = new MarketCommand(this);
        getCommand("market").setExecutor(cmd);
        asyncDb = new AsyncDatabase(this);
        storage = new MarketStorage(this, asyncDb);
        worldLinks = new HashMap<String, String[]>();
        initializeStorage();
    }

    public void initializeStorage() {
        Database db = config.createConnection();
        if (!db.connect()) {
            log.severe("Couldn't connect to the configured database! GlobalMarket can't continue without a connection, please check your config and do /market reload or restart your server");
            return;
        }
        storage.loadSchema(db);
        storage.load(db);
        db.close();
        asyncDb.startTask();
        if (interfaceHandler == null) {
            intialize();
        }
    }

    public void intialize() {
        if (Importer.importNeeded(this)) {
            Importer.importLegacyData(config, storage, this);
        }
        interfaceHandler = new InterfaceHandler(this, storage);
        interfaceHandler.registerInterface(new ListingsInterface(this));
        interfaceHandler.registerInterface(new MailInterface(this));
        core = new MarketCore(this, interfaceHandler, storage);
        listener = new InterfaceListener(this, interfaceHandler, storage, core);
        getServer().getPluginManager().registerEvents(listener, this);
        tasks.add(new ExpireTask(this, config, core, storage).runTaskTimerAsynchronously(this, 0, 72000).getTaskId());
        tasks.add(getServer().getScheduler().scheduleSyncRepeatingTask(this, new CleanTask(this, interfaceHandler), 0, 20));
        tasks.add(new Queue(this).runTaskTimer(this, 0, 1200).getTaskId());
        if (getConfig().getBoolean("enable_metrics")) {
            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (Exception e) {
                log.info("Failed to start Metrics!");
            }
        }
        searching = new HashMap<String, String>();
        if (enableHistory()) {
            history = new HistoryHandler(this, asyncDb, config);
        }
        infiniteSeller = getConfig().getString("infinite.seller");
        getServer().getPluginManager().registerEvents(this, this);
        if (enableMultiworld()) {
            buildWorldLinks();
        }
        chat = new ChatComponent(this);
        updater = new Updater(this, 56267, this.getFile(), Updater.UpdateType.DEFAULT, false);
    }

    public ItemIndex getItemIndex() {
        return items;
    }

    public ChatComponent getChat() {
        return chat;
    }

    public Economy getEcon() {
        return econ;
    }

    public Permission getPerms() {
        return perms;
    }

    public static Market getMarket() {
        return market;
    }

    public MarketStorage getStorage() {
        return storage;
    }

    public MarketCore getCore() {
        return core;
    }

    public LocaleHandler getLocale() {
        return locale;
    }

    public ConfigHandler getConfigHandler() {
        return config;
    }

    public InterfaceHandler getInterfaceHandler() {
        return interfaceHandler;
    }

    public boolean mcpcpSupportEnabled() {
        return mcpcp;
    }

    public boolean useProtocolLib() {
        return packet != null;
    }

    public PacketManager getPacket() {
        return packet;
    }

    public int getStallRadius() {
        return getConfig().getInt("stall_radius");
    }

    public int getMailboxRadius() {
        return getConfig().getInt("mailbox_radius");
    }

    public boolean announceOnCreate() {
        return getConfig().getBoolean("announce_new_listings");
    }

    public int getMaxMail(String player, String world) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.has(world, player, "globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".max_mail");
            }
        }
        return getConfig().getInt("limits.default.max_mail");
    }

    public int getMaxMail(Player player) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".max_mail");
            }
        }
        return getConfig().getInt("limits.default.max_mail");
    }

    public double getCut(double amount, String player, String world) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.has(world, player, "globalmarket.limits." + k)) {
                if (getConfig().isDouble("limits." + k + ".cut")) {
                    return new BigDecimal(amount * getConfig().getDouble("limits." + k + ".cut")).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                } else {
                    return getConfig().getDouble("limits." + k + ".cut");
                }
            }
        }
        if (getConfig().isDouble("limits.default.cut")) {
            return new BigDecimal(amount * getConfig().getDouble("limits.default.cut")).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        } else {
            return getConfig().getDouble("limits.default.cut");
        }
    }

    public double getCreationFee(Player player, double price) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                if (getConfig().isDouble("limits." + k + ".creation_fee")) {
                    return new BigDecimal(price * getConfig().getDouble("limits." + k + ".creation_fee")).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                } else {
                    return getConfig().getDouble("limits." + k + ".creation_fee");
                }
            }
        }
        if (getConfig().isDouble("limits.default.creation_fee")) {
            return new BigDecimal(price * getConfig().getDouble("limits.default.creation_fee")).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        } else {
            return getConfig().getDouble("limits.default.creation_fee");
        }
    }

    public boolean autoPayment() {
        return getConfig().getBoolean("automatic_payments");
    }

    public void addSearcher(String name, String interfaceName) {
        searching.put(name, interfaceName);
    }

    @SuppressWarnings("deprecation")
    public double getMaxPrice(Player player, ItemStack item) {
        String limitGroup = "default";
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                limitGroup = k;
            }
        }
        String itemPath = "limits." + limitGroup + ".max_item_prices." + item.getType().toString().toLowerCase();
        boolean hasPrice = false;
        if (getConfig().isSet(itemPath)) {
            hasPrice = true;
        } else {
            itemPath = "limits." + limitGroup + ".max_item_prices." + item.getTypeId();
            if (getConfig().isSet(itemPath)) {
                hasPrice = true;
            }
        }
        if (hasPrice) {
            int dmg = getConfig().getInt(itemPath + ".dmg");
            if (dmg == -1 || dmg == item.getDurability()) {
                return getConfig().getDouble(itemPath + ".price");
            }
        }
        return getConfig().getDouble("limits." + limitGroup + ".max_price");
    }


    public double getMaxPrice(String player, String world, ItemStack item) {
        String limitGroup = "default";
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.playerHas(world, player, "globalmarket.limits." + k)) {
                limitGroup = k;
            }
        }
        String itemPath = "limits." + limitGroup + ".max_item_prices." + item.getType().toString().toLowerCase();
        if (getConfig().isSet(itemPath)) {
            int dmg = getConfig().getInt(itemPath + ".dmg");
            if (dmg == -1 || dmg == item.getDurability()) {
                return getConfig().getDouble(itemPath + ".price");
            }
        }
        return getConfig().getDouble("limits." + limitGroup + ".max_price");
    }

    public void startSearch(Player player, String interfaceName) {
        player.sendMessage(ChatColor.GREEN + getLocale().get("type_your_search"));
        final String name = player.getName();
        if (!searching.containsKey(name)) {
            addSearcher(name, interfaceName);
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    if (searching.containsKey(name)) {
                        searching.remove(name);
                        Player player = getServer().getPlayer(name);
                        if (player != null) {
                            player.sendMessage(prefix + getLocale().get("search_cancelled"));
                        }
                    }
                }
            }, 200);
        }
    }

    public int getTradeTime(Player player) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".queue_trade_time");
            }
        }
        return getConfig().getInt("limits.default.queue_trade_time");
    }

    public int getTradeTime(String player, String world) {
        if (perms == null) {
            return getConfig().getInt("limits.default.queue_trade_time");
        }
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.playerHas(world, player, "globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".queue_trade_time");
            }
        }
        return getConfig().getInt("limits.default.queue_trade_time");
    }

    public int getMailTime(Player player) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".queue_mail_time");
            }
        }
        return getConfig().getInt("limits.default.queue_mail_time");
    }

    public int getMailTime(String player, String world) {
        if (perms == null) {
            return getConfig().getInt("limits.default.queue_mail_time");
        }
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.playerHas(world, player, "globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".queue_mail_time");
            }
        }
        return getConfig().getInt("limits.default.queue_mail_time");
    }

    public boolean queueOnBuy() {
        return getConfig().getBoolean("queue.queue_mail_on_buy");
    }

    public boolean queueOnCancel() {
        return getConfig().getBoolean("queue.queue_mail_on_cancel");
    }

    public int maxListings(Player player) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".max_listings");
            }
        }
        return getConfig().getInt("limits.default.max_listings");
    }

    public int maxListings(String player, String world) {
        if (perms == null) {
            return getConfig().getInt("limits.default.max_listings");
        }
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.playerHas(world, player, "globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".max_listings");
            }
        }
        return getConfig().getInt("limits.default.max_listings");
    }

    public int getExpireTime(String player, String world) {
        if (perms == null) {
            return getConfig().getInt("limits.default.expire_time");
        }
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (perms.playerHas(world, player, "globalmarket.limits." + k)) {
                return getConfig().getInt("limits." + k + ".expire_time");
            }
        }
        return getConfig().getInt("limits.default.expire_time");
    }

    @SuppressWarnings("deprecation")
    public boolean itemBlacklisted(ItemStack item) {
        boolean isWhitelist = getConfig().getBoolean("blacklist.as_whitelist");
        if (getConfig().isSet("blacklist.item_id." + item.getTypeId())) {
            String path = "blacklist.item_id." + item.getTypeId();
            if (getConfig().isList(path)) {
                if (getConfig().getIntegerList(path).contains(new Integer(item.getDurability()))) {
                    return isWhitelist ? false : true;
                }
            } else {
                if (getConfig().getInt(path) == -1 || getConfig().getInt(path) == item.getDurability()) {
                    return isWhitelist ? false : true;
                }
            }
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            List<String> bl = getConfig().getStringList("blacklist.item_name");
            if (meta.hasDisplayName()) {
                if (getConfig().getBoolean("blacklist.custom_names")) {
                    return isWhitelist ? false : true;
                }
                for (String str : bl) {
                    if (meta.getDisplayName().equalsIgnoreCase(str)) {
                        return isWhitelist ? false : true;
                    }
                }
            }
            if (meta instanceof BookMeta) {
                if (((BookMeta) meta).hasTitle()) {
                    for (String str : bl) {
                        if (((BookMeta) meta).getTitle().equalsIgnoreCase(str)) {
                            return isWhitelist ? false : true;
                        }
                    }
                }
            }
            if (meta.hasEnchants()) {
                List<Integer> ebl = getConfig().getIntegerList("blacklist.enchant_id");
                for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    if (ebl.contains(entry.getKey().getId())) {
                        return isWhitelist ? false : true;
                    }
                }
            }
            if (meta.hasLore()) {
                List<String> lbl = getConfig().getStringList("blacklist.lore");
                List<String> lore = meta.getLore();
                for (String str : lbl) {
                    if (lore.contains(str)) {
                        return isWhitelist ? false : true;
                    }
                }
            }
        }
        return isWhitelist ? true : false;
    }

    public boolean blacklistMail() {
        return getConfig().getBoolean("blacklist.use_with_mail");
    }

    public boolean enableHistory() {
        return getConfig().getBoolean("enable_history");
    }

    public String getInfiniteSeller() {
        return infiniteSeller;
    }

    public String getInfiniteAccount() {
        return getConfig().getString("infinite.account");
    }

    public String getItemName(ItemStack item) {
        if (mcpcp) {
            return ((me.dasfaust.GlobalMarket.WrappedItemStack) item).getItemName();
        }
        String itemName = items.getItemName(item);
        if (item.getAmount() > 1) {
            return locale.get("friendly_item_name_with_amount", item.getAmount(), itemName);
        } else {
            return locale.get("friendly_item_name", itemName);
        }
    }

    public String getItemNameSingle(ItemStack item) {
        return items.getItemName(item);
    }

    public MarketCommand getCmd() {
        return cmd;
    }

    public HistoryHandler getHistory() {
        return history;
    }

    public boolean enableMultiworld() {
        return getConfig().getBoolean("multiworld.enable");
    }

    public String[] getLinkedWorlds(String world) {
        return worldLinks.containsKey(world) ? worldLinks.get(world) : new String[0];
    }

    public void buildWorldLinks() {
        worldLinks.clear();
        Map<String, List<String>> links = new HashMap<String, List<String>>();
        Set<String> linkList = getConfig().getConfigurationSection("multiworld.links").getKeys(false);
        for (World wor : getServer().getWorlds()) {
            String world = wor.getName();
            links.put(world, linkList.contains(world) ? getConfig().getStringList("multiworld.links." + world) : new ArrayList<String>());
            for (String w : linkList) {
                if (!w.equalsIgnoreCase(world)) {
                    if (getConfig().getStringList("multiworld.links." + w).contains(world)) {
                        if (!links.get(world).contains(w)) {
                            links.get(world).add(w);
                        }
                    }
                }
            }
        }
        for (Entry<String, List<String>> entry : links.entrySet()) {
            worldLinks.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
    }

    public boolean allowCreative(Player player) {
        for (String  k : getConfig().getConfigurationSection("limits").getKeys(false)) {
            if (player.hasPermission("globalmarket.limits." + k)) {
                return getConfig().getBoolean("limits." + k + ".allow_creative");
            }
        }
        return getConfig().getBoolean("limits.default.allow_creative");
    }

    public void notifyPlayer(String who, String notification) {
        Player player = getServer().getPlayer(who);
        if (player != null) {
            player.sendMessage(locale.get("cmd.prefix") + notification);
        }
        for (Handler handler : interfaceHandler.getHandlers()) {
            handler.notifyPlayer(who, notification);
        }
    }

    public List<Location> getStallLocations() {
        List<Location> locations = new ArrayList<Location>();
        if (getConfig().isSet("stall")) {
            for (String loc : getConfig().getConfigurationSection("stall").getKeys(false)) {
                try {
                    locations.add(locationFromString(loc));
                } catch(IllegalArgumentException ignored) {}
            }
        }
        return locations;
    }

    public List<Location> getMailboxLocations() {
        List<Location> locations = new ArrayList<Location>();
        if (getConfig().isSet("mailbox")) {
            for (String loc : getConfig().getConfigurationSection("mailbox").getKeys(false)) {
                try {
                    locations.add(locationFromString(loc));
                } catch(IllegalArgumentException ignored) {}
            }
        }
        return locations;
    }

    public String locationToString(Location loc) {
        StringBuilder sb = new StringBuilder();
        sb.append(loc.getWorld().getName());
        sb.append(",");
        sb.append(loc.getBlockX());
        sb.append(",");
        sb.append(loc.getBlockY());
        sb.append(",");
        sb.append(loc.getBlockZ());
        return sb.toString();
    }

    public Location locationFromString(String loc) {
        String[] xyz = loc.split(",");
        if (xyz.length < 4) {
            throw new IllegalArgumentException("Invalid location string");
        }
        World world = Bukkit.getServer().getWorld(xyz[0]);
        if (world == null) {
            throw new IllegalArgumentException("World no longer exists");
        }
        return new Location(world, Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2]), Double.parseDouble(xyz[3]));
    }

    public void onQuit(PlayerQuitEvent event) {
        if (interfaceHandler != null) {
            interfaceHandler.purgeViewer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (searching.containsKey(player.getName())) {
            event.setCancelled(true);
            String search = event.getMessage();
            if (search.equalsIgnoreCase("cancel")) {
                interfaceHandler.openInterface(player, null, searching.get(player.getName()));
                searching.remove(player.getName());
            } else {
                interfaceHandler.openInterface(player, search, searching.get(player.getName()));
                searching.remove(player.getName());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.isCancelled() && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.CHEST
                    // Trapped chest
                    || event.getClickedBlock().getTypeId() == 146
                    || event.getClickedBlock().getType() == Material.SIGN
                    || event.getClickedBlock().getType() == Material.SIGN_POST
                    || event.getClickedBlock().getType() == Material.WALL_SIGN) {
                Player player = event.getPlayer();
                Location location = event.getClickedBlock().getLocation();
                String loc = locationToString(location);
                if (getConfig().isSet("mailbox." + loc)) {
                    if (player.getGameMode() == GameMode.CREATIVE && !allowCreative(player)) {
                        player.sendMessage(ChatColor.RED + locale.get("not_allowed_while_in_creative"));
                        return;
                    }
                    event.setCancelled(true);
                    interfaceHandler.openInterface(player, null, "Mail");
                }
                if (getConfig().isSet("stall." + loc)) {
                    if (player.getGameMode() == GameMode.CREATIVE && !allowCreative(player)) {
                        player.sendMessage(ChatColor.RED + locale.get("not_allowed_while_in_creative"));
                        return;
                    }
                    event.setCancelled(true);
                    if (event.getClickedBlock().getType() == Material.SIGN
                            || event.getClickedBlock().getType() == Material.SIGN_POST
                            || event.getClickedBlock().getType() == Material.WALL_SIGN) {
                        Sign sign = (Sign) event.getClickedBlock().getState();
                        String line = sign.getLine(3);
                        if (line != null && line.length() > 0) {
                            interfaceHandler.openInterface(player, line, "Listings");
                            return;
                        }
                    }
                    if (event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        startSearch(player, "Listings");
                    } else {
                        interfaceHandler.openInterface(player, null, "Listings");
                    }
                }
            }
        }
        ItemStack item = event.getPlayer().getItemInHand();
        if (item != null && listener.isMarketItem(item)) {
            item.setType(Material.AIR);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST
                // Trapped chest
                || block.getTypeId() == 146
                || block.getType() == Material.SIGN
                || block.getType() == Material.SIGN_POST
                || block.getType() == Material.WALL_SIGN) {
            Location location = block.getLocation();
            String loc = locationToString(location);
            if (getConfig().isSet("mailbox." + loc)) {
                getConfig().set("mailbox." + loc, null);
                saveConfig();
                event.getPlayer().sendMessage(ChatColor.YELLOW + locale.get("mailbox_removed"));
            }
            if (getConfig().isSet("stall." + loc)) {
                getConfig().set("stall." + loc, null);
                saveConfig();
                event.getPlayer().sendMessage(ChatColor.YELLOW + locale.get("stall_removed"));
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        if (getConfig().getBoolean("new_mail_notification")) {
            new BukkitRunnable() {
                public void run() {
                    Player player = market.getServer().getPlayer(name);
                    if (player != null) {
                        if (storage.getNumMail(player.getName(), player.getWorld().getName(), false) > 0) {
                            player.sendMessage(prefix + locale.get("you_have_new_mail"));
                        }
                    }
                }
            }.runTaskLater(this, getConfig().getInt("new_mail_notification_delay"));
        }
        final Player player = event.getPlayer();
        if (player.hasPermission("globalmarket.admin")) {
            if (getConfig().getBoolean("notify_on_update")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                            player.sendMessage(prefix + "A new version is available: " + updater.getLatestName());
                        }
                    }
                }.runTaskAsynchronously(this);
            }
        }
    }

    public void onDisable() {
        interfaceHandler.closeAllInterfaces();
        for(int i = 0; i < tasks.size(); i++) {
            getServer().getScheduler().cancelTask(tasks.get(i));
        }
        asyncDb.cancel();
        if (asyncDb.isProcessing()) {
            log.info("Please wait while the database queue is processed.");
            while(asyncDb.isProcessing()) {}
        }
        asyncDb.processQueue(true);
        asyncDb.close();
        if (packet != null) {
            packet.unregister();
        }
    }
}