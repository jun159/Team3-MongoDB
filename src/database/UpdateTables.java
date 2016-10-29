package database;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class UpdateTables {

	public static void main(String[] args) {
		  try{
		         // To connect to mongodb server
		        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		         // Now connect to your databases
				MongoDatabase db = mongoClient.getDatabase( "team3" );
		        
				//find all district
		        MongoCollection<Document> districtTable = db.getCollection("district");
		        MongoCollection<Document> warehouseTable = db.getCollection("warehouse");       
		        
		        MongoCursor<Document> cursor = districtTable.find().iterator();
		         while (cursor.hasNext()) {
		        	
		        	Document districtObject = cursor.next();
		        	System.out.println(districtObject.getString("d_w_id"));
		        	
		        	BasicDBObject district = new BasicDBObject();
		        	district.put("d_id", districtObject.getInteger("d_id"));
		        	district.put("d_name", districtObject.getString("d_name"));
		        	district.put("d_street_1", districtObject.getString("d_street_1"));
		        	district.put("d_street_2", districtObject.getString("d_street_2"));
		        	district.put("d_city", districtObject.getString("d_city"));
	        		district.put("d_state", districtObject.getString("d_state"));
	        		district.put("d_zip", districtObject.getInteger("d_zip"));
	        		district.put("d_tax", districtObject.getDouble("d_tax"));
	        		district.put("d_ytd", districtObject.getDouble("d_ytd"));
	        		district.put("d_next_o_id", districtObject.getInteger("d_next_o_id"));
		        	  
		        	//find the ware house record
		        	BasicDBObject searchWarehouse = new BasicDBObject();
		        	searchWarehouse.put("w_id", districtObject.getInteger("d_w_id"));
		        	
		        	BasicDBObject updateObj = new BasicDBObject();
		        	updateObj.put("$push", new BasicDBObject("district",district));
		        	
//		        	BasicDBObject updateObj = new BasicDBObject();
//		        	updateObj.put("district", insertData);
		        	warehouseTable.updateOne(searchWarehouse, updateObj);
		        	
//		        	DBCursor cursor2 = warehouseTable.find(searchWarehouse);
//		        	while (cursor2.hasNext()) {
//		        		BasicDBObject warehouseObject = (BasicDBObject) cursor.next();
//		        		warehouseDistrictObject.put("w_id", warehouseObject.getInt("w_id"));
//		        		warehouseDistrictObject.put("w_name", warehouseObject.getString("w_name"));
//		        		warehouseDistrictObject.put("w_street_1", warehouseObject.getString("w_street_1"));
//		        		warehouseDistrictObject.put("w_street_2", warehouseObject.getString("w_street_2"));
//		        		warehouseDistrictObject.put("w_city", warehouseObject.getString("w_city"));
//		        		warehouseDistrictObject.put("w_state", warehouseObject.getString("w_state"));
//		        		warehouseDistrictObject.put("w_zip", warehouseObject.getInt("w_zip"));
//		        		warehouseDistrictObject.put("w_tax", warehouseObject.getDouble("w_tax"));
//		        		warehouseDistrictObject.put("w_ytd", warehouseObject.getDouble("w_ytd"));
//		        		
//		        		warehouseObject.put("district", district);
//		        		
//		        	}
		        	
		         }
		         
		         System.out.println("Connect to database successfully");
		         
		      }catch(Exception e){
		    	  
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		         
		      }

	}

}
