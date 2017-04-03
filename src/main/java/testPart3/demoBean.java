package testPart3;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.joda.time.DateTime;

import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

@ManagedBean
@SessionScoped
public class demoBean  implements Serializable {	
	
	private static final long serialVersionUID = 1L;
	
	
	
	private String pickupPoint , dropoffPoint , share, 
	passNum , taxis , startTaxi, closestStatus 
	, polyline , method , percentage , startTime , dayOfWeek ,
	time , traffic , multiple , multipleTime , randomPass , errorMessage , backToHub;
	private TaxiRank taxiList;
	private TaxiStats taxiStatistics;
	private ArrayList<LatLng> startList = new ArrayList<LatLng>();
	private ArrayList<String> requestDetails;
	private ArrayList<Multiples> multipleList;
	private GeoApiContext context = new GeoApiContext();
	private int totalTime;
	private double benchmark;
	private boolean redo = false , moveToHub;
	private ResultSet requests;
	private DateTime timeOfSim;
	
	private DatabaseManager db;
	

	
	
	public String getBackToHub() {
		return backToHub;
	}



	public void setBackToHub(String backToHub) {
		this.backToHub = backToHub;
	}



	public String getPercentage() {
		return percentage;
	}



	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	

	public String getMultiple() {
		return multiple;
	}



	public void setMultiple(String multiple) {
		this.multiple = multiple;
	}



	public String getMultipleTime() {
		return multipleTime;
	}



	public void setMultipleTime(String multipleTime) {
		this.multipleTime = multipleTime;
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


	public String getDayOfWeek() {
		return dayOfWeek;
	}



	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}



	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	
	
	public String getTime() {
		time = timeOfSim.toLocalTime().toString();
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

	
	
	public String getErrorMessage() {
		if(errorMessage == null)
			errorMessage = "";
		return errorMessage;
	}



	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	

	public String getRandomPass() {
		return randomPass;
	}



	public void setRandomPass(String randomPass) {
		this.randomPass = randomPass;
	}



	public void addStartLocation(){
		context.setApiKey("AIzaSyCpto6czmXSCmH6FzaiHsX1OmuTi96ZRLE"); 
		GeocodingResult[] results;
		try {
			results = com.google.maps.GeocodingApi.geocode(context, startTaxi).await();
			LatLng latLngLocation = results[0].geometry.location;
			startList.add(latLngLocation);
			errorMessage = "";
		} catch (Exception e) {
			
			errorMessage = "Not a valid Location";
		}
		
		
	}
	
	public List<Taxi> getTaxiList(){
		return taxiList.getTaxis();
	}

	public String Initiate() throws Exception{
		//initialise all variables
		if(startList.size() == 0){
			errorMessage = "Please add at least one location";
			return "setupSim";
		}
		errorMessage = "";
		totalTime = 0;
		db = new DatabaseManager();
		requestDetails = new ArrayList<String>();
		multipleList = new ArrayList<Multiples>();
		
		moveToHub = Boolean.parseBoolean( backToHub);
		
		//sets time of sim to time specified in customization
		LocalTime timeOfDay = LocalTime.parse(startTime);
		int day = Integer.parseInt(dayOfWeek);
		LocalDate date = LocalDate.now();
		//cannot be the before current time
		date = date.plusDays(1);
		while(date.getDayOfWeek().getValue() != day){
			date = date.plusDays(1);
		}
		
		timeOfSim = new DateTime(date.getYear(),date.getMonthValue(),
										date.getDayOfMonth(),timeOfDay.getHour(),
										timeOfDay.getMinute(), timeOfDay.getSecond());
		
		//sets up the taxis
		taxiList = new TaxiRank(Integer.parseInt(taxis) , startList , percentage);
		closestStatus = "";
		// setting up taxis that already have fares
		/*
		String origins = "Raheen, Limerick, Ireland" ;
		String destinations =  "Castletroy, Limerick, Ireland" ;
		String traffic = "BEST_GUESS";
		Fare f1 = new Fare(true, origins, destinations, 2 ,traffic , timeOfSim);
		Taxi t1 = taxiList.getTaxi(0);
		t1.addFare(f1);
		origins =  "Monaleen, Limerick, Ireland" ;
		destinations =  "Parteen, Limerick, Ireland" ;
		Fare f2 = new Fare(true, origins, destinations, 3 , traffic , timeOfSim);
		Taxi t2 = taxiList.getTaxi(1);
		t2.addFare(f2);
		
		System.out.println(t1.toString());
		System.out.println(t2.toString());
		
		polyline = t1.getPolyLine();
		*/
		//returns name of page to move to
		if(redo)
			return simAll();
		else
			return "testResult";
	}

	public void Demo() throws Exception{
		
			errorMessage = "";
			String origins, destinations , trafficModel;
			boolean sharing = Boolean.parseBoolean(share);
			int passengerNum = Integer.parseInt(passNum);
			// Allowing new fare to be created
		if (validLocation(pickupPoint) && validLocation(dropoffPoint)) {
			origins = pickupPoint;
			destinations = dropoffPoint;
			trafficModel = traffic;
			Fare f3 = new Fare(sharing, origins, destinations, passengerNum, trafficModel, timeOfSim);

			String insertionString = totalTime + "--" + origins + "--" + destinations + "--" + passengerNum;

			if (Boolean.parseBoolean(method)) {
				quickestPickup(f3);
				insertionString += "--1";
			} else {
				totalTime(f3);
				insertionString += "--0";
			}
			// converts boolean to int
			int val = sharing ? 1 : 0;
			insertionString += "--" + val;
			System.out.print(traffic);
			insertionString += "--" + trafficModel;
			requestDetails.add(insertionString);
		}	
				
		
	}
	
	public String simAll() throws Exception{
		while(requests.next()){
			//goes through all requests for sim wanted to be replicated
			//and requests them at the same times with these new conditions
			Update(requests.getInt(3) - totalTime);
			share = "" + requests.getByte(8);
			if(requests.getByte(8) == 0)
				share = "false";
			else 
				share = "true";
			passNum = "" + requests.getInt(6);
			pickupPoint = requests.getString(4);
			dropoffPoint = requests.getString(5);
			if( requests.getInt(7) == 0)
				method = "false";
			else 
				method = "true";
			traffic = requests.getString(9);
			Demo();
			
		}
		return Finish();
	}
	
	public void quickestPickup(Fare f3) throws Exception{
		Taxi t;
		ArrayList<Taxi> checked = new ArrayList<Taxi>();
		// find closest taxi to the person
		Long shortest = null, compare;
		//System.out.println("Amount of taxis = " +  taxiList.getTaxiAmount());
		int index = 0;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			
			t = taxiList.getTaxi(i);
			if (t.isEmpty()) {
				System.out.print(i + "Check");
				int duplicate = (hasBeenChecked(t, checked));
				
				if (duplicate == -1) {
					checked.add(t);
					t.available(f3);
					if (shortest == null) {
						compare = t.evaluateFare(f3);
						shortest = compare;
						index = i;
						benchmark = t.getTaxiDistance(f3.getPickupLatLng());
					} else if (isConsiderable(t, f3, benchmark)) {
						System.out.println("\n" + t.getNumber() + " was considered");
						compare = t.evaluateFare(f3);
						if (compare < shortest) {
							shortest = compare;
							index = i;
						}
					}
				}
			}
		}
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			
			t = taxiList.getTaxi(i);
			if ((!t.isEmpty())) {
				System.out.print(i + "Check");
				t.available(f3);
				int duplicate = (hasBeenChecked(t, checked));
				if (duplicate == -1) {
					checked.add(t);

					if (shortest == null) {
						compare = t.evaluateFare(f3);
						shortest = compare;
						index = i;
						benchmark = t.getTaxiDistance(f3.getPickupLatLng());
					} 
					else if (isConsiderable(t, f3, benchmark)) {
						System.out.println("\n" + t.getNumber() + " was considered");
						compare = t.evaluateFare(f3);
						if (compare < shortest) {
							shortest = compare;
							index = i;
						}
					}
				}
			}

		}
		if (!(shortest == null)) {
			t = taxiList.getTaxi(index);
			t.addFare(f3);
			closestStatus = "The closest taxi is " + t.toString();
		}
	}
	
	
	
	public void totalTime(Fare f3) throws Exception{
		//find fastest overall route for this fare
		Taxi t;
		ArrayList<Taxi> checked = new ArrayList<Taxi>();
		int availableTaxis = 0;
		Long shortest = null , compare;
		int index = 0;
		//shortest = -1;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			
			t = taxiList.getTaxi(i);
			if (t.isEmpty()) {
				System.out.print(i + "Check");
				int duplicate = (hasBeenChecked(t, checked));
				t.available(f3);
				if (duplicate == -1) {
					checked.add(t);

					if (shortest == null) {
						compare =  t.evaluateTotalLength(f3);
						shortest = compare;
						index = i;
						benchmark = t.getTaxiDistance(f3.getPickupLatLng());
					} else if (isConsiderable(t, f3, benchmark)) {
						System.out.println("\n" + t.getNumber() + " was considered");
						compare =  t.evaluateTotalLength(f3);
						if (compare < shortest) {
							shortest = compare;
							index = i;
						}
					}
				}
			}
		}
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			
			t = taxiList.getTaxi(i);
			if ((!t.isEmpty())) {
				System.out.print(i + "Check");
				t.available(f3);
				int duplicate = (hasBeenChecked(t, checked));
				if (duplicate == -1) {
					checked.add(t);

					if (shortest == null) {
						compare =  t.evaluateTotalLength(f3);
						shortest = compare;
						index = i;
						benchmark = t.getTaxiDistance(f3.getPickupLatLng());
					} 
					else if (isConsiderable(t, f3, benchmark)) {
						System.out.println("\n" + t.getNumber() + " was considered");
						compare = t.evaluateTotalLength(f3);
						if (compare < shortest) {
							shortest = compare;
							index = i;
						}
					}
				}
			}

		}
		if (!(shortest == null)) {
			t = taxiList.getTaxi(index);
			t.addFare(f3);
			closestStatus = "The closest taxi is " + t.toString();
		}
		else if(availableTaxis == 0)
			closestStatus = "No taxis currently available, please try again later";
		else
			closestStatus = "taxi is here";
		
		
	}
	
	public int hasBeenChecked(Taxi t , ArrayList<Taxi> checked){
		for(int i = 0;i < checked.size();i++){
			if(checked.get(i).getLocation().equals(t.getLocation()))
				if(checked.get(i).getNumberOfFares() == 0 
				&& t.getNumberOfFares() == 0)
					return i;
		}
		return -1;
	}


	public void Update(int secondsMoved) throws Exception {
		// TODO Auto-generated method stub
		System.out.print("Time = " + getTime());
		timeOfSim = timeOfSim.plusSeconds(secondsMoved);
		totalTime += secondsMoved;
		Taxi t;
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			t = taxiList.getTaxi(i);
			if(t.getIsActive() && t.isEmpty() && moveToHub && (!t.getTravelBack())){
				double min = t.getTaxiDistance(startList.get(0));
				int index = 0;
				for(int j = 1; j < startList.size();j++){
					if(t.getTaxiDistance(startList.get(j)) < min){
						min = t.getTaxiDistance(startList.get(j));
						index = j;
					}
				}
				t.moveToHub(startList.get(index).toString(), timeOfSim);
			}
			if (t.getIsActive()){
				t.update(secondsMoved);
			}
		}
		checkMultiples(secondsMoved);
	}
	
	public void checkMultiples(int secondsMoved) throws Exception{
		int i =0;
		while(i < multipleList.size()){
			Multiples m = multipleList.get(i);
			//gets the amount of requests to request during this update;
			int portion = m.getPortion(secondsMoved);
			for (int j = 0; j < portion; j++){
				share = m.getWillingToShare();
				if(Boolean.parseBoolean(m.getRandomPass()))
					passNum = "" +((int) (Math.random()*4) + 1);
				else
					passNum = m.getNumberOfPassengers();
				pickupPoint = m.getOrigin();
				dropoffPoint = m.getDestination();
				method = m.getMethod();
				traffic = m.getTrafficModel();
				Demo();
			}
			if(m.getAmount() == 0)
				multipleList.remove(m);
			else
				i++;
		}
	}
	
	//checks if this taxi should be considered
	public boolean isConsiderable(Taxi taxi, Fare fare , double benchmark){
		Double distance;
		if(taxi.isEmpty()){
			distance = taxi.getTaxiDistance(fare.getPickupLatLng());
			if(distance < (benchmark * 2) || distance < 2)
				return true;
			else
				return false;
		}
		else{
			distance = taxi.getTaxiDistance(fare.getPickupLatLng());
			if(distance < (benchmark * 2) || distance < 2)
				return true;
			
			/*distance = taxi.haversine(fare.getPickupLatLng().toString() , taxi.getDestLatLng());
			distance += taxi.getTaxiDistance(fare.getPickupLatLng());
			if(distance < (benchmark * 1.5))
				return true;
			*/
			return false;
			//return taxi.between(fare.getPickupLatLng());
			
		}
	}
	
	public void Multiply(){
		//simulate multiple routes at once 
		int numberOfRequests = Integer.parseInt(multiple);
		int amountOfTime = Integer.parseInt(multipleTime);
		
		Multiples mult = new Multiples(numberOfRequests , amountOfTime,
										share , pickupPoint , dropoffPoint,
										passNum , method , randomPass, traffic);
		
		multipleList.add(mult);
		
		closestStatus = "";
		
		
	}
	
	public String Finish() throws Exception{
		Taxi t;
		int longestTimeLeft = 0;
		int timeLeft;
		//make sure all promised requests are made
		ArrayList<Integer> ratesOfrequest = new ArrayList<Integer>();
		while(multipleList.size() > 0){
			for(int i = 0;i < multipleList.size();i++){
				ratesOfrequest.add(multipleList.get(i).getRate());
			}
			//sorted list of rates
			Collections.sort(ratesOfrequest);
			//updates for each rate so all are called once
			Update(ratesOfrequest.get(0));
			for(int j = 1; j < ratesOfrequest.size();j++){
				Update(ratesOfrequest.get(j) - ratesOfrequest.get(j-1));
			}
		}
		for (int i = 0; i < taxiList.getTaxiAmount(); i++) {
			t = taxiList.getTaxi(i);
			timeLeft = t.finishUp();
			if(timeLeft > longestTimeLeft)
				longestTimeLeft = timeLeft;
			}
		System.out.println("tOTAL tIME WAS :" + totalTime);
		//if finished early, this tells you how long the simulation went on for
		totalTime += longestTimeLeft;
		System.out.println("tOTAL tIME IS :" + totalTime);
		
		taxiStatistics = new TaxiStats(taxiList , totalTime , requestDetails , startTime , dayOfWeek);
		int SimID = taxiStatistics.getSimID();
		
		return "results.xhtml?simID" + SimID;
	}
	
	
	public String Redo(int simID) throws SQLException{
		db = new DatabaseManager();
		requests = db.getRequests(simID);
		startList = new ArrayList<LatLng>();
		redo = true;
		return "setupSim";
	}
	
	public String Redo() throws SQLException{
		redo = false;
		startList = new ArrayList<LatLng>();
		return "setupSim";
	}
	
	public boolean validLocation(String input){
		context.setApiKey("AIzaSyCpto6czmXSCmH6FzaiHsX1OmuTi96ZRLE"); 
		GeocodingResult[] results;
		try {
			results = com.google.maps.GeocodingApi.geocode(context, input).await();
			if(results[0].geometry.location != null)
				return true;
			else{
				errorMessage += "'" + input + "' is not a valid Location. ";
				return false;		
			}
		} catch (Exception e) {
			
			errorMessage += "'" +input + "' is not a valid Location. ";
			return false;
		}
	}
	
}

