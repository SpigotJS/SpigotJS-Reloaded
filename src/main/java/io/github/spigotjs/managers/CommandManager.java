package io.github.spigotjs.managers;

import java.util.function.Consumer;

import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.utils.ModuleBukkitCommand;
import io.github.spigotjs.utils.ModuleCommand;

public class CommandManager {
	public void on(String name, String prefix, Consumer<ModuleCommand> consumer) {
		if (prefix == null) { prefix = name; }
		ModuleBukkitCommand command = new ModuleBukkitCommand(name, consumer);
		SpigotJSReloaded.getInstance().getScriptManager().getScriptBukkitCommands().add(command);
		SpigotJSReloaded.getInstance().getScriptManager().getCommandMap().register(name, prefix, command);
	}
	
	public void on(String name, Consumer<ModuleCommand> consumer) {
		on(name, null, consumer);
	}
}
