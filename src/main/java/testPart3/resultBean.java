package testPart3;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class resultBean  implements Serializable{
	
	private static final long serialVersionUID = 1L;

	DatabaseManager db;

	private String simID , compareSim;
	private int sim , compareSimID;
	private resultStats initResult , compareResult;
	private List<String> simList;
	
	public void Initiate() throws Exception{
		//Initialize all variables
		db = new DatabaseManager();
		if(simID == null || compareSim == null){
			sim = db.getNewSimID();
			simID = sim + "";
		}
		else{
			sim = Integer.parseInt(simID);
			if(sim > db.getNewSimID()){
				compareSim = "No results available for Sim " + sim;
				sim = db.getNewSimID();
				simID = sim + "";
			}
		}
		
		System.out.println("Init" + compareSimID);
			
		if(compareSim == null){
			compareSimID = sim - 1;
			compareSim = "";
			
			simList = db.getSimList();
		}
		
		
		try{
			initResult = new resultStats(sim , db);
		}
		catch(Exception e){
			compareSim = "No results for simulator " + sim;
			sim = db.getNewSimID();
			simID = sim + "";
			initResult = new resultStats(sim , db);
		}
		GetCompareResult();
	}
	
	public resultStats getMainResult(){
		return initResult;
	}
	
	public String GetSimTimeOfDay() throws SQLException{
		return db.getSimTime("TimeOfDay" , sim);
	}
	
	public String GetSimDayOfWeek() throws SQLException{
		return db.getSimDay(sim);
	}
	
	public String getSimID() {
		return simID;
	}

	public void setSimID(String simID) {
		this.simID = simID;
	}
	
	public String GetSimTime() throws SQLException{
		return db.getSimTime("Duration" , sim);
	}

	public resultStats GetCompareResult() throws SQLException{
		System.out.println("Now here:"+compareSim);
		try{
		compareResult = new resultStats(compareSimID , db);
		return compareResult;
		}
		catch(NullPointerException e){
			compareSim = "No results for available for " + compareSimID;
			compareSimID = db.getNewSimID();
			compareResult = new resultStats(compareSimID , db);
			
			return compareResult;
		}
	}
	
	public List<String> GetListOfSimulators(){
		return simList;
	}

	public List<String> getSimList() {
		return simList;
	}

	public void setSimList(List<String> simList) {
		this.simList = simList;
	}
	
	public void ComparePrev(){
		int newNum;
		newNum = compareSimID;
		if(newNum - 1 <= 0){
			compareSim = "This is the oldest result, no further results";
		}
		else{
			compareSim = "";
			compareSimID--;
		}
		System.out.println("Here" + compareSimID);
	}
	
	public void CompareNext() throws SQLException{
		int newNum;
		newNum = compareSimID;
		if(newNum + 1 > db.getNewSimID()){
			compareSim = "This is the most recent result, no further results";
		}
		else{
			compareSim = "";
			compareSimID++;
		}
		
	}

	public String getCompareSim() {
		return compareSim;
	}

	public void setCompareSim(String compareSim) {
		this.compareSim = compareSim;
	}
	
	public void finished(){
		compareSim = null;
	}
	
	public String GetCompSimTimeOfDay(){
		return compareResult.getSimTimeOfDay();
	}
	
	public String GetCompSimDayOfWeek(){
		return compareResult.getSimDayOfWeek();
	}
	
	public String GetCompSimID(){
		return "" + compareSimID;
	}
	
	public String GetCompSimTime() throws SQLException{
		return db.getSimTime("Duration" , compareSimID);
	}
	
	public String GoTo() {
		simID = "" + compareSimID;
		return "results?faces-redirect=true&simID=" + compareSimID;
	}
}
