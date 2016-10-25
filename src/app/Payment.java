package app;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class Payment {
	
	private static final String MESSAGE_WAREHOUSE = "Warehouse address: Street(%1$s %2$s) City(%3$s) State(%4$s) Zip(%5$s)";
	private static final String MESSAGE_DISTRICT = "District address: Street(%1$s %2$s) City(%3$s) State(%4$s) Zip(%5$s)";
	private static final String MESSAGE_CUSTOMER = "Customer information: ID(%1$s, %2$s, %3$s), Name(%4$s, %5$s, %6$s), "
			+ "Address(%7$s, %8$s, %9$s, %10$s, %11$s), Phone(%12$s), Since(%13$s), Credits(%14$s, %15$s, %16$s, %17$s)";
	private static final String MESSAGE_PAYMENT = "Payment amount: %1$s";

	// Databases
	private static final String WAREHOUSEDISTRICT = "warehousedistrict";
	private static final String CUSTOMER = "customer";
	
	private DB database;
	private DBCollection tableWarehouseDistrict;
	private DBCollection tableCustomer;
	private BasicDBObject targetWarehouseDistrict;
	private BasicDBObject targetCustomer;
	
	public Payment(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouseDistrict = database.getCollection(WAREHOUSEDISTRICT);
		this.tableCustomer = database.getCollection(CUSTOMER);
	}
	
	public void processPayment(final int w_id, final int d_id, 
			final int c_id, final float payment) {
		selectWarehouseDistrict(w_id, d_id, payment);
		updateWarehouseDistrict(w_id, d_id, payment);
		selectCustomer(w_id, d_id, c_id, payment);
		updateCustomer(w_id, d_id, c_id, payment);
		outputResults(payment);
	}
	
	private void outputResults(float payment) {
		System.out.println(String.format(MESSAGE_CUSTOMER, 
				targetCustomer.getInt("c_w_id"),
				targetCustomer.getInt("c_d_id"),
				targetCustomer.getInt("c_id"),
				
				targetCustomer.getString("c_first"),
				targetCustomer.getString("c_middle"),
				targetCustomer.getString("c_last"),
				
				targetCustomer.getString("c_street_1"),
				targetCustomer.getString("c_street_2"),
				targetCustomer.getString("c_city"),
				targetCustomer.getString("c_state"),
				targetCustomer.getString("c_zip"),
				
				targetCustomer.getString("c_phone"),
				targetCustomer.getDate("c_since"),
				
				targetCustomer.getString("c_credit"),
				targetCustomer.getDouble("c_credit_lim"),
				targetCustomer.getDouble("c_discount"),
				targetCustomer.getDouble("c_balance")));
		
		System.out.println(String.format(MESSAGE_WAREHOUSE, 
				targetWarehouseDistrict.getString("w_street_1"),
				targetWarehouseDistrict.getString("w_street_2"),
				targetWarehouseDistrict.getString("w_city"),
				targetWarehouseDistrict.getString("w_state"),
				targetWarehouseDistrict.getString("w_zip")));
		
		System.out.println(String.format(MESSAGE_DISTRICT, 
				targetWarehouseDistrict.getString("d_street_1"),
				targetWarehouseDistrict.getString("d_street_2"),
				targetWarehouseDistrict.getString("d_city"),
				targetWarehouseDistrict.getString("d_state"),
				targetWarehouseDistrict.getString("d_zip")));
		
		System.out.println(String.format(MESSAGE_PAYMENT, payment));
	}
	
	private void selectWarehouseDistrict(final int w_id, final int d_id, final float payment) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("w_id", w_id);
		searchQuery.put("d_id", d_id);
		
		// Retrieve rows from table that satisfy where clause
		DBCursor cursor = this.tableWarehouseDistrict.find(searchQuery);
		if(cursor.hasNext()) {
			targetWarehouseDistrict = (BasicDBObject) cursor.next();
		}
	}
	
	private void selectCustomer(final int w_id, final int d_id, 
			final int c_id, final float payment) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("w_id", w_id);
		searchQuery.put("d_id", d_id);
		searchQuery.put("c_id", c_id);

		// Retrieve rows from table that satisfy where clause
		DBCursor cursor = this.tableCustomer.find(searchQuery);
		if(cursor.hasNext()) {
			targetCustomer = (BasicDBObject) cursor.next();
		}
	}
	
	private void updateWarehouseDistrict(final int w_id, final int d_id, final float payment) {
		// Where clause
		BasicDBObject query = new BasicDBObject();
		query.put("w_id", w_id);
		query.put("d_id", d_id);
		
		// Set update attributes
		double w_ytd = targetWarehouseDistrict.getDouble("w_ytd") + payment;
		double d_ytd = targetWarehouseDistrict.getDouble("d_ytd") + payment;
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("w_ytd", w_ytd);
		newDocument.put("d_ytd", d_ytd);

		// Update
		BasicDBObject update = new BasicDBObject();
		update.put("$set", newDocument);
		tableWarehouseDistrict.update(query, update);
	}
	
	private void updateCustomer(final int w_id, final int d_id, 
			final int c_id, final float payment) {		
		// Where clause
		BasicDBObject query = new BasicDBObject();
		query.put("w_id", w_id);
		query.put("d_id", d_id);
		query.put("c_id", d_id);
				
		// Set update attributes
		double c_balance = targetCustomer.getDouble("c_balance") - payment;
		double c_ytd_payment = targetCustomer.getDouble("c_ytd_payment") + payment;
		int c_payment_cnt = targetCustomer.getInt("c_payment_cnt") + 1;
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("c_balance", c_balance);
		newDocument.put("c_ytd_payment", c_ytd_payment);
		newDocument.put("c_payment_cnt", c_payment_cnt);

		// Update
		BasicDBObject update = new BasicDBObject();
		update.put("$set", newDocument);
		tableCustomer.update(query, update);
	}
}