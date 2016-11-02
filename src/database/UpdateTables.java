package database;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class UpdateTables {
	private MongoClient mongoClient;
	private DB db;
	private static BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
	
	public UpdateTables(){
		// To connect to mongodb server
		this.mongoClient = new MongoClient( "localhost" , 27017 );
		
		// Connect to databases
		this.db = mongoClient.getDB( "team3" );
		
	}
	
	public static void main(String[] args) {
	try {
		UpdateTables upData = new UpdateTables();
		upData.updateWareHouse();
		upData.updateStockItem();
		upData.updateCustomer();
		upData.updateOrderLine();
		upData.updateOrder();
		upData.combineOrderOrderLine();
			
		log.write("End of the function\n");
		log.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//combine warehouse district
	public void updateWareHouse(){
		//create the index
		try {
			log.write("warehouseTable creating index.....\n");
			log.flush();
			db.getCollection("warehouse").createIndex(new BasicDBObject("w_id",1));
			
			//find all district
	        DBCollection districtTable = db.getCollection("district");
	        DBCollection warehouseTable = db.getCollection("warehouse");
	        DBCursor cursor = districtTable.find();
	        
	        while (cursor.hasNext()) {
	        	BasicDBObject districtObject = (BasicDBObject) cursor.next();
	        	log.write("wareHouseTable: "+districtObject.getString("d_w_id")+"\n");
	        	log.flush();
	        	BasicDBObject district = new BasicDBObject();
	        	district.put("d_id", districtObject.getInt("d_id"));
	        	district.put("d_name", districtObject.getString("d_name"));
	        	district.put("d_street_1", districtObject.getString("d_street_1"));
	        	district.put("d_street_2", districtObject.getString("d_street_2"));
	        	district.put("d_city", districtObject.getString("d_city"));
	    		district.put("d_state", districtObject.getString("d_state"));
	    		district.put("d_zip", districtObject.getInt("d_zip"));
	    		district.put("d_tax", districtObject.getDouble("d_tax"));
	    		district.put("d_ytd", districtObject.getDouble("d_ytd"));
	    		district.put("d_next_o_id", districtObject.getInt("d_next_o_id"));
	        	  
	        	//find the ware house record
	        	BasicDBObject searchWarehouse = new BasicDBObject();
	        	searchWarehouse.put("w_id", districtObject.getInt("d_w_id"));
	        	
	        	BasicDBObject updateObj = new BasicDBObject();
	        	updateObj.put("$push", new BasicDBObject("district",district));
	//        	updateObj.put("$set", new BasicDBObject(districtObject.getString("d_id"),district));
	        	
	        	
	        	warehouseTable.update(searchWarehouse, updateObj);
	         }
	        warehouseTable.rename("warehouseDistrict");
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//combine stock item table
	public void updateStockItem() {
		//create the index
		try {
			log.write("stockItemTable creating index.....\n");
			log.flush();
			db.getCollection("stock").createIndex(new BasicDBObject("s_i_id",1));
			db.getCollection("stock").createIndex(new BasicDBObject("s_w_id",1));
			
			
			//find all district
	        DBCollection stockTable = db.getCollection("stock");
	        DBCollection itemTable = db.getCollection("item");
	        
	        DBCursor cursor = itemTable.find();
	        //disable timeout
	        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
	        
	        while (cursor.hasNext()) {
	        	BasicDBObject itemObject = (BasicDBObject) cursor.next();
	        	log.write("stockItemTable " + itemObject.getString("i_id")+"\n");
	        	log.flush();
	        	BasicDBObject item = new BasicDBObject();
	        	item.append("i_name", itemObject.getString("i_name"));
	        	item.append("i_price", itemObject.getDouble("i_price"));
	        	item.append("i_im_id", itemObject.getInt("i_im_id"));
	        	item.append("i_data", itemObject.getString("i_data"));
	//        	item.put("$set", new BasicDBObject().append("i_name", itemObject.getString("i_name")));
	//        	item.put("$set", new BasicDBObject().append("i_price", itemObject.getDouble("i_price")));
	//        	item.put("$set", new BasicDBObject().append("i_im_id", itemObject.getInt("i_im_id")));
	//        	item.put("$set", new BasicDBObject().append("i_data", itemObject.getString("i_data")));
	        	
	        	
	        	BasicDBObject searchStock = new BasicDBObject();
	        	searchStock.append("s_i_id", itemObject.getInt("i_id"));
	        	
	        	BasicDBObject updateObj = new BasicDBObject();
	        	updateObj.append("$set", item);
	     
	        	stockTable.updateMulti(searchStock, updateObj);
	         }
	        stockTable.rename("stockItem");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	//Customer: Adding {w_name, d_name}
	public void updateCustomer(){
		//create the index
		try {
		log.write("customerTable creating index.....\n");
		log.flush();
		db.getCollection("customer").createIndex(new BasicDBObject("c_w_id",1));
		db.getCollection("customer").createIndex(new BasicDBObject("c_d_id",1));
		db.getCollection("customer").createIndex(new BasicDBObject("c_id",1));
		
		//find all district
        DBCollection customerTable = db.getCollection("customer");
        DBCollection warehouseDistrictTable = db.getCollection("warehouseDistrict");
        
        DBCursor cursor = warehouseDistrictTable.find();
        //disable timeout
        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
        
        while (cursor.hasNext()) {
        	BasicDBObject itemObject = (BasicDBObject) cursor.next();
        	
        	ArrayList<BasicDBObject> warehouseTemp =  (ArrayList<BasicDBObject>) itemObject.get("district");
        	Iterator<BasicDBObject> it = warehouseTemp.iterator();
        	while(it.hasNext()){
        		BasicDBObject districtObject = it.next();
        		log.write("customerTable d_id "+ districtObject.getString("d_id")+"\n");
        		log.flush();
        		BasicDBObject item = new BasicDBObject();
        		
        		item.append("w_name", itemObject.getString("w_name"));
        		item.append("d_name", districtObject.getString("d_name"));
        		
       
            	BasicDBObject searchCustomer = new BasicDBObject();
            	searchCustomer.append("c_w_id", itemObject.getInt("w_id"));
            	searchCustomer.append("c_d_id", districtObject.getInt("d_id"));
            	
            	
            	BasicDBObject updateObj = new BasicDBObject();
            	updateObj.append("$set", item);
            	
            	customerTable.updateMulti(searchCustomer, updateObj);
        	}     	
         }
	} catch (IOException e) {
		e.printStackTrace();
	}
		
	}

//	OrderLine: Adding {i_name}
	public void updateOrderLine(){
		try {
			log.write("orderLineTable creating index.....\n");
			log.flush();
			db.getCollection("orderline").createIndex(new BasicDBObject("ol_w_id",1));
			db.getCollection("orderline").createIndex(new BasicDBObject("ol_d_id",1));
			db.getCollection("orderline").createIndex(new BasicDBObject("ol_o_id",1));
		
			//Temporary index
			db.getCollection("orderline").createIndex(new BasicDBObject("ol_i_id",1));
			
	        DBCollection itemTable = db.getCollection("item");
	        DBCollection orderLineTable = db.getCollection("orderline");
	        
	        DBCursor cursor = itemTable.find();
	        //disable timeout
	        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
	        
	        while (cursor.hasNext()) {
	        	BasicDBObject itemObject = (BasicDBObject) cursor.next();
	        	log.write("orderLineTable i_id " +itemObject.get("i_id")+"\n");
	        	log.flush();
	    		BasicDBObject item = new BasicDBObject();
	    		
	    		item.append("i_name", itemObject.getString("i_name"));
	    		
	   
	        	BasicDBObject searchOrderLine = new BasicDBObject();
	        	searchOrderLine.append("ol_i_id", itemObject.getInt("i_id"));
	       
	    
	        	BasicDBObject updateObj = new BasicDBObject();
	        	updateObj.append("$set", item);
	        	
	        	orderLineTable.updateMulti(searchOrderLine, updateObj);
	        	     	
	         }
	        
	        //drop the index
	        db.getCollection("orderline").dropIndex("ol_i_id_1");
        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
//	Orders: Adding {c_first, c_middle, c_last}
	public void updateOrder(){
		try {
			log.write("ordersTable creating index.....\n");
			log.flush();
			db.getCollection("orders").createIndex(new BasicDBObject("o_w_id",1));
			db.getCollection("orders").createIndex(new BasicDBObject("o_d_id",1));
			db.getCollection("orders").createIndex(new BasicDBObject("o_id",1));
			
			//Temporary index
			db.getCollection("orders").createIndex(new BasicDBObject("o_c_id",1));
			
	        DBCollection customerTable = db.getCollection("customer");
	        DBCollection ordersTable = db.getCollection("orders");
	        
	        DBCursor cursor = customerTable.find();
	        //disable timeout
	        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
	        
	        while (cursor.hasNext()) {
	        	BasicDBObject itemObject = (BasicDBObject) cursor.next();
	        	log.write("ordertable w_id " +itemObject.get("c_w_id") 
	        			+ " customerID "+itemObject.get("c_id")+"\n");
	        	log.flush();
	    		BasicDBObject item = new BasicDBObject();
	    		item.append("c_first", itemObject.getString("c_first"));
	    		item.append("c_middle", itemObject.getString("c_middle"));
	    		item.append("c_last", itemObject.getString("c_last"));
	   
	        	BasicDBObject searchOrder = new BasicDBObject();
	        	searchOrder.append("o_w_id", itemObject.getInt("c_w_id"));
	        	searchOrder.append("o_d_id", itemObject.getInt("c_d_id"));
	        	searchOrder.append("o_c_id", itemObject.getInt("c_id"));
	        	
	    
	        	BasicDBObject updateObj = new BasicDBObject();
	        	updateObj.append("$set", item);
	        	
	        	ordersTable.updateMulti(searchOrder, updateObj);
	         }
	        //drop the index
	        db.getCollection("orders").dropIndex("o_c_id_1");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	public void combineOrderOrderLine(){
			//create the index
		try {	
			//find all
	        DBCollection orderTable = db.getCollection("orders");
	        DBCollection orderLineTable = db.getCollection("orderline");
	        DBCursor cursor = orderLineTable.find();
	        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
	        
	        while (cursor.hasNext()) {
	        	BasicDBObject orderLineObject = (BasicDBObject) cursor.next();
	        	log.write("combineOrderLineTable: ol_w_id "+ orderLineObject.getString("ol_w_id")
	        			+" ol_o_id " +orderLineObject.getString("ol_o_id")+"\n");
	        	log.flush();
	        	
	        	BasicDBObject orderLine = new BasicDBObject();
	        	orderLine.put("ol_w_id", orderLineObject.getInt("ol_w_id"));
	        	orderLine.put("ol_d_id", orderLineObject.getInt("ol_d_id"));
	        	orderLine.put("ol_o_id", orderLineObject.getInt("ol_o_id"));
	        	orderLine.put("ol_number", orderLineObject.getInt("ol_number"));
	        	orderLine.put("ol_i_id", orderLineObject.getInt("ol_i_id"));
	        	orderLine.put("ol_delivery_d", orderLineObject.getString("ol_delivery_d"));
	        	orderLine.put("ol_amount", orderLineObject.getDouble("ol_amount"));
	        	orderLine.put("ol_supply_w_id", orderLineObject.getInt("ol_supply_w_id"));
	        	orderLine.put("ol_quantity", orderLineObject.getInt("ol_quantity"));
	        	orderLine.put("ol_dist_info", orderLineObject.getString("ol_dist_info"));
	    		
	        	orderLine.put("i_name", orderLineObject.getString("i_name"));
	
	       
	        	BasicDBObject searchOrder = new BasicDBObject();
	        	searchOrder.put("o_w_id", orderLineObject.getInt("ol_w_id"));
	        	searchOrder.put("o_d_id", orderLineObject.getInt("ol_d_id"));
	        	searchOrder.put("o_id", orderLineObject.getInt("ol_o_id"));
	        	
	        	BasicDBObject updateObj = new BasicDBObject();
			    updateObj.put("$push", new BasicDBObject("orderLine",orderLine));
	//        	updateObj.put("$set", new BasicDBObject(districtObject.getString("d_id"),district));
	        	
	        	
	        	orderTable.update(searchOrder, updateObj);
	         }
	        orderTable.rename("orderOrderLine");
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
}