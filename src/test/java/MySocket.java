import com.rcpooley.datasyncjs.Callbacks;
import com.rcpooley.datasyncjs.DataSocket;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MySocket extends DataSocket {

	private Socket socket;

	private Map<Callbacks.ObjectsCallback, Emitter.Listener> listenerMap;

	public MySocket() {
		this.listenerMap = new HashMap<>();

		IO.Options opts = new IO.Options();
		opts.reconnection = true;

		try {
			socket = IO.socket("http://localhost:4321", opts);
			socket.connect();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public boolean connected() {
		return socket.connected();
	}

	@Override
	public void on(String event, Callbacks.ObjectsCallback callback) {
		Emitter.Listener list = objects -> {
			try {
				Method cb = callback.getClass().getMethod("callback", Object[].class);
				cb.setAccessible(true);
				cb.invoke(callback, (Object) objects);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		};
		listenerMap.put(callback, list);
		socket.on(event, list);
	}

	@Override
	public void off(String event, Callbacks.ObjectsCallback callback) {
		if (callback != null) {
			socket.off(event, listenerMap.get(callback));
			listenerMap.remove(callback);
		} else {
			socket.off(event);
		}
	}

	@Override
	public void emit(String event, Object... data) {
		try {
			Method emit = socket.getClass().getMethod("emit", String.class, Object[].class);
			emit.invoke(socket, event, data);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	@Override
	public String id() {
		return "lolid";
	}
}
