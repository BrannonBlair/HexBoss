package BrannonBlair.com.github;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.bossbar.BossBarAPI;

public class HexBoss extends JavaPlugin implements Listener {
	HashMap<UUID, String> mobChallengeList = new HashMap<UUID, String>();
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

	String prefix = ChatColor.AQUA + "[" + ChatColor.GREEN + "HexBoss" + ChatColor.AQUA + "] ";
	int interval = bossInterval;
	int minutesToCountDown = interval;
	Location SkeleDead;

	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Successfully Started");
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		SkeletonDie();
		pigsDie();
		startMinutesCountdown();
	}

	public void SkeletonDie() {
		for (Entity en : getServer().getWorld(worldName).getEntitiesByClasses(Skeleton.class)) {
			if (((LivingEntity) en).getCustomName() == null) {
				continue;
			}
			if (((LivingEntity) en).getCustomName().equals(ChatColor.translateAlternateColorCodes('&', bossName))) {
				en.remove();
			}
		}
	}

	public void fixBar(Player p) {
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
	public void barRemoval(Player p) {
		double dis = 26.0D;
		if (p.getLocation().distance(((Entity) SkeleDead).getLocation()) < dis) {
			dis = p.getLocation().distance(((Entity) SkeleDead).getLocation());
		}
		removeBar(p);
	}
	public void clearInfo(Player player) {
			BossBarAPI.removeBar(player);
			Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Removed");
	}

	public void showBossBar(Player p, Entity e) {
		String tittle = null;
		if (bossTitleName != null)
			tittle = bossTitleName;
		int count = 4;
		try {
			do {
				count--;
				if (count <= 0) {
					break;
				}
			} while (

			tittle.length() > 64);
		} catch (Exception x) {
			System.out.println("showBossBar error: ");
			x.printStackTrace();
		}
		tittle = ChatColor.translateAlternateColorCodes('&', tittle);

		float health = (float) ((Damageable) e).getHealth();
		float maxHealth = (float) ((Damageable) e).getMaxHealth();
		float setHealth = health * 100.0F / maxHealth;
		try {
			BossBarAPI.setMessage(p, tittle, setHealth);
		} catch (Exception localException1) {
		}
	}

	public Runnable startMinutesCountdown() {
		this.taskid = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
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

	public Runnable isDead() {
		this.taskid2 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (dead == true) {
					Bukkit.getServer().getScheduler().cancelTask(taskid2);
					Bukkit.getServer().getScheduler().cancelTask(taskid3);
					pigsDie();
					HexBoss.this.minutesToCountDown = interval;
				}
			}
		}, 0L, 20L);
		return null;
	}

	public Runnable removeBar(Player p) {
		this.taskid4 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (dead == true) {
					clearInfo(p);
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + "This event is canceled"));
					Bukkit.getServer().getScheduler().cancelTask(taskid4);
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + "task 4"));
				}
			}
		}, 0L, 20L);
		return null;
	}
	
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		UUID entityUUID = event.getEntity().getUniqueId();
		if (mobChallengeList.containsKey(entityUUID)) {
			SkeleDead = skeleton.getLocation();
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + bossDeath));
			mobChallengeList.remove(entityUUID);
			dead = true;
		}
	}

	public void pigsDie() {
		for (Entity en : getServer().getWorld(worldName).getEntitiesByClasses(PigZombie.class)) {
			if (((LivingEntity) en).getCustomName() == null) {
				continue;
			}
			if (((LivingEntity) en).getCustomName().equals(ChatColor.translateAlternateColorCodes('&', pigName))) {
				en.remove();
			}
		}
	}

	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof PigZombie) {
			PigZombie AngryPig = (PigZombie) event.getEntity();
			AngryPig.setAngry(true);
		}
	}

	public Runnable spawnPigs() {
		this.taskid3 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				Random rn = new Random();
				int range = pigRange;
				int randomNum = rn.nextInt(range);
				for (int z1 = 1; z1 < randomNum; z1++) {
					int x = skeleton.getLocation().getBlockX() + rn.nextInt(3 - (-3) + 1) + (-3);
					int y = skeleton.getLocation().getBlockY() + 1;
					int z = skeleton.getLocation().getBlockZ() + rn.nextInt(3 - (-3) + 1) + (-3);
					World w = getServer().getWorld("world");
					Location loc = new Location(w, x, y, z);
					PigZombie pig = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
					pig.setMaxHealth(pigMaxhealth);
					pig.setHealth(pigSethealth);
					pig.setCustomName(ChatColor.translateAlternateColorCodes('&', pigName));
					pig.setCustomNameVisible(true);
					pig.setAngry(true);
					pig.setBaby(true);
				}
			}
		}, 0L, 40L * minionInterval);
		return null;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEnitityDamaged(EntityDamageEvent e) {
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

	@SuppressWarnings("deprecation")
	public void spawnWitherSkeleton(Location loc) {
		skeleton = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
		skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
		skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 99999999, haste));
		skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, speed));
		skeleton.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 99999999, strength));
		skeleton.setMaxHealth(sMaxhealth);
		skeleton.setHealth(sSethealth);
		skeleton.setCustomName(ChatColor.translateAlternateColorCodes('&', bossName));
		skeleton.setCustomNameVisible(true);
		mobChallengeList.put(skeleton.getUniqueId(), "Server");
	}
}