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
	private static String DB_TABLE = "review";
	private static String DB_NAME  = "crr";
	private static String DB_URL   = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";	
	private Connection conn        = null;
	private Statement  stmt        = null;
	private PrintWriter out;
	
	/**
	 * This servlet lets the user see other users review of a specific service provider as well as write or edit their
	 *   own reviews.
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
		
		//display all the reviews in an HTML table
		out.println(getReviews(serviceName, isStorage));

		//display the users past review if they have any, otherwise give them a blank form 
		createOrEditReview(name, serviceName, isStorage);
		
		//finally close out the html tags and the big page table
		out.println("</td></tr></table></body></html>");
	}

	/**
	 * Display an HTML form the user can interact with. They can see their past review of this
	 *   specific provider and edit it if they wish, or they will see a blank form they can 
	 *   use to write a new review.
	 * Note, user input sanitization is handled by WriteReview.doGet()  This method only reads
	 *   from the db using already sanitized user input.
	 * @param name the user who's adding or editing a review
	 * @param serviceName the service providers name
	 * @param isStorage true if this provider is Storage based, false if IaaS
	 */
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

 		//this is for future use, true/false value does nothing currently
		return false;
	}

	/**
	 * Retrieve all the reviews that have been left for this particular service provider.
	 *   Data will be returned as a self contained HTML table
	 * @param serviceName the service providers name
	 * @param isStorage true if the service provider is storage based, false if IaaS
	 * @return the HTML table containing all the reviews, as a String
	 */
	public static String getReviews(String serviceName, boolean isStorage) {
		//setup the table we'll be working on, the output String and a counter
		String DB_TABLE    = "review";
		String userReviews = "";
		int numUserReviews = 0;
		       
		//start the string with the correct image
		userReviews += "<div id='wrapper'><img src='http://52.26.169.0/pictures/"+serviceName+".png' class='center'><br>\n";
		
		//next add the table headings
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
	
	/**
	 * Retrieve all the reviews from the database that have been written by this particular user.
	 *   Sends back as a fully self contained HTML table.
	 * @param userName the name of the user who's reviews are to be retrieved
	 * @return the HTML table containing all the user's reviews, as a String
	 */
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
				
				//if isStorage is marked true, retrieve service name from the storage_name column
				if(isStorage) 
					serviceName = rs.getString("storage_name");
				//else retrieve service name from the iaas_name column
				else
					serviceName = rs.getString("iaas_name");
				
				//now make the entire row for this user review
				userReviews += "<tr><td><img src='http://52.26.169.0/pictures/"+serviceName+".png' class='tinyimage'></td>\n"
							+  "<td class='littletable'>"+serviceName+"</td>\n"
							+  "<td class='littletable'>"+rs.getString("text")+"</td>\n"
							+  "<td class='littletable'>Storage</td>\n</tr>";
			}
			
			//if no reviews were found, print that in the table
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
	
	/**
	 * Given an array of Cookie objects, look for one named "CCRserviceName", if it exists return it's value
	 *   which should be the name of the service provider
	 * @param cookies an array of Cookie objects
	 * @return the name of the service contained in the CCRserviceName cookie, or "" if it doesn't exist (as a String)
	 */
	private String getServiceName(Cookie[] cookies){
		String serviceName = "";
		
		//look through the array of cookies for one named CCRserviceName
		if(cookies != null)
			for(Cookie cookie : cookies) 
				//if found, set it's value as the String we'll be returning
				if(cookie.getName().equals("CCRserviceName"))
					serviceName = cookie.getValue();

		return serviceName;
	}
	
	/**
	 * Given an array of Cookie objects, look for one named "CCRisStorage", if it exists return it's value
	 *   which should be a boolean that indicates whether this service provider is storage or IaaS based
	 * @param cookies an array of Cookie objects
	 * @return true if the service provider is storage based, false if they are an IaaS provider (boolean)
	 */
	private boolean getIsStorage(Cookie[] cookies) {
		String booleanInsideCookie = "";
		
		//look through the array of cookies for one named CCRisStorage
		if(cookies != null)
			for(Cookie cookie : cookies) 
				//if found, grab its value
				if(cookie.getName().equals("CCRisStorage"))
					booleanInsideCookie = cookie.getValue();
		
		//catch statement that shouldn't occur, but if the cookie is empty return false and print to terminal
		if(booleanInsideCookie.equals("")){
			System.out.println("cookie not found in Review, it should have been found");
			return false;
		}
			
		//cookies contain strings, so if the string was "true" then return a boolean true value
		if(booleanInsideCookie.equals("true"))
			return true;
		//else return a boolean false value
		else
			return false;
	}
	
	/**
	 * boilerplate servlet code
	 */
    public Review() {
        super();
    }

	/**
	 * boilerplate servlet code
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}