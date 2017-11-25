import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
	//database variables setup, note when writing to DB the DB_URL is different
	private static String DB_TABLE         = "review";
	private static String DB_NAME          = "crr";
	private static String DB_URL           = "jdbc:mysql://localhost:3306/"+DB_NAME+"?autoReconnect=true&relaxAutoCommit=true";
	private static PreparedStatement pstmt = null;
	private static Connection conn         = null;
	private static Statement stmt          = null;
	
	/**
	 * Servlet accepts a users new or edited review and makes the neccessary changes to the database
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
			//open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			conn=(Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
			
			//if user already left a review for this service, delete old record 
			if(hasExisitingRecord.equals("true")) 
				deleteOldReview(serviceType, serviceName, name);

			//taking a performance hit with prepareStatement to sanitize inputs
			String sql = "INSERT INTO "+DB_TABLE+" VALUES (?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);

			//sanitize the name and reviewText inputs
			pstmt.setString(1, name);
			pstmt.setString(2, reviewText);
			
			//if the service is a Storage type indicate in the db and store serviceName
			if(serviceType.equals("storage")) {
				pstmt.setBoolean(3, true);
				pstmt.setString(4, "");
				pstmt.setString(5, serviceName);
			}
			//else indicate the service is an IaaS type and store the serviceName
			else{
				pstmt.setBoolean(3, false);
				pstmt.setString(4, serviceName);
				pstmt.setString(5, "");
			}

			//now write the update to the db
			pstmt.executeUpdate();
			conn.commit();
		} catch (SQLException | ClassNotFoundException e) {
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

		//create a cookie for the serviceName and one for the boolean isStorage
		Cookie servcCookie;
		Cookie isStorageCookie;
		servcCookie = new Cookie("CCRserviceName", serviceName);
		if(serviceType.equals("storage"))
			isStorageCookie = new Cookie("CCRisStorage", "true");
		else
			isStorageCookie = new Cookie("CCRisStorage", "false");
		
		//cookies only need to live long enough for Review to read what provider the user was looking at
		isStorageCookie.setMaxAge(10); 
		servcCookie.setMaxAge(10); 

 		//add the cookie to the response returned to the client
 		response.addCookie(servcCookie);
 		response.addCookie(isStorageCookie);

 		//return the user to the Review page for the product they just reviewed
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
			stmt=(Statement) conn.createStatement();
			stmt.executeUpdate(sql);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * boilerplate servlet code
	 */
    public WriteReview() {
        super();
    }

	/**
	 * boilerplate servlet code
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}
