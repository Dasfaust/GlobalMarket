package com.survivorserver.GlobalMarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.survivorserver.GlobalMarket.Command.MarketCommand;
import com.survivorserver.GlobalMarket.Tasks.CleanTask;
import com.survivorserver.GlobalMarket.Tasks.ExpireTask;
import com.survivorserver.GlobalMarket.Tasks.SaveTask;
import com.survivorserver.GlobalMarket.WebInterface.WebHandler;

public class Market extends JavaPlugin implements Listener {

	Logger log;
	private ArrayList<Integer> tasks;
	static Market market;
	private ConfigHandler config;
	private MarketStorage storageHandler;
	private InterfaceHandler interfaceHandler;
	private MarketCore core;
	private InterfaceListener listener;
	private Economy econ;
	private Permission perms;
	private LocaleHandler locale;
	private Map<String, String> searching;
	private MarketQueue queue;
	private PriceHandler prices;
	private WebHandler webHandler;
	private MarketCommand cmd;
	public String infiniteSeller;
	String prefix;

	public void onEnable() {
		log = getLogger();
		getServer().getPluginManager().registerEvents(this, this);
		tasks = new ArrayList<Integer>();
		market = this;
		reloadConfig();
		getConfig().addDefault("server.enable", false);
		getConfig().addDefault("automatic_payments", false);
		getConfig().addDefault("enable_cut", true);
		getConfig().addDefault("cut_amount", 0.05);
		getConfig().addDefault("cut_account", "");
		getConfig().addDefault("enable_metrics", true);
		getConfig().addDefault("max_price", 0.0);
		getConfig().addDefault("creation_fee", 0.0);
		getConfig().addDefault("queue.trade_time", 0);
		getConfig().addDefault("queue.mail_time", 0);
		getConfig().addDefault("queue.queue_mail_on_buy", true);
		getConfig().addDefault("queue.queue_on_cancel", true);
		getConfig().addDefault("max_listings_per_player", 0);
		getConfig().addDefault("expire_time", 168);
		getConfig().addDefault("price_check.enable", true);
		getConfig().addDefault("infinite.seller", "Server");
		getConfig().addDefault("infinite.account", "");
		getConfig().addDefault("notify_on_update", true);
		
		List<String> b1 = new ArrayList<String>();
		b1.add("Transaction Log");
		b1.add("Market History");
		getConfig().addDefault("blacklist.item_name", b1);
		
		getConfig().addDefault("blacklist.item_id.0", 0);
		
		List<String> b3 = new ArrayList<String>();
		getConfig().addDefault("blacklist.enchant_id", b3);
		
		List<String> b4 = new ArrayList<String>();
		getConfig().addDefault("blacklist.lore", b4);
		
		getConfig().addDefault("blacklist.use_with_mail", false);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
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
        }
        try {
        	Class.forName("net.milkbowl.vault.item.Items");
        	Class.forName("net.milkbowl.vault.item.ItemInfo");
        } catch(Exception e) {
        	log.warning("You have an old or corrupt version of Vault that's missing the Vault Items API. Defaulting to Bukkit item names. Please consider updating Vault!");
        }
		config = new ConfigHandler(this);
		locale = new LocaleHandler(config);
		prefix = locale.get("cmd.prefix");
		storageHandler = new MarketStorage(config, this);
		interfaceHandler = new InterfaceHandler(this, storageHandler);
		interfaceHandler.registerInterface(new ListingsInterface(this));
		interfaceHandler.registerInterface(new MailInterface(this));
		if (getConfig().getBoolean("server.enable")) {
			webHandler = new WebHandler(this);
		}
		core = new MarketCore(this, interfaceHandler, storageHandler);
		listener = new InterfaceListener(this, interfaceHandler, storageHandler, core);
		queue = new MarketQueue(this, storageHandler);
		getServer().getPluginManager().registerEvents(listener, this);
		if (getConfig().getDouble("expire_time") > 0) {
			new ExpireTask(this, storageHandler, core).runTaskTimerAsynchronously(this, 0, 72000);
		}
		tasks.add(getServer().getScheduler().scheduleSyncRepeatingTask(this, new CleanTask(this, interfaceHandler), 0, 20));
		if (getConfig().getBoolean("enable_metrics")) {
			try {
			    MetricsLite metrics = new MetricsLite(this);
			    metrics.start();
			} catch (Exception e) {
			    log.info("Failed to start Metrics!");
			}
		}
		searching = new HashMap<String, String>();
		if (enablePrices()) {
			prices = new PriceHandler(this);
		}
		tasks.add(new SaveTask(log, config).runTaskTimerAsynchronously(this, 0, 1200).getTaskId());
		infiniteSeller = getConfig().getString("infinite.seller");
		cmd = new MarketCommand(this);
		getCommand("market").setExecutor(cmd);
	}
	
	public Economy getEcon() {
		return econ;
	}
	
	public static Market getMarket() {
		return market;
	}	

	public MarketCore getCore() {
		return core;
	}
	
	public MarketStorage getStorage() {
		return storageHandler;
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
	
	public boolean serverEnabled() {
		return getConfig().getBoolean("server.enable");
	}
	
	public double getCut(double amount) {
		if (amount < 10 || !getConfig().getBoolean("enable_cut")) {
			return 0;
		}
		double cut = new BigDecimal(amount * getConfig().getDouble("cut_amount")).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
		String cutAccount = getConfig().getString("cut_account");
		if (cutAccount.length() >= 1) {
			econ.depositPlayer(cutAccount, cut);
		}
		return cut;
	}
	
	public double getCreationFee(double amount) {
		return getConfig().getDouble("creation_fee");
	}
	
	public double getCreationFee(Player player, double amount) {
		if (player.hasPermission("globalmarket.nofee")) {
			return 0;
		}
		return getConfig().getDouble("creation_fee");
	}
	
	public boolean autoPayment() {
		return getConfig().getBoolean("automatic_payments");
	}
	
	public boolean cutTransactions() {
		return getConfig().getBoolean("enable_cut");
	}
	
	public void addSearcher(String name, String interfaceName) {
		searching.put(name, interfaceName);
	}
	
	public double getMaxPrice() {
		return getConfig().getDouble("max_price");
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
	
	public MarketQueue getQueue() {
		return queue;
	}
	
	public int getTradeTime() {
		return getConfig().getInt("queue.trade_time");
	}
	
	public int getMailTime() {
		return getConfig().getInt("queue.mail_time");
	}
	
	public boolean queueOnBuy() {
		return getConfig().getBoolean("queue.queue_mail_on_buy");
	}
	
	public boolean queueOnCancel() {
		return getConfig().getBoolean("queue.queue_mail_on_cancel");
	}
	
	public int maxListings() {
		return getConfig().getInt("max_listings_per_player");
	}
	
	public double getExpireTime() {
		return getConfig().getDouble("expire_time");
	}
	
	public boolean itemBlacklisted(ItemStack item) {
		if (getConfig().isSet("blacklist.item_id." + item.getTypeId())) {
			String path = "blacklist.item_id." + item.getTypeId();
			if (getConfig().getInt(path) == -1 || getConfig().getInt(path) == item.getData().getData()) {
				return true;
			}
		}
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			List<String> bl = getConfig().getStringList("blacklist.item_name");
			if (meta.hasDisplayName()) {
				for (String str : bl) {
					if (meta.getDisplayName().equalsIgnoreCase(str)) {
						return true;
					}
				}
			}
			if (meta instanceof BookMeta) {
				if (((BookMeta) meta).hasTitle()) {
					for (String str : bl) {
						if (((BookMeta) meta).getTitle().equalsIgnoreCase(str)) {
							return true;
						}
					}
				}
			}
			if (meta.hasEnchants()) {
				List<Integer> ebl = getConfig().getIntegerList("blacklist.enchant_id");
				for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
					if (ebl.contains(entry.getKey().getId())) {
						return true;
					}
				}
			}
			if (meta.hasLore()) {
				List<String> lbl = getConfig().getStringList("blacklist.lore");
				List<String> lore = meta.getLore();
				for (String str : lbl) {
					if (lore.contains(str)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean blacklistMail() {
		return getConfig().getBoolean("blacklist.use_with_mail");
	}
	
	public PriceHandler getPrices() {
		return prices;
	}
	
	public boolean enablePrices() {
		return getConfig().getBoolean("price_check.enable");
	}
	
	public String getInfiniteSeller() {
		return infiniteSeller;
	}
	
	public String getInfiniteAccount() {
		return getConfig().getString("infinite.account");
	}

	public String getItemName(ItemStack item) {
		int amount = item.getAmount();
		String itemName = item.getType().toString();
		if (amount > 1) {
			itemName = amount + " " + itemName;
		}
		try {
			Class.forName("net.milkbowl.vault.item.Items");
        	Class.forName("net.milkbowl.vault.item.ItemInfo");
			net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(item.getTypeId());
			if (itemInfo != null) {
				itemName = itemInfo.getName();
			}
		} catch(Exception ignored) { }
		return itemName;
	}
	
	public boolean hasCut(Player buyer, String seller) {
		if (perms != null) {
			return perms.playerHas(buyer.getWorld(), seller, "globalmarket.nocut");
		}
		return false;
	}
	
	public MarketCommand getCmd() {
		return cmd;
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
				Location loc = event.getClickedBlock().getLocation();
				int x = loc.getBlockX();
				int y = loc.getBlockY();
				int z = loc.getBlockZ();
				if (getConfig().isSet("mailbox." + x + "," + y + "," + z)) {
					event.setCancelled(true);
					interfaceHandler.openInterface(player, null, "Mail");
				}
				if (getConfig().isSet("stall." + x + "," + y + "," + z)) {
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
	
	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (storageHandler.getNumMail(player.getName()) > 0) {
			player.sendMessage(prefix + locale.get("you_have_new_mail"));
		}
		if (player.hasPermission("globalmarket.admin")) {
			if (getConfig().getBoolean("notify_on_update")) {
				new UpdateCheck(this, player.getName());
			}
		}
	}
	
	public void onDisable() {
		interfaceHandler.closeAllInterfaces();
		if (getConfig().getBoolean("server.enable")) {
			webHandler.stopServer();
		}
		for(int i = 0; i < tasks.size(); i++) {
			getServer().getScheduler().cancelTask(tasks.get(i));
		}
		config.save();
	}
}