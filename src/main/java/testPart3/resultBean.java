package testPart3;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class resultBean  implements Serializable{
	
	DatabaseManager db;

	private String simID;
	private int sim;
	
	public void Initiate() throws Exception{
		//Initialize all variables
		db = new DatabaseManager();
		if(simID == null){
			sim = db.getNewSimID();
			simID = sim + "";
		}
		else sim = Integer.parseInt(simID);
	}
	
	
	public String GetMaxFareTime() throws SQLException {
		System.out.println(simID);
		return db.getFareDetails("MAX(TimeTaken)" , sim);
	}



	public String GetMinFareTime() throws SQLException {
		return db.getFareDetails("MIN(TimeTaken)" , sim);
	}



	public String GetMaxWaitTime() throws SQLException {
		return db.getFareDetails("MAX(TimeWaited)" , sim);
	}



	public String GetMinWaitTime() throws SQLException {
		return db.getFareDetails("MIN(TimeWaited)" , sim);
	}



	public String GetAverageWaitTime() throws SQLException {
		return db.getFareDetails("SEC_TO_TIME(AVG(TIME_TO_SEC(TimeWaited)))" , sim);
	}



	public String GetAverageTripTime() throws SQLException {
		return db.getFareDetails("SEC_TO_TIME(AVG(TIME_TO_SEC(TimeTaken)))" , sim);
	}
	
	public String getSimID() {
		return simID;
	}

	public void setSimID(String simID) {
		this.simID = simID;
	}

	

}
