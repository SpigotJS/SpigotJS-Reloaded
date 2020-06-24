package io.github.spigotjs.utils;

import java.util.function.Consumer;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class ModuleBukkitCommand extends BukkitCommand {

	Consumer<ModuleCommand> consumer;
	
	public ModuleBukkitCommand(String name, Consumer<ModuleCommand> consumer) {
		super(name);
		this.consumer = consumer;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		ModuleCommand command = new ModuleCommand(sender, commandLabel, args);
		consumer.accept(command);
		return false;
	}

}
