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

public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DB_TABLE       = "review";
	private static final String DB_NAME        = "crr";
	private static final String DB_URL         = "jdbc:mysql://localhost:3306/"+DB_NAME;
	private PrintWriter out;
	private Connection conn = null;
	private Statement  stmt = null;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//set the file type and print writer
		response.setContentType("text/html;charset=UTF-8");
		out=response.getWriter();

		//get the users login name
		String name = getUserName(request.getCookies());
//todo important: if login cookie isn't valid boot the user to the login screen
		
		//display the website template
		loadTemplate(name);
		
		//start the table for the right side and display the big CRR log
		out.println("<table><tr><td>"
				  + "<img src='http://52.26.169.0/pictures/logo.jpg'><br><br>"
				  + "</td></tr></table><br><br><br><br><br><br>");

		//start the table for the users own reviews 
		out.println("<div id='wrapper'><table id='keywords' cellspacing=0 cellpadding=0>"
				  + "<thead><tr>\n");
		out.println("<th><span>Service Name</span></th>\n"
				  + "<th><span>Your Reviews</span></th>\n"
				  + "<th><span>Type</span></th></tr></thead><tr>\n");
		
		try {
			//Open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
						
			//Create and execute a query for all of this users reviews
			stmt         = (Statement) conn.createStatement();
			String sql   = "SELECT * FROM "+DB_TABLE+" WHERE login_name='"+name+"';";
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);
			
			//loop through the result set and print in the table
			while(rs.next()) {
				Boolean isStorage = rs.getBoolean("isStorage");
				if(isStorage) {
					out.println("<tr><td>"+rs.getString("storage_name")+"</td>\n");
					out.println("<td>"+rs.getString("text")+"</td>\n");
					out.println("<td>Storage</td>\n</tr>");
				}
				else {
					out.println("<tr><td>"+rs.getString("iaas_name")+"</td>\n");
					out.println("<td>"+rs.getString("text")+"</td>\n");
					out.println("<td>IaaS</td>\n</tr>");
				}
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

		//finally close out the html tags and the big page table
		out.println("</td></tr></table></body></html>");
	}
	

	/**
	 * helper method to make doGet more readable. Load the Head and CSS template, 
	 *    print the users name in the title, the body template and JS sorting 
	 *    script, finally print the left side bar
	 * @param name the users name (passed from the previous page
	 */
	private void loadTemplate(String name) {
		//load the file with the template that contains the head and CSS
		String printTemplate = "headtemplate.html";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(printTemplate);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		//while the file contains data, print it for the user to see
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
		
		//load the template file that contains the body formatting (the sort script)
		printTemplate="bodytemplate.html";
		inputStream = getClass().getClassLoader().getResourceAsStream(printTemplate);
		reader = new BufferedReader(new InputStreamReader(inputStream));
		//while the file contains data, print it for the user to see
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
    public Home() {
        super();
    }
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}