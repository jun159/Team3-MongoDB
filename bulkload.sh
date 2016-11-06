#!/bin/bash

# The following script allows user to retrieve data and transactions
# and load the data into the database by running the following:
# bash bulkload.sh arg0 arg1

# The arguments can have the following values:
#		arg0: Database - 8, 40
#		arg1: Number of clients - 10, 20, 40

# Conditions:
#       Project (Team3-MongoDB) is in home directory
#		cqlsh is within /temp/datastax-ddc-3.9.0/bin directory

declare -r FOLDER_DATA="data"
declare -r FOLDER_D8="D8-data"
declare -r FOLDER_D40="D40-data"

# Create data folder only if not exist
echo -ne "Checking whether data folder exist..."
if [ -d $FOLDER_DATA ]
then
    echo "yes"
else
    mkdir data
    echo "new folder created successfully"
fi

cd data

# Download D8 database if not exist
if [ $1 == 8 ]
then
    echo -ne "Checking whether D8-data exist..."
    if [ -d $FOLDER_D8 ]
    then
        echo "yes"
    else
        echo "no"
        echo "Start downloading D8-data..."
        wget http://www.comp.nus.edu.sg/~cs4224/D8-data.zip
        unzip D8-data.zip
        wget http://www.comp.nus.edu.sg/~cs4224/D8-xact-revised-b.zip
        unzip D8-xact-revised-b.zip
        mv D8-xact-revised-b D8-xact
        echo "D8-data download completed"
    fi
fi

# Download D40 database if not exist
if [ $1 == 40 ]
then
    echo -ne "Checking whether D40-data exist..."
    if [ -d $FOLDER_D40 ]
    then
        echo "yes"
    else
        echo "no"
        echo -ne "Start downloading D40-data..."
        wget http://www.comp.nus.edu.sg/~cs4224/D40-data.zip
        unzip D40-data.zip &>/dev/null
        wget http://www.comp.nus.edu.sg/~cs4224/D40-xact-revised-b.zip
        unzip D40-xact-revised-b.zip
        mv D40-xact-revised-b D40-xact
        echo "D40-data download completed"
    fi
fi

cd

# Bulk load data
echo -ne "\nLoading warehouse, district, customer, order, orderline and stock data into MongoDB..."
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin

./mongo team3 --eval "printjson(db.dropDatabase())"

if [ $1 == 8 ]
then
    bash ~/Team3-MongoDB/mongoimport8.sh
else
    bash ~/Team3-MongoDB/mongoimport40.sh
fi

cd ~/Team3-MongoDB
echo -ne "\nLoading item data into MongoDB.."
mvn -q install &>/dev/null
mvn -q compile &>/dev/null
mvn -q exec:java -Dexec.mainClass="database.UpdateTables" -Dexec.args="$1"
