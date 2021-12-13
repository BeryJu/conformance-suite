package net.openid.conformance.util.field;


import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The base class describes features of value that can contain key
 */
public class Field {

	private boolean optional;
	private boolean nullable;
	private String path = "";
	private String pattern = "";
	private int maxLength;
	private int minLength;
	private int maxItems;
	private int minItems;
	private int maxValue;
	private int minimum = Integer.MIN_VALUE;
	private Set<String> enums = Collections.emptySet();
	protected Consumer<JsonObject> validator;

	public Field() {
	}

	public Field(String path) {
		this.path = path;
	}

	public Field(boolean optional, boolean nullable, String path) {
		this.optional = optional;
		this.nullable = nullable;
		this.path = path;
	}

	public Field(boolean optional, boolean nullable, String path, Consumer<JsonObject> validator) {
		this.optional = optional;
		this.nullable = nullable;
		this.path = path;
		this.validator = validator;
	}

	protected Field(boolean optional, boolean nullable, String path, String pattern, int maxLength, int minLength, int maxItems, int minItems, int maxValue, Set<String> enums) {
		this.optional = optional;
		this.nullable = nullable;
		this.path = path;
		this.pattern = pattern;
		this.maxLength = maxLength;
		this.minLength = minLength;
		this.maxItems = maxItems;
		this.minItems = minItems;
		this.maxValue = maxValue;
		this.enums = enums;
	}

	protected Field(boolean optional, boolean nullable, String path, String pattern,
					int maxLength, int minLength, int maxItems, int minItems, int maxValue,
					Set<String> enums, int minimum) {
		this.optional = optional;
		this.nullable = nullable;
		this.path = path;
		this.pattern = pattern;
		this.maxLength = maxLength;
		this.minLength = minLength;
		this.maxItems = maxItems;
		this.minItems = minItems;
		this.maxValue = maxValue;
		this.enums = enums;
		this.minimum = minimum;
	}

	public Field(boolean optional, boolean nullable, String path, int maxItems, int minItems, Consumer<JsonObject> validator) {
		this.optional = optional;
		this.nullable = nullable;
		this.path = path;
		this.maxItems = maxItems;
		this.minItems = minItems;
		this.validator = validator;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isNullable() { return nullable; }

	public void setNullable(boolean nullable) { this.nullable = nullable; }

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public int getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}

	public int getMinItems() {
		return minItems;
	}

	public void setMinItems(int minItems) {
		this.minItems = minItems;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public Set<String> getEnums() {
		return enums;
	}

	public void setEnums(Set<String> enums) {
		this.enums = enums;
	}

	public Consumer<JsonObject> getValidator() { return this.validator; }

	public int getMinimum() {
		return this.minimum;
	}

	public abstract static class FieldBuilder {
		protected boolean optional;
		protected boolean nullable;
		protected String path = "";
		protected String pattern = "";
		protected int maxLength;
		protected int minLength;
		protected int maxItems;
		protected int minItems;
		protected int maxValue;
		protected  int minimum;
		protected Set<String> enums = Collections.emptySet();
		protected Consumer<JsonObject> validator;

		public FieldBuilder(String path) {
			this.path = path;
		}

		public FieldBuilder setOptional() {
			this.optional = true;
			return this;
		}

		public FieldBuilder setNullable() {
			this.nullable = true;
			return this;
		}

		public FieldBuilder setPath(String path) {
			this.path = path;
			return this;
		}

		public FieldBuilder setPattern(String pattern) {
			this.pattern = pattern;
			return this;
		}

		public FieldBuilder setMaxLength(int maxLength) {
			this.maxLength = maxLength;
			return this;
		}

		public FieldBuilder setMinLength(int minLength) {
			this.minLength = minLength;
			return this;
		}

		public FieldBuilder setMaxItems(int maxItems) {
			this.maxItems = maxItems;
			return this;
		}

		public FieldBuilder setMinItems(int minItems) {
			this.minItems = minItems;
			return this;
		}

		public FieldBuilder setMaxValue(int maxValue) {
			this.maxValue = maxValue;
			return this;
		}

		public FieldBuilder setEnums(Set<String> enums) {
			this.enums = enums;
			return this;
		}

		public FieldBuilder setValidator(Consumer<JsonObject> validator) {
			this.validator = validator;
			return this;
		}

		public FieldBuilder setMinimum(int minimum) {
			this.minimum = minimum;
			return this;
		}

		public abstract Field build();
	}
}
