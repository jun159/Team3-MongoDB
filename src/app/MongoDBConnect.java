package app;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnect {
	
	private MongoClient client;
	private MongoDatabase database;
	
	public MongoDBConnect(String node, int port, String database) {
		this.client = new MongoClient(node, port);
		this.database = client.getDatabase(database);
	}
	
	public MongoDatabase getDatabase() {
		return this.database;
	}
	
	public void close() {
		this.client.close();
	}
}
