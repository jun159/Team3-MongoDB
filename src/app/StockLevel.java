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

	private static final String MESSAGE_START = "=============Stock Level=============\nLast '%d' orders o_id {'%d' to '%d'} in (w_id = %d, d_id = %d)\n";
	private static final String MESSAGE_OUTPUT = "Number of items below stock threshold (%d) : %d\n";
	private static final String TABLE_WAREHOUSE_DISTRICT = "warehouseDistrict";
	private static final String TABLE_ORDER_ORDERLINE = "orderOrderLine";
	private static final String TABLE_STOCK_ITEM = "stockItem";

	//====================================================================================
	// Preparing for session
	//====================================================================================

	private MongoDatabase database;
	private MongoCollection<Document> tableWarehouseDistrict;
	private MongoCollection<Document> tableOrder_OrderLine;
	private MongoCollection<Document> tableStockItem;

	public StockLevel(MongoDBConnect connect) {
		this.database = connect.getDatabase();
		this.tableWarehouseDistrict = database.getCollection(TABLE_WAREHOUSE_DISTRICT);
		this.tableOrder_OrderLine = database.getCollection(TABLE_ORDER_ORDERLINE);
		this.tableStockItem = database.getCollection(TABLE_STOCK_ITEM);
	}

	//====================================================================================
	// Processing for StockLevel transaction
	//====================================================================================

	@SuppressWarnings("unchecked")
	public void processStockLevel(int w_id, int d_id, int stockThreshold, int numOfLastOrder) {
		int count = 0;
		int ol_i_id = 0;
		int nextOrderID = getNextOrderNum(w_id, d_id);
		int startOrderID = nextOrderID - numOfLastOrder;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		
		try {
			bw.write(String.format(MESSAGE_START, numOfLastOrder, startOrderID, (nextOrderID - 1), w_id, d_id));
			
			List<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(match(and(eq("o_w_id", w_id), eq("o_d_id", d_id), gte("o_id", startOrderID), lt("o_id", nextOrderID))));
			pipeline.add(project(fields(include("orderLine"), excludeId())));
			
			ArrayList<Document> orderArrayList = tableOrder_OrderLine.aggregate(pipeline).into(new ArrayList<Document>());
			
			for(int i = 0; i < orderArrayList.size(); i++) {
				Document order = orderArrayList.get(i);
				ArrayList<Document> orderLine = (ArrayList<Document>) order.get("orderLine");
				for(int j = 0; j < orderLine.size(); j++) {
					Document ol = orderLine.get(j);
					ol_i_id = ol.getInteger("ol_i_id");
					count += tableStockItem.count(and(eq("s_w_id", w_id), eq("s_i_id", ol_i_id), lt("s_quantity", stockThreshold)));
				}	
			}
			
			bw.write(String.format(MESSAGE_OUTPUT, stockThreshold, count));
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
		
		MongoCursor<Document> cursor = tableWarehouseDistrict.aggregate(pipeline).iterator();
		
		while(cursor.hasNext()){
			Document currentDoc = (Document) cursor.next().get("district");
			nextOrderNum = currentDoc.getInteger("d_next_o_id");
		}

		return nextOrderNum;
	}
	
}
