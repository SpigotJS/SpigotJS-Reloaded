package io.github.spigotjs.utils;

import org.bukkit.command.CommandSender;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ScriptCommand {

	private CommandSender sender;
	private String label;
	private String[] args;
	
}
