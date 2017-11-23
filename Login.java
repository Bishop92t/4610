import java.io.IOException;
import java.io.PrintWriter;
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

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
	 * Using information from a html form post, attempt to load a user's info
	 *   from the mysql server. If found display it, otherwise inform user.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//set the file type, print writer, and declare the document html type
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out=response.getWriter();
		String docType="<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";
		
		//create the first bit of html to be displayed
		out.println(docType + "<html><head><title>User Login</title></head><body>\n");
		
		// database URL, and init connection/statement
		String DB_TABLE    = "login";
		String DB_NAME     = "crr";
		String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME;
		Connection conn = null;
		Statement  stmt = null;
		
		//get the name & password from the HTML form and setup a success/fail var
		String nameInput=request.getParameter("name");
		String passInput=request.getParameter("password");
		Boolean foundName=false;
		
		//declare the SQL variables
		String name, password;
		
		//try to connect to db and search for the user
		try {
			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
	
			//Create a query
			stmt = (Statement) conn.createStatement();
			String sql = "SELECT * FROM "+DB_TABLE+";";
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);			
			
			//look through result set for the user's name and password
			while(rs.next()){
				//Retrieve by column name (from the SQL server)
				name     = rs.getString("name");
				password = rs.getString("password");
				
				//if we find a match, print it
			 	if(name.equals(nameInput) && password.equals(passInput)) { 
			 		foundName=true;
			 		//create the login token cookie
			 		Cookie loginCookie = new Cookie ("CCRLogin", name);
			 		loginCookie.setMaxAge(60 * 60);

			 		//add the cookie to the response returned to the client
			 		response.addCookie(loginCookie);
			 		response.sendRedirect("Home");
			 	}
			}
			//close all the connections
	 		if(rs != null)
	 			rs.close();
	 		if(stmt != null) 
 				stmt.close();
	 		if(conn != null)
	 			conn.close();
System.out.println("closed connection in login");	 		

	 		//if no name match, inform the user and give a way back
			if(!foundName)
				out.println("<h1>Incorrect name or password!</h1><a href='MainPage.html'>Go Back</a>");
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

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