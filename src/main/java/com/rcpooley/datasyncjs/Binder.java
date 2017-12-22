package com.rcpooley.datasyncjs;

import org.json.JSONObject;

import java.util.*;

public class Binder {

	private static class BindInfo {
		public DataStore store;
		public EventEmitter.Callback listener;

		public BindInfo(DataStore store, EventEmitter.Callback listener) {
			this.store = store;
			this.listener = listener;
		}
	}

	private Map<String, Map<String, BindInfo>> listeners;

	public Binder() {
		this.listeners = new HashMap<>();
	}

	private Map<String, BindInfo> getListeners(String socketID) {
		if (!this.listeners.containsKey(socketID)) {
			this.listeners.put(socketID, new HashMap<>());
		}
		return this.listeners.get(socketID);
	}

	public void bindStore(DataSocket socket, DataStore store, String bindID) {
		Callbacks.SendUpdateCallback sendUpdate = (String path, Object value, boolean remove) -> {
			socket.emit("datasync_update_" + bindID, new JSONObject()
					.put("path", path)
					.put("value", DataUtil.toJSONString(value))
					.put("remove", remove));
		};

		socket.on("datasync_update_" + bindID, (Object... objs) -> {
			Object obj = objs[0];
			JSONObject json = (JSONObject) obj;

			String path = json.getString("path");
			Object updateValue = DataUtil.parseJSONString(json.getString("value"));
			boolean remove = json.getBoolean("remove");
			String[] flags = {socket.id()};

			if (remove) {
				store.remove(path, flags);
			} else {
				store.update(path, updateValue, flags);
			}
		});

		this.getListeners(socket.id()).put(bindID, new BindInfo(store,
				store.ref("/").on("update", ((value, path, flags) -> {
					List<String> flagz = Arrays.asList(flags);
					if (flagz.contains(socket.id())) {
						return;
					}

					store.ref(path).value((value1, path1) -> {
						sendUpdate.callback(path1, value1, flagz.contains("__ds__removed"));
					});
				}))));
	}

	public void unbindStore(DataSocket socket, String bindID) {
		socket.off("datasync_update_" + bindID);

		BindInfo info = this.getListeners(socket.id()).get(bindID);

		if (info == null) {
			return;
		}

		info.store.off(info.listener);

		this.getListeners(socket.id()).remove(bindID);
	}

	public void unbindAll(DataSocket socket) {
		List<String> bindIDs = new ArrayList<>(this.getListeners(socket.id()).keySet());
		bindIDs.forEach(bindID -> this.unbindStore(socket, bindID));
	}
}
