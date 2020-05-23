package xyz.acrylicstyle.mwb;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicalWaterBucket extends JavaPlugin implements Listener {
    public static NamespacedKey MAGICAL_WATER_BUCKET = null;
    public static NamespacedKey MAGICAL_BUCKET = null;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        MAGICAL_WATER_BUCKET = new NamespacedKey(this, "magical_water_bucket");
        ShapedRecipe recipe = new ShapedRecipe(MAGICAL_WATER_BUCKET, getMagicalBucket(Type.WATER));
        recipe.shape("XXX", "XXX", "XXX");
        recipe.setIngredient('X', Material.WATER_BUCKET);
        Bukkit.addRecipe(recipe);

        MAGICAL_BUCKET = new NamespacedKey(this, "magical_bucket");
        ShapedRecipe recipe2 = new ShapedRecipe(MAGICAL_BUCKET, getMagicalBucket(Type.EMPTY));
        recipe2.shape("XXX", "XXX", "XXX");
        recipe2.setIngredient('X', Material.BUCKET);
        Bukkit.addRecipe(recipe);
    }

    public ItemStack getMagicalBucket(Type type) {
        Material material;
        if (type == Type.LAVA) {
            material = Material.LAVA_BUCKET;
        } else if (type == Type.WATER) {
            material = Material.WATER_BUCKET;
        } else {
            material = Material.BUCKET;
        }
        ItemStack result = new ItemStack(material);
        return addData(result, type);
    }

    public enum Type {
        LAVA,
        WATER,
        EMPTY,
    }

    private ItemStack addData(ItemStack result, Type type) {
        String s;
        if (type == Type.LAVA) {
            s = ChatColor.RED + "Magical Lava Bucket";
        } else if (type == Type.WATER) {
            s = ChatColor.AQUA + "Magical Water Bucket";
        } else {
            s = ChatColor.GREEN + "Magical Empty Bucket";
        }
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(s);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        result.setItemMeta(meta);
        net.minecraft.server.v1_15_R1.ItemStack handle = CraftItemStack.asNMSCopy(result);
        NBTTagCompound tag = handle.getOrCreateTag();
        tag.setBoolean("infinite", true);
        handle.setTag(tag);
        result = CraftItemStack.asBukkitCopy(handle);
        return result;
    }

    public static boolean isMagicalWaterBucket(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.WATER_BUCKET) return false;
        return hasInfiniteTag(item);
    }

    public static boolean isMagicalBucket(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.BUCKET) return false;
        return hasInfiniteTag(item);
    }

    private static boolean hasInfiniteTag(@NotNull ItemStack item) {
        net.minecraft.server.v1_15_R1.ItemStack handle = CraftItemStack.asNMSCopy(item);
        if (!handle.hasTag()) return false;
        NBTTagCompound tag = handle.getOrCreateTag();
        return tag.hasKey("infinite") && tag.getBoolean("infinite");
    }

    @Override
    public void onDisable() {
        Bukkit.removeRecipe(MAGICAL_WATER_BUCKET);
        Bukkit.removeRecipe(MAGICAL_BUCKET);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        boolean mainHand;
        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            mainHand = false;
        } else if (e.getHand() == EquipmentSlot.HAND) {
            mainHand = true;
        } else return;
        if (isMagicalWaterBucket(mainHand ? e.getPlayer().getInventory().getItemInMainHand() : e.getPlayer().getInventory().getItemInOffHand())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (mainHand) {
                        e.getPlayer().getInventory().setItemInMainHand(getMagicalBucket(Type.WATER));
                    } else {
                        e.getPlayer().getInventory().setItemInOffHand(getMagicalBucket(Type.WATER));
                    }
                }
            }.runTask(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        boolean mainHand;
        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            mainHand = false;
        } else if (e.getHand() == EquipmentSlot.HAND) {
            mainHand = true;
        } else return;
        if (isMagicalBucket(mainHand ? e.getPlayer().getInventory().getItemInMainHand() : e.getPlayer().getInventory().getItemInOffHand())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (mainHand) {
                        e.getPlayer().getInventory().setItemInMainHand(getMagicalBucket(Type.EMPTY));
                    } else {
                        e.getPlayer().getInventory().setItemInOffHand(getMagicalBucket(Type.EMPTY));
                    }
                }
            }.runTask(this);
        }
    }
}
