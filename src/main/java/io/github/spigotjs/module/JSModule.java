package io.github.spigotjs.module;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JSModule {

	private String name;
	private String folderName;
	private File mainFile;
	private String author;
	private String version;

}
