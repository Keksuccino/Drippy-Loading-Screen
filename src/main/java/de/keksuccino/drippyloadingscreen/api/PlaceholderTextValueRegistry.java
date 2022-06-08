package de.keksuccino.drippyloadingscreen.api;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PlaceholderTextValueRegistry {
	
	protected Map<String, PlaceholderValue> values = new TreeMap<String, PlaceholderValue>();
	protected List<String> categories = new ArrayList<String>();
	
	private static PlaceholderTextValueRegistry instance;
	
	public void registerValue(String valueKey, String valueDisplayName, @Nullable String valueCategory, IPlaceholderValueContent valueContent) {
		values.put(valueKey, new PlaceholderValue(valueKey, valueDisplayName, valueCategory, valueContent));
		if (!categories.contains(valueCategory)) {
			categories.add(valueCategory);
		}
	}
	
	public Map<String, PlaceholderValue> getValues() {
		return values;
	}
	
	public List<PlaceholderValue> getValuesAsList() {
		List<PlaceholderValue> l = new ArrayList<PlaceholderValue>();
		l.addAll(values.values());
		return l;
	}
	
	public PlaceholderValue getValue(String valueKey) {
		return values.get(valueKey);
	}
	
	public List<String> getCategories() {
		return categories;
	}
	
	public List<PlaceholderValue> getValuesForCategory(String category) {
		List<PlaceholderValue> l = new ArrayList<PlaceholderValue>();
		for (PlaceholderValue v : getValuesAsList()) {
			if (v.valueCategory.equals(category)) {
				l.add(v);
			}
		}
		return l;
	}
	
	public interface IPlaceholderValueContent {
		
		String getContent(PlaceholderValue value);
		
	}
	
	public static class PlaceholderValue {
		
		public final String valueKey;
		public final String valueDisplayName;
		public final String valueCategory;
		public final IPlaceholderValueContent valueContent;
		
		public PlaceholderValue(String valueKey, String valueDisplayName, String valueCategory, IPlaceholderValueContent valueContent) {
			this.valueKey = valueKey;
			this.valueDisplayName = valueDisplayName;
			this.valueContent = valueContent;
			this.valueCategory = valueCategory;
		}
		
		public String get() {
			return valueContent.getContent(this);
		}
		
		public String getPlaceholder() {
			return "%" + this.valueKey + "%";
		}
		
	}
	
	public static PlaceholderTextValueRegistry getInstance() {
		if (instance == null) {
			instance = new PlaceholderTextValueRegistry();
		}
		return instance;
	}

}
