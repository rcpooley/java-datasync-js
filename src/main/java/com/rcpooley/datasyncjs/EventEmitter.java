package com.rcpooley.datasyncjs;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventEmitter {

	@FunctionalInterface
	public interface Callback {
		void callback(JSONObject data);
	}

	private Map<String, List<Callback>> listeners;

	public EventEmitter() {
		this.listeners = new HashMap<>();
	}

	private List<Callback> getListeners(String event) {
		if (!listeners.containsKey(event)) {
			listeners.put(event, new ArrayList<>());
		}
		return listeners.get(event);
	}

	public void on(String event, Callback listener) {
		getListeners(event).add(listener);
	}

	public void off(String event) {
		off(event, null);
	}

	public void off(String event, Callback listener) {
		if (listener == null) {
			listeners.remove(event);
		} else {
			getListeners(event).remove(listener);
		}
	}

	public void emit(String event, JSONObject data) {
		new ArrayList<>(getListeners(event)).forEach(callback -> {
			callback.callback(data);
		});
	}
}
