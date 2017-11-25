import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

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

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
	 * Using information from a html form post, attempt to load a user's info
	 *   from the mysql server. 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// database URL, and init connection/statement
		String DB_TABLE         = "login";
		String DB_NAME          = "crr";
		String DB_URL           = "jdbc:mysql://localhost:3306/"+DB_NAME;
		Connection conn         = null;
		PreparedStatement pstmt = null;
		
		//get the name & password from the HTML form 
		String nameInput=request.getParameter("name");
		String passInput=request.getParameter("password");
		
		//default message and login status variables
		String message = "user name and password not found.";
		boolean foundName = false;

		try {
			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
	
			//Create a query using preparedStatement to sanitize inputs
			String sql   = "SELECT * FROM "+DB_TABLE+" where name=? AND password=?;";
			pstmt        = conn.prepareStatement(sql);

			//sanitize the name and password inputs
			pstmt.setString(1, nameInput);
			pstmt.setString(2, passInput);

			//now execute the sanitized query
			ResultSet rs = pstmt.executeQuery();			
			
			//if a match is found, the user has typed in the correct password
			if(rs.next()){
			 	message   = "thanks for logging in.";
			 	foundName = true;
			}

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

		//send true if the username was found (otherwise send false), and the users name
 		messageUserThenRedirectHome(foundName, nameInput, message, response);
	}

	/*
	 * Print the message String for the user and then either create their login token or boot them back
	 *   to the login screen.
	 * @param isValidLogin true if the user has succesfully logged in, otherwise false
	 * @param name the name of the user
	 * @param message the message to send the user before redirecting them
	 */
	public static void messageUserThenRedirectHome(boolean isValidLogin, String name, String message, HttpServletResponse response) throws IOException {
		//set the file type, print writer, and declare the document html type
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out=response.getWriter();
		String docType="<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";
		
		//create the first bit of html to be displayed
		out.println(docType + "<html><head><title>User Login</title></head><body>\n");

		out.println("<h2>"+name+", "+message+" You're being redirected shortly</h2>\n");

		//if the login is valid, create a login cookie and send user to the home page
		if(isValidLogin) {
			//create the login token cookie
			Cookie loginCookie = new Cookie ("CCRLogin", name);
			loginCookie.setMaxAge(60 * 60);

 			//add the cookie to the response returned to the client
			response.addCookie(loginCookie);
			response.setHeader("Refresh", "5; URL=http://52.24.2.46:8080/4610/Home");
 		}
 		//else send them to the login screen
 		else
			response.setHeader("Refresh", "5; URL=http://52.26.169.0/4610.html");

		//finally close out the html tags
		out.println("</body></html>");
	}

	public Login() {        
		super();    
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}