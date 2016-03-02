package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBImport {
  public static final int PORT = 3306;
  public static final String DB_HOST_NAME = "jdbc:mysql://localhost";
  public static final String DB_NAME = "where-to-eat";
  public static final String DB_USER = "root";
  public static final String DB_PASSWORD = "root";

  /**
   * A string processing method. Replace " by \", / by or, and ' by space.
   * <p>
   * Example: abc"def/ghi'jkl => abc\"def or ghi jkl
   * 
   * @param str the input string to be processed
   * @return the string after processing
   */
  public static String parseString(String str) {
    return str.replace("\"", "\\\"").replace("/", " or ").replace("'", "");
  }

  /**
   * Convert a JSONArray to a string whose fields are separated by comma.
   * <p>
   * Example: ['foo1', 'foo2', 'foo3'] => "foo1,foo2,foo3"
   * 
   * @param array the input JSONArray
   * @return the output string
   * @see JSONArray
   */
  public static String jsonArrayToString(JSONArray array) {
    StringBuilder sb = new StringBuilder();
    try {
      for (int i = 0; i < array.length(); i++) {
        String obj = (String) array.get(i);
        sb.append(obj);
        if (i != array.length() - 1) {
          sb.append(",");
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  /**
   * Convert a string whose fields are separated by comma to a JSONArray.
   * <p>
   * Example: "foo1,foo2,foo3" => ['foo1', 'foo2', 'foo3']
   * 
   * @param array the input string
   * @return the created JSONArray
   * @see JSONArray
   */
  public static JSONArray stringToJSONArray(String str) {
    try {
      return new JSONArray("[" + str + "]");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      Connection conn = null;
      String line = null;

      try {
        conn = DriverManager.getConnection(
            DB_HOST_NAME + ':' + PORT + '/' + DB_NAME + '?' + "user=" + DB_USER + "&password=" + DB_PASSWORD);
      } catch (SQLException e) {
        System.out.println("SQLException " + e.getMessage());
        System.out.println("SQLState " + e.getSQLState());
        System.out.println("VendorError " + e.getErrorCode());
      }
      if (conn == null) {
        return;
      }
      /* Step 1 Drop tables whose names are same as those to be created */
      Statement stmt = conn.createStatement();
      String sql = "DROP TABLE IF EXISTS USER_VISIT_HISTORY";
      stmt.executeUpdate(sql);

      sql = "DROP TABLE IF EXISTS RESTAURANTS";
      stmt.executeUpdate(sql);

      sql = "DROP TABLE IF EXISTS USERS";
      stmt.executeUpdate(sql);

      sql = "DROP TABLE IF EXISTS USER_REVIEW_HISTORY";
      stmt.executeUpdate(sql);

      sql = "DROP TABLE IF EXISTS USER_CATEGORY_HISTORY";
      stmt.executeUpdate(sql);

      /* Step 2: create tables */
      sql = "CREATE TABLE RESTAURANTS " + "(business_id VARCHAR(255) NOT NULL, " + " name VARCHAR(255), "
          + "categories VARCHAR(255), " + "city VARCHAR(255), " + "state VARCHAR(255), " + "stars FLOAT,"
          + "full_address VARCHAR(255), " + "latitude FLOAT, " + " longitude FLOAT, " + "image_url VARCHAR(255), "
          + " PRIMARY KEY ( business_id ))";
      stmt.executeUpdate(sql);

      sql = "CREATE TABLE USERS " + "(user_id VARCHAR(255) NOT NULL, "
          + " first_name VARCHAR(255), last_name VARCHAR(255), " + " PRIMARY KEY ( user_id ))";
      stmt.executeUpdate(sql);

      sql = "CREATE TABLE USER_VISIT_HISTORY " + "(visit_history_id bigint(20) unsigned NOT NULL AUTO_INCREMENT, "
          + " user_id VARCHAR(255) NOT NULL , " + " business_id VARCHAR(255) NOT NULL, "
          + " last_visited_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " + " PRIMARY KEY (visit_history_id),"
          + "FOREIGN KEY (business_id) REFERENCES RESTAURANTS(business_id),"
          + "FOREIGN KEY (user_id) REFERENCES users(user_id))";
      stmt.executeUpdate(sql);

      sql = "CREATE TABLE USER_REVIEW_HISTORY " + "(visit_review_id bigint(20) unsigned NOT NULL AUTO_INCREMENT, "
          + " user_id VARCHAR(255) NOT NULL , " + " business_id VARCHAR(255) NOT NULL, "
          + " PRIMARY KEY (visit_review_id))";
      stmt.executeUpdate(sql);

      sql = "CREATE TABLE USER_CATEGORY_HISTORY " + "(category_id bigint(20) unsigned NOT NULL AUTO_INCREMENT, "
          + " first_id VARCHAR(255) NOT NULL , " + " second_id VARCHAR(255) NOT NULL, " + " count bigint(20) NOT NULL, "
          + " PRIMARY KEY (category_id))";
      stmt.executeUpdate(sql);

      /* Step 3: insert data from the local file */
      BufferedReader reader = new BufferedReader(new FileReader("src/dataset/yelp_academic_dataset_business.json"));
      while ((line = reader.readLine()) != null) {
        JSONObject restaurant = new JSONObject(line);
        String business_id = restaurant.getString("business_id");
        String name = parseString(restaurant.getString("name"));
        String categories = parseString(jsonArrayToString(restaurant.getJSONArray("categories")));
        String city = parseString(restaurant.getString("city"));
        String state = restaurant.getString("state");
        String fullAddress = parseString(restaurant.getString("full_address"));
        double stars = restaurant.getDouble("stars");
        double latitude = restaurant.getDouble("latitude");
        double longitude = restaurant.getDouble("longitude");
        String imageUrl = "http://www.example.com/img.JPG";
        sql = "INSERT INTO RESTAURANTS " + "VALUES ('" + business_id + "', \"" + name + "\", \"" + categories + "\", '"
            + city + "', '" + state + "', " + stars + ", \"" + fullAddress + "\", " + latitude + "," + longitude
            + ", \"" + imageUrl + "\"" + ")";
        System.out.println(sql);
        stmt.executeUpdate(sql);
      }
      reader.close();

      /* Step 4: insert user */
      sql = "INSERT INTO USERS " + "VALUES (\"1111\", \"John\", \"Smith\")";
      stmt.executeUpdate(sql);
      System.out.println("Done importing.");
    } catch (Exception e) { /* report an error */
      System.out.println(e.getMessage());
    }
  }
}
