package com.rcpooley.datasyncjs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class DataUtil {

	public static String formatPath(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.equals("")) {
			path = "/";
		}
		return path;
	}

	public static String getNameFromPath(String path) {
		String[] spl = formatPath(path).split("/", -1);
		return spl[spl.length - 1];
	}

	public static Object traverseObject(Object obj, String rawPath) {
		String path = DataUtil.formatPath(rawPath);

		String[] spl;
		if (path.length() > 1) {
			String[] spll = path.split("/");
			spl = Arrays.copyOfRange(spll, 1, spll.length);
		} else {
			spl = new String[0];
		}

		return traverseObjectWithArray(obj, spl);
	}

	private static Object traverseObjectWithArray(Object obj, String[] pathArray) {
		if (pathArray.length == 0) {
			return obj;
		}

		if (!(obj instanceof JSONObject)) {
			return null;
		}

		JSONObject json = (JSONObject) obj;

		String curNode = pathArray[0];

		if (json.has(curNode)) {
			return traverseObjectWithArray(json.get(curNode), Arrays.copyOfRange(pathArray, 1, pathArray.length));
		}

		return null;
	}

	public static JSONObject traverseObjectForReference(Object obj, String rawPath) {
		String path = formatPath(rawPath);

		String[] spl;
		if (path.length() > 1) {
			String[] spll = path.split("/");
			spl = Arrays.copyOfRange(spll, 1, spll.length);
		} else {
			spl = new String[0];
		}

		return traverseObjectForReferenceWithArray(obj, spl);
	}

	private static JSONObject traverseObjectForReferenceWithArray(Object obj, String[] pathArray) {
		if (pathArray.length == 0 || !(obj instanceof JSONObject)) {
			return null;
		}

		JSONObject json = (JSONObject) obj;

		if (pathArray.length == 1) {
			return json;
		}

		String curNode = pathArray[0];

		if (!(json.has(curNode)) || !(json.get(curNode) instanceof JSONObject)) {
			json.put(curNode, new JSONObject());
		}

		return traverseObjectForReferenceWithArray(json.get(curNode), Arrays.copyOfRange(pathArray, 1, pathArray.length));
	}

	public static Object parseJSONString(String str) {
		if (str.startsWith("{") && str.endsWith("}")) {
			return new JSONObject(str);
		}

		try {
			return Integer.parseInt(str);
		} catch (Exception e1) {
			try {
				return Double.parseDouble(str);
			} catch (Exception e2) {
				return str.substring(1, str.length() - 1);
			}
		}
	}

	public static String toJSONString(Object val) {
		if (val instanceof String) {
			return "\"" + val.toString() + "\"";
		} else {
			return val.toString();
		}
	}

	public static String[] toStringArray(JSONArray arr) {
		if (arr == null) {
			return null;
		}

		String[] s = new String[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			s[i] = arr.optString(i);
		}
		return s;
	}

	public static JSONArray toJSONArray(Object[] arr) {
		JSONArray a = new JSONArray();
		for (Object o : arr) {
			a.put(o);
		}
		return a;
	}
}
