package api;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import db.DBConnection;

/**
 * Servlet implementation class RecommendRestaurants
 */
@WebServlet("/recommendation")
public class RecommendRestaurants extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final DBConnection connection = new DBConnection();

  /**
   * @see HttpServlet#HttpServlet()
   */
  public RecommendRestaurants() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    JSONArray array = new JSONArray();
    if (request.getParameterMap().containsKey("user_id")) {
      String userId = request.getParameter("user_id");
      array = connection.getRecommendations(userId);
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
