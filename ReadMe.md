# Java Coding Challenge

Developed on Ubuntu

Java version<br/>
openjdk version "1.8.0_181"<br/>
OpenJDK Runtime Environment (build 1.8.0_181-8u181-b13-0ubuntu0.16.04.1-b13)<br/>
OpenJDK 64-Bit Server VM (build 25.181-b13, mixed mode)<br/>

For the challenge:  
  - One user account cannot be logged in at the same time
  - Sqlite3 database connection is stable for simplicity (In real practice will start a new thread in the class to communicate with the script from the database side and send 'heartbeat')
  - Four tables (User, Account, TransactionRecord, UserLog) in database
  - Able to deal with concurrency (try open several command lines at the same time and log in to different accounts to try that)
  - Use of MD5 to store the password in the database
  - Don't use ctrl-c to exit the program when you are logged in (Log in record won't be cleared from the database)


## How to build this project

Build the database first
```sh
$ sqlite3 Bank.db < databasecommand.sql
```
Compile the java program
```sh
$ javac *.java
```
Start from the driver
```sh
$ java -classpath ".:sqlite-jdbc-3.23.1.jar" Driver
```
