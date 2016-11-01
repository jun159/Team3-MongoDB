/* Author: Luah Bao Jun
 * ID: A0126258A
 * Team: 3
 */

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
	private static final String TABLE_CUSTOMER = "customer";
	
	private static final boolean DEBUG = true;
	
	private MongoDatabase database;
	private MongoCollection<Document> tableWarehouseDistrict;
	private MongoCollection<Document> tableCustomer;
	private Document targetWarehouse;
	private Document targetDistrict;
	private Document targetCustomer;
	
	public Payment(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouseDistrict = database.getCollection(TABLE_WAREHOUSE);
		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
	}
	
	public void processPayment(final int w_id, final int d_id, 
			final int c_id, final float payment) {
		selectWarehouseDistrict(w_id, d_id);
		updateWarehouseDistrict(w_id, d_id, payment);
		selectCustomer(w_id, d_id, c_id);
		updateCustomer(w_id, d_id, c_id, payment);
		
		if(DEBUG) {
			System.out.println("Before w:  " + targetWarehouse.get("w_ytd"));
			System.out.println("Before d:  " + targetDistrict.get("d_ytd"));
			selectWarehouseDistrict(w_id, d_id);
			System.out.println("After w:   " + targetWarehouse.get("w_ytd"));
			System.out.println("After d:   " + targetDistrict.get("d_ytd"));
			System.out.println("Before c: " + targetCustomer.getDouble("c_balance")
					+ " " + targetCustomer.getDouble("c_ytd_payment")
					+ " " + targetCustomer.getInteger("c_payment_cnt"));
			selectCustomer(w_id, d_id, c_id);
			System.out.println("After c:  " + targetCustomer.getDouble("c_balance")
					+ " " + targetCustomer.getDouble("c_ytd_payment")
					+ " " + targetCustomer.getInteger("c_payment_cnt"));
		}
			
		outputResults(payment);
	}
	
	private void outputResults(float payment) {
		System.out.println(String.format(MESSAGE_CUSTOMER, 
				targetCustomer.get("c_w_id"),
				targetCustomer.get("c_d_id"),
				targetCustomer.get("c_id"),
				
				targetCustomer.get("c_first"),
				targetCustomer.get("c_middle"),
				targetCustomer.get("c_last"),
				
				targetCustomer.get("c_street_1"),
				targetCustomer.get("c_street_2"),
				targetCustomer.get("c_city"),
				targetCustomer.get("c_state"),
				targetCustomer.get("c_zip"),
				
				targetCustomer.get("c_phone"),
				targetCustomer.get("c_since"),
				
				targetCustomer.get("c_credit"),
				targetCustomer.get("c_credit_lim"),
				targetCustomer.get("c_discount"),
				targetCustomer.get("c_balance")));
		
		System.out.println(String.format(MESSAGE_WAREHOUSE, 
				targetWarehouse.get("w_street_1"),
				targetWarehouse.get("w_street_2"),
				targetWarehouse.get("w_city"),
				targetWarehouse.get("w_state"),
				targetWarehouse.get("w_zip")));
		
		System.out.println(String.format(MESSAGE_DISTRICT, 
				targetDistrict.get("d_street_1"),
				targetDistrict.get("d_street_2"),
				targetDistrict.get("d_city"),
				targetDistrict.get("d_state"),
				targetDistrict.get("d_zip")));
		
		System.out.println(String.format(MESSAGE_PAYMENT, payment));
	}
	
	private void selectWarehouseDistrict(final int w_id, final int d_id) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.append("w_id", w_id);
		
		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableWarehouseDistrict.find(searchQuery).iterator();
		while(cursor.hasNext()) {
			targetWarehouse = cursor.next();
			targetDistrict = (Document) targetWarehouse.get("" + d_id);
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
			targetCustomer = (Document) cursor.next();
		} 
		cursor.close();
	}
	
	private void updateWarehouseDistrict(final int w_id, final int d_id, final float payment) {
		// Set new w_ytd
		double w_ytd = targetWarehouse.getDouble("w_ytd") + payment;
		BasicDBObject newWarehouse = new BasicDBObject();
		newWarehouse.append("$set", new BasicDBObject().append("w_ytd", w_ytd));
		
		// Set new d_ytd
		double d_ytd = targetDistrict.getDouble("d_ytd") + payment;
		BasicDBObject newDistrict = new BasicDBObject(d_id + ".d_ytd", d_ytd);
		
		// Update
		BasicDBObject searchQuery = new BasicDBObject().append("w_id", w_id); 
		tableWarehouseDistrict.updateOne(searchQuery, newWarehouse);
		tableWarehouseDistrict.updateOne(searchQuery, new BasicDBObject("$set", newDistrict));
	}
	
	private void updateCustomer(final int w_id, final int d_id, 
			final int c_id, final float payment) {			
		// Set new attributes
		double c_balance = targetCustomer.getDouble("c_balance") - payment;
		double c_ytd_payment = targetCustomer.getDouble("c_ytd_payment") + payment;
		int c_payment_cnt = targetCustomer.getInteger("c_payment_cnt") + 1;
		BasicDBObject newCustomer = new BasicDBObject();
		newCustomer.append("c_balance", c_balance);
		newCustomer.append("c_ytd_payment", c_ytd_payment);
		newCustomer.append("c_payment_cnt", c_payment_cnt);

		// Update
		BasicDBObject setQuery = new BasicDBObject();
		setQuery.append("$set", newCustomer);
		BasicDBObject searchQuery = new BasicDBObject().append("c_w_id", w_id)
				.append("c_d_id", d_id).append("c_id", c_id);
		tableCustomer.updateOne(searchQuery, setQuery);
	}
}