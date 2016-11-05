#!/bin/bash

# The following script allows user to benchmark MongoDB performance
# Enter 4 arguments to select the data to benchmark:
# /benchmark.sh arg0 arg1

# The arguments can have the following values:
#		arg0: Database - 8, 40
#		arg1: Number of clients - 10, 20, 40

# Run app
echo -ne "Compiling project..."
mvn -q install &>/dev/null
mvn -q compile
echo "success"

rm -rf log
mkdir log
echo -ne "Execute $2 clients in background..."

ONE=1
let "NUM_CLIENTS = $2 - $ONE"

for i in `seq 0 $NUM_CLIENTS`; do
mvn -q exec:java -Dexec.mainClass="app.MainDriver" -Dexec.args="$1 $i" 1> log/output$i.log 2> log/error$i.log &
done
wait
echo "completed"
