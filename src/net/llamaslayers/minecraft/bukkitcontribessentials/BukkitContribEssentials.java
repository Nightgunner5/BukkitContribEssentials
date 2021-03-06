package net.llamaslayers.minecraft.bukkitcontribessentials;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkitcontrib.event.bukkitcontrib.BukkitContribSPEnable;
import org.bukkitcontrib.player.ContribCraftPlayer;

/**
 * @author Nightgunner5
 */
public class BukkitContribEssentials extends JavaPlugin {
	protected final static Logger log = Logger
			.getLogger("Minecraft.BukkitContribEssentials");
	protected static BukkitContribEssentials instance;
	protected BCEPlayerListener playerListener;
	protected BCEMusicCommand musicCommand;
	protected BCECommand masterCommand;

	public void onDisable() {
	}

	public void onEnable() {
		instance = this;
		PluginManager pm = getServer().getPluginManager();

		if (pm.getPlugin("BukkitContrib") == null) {
			try {
				download(log, new URL("http://bit.ly/autoupdateBukkitContrib"),
						new File("plugins/BukkitContrib.jar"));
				pm.loadPlugin(new File("plugins/BukkitContrib.jar"));
				pm.enablePlugin(pm.getPlugin("BukkitContrib"));
			} catch (Exception ex) {
				log.warning("[BukkitContribEssentials] Failed to install BukkitContrib, you may have to restart your server or install it manually.");
			}
		}
		playerListener = new BCEPlayerListener();
		musicCommand = new BCEMusicCommand();
		masterCommand = new BCECommand();

		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.CUSTOM_EVENT, new CustomEventListener() {
			@Override
			public void onCustomEvent(Event _event) {
				if (_event instanceof BukkitContribSPEnable) {
					BukkitContribSPEnable event = (BukkitContribSPEnable) _event;
					playerListener.doWorldBasedActions(event.getPlayer()
							.getWorld(), event.getPlayer(), getConfiguration());
				}
			}
		}, Event.Priority.Normal, this);

		getCommand("playmusic").setExecutor(musicCommand);
		getCommand("bukkitcontribessentials").setExecutor(masterCommand);

		if (getConfiguration().getKeys().isEmpty()) {
			Configuration config = getConfiguration();
			config.setProperty("texturepack.default", "");
			config.setProperty("texturepack.world_voxel",
					"http://dl.dropbox.com/u/32644765/texturepacks/vbtp0-pangea.zip");

			List<Map<String, Object>> worldRegions = new ArrayList<Map<String, Object>>();
			worldRegions.add(new HashMap<String, Object>());
			worldRegions.add(new HashMap<String, Object>());

			Map<String, Object> tmp;

			worldRegions.get(0).put("name", "Example Region");
			worldRegions.get(0).put("description", "Is that... sand?");
			worldRegions.get(0).put("icon", "SANDSTONE");
			tmp = new HashMap<String, Object>();
			worldRegions.get(0).put("min", tmp);
			tmp.put("x", -10);
			tmp.put("y", 0);
			tmp.put("z", -10);
			tmp = new HashMap<String, Object>();
			worldRegions.get(0).put("max", tmp);
			tmp.put("x", 10);
			tmp.put("y", 127);
			tmp.put("z", 10);

			worldRegions.get(1).put("name", "Other Region");
			worldRegions.get(1).put("description", "That is also sand.");
			worldRegions.get(1).put("icon", "SAND");
			worldRegions.get(1).put("music", "MINECRAFT_THEME");
			tmp = new HashMap<String, Object>();
			worldRegions.get(1).put("min", tmp);
			tmp.put("x", 50);
			tmp.put("y", 0);
			tmp.put("z", -5);
			tmp = new HashMap<String, Object>();
			worldRegions.get(1).put("max", tmp);
			tmp.put("x", 60);
			tmp.put("y", 127);
			tmp.put("z", 10);

			config.setProperty("regions.world", worldRegions);

			config.setProperty("player.nightgunner5.cape",
					"http://llamaslayers.net/cupcape.png");
			config.setProperty("player.nightgunner5.title",
					"Nightgunner5\nThe Great");

			config.save();
		}

		// If any players joined before this plugin was started
		// (eg. if the /reload command is used):
		for (World world : getServer().getWorlds()) {
			for (Player player : world.getPlayers()) {
				playerListener.doPlayerBasedActions(
						ContribCraftPlayer.getContribPlayer(player),
						getConfiguration());
				playerListener.doWorldBasedActions(world,
						ContribCraftPlayer.getContribPlayer(player),
						getConfiguration());
			}
		}
	}

	/**
	 * @param location
	 *            The location with which to look for a containing region
	 * @return The configuration node where the region was defined, or null if
	 *         no region was applicable
	 */
	public static ConfigurationNode getRegion(Location location) {
		List<ConfigurationNode> regions = instance.getConfiguration()
				.getNodeList("regions." + location.getWorld().getName(), null);
		for (ConfigurationNode region : regions) {
			if (region.getInt("max.x", Integer.MIN_VALUE) >= location.getX()
					&& region.getInt("max.y", Integer.MIN_VALUE) >= location
							.getY()
					&& region.getInt("max.z", Integer.MIN_VALUE) >= location
							.getZ()
					&& region.getInt("min.x", Integer.MAX_VALUE) <= location
							.getX()
					&& region.getInt("min.y", Integer.MAX_VALUE) <= location
							.getY()
					&& region.getInt("min.z", Integer.MAX_VALUE) <= location
							.getZ())
				return region;
		}
		return null;
	}

	/**
	 * Download a file. Code is from
	 * {@link "http://forums.bukkit.org/threads/dev-bukkitcontrib-alpha-0-1-3-953.18192/page-36#post-457314"}
	 * 
	 * @param log
	 *            Logger to use for progress information
	 * @param url
	 *            URL to download
	 * @param file
	 *            File to write contents of URL to
	 * @throws IOException
	 *             If an error occurred during compilation
	 */
	public static void download(Logger log, URL url, File file)
		throws IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		final int size = url.openConnection().getContentLength();
		log.info("Downloading " + file.getName() + " (" + size / 1024
				+ "kb) ...");
		final InputStream in = url.openStream();
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(
				file));
		final byte[] buffer = new byte[1024];
		int len, downloaded = 0, msgs = 0;
		final long start = System.currentTimeMillis();
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
			downloaded += len;
			if ((int) ((System.currentTimeMillis() - start) / 500) > msgs) {
				log.info((int) ((double) downloaded / (double) size * 100d)
						+ "%");
				msgs++;
			}
		}
		in.close();
		out.close();
		log.info("Download finished");
	}
}
