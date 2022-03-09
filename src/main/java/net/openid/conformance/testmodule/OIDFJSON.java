package net.openid.conformance.testmodule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Wrappers around the GSON getAsXXXX methods
 *
 * These should generally not be called directly from TestConditions (although there are a lot of historical uses)
 * as the errors they end up providing to the user are at best unhelpful - instead use AbstractCondition's
 * getStringFromEnvironment and similar.
 *
 * The 'getAs' methods automatically coerce types, for example if 'getAsNumber' finds a string, it will automatically
 * convert it to a number. This is not desirable behaviour when we're trying to write a conformance suite that
 * checks if the returned values are actually the correct type (for example it's pretty wrong to return 'expires_in'
 * as a string, it should always be a number.
 *
 * The 'getAs' methods should never be directly used in our code, these wrappers should always be used.
 *
 * 'get' (or 'getAs') methods in this class must NEVER do any type conversion; if type conversion is necessary call
 * the method 'forceConversionTo...'
 *
 * See https://gitlab.com/openid/conformance-suite/issues/398
 */
public final class OIDFJSON {

	public static Number getNumber(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getNumber called on something that is not a number: " + json);
		}
		return json.getAsNumber();
	}

	public static byte getByte(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getByte called on something that is not a number: " + json);
		}
		return json.getAsByte();
	}

	public static short getShort(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getShort called on something that is not a number: " + json);
		}
		return json.getAsShort();
	}

	public static int getInt(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getInt called on something that is not a number: " + json);
		}
		String value = json.getAsJsonPrimitive().getAsString();
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new UnexpectedJsonTypeException("getInt called on something that is not a int: " + json);
		}
		return json.getAsInt();
	}

	public static float getFloat(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getFloat called on something that is not a number: " + json);
		}

		String value = json.getAsJsonPrimitive().getAsString();
		try {
			Float.parseFloat(value);
		} catch (NumberFormatException e) {
			throw new UnexpectedJsonTypeException("getFloat called on something that is not a Float " + json);
		}
		return json.getAsFloat();
	}

	public static double getDouble(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getDouble called on something that is not a number: " + json);
		}
		String value = json.getAsJsonPrimitive().getAsString();
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new UnexpectedJsonTypeException("getDouble called on something that is not a Double " + json);
		}
		return json.getAsDouble();
	}

	public static long getLong(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber()) {
			throw new UnexpectedJsonTypeException("getLong called on something that is not a number: " + json);
		}
		String value = json.getAsJsonPrimitive().getAsString();
		try {
			Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new UnexpectedJsonTypeException("getLong called on something that is not a Long: " + json);
		}
		return json.getAsLong();
	}

	public static String getString(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
			throw new UnexpectedJsonTypeException("getString called on something that is not a string: " + json);
		}
		return json.getAsString();
	}

	public static List<String> getStringArray(JsonElement json) {
		if (!json.isJsonArray()) {
			throw new UnexpectedJsonTypeException("getString called on something that is not an array: " + json);
		}
		JsonArray array = json.getAsJsonArray();
		Iterator<JsonElement> iterator = array.iterator();
		List<String> strings = new ArrayList<>();
		while(iterator.hasNext()) {
			strings.add(getString(iterator.next()));
		}
		return strings;
	}

	public static List<Number> getNumberArray(JsonElement json) {
		if (!json.isJsonArray()) {
			throw new UnexpectedJsonTypeException("getString called on something that is not an array: " + json);
		}
		JsonArray array = json.getAsJsonArray();
		Iterator<JsonElement> iterator = array.iterator();
		List<Number> numbers = new ArrayList<>();
		while(iterator.hasNext()) {
			numbers.add(getNumber(iterator.next()));
		}
		return numbers;
	}

	public static List<Integer> getIntArray(JsonElement json) {
		if (!json.isJsonArray()) {
			throw new UnexpectedJsonTypeException("getInt called on something that is not an array: " + json);
		}
		JsonArray array = json.getAsJsonArray();
		Iterator<JsonElement> iterator = array.iterator();
		List<Integer> ints = new ArrayList<>();
		while(iterator.hasNext()) {
			ints.add(getInt(iterator.next()));
		}
		return ints;
	}

	public static boolean getBoolean(JsonElement json) {
		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isBoolean()) {
			throw new UnexpectedJsonTypeException("getBoolean called on something that is not a boolean: " + json);
		}
		return json.getAsBoolean();
	}

	public static String forceConversionToString(JsonElement json) {
		if (!json.isJsonPrimitive() || (!json.getAsJsonPrimitive().isNumber() && !json.getAsJsonPrimitive().isString())) {
			// I'm not 100% sure if bool/object conversions should be blocked; I suspect if we ever find a reason to
			// allow them then it's fine to do so, it's just not a path the current code uses.
			throw new UnexpectedJsonTypeException("forceConversionToString called on something that is neither a number nor a string: " + json);
		}

		return json.getAsString();
	}

	public static JsonObject toObject(JsonElement json) {
		if (!json.isJsonObject()) {
			// I'm not 100% sure if bool/object conversions should be blocked; I suspect if we ever find a reason to
			// allow them then it's fine to do so, it's just not a path the current code uses.
			throw new UnexpectedJsonTypeException("toObject called on something that is not a JsonObject: " + json);
		}

		return json.getAsJsonObject();
	}

	/**
	 * Uses JsonElement.getAsNumber() which will automatically convert to number
	 * as long as it is not JsonNull.
	 * Unlike getNumber, it will not throw an error if it's a json string
	 * @param json
	 * @return
	 * @throws ValueIsJsonNullException
	 */
	public static Number forceConversionToNumber(JsonElement json) throws ValueIsJsonNullException {
		if(json.isJsonNull()) {
			throw new ValueIsJsonNullException("Element has a JsonNull value");
		}
		if (!json.isJsonPrimitive() || (!json.getAsJsonPrimitive().isNumber() && !json.getAsJsonPrimitive().isString())) {
			throw new UnexpectedJsonTypeException("forceConversionToNumber called on something that is neither a number nor a string: " + json);
		}
		return json.getAsNumber();
	}

	/**
	 * Thrown if the value is JsonNull
	 */
	@SuppressWarnings("serial")
	public static class ValueIsJsonNullException extends Exception {
		public ValueIsJsonNullException(String msg) {
			super(msg);
		}
	}

	/**
	 * To allow conditions catch these exceptions when necessary
	 * i.e to catch and throw a nicer 'error(..., args(...))' from a condition
	 */
	@SuppressWarnings("serial")
	public static class UnexpectedJsonTypeException extends RuntimeException {
		public UnexpectedJsonTypeException(String msg) {
			super(msg);
		}
	}
}
