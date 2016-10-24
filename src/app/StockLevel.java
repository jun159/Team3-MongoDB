package app;

public class StockLevel {
	private static final String MESSAGE_ITEM_BELOW_THRESHOLD = "For last '%d' orders in (w_id = %d, d_id = %d), "
			+ "num of items below threshold (%d) : %d\n";

	//====================================================================================
	// MongoDB Queries for StockLevel transactions
	//====================================================================================
	

	//====================================================================================
	// Preparing for session
	//====================================================================================

	public StockLevel() {
		
	}

	//====================================================================================
	// Processing for StockLevel transaction
	//====================================================================================

	public void processStockLevel() {
		
	}
	
	public void printTotalNumBelowThreshold(int numOfLastOrder, int w_id, int d_id, int stockThreshold, long numOfItemBelowThreshold) {
		System.out.println(String.format(MESSAGE_ITEM_BELOW_THRESHOLD, numOfLastOrder, w_id, d_id, stockThreshold, numOfItemBelowThreshold));
	}

}
