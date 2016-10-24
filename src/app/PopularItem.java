package app;

public class PopularItem {
	private static final String MESSAGE_PROCESSING = "\n********Find the popular items for last %d orders at warehouse district { %d, %d}********\n";
	private static final String MESSAGE_DISTRICT_IDENTIFIER = "District Identifier : w_id = %d, d_id = %d";
	private static final String MESSAGE_NUM_LAST_ORDER = "Number of last orders examined: %d";
	private static final String MESSAGE_ORDER_ID_AND_DATE_TIME = "\nOrder ID: %d, Date and Time: %s";
	private static final String MESSAGE_CUSTOMER_NAME = "Customer Name : %s";
	private static final String MESSAGE_POPULAR_ITEM = "{Item name: %s, OL_Quantity = %s}";
	private static final String MESSAGE_PERCENTAGE_OF_ORDER_CONTAINING_POPULAR_ITEM = "%.2f%% of orders contains item '%s'";


	//====================================================================================
	// MongoDB Queries for Popular Item transactions
	//====================================================================================


	public PopularItem() {

	}

	public void processPopularItem(int w_id, int d_id, int numOfLastOrder) {
		System.out.printf(MESSAGE_PROCESSING, numOfLastOrder, w_id, d_id);
	}

	//=====================================================================================
	// Methods for printing outputs
	//=====================================================================================

	public void printDistrictIdentifierAndNumOfLastOrder(int w_id, int d_id, int numOfLastOrder) {
		System.out.println(String.format(MESSAGE_DISTRICT_IDENTIFIER, w_id, d_id));
		System.out.println(String.format(MESSAGE_NUM_LAST_ORDER, numOfLastOrder));
	}

	public void printOrderDetail(int o_id, String dateTime) {
		System.out.println(String.format(MESSAGE_ORDER_ID_AND_DATE_TIME, o_id, dateTime));
	}

	public void printCustomerName(String name) {
		System.out.println(String.format(MESSAGE_CUSTOMER_NAME, name));
	}

	public void printPopularItem(String itemName, Double max_ol_quantity) {
		System.out.println(String.format(MESSAGE_POPULAR_ITEM, itemName, max_ol_quantity.toString()));
	}

	public void printPercentageOrder(String itemName, float percentage) {
		System.out.println(String.format(MESSAGE_PERCENTAGE_OF_ORDER_CONTAINING_POPULAR_ITEM, percentage, itemName));
	}
}
