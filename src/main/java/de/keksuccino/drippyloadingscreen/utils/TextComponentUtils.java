package de.keksuccino.drippyloadingscreen.utils;

import java.util.stream.Stream;

import com.google.common.collect.Streams;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class TextComponentUtils {

	public static String getFormattedStringFromComponent(ITextComponent component) {
		StringBuilder stringbuilder = new StringBuilder();
		String s = "";

		String s1 = component.getString();
		if (!s1.isEmpty()) {
			String s2 = getFormattingCodesFromStyle(component.getStyle());
			if (!s2.equals(s)) {
				if (!s.isEmpty()) {
					stringbuilder.append((Object)TextFormatting.RESET);
				}

				stringbuilder.append(s2);
				s = s2;
			}

			stringbuilder.append(s1);
		}

		for (ITextComponent c : component.getSiblings()) {
			String s3 = c.getString();
			if (!s3.isEmpty()) {
				String s2 = getFormattingCodesFromStyle(c.getStyle());
				if (!s2.equals(s)) {
					if (!s.isEmpty()) {
						stringbuilder.append((Object)TextFormatting.RESET);
					}

					stringbuilder.append(s2);
					s = s2;
				}

				stringbuilder.append(s3);
			}
		}

		if (!s.isEmpty()) {
			stringbuilder.append((Object)TextFormatting.RESET);
		}

		return stringbuilder.toString();
	}

	public static Stream<ITextComponent> getStreamOfComponent(ITextComponent component) {
		return Streams.concat(Stream.of(component));
	}

	public static String getFormattingCodesFromStyle(Style style) {
		String s = "";

		Color c = style.getColor();
		if (c != null) {
			if (c.getColor() == TextFormatting.AQUA.getColor()) {
				s += "§b";
			}
			if (c.getColor() == TextFormatting.BLACK.getColor()) {
				s += "§0";
			}
			if (c.getColor() == TextFormatting.BLUE.getColor()) {
				s += "§9";
			}
			if (c.getColor() == TextFormatting.DARK_AQUA.getColor()) {
				s += "§3";
			}
			if (c.getColor() == TextFormatting.DARK_BLUE.getColor()) {
				s += "§1";
			}
			if (c.getColor() == TextFormatting.DARK_GRAY.getColor()) {
				s += "§8";
			}
			if (c.getColor() == TextFormatting.DARK_GREEN.getColor()) {
				s += "§2";
			}
			if (c.getColor() == TextFormatting.DARK_PURPLE.getColor()) {
				s += "§5";
			}
			if (c.getColor() == TextFormatting.DARK_RED.getColor()) {
				s += "§4";
			}
			if (c.getColor() == TextFormatting.GOLD.getColor()) {
				s += "§6";
			}
			if (c.getColor() == TextFormatting.GRAY.getColor()) {
				s += "§7";
			}
			if (c.getColor() == TextFormatting.GREEN.getColor()) {
				s += "§a";
			}
			if (c.getColor() == TextFormatting.LIGHT_PURPLE.getColor()) {
				s += "§d";
			}
			if (c.getColor() == TextFormatting.RED.getColor()) {
				s += "§c";
			}
			if (c.getColor() == TextFormatting.WHITE.getColor()) {
				s += "§f";
			}
			if (c.getColor() == TextFormatting.YELLOW.getColor()) {
				s += "§e";
			}
		}

		if (style.getBold()) {
			s += "§l";
		}
		if (style.getItalic()) {
			s += "§o";
		}
		if (style.getObfuscated()) {
			s += "§k";
		}
		if (style.getStrikethrough()) {
			s += "§m";
		}
		if (style.getUnderlined()) {
			s += "§n";
		}

		return s;
	}

	public static String toHexString(int red, int green, int blue) {
		String hex = String. format("#%02X%02X%02X", red, green, blue);
		return hex;
	}
	
	public static StringTextComponent getWithGradient(java.awt.Color start, String text, java.awt.Color end) {

		StringTextComponent sc = new StringTextComponent("");

		int length = text.length();
		int i = 0;
		for (char c : text.toCharArray()) {

			int red = (int)(start.getRed() + ((float)(end.getRed() - start.getRed())) / (length - 1) * i);
			int green = (int)(start.getGreen() + ((float)(end.getGreen() - start.getGreen())) / (length - 1) * i);
			int blue = (int)(start.getBlue() + ((float)(end.getBlue() - start.getBlue())) / (length - 1) * i);

			StringTextComponent charComp = new StringTextComponent("" + c);

			Style s = charComp.getStyle();
			Color textColor = Color.fromHex(toHexString(red, green, blue));
			Style colored = s.setColor(textColor);

			charComp.setStyle(colored);

			sc.appendSibling(charComp);
			
			i++;

		}

		return sc;

	}

}
