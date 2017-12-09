package com.rcpooley.datasyncjs;

import java.util.HashMap;
import java.util.Map;

public abstract class DataSocket {

	private Map<String, EventEmitter.Callback> listeners;

	public DataSocket() {
		listeners = new HashMap<>();
	}

	public Map<String, EventEmitter.Callback> __ds__getListeners() {
		return listeners;
	}

	public abstract void on(String event, Callbacks.ObjectCallback callback);

	public abstract void off(String event, Callbacks.ObjectCallback callback);

	public void off(String event) {
		off(event, null);
	}

	public abstract void emit(String event, Object data);
}
