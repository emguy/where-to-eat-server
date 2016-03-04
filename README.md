#Where To Eat (Server)

This web api server fetchs a list of restaurant data from Yelp according to
user's specified location (or from the GPS data). From those restaurant date,
it determines the resturant recommendation through a restaurant learning
algorithm.

## Requirements

1. [Java SE 8 (It MUST BE Java 8, not Java 6, not Java 7)][1] 
2. [Apache Tomcat][2]
3. [MySQL][3]
4. [java-json][4]

## Usage

Before deply the web server, we must create the data base `wheretoeat` on the MySQL.

1.  Login the MySQL server with the following command.
```
mysql - u root -p
```

2.  In the MySQL shell, execute the following to  create the database `wheretoeat`.
```sql
DROP DATABASE IF EXISTS wheretoeat;
CREATE DATABASE wheretoeat;

```

3.  Then we define the following empty tables.
```sql
CREATE TABLE restaurants (business_id VARCHAR(255) NOT NULL, name VARCHAR(255), categories VARCHAR(255), city VARCHAR(255), state VARCHAR(255), stars FLOAT, full_address VARCHAR(255), latitude FLOAT,  longitude FLOAT, image_url VARCHAR(255), url VARCHAR(255), PRIMARY KEY ( business_id ));
CREATE TABLE users (user_id VARCHAR(255) NOT NULL, first_name VARCHAR(255), last_name VARCHAR(255),  PRIMARY KEY ( user_id ));
CREATE TABLE history (visit_history_id bigint(20) unsigned NOT NULL AUTO_INCREMENT, user_id VARCHAR(255) NOT NULL ,  business_id VARCHAR(255) NOT NULL, last_visited_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (visit_history_id), FOREIGN KEY (business_id) REFERENCES restaurants(business_id), FOREIGN KEY (user_id) REFERENCES users(user_id));
```

4. We insert a dummy user into the table `users`.
```sql
INSERT INTO USERS VALUES ("1111", "John", "Smith");
```


[1]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[2]: http://tomcat.apache.org/download-80.cgi
[3]: http://dev.mysql.com/downloads/
[4]: https://ev.mysql/com/downloads/connector/j/
[5]: http://www.java2s.com/Code/JarDownload/java-json/java-json.jar.zip
