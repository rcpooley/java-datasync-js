package com.rcpooley.datasyncjs;

import org.json.JSONObject;

public class Callbacks {
	@FunctionalInterface
	public interface ValuePathCallback {
		void callback(Object value, String path);
	}

	@FunctionalInterface
	public interface StoreUpdateCallback {
		void callback(Object value, String path, String[] flags);
	}

	@FunctionalInterface
	public interface JSONCallback {
		void callback(JSONObject json);
	}

	@FunctionalInterface
	public interface VoidCallback {
		void callback();
	}

	@FunctionalInterface
	public interface ObjectCallback {
		void callback(Object obj);
	}
}
