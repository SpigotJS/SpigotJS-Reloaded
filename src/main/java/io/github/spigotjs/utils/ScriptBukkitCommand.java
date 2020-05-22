package io.github.spigotjs.utils;

import java.util.function.Consumer;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class ScriptBukkitCommand extends BukkitCommand {

	Consumer<ScriptCommand> consumer;
	
	public ScriptBukkitCommand(String name, Consumer<ScriptCommand> consumer) {
		super(name);
		this.consumer = consumer;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		ScriptCommand command = new ScriptCommand(sender, commandLabel, args);
		consumer.accept(command);
		return false;
	}

}
