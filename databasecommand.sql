CREATE TABLE User(username Text, password Text, Primary Key(username));
CREATE TABLE Account(username Text, amount Numeric, Primary Key(username));
CREATE TABLE TransactionRecord(username Text, transactime TIMESTAMP, Type Text, amount Numeric, Primary Key(username, transactime));
CREATE TABLE UserLog(username Text, Primary Key(username));
INSERT INTO User Values('root', '63a9f0ea7bb98050796b649e85481845');
INSERT INTO Account Values('root', 10000);