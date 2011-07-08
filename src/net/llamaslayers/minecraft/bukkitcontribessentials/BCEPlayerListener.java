package net.llamaslayers.minecraft.bukkitcontribessentials;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
		doWorldBasedEvents(event.getPlayer().getWorld(),
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
			doWorldBasedEvents(event.getTo().getWorld(),
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

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		ContribPlayer player = ContribCraftPlayer.getContribPlayer(event
				.getPlayer());
		if (player.isEnabledBukkitContribSinglePlayerMod()
				&& !playerSentTexturePack.contains(player.getName())) {
			playerSentTexturePack.add(player.getName());
			doWorldBasedEvents(player.getWorld(), player,
					BukkitContribEssentials.instance.getConfiguration());
		}
	}

	private static class BCEPlayerTask implements Runnable {
		private final String playerName;
		private final BCEPlayerListener listener;

		public BCEPlayerTask(String playerName, BCEPlayerListener listener) {
			this.playerName = playerName;
			this.listener = listener;
		}

		public void run() {
			listener.doPlayerBasedActions(
					ContribCraftPlayer.getContribPlayer(Bukkit.getServer()
							.getPlayer(playerName)),
					BukkitContribEssentials.instance.getConfiguration());
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
			ConfigurationNode region = BukkitContribEssentials.getRegion(event
					.getTo());
			if (region != null) {
				if (region.getString("name", "").length() > 26) {
					BukkitContribEssentials.log
							.severe("[BukkitContribEssentials] Region name for "
									+ region.getString("name", "")
									+ " is too long! Maximum length: 26 characters");
				} else if (region.getString("description", "").length() > 26) {
					BukkitContribEssentials.log
							.severe("[BukkitContribEssentials] Region description for "
									+ region.getString("name", "")
									+ " is too long! Maximum length: 26 characters");
				} else {
					try {
						player.sendNotification(region.getString("name", ""),
								region.getString("description", ""),
								Material.valueOf(region.getString("icon", "")));
					} catch (IllegalArgumentException ex) {
						BukkitContribEssentials.log
								.severe("[BukkitContribEssentials] Region icon for "
										+ region.getString("name", "")
										+ " is invalid.");
					}
				}
				if (region.getString("music") != null) {
					try {
						BukkitContrib.getSoundManager().playMusic(player,
								Music.valueOf(region.getString("music")));
					} catch (IllegalArgumentException ex) {
						BukkitContribEssentials.log
								.severe("[BukkitContribEssentials] Region music for "
										+ region.getString("name", "")
										+ " is invalid.");
					}
				}
			}
		}
	}

	private void doWorldBasedEvents(World world, ContribPlayer player,
			Configuration config) {
		String texturePackUrl = config.getString(
				"texturepack." + world.getName(),
				config.getString("texturepack.default"));
		if (texturePackUrl != null) {
			try {
				player.setTexturePack(texturePackUrl);
			} catch (IllegalArgumentException ex) {
			}
		}
	}

	private void doPlayerBasedActions(ContribPlayer player, Configuration config) {
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
