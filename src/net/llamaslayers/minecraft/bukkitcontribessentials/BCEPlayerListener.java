package net.llamaslayers.minecraft.bukkitcontribessentials;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkitcontrib.BukkitContrib;
import org.bukkitcontrib.player.ContribCraftPlayer;
import org.bukkitcontrib.player.ContribPlayer;
import org.bukkitcontrib.sound.Music;

public class BCEPlayerListener extends PlayerListener {
	private final Map<Integer, Location> locationCache = new HashMap<Integer, Location>();
	private final Set<String> playerSentTexturePack = new HashSet<String>();

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		doWorldBasedActions(event.getPlayer().getWorld(),
				ContribCraftPlayer.getContribPlayer(event.getPlayer()),
				BukkitContribEssentials.instance.getConfiguration());
		locationCache.put(event.getPlayer().getEntityId(), event.getPlayer()
				.getLocation());

		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(BukkitContribEssentials.instance,
						new BCEPlayerTask(event.getPlayer().getName(), this), 1);
	}

	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getFrom().getWorld() != event.getTo().getWorld()) {
			doWorldBasedActions(event.getTo().getWorld(),
					ContribCraftPlayer.getContribPlayer(event.getPlayer()),
					BukkitContribEssentials.instance.getConfiguration());
		}
		locationCache.put(event.getPlayer().getEntityId(), event.getPlayer()
				.getLocation());
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		playerSentTexturePack.remove(event.getPlayer().getName());
	}

	private static class BCEPlayerTask implements Runnable {
		private final String playerName;
		private final BCEPlayerListener listener;

		public BCEPlayerTask(String playerName, BCEPlayerListener listener) {
			this.playerName = playerName;
			this.listener = listener;
		}

		public void run() {
			Player player = Bukkit.getServer().getPlayer(playerName);
			if (player != null) {
				listener.doPlayerBasedActions(
						ContribCraftPlayer.getContribPlayer(player),
						BukkitContribEssentials.instance.getConfiguration());
			}
		}
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(BukkitContribEssentials.instance,
						new BCEPlayerTask(event.getPlayer().getName(), this), 1);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		ContribPlayer player = ContribCraftPlayer.getContribPlayer(event
				.getPlayer());
		Location oldLocation = locationCache.containsKey(event.getPlayer()
				.getEntityId()) ? locationCache.get(event.getPlayer()
				.getEntityId()) : event.getFrom();
		locationCache.put(event.getPlayer().getEntityId(), event.getTo());
		ConfigurationNode from = BukkitContribEssentials.getRegion(oldLocation);
		ConfigurationNode to = BukkitContribEssentials.getRegion(event.getTo());
		if ((from != to && (from == null || to == null))
				|| (from != null && to != null && !from.getString("name", "")
						.equals(to.getString("name")))) {

			if (to != null) {
				if (to.getString("name", "").length() > 26) {
					BukkitContribEssentials.log
							.severe("[BukkitContribEssentials] Region name for "
									+ to.getString("name", "")
									+ " is too long! Maximum length: 26 characters");
				} else if (to.getString("description", "").length() > 26) {
					BukkitContribEssentials.log
							.severe("[BukkitContribEssentials] Region description for "
									+ to.getString("name", "")
									+ " is too long! Maximum length: 26 characters");
				} else {
					try {
						player.sendNotification(to.getString("name", ""),
								to.getString("description", ""),
								Material.valueOf(to.getString("icon", "")));
					} catch (IllegalArgumentException ex) {
						BukkitContribEssentials.log
								.severe("[BukkitContribEssentials] Region icon for "
										+ to.getString("name", "")
										+ " is invalid.");
					}
				}
				if (from != null && from.getString("music") != null) {
					BukkitContrib.getSoundManager().stopMusic(player, false,
							2000);
				}
				if (to.getString("music") != null) {
					try {
						BukkitContrib.getSoundManager().playMusic(player,
								Music.valueOf(to.getString("music")));
					} catch (IllegalArgumentException ex) {
						BukkitContribEssentials.log
								.severe("[BukkitContribEssentials] Region music for "
										+ to.getString("name", "")
										+ " is invalid.");
					}
				}
			}
		}
	}

	protected void doWorldBasedActions(World world, ContribPlayer player,
			Configuration config) {
		String texturePackUrl = config.getString(
				"texturepack." + world.getName(),
				config.getString("texturepack.default"));
		if (texturePackUrl != null) {
			try {
				player.setTexturePack(texturePackUrl);
			} catch (IllegalArgumentException ex) {
				BukkitContribEssentials.log
						.severe("[BukkitContribEssentails] Error with texture pack for world "
								+ player.getWorld().getName()
								+ " : "
								+ ex.getMessage());
			}
		}
	}

	protected void doPlayerBasedActions(ContribPlayer player,
			Configuration config) {
		if (config.getString("player." + player.getName() + ".cape") != null) {
			BukkitContrib.getAppearanceManager().setGlobalCloak(player,
					config.getString("player." + player.getName() + ".cape"));
		}
		if (config.getString("player." + player.getName() + ".skin") != null) {
			BukkitContrib.getAppearanceManager().setGlobalSkin(player,
					config.getString("player." + player.getName() + ".skin"));
		}
		if (config.getString("player." + player.getName() + ".title") != null) {
			BukkitContrib.getAppearanceManager().setGlobalTitle(player,
					config.getString("player." + player.getName() + ".title"));
		}
	}
}
