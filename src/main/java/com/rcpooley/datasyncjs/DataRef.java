package com.rcpooley.datasyncjs;

import org.json.JSONObject;

public class DataRef {

	private DataStore store;
	private String path;
	private String name;

	public DataRef(DataStore store, String path) {
		this.store = store;
		this.path = path;
		this.name = DataUtil.getNameFromPath(this.path);
	}

	public String path() {
		return path;
	}

	public String name() {
		return name;
	}

	public DataRef parent() {
		return store.ref(this.path.substring(0, this.path.length() - this.name.length()));
	}

	public boolean hasChild(DataRef ref) {
		return ref.path().indexOf(this.path) == 0;
	}

	public boolean isChildOf(DataRef ref) {
		return ref.hasChild(this);
	}

	public String getRelativeChildPath(DataRef ref) {
		return DataUtil.formatPath(ref.path()).substring(this.path.length());
	}

	public DataRef ref(String path) {
		String tmpPath = this.path + DataUtil.formatPath(path);
		if (this.path.equals("/")) {
			tmpPath = path;
		}
		return store.ref(tmpPath);
	}

	public void value(Callbacks.ValuePathCallback callback) {
		store.value(this.path, callback);
	}

	public void update(Object newVal) {
		store.update(this.path, newVal);
	}

	public void update(Object newVal, String[] flags) {
		store.update(this.path, newVal, flags);
	}

	public void remove() {
		this.remove(new String[0]);
	}

	public void remove(String[] flags) {
		store.remove(this.path, flags);
	}

	public EventEmitter.Callback on(String event, Callbacks.StoreUpdateCallback callback) {
		return on(event, callback, false);
	}

	public EventEmitter.Callback on(String event, Callbacks.StoreUpdateCallback callback, boolean emitOnBind) {
		return store.on(event, this.path, callback, emitOnBind);
	}

	public void off(EventEmitter.Callback listener) {
		store.off(listener);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof DataRef && ((DataRef) other).path().equals(this.path);
	}
}
