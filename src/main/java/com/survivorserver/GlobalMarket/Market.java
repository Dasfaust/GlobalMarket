package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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

import com.survivorserver.GlobalMarket.tasks.CleanTask;
import com.survivorserver.GlobalMarket.tasks.ExpireTask;
import com.survivorserver.GlobalMarket.tasks.SaveTask;

public class Market extends JavaPlugin implements Listener {

	Logger log;
	ArrayList<Integer> tasks;
	static Market market;
	ConfigHandler config;
	MarketStorage storageHandler;
	MarketServer server;
	InterfaceHandler interfaceHandler;
	MarketCore core;
	InterfaceListener listener;
	Economy econ;
	LocaleHandler locale;
	String prefix;
	boolean bukkitItems = false;
	List<String> searching;
	MarketQueue queue;
	PriceHandler prices;
	String infiniteSeller;

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
		
		List<String> b1 = new ArrayList<String>();
		b1.add("Transaction Log");
		b1.add("Market History");
		getConfig().addDefault("blacklist.item_name", b1);
		
		getConfig().addDefault("blacklist.item_id.0", 0);
		
		List<String> b3 = new ArrayList<String>();
		getConfig().addDefault("blacklist.enchant_id", b3);
		
		List<String> b4 = new ArrayList<String>();
		getConfig().addDefault("blacklist.enchant_lore", b4);
		
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
        try {
        	Class.forName("net.milkbowl.vault.item.Items");
        	Class.forName("net.milkbowl.vault.item.ItemInfo");
        } catch(Exception e) {
        	log.warning("You have an old or corrupt version of Vault that's missing the Vault Items API. Defaulting to Bukkit item names. Please consider updating Vault!");
        	bukkitItems = true;
        }
		config = new ConfigHandler(this);
		locale = new LocaleHandler(config);
		prefix = locale.get("cmd.prefix");
		storageHandler = new MarketStorage(config, this);
		interfaceHandler = new InterfaceHandler(this, storageHandler);
		if (getConfig().getBoolean("server.enable")) {
			server = new MarketServer(this, storageHandler, interfaceHandler);
			server.start();
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
		searching = new ArrayList<String>();
		if (enablePrices()) {
			prices = new PriceHandler(this);
		}
		tasks.add(new SaveTask(log, config).runTaskTimerAsynchronously(this, 0, 1200).getTaskId());
		infiniteSeller = getConfig().getString("infinite.seller");
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
	
	public boolean serverEnabled() {
		if (server != null) {
			return true;
		}
		return false;
	}
	
	public MarketServer server() {
		return server;
	}
	
	public double getCut(double amount) {
		if (amount < 10 || !getConfig().getBoolean("enable_cut")) {
			return 0;
		}
		return amount * getConfig().getDouble("cut_amount");
	}
	
	public double getCreationFee(double amount) {
		double fee = getConfig().getDouble("creation_fee");
		return amount * fee;
	}
	
	public boolean autoPayment() {
		return getConfig().getBoolean("automatic_payments");
	}
	
	public boolean cutTransactions() {
		return getConfig().getBoolean("enable_cut");
	}
	
	public void addSearcher(String name) {
		searching.add(name);
	}
	
	public double getMaxPrice() {
		return getConfig().getDouble("max_price");
	}
	
	public void startSearch(Player player) {
		player.sendMessage(ChatColor.GREEN + getLocale().get("type_your_search"));
		final String name = player.getName();
		if (!searching.contains(name)) {
			addSearcher(name);
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					if (searching.contains(name)) {
						searching.remove(name);
						Player player = market.getServer().getPlayer(name);
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
				List<String> lbl = getConfig().getStringList("blacklist.enchant_id");
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
		String itemName = item.getType().toString();
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (searching.contains(player.getName())) {
			event.setCancelled(true);
			String search = event.getMessage();
			if (search.equalsIgnoreCase("cancel")) {
				searching.remove(player.getName());
				interfaceHandler.showListings(player, null);
			} else {
				interfaceHandler.showListings(player, search);
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
					interfaceHandler.showMail(player);
				}
				if (getConfig().isSet("stall." + x + "," + y + "," + z)) {
					event.setCancelled(true);
					if (event.getClickedBlock().getType() == Material.SIGN
							|| event.getClickedBlock().getType() == Material.SIGN_POST
							|| event.getClickedBlock().getType() == Material.WALL_SIGN) {
						Sign sign = (Sign) event.getClickedBlock().getState();
						String line = sign.getLine(3);
						if (line != null && line.length() > 0) {
							interfaceHandler.showListings(player, line);
							return;
						}
					}
					if (event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						startSearch(player);
					} else {
						interfaceHandler.showListings(player, null);
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
			new UpdateCheck(this, player.getName());
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("market")) {
			if (args.length < 1 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				sender.sendMessage(prefix + locale.get("cmd.help_legend"));
				sender.sendMessage(prefix + locale.get("cmd.listings_syntax") + " " + locale.get("cmd.listings_descr"));
				sender.sendMessage(prefix + locale.get("cmd.create_syntax") + " " + locale.get("cmd.create_descr"));
				if (sender.hasPermission("globalmarket.quickmail")) {
					sender.sendMessage(prefix + locale.get("cmd.mail_syntax") + " " + locale.get("cmd.mail_descr"));
				}
				if (sender.hasPermission("globalmarket.pricecheck")) {
					sender.sendMessage(prefix + locale.get("cmd.pc_syntax") + " " + locale.get("cmd.pc_descr"));
				}
				if (sender.hasPermission("globalmarket.util")) {
					sender.sendMessage(prefix + locale.get("cmd.mailbox_syntax") + " " + locale.get("cmd.mailbox_descr"));
					sender.sendMessage(prefix + locale.get("cmd.stall_syntax") + " " + locale.get("cmd.stall_descr"));
				}
				if (sender.hasPermission("globalmarket.history")) {
					sender.sendMessage(prefix + locale.get("cmd.history_syntax") + " " + locale.get("cmd.history_descr"));
				}
				sender.sendMessage(prefix + locale.get("cmd.send_syntax") + " " + locale.get("cmd.send_descr"));
				if (sender.hasPermission("globalmarket.admin")) {
					sender.sendMessage(prefix + locale.get("cmd.reload_syntax") + " " + locale.get("cmd.reload_descr"));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("globalmarket.admin")) {
				reloadConfig();
				config.reloadLocaleYML();
				locale.setSelected();
				infiniteSeller = getConfig().getString("infinite.seller");
				sender.sendMessage(prefix + market.getLocale().get("config_reloaded"));
				return true;
			}
			if (sender instanceof ConsoleCommandSender) {
				sender.sendMessage(prefix + locale.get("player_context_required"));
				return true;
			}
			if (args[0].equalsIgnoreCase("mail") && sender.hasPermission("globalmarket.quickmail")) {
				Player player = (Player) sender;
				interfaceHandler.showMail(player);
				return true;
			}
			if (args[0].equalsIgnoreCase("send")) {
				if (sender.hasPermission("globalmarket.send")) {
					Player player = (Player) sender;
					if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && args.length >= 2) {
						if (blacklistMail()) {
							if (itemBlacklisted(player.getItemInHand())) {
								sender.sendMessage(ChatColor.RED + locale.get("item_is_blacklisted_from_mail"));
								return true;
							}
						}
						if (args.length < 2) {
							sender.sendMessage(prefix + locale.get("cmd.send_syntax"));
							return true;
						}
						if (args[1].equalsIgnoreCase(player.getName())) {
							sender.sendMessage(prefix + locale.get("cant_mail_to_self"));
							return true;
						}
						OfflinePlayer off = getServer().getOfflinePlayer(args[1]);
						if (!off.hasPlayedBefore()) {
							sender.sendMessage(prefix + locale.get("player_not_found", args[1]));
							return true;
						}
						args[1] = off.getName();
						if (args.length == 3) {
							int amount = 0;
							try {
								amount = Integer.parseInt(args[2]);
							} catch(Exception e) {
								player.sendMessage(ChatColor.RED + locale.get("not_a_valid_number", args[2]));
								return true;
							}
							if (amount <= 0) {
								player.sendMessage(ChatColor.RED + locale.get("not_a_valid_amount", args[2]));
								return true;
							}
							if (player.getItemInHand().getAmount() < amount) {
								player.sendMessage(ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount));
								return true;
							}
							ItemStack toList = new ItemStack(player.getItemInHand());
							if (player.getItemInHand().getAmount() == amount) {
								player.setItemInHand(new ItemStack(Material.AIR));
							} else {
								player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
							}
							toList.setAmount(amount);
							if (getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
								queue.queueMail(toList, args[1]);
								sender.sendMessage(prefix + locale.get("item_will_send"));
							} else {
								storageHandler.storeMail(toList, args[1], true);
								sender.sendMessage(prefix + locale.get("item_sent"));
							}
						} else {
							ItemStack toList = new ItemStack(player.getItemInHand());
							if (getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
								queue.queueMail(toList, args[1]);
								sender.sendMessage(prefix + locale.get("item_will_send"));
							} else {
								storageHandler.storeMail(toList, args[1], true);
								sender.sendMessage(prefix + locale.get("item_sent"));
							}
							player.setItemInHand(new ItemStack(Material.AIR));
						}
					} else {
						sender.sendMessage(prefix + locale.get("hold_an_item") + " " + locale.get("cmd.send_syntax"));
					}
				} else {
					sender.sendMessage(ChatColor.YELLOW + locale.get("no_permission_for_this_command"));
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("listings")) {
				if (sender.hasPermission("globalmarket.quicklist")) {
					Player player = (Player) sender;
					String search = null;
					if (args.length >= 2) {
						search = args[1];
						if (args.length > 2) {
							for (int i = 2 ; i < args.length ; i++) {
								search = search + " " + args[i];
							}
						}
					}
					interfaceHandler.showListings(player, search);
				} else {
					sender.sendMessage(ChatColor.YELLOW + locale.get("no_permission_for_this_command"));
					return true;
				}
				return true;
			}
			if (sender.hasPermission("globalmarket.history")) {
				if (args[0].equalsIgnoreCase("history")) {
					Player player = (Player) sender;
					core.showHistory(player);
					sender.sendMessage(ChatColor.GREEN + locale.get("check_your_inventory"));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("pricecheck") || args[0].equalsIgnoreCase("pc")) {
				Player player = (Player) sender;
				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					ItemStack item = player.getItemInHand();
					sender.sendMessage(prices.getPricesInformation(item));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("create")) {
				if (sender.hasPermission("globalmarket.create")) {
					Player player = (Player) sender;
					if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && args.length >= 2) {
						if (itemBlacklisted(player.getItemInHand())) {
							sender.sendMessage(ChatColor.RED + locale.get("item_is_blacklisted"));
							return true;
						}
						double price = 0;
						try {
							price = Double.parseDouble(args[1]);
						} catch(Exception e) {
							player.sendMessage(ChatColor.RED + locale.get("not_a_valid_number", args[1]));
							return true;
						}
						if (price < 0.01) {
							sender.sendMessage(prefix + locale.get("price_too_low"));
							return true;
						}
						double maxPrice = getMaxPrice();
						if (maxPrice > 0 && price > maxPrice && !sender.hasPermission("globalmarket.nolimit.maxprice")) {
							sender.sendMessage(prefix + locale.get("price_too_high"));
							return true;
						}
						double fee = getCreationFee(price);
						if (maxListings() > 0 && storageHandler.getNumListings(sender.getName()) >= maxListings() && !sender.hasPermission("globalmarket.nolimit.maxlistings")) {
							sender.sendMessage(ChatColor.RED + locale.get("selling_too_many_items"));
							return true;
						}
						boolean infinite = false;
						for (int i = 0; i < args.length; i++) {
							if (args[i].equalsIgnoreCase("-inf") && sender.hasPermission("globalmarket.infinitelistings")) {
								infinite = true;
							}
						}
						if ((args.length == 3 && !infinite) || (args.length == 4 && infinite)) {
							int amount = 0;
							try {
								amount = Integer.parseInt(args[2]);
							} catch(Exception e) {
								player.sendMessage(ChatColor.RED + locale.get("not_a_valid_number", args[2]));
								return true;
							}
							if (amount <= 0) {
								player.sendMessage(ChatColor.RED + locale.get("not_a_valid_amount", args[2]));
								return true;
							}
							if (!infinite && player.getItemInHand().getAmount() < amount) {
								player.sendMessage(ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount));
								return true;
							}
							if (fee > 0) {
								if (econ.has(sender.getName(), fee)) {
									econ.withdrawPlayer(sender.getName(), fee);
									storageHandler.incrementSpent(sender.getName(), fee);
								} else {
									sender.sendMessage(ChatColor.RED + locale.get("you_cant_pay_this_fee"));
									return true;
								}
							}
							ItemStack toList = new ItemStack(player.getItemInHand());
							if (player.getItemInHand().getAmount() == amount) {
								if (!infinite) {
									player.setItemInHand(new ItemStack(Material.AIR));
								}
							} else {
								if (!infinite) {
									player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
								}
							}
							toList.setAmount(amount);
							if (getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
								queue.queueListing(toList, player.getName(), price);
								sender.sendMessage(ChatColor.GREEN + locale.get("item_queued", getTradeTime()));
							} else {
								storageHandler.storeListing(toList, infinite ? getInfiniteSeller() : player.getName(), price);
								sender.sendMessage(ChatColor.GREEN + locale.get("item_listed"));
							}
							if (fee > 0) {
								player.sendMessage(ChatColor.GREEN + locale.get("charged_fee", econ.format(fee)));
							}
							String itemName = getItemName(toList);
							storageHandler.storeHistory(player.getName(), locale.get("history.item_listed", itemName + "x" + toList.getAmount(), price));
						} else {
							if (fee > 0) {
								if (econ.has(sender.getName(), fee)) {
									econ.withdrawPlayer(sender.getName(), fee);
									storageHandler.incrementSpent(sender.getName(), fee);
								} else {
									sender.sendMessage(ChatColor.RED + locale.get("you_cant_pay_this_fee"));
									return true;
								}
							}
							ItemStack toList = new ItemStack(player.getItemInHand());
							if (getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
								queue.queueListing(toList, player.getName(), price);
								sender.sendMessage(ChatColor.GREEN + locale.get("item_queued", getTradeTime()));
							} else {
								storageHandler.storeListing(toList, infinite ? getInfiniteSeller() : player.getName(), price);
								sender.sendMessage(ChatColor.GREEN + locale.get("item_listed"));
							}
							if (fee > 0) {
								player.sendMessage(ChatColor.GREEN + locale.get("charged_fee", econ.format(fee)));
							}
							if (!infinite) {
								player.setItemInHand(new ItemStack(Material.AIR));
							}
							String itemName = getItemName(toList);
							storageHandler.storeHistory(player.getName(), locale.get("history.item_listed", itemName + "x" + toList.getAmount(), price));
						}
					} else {
						sender.sendMessage(prefix + locale.get("hold_an_item") + " " + locale.get("cmd.create_syntax"));
					}
				} else {
					sender.sendMessage(ChatColor.YELLOW + locale.get("no_permission_for_this_command"));
					return true;
				}
				return true;
			}
			if (sender.hasPermission("globalmarket.util")) {
				if (args[0].equalsIgnoreCase("mailbox") || args[0].equalsIgnoreCase("stall")) {
					Player player = (Player) sender;
					Location loc = null;
					Block block = player.getTargetBlock(null, 4);
					if (block.getType() == Material.CHEST
							// Trapped chest
							|| block.getTypeId() == 146
							|| block.getType() == Material.SIGN
							|| block.getType() == Material.SIGN_POST
							|| block.getType() == Material.WALL_SIGN) {
						loc = block.getLocation();
					} else {
						player.sendMessage(ChatColor.RED + locale.get("aim_cursor_at_chest_or_sign"));
						return true;
					}
					int x = loc.getBlockX();
					int y = loc.getBlockY();
					int z = loc.getBlockZ();
					if (args.length == 2 && args[1].equalsIgnoreCase("remove")) {
						if (getConfig().isSet("mailbox." + x + "," + y + "," + z)) {
							getConfig().set("mailbox." + x + "," + y + "," + z, null);
							saveConfig();
							player.sendMessage(ChatColor.YELLOW + locale.get("mailbox_removed"));
							return true;
						} else if (getConfig().isSet("stall." + x + "," + y + "," + z)) {
							getConfig().set("stall." + x + "," + y + "," + z, null);
							saveConfig();
							player.sendMessage(ChatColor.YELLOW + locale.get("stall_removed"));
							return true;
						} else {
							player.sendMessage(ChatColor.RED + locale.get("no_stall_found"));
							return true;
						}
					}
					if (getConfig().isSet("mailbox." + x + "," + y + "," + z)) {
						sender.sendMessage(ChatColor.RED + locale.get("mailbox_already_exists"));
						return true;
					} else if (getConfig().isSet("stall." + x + "," + y + "," + z)) {
						sender.sendMessage(ChatColor.RED + locale.get("stall_already_exists"));
						return true;
					}
					if (args[0].equalsIgnoreCase("mailbox")) {
						getConfig().set("mailbox." + x + "," + y + "," + z, true);
						saveConfig();
						sender.sendMessage(ChatColor.GREEN + locale.get("mailbox_added"));
						return true;
					} else if (args[0].equalsIgnoreCase("stall")) {
						getConfig().set("stall." + x + "," + y + "," + z, true);
						saveConfig();
						sender.sendMessage(ChatColor.GREEN + locale.get("stall_added"));
						return true;
					}
				}
			}
			sender.sendMessage(prefix + locale.get("cmd.help"));
		}
		return true;
	}
	
	public void onDisable() {
		interfaceHandler.closeAllInterfaces();
		if (getConfig().getBoolean("server.enable")) {
			server.setDisabled();
		}
		for(int i = 0; i < tasks.size(); i++) {
			getServer().getScheduler().cancelTask(tasks.get(i));
		}
		config.save();
	}
}