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
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class StockLevel {

	private static final String MESSAGE_START = "=============Stock Level=============\nLast '%d' orders in (w_id = %d, d_id = %d)\n";
	private static final String MESSAGE_OUTPUT = "Number of items below stock threshold (%d) : %d\n";
	private static final String TABLE_WAREHOUSE = "warehouse";
	private static final String TABLE_ORDERLINE = "orderline";
	private static final String TABLE_STOCK = "stock";

	//====================================================================================
	// Preparing for session
	//====================================================================================

	private MongoDatabase database;
	private MongoCollection<Document> tableWarehouse;
	private MongoCollection<Document> tableOrderLine;
	private MongoCollection<Document> tableStock;

	public StockLevel(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouse = database.getCollection(TABLE_WAREHOUSE);
		this.tableOrderLine = database.getCollection(TABLE_ORDERLINE);
		this.tableStock = database.getCollection(TABLE_STOCK);
	}

	//====================================================================================
	// Processing for StockLevel transaction
	//====================================================================================

	public void processStockLevel(int w_id, int d_id, int stockThreshold, int numOfLastOrder) {

		int nextOrderID = getNextOrderNum(w_id, d_id);
		int startOrderID = nextOrderID - numOfLastOrder;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		
		try {
			bw.write(String.format(MESSAGE_START, numOfLastOrder, w_id, d_id));
			ArrayList<Document> itemID_ArrayList = getSetOfItemID(d_id, w_id, startOrderID, nextOrderID);
			long count_below_threshold = countItem(itemID_ArrayList, stockThreshold, w_id);
			bw.write(String.format(MESSAGE_OUTPUT, stockThreshold, count_below_threshold));
			bw.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//====================================================================================
	// QUERY: Retrieve next available order number 'D_NEXT_O_ID' for district (W ID,D ID)
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

	//=====================================================================================
	// QUERY: Find items in order-line within [startOrderID,end OrderID) matching (W ID,D ID)
	//=====================================================================================

	private ArrayList<Document> getSetOfItemID(int d_id, int w_id, int startOrderID, int nextOrderID) {

		ArrayList<Document> itemArrayList = tableOrderLine
				.find(and(eq("ol_w_id", w_id), eq("ol_d_id", d_id), gte("ol_o_id", startOrderID), lt("ol_o_id", nextOrderID)))
				.projection(include("ol_i_id"))
				.into(new ArrayList<Document>());

		return itemArrayList;

	}

	//=====================================================================================
	// QUERY: Count the number of items below the given threshold T
	//=====================================================================================

	private long countItem(ArrayList<Document> itemID_ArrayList, int stockThreshold, int w_id) {

		int count = 0;
		int itemID = 0;

		for(int i = 0; i < itemID_ArrayList.size(); i++) {
			itemID = itemID_ArrayList.get(i).getInteger("ol_i_id");
			count += tableStock.count(and(eq("s_w_id", w_id), eq("s_i_id", itemID), lt("s_quantity", stockThreshold)));
		}

		return count;
	}
	
}
