import com.rcpooley.datasyncjs.DataRef;
import com.rcpooley.datasyncjs.DataStore;
import com.rcpooley.datasyncjs.DataStoreClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class Test {

	private static final int COUNT = 0;
	private static final int WORD = 1;
	private static final int DELETE = 2;
	private static final int PINGPONG = 3;
	private static final int EVENTCEPTION = 4;
	private static final int TESTEXCEPTION = 5;

	private static int MODE = PINGPONG;

	public static void main(String[] args) {
		DataStoreClient client = new DataStoreClient();

		MySocket socket = new MySocket();

		socket.on("connect", obj -> {
			System.out.println("Connected!");
			client.setSocket(socket).connectStore("store");
		});
		socket.on("disconnect", obj -> {
			System.out.println("Disconnected :(");
			client.clearSocket();
		});

		DataStore store = client.getStore("store");

		int tick = 0;

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!socket.connected()) continue;

			switch (MODE) {
				case COUNT: {
					store.ref("/count").value((Object value, String path) -> {
						int i = (int) value;

						System.out.println("Updating " + path + " from " + i + " to " + (++i));

						store.ref("/count").update(i);
					});
					break;
				}

				case WORD: {
					store.ref("/word").value((Object value, String path) -> {
						String word = (String) value;
						String newWord = (char)(word.charAt(0) + 1) + word.substring(1);

						System.out.println("Updating " + word + " to " + newWord);

						store.ref("/word").update(newWord);
					});
					break;
				}

				case DELETE: {
					System.out.println("Tick " + tick + ":");
					store.ref("/delete").value((Object value, String path) -> {
						JSONObject val = (JSONObject) value;
						if (val == null) {
							System.out.println("   null");
						} else {
							val.keySet().forEach(key -> {
								System.out.println("   " + key + ": " + val.get(key));
							});
						}
					});

					DataRef ref = store.ref("/delete-cmd");

					switch(tick) {
						case 0:
							ref.update(new JSONObject().put("cmd", "set").put("args", new JSONArray().put("/a").put("aval")));
							ref.update(new JSONObject().put("cmd", "set").put("args", new JSONArray().put("/b").put("bval")));
							break;
						case 1:
							ref.update(new JSONObject().put("cmd", "set").put("args", new JSONArray().put("/a")));
							break;
						case 2:
							ref.update(new JSONObject().put("cmd", "del").put("args", new JSONArray().put("/b")));
							break;
					}
					tick = (tick + 1) % 3;
					break;
				}

				case PINGPONG: {
					Object text = new JSONArray().put("first");

					store.ref("/ping").update(text);

					store.ref("/pong").value(((value, path) -> {
						if (value == null) value = "NULL";
						System.out.println(text + " = " + value + ": " + (text.toString().equals(value.toString()) ? "TRUE" : "FALSE"));
					}));
					break;
				}

				case EVENTCEPTION: {
					store.ref("/pong").on("update", ((value, path, flags) -> {
						store.ref("lol").on("update", ((value1, path1, flags1) -> {

						}));
					}));

					store.ref("/ping").update("cool");
					break;
				}

				case TESTEXCEPTION: {
					store.ref("/ping").update(new JSONArray().put("first"));
					break;
				}
			}
		}
	}

}
