package io.github.spigotjs.script.addons;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * A code-addon adds the declared code before a script gets executed.
 * 
 * @author elias
 *
 */
@Getter @AllArgsConstructor
public class ScriptCodeAddon {

	private String source;
	private String code;
}
