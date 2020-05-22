package io.github.spigotjs.script;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScriptResource {

	private String name;
	private File mainFile;
	private String author;
	private String version;

}
