package net.llamaslayers.minecraft.bukkitcontribessentials;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkitcontrib.BukkitContrib;
import org.bukkitcontrib.player.ContribCraftPlayer;
import org.bukkitcontrib.sound.Music;

public class BCEMusicCommand implements CommandExecutor {
	private static final String trackList;
	static {
		StringBuilder sb = new StringBuilder();
		for (Music track : Music.values()) {
			if (track != Music.CUSTOM) {
				if (sb.length() != 0) {
					sb.append(',');
				}
				String[] pieces = track.name().split("_");
				for (String piece : pieces) {
					sb.append(' ').append(piece.charAt(0))
							.append(piece.substring(1).toLowerCase());
				}
			}
		}
		trackList = sb.toString();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Available tracks:" + trackList);
			return true;
		}
		boolean global = false;
		int i = 0;
		if (args[i].equalsIgnoreCase("-global")) {
			if (!sender.isOp()) {
				sender.sendMessage("You are not allowed to play global music.");
				return true;
			}
			global = true;
			i++;
		}
		if (args[i].equalsIgnoreCase("-custom")) {
			i++;
			if (i >= args.length)
				return false;
			try {
				if (global) {
					BukkitContrib.getSoundManager().playGlobalCustomMusic(
							BukkitContribEssentials.instance, args[i], true);
					return true;
				} else if (sender instanceof Player) {
					BukkitContrib.getSoundManager().playCustomMusic(
							BukkitContribEssentials.instance,
							ContribCraftPlayer
									.getContribPlayer((Player) sender),
							args[i], true);
					return true;
				}
			} catch (UnsupportedOperationException ex) {
				sender.sendMessage(ex.getMessage());
				return true;
			}
		}
		StringBuilder sb = new StringBuilder();
		for (; i < args.length; i++) {
			if (sb.length() != 0) {
				sb.append('_');
			}
			sb.append(args[i].toUpperCase());
		}
		Music track;
		try {
			track = Music.valueOf(sb.toString());
		} catch (IllegalArgumentException ex) {
			sender.sendMessage("Unknown track " + sb.toString());
			return false;
		}
		if (global) {
			BukkitContrib.getSoundManager().playGlobalMusic(track, 300);
			return true;
		} else if (sender instanceof Player) {
			BukkitContrib.getSoundManager().playMusic(
					ContribCraftPlayer.getContribPlayer((Player) sender),
					track, 300);
			return true;
		}
		return false;
	}
}
