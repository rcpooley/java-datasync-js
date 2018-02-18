import com.rcpooley.datasyncjs.DataRef;
import com.rcpooley.datasyncjs.DataStore;
import com.rcpooley.datasyncjs.DataStoreClient;

public class Test2 {

    public static void main(String[] args) {
        DataStoreClient client = new DataStoreClient();
        DataStore store = client.getStore("store");

        store.update("/ref/a", 42);

        DataRef ref = store.ref("/ref");
        DataRef refB = ref.ref("/b");

        DataRef refBParent = refB.parent();

        refBParent.ref("/a").value((value, path) -> {
            System.out.println("Path: " + path);
            System.out.println("Value: " + value);
        });
    }

}
