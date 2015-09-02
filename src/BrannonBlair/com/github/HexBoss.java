package BrannonBlair.com.github;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HexBoss extends JavaPlugin implements Listener {
	static HashMap<UUID, String> mobChallengeList = new HashMap<UUID, String>();
	public HexBoss plugin;
	int taskid;
	int taskid2;
	int taskid3;
	int interval = 1;
	int minutesToCountDown = interval;
	public boolean dead = true;
	String prefix = ChatColor.AQUA + "[" + ChatColor.GREEN + "HexBoss 1.0" + ChatColor.AQUA + "] ";

	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Successfully Started");
		getServer().getPluginManager().registerEvents(this, this);

		startMinutesCountdown();
	}

	public Runnable startMinutesCountdown() {
		this.taskid = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				HexBoss.this.minutesToCountDown -= 1;
				if (HexBoss.this.minutesToCountDown == 0) {
					Bukkit.broadcastMessage(prefix + "You better run!");
					Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Wither Spawning");
					World w = getServer().getWorld("world");
					Location location = new Location(w, -57, 67, 273);
					spawnWitherSkeleton(location);
					dead = false;
					spawnPigs(location);
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
					HexBoss.this.minutesToCountDown = interval;
					Bukkit.getServer().getScheduler().cancelTask(taskid2);
					Bukkit.getServer().getScheduler().cancelTask(taskid3);
				}
			}
		}, 0L, 20L);
		return null;
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		UUID entityUUID = event.getEntity().getUniqueId();
		if (mobChallengeList.containsKey(entityUUID)) {
			Bukkit.broadcastMessage(prefix + "You killed the boss!");
			Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Wither dead");
			mobChallengeList.remove(entityUUID);
			dead = true;
		}
		return;
	}

	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof PigZombie) {
			PigZombie AngryPig = (PigZombie) event.getEntity();
			AngryPig.setAngry(true);
		}
	}

	public Runnable spawnPigs(Location loc) {
		this.taskid3 = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				int minimum = 1;
				int maximum = 5;
				Random rn = new Random();
				int range = maximum - minimum + 1;
				int randomNum =  rn.nextInt(range) + minimum;
				for (int z = 1; z < randomNum; z++) {
					PigZombie pig = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
					pig.setAngry(true);
				}
			}
		}, 0L, 200L);
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void spawnWitherSkeleton(Location loc) {
		Skeleton skeleton = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
		skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
		skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 99999999, 2));
		skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, 2));
		skeleton.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 99999999, 2));
		skeleton.setMaxHealth(1001);
		skeleton.setHealth(200);
		skeleton.setCustomName("Dear god");
		skeleton.setCustomNameVisible(true);
		mobChallengeList.put(skeleton.getUniqueId(), "Server");
	}
}
