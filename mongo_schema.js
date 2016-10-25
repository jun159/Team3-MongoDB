conn = new Mongo();
db = conn.getDB("team3");

db.WarehouseDistrict.drop();
db.Order.drop();
db.Customer.drop();
db.StockItem.drop();

db.createCollection("WarehouseDistrict");
db.createCollection("Order");
db.createCollection("Customer");
db.createCollection("StockItem");
print("Collections created.")