package app;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TopBalance {

	private static final String MESSAGE_OUTPUT = "C_NAME: {%s} C_BALANCE: {%2f} W_NAME, D_NAME: {%s}\n";

	private static final String TABLE_CUSTOMER= "customer";
	private static final String TABLE_WAREHOUSE = "warehouse";

	//====================================================================================
	// Preparing for session
	//====================================================================================

	private MongoDatabase database;
	private MongoCollection<Document> tableCustomer;
	private MongoCollection<Document> tableWarehouse;

	public TopBalance(MongoDBConnect connect){
		this.database = connect.getDatabase();
		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
		this.tableWarehouse = database.getCollection(TABLE_WAREHOUSE);
	}

	public void processTopBalance() {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		String c_name, warehouseDistrictName;
		Double c_balance;
		
		try {
			bw.write("======Processing TopBalance transaction========\n");
			ArrayList<Document> customers = selectTopBalance();
			for(int i = 0; i< customers.size(); i++) {
				Document customer = customers.get(i);
				c_name = customer.getString("c_first") + " " + customer.getString("c_middle") + " " + customer.getString("c_last");
				c_balance = customer.getDouble("c_balance");
				warehouseDistrictName = getWarehouseDistrictName(customer.getInteger("c_w_id"), customer.getInteger("c_d_id"));
				bw.write(String.format(MESSAGE_OUTPUT, c_name, c_balance, warehouseDistrictName));
			}

			bw.write("\n");
			bw.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String getWarehouseDistrictName(int c_w_id, int c_d_id) {
		String name = "";

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(match(eq("w_id", c_w_id)));
		pipeline.add(project(include("w_name","district")));
		pipeline.add(unwind("$district"));
		pipeline.add(match(eq("district.d_id", c_d_id)));
		pipeline.add(project(include("w_name", "district.d_name")));

		MongoCursor<Document> cursor = tableWarehouse.aggregate(pipeline).iterator();
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			Document district = (Document) doc.get("district");
			name = doc.getString("w_name") + ", " + district.getString("d_name");
		}

		return name;
	}

	private ArrayList<Document> selectTopBalance() {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(project(fields(include("c_first", "c_middle", "c_last", "c_balance", "c_w_id", "c_d_id"), excludeId())));
		pipeline.add(sort(descending("c_balance")));
		pipeline.add(limit(10));

		ArrayList<Document> topTen = tableCustomer.aggregate(pipeline).into(new ArrayList<Document>());

		return topTen;
	}

}
