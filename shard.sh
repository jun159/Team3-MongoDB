echo -ne "\n============================== Beginning sharding process ========================================\n"

cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin

./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.enableSharding("team3")'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.warehouseDistrict", {w_id: 1})'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.customer", { c_w_id: 1, c_d_id: 1, c_id: 1 })'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.orderOrderLine", { o_w_id: 1, o_d_id: 1, o_id: 1 })'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.stockItem", { s_w_id: 1, s_i_id: 1})'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.warehouse", { w_id: 1 })'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.order", { o_w_id: 1, o_d_id: 1, o_id: 1 })'
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.shardCollection("team3.stock", { s_w_id: 1, s_i_id: 1 })'

echo -ne "\n=============================== Starting Balancer ============================================\n\n"

./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.startBalancer()'

echo -ne "\n=============================== Check chunks ============================================\n\n"

./mongo --host xcnd6.comp.nus.edu.sg --port 27017 --eval 'sh.status()'

echo -ne "\n=============================== End of Sharding ============================================\n\n"
