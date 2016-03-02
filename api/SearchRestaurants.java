package api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import db.DBConnection;

/**
 * Servlet implementation class SearchRestaurants
 */
@WebServlet("/restaurants")
public class SearchRestaurants extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final DBConnection connection = new DBConnection();

  /**
   * @see HttpServlet#HttpServlet()
   */
  public SearchRestaurants() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    JSONArray array = new JSONArray();
    Map<String, String[]> parameterMap = request.getParameterMap();
    if (parameterMap.containsKey("lat") && parameterMap.containsKey("lon")) {
      double lat = Double.parseDouble(request.getParameter("lat"));
      double lon = Double.parseDouble(request.getParameter("lon"));
      array = connection.searchRestaurants(lat, lon);
    }
    RpcParser.writeOutput(response, array);
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
