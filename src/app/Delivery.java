package app;

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
	
	
	public Delivery() {}
	public static void main (String arg[]) {
		Delivery deli = new Delivery();
		deli.processDelivery();
	}
	
	
	public void  processDelivery() {
		   try{
//		          To connect to mongodb server
		        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		         // Now connect to your databases
				DB db = mongoClient.getDB( "team3" );
		         
		        DBCollection table = db.getCollection("warehouseDistrict");
		        BasicDBObject orderSearchQuery = new BasicDBObject();
		        

		        orderSearchQuery.append("district.d_id", 1);
//		         DBCursor cursor = table.find();
		         DBCursor cursor = table.find(orderSearchQuery);
		         
		         
		         
		         while (cursor.hasNext()) {
//		         	System.out.println(cursor.next());
		         	BasicDBObject districtObject = (BasicDBObject) cursor.next();
		        	System.out.println(districtObject.getString("district.d_name"));
		         }
		        
		        
//		        for(int i = 0; i<10; i++){
//			         orderSearchQuery.append("o_w_id", 1);
////			         DBCursor cursor = table.find();
//			         DBCursor cursor = table.find(searchQuery);
//
//			         while (cursor.hasNext()) {
//			         	System.out.println(cursor.next());
////			         	BasicDBObject districtObject = (BasicDBObject) cursor.next();
////			        	System.out.println(districtObject.getString("district"));
//			        	
//			        	
//			         }
//		        }
	
			   	
//			   MongoClient client = new MongoClient("localhost" , 27017);
//			   MongoDatabase database = client.getDatabase("team3");
//			   MongoCollection<Document> table = database.getCollection("warehouseDistrict");
//			   
//			   BasicDBObject searchQuery = new BasicDBObject();
//			   searchQuery.put("district.d_id", 1);
//			   searchQuery.put("w_id", 1);
//			   
//			   MongoCursor<Document> cursor = table.find(searchQuery).iterator();
//			   
//		         while (cursor.hasNext()) {
//		        	 
////		         	System.out.println(cursor.next().get("district"));
//		        	List<Document> districtObject =  (List<Document>) cursor.next().get("district");
//		        	Iterator<Document> doc = districtObject.iterator();
//		        	while(doc.hasNext()){
//		        		Document temp = doc.next();
//		        		if(temp.getInteger("d_id") == 1){
//		        			System.out.println(temp.get("d_ytd"));
//		        		}
//		        	}   		      
//		         }
		         
		         System.out.println("Connect to database successfully");
		      }catch(Exception e){
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      }
	}
	
}
