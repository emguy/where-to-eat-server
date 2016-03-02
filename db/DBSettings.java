package db;

public class DBSettings {
  public static final String DB_HOST_NAME = "localhost";
  public static final String DB_PORT = "3306";
  public static final String DB_NAME = "where-to-eat";
  public static final String DB_USER = "root";
  public static final String DB_PASSWORD = "root";
  public static final String URL;

  static {
    URL = "jdbc:mysql://" + DB_HOST_NAME + ":" + DB_PORT + "/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASSWORD;
  }
}
