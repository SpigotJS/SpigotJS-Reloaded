package io.github.spigotjs.managers;

import java.util.function.Consumer;

import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.utils.ScriptBukkitCommand;
import io.github.spigotjs.utils.ScriptCommand;

public class CommandManager {
	public void on(String name, String prefix, Consumer<ScriptCommand> consumer) {
		ScriptBukkitCommand command = new ScriptBukkitCommand(name, consumer);
		SpigotJSReloaded.getInstance().getScriptManager().getScriptBukkitCommands().add(command);
		SpigotJSReloaded.getInstance().getScriptManager().getCommandMap().register(name, prefix, command);
	}
	public void on(String name, Consumer<ScriptCommand> consumer) {
		on(name, null, consumer);
	}
}
