import com.rcpooley.datasyncjs.Callbacks;
import com.rcpooley.datasyncjs.DataSocket;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MySocket extends DataSocket {

	private Socket socket;

	private Map<Callbacks.ObjectCallback, Emitter.Listener> listenerMap;

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
	public void on(String event, Callbacks.ObjectCallback callback) {
		Emitter.Listener list = objects -> callback.callback(objects.length > 0 ? objects[0] : null);
		listenerMap.put(callback, list);
		socket.on(event, list);
	}

	@Override
	public void off(String event, Callbacks.ObjectCallback callback) {
		if (callback != null) {
			socket.off(event, listenerMap.get(callback));
			listenerMap.remove(callback);
		} else {
			socket.off(event);
		}
	}

	@Override
	public void emit(String event, Object data) {
		socket.emit(event, data);
	}
}
