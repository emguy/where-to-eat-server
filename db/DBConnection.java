package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Restaurant;
import yelp.YelpAPI;

public class DBConnection {
  private Connection conn = null;
  private static final int MAX_RECOMMENDED_RESTAURANTS = 10;
  private static final int MIN_RECOMMENDED_RESTAURANTS = 3;

  public DBConnection() {
    this(DBSettings.URL); // call the other constructor
  }

  public DBConnection(String url) {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void close() {
    if (conn != null) {
      try {
        conn.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void executeUpdateStatement(String query) {
    if (conn == null) {
      return;
    }
    try {
      Statement stmt = (Statement) conn.createStatement();
      System.out.println("\nDBConnection executing query:\n" + query);
      stmt.executeUpdate(query);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private ResultSet executeFetchStatement(String query) {
    if (conn == null) {
      return null;
    }
    try {
      Statement stmt = (Statement) conn.createStatement();
      System.out.println("\nDBConnection executing query:\n" + query);
      return stmt.executeQuery(query);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public JSONArray searchRestaurants(double lat, double lon) {
    try {
      YelpAPI api = new YelpAPI();
      JSONObject response = new JSONObject(api.searchForBusinessesByLocation(lat, lon));
      JSONArray array = (JSONArray) response.get("businesses");

      List<JSONObject> list = new ArrayList<JSONObject>();

      for (int i = 0; i < array.length(); i++) {
        JSONObject object = array.getJSONObject(i);
        Restaurant restaurant = new Restaurant(object);
        String business_id = restaurant.getBusinessId();
        String name = restaurant.getName();
        String categories = restaurant.getCategories();
        String city = restaurant.getCity();
        String state = restaurant.getState();
        String fullAddress = restaurant.getFullAddress();
        double stars = restaurant.getStars();
        double latitude = restaurant.getLatitude();
        double longitude = restaurant.getLongitude();
        String imageUrl = restaurant.getImageUrl();
        String url = restaurant.getUrl();
        String sql = "INSERT IGNORE INTO RESTAURANTS " + "VALUES ('" + business_id + "', \"" + name + "\", \""
            + categories + "\", '" + city + "', '" + state + "', " + stars + ", \"" + fullAddress + "\", " + latitude
            + "," + longitude + ",\"" + imageUrl + "\", \"" + url + "\")";
        System.out.println(sql);
        executeUpdateStatement(sql);
        JSONObject obj = restaurant.toJSONObject();
        list.add(obj);
      }
      return new JSONArray(list);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  public void setVisitedRestaurants(String userId, List<String> businessIds) {
    for (String businessId : businessIds) {
      String sql = "INSERT INTO USER_VISIT_HISTORY (`user_id`, `business_id`) VALUES (\"" + userId + "\", \""
          + businessId + "\")";
      executeUpdateStatement(sql);
    }
  }

  public void unsetVisitedRestaurants(String userId, List<String> businessIds) {
    for (String businessId : businessIds) {
      String sql = "DELETE FROM USER_VISIT_HISTORY WHERE `user_id`=\"" + userId + "\" and `business_id` = \""
          + businessId + "\"";
      executeUpdateStatement(sql);
    }
  }

  public Set<String> getVisitedRestaurants(String userId) {
    Set<String> visitedRestaurants = new HashSet<String>();
    try {
      String sql = "SELECT business_id from USER_VISIT_HISTORY WHERE user_id=" + userId;
      ResultSet rs = executeFetchStatement(sql);
      while (rs.next()) {
        String visited_restaurant = rs.getString("business_id");
        visitedRestaurants.add(visited_restaurant);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return visitedRestaurants;
  }

  private Set<String> getCategories(String business_id) {
    try {
      String sql = "SELECT categories from RESTAURANTS WHERE business_id='" + business_id + "'";
      ResultSet rs = executeFetchStatement(sql);
      if (rs.next()) {
        Set<String> set = new HashSet<>();
        String[] categories = rs.getString("categories").split(",");
        for (String category : categories) {
          String category_trim = category.trim(); // ' Japanese ' -> 'Japanese'
          if (!category_trim.equals("Restaurants")) {
            set.add(category_trim);
          }
        }
        return set;
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return new HashSet<String>();
  }

  private Set<String> getBusinessIdsByCategory(String category) {
    Set<String> set = new HashSet<>();
    try {
      String sql = "SELECT business_id from RESTAURANTS WHERE categories LIKE '%" + category + "%'";
      ResultSet rs = executeFetchStatement(sql);
      while (rs.next()) {
        String business_id = rs.getString("business_id");
        set.add(business_id);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return set;
  }

  private JSONObject getRestaurantsById(String businessId) {
    try {
      String sql = "SELECT business_id, name, full_address, categories, stars, latitude, longitude, city, state, image_url from "
          + "RESTAURANTS where business_id='" + businessId + "'" + " ORDER BY stars DESC";
      ResultSet rs = executeFetchStatement(sql);
      if (rs.next()) {
        Restaurant restaurant = new Restaurant(rs.getString("business_id"), rs.getString("name"),
            rs.getString("categories"), rs.getString("city"), rs.getString("state"), rs.getString("full_address"),
            rs.getDouble("stars"), rs.getDouble("latitude"), rs.getDouble("longitude"), rs.getString("image_url"),
            rs.getString("url"));
        JSONObject obj = restaurant.toJSONObject();
        // obj.put("is_visited", isVisited);
        return obj;
      }
    } catch (Exception e) { /* report an error */
      System.out.println(e.getMessage());
    }
    return null;
  }

  private Set<String> getMoreCategories(Set<String> existingCategories) {
    Set<String> allCategories = new HashSet<>();
    for (String category : existingCategories) {
      allCategories.addAll(getMoreCategories(category));
    }
    return allCategories;
  }

  private Set<String> getMoreCategories(String category) {
    Set<String> allCategories = new HashSet<>();
    try {
      String sql = "SELECT categories from RESTAURANTS WHERE categories LIKE '%" + category + "%'";
      ResultSet rs = executeFetchStatement(sql);
      while (rs.next()) {
        String[] categories = rs.getString("categories").split(",");
        for (String cat : categories) {
          String category_trim = cat.trim();
          if (!category_trim.equals("Restaurants")) {
            allCategories.add(category_trim);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return allCategories;
  }

  public JSONArray getRecommendations(String userId) {
    try {
      Set<String> visitedRestaurants = getVisitedRestaurants(userId);
      Set<String> allCategories = new HashSet<>();
      for (String restaurant : visitedRestaurants) {
        allCategories.addAll(getCategories(restaurant));
      }
      Set<String> allRestaurants = new HashSet<>();
      for (String category : allCategories) {
        Set<String> set = getBusinessIdsByCategory(category);
        allRestaurants.addAll(set);
      }
      Set<JSONObject> diff = new HashSet<>();
      int count = 0;
      for (String business_id : allRestaurants) {
        // Perform filtering
        if (!visitedRestaurants.contains(business_id)) {
          diff.add(getRestaurantsById(business_id));
          count++;
          if (count >= MAX_RECOMMENDED_RESTAURANTS) {
            break;
          }
        }
      }
      if (count < MIN_RECOMMENDED_RESTAURANTS) {
        allCategories.addAll(getMoreCategories(allCategories));
        for (String category : allCategories) {
          Set<String> set = getBusinessIdsByCategory(category);
          allRestaurants.addAll(set);
        }
        for (String business_id : allRestaurants) {
          if (!visitedRestaurants.contains(business_id)) {
            diff.add(getRestaurantsById(business_id));
            count++;
            if (count >= MAX_RECOMMENDED_RESTAURANTS) {
              break;
            }
          }
        }
      }
      return new JSONArray(diff);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }
}
