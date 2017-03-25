package testPart3;

public class Multiples {

	private int amount , amountLeft;
	private double secondsLeft;
	private double secondsAdded;
	private String origin;
	private String destination , willingToShare,  method, 
			trafficModel, numberOfPassengers;
	
	public Multiples(int numOfRequests , int minutes , String willingToShare , 
			String origin, String destination, String num , String trafficModel){
		secondsLeft = minutes * 60;
		amount = numOfRequests;
		this.willingToShare = willingToShare;
		this.origin = origin;
		this.destination = destination;
		this.numberOfPassengers = num;
		this.trafficModel = trafficModel;
		amountLeft = amount;
		secondsAdded = 0;
	}
	
	public int getPortion(int secondsMoved){
		int number = 0;
		secondsAdded += secondsMoved;
		double piece = amount * (secondsAdded / secondsLeft);
		System.out.println("piece = " + piece + " secondsLeft =" + 
							secondsLeft + " secondsAdded =" + secondsAdded);
		
		
		if(piece >= 1 && piece <= amountLeft){
			number = (int) piece;
			secondsAdded = 0;
			amountLeft -= number;
		}
		else if(piece > amountLeft){
			number = amountLeft;
			amountLeft -= number;
		}
		
		return number;
	}

	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}

	public String getWillingToShare() {
		return willingToShare;
	}

	public String getMethod() {
		return method;
	}

	public String getTrafficModel() {
		return trafficModel;
	}

	public String getNumberOfPassengers() {
		return numberOfPassengers;
	}
	
	public int getAmount(){
		return (int) amount;
	}
	
}
