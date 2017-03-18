package testPart3;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.joda.time.DateTime;

import com.google.maps.model.Duration;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class demoBean  implements Serializable {	
	
	private static final long serialVersionUID = 1L;
	
	private String pickupPoint , dropoffPoint , share, 
	passNum , taxis , startTaxi, closestStatus 
	, polyline , method , percentage , startTime ,
	time , traffic;
	private TaxiRank taxiList;
	private TaxiStats taxiStatistics;
	private ArrayList<String> startList = new ArrayList<String>();
	private ArrayList<String> requestDetails;
	private int totalTime;
	private boolean redo = false;
	private ResultSet requests;
	private LocalTime timeOfSim;
	
	private DatabaseManager db;
	

	public String getPercentage() {
		return percentage;
	}



	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	

	public String getTraffic() {
		return traffic;
	}



	public void setTraffic(String traffic) {
		this.traffic = traffic;
	}



	public String getStartTime() {
		
		return startTime;
	}


	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public String getTime() {
		time = timeOfSim.toString();
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}

	


	public String getMethod() {
		return method;
	}



	public void setMethod(String method) {
		this.method = method;
	}



	public String getTaxis() {
		return taxis;
	}



	public String getPolyline() {
		return polyline;
	}



	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}



	public String getClosestStatus() {
		return closestStatus;
	}



	public void setClosestStatus(String closestStatus) {
		this.closestStatus = closestStatus;
	}



	public void setTaxis(String taxis) {
		this.taxis = taxis;
	}



	public String getPickupPoint() {
		return pickupPoint;
	}



	public void setPickupPoint(String pickupPoint) {
		this.pickupPoint = pickupPoint;
	}



	public String getDropoffPoint() {
		return dropoffPoint;
	}



	public void setDropoffPoint(String dropoffPoint) {
		this.dropoffPoint = dropoffPoint;
	}



	public String getShare() {
		return share;
	}



	public void setShare(String share) {
		this.share = share;
	}



	public String getStartTaxi() {
		return startTaxi;
	}



	public void setStartTaxi(String startTaxi) {
		this.startTaxi = startTaxi;
	}



	public String getPassNum() {
		return passNum;
	}



	public void setPassNum(String passNum) {
		this.passNum = passNum;
	}

	
	public void addStartLocation(){
		startList.add(startTaxi);
	}
	
	public List<Taxi> getTaxiList(){
		return taxiList.getTaxis();
	}

	public String Initiate() throws Exception{
		//initialise all variables
		totalTime = 0;
		db = new DatabaseManager();
		requestDetails = new ArrayList<String>();
		
		timeOfSim = LocalTime.parse(startTime);
		
		//sets up the taxis
		taxiList = new TaxiRank(Integer.parseInt(taxis) , startList , percentage);
		closestStatus = "";
		// setting up taxis that already have fares
		String origins = "Raheen, Limerick, Ireland" ;
		String destinations =  "Castletroy, Limerick, Ireland" ;
		String traffic = "BEST_GUESS";
		Fare f1 = new Fare(false, origins, destinations, 2 ,traffic , timeOfSim);
		Taxi t1 = taxiList.getTaxi(0);
		t1.addFare(f1);
		origins =  "Monaleen, Limerick, Ireland" ;
		destinations =  "Parteen, Limerick, Ireland" ;
		Fare f2 = new Fare(false, origins, destinations, 3 , traffic , timeOfSim);
		Taxi t2 = taxiList.getTaxi(1);
		t2.addFare(f2);
		System.out.println(t1.toString());
		System.out.println(t2.toString());
		
		polyline = t1.getPolyLine();

		//returns name of page to move to
		if(redo)
			return simAll();
		else
			return "testResult";
	}

	public void Demo() throws Exception{
		
			
			String origins, destinations , trafficModel;
			boolean sharing = Boolean.parseBoolean(share);
			int passengerNum = Integer.parseInt(passNum);
			// Allowing new fare to be created
			origins =  pickupPoint ;
			destinations = dropoffPoint ;
			trafficModel = traffic; 
			Fare f3 = new Fare(sharing, origins, destinations,
								passengerNum , trafficModel , timeOfSim);
	
			String insertionString = totalTime+"--"+origins+"--"+destinations+
					"--"+passengerNum;
			
			
			if(Boolean.parseBoolean(method)){
				quickestPickup(f3);
				insertionString += "--0";
			}
			else{
				totalTime(f3);
				insertionString += "--1";
			}
			//converts boolean to int
			int val = sharing? 1 : 0;
			insertionString += "--" + val;
			System.out.print(traffic);
			insertionString += "--"+trafficModel;
			requestDetails.add(insertionString);
				
		
	}
	
	public String simAll() throws Exception{
		while(requests.next()){
			//goes through all requests for sim wantd to be replicated
			//and requests them at the same times with these new conditions
			Update(requests.getInt(3) - totalTime);
			share = "" + requests.getByte(8);
			passNum = "" + requests.getInt(6);
			pickupPoint = requests.getString(4);
			dropoffPoint = requests.getString(5);
			method = "" + requests.getInt(7);
			traffic = requests.getString(9);
			Demo();
			
		}
		return Finish();
	}
	
	public void quickestPickup(Fare f3) throws Exception{
		Taxi t;
		// find closest taxi to the person
		Duration shortest = null, compare;
		//System.out.println("Amount of taxis = " +  taxiList.getTaxiAmount());
		int index = 0;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			System.out.print(i);
			t = taxiList.getTaxi(i);
			if (t.available(f3)) {
				compare = t.evaluateFare(f3);
				if (shortest == null) {
					shortest = compare;
					index = i;
				} else if (compare.inSeconds < shortest.inSeconds) {
					shortest = compare;
					index = i;
				}
			}
		}
		if (!(shortest == null)) {
			t = taxiList.getTaxi(index);
			t.addFare(f3);
			closestStatus = "The closest taxi is " + t.toString() + 
							 ". Time to pickup: " + shortest;
		} else
			closestStatus = "taxi is here";
	}
	
	
	
	public void totalTime(Fare f3) throws Exception{
		//find fastest overall route for this fare
		Taxi t;
		Long shortest = null , compare;
		int index = 0;
		//shortest = -1;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			System.out.print(i);
			t = taxiList.getTaxi(i);
			if(t.available(f3)){
				compare = t.evaluateTotalLength(f3);
				if(shortest == null){
						shortest = compare;
						index = i;
				}
				else if (compare < shortest){
					shortest = compare;
					index = i;
				}
			}
		}
		if (!(shortest == null)) {
			t = taxiList.getTaxi(index);
			t.addFare(f3);
			closestStatus = "The closest taxi is " + t.toString() + 
							 ". Time for overall fare: " + shortest;
		} else
			closestStatus = "taxi is here";
		
		
	}
	


	public void Update(int secondsMoved) throws Exception {
		// TODO Auto-generated method stub
		System.out.print("Time = " + getTime());
		timeOfSim = timeOfSim.plusSeconds(secondsMoved);
		totalTime += secondsMoved;
		Taxi t;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			t = taxiList.getTaxi(i);
			if (!(t.isEmpty())){
				t.update(secondsMoved);
			}
		}
	}
	
	public String Finish() throws Exception{
		Taxi t;
		int longestTimeLeft = 0;
		int timeLeft;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			t = taxiList.getTaxi(i);
			timeLeft = t.finishUp();
			if(timeLeft > longestTimeLeft)
				longestTimeLeft = timeLeft;
			}
		//if finished early, this tells you how long the simulation went on for
		totalTime += longestTimeLeft;
		
		taxiStatistics = new TaxiStats(taxiList , totalTime , requestDetails);
		int SimID = taxiStatistics.getSimID();
		
		return "results.xhtml?simID" + SimID;
	}
	
	
	public String Redo(int simID) throws SQLException{
		db = new DatabaseManager();
		requests = db.getRequests(simID);
		redo = true;
		return "setupSim";
	}
	
}

