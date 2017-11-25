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

public class Storage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String DB_NAME     = "crr";
	private String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME;
	private Connection conn    = null;
	private Statement  stmt    = null;
	private PrintWriter out;

	/**
	 * Servlet handles displaying all the Storage service providers. It builds a sortable HTML table that 
	 *   shows all the Storage providers along with info about them. The user can click on the providers name
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
		
		//chose which db table to load and init the storage object counter
		String DB_TABLE = "storage";
		int counter     = 0;
		
		//initialize the array of Storage Provider objects
		CloudStorageProvider[] cloudStorageProvider = new CloudStorageProvider[100];
		String temp;

		//connect to the db and read all table rows into array of objects
		try {
			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
	
			//Create and execute a query
			stmt = (Statement) conn.createStatement();
			String sql = "SELECT * FROM "+DB_TABLE+";";
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);
						
			//start the HTML table that will contain all the Storage service providers 
			out.println("<div id='wrapper'>" 
					  + "<table id='keywords' cellspacing=0 cellpadding=0>"
					  + "<thead><tr>"
					  + "<th><span>Name</span></th>"
					  + "<th><span>Encryption</span></th>"
					  + "<th><span>Lowest Price</span></th>"
					  + "<th><span>Highest Price</span></th>"
					  + "<th><span>Free Tier</span></th>"
					  + "<th><span>Public Dir</span></th>"
					  + "<th><span>Min Storage</span></th>"
					  + "<th><span>Max Storage</span></th>"
					  + "<th><span>Operating Systems</span></th></tr></thead>");

			//load all the results into an array of Storage Provider objects
			while(rs.next()) {
				//this is used to see if the name is already an object
				temp=rs.getString("name");
				
				//if the name hasn't been added then create new object 
				if(!findName(cloudStorageProvider, temp, counter)) {
					cloudStorageProvider[counter] = new CloudStorageProvider(temp);
					cloudStorageProvider[counter].setHasEncrypt(rs.getString("hasEncrypt"));
					cloudStorageProvider[counter].setPriceLow(rs.getFloat("priceLow"));
					cloudStorageProvider[counter].setPriceHigh(rs.getFloat("priceHigh"));
					cloudStorageProvider[counter].setHasFree(rs.getBoolean("hasFree"));
					cloudStorageProvider[counter].setHasPublic(rs.getBoolean("hasPublic"));
					cloudStorageProvider[counter].setStorageLow(rs.getFloat("storageLow"));
					cloudStorageProvider[counter].setStorageHigh(rs.getFloat("storageHigh"));
					cloudStorageProvider[counter].addOS(rs.getInt("os_id"));
					counter++;
				}
				//else just add the OS to the current object
				else {
					counter--;
					cloudStorageProvider[counter].addOS(rs.getInt("os_id"));
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
		//Loop through the array of storage provider objects and print each one in the HTML table
		for(int i=0; i<counter; i++) 
			out.println("<tr><td>"
					  + "<form action='/4610/Review' method='post'>"
					  + "<input type='hidden' name='name' value="+name+">"
					  + "<input type='hidden' name='isStorage' value='true'>"
					  + "<input type='hidden' name='serviceName' value='"+cloudStorageProvider[i].getCloudStorageName()+"'>"
					  + "<button type='submit'>"+cloudStorageProvider[i].getCloudStorageName()+"</button></form></td>\n"
					  + "<td>"+cloudStorageProvider[i].getHasEncrypt()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getPriceLow()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getPriceHigh()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getHasFree()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getHasPublic()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getStorageLow()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getStorageHigh()+"</td>\n"
					  + "<td>"+cloudStorageProvider[i].getOSStrings()+"</td></tr>\n");

		//finally close out the little and big table, and the html tags
		out.println("</table></td></tr></table></body></html>");
	}
	
	/**
	 * look through all the Storage objects for a name, return true if found otherwise return false
	 * @param cloudStorageProvider the array of Storage objects
	 * @param name the storage providers name we're looking for
	 * @param counter the number of storage objects in this storage Array
	 * @return true if the name is found, otherwise false
	 */
	public boolean findName(CloudStorageProvider[] cloudStorageProvider, String name, int counter) {
		for(int i=0; i<counter; i++) 
			if(cloudStorageProvider[i].getCloudStorageName().equals(name))
				return true;
		return false;
	}
	
	/**
	 * construct and maintain the Storage Providers object
	 */
	public class CloudStorageProvider {
		private String cloudStorageName, hasEncrypt;
		private String[] osNameAndVer=new String[50];
		private boolean hasFree, hasPublic;
		private float priceLow, priceHigh, storageLow, storageHigh;
		private int numOs;

		/**
		 * The constructor for the Storage object, assigns the name and the 
		 *    counter for the number of OS's this object has
		 * @param cloudStorageName the name of this object (Storage providers name)
		 */
		public CloudStorageProvider(String cloudStorageName) {
			this.cloudStorageName=cloudStorageName;
			//this is an array indexer, it will start at 0 as soon as an OS is added
			this.numOs=-1;
		}
		
		/**
		 * Set this particular storage providers encryption policy.  Most are either
		 *   "native", "no" or "3rd party"
		 * @param hasEncrypt This storage providers encryption option as a String
		 */
		public void setHasEncrypt(String hasEncrypt) {
			this.hasEncrypt=hasEncrypt;
		}

		/**
		 * Set this particular storage providers hasFree to true if they have a free tier
		 *   available, otherwise false
		 * @param hasFree This storage providers free tier (boolean)
		 */
		public void setHasFree(boolean hasFree) {
			this.hasFree=hasFree;
		}
		
		/**
		 * Set this particular storage providers hasPublic to true if they allow users
		 *   to make their files available to the public, otherwise false
		 * @param hasPublic This storage providers resources (boolean)
		 */
		public void setHasPublic(boolean hasPublic) {
			this.hasPublic=hasPublic;
		}
		
		/**
		 * Set this particular storage providers lowest price tier
		 * @param priceLow This storage providers lowest price tier as a float
		 */
		public void setPriceLow(float priceLow) {
			this.priceLow=priceLow;
		}
		
		/**
		 * Set this particular storage providers highest price tier
		 * @param priceHigh This storage providers highest price tier as a float
		 */
		public void setPriceHigh(float priceHigh) {
			this.priceHigh=priceHigh;
		}
				
		/**
		 * Set this particular storage providers lowest tier storage space
		 * @param storageLow This storage providers lowest tier storage limit as a float
		 */
		public void setStorageLow(float storageLow) {
			this.storageLow=storageLow;
		}
		
		/**
		 * Set this particular storage providers 
		 * @param storageHigh This storage providers highest tier storage limit as a float
		 */
		public void setStorageHigh(float storageHigh) {
			this.storageHigh=storageHigh;
		}
		
		/**
		 * add the OS Name and Version to the Storage object and increment the counter 
		 *   by looking through the 'os' table for a unique id number
		 * @param os_id the unique OS id number
		 */
		public void addOS(int os_id) {
			try {
				//adding the first OS starts the array index at 0
				numOs++;

				//create the SQL statement to look for the os_id (unique)
				Statement stmtOs = (Statement) conn.createStatement();
				String sql = "SELECT * FROM os WHERE id="+os_id+";";

				//set the string to be the OS Name and OS Version
				ResultSet rsOs = (ResultSet) stmtOs.executeQuery(sql);
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
		 * Retrieve this Storage providers name
		 * @return the providers name as a String
		 */
		public String getCloudStorageName() {
			return cloudStorageName;
		}
		
		/**
		 * Retrieve this Storage providers encryption option
		 * @return the providers encryption option as a String
		 */
		public String getHasEncrypt() {
			return hasEncrypt;
		}
		
		/**
		 * Retrieve this Storage providers free service option. Returns "X" if they have a free tier
		 *   otherwise return a blank String.
		 * @return the providers free service option as a String
		 */
		public String getHasFree() {
			if(hasFree)
				return "X";
			else
				return " ";
		}

		/**
		 * Retrieve this Storage providers public facting storage option. Returns "X" if they allow
		 *   users to face their storage publically, or blank if all storage is private.
		 * @return the providers public facing storage option as a String
		 */
		public String getHasPublic() {
			if(hasPublic)
				return "X";
			else
				return " ";
		}
		
		/**
		 * Retrieve this Storage providers lowest tier price.
		 * @return the providers lowest tier price as a String
		 */
		public String getPriceLow() {
			return "$"+priceLow;
		}
		
		/**
		 * Retrieve this Storage providers highest tier price.
		 * @return the providers highest tier price as a String
		 */
		public String getPriceHigh() {
			return "$"+priceHigh;
		}
				
		/**
		 * Retrieve this Storage providers lowest tier storage capacity.
		 * @return the providers lowest tier storage capacity as a String
		 */
		public String getStorageLow() {
			return ""+storageLow+" TB";
		}
		
		/**
		 * Retrieve this Storage providers highest tier storage capacity.
		 * @return the providers highest tier storage capacity as a String
		 */
		public String getStorageHigh() {
			return ""+storageHigh+" TB";
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
    public Storage() {
        super();
    }

	/**
	 * boilerplate servlet code
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}