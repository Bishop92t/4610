import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
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

public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Using information from a html form post, attempt to add that user to the
	 *   mysql database
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//set the file type, print writer, and declare the document html type
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out=response.getWriter();
		String docType="<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";
		
		//print out the first bit of html
		out.println(docType+"<html>\n<head><title>User registration</title></head>\n"
		                   +"<body>\n <h1 align=\"center\">");
		
		//setting up variables passed from the html	form	
		String name  = request.getParameter("name");
		String pass  = request.getParameter("password");
		String email = request.getParameter("email");
		
		//db setup
		String DB_TABLE = "login";
		String DB_NAME  = "crr";
		String DB_URL   = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";
		Connection conn = null;
		Statement stmt  = null;

		//try to write the data and close the connection
		try {
			//open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn=(Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");

			//create the statement to write the data to the database
//**** TO DO: add check for existing username first  ****
			String sql="INSERT INTO "+DB_TABLE+"(name, password, email)"+ 
					   "VALUES ('"+name+"','"+pass+"','"+email+"');";

			//taking a performance hit with prepareStatement to sanitize inputs
			stmt=(Statement) conn.prepareStatement(sql);

			//send that statement to the db and commit
			stmt.executeUpdate(sql);
			conn.commit();
			
		 	//now that we have all the data sent, print welcome message
			out.println("<h1><br>Welcome "+name+"</h1><ul>"+
				        "<b>You're registered with email</b>: "+email+"\n");
		} catch (ClassNotFoundException | SQLException e) {
			out.println("<h1>Database Error</h1>");
		} finally {
			//finally, attempt close the connection
			try {
				if(stmt!=null)
					conn.close();
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//close out the html tags
		out.println("</body></html>");
	}
	
    public SignUp() {        
    	super();    
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}