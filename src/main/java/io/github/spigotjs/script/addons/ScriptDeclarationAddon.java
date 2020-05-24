package io.github.spigotjs.script.addons;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ScriptDeclarationAddon {

	private String source;
	private String className;
	private Class<?> targetClass;
	
}
