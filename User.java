import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String DB_NAME     = "crr";
	private static String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME;
	
	/**
	 * Pass this helper method an array of cookies and it will attempt to find one that's named CCRLogin
	 *   signaling that this cookie is the valid login name
	 * @param cookies an array of cookies from the HttpServletRequest
	 * @return the value of that named cookie or "" if nothing found
	 */
	public static String getUserName(Cookie[] cookies) {
		String name = "";
		
		//look through the array of cookies for one named CCRLogin
		if(cookies != null)
			for(Cookie cookie : cookies)
				//if found, retrieve it's value as the user name to be returned
				if(cookie.getName().equals("CCRLogin"))
					name = cookie.getValue();
		
		return name;
	}
	
	/**
	 * Given the users name, attempt to retrieve their email address from the database
	 * @param name the name of the user
	 * @return the email address of the user (as a String)
	 */
	public static String getUserEmail(String name) {
		String userEmail = "";
		String DB_TABLE  = "login"; 

		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//setup and execute the SQL query
			Statement stmt = (Statement) conn.createStatement();
			String sql     = "SELECT * FROM "+DB_TABLE+" WHERE name='"+name+"';";
			ResultSet rs   = (ResultSet) stmt.executeQuery(sql);
			
			//if there were any results, save the email as a String
			if(rs.next())
				userEmail = rs.getString("email");
				
			//close all the connections
	 		if(rs != null)
	 			rs.close();
	 		if(stmt != null) 
 				stmt.close();
	 		if(conn != null)
	 			conn.close();
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		//now return the user email if found (if not found it will return blank String)
		return userEmail;
	}
	
	/**
	 * Looks through the db to see if someone with that email has already registered
	 * @param email the email to be searched for
	 * @return true if that email is already in the db
	 */
	public static boolean isExistingUserEmail(String email) {
		boolean isExistingUser = false;
		String DB_TABLE  = "login";

		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//setup and execute the SQL query
			Statement stmt = (Statement) conn.createStatement();
			String sql     = "SELECT * FROM "+DB_TABLE+" WHERE email='"+email+"';";
			ResultSet rs   = (ResultSet) stmt.executeQuery(sql);
			
			//if there were any results, save the email as a String
			if(rs.next())
				isExistingUser = true;
				
			//close all the connections
	 		if(rs != null)
	 			rs.close();
	 		if(stmt != null) 
 				stmt.close();
	 		if(conn != null)
	 			conn.close();
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
		return isExistingUser;
	}
	
	/**
	 * Look through an array of cookies for a CCRLogin token. If found, set it to expire. Then send
	 *   the user to the login screen
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//look through the array of cookies for one named CCRLogin
		if(request.getCookies() != null)
			for(Cookie cookie : request.getCookies()) 
				//if found, set that cookie to expire and delete it's value 
				if(cookie.getName().equals("CCRLogin")) {
					cookie.setMaxAge(0);
					cookie.setValue(null);
					response.addCookie(cookie);
				}
		response.sendRedirect("http://52.26.169.0/4610.html");
	}
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public User() {
        super();
    }
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}