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
 * Servlet implementation class User
 */
public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String DB_NAME     = "crr";
	private static String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public User() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
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
				if(cookie.getName().equals("CCRLogin"))
					name = cookie.getValue();
		
		return name;
	}
	
	
	public static String getUserEmail(String name) {
		String userEmail = "";
		String DB_TABLE  = "login"; 

		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			Statement stmt = (Statement) conn.createStatement();
			String sql     = "SELECT * FROM "+DB_TABLE+" WHERE name='"+name+"';";
			ResultSet rs   = (ResultSet) stmt.executeQuery(sql);
			
			//loop through the result set and print in the table
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
		
		return userEmail;
	}
	
}