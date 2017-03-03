package testPart3;

public class FareStats {
	
	private int timestamp , pickupTime , dropoffTime , passengers;
	
	public FareStats(int timestamp , int passengers){
		this.timestamp = timestamp;
		this.passengers = passengers;
	}

	public int getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(int pickupTime) {
		this.pickupTime = pickupTime - timestamp;
	}

	public int getDropoffTime() {
		return dropoffTime;
	}

	public void setDropoffTime(int dropoffTime) {
		this.dropoffTime = dropoffTime - timestamp;
	}
	
	public int getTripTime(){
		return dropoffTime - pickupTime;
	}
	
	public int getTimestamp(){
		return timestamp;
	}
	
	
	
	

}
