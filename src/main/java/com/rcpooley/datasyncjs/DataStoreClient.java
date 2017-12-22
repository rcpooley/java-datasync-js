package com.rcpooley.datasyncjs;

import org.json.JSONObject;

import java.util.*;

public class DataStoreClient {

	private static class ReqInfo {
		public String storeID;
		public String userID;
		public ReqInfo(String storeID, String userID) {
			this.storeID = storeID;
			this.userID = userID;
		}
	}

	private Binder binder;
	private DataStores stores;
	private DataSocket socket;
	private Map<String, ReqInfo> reqMap;

	public DataStoreClient() {
		this.binder = new Binder();
		this.stores = new DataStores();
		this.reqMap = new HashMap<>();
	}

	private String genReqID() {
		boolean valid;
		String reqID;

		do {
			reqID = DataUtil.randomString(10);
			valid = !this.reqMap.containsKey(reqID);
		} while (!valid);

		return reqID;
	}

	public DataStoreClient setSocket(DataSocket socket) {
		this.clearSocket();

		this.socket = socket;

		this.socket.on("datasync_bindstore", (Object... args) -> {
			String reqID = (String) args[0];
			String bindID = (String) args[1];
			ReqInfo req = this.reqMap.get(reqID);

			if (bindID != null) {
				DataStore store = this.stores.getStore(req.storeID, req.userID, true);
				this.binder.bindStore(socket, store, bindID);
				socket.emit("datasync_fetchall_" + bindID, "");
			}

			this.reqMap.remove(reqID);
		});

		return this;
	}

	public void clearSocket() {
		if (socket == null) {
			return;
		}

		this.socket.off("datasync_bindstore");

		this.binder.unbindAll(this.socket);
		this.socket = null;
	}

	public DataStoreClient connectStore(String storeID) {
		return this.connectStore(storeID, "global");
	}

	public DataStoreClient connectStore(String storeID, String userID) {
		return this.connectStore(storeID, userID, new JSONObject());
	}

	public DataStoreClient connectStore(String storeID, String userID, JSONObject connInfo) {
		String reqID = this.genReqID();

		this.reqMap.put(reqID, new ReqInfo(storeID, userID));

		this.socket.emit("datasync_bindrequest", reqID, storeID, connInfo);

		return this;
	}

	public DataStore getStore(String storeID) {
		return this.getStore(storeID, "global");
	}

	public DataStore getStore(String storeID, String userID) {
		return this.stores.getStore(storeID, userID, true);
	}
}
