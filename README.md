#Where To Eat (Server)

This web api server fetchs a list of restaurant data from Yelp according to
user's specified location (or from the GPS data). From those restaurant date,
it determines the resturant recommendation through a restaurant learning
algorithm.

## Requirements for Compilation from Source

1. [Java SE 8 (It MUST BE Java 8, not Java 6, not Java 7)][1] 
2. [Apache Tomcat][2]
3. [MySQL][3]
4. [MySQL JDBC Connector][4]
5. [java-json][5]
6. [scribe][6]

## Usage

You must register an yelp account to be able to use the yelp api for retrieving
the restaurant data. After you login to Yelp, you can obtain your oauth
key/secret pairs through [here][7]. Then use those new obtained key/secret
pairs to fill in the respective lines in the class file `YelpAPI.java`.

Before deply the web server, you must also create the data base `wheretoeat` on
the MySQL.

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
CREATE TABLE RESTAURANTS (business_id VARCHAR(255) NOT NULL, name VARCHAR(255), categories VARCHAR(255), city VARCHAR(255), state VARCHAR(255), stars FLOAT, full_address VARCHAR(255), latitude FLOAT,  longitude FLOAT, image_url VARCHAR(255), url VARCHAR(255), PRIMARY KEY ( business_id ));
CREATE TABLE USERS (user_id VARCHAR(255) NOT NULL, first_name VARCHAR(255), last_name VARCHAR(255),  PRIMARY KEY ( user_id ));
CREATE TABLE HISTORY (visit_history_id bigint(20) unsigned NOT NULL AUTO_INCREMENT, user_id VARCHAR(255) NOT NULL ,  business_id VARCHAR(255) NOT NULL, last_visited_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (visit_history_id), FOREIGN KEY (business_id) REFERENCES restaurants(business_id), FOREIGN KEY (user_id) REFERENCES users(user_id));
```

4. We insert a dummy user into the table `users`.
```sql
INSERT INTO USERS VALUES ("1111", "John", "Smith");
```

## Api Endpoint Reference

### GET `/restaurants`

It takes two float arguments `lat` and `lon`. They are the latitude and
latitude of a specified location. It returns a JSON array containing a list of
restaurant data retrieved from Yelp.

### GET `/recommendation`

It takes one string argument `user_id`, and returns a JSON array which contains
a list of recommended restaurants.

### GET `/history`

It takes one string argument `user_id`, and returns a JSON array which contains
all restaurants in the user's visiting record.

### POST `/history`

It submits a JSON object with the following format
```JSON
{
'user_id':'user_id',
'visited':[
'restaurant_1',
'restaurant_2'
]
}
```
where, `user_id` is user id, and the JSON array `visited` contains a list of
`restaurant_id` which have been visited by the user. Once this POST request is
received, all restaurants listed in the JSON array `visited` will be inserted
into the user's database.

### DELETE `/history`

It takes the same JSON object as the one for its POST method. Once this DELETE
request is received, all restaurants matches the entries in the JSON array
`visited` will be deleted from the user's database.


[1]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[2]: http://tomcat.apache.org/download-80.cgi
[3]: http://dev.mysql.com/downloads/
[4]: https://ev.mysql/com/downloads/connector/j/
[5]: http://www.java2s.com/Code/JarDownload/java-json/java-json.jar.zip
[6]: http://mvnrepository.com/artifact/org.scribe/scribe/1.3.7
[7]: https://www.yelp.com/developers/manage_api_keys
