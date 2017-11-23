import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	private String DB_TABLE    = "review";
	private String DB_NAME     = "crr";
	private String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";	
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
		
		/**
		 * If WriteReview.java calls, data will be via cookie
		 * If IAAS.java or Storage.java calls, data will be via a form post
		 */
		String name        = getUserName(request.getCookies());
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
		loadTemplate(name);
		
		//connect to the db and read all table rows into array of objects
		try {
			//start the table
			out.println("<div id='wrapper'>"+ 
						"<table id='keywords' cellspacing=0 cellpadding=0>"+
						"<thead><tr>\n"+
					    "<th><span>Name</span></th>\n"+
					    "<th><span>Review</span></th>\n"+
			            "<th><span>User</span></th></tr></thead><tr>\n");

			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//Create and execute a query for either the IaaS or Storage name fields
			stmt = (Statement) conn.createStatement();
			String sql = "SELECT * FROM "+DB_TABLE+" WHERE isStorage=FALSE AND iaas_name='"+serviceName+"';";
			if(isStorage)
				sql = "SELECT * FROM "+DB_TABLE+" WHERE isStorage=TRUE AND storage_name='"+serviceName+"';";
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);
			
			//print the entire result set in the table
			while(rs.next()) {
				//if storage print the storage column
				if(isStorage)
					out.println("<td>"+rs.getString("storage_name")+"</td>\n");
				//else print the iaas column
				else
					out.println("<td>"+rs.getString("iaas_name")+"</td>\n");
				//now print the review text and the users name
				out.println("<td>"+rs.getString("text")+"</td>\n");
				out.println("<td>"+rs.getString("login_name")+"</td>\n");
			}
			
			//close out the table tags
			out.println("</tr></table>");
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

		createOrEditReview(name, serviceName, isStorage);
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
		
	
	/**
	 * helper method to make doGet more readable. Load the Head and CSS template, 
	 *    print the users name in the title, the body template and JS sorting 
	 *    script, finally print the left side bar
	 * @param name the users name (passed from the previous page
	 */
	private void loadTemplate(String name) {
		//print the template that contains the head and CSS
		String printTemplate = "headtemplate.html";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(printTemplate);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			while((printTemplate=reader.readLine()) != null)
				out.println(printTemplate);
			inputStream.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Print the title with user name
		out.println("<title>C R R welcomes you "+name+"</title>");
		
		//print the template that contains the body formatting (the sort script)
		printTemplate="bodytemplate.html";
		inputStream = getClass().getClassLoader().getResourceAsStream(printTemplate);
		reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			while((printTemplate=reader.readLine()) != null)
				out.println(printTemplate);
			inputStream.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//big table contains all of the user viewable content
		out.println("<table><tr><td class='bigtable'>");

		//small table for the left side bar
		out.println("<table>");
		
		//left side bar link to the users home
		out.println("<tr><td><form action='/4610/Home' method='post'>" 
				  + "<input type='hidden' name='name' value="+name+">"  
				  + "<input type='image' src='http://52.26.169.0/pictures/logo.jpg' width=200 alt='Submit'>" 
				  + "</form><br><br><br><br></td></tr>");

		//left side bar link to IAAS
		out.println("<tr><td><form action='/4610/IAAS' method='post'>"
				  + "<input type='hidden' name='name' value="+name+">" 
				  + "<input type='image' src='http://52.26.169.0/pictures/iaas.jpg' width=200 alt='Submit'>"
				  + "</form><br><br></td></tr>");
		
		//left side bar link to Storage and end the small table
		out.println("<tr><td><form action='/4610/Storage' method='post'>"
				  + "<input type='image' src='http://52.26.169.0/pictures/storage.jpg' width=200 alt='Submit'>"
				  + "</form><br><br><br></td></tr></table>");
		
		//wrap up the left side bar and start the user content that goes on the right
		out.println("</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("<td class='bigtable'>");
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
	 * Pass this helper method an array of cookies and it will attempt to find one that's named CCRLogin
	 *   signaling that this cookie is the valid login name
	 * @param cookies an array of cookies from the HttpServletRequest
	 * @return the value of that named cookie or "" if nothing found
	 */
	private String getUserName(Cookie[] cookies){
		String name = "";
		
		//look through the array of cookies for one named CCRLogin
		if(cookies != null)
			for(Cookie cookie : cookies) 
				if(cookie.getName().equals("CCRLogin"))
					name = cookie.getValue();
		
		return name;
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
