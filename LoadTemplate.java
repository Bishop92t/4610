import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;

public class LoadTemplate extends HttpServlet {
private static final long serialVersionUID = 1L;
	
	public static void loadTemplate(String name, PrintWriter out) {
		//load the file with the template that contains the head and CSS
		String printTemplate = "index.html";
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(printTemplate); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		//while the file contains data, print it for the user to see
		try {
			while((printTemplate=reader.readLine()) != null)
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
