package io.github.spigotjs.managers;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.github.spigotjs.SpigotJSReloaded;

public class TaskManager {
	
	public BukkitTask runLater(Runnable run, int ticks) {
		return new BukkitRunnable() {
			
			@Override
			public void run() {
				run.run();
				
			}
		}.runTaskLater(SpigotJSReloaded.getInstance(), ticks);
	}
	
	public void cancelTask(BukkitTask task) {
		task.cancel();
	}
	
	public BukkitTask runTimer(Runnable run, int ticks) {
		return new BukkitRunnable() {
			
			@Override
			public void run() {
				run.run();
				
			}
		}.runTaskTimer(SpigotJSReloaded.getInstance(), 0, ticks);
	}

}
