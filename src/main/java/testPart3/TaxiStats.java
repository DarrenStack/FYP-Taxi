package testPart3;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaxiStats {
	
	private TaxiRank taxiList;
	private int maxFareTime, minFareTime,
				maxWaitTime, minWaitTime,
				SimID;
	private double averageWaitTime , averageTripTime;
	private DatabaseManager db;
	private List<Taxi> allTaxis;
	private ArrayList<String> requests;
	
	public TaxiStats(TaxiRank taxiList, int time , ArrayList<String> requests) throws SQLException{
		this.taxiList = taxiList;
		this.requests = requests;
		maxFareTime = 0;
		minFareTime = -1;
		allTaxis = taxiList.getTaxis();
		ArrayList<FareStats> fares;
		double totalFareTimes = 0, count = 0, totalWaitTimes = 0;
		Taxi taxi;
		for(int i = 0;i < allTaxis.size();i++){
			taxi = allTaxis.get(i);
			fares = taxi.getFareStats();
			for(int j = 0;j < fares.size();j++){
				count++;
				totalFareTimes += fares.get(0).getTripTime();
				if (fares.get(0).getTripTime() > maxFareTime)
					maxFareTime = fares.get(0).getTripTime();
				if(fares.get(0).getTripTime() < minFareTime || minFareTime == -1)
					minFareTime =  fares.get(0).getTripTime();
				totalWaitTimes += fares.get(0).getPickupTime();
				if (fares.get(0).getPickupTime() > maxWaitTime)
					maxWaitTime = fares.get(0).getPickupTime();
				if(fares.get(0).getPickupTime() < minWaitTime || minWaitTime == -1)
					minWaitTime =  fares.get(0).getPickupTime();			
			}
		}
		averageWaitTime = totalWaitTimes / count;
		averageTripTime = totalFareTimes / count;
		
		db = new DatabaseManager();
		db.addSimulator(time);
		SimID = db.getNewSimID();
		uploadStats();
	}
	
	public void uploadStats() throws SQLException{
		ArrayList<FareStats> fares;
		Taxi taxi;
		FareStats fare;
		for(int i = 0;i < allTaxis.size();i++){
			taxi = allTaxis.get(i);
			fares = taxi.getFareStats();
			//add taxi stats for each taxi
			db.addTaxi(SimID, taxi.getNumber() , fares.size() , 
						taxi.getTravelTime(),0);
			for(int j = 0;j < fares.size();j++){
				//add fare stats for each fare
				fare = fares.get(j);
				db.addFare(j, SimID, taxi.getNumber(), fare.getPickupTime(),
							fare.getTripTime());
			}
		}
		
		
		String insertionString;
		for(int i = 0; i < requests.size();i++){
			insertionString = SimID + "--" + requests.get(i);
			db.addRequest(insertionString);
		}
	}
	
	public int getSimID(){
		return SimID;
	}

}
