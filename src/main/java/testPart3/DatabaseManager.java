package testPart3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DatabaseManager {
	
	private DataSource ds;
	private Connection con;
	
	public DatabaseManager() throws SQLException{
		try {
			Context ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/fypStats");
		  } catch (NamingException e) {
			System.out.print("Error establishing connection");
		  }
		
		
	}
	
	public void addSimulator(int totalTime) throws SQLException{
		con = ds.getConnection();
		String sqlTime = (totalTime/3600) + ":" + ((totalTime/60)%60) + ":" + (totalTime%60);
		System.out.println(sqlTime);
		Time durationOfSim = java.sql.Time.valueOf(sqlTime);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "INSERT INTO Simulators (Timestamp , Duration) VALUES (? , ?)");

			ps.setTimestamp(1, timestamp);
			ps.setTime(2, durationOfSim);
			
			ps.executeUpdate();

		}
		
		con.close();
		
	}
	
	public void addTaxi(int SimID , int taxiNum , int fareNum, int travelTime , int distance) throws SQLException{
		con = ds.getConnection();
		//converting seconds into time format
		String sqlTime = (travelTime/3600) + ":" + ((travelTime/60)%60) + ":" + (travelTime%60);
		Time taxiTime = java.sql.Time.valueOf(sqlTime);
		
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			"INSERT INTO taxiStats (SimID , TaxiNum , Fares, TimeMoving, "
			+ "Distance ) "
			   + "VALUES (?,?,?,?,?)");

			ps.setInt(1, SimID);
			ps.setInt(2, taxiNum);
			ps.setInt(3, fareNum);
			ps.setTime(4, taxiTime);
			ps.setInt(5, distance);
			
			ps.executeUpdate();

		}
		
		con.close();
	}
	
	public void addFare(int fareNum, int SimID, int taxiNum ,int timeWaited,
						int fareDuration ) throws SQLException{
		con = ds.getConnection();
		//converting seconds into time format
		String sqlTime = (timeWaited/3600) + ":" + ((timeWaited/60)%60) + ":" + (timeWaited%60);
		Time waitTime = java.sql.Time.valueOf(sqlTime);
		
		sqlTime = (fareDuration/3600) + ":" + ((fareDuration/60)%60) + ":" + (fareDuration%60);
		Time fareTime = java.sql.Time.valueOf(sqlTime);
		
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			"INSERT INTO fareStats (SimID , TaxiNum , FareNum, TimeWaited"
			   + ", TimeTaken) "
			   + "VALUES (?,?,?,?,?)");

			ps.setInt(1, SimID);
			ps.setInt(2, taxiNum);
			ps.setInt(3, fareNum);
			ps.setTime(4, waitTime);
			ps.setTime(5, fareTime);
			
			ps.executeUpdate();

		}
		
		con.close();
	}
	
	public void addRequest(String insertionString) throws SQLException{
		con = ds.getConnection();
		String[] pieces = insertionString.split("--");
		System.out.println(insertionString);
		
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			"INSERT INTO requests (SimID , TimeOfRequest , Origin, "
			+ "Destination, Passengers, Method, Sharing, Traffic)"
			+ "VALUES (?,?,?,?,?,?,?,?)");
			
			ps.setInt(1, Integer.parseInt(pieces[0]));
			ps.setInt(2, Integer.parseInt(pieces[1]));
			ps.setString(3, pieces[2]);
			ps.setString(4, pieces[3]);
			ps.setInt(5, Integer.parseInt(pieces[4]));
			ps.setInt(6, Integer.parseInt(pieces[5]));
			ps.setByte(7, Byte.parseByte(pieces[6]));
			ps.setString(8, pieces[7]);
			
			System.out.println(insertionString);
			
			
			ps.executeUpdate();

		}
		
		con.close();
	}
	
	
	public int getNewSimID() throws SQLException{
		con = ds.getConnection();
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "SELECT MAX(SimID) FROM Simulators");
			
			int id = -1;
			ResultSet result =  ps.executeQuery();
			while(result.next())
				id = result.getInt(1);
			con.close();
			return id;


		}
		else
			return -1;	
		
	}
	
	public String getFareDetails(String query, int SimID) throws SQLException{ 
		con = ds.getConnection();
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "SELECT " + query + " FROM FareStats WHERE SimID = ?");
			
			ps.setInt(1, SimID);
			
			Time id = null;
			ResultSet result =  ps.executeQuery();
			while(result.next())
				id = result.getTime(1);
			con.close();
			return id.toString();


		}
		else
			return null;
	}
	
	public String getTaxiTimeDetails(String query, int SimID) throws SQLException{ 
		con = ds.getConnection();
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "SELECT " + query + " FROM TaxiStats WHERE SimID = ?");
			
			ps.setInt(1, SimID);
			
			Time id = null;
			ResultSet result =  ps.executeQuery();
			while(result.next())
				id = result.getTime(1);
			con.close();
			return id.toString();


		}
		else
			return null;
	}
	
	public String getTaxiFareDetails(String query, int SimID) throws SQLException{ 
		con = ds.getConnection();
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "SELECT " + query + " FROM TaxiStats WHERE SimID = ?");
			
			ps.setInt(1, SimID);
			
			String id = null;
			ResultSet result =  ps.executeQuery();
			while(result.next())
				id = "" + result.getInt(1);
			con.close();
			return id;


		}
		else
			return null;
	}

	public ResultSet getRequests(int simID) throws SQLException {
		
		if(simID == 0)
			simID = getNewSimID();
		
		con = ds.getConnection();
		ResultSet result = null;
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "SELECT * FROM Requests WHERE SimID = ? ORDER BY TimeOfRequest");
			
			ps.setInt(1, simID);
			
			result =  ps.executeQuery();
	}
		System.out.println(simID + "= SimID Size = ");
		System.out.print(result.getFetchSize());
		return result;
	}
	
	public String getSimTime(int simID) throws SQLException{
		con = ds.getConnection();
		if(con != null){
			PreparedStatement ps
			= con.prepareStatement(
			   "SELECT Duration FROM Simulators WHERE SimID = ?");
			
			ps.setInt(1, simID);
			
			Time id = null;
			ResultSet result =  ps.executeQuery();
			while(result.next())
				id = result.getTime(1);
			con.close();
			return id.toString();



		}
		else
			return null;
	}

}
