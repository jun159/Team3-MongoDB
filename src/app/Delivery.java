package app;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;















import javax.naming.spi.DirStateFactory.Result;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Delivery {
	
	private static MongoDBConnect connect;
	
//	private PreparedStatement orderSelect;
//	private PreparedStatement orderSelect_o_c_id;	
//	private PreparedStatement orderUpdate;
//	private PreparedStatement orderLineSelect_ol_number;
//	private PreparedStatement orderLineUpdate;
//	private PreparedStatement orderLineSelect;
//	private PreparedStatement customerSelect;
//	private PreparedStatement customerUpdate;
	private MongoDatabase database;
	private MongoCollection<Document> tableWarehouseDistrict;
	private MongoCollection<Document> tableCustomer;
	private static String TABLE_WAREHOUSEDISTRICT ="warehouseDistrict";
	private static String TABLE_ORDERORDERLINE ="orderOrderLine";
	private static String TABLE_CUSTOMER ="customer";
	
	public Delivery(MongoDBConnect connect) {
		this.database = connect.getDatabase();
//		this.tableWarehouseDistrict = database.getCollection(TABLE_WAREHOUSEDISTRICT);
//		this.tableCustomer = database.getCollection(TABLE_CUSTOMER);
	}
	public static void main (String arg[]) {
//		Delivery deli = new Delivery();
		
	}
	
	
	public void  processDelivery(int W_ID, int CARRIER_ID) {
		   try{
//			   database.getCollection("orderOrderLine").createIndex(new BasicDBObject("o_carrier_id",1));
			   
			   for(int districtNO = 1;districtNO<11;districtNO++){
				   
			   		MongoCollection<Document> orderOrderLineTable = database.getCollection(TABLE_ORDERORDERLINE);
			   		MongoCollection<Document> customerTable = database.getCollection(TABLE_CUSTOMER);
			   		
		        
			   		BasicDBObject orderSearchQuery = new BasicDBObject();
			   		orderSearchQuery.append("o_w_id", W_ID);
			   		orderSearchQuery.append("o_d_id", districtNO);
			   		orderSearchQuery.append("o_carrier_id", "null");
			   		
			   		//a) find the smallest o_id
			   		MongoCursor<Document> cursor = orderOrderLineTable.find(orderSearchQuery)
			   				.sort(new BasicDBObject("o_id",1)).limit(1).iterator();
		         
			   		int smallestID = 0;
			   		int orderLineNumber =0;
			   		Double sum_OL_AMOUNT = 0.0;
			   		int customerID = 0;
			   		while (cursor.hasNext()) {
			   			Document orderDocument = cursor.next();
			   			smallestID = orderDocument.getInteger("o_id");
			   			customerID = orderDocument.getInteger("o_c_id");
			   			ArrayList<Document> count = (ArrayList<Document>) orderDocument.get("orderLine");
			   			orderLineNumber = count.size();
			   			Iterator<Document> orderLineItor = count.iterator();
			   			while(orderLineItor.hasNext()){
			   				sum_OL_AMOUNT += orderLineItor.next().getDouble("ol_amount");
			   			}
			   		}
			   		
//			   		System.out.println("smallestID "+smallestID);
//			   		System.out.println("o_carrier_id "+CARRIER_ID);
//			   		System.out.println("sum_OL_AMOUNT "+sum_OL_AMOUNT);
//			   		System.out.println("customerID "+customerID);
//			   		System.out.println(" ");
			   		//b) update the order and oderLine
			   		BasicDBObject  searchOrder = new BasicDBObject();
			   		searchOrder.append("o_w_id",W_ID );
			   		searchOrder.append("o_d_id",districtNO );
			   		searchOrder.append("o_id", smallestID);
			   		
			   		
			   		Date now = new Date();
			   		BasicDBObject  orderItem = new BasicDBObject();
			   		orderItem.append("o_carrier_id",CARRIER_ID);
			   		for(int i=0; i<orderLineNumber;i++){
			   			orderItem.append("orderLine."+i+".ol_delivery_d",now);
			   		}
			   		
			   		BasicDBObject  updateObj = new BasicDBObject ();
			   		updateObj.append("$set", orderItem);
		        	
		        	orderOrderLineTable.updateOne(searchOrder, updateObj);
		        	
		        	
		        	//d)Update customer c_balance and c_delivery_cnt
		        	
		        	BasicDBObject  searchCustomer = new BasicDBObject();
		        	searchCustomer.append("c_w_id",W_ID );
		        	searchCustomer.append("c_d_id",districtNO );
		        	searchCustomer.append("c_id",customerID );
			   		
			   		BasicDBObject  customerItem = new BasicDBObject ();
			   		customerItem.append("c_balance", sum_OL_AMOUNT);
			   		customerItem.append("c_delivery_cnt", 1);
//			   		customerItem.append("$inc", new BasicDBObject().append("c_balance", sum_OL_AMOUNT));
//			   		customerItem.append("$inc", new BasicDBObject().append("c_delivery_cnt", 1));
			   		
			   		
			   		BasicDBObject  customerUpdateObj = new BasicDBObject ();
			   		customerUpdateObj.append("$inc", customerItem);
		        	
		        	
			   		customerTable.updateOne(searchCustomer, customerUpdateObj);
		        	
			   	}
		    
			   
		      }catch(Exception e){
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      }
	}
	
}
