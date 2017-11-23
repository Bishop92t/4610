import java.io.IOException;
import java.io.PrintWriter;

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

public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private PrintWriter out;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//set the file type and print writer
		response.setContentType("text/html;charset=UTF-8");
		out=response.getWriter();

		//get the users login name
		String name = User.getUserName(request.getCookies());
//todo important: if login cookie isn't valid boot the user to the login screen
		
		//display the website template
		LoadTemplate.loadTemplate(name, out);
		
		//start the table for the right side and display the big CRR log
		out.println("<table><tr><td>"
				  + "<img src='http://52.26.169.0/pictures/logo.jpg'><br><br>"
				  + "</td></tr></table><br><br><br><br><br><br>");

		out.println(Review.getReviews(name));
			
		out.println("</td></tr></table></body></html>");
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