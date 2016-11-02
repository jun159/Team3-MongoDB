/* Author: Luah Bao Jun
 * ID: A0126258A
 * Team: 3
 */

package app;

import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class NewOrder {
	
	private static final String MESSAGE_CUSTOMER = "Customer information: ID(%1$s, %2$s, %3$s), LastName(%4$s), "
			+ "Credit(%5$s), Discount(%6$s)";
	private static final String MESSAGE_WAREHOUSE = "Warehouse tax rate: %1$s";
	private static final String MESSAGE_DISTRICT = "District tax rate: %1$s";
	private static final String MESSAGE_ORDER = "Order: OrderNumber(%1$s), EntryDate(%2$s)";
	private static final String MESSAGE_NUM_ITEMS = "Number of items: %1$s";
	private static final String MESSAGE_TOTAL_AMOUNT = "Total amount: %1$s";
	private static final String MESSAGE_ORDER_ITEM = "Order item: ItemNumber(%1$s), ItemName(%2$s), "
			+ "Warehouse(%3$s), Quantity(%4$s), Amount(%5$s), TotalQuantity(%6$s)";
	
	private static final String TABLE_WAREHOUSE = "warehouse";
	private static final String TABLE_CUSTOMER = "customer";
	private static final String TABLE_ORDER = "orders";
	private static final String TABLE_ORDERLINE = "orderline";
	private static final String TABLE_STOCK = "stock";
	private static final String TABLE_ITEM = "item";
	
	private static final boolean DEBUG = true;
	
	private MongoDatabase database;
	private MongoCollection<Document> tableWarehouseDistrict;
	private MongoCollection<Document> tableCustomer;
	private MongoCollection<Document> tableOrder;
	private MongoCollection<Document> tableOrderline;
	private MongoCollection<Document> tableStock;
	private MongoCollection<Document> tableItem;
	private Document targetWarehouse;
	private Document targetDistrict;
	private Document targetCustomer;
	private Document targetStock;
	private Document targetItem;
	private double total_amount = 0;
	private Date o_entry_id;
	private int quantity = 0;
	
	public NewOrder(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouseDistrict = database.getCollection(TABLE_WAREHOUSE);
		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
		this.tableOrder = database.getCollection(TABLE_ORDER);
		this.tableOrderline = database.getCollection(TABLE_ORDERLINE);
		this.tableStock = database.getCollection(TABLE_STOCK);
		this.tableItem = database.getCollection(TABLE_ITEM);
	}
	
	public void processNewOrder(final int w_id, final int d_id, final int c_id, 
			final float num_items, final int[] item_number, 
			final int[] supplier_warehouse, final int[] quantity) {
		selectWarehouseDistrict(w_id, d_id);
		selectCustomer(w_id, d_id, c_id);
		updateWarehouseDistrict(w_id, d_id);
		
		if(DEBUG) {
			System.out.println("Before d:  " + targetDistrict.get("d_next_o_id"));
			selectWarehouseDistrict(w_id, d_id);
			System.out.println("After d:   " + targetDistrict.get("d_next_o_id"));
		}
		
		insertOrder(w_id, d_id, c_id, num_items, supplier_warehouse);
		insertOrderLines(w_id, d_id, num_items, item_number, supplier_warehouse, quantity);
		computeTotal();
		outputResults(num_items);
	}
	
	private void outputResults(float num_items) {
		System.out.println(String.format(MESSAGE_CUSTOMER, 
				targetCustomer.getInteger("c_w_id"),
				targetCustomer.getInteger("c_d_id"),
				targetCustomer.getInteger("c_id"),
				targetCustomer.getString("c_last"),
				targetCustomer.getString("c_credit"),
				targetCustomer.getDouble("c_discount")));
				
		System.out.println(String.format(MESSAGE_WAREHOUSE, 
				targetWarehouse.getDouble("w_tax")));
				
		System.out.println(String.format(MESSAGE_DISTRICT, 
				targetDistrict.getDouble("d_tax")));
		
		System.out.println(String.format(MESSAGE_ORDER, 
				targetDistrict.getDouble("d_next_o_id"),
				o_entry_id));
										
		System.out.println(String.format(MESSAGE_NUM_ITEMS,
				num_items));
												
		System.out.println(String.format(MESSAGE_TOTAL_AMOUNT,
				total_amount));
	}
	
	private void selectWarehouseDistrict(final int w_id, final int d_id) {
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.append("w_id", w_id);
		
		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableWarehouseDistrict.find(searchQuery).iterator();
		while(cursor.hasNext()) {
			targetWarehouse = cursor.next();
			ArrayList<Document> districtList = (ArrayList<Document>) targetWarehouse.get("district");
			targetDistrict = districtList.get(d_id - 1);
			System.out.println(targetDistrict.toJson());
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
//			System.out.println("customer: " + targetCustomer.getInteger("c_id"));
		} 
		cursor.close();
	}
	
	private void selectStock(final int w_id, final int i_id) {				
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("s_w_id", w_id);
		searchQuery.put("s_i_id", i_id);

		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableStock.find(searchQuery).iterator();
		if(cursor.hasNext()) {
			targetStock = (Document) cursor.next();
//			System.out.println("stock: " + targetStock.getInteger("s_w_id"));
		} 
		cursor.close();
	}
	
	private void selectItem(final int i_id) {			
		// Where clause
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("i_id", i_id);

		// Retrieve rows from table that satisfy where clause
		MongoCursor<Document> cursor = this.tableItem.find(searchQuery).iterator();
		if(cursor.hasNext()) {
			targetItem = (Document) cursor.next();
//			System.out.println("item: " + targetItem.getInteger("i_id"));
		} 
		cursor.close();
	}
	
	private void updateWarehouseDistrict(final int w_id, final int d_id) {
		// Set new d_next_o_id
		double d_next_o_id = targetDistrict.getDouble("d_next_o_id") + 1;
		BasicDBObject setDistrict = new BasicDBObject();
		setDistrict.append("district." + (d_id - 1) + ".d_next_o_id", d_next_o_id);
		BasicDBObject newDistrict = new BasicDBObject("$set", setDistrict);
		
		// Update
		BasicDBObject searchQuery = new BasicDBObject().append("w_id", w_id); 
		tableWarehouseDistrict.updateOne(searchQuery, newDistrict);
	}
	
	private void updateStock(final int w_id, final int d_id, final int i_id, final int warehouse, final int quantity) {	
		int s_quantity = targetStock.getInteger("s_quantity") - quantity;
		if(s_quantity == 10) {
			s_quantity += 100;
		}
		
		double s_ytd = targetStock.getDouble("s_ytd") + quantity;
		int s_order_cnt = targetStock.getInteger("s_order_cnt") + 1;
		
		int s_remote_cnt = 0;
		if(warehouse != w_id) {
			s_remote_cnt = 1;
		}
		
		// Set new attributes
		BasicDBObject newStock = new BasicDBObject();
		newStock.append("s_quantity", s_quantity);
		newStock.append("s_ytd", s_ytd);
		newStock.append("s_order_cnt", s_order_cnt);
		newStock.append("s_remote_cnt", s_remote_cnt);

		// Update
		BasicDBObject setQuery = new BasicDBObject();
		setQuery.append("$set", newStock);
		BasicDBObject searchQuery = new BasicDBObject().append("s_w_id", warehouse)
				.append("s_i_id", i_id);
		tableCustomer.updateOne(searchQuery, setQuery);
		
		this.quantity = s_quantity;
	}
	
	private void insertOrder(final int w_id, final int d_id, 
			final int c_id, final float num_items, final int[] supplier_warehouse) {
		
		o_entry_id = new Date();
		double o_id = targetDistrict.getDouble("d_next_o_id");
		double o_all_local = 1;
		double o_ol_cnt = num_items;
		
		for (int id : supplier_warehouse) {
			if (w_id != id) {
				o_all_local = 0;
				break;
			}
		}
		
		Document document = new Document();
		document.put("o_w_id", w_id);
		document.put("o_d_id", d_id);
		document.put("o_id", o_id);
		document.put("o_c_id", c_id);
		document.put("o_carrier_id", null);
		document.put("o_ol_cnt", o_ol_cnt);
		document.put("o_all_local", o_all_local);
		document.put("o_entry_d", o_entry_id);

		tableOrder.insertOne(document);
	}
	
	private void insertOrderLines(final int w_id, final int d_id,  
			final float num_items, final int[] item_number, 
			final int[] supplier_warehouse, final int[] quantity) {
		
		double o_id = targetDistrict.getDouble("d_next_o_id");
		
		for(int i = 0; i < num_items; i++) {
			selectStock(w_id, d_id);
			updateStock(w_id, d_id, item_number[i], supplier_warehouse[i], quantity[i]);
			selectItem(item_number[i]);
			double item_price = targetItem.getDouble("i_price");
			double item_amount = quantity[i] * item_price;		
			total_amount = total_amount + item_amount;
			String d_dist_id = String.format("s_dist_%1$s", String.format("%02d", d_id));
			
			Document document = new Document();
			document.put("ol_w_id", w_id);
			document.put("ol_d_id", d_id);
			document.put("ol_o_id", o_id);
			document.put("ol_number", i);
			document.put("ol_i_id", item_number[i]);
			document.put("ol_delivery_d", null);
			document.put("ol_amount", item_amount);
			document.put("ol_supply_w_id", supplier_warehouse[i]);
			document.put("ol_quantity",  quantity[i]);
			document.put("ol_dist_info", targetStock.getString(d_dist_id));

			tableOrderline.insertOne(document);
			
			System.out.println(String.format(MESSAGE_ORDER_ITEM, 
					item_number[i],
					targetItem.getString("i_name"),
					supplier_warehouse[i],
					quantity[i],
					item_amount,
					this.quantity));
		}
	}
	
	private void computeTotal() {
		double w_tax = targetWarehouse.getDouble("w_tax");
		double d_tax = targetDistrict.getDouble("d_tax");
		double c_discount = targetCustomer.getDouble("c_discount");
		total_amount = total_amount * (1 + d_tax + w_tax) * (1 - c_discount);
	}
}