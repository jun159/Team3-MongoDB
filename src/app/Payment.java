package app;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Payment {
	
	private static final String MESSAGE_WAREHOUSE = "Warehouse address: Street(%1$s %2$s) City(%3$s) State(%4$s) Zip(%5$s)";
	private static final String MESSAGE_DISTRICT = "District address: Street(%1$s %2$s) City(%3$s) State(%4$s) Zip(%5$s)";
	private static final String MESSAGE_CUSTOMER = "Customer information: ID(%1$s, %2$s, %3$s), Name(%4$s, %5$s, %6$s), "
			+ "Address(%7$s, %8$s, %9$s, %10$s, %11$s), Phone(%12$s), Since(%13$s), Credits(%14$s, %15$s, %16$s, %17$s)";
	private static final String MESSAGE_PAYMENT = "Payment amount: %1$s";

	private static final String TABLE_WAREHOUSE = "warehouse";
	private static final String TABLE_DISTRICT = "district";
	private static final String TABLE_CUSTOMER = "customer";
	
	private MongoDatabase database;
	private MongoCollection<Document> tableWarehouse;
	private MongoCollection<Document> tableDistrict;
	private MongoCollection<Document> tableCustomer;
	private Document targetWarehouse;
	private Document targetDistrict;
	private Document targetCustomer;
	
	public Payment(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouse = database.getCollection(TABLE_WAREHOUSE);
		this.tableDistrict = database.getCollection(TABLE_DISTRICT);
		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
	}
	
	public void processPayment(final int w_id, final int d_id, 
			final int c_id, final float payment) {
		selectWarehouse(w_id);
		selectDistrict(w_id, d_id);
		updateWarehouse(w_id, payment);
		updateDistrict(w_id, d_id, payment);
		selectCustomer(w_id, d_id, c_id);
		updateCustomer(w_id, d_id, c_id, payment);
		outputResults(payment);
	}
	
	private void outputResults(float payment) {
		System.out.println(String.format(MESSAGE_CUSTOMER, 
				targetCustomer.getInteger("c_w_id"),
				targetCustomer.getInteger("c_d_id"),
				targetCustomer.getInteger("c_id"),
				
				targetCustomer.getString("c_first"),
				targetCustomer.getString("c_middle"),
				targetCustomer.getString("c_last"),
				
				targetCustomer.getString("c_street_1"),
				targetCustomer.getString("c_street_2"),
				targetCustomer.getString("c_city"),
				targetCustomer.getString("c_state"),
				targetCustomer.getInteger("c_zip"),
				
				targetCustomer.getLong("c_phone"),
				targetCustomer.getString("c_since"),
				
				targetCustomer.getString("c_credit"),
				targetCustomer.getDouble("c_credit_lim"),
				targetCustomer.getDouble("c_discount"),
				targetCustomer.getDouble("c_balance")));
		
		System.out.println(String.format(MESSAGE_WAREHOUSE, 
				targetWarehouse.getString("w_street_1"),
				targetWarehouse.getString("w_street_2"),
				targetWarehouse.getString("w_city"),
				targetWarehouse.getString("w_state"),
				targetWarehouse.getInteger("w_zip")));
		
		System.out.println(String.format(MESSAGE_DISTRICT, 
				targetDistrict.getString("d_street_1"),
				targetDistrict.getString("d_street_2"),
				targetDistrict.getString("d_city"),
				targetDistrict.getString("d_state"),
				targetDistrict.getInteger("d_zip")));
		
		System.out.println(String.format(MESSAGE_PAYMENT, payment));
	}
	
	private void selectWarehouse(final int w_id) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("w_id", w_id);
		
		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableWarehouse.find(searchQuery).iterator();
		if(cursor.hasNext()) {
			targetWarehouse = cursor.next();
		} 
		cursor.close();
	}
	
	private void selectDistrict(final int w_id, final int d_id) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("d_w_id", w_id);
		searchQuery.put("d_id", d_id);
		
		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableDistrict.find(searchQuery).iterator();
		if(cursor.hasNext()) {
			targetDistrict = cursor.next();
		}
		cursor.close();
	}
	
	private void selectCustomer(final int w_id, final int d_id, final int c_id) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("c_w_id", w_id);
		searchQuery.put("c_d_id", d_id);
		searchQuery.put("c_id", c_id);

		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableCustomer.find(searchQuery).iterator();
		if(cursor.hasNext()) {
			targetCustomer = cursor.next();
		} 
		cursor.close();
	}
	
	private void updateWarehouse(final int w_id, final float payment) {
		// Where clause
		BasicDBObject query = new BasicDBObject();
		query.put("w_id", w_id);
		
		// Set update attributes
		double w_ytd = targetWarehouse.getDouble("w_ytd") + payment;
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("w_ytd", w_ytd);

		// Update
		BasicDBObject update = new BasicDBObject();
		update.put("$set", newDocument);
		tableWarehouse.updateOne(query, update);
	}
	
	private void updateDistrict(final int w_id, final int d_id, final float payment) {
		// Where clause
		BasicDBObject query = new BasicDBObject();
		query.put("d_w_id", w_id);
		query.put("d_id", d_id);
		
		// Set update attributes
		double w_ytd = targetDistrict.getDouble("d_ytd") + payment;
		double d_ytd = targetDistrict.getDouble("d_ytd") + payment;
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("w_ytd", w_ytd);
		newDocument.put("district.d_ytd", d_ytd);

		// Update
		BasicDBObject update = new BasicDBObject();
		update.put("$set", newDocument);
		tableDistrict.updateOne(query, update);
	}
	
	private void updateCustomer(final int w_id, final int d_id, 
			final int c_id, final float payment) {		
		// Where clause
		BasicDBObject query = new BasicDBObject();
		query.put("c_w_id", w_id);
		query.put("c_d_id", d_id);
		query.put("c_id", d_id);
				
		// Set update attributes
		double c_balance = targetCustomer.getDouble("c_balance") - payment;
		double c_ytd_payment = targetCustomer.getDouble("c_ytd_payment") + payment;
		int c_payment_cnt = targetCustomer.getInteger("c_payment_cnt") + 1;
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("c_balance", c_balance);
		newDocument.put("c_ytd_payment", c_ytd_payment);
		newDocument.put("c_payment_cnt", c_payment_cnt);

		// Update
		BasicDBObject update = new BasicDBObject();
		update.put("$set", newDocument);
		tableCustomer.updateOne(query, update);
	}
}