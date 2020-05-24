package io.github.spigotjs.script.addons;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class ScriptAddonManager {

	private List<ScriptDeclarationAddon> declarationAddons;
	private List<ScriptCodeAddon> codeAddons;
	
	public ScriptAddonManager() {
		this.declarationAddons = new ArrayList<ScriptDeclarationAddon>();
		this.codeAddons = new ArrayList<ScriptCodeAddon>();
	}
	
	public void registerDeclarationAddon(ScriptDeclarationAddon addon) {
		declarationAddons.add(addon);
	}
	
	public void registerCodeAddon(ScriptCodeAddon addon) {
		codeAddons.add(addon);
	}

}
