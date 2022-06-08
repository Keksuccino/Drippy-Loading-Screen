package de.keksuccino.drippyloadingscreen.customization.placeholdervalues;

import java.io.File;
import java.util.*;
import com.mojang.blaze3d.platform.GlUtil;
import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry;
import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry.PlaceholderValue;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.file.FileUtils;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

public class PlaceholderTextValueHelper {

	public static String currentLoadingProgressValue = "0";
	public static Map<String, RandomTextPackage> randomTextIntervals = new HashMap<>();
	private static final File MOD_DIRECTORY = new File("mods");
	private static int cachedTotalMods = -10;

	public static String convertFromRaw(String in) {
		try {

			Minecraft mc = Minecraft.getInstance();

			if (mc == null) {
				return in;
			}

			//Convert &-formatcodes to real ones
			in = StringUtils.convertFormatCodes(in, "&", "§");

			//Only for internal use
			in = in.replace("%guiwidth%", "" + Minecraft.getInstance().getWindow().getGuiScaledWidth());
			in = in.replace("%guiheight%", "" + Minecraft.getInstance().getWindow().getGuiScaledHeight());
			//-------------

			//Replace player name and uuid placeholders
			in = in.replace("%playername%", mc.getUser().getName());
			in = in.replace("%playeruuid%", mc.getUser().getUuid());

			//Replace mc version placeholder
			in = in.replace("%mcversion%", SharedConstants.getCurrentVersion().getReleaseTarget());

			//Replace mod version placeholder
			in = replaceModVersionPlaceholder(in);

			//Replace loaded mods placeholder
			in = in.replace("%loadedmods%", "" + getLoadedMods());

			//Replace total mods placeholder
			in = in.replace("%totalmods%", "" + getTotalMods());

			if (in.contains("%realtime")) {

				Calendar c = Calendar.getInstance();

				in = in.replace("%realtimeyear%", "" + c.get(Calendar.YEAR));

				in = in.replace("%realtimemonth%", formatToFancyDateTime(c.get(Calendar.MONTH) + 1));

				in = in.replace("%realtimeday%", formatToFancyDateTime(c.get(Calendar.DAY_OF_MONTH)));

				in = in.replace("%realtimehour%", formatToFancyDateTime(c.get(Calendar.HOUR_OF_DAY)));

				in = in.replace("%realtimeminute%", formatToFancyDateTime(c.get(Calendar.MINUTE)));

				in = in.replace("%realtimesecond%", formatToFancyDateTime(c.get(Calendar.SECOND)));

			}

			if (in.contains("%fps%")) {
				in = in.replace("%fps%", mc.fpsString.split("[ ]", 2)[0]);
			}

			if (in.contains("ram%")) {
				long i = Runtime.getRuntime().maxMemory();
				long j = Runtime.getRuntime().totalMemory();
				long k = Runtime.getRuntime().freeMemory();
				long l = j - k;

				in = in.replace("%percentram%", (l * 100L / i) + "%");

				in = in.replace("%usedram%", "" + bytesToMb(l));

				in = in.replace("%maxram%", "" + bytesToMb(i));
			}

			in = in.replace("%loadingprogress%", currentLoadingProgressValue);

			in = in.replace("%cpuinfo%", GlUtil.getCpuInfo());

			in = in.replace("%gpuinfo%", GlUtil.getRenderer());

			String javaVersion = System.getProperty("java.version");
			if (javaVersion == null) {
				javaVersion = "0";
			}
			in = in.replace("%javaversion%", javaVersion);

			String osName = System.getProperty("os.name");
			if (osName == null) {
				osName = "unknown";
			}
			in = in.replace("%osname%", osName);

			in = in.replace("%openglversion%", GlUtil.getOpenGLVersion());

			in = replaceRandomTextValue(in);

			//Apply all custom values
			for (PlaceholderValue v : PlaceholderTextValueRegistry.getInstance().getValuesAsList()) {
				in = in.replace(v.getPlaceholder(), v.get());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return in;
	}

	public static boolean containsPlaceholderValues(String in) {
		String s = convertFromRaw(in);
		return !s.equals(in);
	}

	private static String replaceModVersionPlaceholder(String in) {
		try {
			for (String s : getReplaceablesWithValue(in, "%version:")) {
				if (s.contains(":")) {
					String blank = s.substring(1, s.length()-1);
					String mod = blank.split(":", 2)[1];
					if (FabricLoader.getInstance().isModLoaded(mod)) {
						Optional<ModContainer> o = FabricLoader.getInstance().getModContainer(mod);
						if (o.isPresent()) {
							ModContainer c = o.get();
							String version = c.getMetadata().getVersion().getFriendlyString();
							in = in.replace(s, version);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}

	private static String replaceRandomTextValue(String in) {
		try {
			for (String s : getReplaceablesWithValue(in, "%randomtext:")) { // %randomtext:<filepath>:<change_interval_sec>%
				if (s.contains(":")) {
					String blank = s.substring(1, s.length()-1);
					String value = blank.split(":", 2)[1];
					if (value.contains(":")) {
						String pathString = value.split(":", 2)[0];
						File path = new File(pathString);
						String intervalString = value.split(":", 2)[1];
						if (MathUtils.isLong(intervalString) && path.isFile() && path.getPath().toLowerCase().endsWith(".txt")) {
							long interval = Long.parseLong(intervalString) * 1000;
							if (interval < 0L) {
								interval = 0L;
							}
							long currentTime = System.currentTimeMillis();
							RandomTextPackage p;
							if (randomTextIntervals.containsKey(path.getPath())) {
								p = randomTextIntervals.get(path.getPath());
							} else {
								p = new RandomTextPackage();
								randomTextIntervals.put(path.getPath(), p);
							}
							if ((interval > 0) || (p.currentText == null)) {
								if ((p.lastChange + interval) <= currentTime) {
									p.lastChange = currentTime;
									List<String> txtLines = FileUtils.getFileLines(path);
									if (!txtLines.isEmpty()) {
										p.currentText = txtLines.get(MathUtils.getRandomNumberInRange(0, txtLines.size()-1));
									} else {
										p.currentText = null;
									}
								}
							}
							if (p.currentText != null) {
								in = in.replace(s, p.currentText);
							} else {
								in = in.replace(s, "");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}

	protected static List<String> getReplaceablesWithValue(String in, String placeholderBase) {
		List<String> l = new ArrayList<String>();
		try {
			if (in.contains(placeholderBase)) {
				int index = -1;
				int i = 0;
				while (i < in.length()) {
					String s = "" + in.charAt(i);
					if (s.equals("%")) {
						if (index == -1) {
							index = i;
						} else {
							String sub = in.substring(index, i+1);
							if (sub.startsWith(placeholderBase) && sub.endsWith("%")) {
								l.add(sub);
							}
							index = -1;
						}
					}
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}

	private static int getTotalMods() {
		if (cachedTotalMods == -10) {
			if (MOD_DIRECTORY.exists()) {
				int i = 0;
				for (File f : MOD_DIRECTORY.listFiles()) {
					if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
						i++;
					}
				}
				cachedTotalMods = i+2;
			} else {
				cachedTotalMods = -1;
			}
		}
		return cachedTotalMods;
	}

	private static int getLoadedMods() {
		try {
			int i = 0;
			if (Konkrete.isOptifineLoaded) {
				i++;
			}
			return FabricLoader.getInstance().getAllMods().size() + i;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private static String formatToFancyDateTime(int in) {
		String s = "" + in;
		if (s.length() < 2) {
			s = "0" + s;
		}
		return s;
	}

	private static long bytesToMb(long bytes) {
		return bytes / 1024L / 1024L;
	}

	public static class RandomTextPackage {
		public String currentText = null;
		public long lastChange = 0L;
	}

}
