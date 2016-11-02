package database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class UpdateTables {
	private MongoClient mongoClient;
	private DB db;

	
	public UpdateTables(){
		// To connect to mongodb server
		this.mongoClient = new MongoClient( "localhost" , 27017 );
		
		// Connect to databases
		this.db = mongoClient.getDB( "team3" );
		
	}
	
	public static void main(String[] args) {
		UpdateTables upData = new UpdateTables();
		upData.updateWareHouse();
		upData.updateStockItem();
		upData.createCustomerIndex();
		System.out.println("Create customer index successfully");
		upData.createOrderIndex();
		System.out.println("Create order index successfully");
		upData.createOrderLineIndex();
		System.out.println("Create orderLine index successfully");
		
        System.out.println("End of the function");
	}
	
	public void updateWareHouse(){
		//create the index
		db.getCollection("warehouse").createIndex(new BasicDBObject("w_id",1));
		
		//find all district
        DBCollection districtTable = db.getCollection("district");
        DBCollection warehouseTable = db.getCollection("warehouse");
        DBCursor cursor = districtTable.find();
        
        while (cursor.hasNext()) {
        	BasicDBObject districtObject = (BasicDBObject) cursor.next();
        	System.out.println(districtObject.getString("d_w_id"));
        	
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
//        	updateObj.put("$push", new BasicDBObject("district",district));
        	updateObj.put("$set", new BasicDBObject(districtObject.getString("d_id"),district));
        	
        	
        	warehouseTable.update(searchWarehouse, updateObj);
         }
        warehouseTable.rename("warehouseDistrict");
	}
	
	public void updateStockItem() {
		//create the index
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
        	System.out.println(itemObject.getString("i_id"));
        	
        	BasicDBObject item = new BasicDBObject();
        	item.append("i_name", itemObject.getString("i_name"));
        	item.append("i_price", itemObject.getDouble("i_price"));
        	item.append("i_im_id", itemObject.getInt("i_im_id"));
        	item.append("i_data", itemObject.getString("i_data"));
//        	item.put("$set", new BasicDBObject().append("i_name", itemObject.getString("i_name")));
//        	item.put("$set", new BasicDBObject().append("i_price", itemObject.getDouble("i_price")));
//        	item.put("$set", new BasicDBObject().append("i_im_id", itemObject.getInt("i_im_id")));
//        	item.put("$set", new BasicDBObject().append("i_data", itemObject.getString("i_data")));
        	
        	
        	//find the ware house record
        	BasicDBObject searchStock = new BasicDBObject();
        	searchStock.append("s_i_id", itemObject.getInt("i_id"));
        	
        	BasicDBObject updateObj = new BasicDBObject();
        	updateObj.append("$set", item);
     
        	stockTable.updateMulti(searchStock, updateObj);
         }
        stockTable.rename("stockItem");
	}
	
	public void createCustomerIndex(){
		db.getCollection("customer").createIndex(new BasicDBObject("c_w_id",1));
		db.getCollection("customer").createIndex(new BasicDBObject("c_d_id",1));
		db.getCollection("customer").createIndex(new BasicDBObject("c_id",1));
	}
	public void createOrderIndex(){
		db.getCollection("orders").createIndex(new BasicDBObject("o_w_id",1));
		db.getCollection("orders").createIndex(new BasicDBObject("o_d_id",1));
		db.getCollection("orders").createIndex(new BasicDBObject("o_id",1));
	}
	public void createOrderLineIndex(){
		db.getCollection("orderline").createIndex(new BasicDBObject("ol_w_id",1));
		db.getCollection("orderline").createIndex(new BasicDBObject("ol_d_id",1));
		db.getCollection("orderline").createIndex(new BasicDBObject("ol_o_id",1));
	}
	
	public void updateOrderOrderLine(){
		//create the index
				db.getCollection("orders").createIndex(new BasicDBObject("o_w_id",1));
				
				//find all
		        DBCollection orderTable = db.getCollection("orders");
		        DBCollection orderLineTable = db.getCollection("orderline");
		        DBCursor cursor = orderLineTable.find();
		        
		        while (cursor.hasNext()) {
		        	BasicDBObject districtObject = (BasicDBObject) cursor.next();
		        	System.out.println(districtObject.getString("d_w_id"));
		        	
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
//		        	updateObj.put("$push", new BasicDBObject("district",district));
		        	updateObj.put("$set", new BasicDBObject(districtObject.getString("d_id"),district));
		        	
		        	
		        	orderTable.update(searchWarehouse, updateObj);
		         }
		        orderTable.rename("xxx");
	}
	
}
