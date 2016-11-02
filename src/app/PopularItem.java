package app;

import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class PopularItem {

	private static final String MESSAGE_START = "\nProcessing PopularItem transaction...\n";
	private static final int ZERO = 0;
	private static final String MESSAGE_DISTRICT_ID = "District Identifier : (%d, %d)\n";
	private static final String MESSAGE_LAST_ORDER = "# Last orders examined : %d\n";
	private static final String MESSAGE_OID_DATETIME = "O_ID: %d, Date_Time : %s\n";
	private static final String MESSAGE_CUSTOMER_NAME = "C_NAME : %s\n";
	private static final String MESSAGE_POPULAR_ITEM = "{I_NAME: %s, OL_Quantity = %s}\n";
	private static final String MESSAGE_PERCENTAGE = "%.2f%% of orders contains item '%s'\n";

	private static final String TABLE_WAREHOUSE = "warehouse";
	private static final String TABLE_ORDER = "orders";
	private static final String TABLE_ORDERLINE = "orderline";
	private static final String TABLE_CUSTOMER = "customer";

	// ArrayLists: (1) name of popular item (2) number of orders containing the item.
	private ArrayList<String> distinctItemArrayList;
	private ArrayList<Integer> countOrder;

	//====================================================================================
	// Preparing for session
	//====================================================================================

	private MongoDatabase database;
	private MongoCollection<Document> tableOrder;
	private MongoCollection<Document> tableOrderLine;
	private MongoCollection<Document> tableCustomer;
	private MongoCollection<Document> tableWarehouse;

	public PopularItem(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouse = database.getCollection(TABLE_WAREHOUSE);
		this.tableOrder= database.getCollection(TABLE_ORDER);
		this.tableOrderLine = database.getCollection(TABLE_ORDERLINE);
		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
		distinctItemArrayList = new ArrayList<String>();
		countOrder = new ArrayList<Integer>();
	}

	public void processPopularItem(int w_id, int d_id, int numOfLastOrder) {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

		try {
			bw.write(MESSAGE_START);
			bw.write(String.format(MESSAGE_DISTRICT_ID, w_id, d_id));
			bw.write(String.format(MESSAGE_LAST_ORDER, numOfLastOrder));
			ArrayList<Document> orders = getOrders(w_id, d_id, numOfLastOrder);

			for(int i = 0; i < orders.size(); i++) {
				Document order = orders.get(i);
				int o_id = order.getInteger("o_id");
				bw.write(String.format(MESSAGE_OID_DATETIME, o_id, order.getString("o_entry_d")));
				bw.write(String.format(MESSAGE_CUSTOMER_NAME, getCustomerName(w_id, d_id, order.getInteger("o_c_id"))));

				ArrayList<Document> orderLine = getPopularItem(o_id, w_id, d_id);
				for(int j = 0; j < orderLine.size(); j++) {
					Document ol = orderLine.get(j);
					int i_id = ol.getInteger("ol_i_id");
					bw.write(String.format(MESSAGE_POPULAR_ITEM, i_id, ol.getInteger("ol_quantity")));
					addToDistinctItemArrayList(String.valueOf(i_id));
				}
			}
			
			//====================================================================================
			// Output %order of distinct items
			//====================================================================================
			
			float percentage;

			for(int i = 0; i < distinctItemArrayList.size(); i++) {
				percentage = ( (float) countOrder.get(i) / (float) numOfLastOrder) * 100;
				bw.write(String.format(MESSAGE_PERCENTAGE, percentage, distinctItemArrayList.get(i)));
			}
			
			bw.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	//====================================================================================
	// Track % of order for a distinct item
	//====================================================================================

	private void addToDistinctItemArrayList(String itemName) {
		if(distinctItemArrayList.contains(itemName)) {
			updatePopularItemList(itemName);
		}
		else {
			insertIntoPopularItemList(itemName);
		}
	}

	private void insertIntoPopularItemList(String itemName) {
		distinctItemArrayList.add(itemName);
		countOrder.add(1);
	}

	public void updatePopularItemList(String itemName){
		int index;
		index = distinctItemArrayList.indexOf(itemName);
		countOrder.set(index, countOrder.get(index) + 1);
	}

	//====================================================================================
	// QUERY: Return set of popular items
	//====================================================================================

	private ArrayList<Document> getPopularItem(int o_id, int w_id, int d_id) {

		//======== Get max ol_quantity ============
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(match(and(eq("ol_w_id", w_id), eq("ol_d_id", d_id), eq("ol_o_id", o_id))));
		pipeline.add(project(include("ol_quantity")));
		pipeline.add(sort(descending("ol_quantity")));
		pipeline.add(limit(1));

		ArrayList<Document> max_doc = tableOrderLine.aggregate(pipeline).into(new ArrayList<Document>());
		int max_ol_quantity = max_doc.get(ZERO).getInteger("ol_quantity");

		//======== Get set of items ============
		pipeline = new ArrayList<Bson>();
		pipeline.add(match(and(eq("ol_w_id", w_id), eq("ol_d_id", d_id), eq("ol_o_id", o_id), eq("ol_quantity", max_ol_quantity))));
		pipeline.add(project(fields(include("ol_i_id", "ol_quantity"), excludeId())));

		// Special modifier
		pipeline.add(limit(1));
		// ====================

		ArrayList<Document> items = tableOrderLine.aggregate(pipeline).into(new ArrayList<Document>());

		return items;
	}

	//====================================================================================
	// QUERY: Return customer name
	//====================================================================================

	private String getCustomerName(int w_id, int d_id, int o_c_id) {

		ArrayList<Document> array = tableCustomer
				.find(and(eq("c_w_id", w_id), eq("c_d_id", d_id), eq("c_id", o_c_id)))
				.projection(include("c_first", "c_middle", "c_last"))
				.into(new ArrayList<Document>());
		Document customer = array.get(0);

		return customer.getString("c_first") + " " + customer.getString("c_middle") + " " + customer.getString("c_last");
	}

	//====================================================================================
	// QUERY: Return orders within range [startOrderID, nextOrderID)
	//====================================================================================

	private ArrayList<Document> getOrders(int w_id, int d_id, int numOfLastOrder) {

		int nextOrderID = getNextOrderNum(w_id, d_id);
		int startOrderID = nextOrderID - numOfLastOrder;

		ArrayList<Document> orders = tableOrder
				.find(and(eq("o_w_id", w_id), eq("o_d_id", d_id), gte("o_id", startOrderID), lt("o_id", nextOrderID)))
				.projection(include("o_id", "o_c_id", "o_entry_d"))
				.into(new ArrayList<Document>());

		return orders;
	}

	//====================================================================================
	// QUERY: Return next available order number 'D_NEXT_O_ID' for district (W ID,D ID)
	//====================================================================================

	private int getNextOrderNum(int w_id, int d_id) {

		int nextOrderNum = 0;
		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(match(eq("w_id", w_id)));
		pipeline.add(project(fields(include("district"), excludeId())));
		pipeline.add(unwind("$district"));
		pipeline.add(match(eq("district.d_id", d_id)));
		pipeline.add(project(fields(include("district.d_next_o_id"), excludeId())));

		MongoCursor<Document> cursor = tableWarehouse.aggregate(pipeline).iterator();

		while(cursor.hasNext()){
			Document currentDoc = (Document) cursor.next().get("district");
			nextOrderNum = currentDoc.getInteger("d_next_o_id");
		}

		return nextOrderNum;
	}

}
