package com.rcpooley.datasyncjs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DataStores {

	private static class DataStoreWrap {
		public Object data;
		public DataStore store;

		public DataStoreWrap(DataStores manager, String storeid, String userid) {
			this.data = null;
			this.store = new DataStore(manager, storeid, userid);
		}
	}

	private Map<String, Map<String, DataStoreWrap>> stores;

	public DataStores() {
		this.stores = new HashMap<>();
	}

	private Map<String, DataStoreWrap> getStores(String storeID) {
		if (!stores.containsKey(storeID)) {
			stores.put(storeID, new HashMap<>());
		}
		return stores.get(storeID);
	}

	public void __ds__getDataValue(DataStore store, String rawPath, Callbacks.ValuePathCallback callback) {
		String path = DataUtil.formatPath(rawPath);

		DataStoreWrap wrap = this.getStores(store.storeid()).get(store.userid());

		Object value = DataUtil.traverseObject(wrap.data, path);

		callback.callback(DataUtil.clone(value), path);
	}

	public void __ds__updateData(DataStore store, String rawPath, Object newVal) {
		String path = DataUtil.formatPath(rawPath);

		DataStoreWrap wrap = this.getStores(store.storeid()).get(store.userid());

		if (path.equals("/")) {
			wrap.data = newVal;
		} else {
			if (!(wrap.data instanceof JSONObject)) {
				wrap.data = new JSONObject();
			}

			DataUtil.traverseObjectForReference(wrap.data, path).put(DataUtil.getNameFromPath(path), newVal);
		}
	}

	public void __ds__deleteData(DataStore store, String rawPath) {
		String path = DataUtil.formatPath(rawPath);

		DataStoreWrap wrap = this.getStores(store.storeid()).get(store.userid());

		if (path.equals("/")) {
			wrap.data = null;
		} else {
			if (!(wrap.data instanceof JSONObject)) {
				wrap.data = new JSONObject();
			}

			DataUtil.traverseObjectForReference(wrap.data, path).remove(DataUtil.getNameFromPath(path));
		}
	}

	public DataStore getStore(String storeID, String userID, boolean initialize) {
		if (!this.stores.containsKey(storeID) && !initialize) {
			throw new RuntimeException("Invalid storeID: " + storeID + "-" + userID);
		}

		Map<String, DataStoreWrap> stores = this.getStores(storeID);

		if (!stores.containsKey(userID)) {
			stores.put(userID, new DataStoreWrap(this, storeID, userID));
		}

		return stores.get(userID).store;
	}

	public DataStores serveStore(String storeID) {
		this.getStores(storeID);
		return this;
	}

	public boolean hasStore(String storeID) {
		return this.stores.containsKey(storeID);
	}
}
