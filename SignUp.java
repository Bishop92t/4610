import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

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
	 * Servlet uses information from a html form post to add that user to the mysql database unless they 
	 *   provide an email address that's already been used. If successful, send them to the home screen
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//setting up variables passed from the html	form	
		String name  = request.getParameter("name");
		String pass  = request.getParameter("password");
		String email = request.getParameter("email");
		
		//if the user has already registered with that email, boot them to login screen
		boolean userEmailExists = false;
		if(User.isExistingUserEmail(email)) {
			Login.messageUserThenRedirectHome(false, name, "that email has already been used.", response);
			userEmailExists = true;
		}
		
		//database variables setup, note when writing to DB the DB_URL is different
		String DB_TABLE = "login";
		String DB_NAME  = "crr";
		String DB_URL   = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";

		//connection and sql statement setup, taking the performance hit using PreparedStatement to sanitize inputs
		Connection        conn = null;
		PreparedStatement pstm = null;

		if(!userEmailExists) {
			//try to write the data and close the connection
			try {
				//open a connection
				Class.forName("com.mysql.jdbc.Driver");
				conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
	
				//create the statement to write the data to the database
				String sql = "INSERT INTO "+DB_TABLE+"(name, password, email) VALUES (?, ?, ?);";
				pstm = conn.prepareStatement(sql);
	
				//sanitize the input: name, password and email
				pstm.setString(1, name);
				pstm.setString(2, pass);
				pstm.setString(3, email);
	
				//send that statement to the db and commit
				pstm.executeUpdate();
				conn.commit();
				
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			} finally {
				//finally, attempt close the connection
				try {
					if(pstm!=null)
						conn.close();
					if(conn!=null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			//print out a message for the user that their account was created and then redirect them to the home page 
			Login.messageUserThenRedirectHome(true, name, "welcome to CRR.", response);
		}
	}
	
	/**
	 * boilerplate servlet code
	 */
    public SignUp() {
        super();
    }

	/**
	 * boilerplate servlet code
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}