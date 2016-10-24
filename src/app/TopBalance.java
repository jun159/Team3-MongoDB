package app;

public class TopBalance {

	private static final String MESSAGE_CUSTOMER_NAME = "Name of Customer: %s\n";
	private static final String MESSAGE_CUSTOMER_BALANCE = "Customer Balance: %2f\n";
	private static final String MESSAGE_WAREHOUSE_NAME_OF_CUSTOMER = "Warehouse Name of Customer: %s\n";
	private static final String MESSAGE_DISTRICT_NAME_OF_CUSTOMER = "District Name of Customer: %s\n";
	
	//====================================================================================
	// MongoDB Queries for TopBalance transaction
	//====================================================================================
	
	@SuppressWarnings("unused")
	private static final String CUSTOMER_BALANCE_SORT = 
								"team3.customer"
								+ ".find({c_first, c_middle, c_last, c_balance, c_w_id, c_d_id})"
								+ ".sort(\"c_balance: -1\") ";
	
	@SuppressWarnings("unused")
	private static final String WAREHOUSE_DISTRICT_NAME = 
								"team3.warehousedistrict"
								+ ".find({w_name, district.d_name: ?})";
	
	public TopBalance(){
		
	}
	
	public void processTopBalance(){
		
	}
	

	private void printCustomerName(String customerName) {
		System.out.printf(MESSAGE_CUSTOMER_NAME, customerName);
	}

	private void printCustomerBalance(double d) {
		System.out.printf(MESSAGE_CUSTOMER_BALANCE, d);
	}

	private void printWarehouseNameOfCustomer(String warehouseName) {
		System.out.printf(MESSAGE_WAREHOUSE_NAME_OF_CUSTOMER, warehouseName);
	}

	private void printDistrictNameOfCustomer(String districtName) {
		System.out.printf(MESSAGE_DISTRICT_NAME_OF_CUSTOMER, districtName);
	}
	
}
