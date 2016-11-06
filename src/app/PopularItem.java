package app;

import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class PopularItem {

	private static final String MESSAGE_START = "\nProcessing PopularItem transaction...\n";
	private static final String MESSAGE_DISTRICT_ID = "District Identifier : (%d, %d)\n";
	private static final String MESSAGE_LAST_ORDER = "# Last orders examined : %d\n";
	private static final String MESSAGE_OID_DATETIME = "\nO_ID: %d, Date_Time : %s\n";
	private static final String MESSAGE_C_NAME = "C_NAME : %s\n";
	private static final String MESSAGE_POPULAR_ITEM = "{I_NAME: %s, OL_Quantity = %s}\n";
	private static final String MESSAGE_PERCENTAGE = "%.2f%% of orders contains item '%s'\n";

	private static final String TABLE_WAREHOUSE = "warehouseDistrict";
	private static final String TABLE_ORDER_ORDERLINE = "orderOrderLine";

	// ArrayLists: (1) name of popular item (2) number of orders containing the item.
	private ArrayList<String> distinctItemArrayList;
	private ArrayList<Integer> countOrder;

	//====================================================================================
	// Preparing for session
	//====================================================================================

	private MongoDatabase database;
	private MongoCollection<Document> tableOrder_OrderLine;
	private MongoCollection<Document> tableWarehouseDistrict;

	public PopularItem(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouseDistrict = database.getCollection(TABLE_WAREHOUSE);
		this.tableOrder_OrderLine = database.getCollection(TABLE_ORDER_ORDERLINE);
		distinctItemArrayList = new ArrayList<String>();
		countOrder = new ArrayList<Integer>();
	}

	public void processPopularItem(int w_id, int d_id, int numOfLastOrder) {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		String i_name = "";
		int o_id = 0, max_q = 0, ol_q = 0;
		distinctItemArrayList = new ArrayList<String>();
		countOrder = new ArrayList<Integer>();
		
		try {
			bw.write(MESSAGE_START);
			bw.write(String.format(MESSAGE_DISTRICT_ID, w_id, d_id));
			bw.write(String.format(MESSAGE_LAST_ORDER, numOfLastOrder));
			
			int nextOrderID = getNextOrderNum(w_id, d_id);
			int startOrderID = nextOrderID - numOfLastOrder;
			
			List<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(match(and(eq("o_w_id", w_id), eq("o_d_id", d_id), gte("o_id", startOrderID), lt("o_id", nextOrderID))));
			pipeline.add(project(fields(include("o_id", "o_c_id", "o_entry_d", "c_first", "c_middle", "c_last", "orderLine"), excludeId())));
			
			ArrayList<Document> orderArrayList = tableOrder_OrderLine.aggregate(pipeline).into(new ArrayList<Document>());
		
			for(int i = 0; i < orderArrayList.size(); i++) {
				Document order = orderArrayList.get(i);
				o_id = order.getInteger("o_id");
				bw.write(String.format(MESSAGE_OID_DATETIME, o_id, order.get("o_entry_d")));
				bw.write(String.format(MESSAGE_C_NAME, order.getString("c_first") + " " + order.getString("c_middle") + " " + order.getString("c_last")));
				
				@SuppressWarnings("unchecked")
				// Finding popular items in orderLine
				
				ArrayList<Document> orderLine = (ArrayList<Document>) order.get("orderLine");
				Collections.sort(orderLine, new Comparator<Document>() {
				    public int compare(Document o1, Document o2) {
				        return (o2.getInteger("ol_quantity") - o1.getInteger("ol_quantity"));
				    }
				});
				
				for(int j = 0; j < orderLine.size(); j++) {
					Document ol = orderLine.get(j);
					ol_q = ol.getInteger("ol_quantity");
					i_name = ol.getString("i_name");
					
					if(j == 0) {
						max_q = ol.getInteger("ol_quantity");
					}
					
					if(max_q > ol_q){
						break;
					}
					
					bw.write(String.format(MESSAGE_POPULAR_ITEM, i_name, ol_q));
					addToDistinctItemArrayList(String.valueOf(i_name));
				}
				
			}
			
			// Output %order of distinct items			
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
	// QUERY: Return next available order number 'D_NEXT_O_ID' for district (W_ID,D_ID)
	//====================================================================================

	private int getNextOrderNum(int w_id, int d_id) {

		int nextOrderNum = 0;
		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(match(eq("w_id", w_id)));
		pipeline.add(project(fields(include("district"), excludeId())));
		pipeline.add(unwind("$district"));
		pipeline.add(match(eq("district.d_id", d_id)));
		pipeline.add(project(fields(include("district.d_next_o_id"), excludeId())));

		MongoCursor<Document> cursor = tableWarehouseDistrict.aggregate(pipeline).iterator();

		while(cursor.hasNext()){
			Document currentDoc = (Document) cursor.next().get("district");
			nextOrderNum = currentDoc.getInteger("d_next_o_id");
		}

		return nextOrderNum;
	}

}
