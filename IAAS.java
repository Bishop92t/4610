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
	public String DB_NAME     = "crr";
	public String DB_URL      = "jdbc:mysql://localhost:3306/"+DB_NAME;
	public Connection conn = null;
	public Statement  stmt = null;
	public PrintWriter out;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		
		//set the file type and print writer
		response.setContentType("text/html;charset=UTF-8");
		out=response.getWriter();

		//get the users login name
		String name = User.getUserName(request.getCookies());
		
		//display the website template
		LoadTemplate.loadTemplate(name, out);
		
		//chose which db table to load and init the iaas object counter
		String DB_TABLE = "iaas";
		int counter     = 0;

		
		//connect to the db and read all table rows into array of objects
		try {
			//Open a connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(DB_URL,"root","ilovepizza");
	
			//Create and execute a query
			stmt = (Statement) conn.createStatement();
			String sql = "SELECT * FROM "+DB_TABLE+";";
			ResultSet rs = (ResultSet) stmt.executeQuery(sql);
			
			
			//start the table
			out.println("<div id='wrapper'>"+ 
						"<table id='keywords' cellspacing=0 cellpadding=0>"+
						"<thead><tr>"+
					    "<th><span>Name</span></th>"+
					    "<th><span>Lowest Price</span></th>"+
					    "<th><span>Highest Price</span></th>"+
			            "<th><span>Min # Cores</span></th>"+
			            "<th><span>Max # Cores</span></th>"+
			            "<th><span>Min RAM</span></th>"+
			            "<th><span>Max RAM</span></th>"+
			            "<th><span>Min Storage</span></th>"+
			            "<th><span>Max Storage</span></th>"+
			            "<th><span>Has GPU</span></th>"+
			            "<th><span>Operating Systems</span></th></tr></thead>");
			
			//initialize the array of IaaS objects
			Iaas[] iaas = new Iaas[100];
			String temp;
			
			//load all the results into an array of IaaS objects
			while(rs.next()) {
				//this is used to see if the name is already an object
				temp=rs.getString("name");
				
				//if the name hasn't been added then create new object 
				if(!findName(iaas, temp, counter)) {
					iaas[counter] = new Iaas(temp);
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
			
			//Loop through the array of IaaS objects and print each one in the table
			for(int i=0; i<counter; i++) {
				out.println("<tr><td>"+
							"<form action='/4610/Review' method='post'>"+
							"<input type='hidden' name='name' value="+name+">"+
							"<input type='hidden' name='isStorage' value='false'>"+
							"<input type='hidden' name='serviceName' value='"+iaas[i].getIaas()+"'>"+
							"<button type='submit'>"+iaas[i].getIaas()+"</button></form></td>\n"+
							"<td>"+iaas[i].getPriceLow()+"</td>\n"+
							"<td>"+iaas[i].getPriceHigh()+"</td>\n"+
							"<td>"+iaas[i].getCoresLow()+"</td>\n"+
							"<td>"+iaas[i].getCoresHigh()+"</td>\n"+
							"<td>"+iaas[i].getMemLow()+"</td>\n"+
							"<td>"+iaas[i].getMemHigh()+"</td>\n"+
							"<td>"+iaas[i].getStorageLow()+"</td>\n"+
							"<td>"+iaas[i].getStorageHigh()+"</td>\n"+
							"<td>"+iaas[i].getHasGPUTrue()+"</td>\n"+
							"<td>"+iaas[i].getOSStrings()+"</td></tr>\n");
			}
			out.println("</table>");
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

		//finally close out the html tags
		out.println("</td></tr></table></body></html>");
	}
	
	/**
	 * look through all the iaas objects for a name, return true if found otherwise return false
	 * @param iaas the array of iaas objects
	 * @param name the name we're looking for
	 * @param counter the number of 
	 * @return true if the name is found, otherwise false
	 */
	public boolean findName(Iaas[] iaas, String name, int counter) {
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
			this.numOs=-1;
			this.hasGPU=false;
		}
		
		public void setHasGPUTrue() {
			this.hasGPU=true;
		}
		
		public void setPriceLow(float priceLow) {
			this.priceLow=priceLow;
		}
		
		public void setPriceHigh(float priceHigh) {
			this.priceHigh=priceHigh;
		}
		
		public void setMemLow(float memLow) {
			this.memLow=memLow;
		}
		
		public void setMemHigh(float memHigh) {
			this.memHigh=memHigh;
		}
		
		public void setCoresLow(int coresLow) {
			this.coresLow=coresLow;
		}

		public void setCoresHigh(int coresHigh) {
			this.coresHigh=coresHigh;
		}
		
		public void setStorageLow(int storageLow) {
			this.storageLow=storageLow;
		}
		
		public void setStorageHigh(int storageHigh) {
			this.storageHigh=storageHigh;
		}
		
		/**
		 * add the OS Name and Version to the IaaS object and increment the counter 
		 * @param os_id the unique OS id number
		 */
		public void addOS(int os_id) {
			try {
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

		public String getIaas() {
			return iaasName;
		}
		
		public String getHasGPUTrue() {
			if(hasGPU)
				return "X";
			else
				return " ";
		}
		
		public String getPriceLow() {
			return "$"+priceLow+"/min";
		}
		
		public String getPriceHigh() {
			return "$"+priceHigh+"/min";
		}
		
		public String getMemLow() {
			return ""+memLow+"GB";
		}
		
		public String getMemHigh() {
			return ""+memHigh+"GB";
		}
		
		public int getCoresLow() {
			return coresLow;
		}

		public int getCoresHigh() {
			return coresHigh;
		}
		
		public String getStorageLow() {
			return ""+storageLow+"GB";
		}
		
		public String getStorageHigh() {
			return ""+storageHigh+"GB";
		}
		
		/**
		 * combines all the OS Names and Versions and returns them as a big String
		 * @return all the OS Names and Versions as a String
		 * num0s 0 = 1 result
		 *       1 = 2 results
		 */
		public String getOSStrings() {
			String temp="";
			for(int i=0; i<numOs; i++)
				temp+=osNameAndVer[i]+", ";
			return temp+osNameAndVer[numOs];
		}
	}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public IAAS() {
        super();
    }
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}