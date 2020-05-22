package io.github.spigotjs.utils;

import java.util.Arrays;
import java.util.List;

public class Utils {
	
	public List<Object> asList(Object[] array) {
		return Arrays.asList(array);
	}
	
	public boolean includes(Object[] array, Object data) {
		return asList(array).contains(data);
	}

}
