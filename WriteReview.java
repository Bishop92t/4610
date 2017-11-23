import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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

public class WriteReview extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String DB_TABLE = "review";
	private static String DB_NAME  = "crr";
	private static String DB_URL   = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";
	private static Connection conn = null;
	private static Statement stmt  = null;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		//load all the variables from the form data
		String name               = request.getParameter("name");
		String reviewText         = request.getParameter("reviewText");
		String serviceName        = request.getParameter("serviceName");
		String serviceType        = request.getParameter("serviceType");
		String hasExisitingRecord = request.getParameter("hasExistingRecord");

		try {
			//open a connection and prepare the sql statement variables
			Class.forName("com.mysql.jdbc.Driver");
			conn=(Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
			String sql;
			
			//if user already left a review for this service, delete old record 
			if(hasExisitingRecord.equals("true")) 
				deleteOldReview(serviceType, serviceName, name);

			//create the statement to write the data to the database
			sql = "INSERT INTO "+DB_TABLE+" VALUES ('"+name+"','"+reviewText+"',";

			if(serviceType.equals("storage"))
				sql += "TRUE,'','"+serviceName+"');";
			else
				sql += "FALSE,'"+serviceName+"','');";

System.out.println(sql);

			//taking a performance hit with prepareStatement to sanitize inputs
			stmt=(Statement) conn.prepareStatement(sql);
			stmt.executeUpdate(sql);
			conn.commit();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			//finally, attempt close the connection
			try {
				if(stmt!=null)
					stmt.close();
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		//store the relevant data in a cookie and send user back to Review
		Cookie servcCookie;
		Cookie isStorageCookie;
		
		servcCookie = new Cookie("CCRserviceName", serviceName);
		if(serviceType.equals("storage"))
			isStorageCookie = new Cookie("CCRisStorage", "true");
		else
			isStorageCookie = new Cookie("CCRisStorage", "false");
		
		//set both cookies to live for only 10 seconds
		isStorageCookie.setMaxAge(10); 
		servcCookie.setMaxAge(10); 

 		//add the cookie to the response returned to the client
 		response.addCookie(servcCookie);
 		response.addCookie(isStorageCookie);
 		response.sendRedirect("Review");
	}

	/**
	 * deletes an old review so a new one can be written in it's place
	 * @param serviceType IaaS or Storage based service
	 * @param serviceName the name of the service provider
	 * @param name the name of the person who's review is being updated
	 */
	private static void deleteOldReview(String serviceType, String serviceName, String name) {
		//start the delete sql statement
		String sql = "delete from "+DB_TABLE+" where login_name='"+name+"' and ";
		
		//if the service is storage then add Storage field to the delete statement
		if(serviceType.equals("storage"))
			sql += "storage_name='"+serviceName+"';";
		//else add IaaS field to the delete statement
		else
			sql += "iaas_name='"+serviceName+"';";
		
		//try to execute the statement
		try {
System.out.println(sql);
		//taking a performance hit with prepareStatement to sanitize inputs
		stmt=(Statement) conn.createStatement();
		stmt.executeUpdate(sql);
		conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public WriteReview() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}
