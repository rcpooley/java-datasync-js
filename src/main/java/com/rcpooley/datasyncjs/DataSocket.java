package com.rcpooley.datasyncjs;

public abstract class DataSocket {

	public abstract void on(String event, Callbacks.ObjectsCallback callback);

	public abstract void off(String event, Callbacks.ObjectsCallback callback);

	public void off(String event) {
		off(event, null);
	}

	public abstract void emit(String event, Object... args);

	public abstract String id();
}
