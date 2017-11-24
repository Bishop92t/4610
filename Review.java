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

public class Review extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String DB_TABLE    = "review";
	private static String DB_NAME     = "crr";
	private static String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";	
	private Connection conn = null;
	private Statement  stmt = null;
	private PrintWriter out;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//set the file type and print writer
		response.setContentType("text/html;charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		out=response.getWriter();

		//get the users login name, boot them to login screen if they aren't logged in
		String name = User.getUserName(request.getCookies());
		if(name.equals(""))
			response.sendRedirect("http://52.26.169.0/4610.html");
		
		/**
		 * If WriteReview.java calls, data will be via cookie
		 * If IAAS.java or Storage.java calls, data will be via a form post
		 */
		String serviceName = getServiceName(request.getCookies());
		boolean isStorage  = false;
		
		//if the serviceName isnt blank, a cookie was found so find out service type
		if(!serviceName.equals(""))
			isStorage  = getIsStorage(request.getCookies());
		//else this info will come from form data (from IAAS or Storage servlets)
		else {
			serviceName = request.getParameter("serviceName");
			//isStorage defaults to false, only need to check if the form is true
			if(request.getParameter("isStorage").equals("true"))
				isStorage = true;
		}
			
		//display the website template
		LoadTemplate.loadTemplate(name, out);
		
		out.println(getReviews(serviceName, isStorage));

		createOrEditReview(name, serviceName, isStorage);
		
		//finally close out the html tags and the big page table
		out.println("</td></tr></table></body></html>");
	}

	private Boolean createOrEditReview(String name, String serviceName, boolean isStorage) {
		try {
			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//create the statement that will be used to query the DB
			stmt = (Statement) conn.createStatement();
			
			//look for any reviews from this user for this service name
			String sql = "SELECT * FROM "+DB_TABLE+" WHERE login_name='"+name+"'"
					   + "AND storage_name='"+serviceName+"'"
					   + "OR iaas_name='"+serviceName+"';";

			//now execute the query into a resultset
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);
			
			//start the user review form inside a table
			out.println("<br><br><br><br><br>\n");
			out.println("<table><tr><td>");
			out.println("<form action='http://52.24.2.46:8080/4610/WriteReview' method='post' accept-charset='UTF-8'>\n");
			out.println("<label>Your review of this service:</label><br>"
			          + "<input type='hidden' name='name' value="+name+"> \n"
					  + "<input type='hidden' name='serviceName' value='"+serviceName+"'> \n");

			//include in form which table we want to add this review to
			if(isStorage) 
				out.println("<input type='hidden' name='serviceType' value='storage'>\n");
			else
				out.println("<input type='hidden' name='serviceType' value='iaas'>\n");
			
			
			//if user has left a review put it in the form for editing
			out.println("<textarea name='reviewText' class='bigtextbox' maxlength='250'>");
			if(rs.next()) { 
				out.println(rs.getString("text")+"</textarea>\n");
		        out.println("<input type='hidden' name='hasExistingRecord' value='true'>");
			}
			//otherwise present an empty form
			else { 
				out.println("</textarea>\n");
				out.println("<input type='hidden' name='hasExistingRecord' value='false'>");
			}
			
			//now place the submit button and close the table tags
			out.println("<button type='submit'>Publish Review</button></form>"
					  + "</td></tr></table>");
				
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
 
		return false;
	}

	public static String getReviews(String serviceName, boolean isStorage) {
		//setup the table we'll be working on, the output String and a counter
		String DB_TABLE    = "review";
		String userReviews = "";
		int numUserReviews = 0;
		       
		//start the string with the correct image
		userReviews += "<div id='wrapper'><img src='http://52.26.169.0/pictures/"+serviceName+".png' class='center'><br>\n";
		
		userReviews += "<table id='keywords' cellspacing=0 cellpadding=0><thead><tr>\n"
				    +  "<th><span>Service Name</span></th>\n"
				    +  "<th><span>User Reviews</span></th>\n"
				    +  "<th><span>User Name</span></th></tr></thead>\n";

		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//Create and execute a query for all of this users reviews
			Statement stmt = (Statement) conn.createStatement();
			String sql;
			if(isStorage)
				sql = "SELECT * FROM "+DB_TABLE+" WHERE storage_name='"+serviceName+"';";
			else
				sql = "SELECT * FROM "+DB_TABLE+" WHERE iaas_name='"+serviceName+"';";
				
			ResultSet rs   = (ResultSet) stmt.executeQuery(sql);
			
			//loop through the result set and print in the table
			while(rs.next()) {
				numUserReviews++;
				userReviews += "<tr><td class='littletable'>"+serviceName+"</td>\n"
						    +  "<td class='littletable'>"+rs.getString("text")+"</td>\n"
						    +  "<td class='littletable'>"+rs.getString("login_name")+"</td></tr>";
			}
			
			if(numUserReviews==0)
				userReviews += "<tr><td colspan=3>No Reviews Found</td></tr>\n";
			
			//close out the table tags
			userReviews += "</tr></table>";
			
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
		
		return userReviews;
	}
	
	public static String getReviews(String userName) {
		//setup the table we'll be working on, the output String and a counter
		String DB_TABLE    = "review";
		String userReviews = "";
		String serviceName = "";
		int numUserReviews = 0;
		       
		//start the table for the users own reviews 
		userReviews += "<div id='wrapper'><table id='keywords' cellspacing=0 cellpadding=0>"
				    +  "<thead><tr><th>&nbsp;</th>\n";
		userReviews += "<th><span>Service Name</span></th>\n"
				    +  "<th><span>Your Reviews</span></th>\n"
				    +  "<th><span>Type</span></th></tr></thead><tr>\n";
		
		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//Create and execute a query for all of this users reviews
			Statement stmt = (Statement) conn.createStatement();
			String sql     = "SELECT * FROM "+DB_TABLE+" WHERE login_name='"+userName+"';";
			ResultSet rs   = (ResultSet) stmt.executeQuery(sql);
			
			//loop through the result set and print in the table
			while(rs.next()) {
				numUserReviews++;
				Boolean isStorage = rs.getBoolean("isStorage");
				if(isStorage) 
					serviceName = rs.getString("storage_name");
				else
					serviceName = rs.getString("iaas_name");
				
				userReviews += "<tr><td><img src='http://52.26.169.0/pictures/"+serviceName+".png' class='tinyimage'></td>\n"
							+  "<td class='littletable'>"+serviceName+"</td>\n"
							+  "<td class='littletable'>"+rs.getString("text")+"</td>\n"
							+  "<td class='littletable'>Storage</td>\n</tr>";
				
			}
			
			if(numUserReviews==0)
				userReviews += "<tr><td colspan=3>No Reviews Found</td></tr>";
			
			//close out the table tags
			userReviews += "</tr></table>";
			
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
		
		return userReviews;
	}
	
	private String getServiceName(Cookie[] cookies){
		String serviceName = "";
		
		//look through the array of cookies for one named CCRserviceName
		if(cookies != null)
			for(Cookie cookie : cookies) 
				if(cookie.getName().equals("CCRserviceName"))
					serviceName = cookie.getValue();

		return serviceName;
	}
	
	private boolean getIsStorage(Cookie[] cookies) {
		String booleanInsideCookie = "";
		
		//look through the array of cookies for one named CCRisStorage
		if(cookies != null)
			for(Cookie cookie : cookies) 
				if(cookie.getName().equals("CCRisStorage"))
					booleanInsideCookie = cookie.getValue();
		
		if(booleanInsideCookie.equals("")){
			System.out.println("cookie not found in Review, it should have been found");
			return false;
		}
			
	
		if(booleanInsideCookie.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public Review() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
