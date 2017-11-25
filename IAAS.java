import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

public class IAAS extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String DB_NAME  = "crr";
	private String DB_URL   = "jdbc:mysql://localhost:3306/"+DB_NAME;
	private Connection conn = null;
	private Statement  stmt = null;
	private PrintWriter out;

	/**
	 * Servlet handles displaying all the IaaS service providers. It builds a sortable HTML table that 
	 *   shows all the IaaS providers along with info about them. The user can click on the providers name
	 *   and see all the reviews about that provider.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		//set the file type and print writer
		response.setContentType("text/html;charset=UTF-8");
		out=response.getWriter();

		//get the users login name, boot them to login screen if they aren't logged in
		String name = User.getUserName(request.getCookies());
		if(name.equals(""))
			response.sendRedirect("http://52.26.169.0/4610.html");
		
		//display the website template
		LoadTemplate.loadTemplate(name, out);
		
		//chose which db table to load and init the iaas object counter
		String DB_TABLE = "iaas";
		int counter     = 0;

		//initialize the array of IaaS objects and a temp variable for the providers name
		Iaas[] iaas = new Iaas[100];
		String tempProviderName;
		
		try {
			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
	
			//Create and execute a query
			stmt = (Statement) conn.createStatement();
			String sql = "SELECT * FROM "+DB_TABLE+";";
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);
			
			//start the table
			out.println("<div id='wrapper'>"
					  + "<table id='keywords' cellspacing=0 cellpadding=0>"
					  + "<thead><tr>"
					  + "<th><span>Name</span></th>"
					  + "<th><span>Lowest Price</span></th>"
					  + "<th><span>Highest Price</span></th>"
					  + "<th><span>Min # Cores</span></th>"
					  + "<th><span>Max # Cores</span></th>"
					  + "<th><span>Min RAM</span></th>"
					  + "<th><span>Max RAM</span></th>"
					  + "<th><span>Min Storage</span></th>"
					  + "<th><span>Max Storage</span></th>"
					  + "<th><span>Has GPU</span></th>"
					  + "<th><span>Operating Systems</span></th></tr></thead>");
			
			//load all the results into an array of IaaS objects
			while(rs.next()) {
				//this is used to see if the name is already an object
				tempProviderName=rs.getString("name");
				
				//if the name hasn't been added then create new object 
				if(!isExistingNameInIaasArray(iaas, tempProviderName, counter)) {
					iaas[counter] = new Iaas(tempProviderName);
					if(rs.getBoolean("hasGPU"))    //this value defaults to false
						iaas[counter].setHasGPUTrue();
					iaas[counter].setPriceLow(rs.getFloat("priceLow"));
					iaas[counter].setPriceHigh(rs.getFloat("priceHigh"));
					iaas[counter].setCoresLow(rs.getInt("coresLow"));
					iaas[counter].setCoresHigh(rs.getInt("coresHigh"));
					iaas[counter].setMemLow(rs.getFloat("memLow"));
					iaas[counter].setMemHigh(rs.getFloat("memHigh"));
					iaas[counter].setStorageLow(rs.getInt("storageLow"));
					iaas[counter].setStorageHigh(rs.getInt("storageHigh"));
					iaas[counter].addOS(rs.getInt("os_id"));
					counter++;
				}
				//else just add the OS to the current object
				else {
					counter--;
					iaas[counter].addOS(rs.getInt("os_id"));
					counter++;
				}
			}
			

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

		//Loop through the array of IaaS objects and print each one in the table
		for(int i=0; i<counter; i++) 
			out.println("<tr><td>"
					  +	"<form action='/4610/Review' method='post'>"
					  + "<input type='hidden' name='name' value="+name+">"
					  + "<input type='hidden' name='isStorage' value='false'>"
					  + "<input type='hidden' name='serviceName' value='"+iaas[i].getIaas()+"'>"
					  + "<button type='submit'>"+iaas[i].getIaas()+"</button></form></td>\n"
					  + "<td>"+iaas[i].getPriceLow()+"</td>\n"
					  + "<td>"+iaas[i].getPriceHigh()+"</td>\n"
					  + "<td>"+iaas[i].getCoresLow()+"</td>\n"
					  + "<td>"+iaas[i].getCoresHigh()+"</td>\n"
					  + "<td>"+iaas[i].getMemLow()+"</td>\n"
					  + "<td>"+iaas[i].getMemHigh()+"</td>\n"
					  + "<td>"+iaas[i].getStorageLow()+"</td>\n"
					  + "<td>"+iaas[i].getStorageHigh()+"</td>\n"
					  + "<td>"+iaas[i].getHasGPUTrue()+"</td>\n"
					  + "<td>"+iaas[i].getOSStrings()+"</td></tr>\n");

		//finally close out the little and big table, and the html tags
		out.println("</table></td></tr></table></body></html>");
	}
	
	/**
	 * look through all the iaas objects for a name, return true if found otherwise return false
	 * @param iaas the array of iaas objects
	 * @param name the name we're looking for
	 * @param counter the number of IaaS objects in this IaaS array
	 * @return true if the name is found, otherwise false
	 */
	public boolean isExistingNameInIaasArray(Iaas[] iaas, String name, int counter) {
		for(int i=0; i<counter; i++) 
			if(iaas[i].getIaas().equals(name))
				return true;
		return false;
	}
	
	/**
	 * construct and maintain the IaaS object
	 */
	public class Iaas {
		private String iaasName;
		private String[] osNameAndVer=new String[50];
		private boolean hasGPU;
		private float priceLow, priceHigh, memLow, memHigh;
		private int coresLow, coresHigh, storageLow, storageHigh, numOs;

		/**
		 * The constructor for the IaaS object, assigns the name, the counter
		 *   for the number of OS's this object has, and default hasGPU to false
		 * @param iaasName the name of this object (the IaaS's name)
		 */
		public Iaas(String iaasName) {
			this.iaasName=iaasName;
			//this is an array indexer, it will start at 0 as soon as an OS is added
			this.numOs=-1; 
			this.hasGPU=false;
		}
		
		/**
		 * Set to true if the IaaS provider has a GPU equipped service (it's defaulted to false)
		 */
		public void setHasGPUTrue() {
			this.hasGPU=true;
		}
		
		/**
		 * Set's the lowest price this IaaS provider has
		 * @param priceLow the lowest price as a float
		 */
		public void setPriceLow(float priceLow) {
			this.priceLow=priceLow;
		}
		
		/**
		 * Set's the highest price this IaaS provider has
		 * @param priceHigh the highest price as a float
		 */
		public void setPriceHigh(float priceHigh) {
			this.priceHigh=priceHigh;
		}
		
		/**
		 * Set's the least amount of RAM this IaaS provider has
		 * @param memLow the lowest RAM as a float
		 */
		public void setMemLow(float memLow) {
			this.memLow=memLow;
		}
		
		/**
		 * Set's the most amount of RAM this IaaS provider has
		 * @param memHigh the highest RAM as a float
		 */
		public void setMemHigh(float memHigh) {
			this.memHigh=memHigh;
		}
		
		/**
		 * Set's the least number of available CPU's this IaaS provider has
		 * @param coresLow the least number of cores as an int
		 */
		public void setCoresLow(int coresLow) {
			this.coresLow=coresLow;
		}

		/**
		 * Set's the most number of available CPU's this IaaS provider has
		 * @param coresHigh the most number of cores as an int
		 */
		public void setCoresHigh(int coresHigh) {
			this.coresHigh=coresHigh;
		}
		
		/**
		 * Set's the lowest storage this IaaS provider has
		 * @param the least storage as an int
		 */
		public void setStorageLow(int storageLow) {
			this.storageLow=storageLow;
		}
		
		/**
		 * Set's the highest storage this IaaS provider has
		 * @param the most storage as an int
		 */
		public void setStorageHigh(int storageHigh) {
			this.storageHigh=storageHigh;
		}
		
		/**
		 * add the OS Name and Version to the IaaS object and increment the counter 
		 *   by looking through the 'os' table for a matching unique OS id
		 * @param os_id the unique OS id number
		 */
		public void addOS(int os_id) {
			try {
				//adding the first OS starts the array index at 0
				numOs++;

				//create the SQL statement to look for the os_id (unique)
				Statement stmtOs = (Statement) conn.createStatement();
				String sql = "SELECT * FROM os WHERE id="+os_id+";";
				ResultSet rsOs = (ResultSet) stmtOs.executeQuery(sql);

				//since it's a unique ID #, only one result to look at 
				rsOs.next();

				//write the result of the query to the iaas object
				this.osNameAndVer[numOs]=rsOs.getString("name")+" "+rsOs.getString("version");

				//finally close the resultset and statement
				if(rsOs!=null)
					rsOs.close();
				if(stmtOs!=null)
					stmtOs.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Retrieve this IaaS providers name
		 * @return the providers name as a String
		 */
		public String getIaas() {
			return iaasName;
		}
		
		/**
		 * Retrieve if this IaaS provider has a GPU enabled service
		 * @return true if this provider has GPU, otherwise false
		 */
		public String getHasGPUTrue() {
			if(hasGPU)
				return "X";
			else
				return " ";
		}
		
		/**
		 * Retrieve this IaaS providers lowest price service
		 * @return the lowest price service as a String
		 */
		public String getPriceLow() {
			return "$"+priceLow+"/min";
		}
		
		/**
		 * Retrieve this IaaS providers highest price service
		 * @return the highest price service as a String
		 */
		public String getPriceHigh() {
			return "$"+priceHigh+"/min";
		}
		
		/**
		 * Retrieve this IaaS providers lowest RAM service
		 * @return the lowest RAM service as a String
		 */
		public String getMemLow() {
			return ""+memLow+"GB";
		}
		
		/**
		 * Retrieve this IaaS providers highest RAM service
		 * @return the highest RAM service as a String
		 */
		public String getMemHigh() {
			return ""+memHigh+"GB";
		}
		
		/**
		 * Retrieve this IaaS providers lowest number of CPU cores
		 * @return the lowest number of CPU cores as an int
		 */
		public int getCoresLow() {
			return coresLow;
		}

		/**
		 * Retrieve this IaaS providers highest number of CPU cores
		 * @return the highest number of CPU cores as an int
		 */
		public int getCoresHigh() {
			return coresHigh;
		}
		
		/**
		 * Retrieve this IaaS providers lowest amount of disk storage
		 * @return the lowest amount of disk storage as a String
		 */
		public String getStorageLow() {
			return ""+storageLow+"GB";
		}
		
		/**
		 * Retrieve this IaaS providers highest amount of disk storage
		 * @return the highest amount of disk storage as a String
		 */
		public String getStorageHigh() {
			return ""+storageHigh+"GB";
		}
		
		/**
		 * combines all the OS Names and Versions and returns them as a big comma delimited String
		 *   (note: numOS is the array indexer, so it starts at 0)
		 * @return all the OS Names and Versions as a String
		 */
		public String getOSStrings() {
			String temp="";
			for(int i=0; i<numOs; i++)
				temp+=osNameAndVer[i]+", ";
			return temp+osNameAndVer[numOs];
		}
	}
	
	/**
	 * boilerplate servlet code
	 */
    public IAAS() {
        super();
    }

	/**
	 * boilerplate servlet code
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}