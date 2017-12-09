package com.rcpooley.datasyncjs;

import org.json.JSONObject;

import java.util.*;

public class DataStoreClient {

	private static class DataStoreWrap {
		public Object data;
		public DataStore store;

		public DataStoreWrap(DataStoreClient manager, String storeid) {
			this.data = null;
			this.store = new DataStore(manager, storeid);
		}
	}

	private DataSocket socket;
	private Map<String, DataStoreWrap> storeData;

	public DataStoreClient() {
		this.storeData = new HashMap<>();
	}

	public void __ds__getDataValue(DataStore store, String rawPath, Callbacks.ValuePathCallback callback) {
		String path = DataUtil.formatPath(rawPath);

		DataStoreWrap wrap = storeData.get(store.storeid());

		callback.callback(DataUtil.traverseObject(wrap.data, path), path);
	}

	public void __ds__updateData(DataStore store, String rawPath, Object newVal) {
		String path = DataUtil.formatPath(rawPath);

		DataStoreWrap wrap = storeData.get(store.storeid());

		if (path.equals("/")) {
			wrap.data = newVal;
		} else {
			if (!(wrap.data instanceof JSONObject)) {
				wrap.data = new JSONObject();
			}

			DataUtil.traverseObjectForReference(wrap.data, path).put(DataUtil.getNameFromPath(path), newVal);
		}
	}

	public DataStoreClient serve(String storeid) {
		storeData.put(storeid, new DataStoreWrap(this, storeid));
		return this;
	}

	public DataStore getStore(String storeid) {
		if (storeData.containsKey(storeid)) {
			return storeData.get(storeid).store;
		}

		throw new RuntimeException("Invalid storeid: " + storeid);
	}

	private void bindStore(DataSocket socket, String storeid) {
		Callbacks.ValuePathCallback sendUpdate = (Object value, String path) -> {
			socket.emit("datasync_update_" + storeid, new JSONObject()
					.put("path", path)
					.put("value", DataUtil.toJSONString(value)));
		};

		DataStore store = getStore(storeid);

		socket.on("datasync_update_" + storeid, (Object obj) -> {
			JSONObject json = (JSONObject) obj;

			Object updateValue = DataUtil.parseJSONString(json.getString("value"));

			store.update(json.getString("path"), updateValue, new String[]{"updated"});
		});

		socket.__ds__getListeners().put(storeid, store.ref("/").on("update", ((Object value, String path, String[] flags) -> {
			if (Arrays.asList(flags).contains("updated")) {
				return;
			}

			store.ref(path).value(sendUpdate);
		})));
	}

	private void unbindStore(DataSocket socket, String storeid) {
		DataStore store = getStore(storeid);

		socket.off("datasync_update_" + storeid);

		store.off(socket.__ds__getListeners().get(storeid));

		socket.__ds__getListeners().remove(storeid);
	}

	private void clearStores(DataSocket socket) {
		new ArrayList<>(socket.__ds__getListeners().keySet()).forEach(storeid -> unbindStore(socket, storeid));
	}

	public void setSocket(DataSocket socket) {
		this.clearSocket();

		this.socket = socket;
		storeData.keySet().forEach(storeid -> {
			bindStore(this.socket, storeid);
			socket.emit("datasync_bindstore", storeid);
		});
	}

	public void clearSocket() {
		if (socket != null) {
			this.clearStores(socket);
			socket = null;
		}
	}
}
