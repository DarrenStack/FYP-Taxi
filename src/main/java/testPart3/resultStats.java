package testPart3;

import java.sql.SQLException;

public class resultStats {
	
	private int sim;
	private DatabaseManager db;
	
	private String maxTaxiTime, minTaxiTime, maxFares, minFares,
					avgTaxiTime, avgFares, maxFareTime , minFareTime,
					minWaitTime, maxWaitTime, avgTripTime , avgWaitTime,
					simTimeOfDay , simDayOfWeek , numberOfFares , numberOfTaxis;
					
					
	
	
	public resultStats(int simID, DatabaseManager db) throws SQLException{
		sim = simID;
		this.db = db;
		maxTaxiTime = db.getTaxiTimeDetails("MAX(TimeMoving)" , sim);
		minTaxiTime = db.getTaxiTimeDetails("MIN(TimeMoving)" , sim);
		maxFares = db.getTaxiFareDetails("MAX(Fares)" , sim);
		minFares = db.getTaxiFareDetails("MIN(Fares)" , sim); 
		avgTaxiTime =  db.getTaxiTimeDetails("SEC_TO_TIME(AVG(TIME_TO_SEC(TimeMoving)))" , sim);
		avgFares = db.getTaxiFareDetails("AVG(Fares)" , sim);
		numberOfTaxis = db.getTaxiFareDetails("COUNT(TaxiNum)" , sim);
		numberOfFares = db.getFareIntDetails("COUNT(FareNum)", sim);
		maxFareTime = db.getFareDetails("MAX(TimeTaken)" , sim);
	    minFareTime = db.getFareDetails("MIN(TimeTaken)" , sim);
	    minWaitTime = db.getFareDetails("MIN(TimeWaited)" , sim);
	    maxWaitTime =  db.getFareDetails("MAX(TimeWaited)" , sim);
	    avgTripTime = db.getFareDetails("SEC_TO_TIME(AVG(TIME_TO_SEC(TimeTaken)))" , sim);
	    avgWaitTime = db.getFareDetails("SEC_TO_TIME(AVG(TIME_TO_SEC(TimeWaited)))" , sim);
	    simTimeOfDay = db.getSimTime("TimeOfDay" , simID);
	    simDayOfWeek = db.getSimDay(simID);
	    
	}




	public String getMaxTaxiTime() {
		return maxTaxiTime;
	}




	public String getMinTaxiTime() {
		return minTaxiTime;
	}




	public String getMaxFares() {
		return maxFares;
	}




	public String getMinFares() {
		return minFares;
	}




	public String getAvgTaxiTime() {
		return avgTaxiTime;
	}




	public String getAvgFares() {
		return avgFares;
	}




	public String getMaxFareTime() {
		return maxFareTime;
	}




	public String getMinFareTime() {
		return minFareTime;
	}




	public String getMinWaitTime() {
		return minWaitTime;
	}




	public String getMaxWaitTime() {
		return maxWaitTime;
	}




	public String getAvgTripTime() {
		return avgTripTime;
	}




	public String getAvgWaitTime() {
		return avgWaitTime;
	}




	public String getSimTimeOfDay() {
		return simTimeOfDay;
	}




	public String getSimDayOfWeek() {
		return simDayOfWeek;
	}
	
	public String getNumberOfFares(){
		return numberOfFares;
	}

	public String getNumberOfTaxis() {
		return numberOfTaxis;
	}
	
	
	
	

}
