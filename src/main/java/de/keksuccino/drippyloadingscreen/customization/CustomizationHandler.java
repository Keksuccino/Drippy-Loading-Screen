package de.keksuccino.drippyloadingscreen.customization;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.google.common.io.Files;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.PreloadedLayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.drippyloadingscreen.events.CustomizationSystemReloadedEvent;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSerializer;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.MinecraftClient;

public class CustomizationHandler {
	
	private static boolean initDone = false;
	private static List<String> sounds = new ArrayList<String>();
	
	private static boolean isLightmode = false;
	private static boolean lightModeCached = false;
	
	public static void init() {
		if (!initDone) {

			Konkrete.getEventHandler().registerEventsFrom(new CustomizationHandlerEvents());
			
			CustomizationPropertiesHandler.loadProperties();
			
			initDone = true;
			
		}
	}

	public static void reloadSystem() {
		
		CustomizationPropertiesHandler.loadProperties();
		SplashCustomizationLayer.getInstance().updateCustomizations();
		
		Konkrete.getEventHandler().callEventsFor(new CustomizationSystemReloadedEvent());
		
	}
	
	public static void registerSound(String key, String path) {
		if (!sounds.contains(key)) {
			sounds.add(key);
		}
		SoundHandler.registerSound(key, path);
	}
	
	public static void unregisterSound(String key) {
		if (sounds.contains(key)) {
			sounds.remove(key);
		}
		SoundHandler.unregisterSound(key);
	}
	
	public static void stopSounds() {
		for (String s : sounds) {
			SoundHandler.stopSound(s);
		}
	}
	
	public static void resetSounds() {
		for (String s : sounds) {
			SoundHandler.resetSound(s);
		}
	}

	public static boolean isSoundRegistered(String key) {
		return sounds.contains(key);
	}

	public static List<String> getSounds() {
		return sounds;
	}

	public static String generateRandomActionId() {
		long ms = System.currentTimeMillis();
		String s = UUID.randomUUID().toString();
		return s + ms;
	}
	
	public static void openFile(File f) {
		try {
			String url = f.toURI().toURL().toString();
			String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);
			URL u = new URL(url);
			if (!MinecraftClient.IS_SYSTEM_MAC) {
				if (s.contains("win")) {
					Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
				} else {
					if (u.getProtocol().equals("file")) {
						url = url.replace("file:", "file://");
					}
					Runtime.getRuntime().exec(new String[]{"xdg-open", url});
				}
			} else {
				Runtime.getRuntime().exec(new String[]{"open", url});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void editLayout(File layout) {
		
		try {
			
			if ((layout != null) && (layout.exists()) && (layout.isFile())) {
				
				PropertiesSet set = PropertiesSerializer.getProperties(layout.getPath());
				
				List<PropertiesSection> meta = set.getPropertiesOfType("customization-meta");
				if (meta.isEmpty()) {
					meta = set.getPropertiesOfType("type-meta");
				}
				
				if (!meta.isEmpty()) {
					
					meta.get(0).addEntry("path", layout.getPath());
					
					LayoutEditorScreen.isActive = true;
					MinecraftClient.getInstance().setScreen(new PreloadedLayoutEditorScreen(set));
					stopSounds();
					resetSounds();
					
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Will save the layout as layout file.
	 * 
	 * @param to Full file path with file name + extension.
	 */
	public static boolean saveLayoutTo(PropertiesSet layout, String to) {
		
		File f = new File(to);
		String s = Files.getFileExtension(to);
		if ((s != null) && !s.equals("")) {
			
			if (f.exists() && f.isFile()) {
				f.delete();
			}
			
			PropertiesSerializer.writeProperties(layout, f.getPath());
			
			return true;
			
		}
		
		return false;
		
	}
	
	/**
	 * Will save the layout as layout file.
	 * 
	 * @param to Full file path with file name + extension.
	 */
	public static boolean saveLayoutTo(List<PropertiesSection> layout, String to) {
		
		PropertiesSet props = new PropertiesSet("hud");
		for (PropertiesSection sec : layout) {
			props.addProperties(sec);
		}
		
		return saveLayoutTo(props, to);
		
	}
	
	public static void setLightmode(boolean lightmode) {
		try {
			File f = new File(DrippyLoadingScreen.HOME_DIR.getPath() + "/lightmode.enabled");
			if (lightmode && !isLightmode) {
				if (!f.exists()) {
					if (!DrippyLoadingScreen.HOME_DIR.exists()) {
						DrippyLoadingScreen.HOME_DIR.mkdirs();
					}
					f.createNewFile();
				}
			}
			if (!lightmode) {
				if (f.exists()) {
					f.delete();
				}
			}
			isLightmode = lightmode;
			lightModeCached = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isLightModeEnabled() {
		if (!lightModeCached) {
			setLightmode(lightModeFileExists());
		}
		return isLightmode;
	}
	
	private static boolean lightModeFileExists() {
		try {
			File f = new File(DrippyLoadingScreen.HOME_DIR.getPath() + "/lightmode.enabled");
			return f.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
