package BrannonBlair.com.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.bossbar.BossBarAPI;

import me.confuser.barapi.BarAPI;

public class HexBoss extends JavaPlugin implements Listener {
    HashMap<UUID, String> mobChallengeList = new HashMap<UUID, String>();
    HashMap<UUID, String> pigList = new HashMap<UUID, String>();
    public Skeleton skeleton = null;
    public HexBoss plugin;
    int taskid;
    int taskid2;
    int taskid3;
    int taskid4;
    public boolean dead = true;

    FileConfiguration config = getConfig();
    public String bossTitleName = getConfig().getString("bossBarsName");
    public int bossInterval = Integer.valueOf(getConfig().getInt("boss_interval"));
    public int minionInterval = Integer.valueOf(getConfig().getInt("minion_interval"));
    public String bossSpawn = getConfig().getString("messages.boss_spawn");
    public String bossDeath = getConfig().getString("messages.boss_death");
    
    public String rewardName = getConfig().getString("reward.item_name");
    public String itemName = getConfig().getString("reward.name");
    public String rLore0 = getConfig().getString("reward.lore");
    public String rLore1 = getConfig().getString("reward.lore1");

    public String bossName = getConfig().getString("boss_settings.boss_name");
    public int haste = Integer.valueOf(getConfig().getInt("boss_settings.haste_level"));
    public int speed = Integer.valueOf(getConfig().getInt("boss_settings.speed_level"));
    public int strength = Integer.valueOf(getConfig().getInt("boss_settings.strength_level"));
    public int sMaxhealth = Integer.valueOf(getConfig().getInt("boss_settings.max_health"));
    public int sSethealth = Integer.valueOf(getConfig().getInt("boss_settings.set_health"));

    public String pigName = getConfig().getString("minion_settings.minion_name");
    public int pigMaxhealth = Integer.valueOf(getConfig().getInt("minion_settings.minion_max_health"));
    public int pigSethealth = Integer.valueOf(getConfig().getInt("minion_settings.minion_set_health"));
    public int pigRange = Integer.valueOf(getConfig().getInt("minion_settings.minion_range"));

    public String worldName = getConfig().getString("world_settings.World");
    public int spawnX = Integer.valueOf(getConfig().getInt("world_settings.X"));
    public int spawnY = Integer.valueOf(getConfig().getInt("world_settings.Y"));
    public int spawnZ = Integer.valueOf(getConfig().getInt("world_settings.Z"));

    String prefix = ChatColor.RED + "" + ChatColor.BOLD + "(!) ";
    int interval = bossInterval;
    int minutesToCountDown = interval;
    int piggyCount = Integer.valueOf(getConfig().getInt("minion_settings.minion_max"));

    int pigCount = 0;

    public void onEnable () {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Successfully Started");
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        saveConfig();
        SkeletonDie();
        pigsDie();
        startMinutesCountdown();
    }

    public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("hexboss")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("hexboss.time")) {
                    if (args.length == 0) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                prefix + "&cToo few arguments. &a/hexboss time"));
                    } else if (args[0].equalsIgnoreCase("time")) {
                        if (minutesToCountDown <= 0) {
                            sender.sendMessage(
                                    ChatColor.translateAlternateColorCodes('&', prefix + "&cThe HexBoss is Alive!"));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix
                                    + "&cHexBoss &6Spawning in.. &e" + (minutesToCountDown + 1) + " &6minutes!"));
                        }
                    } else if (args.length > 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                prefix + "&cToo many arguments. &a/hexboss time"));
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "You don't have access to this command.");
                }
                return true;
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command!");
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("hexadmin")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("hexboss.admin")) {
                    if (args.length == 0) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                prefix + "&cToo few arguments. &a/hexadmin [time]"));
                    } else if (args.length == 1) {
                        String test = args[0].replaceAll("[^0-9]+", "");
                        int minuteNumber = Integer.parseInt(test);
                        minutesToCountDown = minuteNumber;
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                prefix + "&cThe boss will now spawn in about &b" + minutesToCountDown + " &cminutes!"));
                        minutesToCountDown = minuteNumber + 1;
                    } else if (args.length > 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                prefix + "&cToo many arguments. &a/hexadmin [number] to set the time."));
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "You don't have access to this command.");
                }
                return true;
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command!");
                return true;
            }
        }
        return true;
    }

    public void SkeletonDie () {
        for (Entity en : getServer().getWorld(worldName).getEntitiesByClasses(Skeleton.class)) {
            if (((LivingEntity) en).getCustomName() == null) {
                continue;
            }
            if (((LivingEntity) en).getCustomName().equals(ChatColor.translateAlternateColorCodes('&', bossName))) {
                en.remove();
            }
        }
    }

    public void fixBar (Player p) {
        double dis = 26.0D;
        Entity b = skeleton;
        if (p.getLocation().distance(skeleton.getLocation()) < dis) {
            dis = p.getLocation().distance(skeleton.getLocation());
            b = skeleton;
        }
        if (b != null) {
            showBossBar(p, b);
        }
    }

    @SuppressWarnings("deprecation")
    public void barRemoval () {
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            BarAPI.removeBar(player);
            i++;
        }
    }

    public void showBossBar (Player p, Entity e) {
        String tittle = null;
        if (bossTitleName != null)
            tittle = bossTitleName;
        try {
            do {
            } while (tittle.length() > 64);
        } catch (Exception x) {
            System.out.println("showBossBar error: ");
            x.printStackTrace();
        }
        tittle = ChatColor.translateAlternateColorCodes('&', tittle);

        float health = (float) ((Damageable) e).getHealth();
        float setHealth = health * 100.0F / (sSethealth - (sSethealth / 20));
        if (setHealth < 200) {
            BarAPI.removeBar(p);
        }
        try {
            BossBarAPI.setMessage(p, tittle, setHealth);
        } catch (Exception localException1) {
        }
    }

    @EventHandler
    public void onEntityDeathEvent (EntityDeathEvent event) {
        ItemStack perms = new ItemStack(Material.getMaterial(rewardName));
        ItemMeta permsmeta = perms.getItemMeta();
        permsmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
        ArrayList<String> Lore11 = new ArrayList<String>();
        Lore11.add(ChatColor.translateAlternateColorCodes('&', rLore0));
        Lore11.add(ChatColor.translateAlternateColorCodes('&', rLore1));
        permsmeta.setLore(Lore11);
        perms.setItemMeta(permsmeta);
        UUID entityUUID = event.getEntity().getUniqueId();
        if (mobChallengeList.containsKey(entityUUID)) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + bossDeath));
            event.getDrops().clear();
            for (int i = 0; i < 20; i++) {
                event.getDrops().add(perms);
            }
            mobChallengeList.remove(entityUUID);
            dead = true;
        }
        if (pigList.containsKey(entityUUID)) {
            pigList.remove(entityUUID);
        }
    }
    

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnitityDamaged (EntityDamageEvent e) {
        Entity mob = e.getEntity();
        UUID entityUUID = e.getEntity().getUniqueId();
        if (mobChallengeList.containsKey(entityUUID)) {
            for (Entity entity : mob.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                if ((entity instanceof Player)) {
                    fixBar((Player) entity);
                }
            }
        }
    }

    public Runnable startMinutesCountdown () {
        this.taskid = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run () {
                HexBoss.this.minutesToCountDown -= 1;
                if (HexBoss.this.minutesToCountDown == 0) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + bossSpawn));
                    World w = getServer().getWorld(worldName);
                    Location location = new Location(w, spawnX, spawnY, spawnZ);
                    spawnWitherSkeleton(location);
                    dead = false;
                    spawnPigs();
                    isDead();
                }
            }
        }, 0L, 1200L);
        return null;
    }

    public void pigsDie () {
        for (Entity en : getServer().getWorld(worldName).getEntitiesByClasses(PigZombie.class)) {
            if (((LivingEntity) en).getCustomName() == null) {
                continue;
            }
            if (((LivingEntity) en).getCustomName().equals(ChatColor.translateAlternateColorCodes('&', pigName))) {
                en.remove();
                pigList.clear();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void spawnWitherSkeleton (Location loc) {
        skeleton = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
        skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 99999999, haste));
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, speed));
        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 99999999, strength));
        skeleton.setMaxHealth(sMaxhealth);
        skeleton.setHealth(sSethealth);
        skeleton.setCustomName(ChatColor.translateAlternateColorCodes('&', bossName));
        skeleton.setCustomNameVisible(true);
        skeleton.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD, 1));
        skeleton.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
        skeleton.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
        skeleton.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
        skeleton.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
        skeleton.setRemoveWhenFarAway(false);
        skeleton.setCanPickupItems(false);
        mobChallengeList.put(skeleton.getUniqueId(), "Server");
    }

    public Runnable spawnPigs () {
        this.taskid3 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @SuppressWarnings("deprecation")
            public void run () {
                Random rn = new Random();
                int range = pigRange;
                int randomNum = rn.nextInt(range);
                if (pigList.size() < piggyCount) {
                    for (int z1 = 1; z1 < randomNum; z1++) {
                        int x = skeleton.getLocation().getBlockX() + rn.nextInt(3 - (-3) + 1) + (-3);
                        int y = skeleton.getLocation().getBlockY() + 1;
                        int z = skeleton.getLocation().getBlockZ() + rn.nextInt(3 - (-3) + 1) + (-3);
                        World w = getServer().getWorld(worldName);
                        Location loc = new Location(w, x, y, z);
                        PigZombie pig = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
                        pig.setMaxHealth(pigMaxhealth);
                        pig.setHealth(pigSethealth);
                        pig.getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET, 1));
                        pig.setCustomName(ChatColor.translateAlternateColorCodes('&', pigName));
                        pig.setCustomNameVisible(true);
                        pig.setAngry(true);
                        pig.setBaby(true);
                        pig.setRemoveWhenFarAway(false);
                        pigList.put(pig.getUniqueId(), "Server" + z1);
                    }
                }
            }
        }, 0L, 40L * minionInterval);
        return null;
    }

    public Runnable isDead () {
        this.taskid2 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run () {
                if (dead == true) {
                    Bukkit.getServer().getScheduler().cancelTask(taskid2);
                    Bukkit.getServer().getScheduler().cancelTask(taskid3);
                    pigsDie();
                    barRemoval();
                    HexBoss.this.minutesToCountDown = interval;
                }
            }
        }, 0L, 20L);
        return null;
    }
}