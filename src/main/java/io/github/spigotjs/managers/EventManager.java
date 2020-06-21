package io.github.spigotjs.managers;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import com.google.common.reflect.ClassPath;

import io.github.spigotjs.SpigotJSReloaded;
import lombok.Getter;

@Getter
public class EventManager implements Listener {

	private Plugin plugin;
	
	@SuppressWarnings("rawtypes")
	private HashMap<String, Class> eventNames;
	private String[] eventPackages;
	
	@SuppressWarnings("rawtypes")
	public EventManager(Plugin plugin) {
		this.plugin = plugin;
		this.eventPackages = new String[] {
			"org.bukkit.event.block.",
			"org.bukkit.event.enchantment.",
			"org.bukkit.event.entity.",
			"org.bukkit.event.hanging.",
			"org.bukkit.event.inventory.",
			"org.bukkit.event.painting.",
			"org.bukkit.event.player.",
			"org.bukkit.event.server.",
			"org.bukkit.event.vehicle.",
			"org.bukkit.event.weather.",
			"org.bukkit.event.world.",
		};
		this.eventNames = new HashMap<String, Class>();
		try {
			ClassPath path = ClassPath.from(Thread.currentThread().getContextClassLoader());
			for(ClassPath.ClassInfo info : path.getTopLevelClasses()) {
				for(String rawPackage : eventPackages) {
					if(info.getName().startsWith(rawPackage)) {
						Class<?> clazz = info.load();
						if(info.getSimpleName().toLowerCase().contains("event")) {
							String name = info.getSimpleName().toLowerCase().replaceAll("event", "");
							eventNames.put(name, clazz);
						
						}
	
					}

				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void on(String eventName, Consumer<Event> consumer) {
		on(eventNames.get(eventName.toLowerCase()), consumer);
	}
	
	@SuppressWarnings("unchecked")
	public void on(String eventName, Consumer<Event> consumer, EventPriority eventPriority) {
		on(eventNames.get(eventName.toLowerCase()), consumer, eventPriority);
	}
	
	public void on(Class<? extends Event> eventClass, Consumer<Event> consumer, EventPriority eventPriority) {
		EventExecutor executor = new EventExecutor() {
			
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				consumer.accept(event);
				
			}
		};
		Bukkit.getPluginManager().registerEvent(eventClass, this, eventPriority, executor, plugin);
		
	}
	
	public void on(Class<? extends Event> eventClass, Consumer<Event> consumer) {
		on(eventClass, consumer, EventPriority.NORMAL);
	}
	
}
