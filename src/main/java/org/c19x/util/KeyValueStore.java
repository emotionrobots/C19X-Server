package org.c19x.util;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class KeyValueStore {
	private final DB db;
	private final ConcurrentMap<String, String> map;

	public KeyValueStore(final File file) {
		this.db = DBMaker.fileDB(file).transactionEnable().closeOnJvmShutdown().make();
		this.map = db.hashMap("map", Serializer.STRING, Serializer.STRING).createOrOpen();
	}

	public void put(final String key, final String value) {
		map.put(key, value);
		db.commit();
	}

	public String get(final String key) {
		return map.get(key);
	}

	public Set<String> keys() {
		return map.keySet();
	}

	public Set<Map.Entry<String, String>> entries() {
		return map.entrySet();
	}

	public void close() {
		if (!db.isClosed()) {
			db.close();
		}
	}

}
