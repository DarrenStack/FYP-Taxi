package testPart3;

import java.time.LocalTime;

import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class Fare {

	private boolean willingToShare , passedOrigin;
	private String origin;
	private String destination;
	private int numberOfPassengers;
	private String trafficModel;
	private LocalTime time;
	private FareStats fareStat;
	
	public Fare(boolean willingToShare , String origin, String destination, 
					int num , String trafficModel , LocalTime time){
		this.willingToShare = willingToShare;
		this.origin = origin;
		this.destination = destination;
		this.numberOfPassengers = num;
		passedOrigin = false;
		this.trafficModel = trafficModel;
		this.time = time;
	}

	public String getTrafficModel() {
		return trafficModel;
	}

	public LocalTime getTime() {
		return time;
	}

	public boolean isWillingToShare() {
		return willingToShare;
	}

	public void setWillingToShare(boolean willingToShare) {
		this.willingToShare = willingToShare;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getNumberOfPassengers() {
		return numberOfPassengers;
	}

	public void setNumberOfPassengers(int numberOfPassengers) {
		this.numberOfPassengers = numberOfPassengers;
	}
	
	public boolean isPassedOrigin() {
		return passedOrigin;
	}
	
	public boolean checkForPickup(String point , GeoApiContext context) throws Exception{
		LatLng latLngLocation;
		GeocodingResult[] results = com.google.maps.GeocodingApi.geocode(context, origin).await();
		latLngLocation = results[0].geometry.location;
		System.out.println("Comparing " + point + " to " + results[0].formattedAddress);
		if(results[0].formattedAddress.equals(point)){
			passedOrigin = true;
			System.out.println("worked");
			return true;
		}
		else
			return false;
	}
	
	public boolean checkForDestination(String point , GeoApiContext context) throws Exception{
		LatLng latLngLocation;
		GeocodingResult[] results = com.google.maps.GeocodingApi.geocode(context, destination).await();
		latLngLocation = results[0].geometry.location;
		System.out.println("Comparing " + point + " to " + results[0].formattedAddress);
		if(results[0].formattedAddress.equals(point)){
			passedOrigin = true;
			System.out.println("worked");
			return true;
		}
		else
			return false;
	}
	
	public void makeStats(int time , int passengers){
		fareStat = new FareStats(time, passengers);
	}
	
	public FareStats getFareStats(){
		return fareStat;
	}
	
	public void setPickup(int time ){
		fareStat.setPickupTime(time);
	}
	
	public void setDropoff(int time ){
		fareStat.setDropoffTime(time);
	}
	
	

}
