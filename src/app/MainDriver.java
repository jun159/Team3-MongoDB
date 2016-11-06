package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class MainDriver {
	
	private static final String MESSAGE_ERROR_ARGS = "Error: Please provide 2 arguments (database, numClients)";
	private static final String MESSAGE_ERROR_READ = "Error: Unable to read the transaction files. Please try again";
	private static final String MESSAGE_XACT_SIZE = "Total number of transactions processed: ";
	private static final String MESSAGE_ELAPSED_TIME = "Total elapsed time for processing the transactions: ";
	private static final String MESSAGE_THROUGHPUT = "Total transaction throughput: ";
	private static final String PATH_XACT = "./data/D%1$s-xact/%2$s.txt";
	
	private static final char XACT_NEWORDER = 'N';
	private static final char XACT_PAYMENT = 'P';
	private static final char XACT_DELIVERY = 'D';
	private static final char XACT_ORDERSTATUS = 'O';
	private static final char XACT_STOCKLEVEL = 'S';
	private static final char XACT_POPULARITEM = 'I';
	private static final char XACT_TOPBALANCE = 'T';
	
	private MongoDBConnect connect;
	private NewOrder newOrderXact;
	private Payment paymentXact;
	private Delivery deliveryXact;
	private OrderStatus orderStatusXact;
	private StockLevel stockLevelXact;
	private PopularItem popularItemXact;
	private TopBalance topBalanceXact;
	private String database;
	private String xactID;
	private int numTransactions;
	private long startTime;
	
	public MainDriver(String database, String xactID) {
		this.connect = new MongoDBConnect("127.0.0.1", 27017, "team3");
		this.newOrderXact = new NewOrder(connect);
		this.paymentXact = new Payment(connect);
		this.deliveryXact = new Delivery(connect);
		this.orderStatusXact = new OrderStatus(connect);
		this.stockLevelXact = new StockLevel(connect);
		this.popularItemXact = new PopularItem(connect);
		this.topBalanceXact = new TopBalance(connect);
		this.database = database;
		this.xactID = xactID;
		this.numTransactions = 0;
		this.startTime = 0;
	}
	
	public void executeQueries() {
		startTime = System.currentTimeMillis();	
		String path = String.format(PATH_XACT, database, xactID);
		File file = new File(path);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String[] args = line.split(",");
				
				switch(line.charAt(0)) {
					case XACT_NEWORDER:
						runNewOrderXact(args, br);
						break;
		            case XACT_PAYMENT:
		               	runPaymentXact(args);
		                break;
		            case XACT_DELIVERY:
		                runDeliveryXact(args);
		                break;
		            case XACT_ORDERSTATUS:
		               	runOrderStatusXact(args);
		               	break;
		            case XACT_STOCKLEVEL:
		               	runStockLevelXact(args);
		               	break;
		            case XACT_POPULARITEM:
		               	runPopularItemXact(args);
		               	break;
		            case XACT_TOPBALANCE:
		                	runTopBalanceXact(Integer.parseInt(database));
		                	break;
	                }
	                
	                numTransactions++;
	            }
	            
	            br.close();
			} catch(IOException e) {
				System.err.println(MESSAGE_ERROR_READ);
				e.printStackTrace();
			}
			
		connect.close();
	}
	
	private void runNewOrderXact(String[] args, BufferedReader br) {
		int c_id = Integer.parseInt(args[1]);
        int w_id = Integer.parseInt(args[2]);
        int d_id = Integer.parseInt(args[3]);
        int num_items = Integer.parseInt(args[4]);
        int[] item_number = new int[num_items];
        int[] supplier_warehouse = new int[num_items];
        int[] quantity = new int[num_items];
        
        try {
	        for (int i = 0; i < num_items; i++) {
	        	String[] items = br.readLine().split(",");
	            item_number[i] = Integer.parseInt(items[0]);
	            supplier_warehouse[i] = Integer.parseInt(items[1]);
	            quantity[i] = Integer.parseInt(items[2]);
	        }
        } catch(IOException e) {
        	System.err.println(MESSAGE_ERROR_READ);
        }
        
        newOrderXact.processNewOrder(w_id, d_id, c_id, num_items, item_number, 
        		supplier_warehouse, quantity);
	}
	
	private void runPaymentXact(String[] args) {
		int w_id = Integer.parseInt(args[1]);
		int d_id = Integer.parseInt(args[2]);
		int c_id = Integer.parseInt(args[3]);
		float payment = Float.parseFloat(args[4]);
		
		paymentXact.processPayment(w_id, d_id, c_id, payment);
	}
	
	private void runDeliveryXact(String[] args) {
		int w_id = Integer.parseInt(args[1]);
		int carrier_id = Integer.parseInt(args[2]);
		
		deliveryXact.processDelivery(w_id, carrier_id);
	}
	
	private void runOrderStatusXact(String[] args) {
		int w_id = Integer.parseInt(args[1]);
		int d_id = Integer.parseInt(args[2]);
		int c_id = Integer.parseInt(args[3]);
		
		orderStatusXact.processOrderStatus(w_id, d_id, c_id);
	}
	
	private void runStockLevelXact(String[] args) {
		int w_id = Integer.parseInt(args[1]);
		int d_id = Integer.parseInt(args[2]);
		int stockThreshold = Integer.parseInt(args[3]);
		int numOfLastOrder = Integer.parseInt(args[4]);
		
		stockLevelXact.processStockLevel(w_id, d_id, stockThreshold, numOfLastOrder);
	}
	
	private void runPopularItemXact(String[] args) {
		int w_id = Integer.parseInt(args[1]);
		int d_id = Integer.parseInt(args[2]);
		int numOfLastOrder = Integer.parseInt(args[3]);
		
		popularItemXact.processPopularItem(w_id, d_id, numOfLastOrder);
	}
	
	private void runTopBalanceXact(int database) {
		topBalanceXact.processTopBalance();
	}
	
	private void outputResults() {
		long endTime = System.currentTimeMillis();
		long totalTime = (endTime - startTime) / 1000;
		long throughput = numTransactions / totalTime;
		System.out.println(MESSAGE_XACT_SIZE + numTransactions);
		System.out.println(MESSAGE_ELAPSED_TIME + totalTime + " s");
		System.out.println(MESSAGE_THROUGHPUT + throughput + "/s");
	}
	
	public static void main(String[] args) {
		if(args.length == 2) {
			MainDriver mainDriver = new MainDriver(args[0], args[1]);
			mainDriver.executeQueries();
			mainDriver.outputResults();
		} else {
			System.err.println(MESSAGE_ERROR_ARGS);
		}

//		Scanner sc = new Scanner(System.in);
//		System.out.print("Enter database: ");	
//		String data = sc.nextLine();
//		System.out.print("Enter file number: ");
//		String fileNum = sc.nextLine();
//		sc.close();
//		MainDriver mainDriver = new MainDriver(data, fileNum);
//		mainDriver.executeQueries();
//		mainDriver.outputResults();
	}
}
