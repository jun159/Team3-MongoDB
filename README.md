# Team3-MongoDB


## Introduction
MongoDB benchmarking measures the performance of different data modeling with different set of nodes and clients. With comparison of different data modeling, this allows us to find out the optimized database schema design for MongoDB.

## Instructions
### 1. Install MongoDB RHEL 7 (>=3.2.0) in temp folder
```
cd /temp
wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel70-3.2.10.tgz
tar zxvf mongodb-linux-x86_64-rhel70-3.2.10.tgz
```

### 2. Install Maven(>=3.3.9) in temp folder
```
cd /temp
wget http://download.nus.edu.sg/mirror/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar xzvf apache-maven-3.3.9-bin.tar.gz
```
### 3. Create / Configure .bash_profile file
```
cd
vim .bash_profile
===================================================
# Add these lines into .bash_profile file:
export PATH=/temp/apache-maven-3.3.9/bin:$PATH
export LANG=en_US.utf-8
export LC_ALL=en_US.utf-8
===================================================
shift + z + z
source .bash_profile
```
### 4. Starting MongoDB server using screen
```
=================================================================
# When starting server for first time, create a data folder in /temp:
mkdir /temp/data
=================================================================
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
screen
./mongod --dbpath /temp/data
ctrl+A, D // Exit the screen
```
### 5. Configuration for three shards
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
mkdir /mongo-metadata
mongod --configsvr --dbpath /mongo-metadata --port 27019
# Stop the mongo server
===================================================================
killall -9 mongo
killall -9 mongod
===================================================================
# Use the ip addresses of three query servers
===================================================================
mongos --configdb [IP address 1],[IP address 2],[IP address 3]
===================================================================
```
Use the same command to start each query server.

### 6. Project directory
Before running the scripts, make sure the project folder is uploaded into the home folder. Change directory to the project folder to prepare for benchmarking.
```
cd Team3-MongoDB 
```
### 7. Bulkload data
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 1 node, run `bash bulkload.sh 8 1`. </br>
b) D8 datasets with 3 nodes, run `bash bulkload.sh 8 3`. </br>
c) D40 datasets with 1 node, run `bash bulkload.sh 40 1`. </br>
d) D40 datasets with 3 nodes, run `bash bulkload.sh 40 3`. 

### 8. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 10 clients, run `bash benchmark.sh 8 10`.</br>
b) D8 datasets with 20 clients, run `bash benchmark.sh 8 20`.</br>
c) D8 datasets with 40 clients, run `bash benchmark.sh 8 40`.</br>
d) D40 datasets with 10 clients, run `bash benchmark.sh 40 10`.</br>
e) D40 datasets with 20 clients, run `bash benchmark.sh 40 20`.</br>
f) D40 datasets with 40 clients, run `bash benchmark.sh 40 40`.</br>

### 9. To stop server
```
killall -9 mongo
killall -9 mongod
```

## References
https://www.digitalocean.com/community/tutorials/how-to-create-a-sharded-cluster-in-mongodb-using-an-ubuntu-12-04-vps
