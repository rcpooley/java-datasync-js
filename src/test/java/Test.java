import com.rcpooley.datasyncjs.DataStore;
import com.rcpooley.datasyncjs.DataStoreClient;

public class Test {

	private static final int COUNT = 0;
	private static final int WORD = 1;

	private static int MODE = WORD;

	public static void main(String[] args) {
		DataStoreClient client = new DataStoreClient().serve("store");

		MySocket socket = new MySocket();

		socket.on("connect", obj -> {
			System.out.println("Connected!");
			client.setSocket(socket);
		});
		socket.on("disconnect", obj -> {
			System.out.println("Disconnected :(");
			client.clearSocket();
		});

		DataStore store = client.getStore("store");

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
			}

		}
	}

}
