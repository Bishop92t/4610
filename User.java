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

/**
 * @author Nathanael Bishop 
 * CSC 4610 Project Fall 2017
 * This is a full featured 3-tier website that contains static content (Apache), dynamic content (Tomcat), 
 *   and a database (MySQL).  These Java Servlets pull static content and database content to present
 *   the user with a ratings and review website for various Cloud services. The focus in building this 
 *   was to showcase what I have learned this semester as well as a few new skills I picked up.
 */

public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String DB_NAME     = "crr";
	private static String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME;
	
	/**
	 * Pass this method an array of cookies and it will attempt to find one that's named CCRLogin
	 *   signaling that this cookie is the valid login name token
	 * @param cookies an array of cookies from the HttpServletRequest
	 * @return the value of that named cookie or "" if nothing found
	 */
	public static String getUserName(Cookie[] cookies) {
		//the users name to be returned if found in the cookies array
		String name = "";
		
		//look through the array of cookies for one named CCRLogin
		if(cookies != null)
			for(Cookie cookie : cookies)
				//if found, retrieve it's value as the user name to be returned
				if(cookie.getName().equals("CCRLogin"))
					name = cookie.getValue();
		
		//now return the users name or the default value of "" if no user was found
		return name;
	}
	
	/**
	 * Given the users name, attempt to retrieve their email address from the database
	 * @param name the name of the user
	 * @return the email address of the user (as a String)
	 */
	public static String getUserEmail(String name) {
		//default userEmail to "", this method operates on the 'login' database table
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
			
			//if there were any results, save the email as the string that will be returned
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
		//default the return value to false, and this method operates on the 'login' table in the db
		boolean isExistingUser = false;
		String DB_TABLE  = "login";

		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//setup and execute the SQL query
			String sql              = "SELECT * FROM "+DB_TABLE+" WHERE email=?;";
			PreparedStatement pstmt = conn.prepareStatement(sql);

			//sanitize the email input
			pstmt.setString(1, email);

			//now execute the sanitized query
			ResultSet rs = pstmt.executeQuery(sql);
			
			//if there were any results the user was found, set return value to true
			if(rs.next())
				isExistingUser = true;
				
			//close all the connections
	 		if(rs != null)
	 			rs.close();
	 		if(pstmt != null) 
 				pstmt.close();
	 		if(conn != null)
	 			conn.close();
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return isExistingUser;
	}
	
	/**
	 * Logout method. Look through an array of cookies for a CCRLogin token. If found, set it to expire. Then send
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
		//now send the user back to the login screen				
		response.sendRedirect("http://52.26.169.0/4610.html");
	}
	
	/**
	 * boilerplate servlet code
	 */
    public User() {
        super();
    }

	/**
	 * boilerplate servlet code
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}