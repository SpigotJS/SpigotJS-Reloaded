package io.github.spigotjs.compiler;

import java.io.IOException;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class ClassDataFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
	
	private final Map<String, byte[]> classData;
	
	/**
	 * Create a new file manager that delegates to the given file manager
	 * 
	 * @param standardJavaFileManager The delegate file manager
	 */
	public ClassDataFileManager(StandardJavaFileManager standardJavaFileManager, Map<String, byte[]> classData) {
		super(standardJavaFileManager);
		this.classData = classData;
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className,
			javax.tools.JavaFileObject.Kind kind, FileObject sibling) throws IOException {
		// TODO Auto-generated method stub
		return new MemoryJavaClassFileObject(className, classData);
	}
}
