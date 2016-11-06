# Team3-MongoDB


## Introduction
Cassandra benchmarking measures the performance of different data modeling with different set of nodes and clients. With comparison of different data modeling, this allows us to find out the optimized database schema design for Cassandra.

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
### 3. Configuration settings
Create / Configure .bash_profile file
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
Configure server settings
```
mkdir /temp/data
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
screen
./mongod --dbpath /temp/data // Start server with chosen datapath
ctrl+A, D // Exit the screen
```

### 4. Configuration for three nodes
```
cd /temp/datastax-ddc-3.9.0/conf
vim cassandra.yaml
```
Edit the settings in 'cassandra.yaml' file:

1) seeds: Add the IP addresses of the three nodes.

<img src="https://github.com/jun159/Team3-Cassandra/blob/master/IMG%20CS4224.jpg" height ="200">
    
2) listen_address: Add in the IP address of the current node in use.

<img src="https://github.com/jun159/Team3-Cassandra/blob/master/IMG%202%20CS4224.png" height ="60">

Save the file and restart the cassandra server.

### 5. Download project
Before running the scripts, make sure that the project is in the home folder. Change directory to the project folder to prepare for benchmarking.
```
cd Team3-MongoDB 
```

### 6. Bulkload data
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) To bulkload all D8 datasets into the database with 1 node, run `bash bulkload.sh 8 1`. </br>
b) To bulkload all D8 datasets into the database with 3 nodes, run `bash bulkload.sh 8 3`. </br>
c) To bulkload all D40 datasets into the database with 1 node, run `bash bulkload.sh 40 1`. </br>
d) To bulkload all D40 datasets into the database with 3 nodes, run `bash bulkload.sh 40 3`. 

### 7. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) To benchmark D8 datasets with 10 clients, run `bash benchmark.sh 8 10`.</br>
b) To benchmark D8 datasets with 20 clients, run `bash benchmark.sh 8 20`.</br>
c) To benchmark D8 datasets with 40 clients, run `bash benchmark.sh 8 40`.</br>
d) To benchmark D40 datasets with 10 clients, run `bash benchmark.sh 40 10`.</br>
e) To benchmark D40 datasets with 20 clients, run `bash benchmark.sh 40 20`.</br>
f) To benchmark D40 datasets with 40 clients, run `bash benchmark.sh 40 40`.</br>

### 8. To stop server
```
killall -9 mongo
killall -9 mongod
```

## References
