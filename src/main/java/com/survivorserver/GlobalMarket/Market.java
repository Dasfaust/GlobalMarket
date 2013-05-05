package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.survivorserver.GlobalMarket.tasks.CleanTask;
import com.survivorserver.GlobalMarket.tasks.ExpireTask;

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

	public void onEnable() {
		log = getLogger();
		getServer().getPluginManager().registerEvents(this, this);
		tasks = new ArrayList<Integer>();
		market = this;
		if (!getConfig().isSet("server.enable")) {
			getConfig().set("server.enable", false);
			saveConfig();
		}
		if (!getConfig().isSet("automatic_payments")) {
			getConfig().set("automatic_payments", false);
			saveConfig();
		}
		if (!getConfig().isSet("enable_cut")) {
			getConfig().set("enable_cut", true);
			saveConfig();
		}
		if (!getConfig().isSet("cut_amount")) {
			getConfig().set("cut_amount", (double) 0.05);
			saveConfig();
		} else if (getConfig().getDouble("cut_amount") >= 1.0) {
			getConfig().set("cut_amount", (double) 0.05);
			saveConfig();
		}
		if (!getConfig().isSet("enable_metrics")) {
			getConfig().set("enable_metrics", true);
			saveConfig();
		}
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
		if (getConfig().getBoolean("server.enable")) {
			server = new MarketServer(this, storageHandler);
			server.start();
		}
		interfaceHandler = new InterfaceHandler(this, storageHandler);
		core = new MarketCore(this, interfaceHandler, storageHandler);
		listener = new InterfaceListener(this, interfaceHandler, storageHandler, core);
		getServer().getPluginManager().registerEvents(listener, this);
		tasks.add(getServer().getScheduler().scheduleSyncRepeatingTask(this, new ExpireTask(this, storageHandler, core), 0, 72000));
		tasks.add(getServer().getScheduler().scheduleSyncRepeatingTask(this, new CleanTask(this, interfaceHandler), 0, 20));
		if (getConfig().getBoolean("enable_metrics")) {
			try {
			    MetricsLite metrics = new MetricsLite(this);
			    metrics.start();
			} catch (Exception e) {
			    log.info("Failed to start Metrics!");
			}
		}
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
	
	public boolean autoPayment() {
		return getConfig().getBoolean("automatic_payments");
	}
	
	public boolean cutTransactions() {
		return getConfig().getBoolean("enable_cut");
	}
	
	public boolean useBukkitNames() {
		try {
        	Class.forName("net.milkbowl.vault.item.Items");
        	Class.forName("net.milkbowl.vault.item.ItemInfo");
        } catch(Exception e) {
        	return true;
        }
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRightClick(PlayerInteractEvent event) {
		if (!event.isCancelled() && event.getClickedBlock() != null
				&& event.getClickedBlock().getType() == Material.CHEST
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
		}
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (storageHandler.getNumMail(player.getName()) > 0) {
			player.sendMessage(prefix + locale.get("you_have_new_mail"));
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("market")) {
			if (sender instanceof ConsoleCommandSender) {
				sender.sendMessage(prefix + locale.get("player_context_required"));
				return true;
			}
			if (args.length < 1 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				sender.sendMessage(prefix + locale.get("cmd.help_legend"));
				sender.sendMessage(prefix + locale.get("cmd.listings_syntax") + " " + locale.get("cmd.listings_descr"));
				sender.sendMessage(prefix + locale.get("cmd.create_syntax") + " " + locale.get("cmd.create_descr"));
				if (sender.hasPermission("market.quickmail")) {
					sender.sendMessage(prefix + locale.get("cmd.mail_syntax") + " " + locale.get("cmd.mail_descr"));
				}
				if (sender.hasPermission("market.util")) {
					sender.sendMessage(prefix + locale.get("cmd.mailbox_syntax") + " " + locale.get("cmd.mailbox_descr"));
				}
				sender.sendMessage(prefix + locale.get("cmd.history_syntax") + " " + locale.get("cmd.history_descr"));
				sender.sendMessage(prefix + locale.get("cmd.send_syntax") + " " + locale.get("cmd.send_descr"));
				if (sender.hasPermission("market.admin")) {
					sender.sendMessage(prefix + locale.get("cmd.reload_syntax") + " " + locale.get("cmd.reload_descr"));
				}
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
							sender.sendMessage(prefix + locale.get("player_not_found"));
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
							if (player.getItemInHand().getAmount() < amount) {
								player.sendMessage(ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount));
								return true;
							}
							ItemStack toList = player.getItemInHand().clone();
							if (player.getItemInHand().getAmount() == amount) {
								player.setItemInHand(new ItemStack(Material.AIR));
							} else {
								player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
							}
							toList.setAmount(amount);
							storageHandler.storeMail(toList, args[1], true);
							sender.sendMessage(prefix + locale.get("item_sent"));
						} else {
							ItemStack toList = player.getItemInHand().clone();
							player.setItemInHand(new ItemStack(Material.AIR));
							storageHandler.storeMail(toList, args[1], true);
							sender.sendMessage(prefix + locale.get("item_sent"));
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
				return true;
			}
			if (args[0].equalsIgnoreCase("history")) {
				Player player = (Player) sender;
				core.showHistory(player);
				sender.sendMessage(ChatColor.GREEN + locale.get("check_your_inventory"));
				return true;
			}
			if (args[0].equalsIgnoreCase("create")) {
				if (sender.hasPermission("globalmarket.create")) {
					Player player = (Player) sender;
					if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && args.length >= 2) {
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
						if (args.length == 3) {
							int amount = 0;
							try {
								amount = Integer.parseInt(args[2]);
							} catch(Exception e) {
								player.sendMessage(ChatColor.RED + locale.get("not_a_valid_number", args[2]));
								return true;
							}
							if (player.getItemInHand().getAmount() < amount) {
								player.sendMessage(ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount));
								return true;
							}
							ItemStack toList = player.getItemInHand().clone();
							if (player.getItemInHand().getAmount() == amount) {
								player.setItemInHand(new ItemStack(Material.AIR));
							} else {
								player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
							}
							toList.setAmount(amount);
							storageHandler.storeListing(toList, player.getName(), price);
							player.sendMessage(ChatColor.GREEN + locale.get("item_listed"));
							// TODO: make this pretty
							String itemName = toList.getType().toString();
							if (!useBukkitNames()) {
								net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(toList.getTypeId());
								if (itemInfo != null) {
									itemName = itemInfo.getName();
								}
							}
							storageHandler.storeHistory(player.getName(), locale.get("history.item_listed", itemName + "x" + toList.getAmount(), price));
						} else {
							ItemStack toList = player.getItemInHand().clone();
							player.setItemInHand(new ItemStack(Material.AIR));
							storageHandler.storeListing(toList, player.getName(), price);
							player.sendMessage(ChatColor.GREEN + locale.get("item_listed"));
							// TODO: make this pretty
							String itemName = toList.getType().toString();
							if (!useBukkitNames()) {
								net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(toList.getTypeId());
								if (itemInfo != null) {
									itemName = itemInfo.getName();
								}
							}
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
			if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("globalmarket.admin")) {
				reloadConfig();
				config.reloadLocaleYML();
				sender.sendMessage(prefix + market.getLocale().get("config_reloaded"));
				return true;
			}
			if (sender.hasPermission("globalmarket.util")) {
				if (args[0].equalsIgnoreCase("mailbox")) {
					Player player = (Player) sender;
					Location loc = null;
					Block block = player.getTargetBlock(null, 4);
					if (block.getType() == Material.CHEST
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
						} else {
							player.sendMessage(ChatColor.RED + locale.get("no_mailbox_found"));
							return true;
						}
					}
					if (getConfig().isSet("mailbox." + x + "," + y + "," + z)) {
						sender.sendMessage(ChatColor.RED + locale.get("mailbox_already_exists"));
						return true;
					}
					getConfig().set("mailbox." + x + "," + y + "," + z, true);
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + locale.get("mailbox_added"));
					return true;
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
	}
}