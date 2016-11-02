package app;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TopBalance {

	private static final String MESSAGE_OUTPUT = "C_NAME: {%s} C_BALANCE: {%2f} W_NAME: {%s} D_NAME: {%s}\n";

	private static final String TABLE_CUSTOMER= "customer";

	//====================================================================================
	// Preparing for session
	//====================================================================================

	private MongoDatabase database;
	private MongoCollection<Document> tableCustomer;

	public TopBalance(MongoDBConnect connect){
		this.database = connect.getDatabase();
		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
	}

	public void processTopBalance() {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		String c_name, w_name, d_name;
		Double c_balance;
		
		try {
			bw.write("======Processing TopBalance transaction========\n");
			ArrayList<Document> customers = selectTopBalance();
			for(int i = 0; i< customers.size(); i++) {
				Document customer = customers.get(i);
				c_name = customer.getString("c_first") + " " + customer.getString("c_middle") + " " + customer.getString("c_last");
				c_balance = customer.getDouble("c_balance");
				w_name = customer.getString("w_name");
				d_name = customer.getString("d_name");
				bw.write(String.format(MESSAGE_OUTPUT, c_name, c_balance, w_name, d_name));
			}

			bw.write("\n");
			bw.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private ArrayList<Document> selectTopBalance() {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(project(fields(include("c_first", "c_middle", "c_last", "c_balance", "w_name", "d_name"), excludeId())));
		pipeline.add(sort(descending("c_balance")));
		pipeline.add(limit(10));

		ArrayList<Document> topTen = tableCustomer.aggregate(pipeline).into(new ArrayList<Document>());

		return topTen;
	}

}
