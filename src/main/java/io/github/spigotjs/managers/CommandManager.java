package io.github.spigotjs.managers;

import java.util.function.Consumer;


import io.github.spigotjs.utils.ScriptBukkitCommand;
import io.github.spigotjs.utils.ScriptCommand;

public class CommandManager {
	
	public void on(String name, Consumer<ScriptCommand> consumer) {
		ScriptBukkitCommand command = new ScriptBukkitCommand(name, consumer);
		//SpigotJS.getInstance().getScriptBukkitCommands().add(command);
		//SpigotJS.getInstance().getCommandMap().register(name, command);
	}

}
