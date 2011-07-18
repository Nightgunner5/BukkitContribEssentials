package net.llamaslayers.minecraft.bukkitcontribessentials;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Nightgunner5
 */
public class BCECommand implements CommandExecutor {
	/**
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender,
	 *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	public boolean onCommand(CommandSender sender, Command command,
		String label, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("You are not allowed to use this command.");
			return true;
		}

		if (args.length != 1)
			return false;
		if (args[0].equals("reload")) {
			Bukkit.getServer().getPluginManager()
					.disablePlugin(BukkitContribEssentials.instance);
			BukkitContribEssentials.instance.getConfiguration().load();
			Bukkit.getServer().getPluginManager()
					.enablePlugin(BukkitContribEssentials.instance);
			sender.sendMessage("BukkitContribEssentials restarted!");
		} else if (args[0].equals("version")) {
			sender.sendMessage("This server has the following installed:");
			sender.sendMessage(Bukkit.getServer().getName() + " "
					+ Bukkit.getServer().getVersion());
			sender.sendMessage(Bukkit.getServer().getPluginManager()
					.getPlugin("BukkitContrib").getDescription().getFullName());
			sender.sendMessage(Bukkit.getServer().getPluginManager()
					.getPlugin("BukkitContribEssentials").getDescription()
					.getFullName());
		} else
			return false;
		return true;
	}
}
