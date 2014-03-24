package com.survivorserver.GlobalMarket.Chat;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TellRawItemStack {

    public int id;
    public int Damage;
    public TellRawItemTag tag;

    @SuppressWarnings("deprecation")
    public TellRawItemStack(ItemStack item) {
        id = item.getTypeId();
        Damage = item.getDurability();
        if (item.hasItemMeta()) {
            tag = new TellRawItemTag(item.getItemMeta());
        }
    }

    public class TellRawItemTag {

        public TellRawDisplayTag display;
        public TellRawEnchantTag[] ench;

        public TellRawItemTag(ItemMeta meta) {
            display = new TellRawDisplayTag(meta);
            if (meta.hasLore()) {
                buildLore(display, meta);
            }
            if (meta.hasEnchants()) {
                buildEnchants(display, meta);
            }
        }

        public void buildEnchants(TellRawDisplayTag tag, ItemMeta meta) {
            Map<Enchantment, Integer> enchants = meta.getEnchants();
            ench = new TellRawEnchantTag[enchants.size()];
            int i = 0;
            for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                ench[i] = new TellRawEnchantTag(entry.getKey(), entry.getValue());
                i++;
            }
        }

        public void buildLore(TellRawDisplayTag tag, ItemMeta meta) {
            List<String> lore = meta.getLore();
            tag.Lore = new String[lore.size()];
            for (int i = 0; i < lore.size(); i++) {
                tag.Lore[i] = lore.get(i);
            }
        }

        public class TellRawDisplayTag {

            public String Name;
            public String[] Lore;

            public TellRawDisplayTag(ItemMeta meta) {
                if (meta.hasDisplayName()) {
                    Name = meta.getDisplayName();
                }
            }
        }

        public class TellRawEnchantTag {

            public int id;
            public int lvl;

            @SuppressWarnings("deprecation")
            public TellRawEnchantTag(Enchantment enchant, int level) {
                id = enchant.getId();
                lvl = level;
            }
        }
    }
}
