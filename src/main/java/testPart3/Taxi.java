package testPart3;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.Duration;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;

public class Taxi {
	
	private ArrayList<Fare> Fares = new ArrayList<Fare>();
	private ArrayList<FareStats> FareAnalytics = new ArrayList<FareStats>();
	private ArrayList<Integer> legLengths;
	private ArrayList<Duration> tempLegs;
	private String location;
	private int secondsWithoutMovement;
	private GeoApiContext context;
	private DirectionsApiRequest route, bestRoute;
	private double percent;

	private DirectionsResult routeResult , tempResult;
	private boolean  empty , quickPick , isActive , validPickup;
	private int size , number , passengers , currentLeg, currentStep,
				  travelTime , totalTime , legTime;
	private String polyLine;

	public Taxi(int size , int number, String newLocation , double percent) throws Exception{
		context = new GeoApiContext();
		context.setApiKey("AIzaSyCpto6czmXSCmH6FzaiHsX1OmuTi96ZRLE");
		empty = true;
		this.size = size;
		this.number = number;
		polyLine = "";
		passengers = 0; 
		travelTime = 0; 
		legTime = 0;
		this.percent = percent;
		LatLng latLngLocation;
		
		GeocodingResult[] results = com.google.maps.GeocodingApi.geocode(context, newLocation).await();
		latLngLocation = results[0].geometry.location;
		location = latLngLocation.toString();
		System.out.print(location);
		secondsWithoutMovement = 0; currentStep = 0; currentLeg = 0;
	}
	
	public DirectionsApiRequest getBestRoute(Fare fare) throws Exception{
		//finds best possible route for all stops in taxi
		System.out.print("a");
		bestRoute = null;
		tempLegs = new ArrayList<Duration>();
		//if no current fare
		if(empty){
			
			String[] origins = new String[]{location , fare.getOrigin()};
			String[] destinations = new String[]{fare.getOrigin() , fare.getDestination() };
			
			DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations);
			
			//sets time of day to be sim time of tommorow since request needs
			//to be in the future to allow use of traffic
			LocalDate date = LocalDate.now();
			LocalTime timeOfSim = fare.getTime();
			date = date.plusDays(1);
			DateTime dateTime = new DateTime(date.getYear(),date.getMonthValue(),
											date.getDayOfMonth(),timeOfSim.getHour(),
											timeOfSim.getMinute(), timeOfSim.getSecond());
			request.departureTime(dateTime);
			if(fare.getTrafficModel().equals("PESSIMISTIC"))
				request.trafficModel(TrafficModel.PESSIMISTIC);
			else if(fare.getTrafficModel().equals("OPTIMISTIC"))
				request.trafficModel(TrafficModel.OPTIMISTIC);
			else
				request.trafficModel(TrafficModel.BEST_GUESS);
			
			request.mode(TravelMode.DRIVING);
			
			DistanceMatrix matrix= request.await();
			
			tempLegs.add(matrix.rows[0].elements[0].durationInTraffic);
			tempLegs.add( matrix.rows[1].elements[1].durationInTraffic);
			System.out.print(tempLegs.get(0));
			bestRoute =	DirectionsApi.getDirections(context, location, fare.getDestination());
			bestRoute = bestRoute.waypoints(fare.getOrigin());
			return bestRoute;
		}
		else {
			//find best possible shared route based on time
			String[] routeOrigins , routeDest;
			
			int timeToFinishFirstFare = 0;
			
			ArrayList<Integer> originPoints = new ArrayList<Integer>();
			ArrayList<Integer> destPoints = new ArrayList<Integer>();
			ArrayList<Integer> positionTracker = new ArrayList<Integer>();
			
			//get all origins and destinations for current fares and new fare
			routeOrigins = new String[(Fares.size() * 2) + 3];
			routeDest = new String[(Fares.size() * 2) + 2];
			positionTracker.add(0, 0);positionTracker.add(1, 1);
			routeOrigins[0]=fare.getOrigin(); routeDest[0]=fare.getOrigin();
			routeOrigins[1]=fare.getDestination(); routeDest[1]=fare.getDestination();
			
			originPoints.add(0);
			destPoints.add(1);
			
			int count = 2;
			
			// populate current waypoints
			for (int k = 0; k < Fares.size(); k++) {
				routeOrigins[2*(k+1)] = Fares.get(k).getOrigin();
				routeOrigins[(2*(k+1)) + 1] = Fares.get(k).getDestination();
				routeDest[(2*(k+1))] = Fares.get(k).getOrigin();
				routeDest[(2*(k+1)) + 1] = Fares.get(k).getDestination();
				
				if(!(Fares.get(k).isPassedOrigin())){
					positionTracker.add(count, (2*(k+1)));
					originPoints.add(count);
					count++;
				}
				
				positionTracker.add(count, (2*(k+1)) + 1);
				destPoints.add(count);
				count++;
			}
			routeOrigins[(Fares.size() * 2) + 2] = location;
			
			//sets up matrix with origins + destinations + traffic
			
			DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, routeOrigins, routeDest);
			
			//sets time of day to be sim time of tommorow since request needs
			//to be in the future to allow use of traffic
			LocalDate date = LocalDate.now();
			LocalTime timeOfSim = fare.getTime();
			date = date.plusDays(1);
			DateTime dateTime = new DateTime(date.getYear(),date.getMonthValue(),
											date.getDayOfMonth(),timeOfSim.getHour(),
											timeOfSim.getMinute(), timeOfSim.getSecond());
			request.departureTime(dateTime);
			if(fare.getTrafficModel().equals("PESSIMISTIC"))
				request.trafficModel(TrafficModel.PESSIMISTIC);
			else if(fare.getTrafficModel().equals("OPTIMISTIC"))
				request.trafficModel(TrafficModel.OPTIMISTIC);
			else
				request.trafficModel(TrafficModel.BEST_GUESS);
			
			request.mode(TravelMode.DRIVING);
			
			DistanceMatrix matrix= request.await();
			Long shortestTime , time;
			shortestTime = null;
			int temp;
			boolean isOrigin;
			String[] updatedWay = new String[0];
			
			int limit = destPoints.size();
			boolean found;
			System.out.println(limit + "\n");
			//Go through all different combinations of waypoints to find
			//fastest
			for (int i = 0; i < limit;i++) {
				System.out.print("b");
				//set up order with different destination at end
				temp = positionTracker.get(destPoints.get(i));
				positionTracker.remove(destPoints.get(i));
				positionTracker.add(temp);
				
				int movingNode = positionTracker.get(destPoints.get(i) - 1);
				isOrigin = false;
				if(originPoints.indexOf(movingNode) != -1)
					isOrigin = true;
				
				//if its an origin move it through points
				if (isOrigin) {
					
					
					for (int j = 0; j < positionTracker.size() - 1; j++) {
						System.out.print("c");
						positionTracker.remove(positionTracker.indexOf(movingNode));
						positionTracker.add(j , movingNode);
						
						timeToFinishFirstFare = (int) matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds;
						found = false;
						
						time = matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds;
						for (int l = 0; l < positionTracker.size() - 1; l++) {
							time += matrix.rows[positionTracker.get(l)].elements[positionTracker
									.get(l + 1)].durationInTraffic.inSeconds;
							System.out.print(time + "");
							if(!(Fares.get(0).checkForDestination(routeDest[positionTracker.get(l)] , context)) && !found){
								timeToFinishFirstFare += matrix.rows[positionTracker.get(l)].elements[positionTracker
								                        .get(l + 1)].durationInTraffic.inSeconds;
								}
						else
							found = true;
							}

						if (shortestTime == null) {
							if (withinBounds(timeToFinishFirstFare)) {
								shortestTime = time;
								//update length of legs of trip
								
								tempLegs.clear();
								
								tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic);
								
								bestRoute = DirectionsApi.getDirections(context, location,
										routeDest[positionTracker.get(positionTracker.size() - 1)]);
								updatedWay = new String[positionTracker.size() - 1];
								for (int n = 0; n < positionTracker.size() - 1; n++) {
									tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
									         									.get(n + 1)].durationInTraffic);
									updatedWay[n] = routeOrigins[positionTracker.get(n)];
								}

							}
						}
						else if( time < shortestTime && withinBounds(timeToFinishFirstFare)){
							shortestTime = time;
							//update length of legs of trip
							
							tempLegs.clear();
							
							tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic);
							
							bestRoute = DirectionsApi.getDirections(context, location,
									routeDest[positionTracker.get(positionTracker.size() - 1)]);
							updatedWay = new String[positionTracker.size() - 1];
							for (int n = 0; n < positionTracker.size() - 1; n++) {
								tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
								         									.get(n + 1)].durationInTraffic);
								updatedWay[n] = routeOrigins[positionTracker.get(n)];
							}
						}
						

					}
					//resets position tracker to original origin points
					positionTracker.remove(positionTracker.indexOf(movingNode));
					positionTracker.add(destPoints.get(i) - 1, movingNode);
				}
				else{
					//if already picked up passengers for fare, 
					//just look at time when destination is at end
					System.out.println("d");
					timeToFinishFirstFare = (int) matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds;
					found = false;
					time = matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds;
					for (int l = 0; l < positionTracker.size() - 1; l++) {
						time += matrix.rows[positionTracker.get(l)].elements[positionTracker
								.get(l + 1)].durationInTraffic.inSeconds;
						System.out.print(time + "");
						if(!(Fares.get(0).checkForDestination(routeDest[positionTracker.get(l)] , context)) && !found){
								timeToFinishFirstFare += matrix.rows[positionTracker.get(l)].elements[positionTracker
								                        .get(l + 1)].durationInTraffic.inSeconds;
								}
						else
							found = true;
						System.out.println(positionTracker.get(l));
					}
					

					if (shortestTime == null) {
						if (withinBounds(timeToFinishFirstFare)) {
							shortestTime = time;
							//update length of legs of trip
							
							tempLegs.clear();
							
							tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic);
							
							bestRoute = DirectionsApi.getDirections(context, location,
									routeDest[positionTracker.get(positionTracker.size() - 1)]);
							updatedWay = new String[positionTracker.size() - 1];
							for (int n = 0; n < positionTracker.size() - 1; n++) {
								tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
								         									.get(n + 1)].durationInTraffic);
								updatedWay[n] = routeOrigins[positionTracker.get(n)];
							}
							
							
						}
					}
					else if( time < shortestTime && withinBounds(timeToFinishFirstFare)){
						shortestTime = time; 
						//update length of legs of trip
						
						tempLegs.clear();
						
						tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic);
						
						bestRoute = DirectionsApi.getDirections(context, location,
								routeDest[positionTracker.get(positionTracker.size() - 1)]);
						updatedWay = new String[positionTracker.size() - 1];
						for (int n = 0; n < positionTracker.size() - 1; n++) {
							tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
							         									.get(n + 1)].durationInTraffic);
							updatedWay[n] = routeOrigins[positionTracker.get(n)];
						}
						
						
					}
				}
				//resets position tracker to original destination points
				positionTracker.remove( positionTracker.size() -1);
				positionTracker.add(destPoints.get(i), temp);
			}
			
			if(shortestTime == null){
				bestRoute = DirectionsApi.getDirections(context, location,
						fare.getDestination());
				tempLegs.clear();
				
				
				
				if(Fares.get(0).isPassedOrigin()){
					updatedWay = new String[]{Fares.get(0).getDestination() ,fare.getOrigin()};
					tempLegs.add(matrix.rows[routeOrigins.length-1].elements[3].durationInTraffic);
					tempLegs.add(matrix.rows[3].elements[0].durationInTraffic);
					tempLegs.add(matrix.rows[0].elements[1].durationInTraffic);
				}
				else{
					updatedWay = new String[]{Fares.get(0).getOrigin(),
							Fares.get(0).getDestination(),fare.getOrigin()};
					tempLegs.add(matrix.rows[routeOrigins.length-1].elements[2].durationInTraffic);
					tempLegs.add(matrix.rows[2].elements[3].durationInTraffic);
					tempLegs.add(matrix.rows[3].elements[0].durationInTraffic);
					tempLegs.add(matrix.rows[0].elements[1].durationInTraffic);
				}
				validPickup = false;
			}
			
			
			
			bestRoute.waypoints(updatedWay);
			return bestRoute;
		}
		
	}
	
	public boolean withinBounds(int timeToFinishFare){
		double band = 0;
		for(int i = 0; i < legLengths.size();i++){
			band += legLengths.get(i);
		}
		System.out.println(band + " = Band Time. ");
		
		band = band * (1 + percent);
		
		int newTime = timeToFinishFare + (travelTime - Fares.get(0).getFareStats().getTimestamp());
		System.out.println(band + " = Band Time. " + newTime + " = New Time");
		if(band > newTime)
			return true;
		else
			return false;
		
	}
	
	public void addFare(Fare fare) throws Exception{
		isActive = true;
		passengers += fare.getNumberOfPassengers();
		
		
		
		//update route of taxi to use both routes
		if(empty){
			getBestRoute(fare);
			empty = false;
			tempResult = bestRoute.await();
		}
		
		route = bestRoute;
		routeResult = tempResult;
		
		fare.makeStats(travelTime, fare.getNumberOfPassengers());
		Fares.add(fare);
		
		//gets length of each leg
		int distanceToEnd = 0;
		legLengths = new ArrayList<Integer>();
		DirectionsRoute r = routeResult.routes[0];
		for (int i = currentLeg; i < r.legs.length; i++) {
			distanceToEnd += (int)r.legs[i].duration.inSeconds;
			legLengths.add(distanceToEnd);
			distanceToEnd = 0;
		}
		
		polyLine = routeResult.routes[0].overviewPolyline.getEncodedPath();
		for(int d = 0;d < routeResult.routes[0].legs.length;d++ )
			System.out.println("Waypoints:" + routeResult.routes[0].legs[d].endAddress + "\n");
		currentStep = 0;
		currentLeg = 0;
		legTime = 0;
		System.out.println("Waypoint number = " +routeResult.geocodedWaypoints.length);
	
	}

	
	public void removeFare(Fare fare ){
		Fares.remove(fare);
		FareAnalytics.add(fare.getFareStats());
		passengers -= fare.getNumberOfPassengers();
		if(passengers == 0)
			empty = true;
		if(Fares.size() == 0)
			isActive = false;
	}
	
	public boolean isEmpty(){
		return empty;
	}
	
	public boolean available(Fare fare){
		if(size - passengers < fare.getNumberOfPassengers())
			return false;
		else if (!empty && !fare.isWillingToShare())
			return false;
		else
			return true;
	}
	
	public void update(int secondsMoved) throws Exception {
		// move location along based on route
		//moving it 2.5 minutes along as 180 seconds = 2.5 minutes
		DirectionsRoute r = routeResult.routes[0];
		DirectionsLeg l;
		if(isActive){
			secondsWithoutMovement += secondsMoved;
			travelTime += secondsMoved;
			legTime += secondsMoved;
		}
		
		int  stepMovement = 0;
		boolean foundPoint = false;

		for (int i = currentLeg; i < r.legs.length && !foundPoint; i++) {
				l = r.legs[i];
				for (int j = currentStep; j < l.steps.length; j++) {
					if(legTime > tempLegs.get(i).inSeconds){
						
						//as long as it isn't the last leg
						while(i < r.legs.length && legTime > tempLegs.get(i).inSeconds ){
							System.out.println(i + ": " + legTime + " Leg Time: " + tempLegs.get(i).inSeconds);
							passengerUpdate(r.legs[i].endAddress);
							currentLeg = i;
							currentStep = 0;
							foundPoint = true;
							secondsWithoutMovement = (int) (legTime - tempLegs.get(i).inSeconds);
							legTime = (int) (legTime - tempLegs.get(i).inSeconds);
							//check if  remaining legTime smaller than next 
							//leg. If not keep checking
							i++;
						}	
						j = l.steps.length;
					}
					else{
					stepMovement += l.steps[j].duration.inSeconds;
					// if they have moved step in allocated time than update
					// location
					if (stepMovement > secondsWithoutMovement) {
						
						// if taxi has moved to new step let the amount of
						// movement be reset
						if (currentLeg != i || currentStep != j)
							secondsWithoutMovement = 0;
						//LIMITATION:to reach the if here, the taxi has 
						//arrived too early and hasn't taken traffic into
						//account because google didn't like using traffic
						//for directions. THis holds the taxi so it arrives
						//on time
						if (currentLeg != i) 
							currentLeg = i;
						else{
							currentStep = j;
							location = l.steps[j].startLocation.toString();
						}

						foundPoint = true;
						i = r.legs.length;
						j = l.steps.length;

					} else if (currentLeg != r.legs.length - 1) {
						//in case taxi jumps too far and misses out on a place
						long timeToNextStop = (long) 0;
						for (int n = currentStep; n < l.steps.length; n++) {
							timeToNextStop += l.steps[n].duration.inSeconds;
						}
						if (secondsWithoutMovement > timeToNextStop) {
							i = r.legs.length;
							j = l.steps.length;
						}
						foundPoint = true;
					}
				}
						
				}
		}
		System.out.println("At leg " + currentLeg + " of " + r.legs.length +
				"At step " + currentStep + " of " + r.legs[currentLeg].steps.length);
		if (!foundPoint){
			
			location = r.legs[(r.legs.length - 1)].endLocation.toString();
			System.out.println(secondsWithoutMovement + "Finished");
			passengerUpdate(r.legs[(r.legs.length - 1)].endAddress);
			polyLine = "";
			
		}
			

	}
	
	public void passengerUpdate(String point) throws Exception{
		System.out.println("CHECKING PASSENGERS");
		boolean foundPickup = false , foundDest = false;
		DirectionsRoute r = routeResult.routes[0];
		long lastStop = 0;
		int  correctTime , offset;
		for (int n = currentStep; n < r.legs[currentLeg].steps.length; n++) {
			lastStop += r.legs[currentLeg].steps[n].duration.inSeconds;
		}
		offset = secondsWithoutMovement - (int) lastStop;
		correctTime = travelTime - offset;
		//checks if stop was pickup or destination
		for(int i = 0; i < Fares.size();i++){
			
			if(Fares.get(i).checkForPickup(point , context)){
				System.out.print(i + " PickupTime " + correctTime);
				Fares.get(i).setPickup(correctTime);
			}
			else if(Fares.get(i).checkForDestination(point , context)){
					//if destination then fare is removed
					System.out.print(i + " DropOff " + correctTime);
					Fares.get(i).setDropoff(correctTime);
					removeFare(Fares.get(i));
				}
		}
		System.out.println("\nCurrently: " + passengers);
		secondsWithoutMovement = 0;
	}
	
	public Duration evaluateFare(Fare fare) throws Exception {  
		//quickPick is used so taxi knows if they're looking for best
		//overall route or quickest pickup to avoid duplicate code
		quickPick = true;
		
		String[] destinations;
		String[] origins;
		Duration shortest;
		long time = 0;
		
			// if no current fare, just checks from location
		  
		  if (empty){
			  origins = new String[] {location};
			  destinations = new String[1];
			  destinations[0] = fare.getOrigin();
			  
			  DistanceMatrix matrix=DistanceMatrixApi.getDistanceMatrix(context, origins, destinations).await();
			  shortest = matrix.rows[0].elements[0].duration;
			  System.out.println("Taxi Num: " + number + "  Time: " + shortest);
			  return shortest;
		  }
			
		  validPickup = true;
		  
		  getBestRoute(fare);

		  tempResult = bestRoute.await();
		  DirectionsRoute r = tempResult.routes[0];
		  boolean found = false;
			  for (int i = 0; i < r.legs.length && !found; i++) {
					if(!(fare.checkForPickup(r.legs[i].startAddress, context)))
						time += tempLegs.get(i).inSeconds;
					else 
						found = true;
				}
			  shortest = new Duration();
			  shortest.inSeconds = time;
			  System.out.println(shortest.humanReadable);
		  //passenger gets taxi thats closest to them and doesn't intrude on current fares
		  //Note: Not the shortest overall trip
		  System.out.println("Taxi Num: " + number + "  Time: " + shortest);
		  return shortest;
		}
	
	public Long evaluateTotalLength(Fare fare) throws Exception {
		
		quickPick = false;
		  
		getBestRoute(fare);

		tempResult = bestRoute.await();
		boolean found = false;
		Long shortest;
		shortest = tempResult.routes[0].legs[0].duration.inSeconds;
		for (int i = 0; i < tempResult.routes[0].legs.length && !found; i++){
			if(!(fare.checkForDestination(tempResult.routes[0].legs[i].startAddress, context)))
				shortest += tempLegs.get(i).inSeconds;
			else 
				found = true;
		}

		return shortest;
	}
	
	public int finishUp() throws Exception{
		//simulator has been requested to finish up so route is fastfowarded to its end
		long distanceToEnd = 0;
		if(isActive){
			DirectionsLeg l;
			DirectionsRoute r = routeResult.routes[0];
			for (int i = currentLeg; i < r.legs.length; i++) {
				l = r.legs[i];
				distanceToEnd += (int)tempLegs.get(i).inSeconds - legTime;
				//sets it up so there should be no offset so time is accurate
				travelTime += distanceToEnd;
				secondsWithoutMovement += distanceToEnd;
				currentLeg = i;
				passengerUpdate(l.endAddress);
				secondsWithoutMovement = 0;
				currentStep = 0;
				legTime = 0;
			}
			
			
			isActive = false;
			update(0);
		}
		return (int)distanceToEnd;
		
	}
	
	public String toString(){
		String result = "Taxi number " + number + ". It has " + passengers + " passengers";
		result += "\n";
		return result;
	}
	
	public String getPolyLine(){
		
		return polyLine;
	}
	
	public ArrayList<FareStats> getFareStats(){
		return FareAnalytics;
	}
	
	public int getTravelTime(){
		return travelTime;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setPolyLine(String polyLine) {
		this.polyLine = polyLine;
	}
}
