import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;

/**
 * @author Nathanael Bishop 
 * CSC 4610 Project Fall 2017
 * This is a full featured 3-tier website that contains static content (Apache), dynamic content (Tomcat), 
 *   and a database (MySQL).  These Java Servlets pull static content and database content to present
 *   the user with a ratings and review website for various Cloud services. The focus in building this 
 *   was to showcase what I have learned this semester as well as a few new skills I picked up outside the
 *   classroom.
 */

public class LoadTemplate extends HttpServlet {
private static final long serialVersionUID = 1L;
	
	/**
	 * This helper method reads each line of index.html and displays it for the user to see. It is assumed
	 *   that the calling method will complete the rest of the page and end the appropriate HTML tags.
	 * Index.html contains all the HTML for the top and left sides of the page, as well as the CSS and 
	 *   JavaScript that each servlet can use.
	 * @param name the name of the user who is currently logged in
	 * @param out the calling methods PrintWriter, just gonna borrow it for a sec
	 */
	public static void loadTemplate(String name, PrintWriter out) {
		//load the file with the template that contains the head and CSS
		String printTemplate = "index.html";
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(printTemplate); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		//while the file contains data, print it for the user to see
		try {
			while((printTemplate=reader.readLine()) != null)
				//catch the <title> line so we can insert the users name into it
				if(printTemplate.equals("<title id='titleBarText'></title>"))
					out.println("<title>Welcome to CRR, "+name+"</title>");
				else
					out.println(printTemplate);
			inputStream.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}