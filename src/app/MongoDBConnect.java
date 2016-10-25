package app;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoDBConnect {
	
	private MongoClient client;
	private DB database;
	
	public MongoDBConnect(String node, int port, String database) {
		this.client = new MongoClient(node, port);
		this.database = client.getDB(database);	
	}
	
	public DB getDatabase() {
		return this.database;
	}
	
	public void close() {
		this.client.close();
	}
}
