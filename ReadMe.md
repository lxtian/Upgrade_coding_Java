# Java Coding Challenge

Developed on Linux Sublime

Java version
openjdk version "1.8.0_181"
OpenJDK Runtime Environment (build 1.8.0_181-8u181-b13-0ubuntu0.16.04.1-b13)
OpenJDK 64-Bit Server VM (build 25.181-b13, mixed mode)

My assumptions for the challenge:  
  - One user account cannot be logged in at the same time
  - Sqlite3 dabase connection is stable for simplicity (In read practice will start a new thread in the class to communicate with the script from the database side and send 'heartbeat')
  - Four tables (User, Account, TransactionRecord, UserLog) in database

## How to build this project

Build the database first
```sh
$ sqlite3 Bank.db < databasecommand.sql
```



