package io.github.spigotjs.commands;

import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import io.github.spigotjs.SpigotJSReloaded;

public class SpigotJSCommandBase implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("spigotjs.admin")) {
			sender.sendMessage(SpigotJSReloaded.PREFIX + "§cIm sorry, but you dont have permissions for that.");
			return false;
		}
		if(args.length == 0) {
			sender.sendMessage(SpigotJSReloaded.PREFIX + "§3SpigotJS-Reloaded version " + SpigotJSReloaded.getInstance().getDescription().getVersion());
			return false;
		}
		if(args.length == 1) {
			String subCommand = args[0];
			if(subCommand.equalsIgnoreCase("help")) {
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§7/" + label + " help - Displays this message");
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§7/" + label + " modules - Shows all modules");
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§7/" + label + " reload - Reloads all scripts");
				return false;
			}
			if(subCommand.equalsIgnoreCase("modules")) {
				Map<String, JSONObject> modules = SpigotJSReloaded.getInstance().getScriptManager().getModules();
				if (modules.size() == 0) {
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§7§oThere are no modules.");
					return false;
				}
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§3There are §e" + modules.size() + " §3modules:");
				Set<String> iteset = modules.keySet();
				for(String moduleName : iteset) {
					JSONObject module = modules.get(moduleName);
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§b" + module.get("name") + " v" + module.get("version") + " by " + module.get("author"));
				}
				return false;
			}
			if(subCommand.equalsIgnoreCase("reload")) {
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§cReloading is not supported. Only use it for development.");
				SpigotJSReloaded.getInstance().getScriptManager().loadScripts();
				return false;
			}
		}
		sender.sendMessage(SpigotJSReloaded.PREFIX + "§cInvalid usage. Type '/" + label + " help' for a list of valid commands.");
		return false;
	}
}
