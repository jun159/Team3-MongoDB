package app;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class OrderStatus {
	BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
	private static String TABLE_ORDERORDERLINE ="orderOrderLine";
	private static String TABLE_CUSTOMER ="customer";
	private static final String MESSAGE_CUSTOMER = 
			"Order status: Customer First: %1$s  Middle: %2$s  Last: %3$s Balance: %4$s\n"; //
	private static final String MESSAGE_LASTORDER = 
			"Order status: Customer Last order, order number: %1$s  entry date: %2$s  "
			+ "carrier identification: %3$s \n";
	private static final String MESSAGE_EACHITEM = 
			"Order status: Customer each item, ol_i_id: %1$s ol_supply_w_id: %2$s  "
			+ "ol_quantity: %3$s ol_amount: %4$s ol_delivery_d: %5$s\n";
	
	
	private MongoDatabase database;
	public OrderStatus(MongoDBConnect connect) {
		this.database = connect.getDatabase();
	}
	public void  processOrderStatus(int C_W_ID, int C_D_ID, int C_ID) {
		
		 try{
			 MongoCollection<Document> orderOrderLineTable = database.getCollection(TABLE_ORDERORDERLINE);
		   	 MongoCollection<Document> customerTable = database.getCollection(TABLE_CUSTOMER);
		   		
		   	 //a) find customer Name
		   		BasicDBObject customerSearchQuery = new BasicDBObject();
		   		customerSearchQuery.append("c_w_id", C_W_ID);
		   		customerSearchQuery.append("c_d_id", C_D_ID);
		   		customerSearchQuery.append("c_id", C_ID);
		   		
		   		MongoCursor<Document> customerCursor = customerTable.find(customerSearchQuery).iterator();
		   		
		   		while (customerCursor.hasNext()) {
		   		
		   			Document customerDocument = customerCursor.next();
//		   			System.out.println(String.format(MESSAGE_CUSTOMER,
//		   					customerDocument.getString("c_first"),
//		   					customerDocument.getString("c_middle"),
//		   					customerDocument.getString("c_last")));
		   			
		   			log.write(String.format(MESSAGE_CUSTOMER, 
		   					customerDocument.getString("c_first"),
		   					customerDocument.getString("c_middle"),
		   					customerDocument.getString("c_last"),
		   					customerDocument.getDouble("c_balance")));
		   		

		   		}
		   		
		   		//b) customer last order
		   		BasicDBObject orderSearchQuery = new BasicDBObject();
		   		orderSearchQuery.append("o_w_id", C_W_ID);
		   		orderSearchQuery.append("o_d_id", C_D_ID);
		   		orderSearchQuery.append("o_c_id", C_ID);
		   		
		   		MongoCursor<Document> orderCursor = orderOrderLineTable.find(orderSearchQuery)
		   				.sort(new BasicDBObject("o_id",-1)).limit(1).iterator();
	         
		   	
		   		while (orderCursor.hasNext()) {
		   			
		   			Document orderDocument = orderCursor.next();
		   			
//		   			System.out.println(String.format(MESSAGE_LASTORDER,
//		   					orderDocument.getInteger("o_id") + " ",
//		   					orderDocument.getString("o_entery_d"),
//		   					orderDocument.get("o_carrier_id"))
//		   					);
		   			log.write(String.format(MESSAGE_LASTORDER, 
		   					orderDocument.getInteger("o_id"),
		   					orderDocument.getString("o_entry_d"),
		   					orderDocument.get("o_carrier_id")));
		   		
		   			ArrayList<Document> orderLineDocument = (ArrayList<Document>) orderDocument.get("orderLine");
		   			Iterator<Document> orderLineItor = orderLineDocument.iterator();
		   			while(orderLineItor.hasNext()){
		   				Document orderLineObject = orderLineItor.next();
//		   				System.out.println(String.format(MESSAGE_LASTORDER,
//		   						orderLineObject.getInteger("ol_i_id"),
//		   						orderLineObject.getInteger("ol_supply_w_id"),
//		   						orderLineObject.getInteger("ol_quantity"),
//		   						orderLineObject.getDouble("ol_amount").toString(),
//		   						orderLineObject.getString("ol_delivery_d")));
		   				log.write(String.format(MESSAGE_EACHITEM, 
		   						orderLineObject.getInteger("ol_i_id"),
		   						orderLineObject.getInteger("ol_supply_w_id"),
		   						orderLineObject.getInteger("ol_quantity"),
		   						orderLineObject.getDouble("ol_amount"),
		   						orderLineObject.getString("ol_delivery_d")));
		   				
		   			}
		   			
		   		}
		   		log.flush();
		   		
		 }
		 catch(Exception e){
			 System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		 }
		
		
		
	}
	
	
	
}
