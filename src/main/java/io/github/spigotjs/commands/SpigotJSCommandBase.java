package io.github.spigotjs.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.script.addons.ScriptCodeAddon;
import io.github.spigotjs.script.addons.ScriptDeclarationAddon;

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
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§7/" + label + " resources - Shows all resources");
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§7/" + label + " addons - Shows all addons");
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§7/" + label + " reload - Reloads all scripts");
				return false;
			}
			if(subCommand.equalsIgnoreCase("resources")) {
				Map<String, JSONObject> modules = SpigotJSReloaded.getInstance().getScriptManager().getModules();
				if (modules.size() == 0) {
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§7§oThere are no resources.");
					return false;
				}
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§3There are §e" + modules.size() + " §3resources:");
				Set<String> iteset = modules.keySet();
				for(String moduleName : iteset) {
					JSONObject module = modules.get(moduleName);
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§b" + module.get("name") + " v" + module.get("version") + " by " + module.get("author"));
				}
				return false;
			}
			if(subCommand.equalsIgnoreCase("addons")) {
				List<ScriptCodeAddon> codeAddons = SpigotJSReloaded.getInstance().getScriptAddonManager().getCodeAddons(); 
				List<ScriptDeclarationAddon> declareAddons = SpigotJSReloaded.getInstance().getScriptAddonManager().getDeclarationAddons();
				if(codeAddons.size() == 0) {
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§7§oThere are no code-addons");
				} else {
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§3There are §e" + codeAddons.size() + " §ecode-addons");
				}
				if(declareAddons.size() == 0) {
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§7§oThere are no declaration-addons");
				} else {
					sender.sendMessage(SpigotJSReloaded.PREFIX + "§3There are §e" + declareAddons.size() + " §edeclaration-addons.");
				}
				return false;
			}
			if(subCommand.equalsIgnoreCase("reload")) {
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§c§lNote: §cCommand reload should work now. Only testet in 1.8");
				sender.sendMessage(SpigotJSReloaded.PREFIX + "§cReloading is not supported. Only use it for development.");
				SpigotJSReloaded.getInstance().getScriptManager().loadScripts();
				return false;
			}
		}
		sender.sendMessage(SpigotJSReloaded.PREFIX + "§cInvalid usage. Type '/" + label + " help' for a list of valid commands.");
		return false;
	}
}
