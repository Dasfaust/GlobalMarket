package com.survivorserver.GlobalMarket.Lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.survivorserver.GlobalMarket.Market;

public class ItemIndex {

	private Market market;
	private Map<String, String> lang;
	private Map<MaterialData, String> materialLangMap;
	private Map<Integer, String> monsterLangMap;
	private Map<Integer, String[]> potionLangMap;
	
	public ItemIndex(Market market) {
		this.market = market;
		lang = new HashMap<String, String>();
		materialLangMap = new HashMap<MaterialData, String>() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public String put(MaterialData data, String path) {
				if (data.material == null) {
					return path;
				}
				return super.put(data, path);
			}
			
		};
		monsterLangMap = new HashMap<Integer, String>();
		potionLangMap = new HashMap<Integer, String[]>();
		market.getConfig().addDefault("language_file", "en_US.lang");
		market.getConfig().options().copyDefaults(true);
		market.saveConfig();
		loadLang();
		mapMaterials();
	}
	
	private void loadLang() {
		File langFile = new File(market.getDataFolder().getAbsolutePath() + File.separator + market.getConfig().getString("language_file"));
		if (!langFile.exists()) {
			market.log.warning("No language file could be found, defaulting to internal Bukkit item names");
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(langFile));
			String line;
			while((line = reader.readLine()) != null) {
				// We only need item/block/potion/entity names
				if (line.contains("=") && (line.contains("item.") || line.contains("tile.") || line.contains("potion.") || line.contains("entity."))) {
					String[] entry = line.split("=");
					if (entry.length == 2) {
						lang.put(entry[0], entry[1]);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			market.log.warning(String.format("An error occurred while loading language file %s:", market.getConfig().getString("language_file")));
			e.printStackTrace();
		}
	}
	
	public String getItemName(ItemStack item) {
		Material mat = item.getType();
		int damage = item.getDurability();
		if (mat == Material.POTION) {
			if (potionLangMap.containsKey(damage)) {
				String[] potionLang = potionLangMap.get(damage);
				return  (potionLang.length == 2 ? getLocalized(potionLang[1]) + " " : "") + getLocalized(potionLang[0]);
			}
		}
		if (mat == Material.MONSTER_EGG) {
			if (monsterLangMap.containsKey(damage)) {
				return getLocalized(materialLangMap.get(new MaterialData(mat))) + " " + getLocalized(monsterLangMap.get(damage));
			}
		}
		if (item.getType().getMaxDurability() > 64) {
			// Tools
			damage = 0;
		}
		MaterialData data = new MaterialData(mat, damage);
		if (!materialLangMap.containsKey(data)) {
			return mat.name();
		}
		String name = getLocalized(materialLangMap.get(data));
		if (mat == Material.SKULL_ITEM && damage == 3) {
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			if (meta.hasOwner()) {
				name = String.format(name, meta.getOwner());
			} else {
				name = getLocalized("item.skull.char.name");
			}
		}
		return name;
	}
	
	public String getLocalized(String path) {
		if (lang.containsKey(path)) {
			return lang.get(path);
		}
		return path;
	}
	
	private void mapMaterials() {
		monsterLangMap.put(50, "entity.Creeper.name");
		monsterLangMap.put(51, "entity.Skeleton.name");
		monsterLangMap.put(52, "entity.Spider.name");
		monsterLangMap.put(54, "entity.Zombie.name");
		monsterLangMap.put(55, "entity.Slime.name");
		monsterLangMap.put(56, "entity.Ghast.name");
		monsterLangMap.put(57, "entity.PigZombie.name");
		monsterLangMap.put(58, "entity.Enderman.name");
		monsterLangMap.put(59, "entity.CaveSpider.name");
		monsterLangMap.put(60, "entity.Silverfish.name");
		monsterLangMap.put(61, "entity.Blaze.name");
		monsterLangMap.put(62, "entity.LavaSlime.name");
		monsterLangMap.put(65, "entity.Bat.name");
		monsterLangMap.put(66, "entity.Witch.name");
		monsterLangMap.put(90, "entity.Pig.name");
		monsterLangMap.put(91, "entity.Sheep.name");
		monsterLangMap.put(92, "entity.Cow.name");
		monsterLangMap.put(93, "entity.Chicken.name");
		monsterLangMap.put(94, "entity.Squid.name");
		monsterLangMap.put(95, "entity.Wolf.name");
		monsterLangMap.put(96, "entity.MushroomCow.name");
		monsterLangMap.put(98, "entity.Ozelot.name");
		monsterLangMap.put(100, "entity.horse.name");
		monsterLangMap.put(120, "entity.Villager.name");
		
		potionLangMap.put(16, new String[] {"potion.prefix.awkward"});
		potionLangMap.put(32, new String[] {"potion.prefix.thick"});
		potionLangMap.put(64, new String[] {"potion.prefix.mundane"});
		potionLangMap.put(8193, new String[] {"potion.regeneration.postfix"});
		potionLangMap.put(8194, new String[] {"potion.moveSpeed.postfix"});
		potionLangMap.put(8196, new String[] {"potion.poison.postfix"});
		potionLangMap.put(8201, new String[] {"potion.damageBoost.postfix"});
		potionLangMap.put(8226, new String[] {"potion.moveSpeed.postfix"});
		potionLangMap.put(8227, new String[] {"potion.fireResistance.postfix"});
		potionLangMap.put(8228, new String[] {"potion.poison.postfix"});
		potionLangMap.put(8229, new String[] {"potion.heal.postfix"});
		potionLangMap.put(8230, new String[] {"potion.nightVision.postfix"});
		potionLangMap.put(8232, new String[] {"potion.weakness.postfix"});
		potionLangMap.put(8233, new String[] {"potion.damageBoost.postfix"});
		potionLangMap.put(8234, new String[] {"potion.moveSlowdown.postfix"});
		potionLangMap.put(8236, new String[] {"potion.harm.postfix"});
		potionLangMap.put(8237, new String[] {"potion.waterBreathing.postfix"});
		potionLangMap.put(8238, new String[] {"potion.invisibility.postfix"});
		potionLangMap.put(8255, new String[] {"potion.regeneration.postfix"});
		potionLangMap.put(8257, new String[] {"potion.regeneration.postfix"});
		potionLangMap.put(8258, new String[] {"potion.moveSpeed.postfix"});
		potionLangMap.put(8259, new String[] {"potion.fireResistance.postfix"});
		potionLangMap.put(8260, new String[] {"potion.poison.postfix"});
		potionLangMap.put(8261, new String[] {"potion.heal.postfix"});
		potionLangMap.put(8262, new String[] {"potion.nightVision.postfix"});
		potionLangMap.put(8264, new String[] {"potion.weakness.postfix"});
		potionLangMap.put(8265, new String[] {"potion.damageBoost.postfix"});
		potionLangMap.put(8266, new String[] {"potion.moveSlowdown.postfix"});
		potionLangMap.put(8268, new String[] {"potion.harm.postfix"});
		potionLangMap.put(8269, new String[] {"potion.waterBreathing.postfix"});
		potionLangMap.put(8270, new String[] {"potion.invisibility.postfix"});
		potionLangMap.put(16385, new String[] {"potion.regeneration.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16386, new String[] {"potion.moveSpeed.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16388, new String[] {"potion.poison.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16393, new String[] {"potion.damageBoost.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16417, new String[] {"potion.regeneration.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16418, new String[] {"potion.moveSpeed.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16419, new String[] {"potion.fireResistance.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16420, new String[] {"potion.poison.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16421, new String[] {"potion.heal.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16422, new String[] {"potion.nightVision.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16424, new String[] {"potion.weakness.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16425, new String[] {"potion.damageBoost.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16426, new String[] {"potion.invisibility.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16428, new String[] {"potion.harm.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16429, new String[] {"potion.waterBreathing.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16430, new String[] {"potion.invisibility.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16449, new String[] {"potion.regeneration.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16450, new String[] {"potion.moveSpeed.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16451, new String[] {"potion.fireResistance.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16452, new String[] {"potion.poison.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16453, new String[] {"potion.heal.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16454, new String[] {"potion.nightVision.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16456, new String[] {"potion.weakness.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16457, new String[] {"potion.damageBoost.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16458, new String[] {"potion.moveSlowdown.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16460, new String[] {"potion.harm.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16461, new String[] {"potion.waterBreathing.postfix", "potion.prefix.grenade"});
		potionLangMap.put(16462, new String[] {"potion.invisibility.postfix", "potion.prefix.grenade"});
		
		materialLangMap.put(new MaterialData(m("APPLE")), "item.apple.name");
		materialLangMap.put(new MaterialData(m("GOLDEN_APPLE")), "item.appleGold.name");
		materialLangMap.put(new MaterialData(m("ARROW")), "item.arrow.name");
		materialLangMap.put(new MaterialData(m("BED")), "item.bed.name");
		materialLangMap.put(new MaterialData(m("COOKED_BEEF")), "item.beefCooked.name");
		materialLangMap.put(new MaterialData(m("RAW_BEEF")), "item.beefRaw.name");
		materialLangMap.put(new MaterialData(m("BLAZE_POWDER")), "item.blazePowder.name");
		materialLangMap.put(new MaterialData(m("BLAZE_ROD")), "item.blazeRod.name");
		materialLangMap.put(new MaterialData(m("BOAT")), "item.boat.name");
		materialLangMap.put(new MaterialData(m("BONE")), "item.bone.name");
		materialLangMap.put(new MaterialData(m("BOOK")), "item.book.name");
		materialLangMap.put(new MaterialData(m("CHAINMAIL_BOOTS")), "item.bootsChain.name");
		materialLangMap.put(new MaterialData(m("LEATHER_BOOTS")), "item.bootsCloth.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_BOOTS")), "item.bootsDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_BOOTS")), "item.bootsGold.name");
		materialLangMap.put(new MaterialData(m("IRON_BOOTS")), "item.bootsIron.name");
		materialLangMap.put(new MaterialData(m("BOW")), "item.bow.name");
		materialLangMap.put(new MaterialData(m("BOWL")), "item.bowl.name");
		materialLangMap.put(new MaterialData(m("BREAD")), "item.bread.name");
		materialLangMap.put(new MaterialData(m("BREWING_STAND_ITEM")), "item.brewingStand.name");
		materialLangMap.put(new MaterialData(m("CLAY_BRICK")), "item.brick.name");
		materialLangMap.put(new MaterialData(m("BUCKET")), "item.bucket.name");
		materialLangMap.put(new MaterialData(m("LAVA_BUCKET")), "item.bucketLava.name");
		materialLangMap.put(new MaterialData(m("WATER_BUCKET")), "item.bucketWater.name");
		materialLangMap.put(new MaterialData(m("CAKE")), "item.cake.name");
		materialLangMap.put(new MaterialData(m("GOLDEN_CARROT")), "item.carrotGolden.name");
		materialLangMap.put(new MaterialData(m("CARROT_STICK")), "item.carrotOnAStick.name");
		materialLangMap.put(new MaterialData(m("CARROT_ITEM")), "item.carrots.name");
		materialLangMap.put(new MaterialData(m("CAULDRON_ITEM")), "item.cauldron.name");
		materialLangMap.put(new MaterialData(m("COAL, 1")), "item.charcoal.name");
		materialLangMap.put(new MaterialData(m("CHAINMAIL_CHESTPLATE")), "item.chestplateChain.name");
		materialLangMap.put(new MaterialData(m("LEATHER_CHESTPLATE")), "item.chestplateCloth.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_CHESTPLATE")), "item.chestplateDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_CHESTPLATE")), "item.chestplateGold.name");
		materialLangMap.put(new MaterialData(m("IRON_CHESTPLATE")), "item.chestplateIron.name");
		materialLangMap.put(new MaterialData(m("COOKED_CHICKEN")), "item.chickenCooked.name");
		materialLangMap.put(new MaterialData(m("RAW_CHICKEN")), "item.chickenRaw.name");
		materialLangMap.put(new MaterialData(m("CLAY_BALL")), "item.clay.name");
		materialLangMap.put(new MaterialData(m("WATCH")), "item.clock.name");
		materialLangMap.put(new MaterialData(m("COAL")), "item.coal.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_COMPARATOR")), "item.comparator.name");
		materialLangMap.put(new MaterialData(m("COMPASS")), "item.compass.name");
		materialLangMap.put(new MaterialData(m("COOKIE")), "item.cookie.name");
		materialLangMap.put(new MaterialData(m("DIAMOND")), "item.diamond.name");
		materialLangMap.put(new MaterialData(m("DIODE")), "item.diode.name");
		materialLangMap.put(new MaterialData(m("IRON_DOOR")), "item.doorIron.name");
		materialLangMap.put(new MaterialData(m("WOOD_DOOR")), "item.doorWood.name");
		materialLangMap.put(new MaterialData(m("INK_SACK")), "item.dyePowder.black.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 4), "item.dyePowder.blue.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 3), "item.dyePowder.brown.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 6), "item.dyePowder.cyan.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 8), "item.dyePowder.gray.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 2), "item.dyePowder.green.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 12), "item.dyePowder.lightBlue.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 10), "item.dyePowder.lime.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 13), "item.dyePowder.magenta.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 14), "item.dyePowder.orange.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 9), "item.dyePowder.pink.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 5), "item.dyePowder.purple.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 1), "item.dyePowder.red.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 7), "item.dyePowder.silver.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 15), "item.dyePowder.white.name");
		materialLangMap.put(new MaterialData(m("INK_SACK"), 11), "item.dyePowder.yellow.name");
		materialLangMap.put(new MaterialData(m("EGG")), "item.egg.name");
		materialLangMap.put(new MaterialData(m("EMERALD")), "item.emerald.name");
		materialLangMap.put(new MaterialData(m("EMPTY_MAP")), "item.emptyMap.name");
		materialLangMap.put(new MaterialData(m("POTION")), "item.emptyPotion.name");
		materialLangMap.put(new MaterialData(m("ENCHANTED_BOOK")), "item.enchantedBook.name");
		materialLangMap.put(new MaterialData(m("ENDER_PEARL")), "item.enderPearl.name");
		materialLangMap.put(new MaterialData(m("EXP_BOTTLE")), "item.expBottle.name");
		materialLangMap.put(new MaterialData(m("EYE_OF_ENDER")), "item.eyeOfEnder.name");
		materialLangMap.put(new MaterialData(m("FEATHER")), "item.feather.name");
		materialLangMap.put(new MaterialData(m("FERMENTED_SPIDER_EYE")), "item.fermentedSpiderEye.name");
		materialLangMap.put(new MaterialData(m("FIREBALL")), "item.fireball.name");
		materialLangMap.put(new MaterialData(m("FIREWORK")), "item.fireworks.name");
		materialLangMap.put(new MaterialData(m("FIREWORK_CHARGE")), "item.fireworksCharge.name");
		// TODO firework charge types?
		materialLangMap.put(new MaterialData(m("RAW_FISH"), 2), "item.fish.clownfish.raw.name");
		materialLangMap.put(new MaterialData(m("COOKED_FISH")), "item.fish.cod.cooked.name");
		materialLangMap.put(new MaterialData(m("RAW_FISH")), "item.fish.cod.raw.name");
		materialLangMap.put(new MaterialData(m("RAW_FISH"), 3), "item.fish.pufferfish.raw.name");
		materialLangMap.put(new MaterialData(m("COOKED_FISH"), 1), "item.fish.salmon.cooked.name");
		materialLangMap.put(new MaterialData(m("RAW_FISH"), 1), "item.fish.salmon.raw.name");
		materialLangMap.put(new MaterialData(m("FISHING_ROD")), "item.fishingRod.name");
		materialLangMap.put(new MaterialData(m("FLINT")), "item.flint.name");
		materialLangMap.put(new MaterialData(m("FLINT_AND_STEEL")), "item.flintAndSteel.name");
		materialLangMap.put(new MaterialData(m("FLOWER_POT_ITEM")), "item.flowerPot.name");
		materialLangMap.put(new MaterialData(m("ITEM_FRAME")), "item.frame.name");
		materialLangMap.put(new MaterialData(m("GHAST_TEAR")), "item.ghastTear.name");
		materialLangMap.put(new MaterialData(m("GLASS_BOTTLE")), "item.glassBottle.name");
		materialLangMap.put(new MaterialData(m("GOLD_NUGGET")), "item.goldNugget.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_AXE")), "item.hatchetDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_AXE")), "item.hatchetGold.name");
		materialLangMap.put(new MaterialData(m("IRON_AXE")), "item.hatchetIron.name");
		materialLangMap.put(new MaterialData(m("STONE_AXE")), "item.hatchetStone.name");
		materialLangMap.put(new MaterialData(m("WOOD_AXE")), "item.hatchetWood.name");
		materialLangMap.put(new MaterialData(m("CHAINMAIL_HELMET")), "item.helmetChain.name");
		materialLangMap.put(new MaterialData(m("LEATHER_HELMET")), "item.helmetCloth.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_HELMET")), "item.helmetDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_HELMET")), "item.helmetGold.name");
		materialLangMap.put(new MaterialData(m("IRON_HELMET")), "item.helmetIron.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_HOE")), "item.hoeDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_HOE")), "item.hoeGold.name");
		materialLangMap.put(new MaterialData(m("IRON_HOE")), "item.hoeIron.name");
		materialLangMap.put(new MaterialData(m("STONE_HOE")), "item.hoeStone.name");
		materialLangMap.put(new MaterialData(m("WOOD_HOE")), "item.hoeWood.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_BARDING")), "item.horsearmordiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_BARDING")), "item.horsearmorgold.name");
		materialLangMap.put(new MaterialData(m("IRON_BARDING")), "item.horsearmormetal.name");
		materialLangMap.put(new MaterialData(m("GOLD_INGOT")), "item.ingotGold.name");
		materialLangMap.put(new MaterialData(m("IRON_INGOT")), "item.ingotIron.name");
		materialLangMap.put(new MaterialData(m("LEASH")), "item.leash.name");
		materialLangMap.put(new MaterialData(m("LEATHER")), "item.leather.name");
		materialLangMap.put(new MaterialData(m("CHAINMAIL_LEGGINGS")), "item.leggingsChain.name");
		materialLangMap.put(new MaterialData(m("LEATHER_LEGGINGS")), "item.leggingsCloth.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_LEGGINGS")), "item.leggingsDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_LEGGINGS")), "item.leggingsGold.name");
		materialLangMap.put(new MaterialData(m("IRON_LEGGINGS")), "item.leggingsIron.name");
		materialLangMap.put(new MaterialData(m("MAGMA_CREAM")), "item.magmaCream.name");
		materialLangMap.put(new MaterialData(m("MAP")), "item.map.name");
		materialLangMap.put(new MaterialData(m("MELON")), "item.melon.name");
		materialLangMap.put(new MaterialData(m("MILK_BUCKET")), "item.milk.name");
		materialLangMap.put(new MaterialData(m("MINECART")), "item.minecart.name");
		materialLangMap.put(new MaterialData(m("STORAGE_MINECART")), "item.minecartChest.name");
		materialLangMap.put(new MaterialData(m("COMMAND_MINECART")), "item.minecartCommandBlock.name");
		materialLangMap.put(new MaterialData(m("POWERED_MINECART")), "item.minecartFurnace.name");
		materialLangMap.put(new MaterialData(m("HOPPER_MINECART")), "item.minecartHopper.name");
		materialLangMap.put(new MaterialData(m("EXPLOSIVE_MINECART")), "item.minecartTnt.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGG")), "item.monsterPlacer.name");
		materialLangMap.put(new MaterialData(m("MUSHROOM_SOUP")), "item.mushroomStew.name");
		materialLangMap.put(new MaterialData(m("NAME_TAG")), "item.nameTag.name");
		materialLangMap.put(new MaterialData(m("NETHER_WARTS")), "item.netherStalkSeeds.name");
		materialLangMap.put(new MaterialData(m("NETHER_STAR")), "item.netherStar.name");
		materialLangMap.put(new MaterialData(m("NETHER_BRICK_ITEM")), "item.netherbrick.name");
		materialLangMap.put(new MaterialData(m("QUARTZ")), "item.netherquartz.name");
		materialLangMap.put(new MaterialData(m("PAINTING")), "item.painting.name");
		materialLangMap.put(new MaterialData(m("PAPER")), "item.paper.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_PICKAXE")), "item.pickaxeDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_PICKAXE")), "item.pickaxeGold.name");
		materialLangMap.put(new MaterialData(m("IRON_PICKAXE")), "item.pickaxeIron.name");
		materialLangMap.put(new MaterialData(m("STONE_PICKAXE")), "item.pickaxeStone.name");
		materialLangMap.put(new MaterialData(m("WOOD_PICKAXE")), "item.pickaxeWood.name");
		materialLangMap.put(new MaterialData(m("GRILLED_PORK")), "item.porkchopCooked.name");
		materialLangMap.put(new MaterialData(m("PORK")), "item.porkchopRaw.name");
		materialLangMap.put(new MaterialData(m("POTATO_ITEM")), "item.potato.name");
		materialLangMap.put(new MaterialData(m("BAKED_POTATO")), "item.potatoBaked.name");
		materialLangMap.put(new MaterialData(m("POISONOUS_POTATO")), "item.potatoPoisonous.name");
		materialLangMap.put(new MaterialData(m("PUMPKIN_PIE")), "item.pumpkinPie.name");
		// TODO proper record names?
		materialLangMap.put(new MaterialData(m("GOLD_RECORD")), "item.record.name");
		materialLangMap.put(new MaterialData(m("GREEN_RECORD")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_3")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_4")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_5")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_6")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_7")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_8")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_9")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_10")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_11")), "item.record.name");
		materialLangMap.put(new MaterialData(m("RECORD_12")), "item.record.name");
		//
		materialLangMap.put(new MaterialData(m("REDSTONE")), "item.redstone.name");
		materialLangMap.put(new MaterialData(m("SUGAR_CANE")), "item.reeds.name");
		materialLangMap.put(new MaterialData(m("ROTTEN_FLESH")), "item.rottenFlesh.name");
		materialLangMap.put(new MaterialData(m("SADDLE")), "item.saddle.name");
		materialLangMap.put(new MaterialData(m("SEEDS")), "item.seeds.name");
		materialLangMap.put(new MaterialData(m("MELON_SEEDS")), "item.seeds_melon.name");
		materialLangMap.put(new MaterialData(m("PUMPKIN_SEEDS")), "item.seeds_pumpkin.name");
		materialLangMap.put(new MaterialData(m("SHEARS")), "item.shears.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_SPADE")), "item.shovelDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_SPADE")), "item.shovelGold.name");
		materialLangMap.put(new MaterialData(m("IRON_SPADE")), "item.shovelIron.name");
		materialLangMap.put(new MaterialData(m("STONE_SPADE")), "item.shovelStone.name");
		materialLangMap.put(new MaterialData(m("WOOD_SPADE")), "item.shovelWood.name");
		materialLangMap.put(new MaterialData(m("SIGN")), "item.sign.name");
		materialLangMap.put(new MaterialData(m("SKULL_ITEM"), 4), "item.skull.creeper.name");
		materialLangMap.put(new MaterialData(m("SKULL_ITEM"), 3), "item.skull.player.name");
		materialLangMap.put(new MaterialData(m("SKULL_ITEM")), "item.skull.skeleton.name");
		materialLangMap.put(new MaterialData(m("SKULL_ITEM"), 1), "item.skull.wither.name");
		materialLangMap.put(new MaterialData(m("SKULL_ITEM"), 2), "item.skull.zombie.name");
		materialLangMap.put(new MaterialData(m("SLIME_BALL")), "item.slimeball.name");
		materialLangMap.put(new MaterialData(m("SNOW_BALL")), "item.snowball.name");
		materialLangMap.put(new MaterialData(m("SPECKLED_MELON")), "item.speckledMelon.name");
		materialLangMap.put(new MaterialData(m("SPIDER_EYE")), "item.spiderEye.name");
		materialLangMap.put(new MaterialData(m("STICK")), "item.stick.name");
		materialLangMap.put(new MaterialData(m("STRING")), "item.string.name");
		materialLangMap.put(new MaterialData(m("SUGAR")), "item.sugar.name");
		materialLangMap.put(new MaterialData(m("SULPHUR")), "item.sulphur.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_SWORD")), "item.swordDiamond.name");
		materialLangMap.put(new MaterialData(m("GOLD_SWORD")), "item.swordGold.name");
		materialLangMap.put(new MaterialData(m("IRON_SWORD")), "item.swordIron.name");
		materialLangMap.put(new MaterialData(m("STONE_SWORD")), "item.swordStone.name");
		materialLangMap.put(new MaterialData(m("WOOD_SWORD")), "item.swordWood.name");
		materialLangMap.put(new MaterialData(m("WHEAT")), "item.wheat.name");
		materialLangMap.put(new MaterialData(m("BOOK_AND_QUILL")), "item.writingBook.name");
		materialLangMap.put(new MaterialData(m("WRITTEN_BOOK")), "item.writtenBook.name");
		materialLangMap.put(new MaterialData(m("GLOWSTONE_DUST")), "item.yellowDust.name");
		
		materialLangMap.put(new MaterialData(m("ACTIVATOR_RAIL")), "tile.activatorRail.name");
		materialLangMap.put(new MaterialData(m("ANVIL")), "tile.anvil.name");
		materialLangMap.put(new MaterialData(m("BEACON")), "tile.beacon.name");
		materialLangMap.put(new MaterialData(m("BED_BLOCK")), "tile.bed.name");
		materialLangMap.put(new MaterialData(m("BEDROCK")), "tile.bedrock.name");
		materialLangMap.put(new MaterialData(m("COAL_BLOCK")), "tile.blockCoal.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_BLOCK")), "tile.blockDiamond.name");
		materialLangMap.put(new MaterialData(m("EMERALD_BLOCK")), "tile.blockEmerald.name");
		materialLangMap.put(new MaterialData(m("GOLD_BLOCK")), "tile.blockGold.name");
		materialLangMap.put(new MaterialData(m("IRON_BLOCK")), "tile.blockIron.name");
		materialLangMap.put(new MaterialData(m("LAPIS_BLOCK")), "tile.blockLapis.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_BLOCK")), "tile.blockRedstone.name");
		materialLangMap.put(new MaterialData(m("BOOKSHELF")), "tile.bookshelf.name");
		materialLangMap.put(new MaterialData(m("BRICK")), "tile.brick.name");
		materialLangMap.put(new MaterialData(m("STONE_BUTTON")), "tile.button.name");
		materialLangMap.put(new MaterialData(m("CACTUS")), "tile.cactus.name");
		materialLangMap.put(new MaterialData(m("CAKE_BLOCK")), "tile.cake.name");
		materialLangMap.put(new MaterialData(m("CARROT")), "tile.carrots.name");
		materialLangMap.put(new MaterialData(m("CAULDRON")), "tile.cauldron.name");
		materialLangMap.put(new MaterialData(m("CHEST")), "tile.chest.name");
		materialLangMap.put(new MaterialData(m("TRAPPED_CHEST")), "tile.chestTrap.name");
		materialLangMap.put(new MaterialData(m("CLAY")), "tile.clay.name");
		materialLangMap.put(new MaterialData(m("HARD_CLAY")), "tile.clayHardened.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 15), "tile.clayHardenedStained.black.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 11), "tile.clayHardenedStained.blue.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 12), "tile.clayHardenedStained.brown.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 9), "tile.clayHardenedStained.cyan.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 7), "tile.clayHardenedStained.gray.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 13), "tile.clayHardenedStained.green.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 3), "tile.clayHardenedStained.lightBlue.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 5), "tile.clayHardenedStained.lime.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 2), "tile.clayHardenedStained.magenta.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 1), "tile.clayHardenedStained.orange.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 6), "tile.clayHardenedStained.pink.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 10), "tile.clayHardenedStained.purple.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 14), "tile.clayHardenedStained.red.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 8), "tile.clayHardenedStained.silver.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY")), "tile.clayHardenedStained.white.name");
		materialLangMap.put(new MaterialData(m("STAINED_CLAY"), 4), "tile.clayHardenedStained.yellow.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 15), "tile.cloth.black.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 11), "tile.cloth.blue.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 12), "tile.cloth.brown.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 9), "tile.cloth.cyan.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 7), "tile.cloth.gray.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 13), "tile.cloth.green.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 3), "tile.cloth.lightBlue.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 5), "tile.cloth.lime.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 2), "tile.cloth.magenta.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 1), "tile.cloth.orange.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 6), "tile.cloth.pink.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 10), "tile.cloth.purple.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 14), "tile.cloth.red.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 8), "tile.cloth.silver.name");
		materialLangMap.put(new MaterialData(m("WOOL")), "tile.cloth.white.name");
		materialLangMap.put(new MaterialData(m("WOOL"), 4), "tile.cloth.yellow.name");
		materialLangMap.put(new MaterialData(m("COBBLE_WALL"), 1), "tile.cobbleWall.mossy.name");
		materialLangMap.put(new MaterialData(m("COBBLE_WALL")), "tile.cobbleWall.normal.name");
		materialLangMap.put(new MaterialData(m("COCOA")), "tile.cocoa.name");
		materialLangMap.put(new MaterialData(m("COMMAND")), "tile.commandBlock.name");
		materialLangMap.put(new MaterialData(m("CROPS")), "tile.crops.name");
		materialLangMap.put(new MaterialData(m("DAYLIGHT_DETECTOR")), "tile.daylightDetector.name");
		materialLangMap.put(new MaterialData(m("DEAD_BUSH")), "tile.deadbush.name");
		materialLangMap.put(new MaterialData(m("DETECTOR_RAIL")), "tile.detectorRail.name");
		materialLangMap.put(new MaterialData(m("DIRT")), "tile.dirt.default.name");
		materialLangMap.put(new MaterialData(m("DIRT"), 2), "tile.dirt.podzol.name");
		materialLangMap.put(new MaterialData(m("DISPENSER")), "tile.dispenser.name");
		materialLangMap.put(new MaterialData(m("IRON_DOOR_BLOCK")), "tile.doorIron.name");
		materialLangMap.put(new MaterialData(m("WOODEN_DOOR")), "tile.doorWood.name");
		materialLangMap.put(new MaterialData(m("DOUBLE_PLANT"), 3), "tile.doublePlant.fern.name");
		materialLangMap.put(new MaterialData(m("DOUBLE_PLANT"), 2), "tile.doublePlant.grass.name");
		materialLangMap.put(new MaterialData(m("DOUBLE_PLANT"), 5), "tile.doublePlant.paeonia.name");
		materialLangMap.put(new MaterialData(m("DOUBLE_PLANT"), 4), "tile.doublePlant.rose.name");
		materialLangMap.put(new MaterialData(m("DOUBLE_PLANT")), "tile.doublePlant.sunflower.name");
		materialLangMap.put(new MaterialData(m("DOUBLE_PLANT"), 1), "tile.doublePlant.syringa.name");
		materialLangMap.put(new MaterialData(m("DRAGON_EGG")), "tile.dragonEgg.name");
		materialLangMap.put(new MaterialData(m("DROPPER")), "tile.dropper.name");
		materialLangMap.put(new MaterialData(m("ENCHANTMENT_TABLE")), "tile.enchantmentTable.name");
		materialLangMap.put(new MaterialData(m("ENDER_PORTAL_FRAME")), "tile.endPortalFrame.name");
		materialLangMap.put(new MaterialData(m("ENDER_CHEST")), "tile.enderChest.name");
		materialLangMap.put(new MaterialData(m("SOIL")), "tile.farmland.name");
		materialLangMap.put(new MaterialData(m("FENCE")), "tile.fence.name");
		materialLangMap.put(new MaterialData(m("FENCE_GATE")), "tile.fenceGate.name");
		materialLangMap.put(new MaterialData(m("IRON_FENCE")), "tile.fenceIron.name");
		materialLangMap.put(new MaterialData(m("FIRE")), "tile.fire.name");
		materialLangMap.put(new MaterialData(m("YELLOW_FLOWER")), "tile.flower1.dandelion.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 2), "tile.flower2.allium.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 1), "tile.flower2.blueOrchid.name");
		//materialLangMap.put(new MaterialData(m("RED_ROSE")), "tile.flower2.houstonia.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 8), "tile.flower2.oxeyeDaisy.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE")), "tile.flower2.poppy.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 5), "tile.flower2.tulipOrange.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 7), "tile.flower2.tulipPink.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 4), "tile.flower2.tulipRed.name");
		materialLangMap.put(new MaterialData(m("RED_ROSE"), 6), "tile.flower2.tulipWhite.name");
		materialLangMap.put(new MaterialData(m("FURNACE")), "tile.furnace.name");
		materialLangMap.put(new MaterialData(m("GLASS")), "tile.glass.name");
		materialLangMap.put(new MaterialData(m("POWERED_RAIL")), "tile.goldenRail.name");
		materialLangMap.put(new MaterialData(m("GRASS")), "tile.grass.name");
		materialLangMap.put(new MaterialData(m("GRAVEL")), "tile.gravel.name");
		materialLangMap.put(new MaterialData(m("HAY_BLOCK")), "tile.hayBlock.name");
		materialLangMap.put(new MaterialData(m("NETHERRACK")), "tile.hellrock.name");
		materialLangMap.put(new MaterialData(m("SOUL_SAND")), "tile.hellsand.name");
		materialLangMap.put(new MaterialData(m("HOPPER")), "tile.hopper.name");
		materialLangMap.put(new MaterialData(m("ICE")), "tile.ice.name");
		materialLangMap.put(new MaterialData(m("PACKED_ICE")), "tile.icePacked.name");
		materialLangMap.put(new MaterialData(m("JUKEBOX")), "tile.jukebox.name");
		materialLangMap.put(new MaterialData(m("LADDER")), "tile.ladder.name");
		materialLangMap.put(new MaterialData(m("LAVA")), "tile.lava.name");
		materialLangMap.put(new MaterialData(m("LEAVES_2")), "tile.leaves.acacia.name");
		materialLangMap.put(new MaterialData(m("LEAVES_2"), 1), "tile.leaves.big_oak.name");
		materialLangMap.put(new MaterialData(m("LEAVES"), 2), "tile.leaves.birch.name");
		materialLangMap.put(new MaterialData(m("LEAVES"), 3), "tile.leaves.jungle.name");
		materialLangMap.put(new MaterialData(m("LEAVES")), "tile.leaves.oak.name");
		materialLangMap.put(new MaterialData(m("LEAVES"), 1), "tile.leaves.spruce.name");
		materialLangMap.put(new MaterialData(m("LEVER")), "tile.lever.name");
		materialLangMap.put(new MaterialData(m("GLOWSTONE")), "tile.lightgem.name");
		materialLangMap.put(new MaterialData(m("JACK_O_LANTERN")), "tile.litpumpkin.name");
		materialLangMap.put(new MaterialData(m("LOG_2")), "tile.log.acacia.name");
		materialLangMap.put(new MaterialData(m("LOG_2"), 1), "tile.log.big_oak.name");
		materialLangMap.put(new MaterialData(m("LOG"), 2), "tile.log.birch.name");
		materialLangMap.put(new MaterialData(m("LOG"), 3), "tile.log.jungle.name");
		materialLangMap.put(new MaterialData(m("LOG")), "tile.log.name");
		materialLangMap.put(new MaterialData(m("LOG")), "tile.log.oak.name");
		materialLangMap.put(new MaterialData(m("LOG"), 1), "tile.log.spruce.name");
		materialLangMap.put(new MaterialData(m("MELON_BLOCK")), "tile.melon.name");
		materialLangMap.put(new MaterialData(m("MOB_SPAWNER")), "tile.mobSpawner.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGGS"), 2), "tile.monsterStoneEgg.brick.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGGS"), 5), "tile.monsterStoneEgg.chiseledbrick.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGGS"), 1), "tile.monsterStoneEgg.cobble.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGGS"), 4), "tile.monsterStoneEgg.crackedbrick.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGGS"), 3), "tile.monsterStoneEgg.mossybrick.name");
		materialLangMap.put(new MaterialData(m("MONSTER_EGGS")), "tile.monsterStoneEgg.stone.name");
		materialLangMap.put(new MaterialData(m("NOTE_BLOCK")), "tile.musicBlock.name");
		materialLangMap.put(new MaterialData(m("MYCEL")), "tile.mycel.name");
		materialLangMap.put(new MaterialData(m("NETHER_BRICK")), "tile.netherBrick.name");
		materialLangMap.put(new MaterialData(m("NETHER_FENCE")), "tile.netherFence.name");
		materialLangMap.put(new MaterialData(m("NETHER_STALK")), "tile.netherStalk.name");
		materialLangMap.put(new MaterialData(m("QUARTZ_ORE")), "tile.netherquartz.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_TORCH_ON")), "tile.notGate.name");
		materialLangMap.put(new MaterialData(m("OBSIDIAN")), "tile.obsidian.name");
		materialLangMap.put(new MaterialData(m("COAL_ORE")), "tile.oreCoal.name");
		materialLangMap.put(new MaterialData(m("DIAMOND_ORE")), "tile.oreDiamond.name");
		materialLangMap.put(new MaterialData(m("EMERALD_ORE")), "tile.oreEmerald.name");
		materialLangMap.put(new MaterialData(m("GOLD_ORE")), "tile.oreGold.name");
		materialLangMap.put(new MaterialData(m("IRON_ORE")), "tile.oreIron.name");
		materialLangMap.put(new MaterialData(m("LAPIS_ORE")), "tile.oreLapis.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_ORE")), "tile.oreRedstone.name");
		materialLangMap.put(new MaterialData(m("PISTON_BASE")), "tile.pistonBase.name");
		materialLangMap.put(new MaterialData(m("PISTON_STICKY_BASE")), "tile.pistonStickyBase.name");
		materialLangMap.put(new MaterialData(m("PORTAL")), "tile.portal.name");
		materialLangMap.put(new MaterialData(m("POTATO")), "tile.potatoes.name");
		materialLangMap.put(new MaterialData(m("STONE_PLATE")), "tile.pressurePlate.name");
		materialLangMap.put(new MaterialData(m("PUMPKIN")), "tile.pumpkin.name");
		materialLangMap.put(new MaterialData(m("QUARTZ_BLOCK"), 1), "tile.quartzBlock.chiseled.name");
		materialLangMap.put(new MaterialData(m("QUARTZ_BLOCK")), "tile.quartzBlock.default.name");
		materialLangMap.put(new MaterialData(m("QUARTZ_BLOCK"), 2), "tile.quartzBlock.lines.name");
		materialLangMap.put(new MaterialData(m("RAILS")), "tile.rail.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_WIRE")), "tile.redstoneDust.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_LAMP_OFF")), "tile.redstoneLight.name");
		materialLangMap.put(new MaterialData(m("REDSTONE_LAMP_ON")), "tile.redstoneLight.name");
		materialLangMap.put(new MaterialData(m("SUGAR_CANE_BLOCK")), "tile.reeds.name");
		materialLangMap.put(new MaterialData(m("SAND")), "tile.sand.default.name");
		materialLangMap.put(new MaterialData(m("SAND"), 1), "tile.sand.red.name");
		materialLangMap.put(new MaterialData(m("SANDSTONE"), 1), "tile.sandStone.chiseled.name");
		materialLangMap.put(new MaterialData(m("SANDSTONE")), "tile.sandStone.default.name");
		materialLangMap.put(new MaterialData(m("SANDSTONE"), 2), "tile.sandStone.smooth.name");
		materialLangMap.put(new MaterialData(m("SAPLING"), 4), "tile.sapling.acacia.name");
		materialLangMap.put(new MaterialData(m("SAPLING"), 2), "tile.sapling.birch.name");
		materialLangMap.put(new MaterialData(m("SAPLING"), 3), "tile.sapling.jungle.name");
		materialLangMap.put(new MaterialData(m("SAPLING")), "tile.sapling.oak.name");
		materialLangMap.put(new MaterialData(m("SAPLING"), 5), "tile.sapling.roofed_oak.name");
		materialLangMap.put(new MaterialData(m("SAPLING"), 1), "tile.sapling.spruce.name");
		materialLangMap.put(new MaterialData(m("SNOW")), "tile.snow.name");
		materialLangMap.put(new MaterialData(m("SPONGE")), "tile.sponge.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 15), "tile.stainedGlass.black.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 11), "tile.stainedGlass.blue.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 12), "tile.stainedGlass.brown.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 9), "tile.stainedGlass.cyan.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 7), "tile.stainedGlass.gray.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 13), "tile.stainedGlass.green.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 3), "tile.stainedGlass.lightBlue.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 5), "tile.stainedGlass.lime.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 2), "tile.stainedGlass.magenta.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 1), "tile.stainedGlass.orange.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 6), "tile.stainedGlass.pink.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 10), "tile.stainedGlass.purple.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 14), "tile.stainedGlass.red.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 8), "tile.stainedGlass.silver.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS")), "tile.stainedGlass.white.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS"), 4), "tile.stainedGlass.yellow.name");
		materialLangMap.put(new MaterialData(m("BRICK_STAIRS")), "tile.stairsBrick.name");
		materialLangMap.put(new MaterialData(m("NETHER_BRICK_STAIRS")), "tile.stairsNetherBrick.name");
		materialLangMap.put(new MaterialData(m("QUARTZ_STAIRS")), "tile.stairsQuartz.name");
		materialLangMap.put(new MaterialData(m("SANDSTONE_STAIRS")), "tile.stairsSandStone.name");
		materialLangMap.put(new MaterialData(m("COBBLESTONE_STAIRS")), "tile.stairsStone.name");
		materialLangMap.put(new MaterialData(m("SMOOTH_STAIRS")), "tile.stairsStoneBrickSmooth.name");
		materialLangMap.put(new MaterialData(m("WOOD_STAIRS")), "tile.stairsWood.name");
		materialLangMap.put(new MaterialData(m("ACACIA_STAIRS")), "tile.stairsWoodAcacia.name");
		materialLangMap.put(new MaterialData(m("BIRCH_WOOD_STAIRS")), "tile.stairsWoodBirch.name");
		materialLangMap.put(new MaterialData(m("DARK_OAK_STAIRS")), "tile.stairsWoodDarkOak.name");
		materialLangMap.put(new MaterialData(m("JUNGLE_WOOD_STAIRS")), "tile.stairsWoodJungle.name");
		materialLangMap.put(new MaterialData(m("SPRUCE_WOOD_STAIRS")), "tile.stairsWoodSpruce.name");
		materialLangMap.put(new MaterialData(m("STONE")), "tile.stone.name");
		materialLangMap.put(new MaterialData(m("MOSSY_COBBLESTONE")), "tile.stoneMoss.name");
		materialLangMap.put(new MaterialData(m("STEP"), 4), "tile.stoneSlab.brick.name");
		materialLangMap.put(new MaterialData(m("STEP"), 3), "tile.stoneSlab.cobble.name");
		materialLangMap.put(new MaterialData(m("STEP"), 6), "tile.stoneSlab.netherBrick.name");
		materialLangMap.put(new MaterialData(m("STEP"), 7), "tile.stoneSlab.quartz.name");
		materialLangMap.put(new MaterialData(m("STEP"), 1), "tile.stoneSlab.sand.name");
		materialLangMap.put(new MaterialData(m("STEP"), 5), "tile.stoneSlab.smoothStoneBrick.name");
		materialLangMap.put(new MaterialData(m("STEP")), "tile.stoneSlab.stone.name");
		materialLangMap.put(new MaterialData(m("STEP"), 2), "tile.stoneSlab.wood.name");
		materialLangMap.put(new MaterialData(m("SMOOTH_BRICK"), 3), "tile.stonebricksmooth.chiseled.name");
		materialLangMap.put(new MaterialData(m("SMOOTH_BRICK"), 2), "tile.stonebricksmooth.cracked.name");
		materialLangMap.put(new MaterialData(m("SMOOTH_BRICK")), "tile.stonebricksmooth.default.name");
		materialLangMap.put(new MaterialData(m("SMOOTH_BRICK"), 1), "tile.stonebricksmooth.mossy.name");
		materialLangMap.put(new MaterialData(m("LONG_GRASS"), 2), "tile.tallgrass.fern.name");
		materialLangMap.put(new MaterialData(m("LONG_GRASS"), 1), "tile.tallgrass.grass.name");
		materialLangMap.put(new MaterialData(m("LONG_GRASS")), "tile.tallgrass.shrub.name");
		materialLangMap.put(new MaterialData(m("THIN_GLASS")), "tile.thinGlass.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 15), "tile.thinStainedGlass.black.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 11), "tile.thinStainedGlass.blue.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 12), "tile.thinStainedGlass.brown.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 9), "tile.thinStainedGlass.cyan.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 7), "tile.thinStainedGlass.gray.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 13), "tile.thinStainedGlass.green.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 3), "tile.thinStainedGlass.lightBlue.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 5), "tile.thinStainedGlass.lime.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 2), "tile.thinStainedGlass.magenta.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 1), "tile.thinStainedGlass.orange.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 6), "tile.thinStainedGlass.pink.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 10), "tile.thinStainedGlass.purple.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 14), "tile.thinStainedGlass.red.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 8), "tile.thinStainedGlass.silver.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE")), "tile.thinStainedGlass.white.name");
		materialLangMap.put(new MaterialData(m("STAINED_GLASS_PANE"), 4), "tile.thinStainedGlass.yellow.name");
		materialLangMap.put(new MaterialData(m("TNT")), "tile.tnt.name");
		materialLangMap.put(new MaterialData(m("TORCH")), "tile.torch.name");
		materialLangMap.put(new MaterialData(m("TRAP_DOOR")), "tile.trapdoor.name");
		materialLangMap.put(new MaterialData(m("TRIPWIRE")), "tile.tripWire.name");
		materialLangMap.put(new MaterialData(m("TRIPWIRE_HOOK")), "tile.tripWireSource.name");
		materialLangMap.put(new MaterialData(m("VINE")), "tile.vine.name");
		materialLangMap.put(new MaterialData(m("WATER")), "tile.water.name");
		materialLangMap.put(new MaterialData(m("WATER_LILY")), "tile.waterlily.name");
		materialLangMap.put(new MaterialData(m("WEB")), "tile.web.name");
		materialLangMap.put(new MaterialData(m("GOLD_PLATE")), "tile.weightedPlate_heavy.name");
		materialLangMap.put(new MaterialData(m("IRON_PLATE")), "tile.weightedPlate_light.name");
		materialLangMap.put(new MaterialData(m("ENDER_STONE")), "tile.whiteStone.name");
		materialLangMap.put(new MaterialData(m("WOOD"), 4), "tile.wood.acacia.name");
		materialLangMap.put(new MaterialData(m("WOOD"), 5), "tile.wood.big_oak.name");
		materialLangMap.put(new MaterialData(m("WOOD"), 2), "tile.wood.birch.name");
		materialLangMap.put(new MaterialData(m("WOOD"), 3), "tile.wood.jungle.name");
		materialLangMap.put(new MaterialData(m("WOOD")), "tile.wood.oak.name");
		materialLangMap.put(new MaterialData(m("WOOD"), 1), "tile.wood.spruce.name");
		materialLangMap.put(new MaterialData(m("WOOD_STEP"), 4), "tile.woodSlab.acacia.name");
		materialLangMap.put(new MaterialData(m("WOOD_STEP"), 5), "tile.woodSlab.big_oak.name");
		materialLangMap.put(new MaterialData(m("WOOD_STEP"), 2), "tile.woodSlab.birch.name");
		materialLangMap.put(new MaterialData(m("WOOD_STEP"), 3), "tile.woodSlab.jungle.name");
		materialLangMap.put(new MaterialData(m("WOOD_STEP")), "tile.woodSlab.oak.name");
		materialLangMap.put(new MaterialData(m("WOOD_STEP"), 1), "tile.woodSlab.spruce.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 15), "tile.woolCarpet.black.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 11), "tile.woolCarpet.blue.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 12), "tile.woolCarpet.brown.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 9), "tile.woolCarpet.cyan.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 7), "tile.woolCarpet.gray.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 13), "tile.woolCarpet.green.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 3), "tile.woolCarpet.lightBlue.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 5), "tile.woolCarpet.lime.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 2), "tile.woolCarpet.magenta.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 1), "tile.woolCarpet.orange.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 6), "tile.woolCarpet.pink.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 10), "tile.woolCarpet.purple.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 14), "tile.woolCarpet.red.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 8), "tile.woolCarpet.silver.name");
		materialLangMap.put(new MaterialData(m("CARPET")), "tile.woolCarpet.white.name");
		materialLangMap.put(new MaterialData(m("CARPET"), 4), "tile.woolCarpet.yellow.name");
		materialLangMap.put(new MaterialData(m("WORKBENCH")), "tile.workbench.name");
		materialLangMap.put(new MaterialData(m("COBBLESTONE")), "tile.stonebrick.name");
	}
	
	public Material m(String name) {
		return Material.getMaterial(name);
	}
	
	public class MaterialData {
		
		public Material material;
		public int damage = 0;
		
		private MaterialData(Material material) {
			this.material = material;
		}
		
		private MaterialData(Material material, int damage) {
			this.material = material;
			this.damage = damage;
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof MaterialData) {
				return ((MaterialData) object).material == material && ((MaterialData) object).damage == damage;
			}
			return false;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public int hashCode() {
			return material.name().length() + damage + material.getId();
		}
	}
}
