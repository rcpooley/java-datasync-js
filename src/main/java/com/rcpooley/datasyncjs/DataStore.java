package com.rcpooley.datasyncjs;

import org.json.JSONObject;

public class DataStore {

	private DataStoreClient manager;
	private EventEmitter emitter;
	private String storeid;

	public DataStore(DataStoreClient manager, String storeid) {
		this.manager = manager;
		this.emitter = new EventEmitter();
		this.storeid = storeid;
	}

	public String storeid() {
		return storeid;
	}

	public DataRef ref(String path) {
		return new DataRef(this, path);
	}

	public void value(String path, Callbacks.ValuePathCallback callback) {
		manager.__ds__getDataValue(this, path, callback);
	}

	public void update(String path, Object newVal) {
		update(path, newVal, new String[0]);
	}

	public void update(String path, Object newVal, String[] flags) {
		manager.__ds__updateData(this, path, newVal);

		emitter.emit("update", new JSONObject()
				.put("path", DataUtil.formatPath(path))
				.put("flags", DataUtil.toJSONArray(flags)));
	}

	public EventEmitter.Callback on(String event, String path, Callbacks.StoreUpdateCallback callback, boolean emitOnBind) {
		DataRef ref = this.ref(path);

		EventEmitter.Callback listener = (JSONObject update) -> {
			String[] flags = DataUtil.toStringArray(update.getJSONArray("flags"));

			DataRef updateRef = this.ref(update.getString("path"));

			if (updateRef.isChildOf(ref)) {
				if (event.equals("updateChild") && ref.equals(updateRef) ||
						event.equals("updateValue") && !ref.equals(updateRef) ||
						event.equals("updateDirect") && !ref.equals(updateRef)) {
					return;
				}

				ref.value((value, path1) -> callback.callback(value, ref.getRelativeChildPath(updateRef), flags));
			} else if (updateRef.hasChild(ref)) {
				if (event.equals("updateChild") || event.equals("updateDirect")) {
					return;
				}

				ref.value((value, path1) -> callback.callback(value, "/", flags));
			}
		};

		emitter.on("update", listener);

		if (emitOnBind) {
			ref.value((value, path1) -> callback.callback(value, "/", new String[0]));
		}

		return listener;
	}

	public void off(EventEmitter.Callback listener) {
		emitter.off("update", listener);
	}
}
