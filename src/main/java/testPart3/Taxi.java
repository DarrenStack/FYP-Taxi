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
	private ArrayList<Long> tempLegs;
	private String location;
	private int secondsWithoutMovement;
	private GeoApiContext context;
	private DirectionsApiRequest route, bestRoute;
	private double percent , distanceBenchmark;

	//AIzaSyAXXZYdq4tkUe1G5Ga8dFem13VrzuvMoeQ
	//AIzaSyCpto6czmXSCmH6FzaiHsX1OmuTi96ZRLE
	
	private DirectionsResult routeResult , tempResult;
	private boolean  empty , quickPick , isActive , validPickup , after, travelBack;
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
		
		after = false;
		
		location = newLocation;
		System.out.print(location);
		secondsWithoutMovement = 0; currentStep = 0; currentLeg = 0;
	}
	
	public DirectionsApiRequest getBestRoute(Fare fare) throws Exception{
		//finds best possible route  for all stops in taxi
		System.out.print("a");
		bestRoute = null;
		tempLegs = new ArrayList<Long>();
		//if no current fare
		if(empty){
			
			String[] origins = new String[]{location , fare.getOrigin()};
			String[] destinations = new String[]{fare.getOrigin() , fare.getDestination() };
			
			DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations);
			
			
			request.departureTime(fare.getTime());
			if(fare.getTrafficModel().equals("PESSIMISTIC"))
				request.trafficModel(TrafficModel.PESSIMISTIC);
			else if(fare.getTrafficModel().equals("OPTIMISTIC"))
				request.trafficModel(TrafficModel.OPTIMISTIC);
			else
				request.trafficModel(TrafficModel.BEST_GUESS);
			
			request.mode(TravelMode.DRIVING);
			
			DistanceMatrix matrix= request.await();
			
			tempLegs.add(matrix.rows[0].elements[0].durationInTraffic.inSeconds);
			tempLegs.add( matrix.rows[1].elements[1].durationInTraffic.inSeconds);
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
			
			//adding new fare points
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
				//only adds origins/destination that are valid
				positionTracker.add(count, (2*(k+1)) + 1);
				destPoints.add(count);
				count++;
			}
			routeOrigins[(Fares.size() * 2) + 2] = location;
			
			//sets up matrix with origins + destinations + traffic
			
			DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, routeOrigins, routeDest);
			
			
			request.departureTime(fare.getTime());
			if(fare.getTrafficModel().equals("PESSIMISTIC"))
				request.trafficModel(TrafficModel.PESSIMISTIC);
			else if(fare.getTrafficModel().equals("OPTIMISTIC"))
				request.trafficModel(TrafficModel.OPTIMISTIC);
			else
				request.trafficModel(TrafficModel.BEST_GUESS);
			
			request.mode(TravelMode.DRIVING);
			
			DistanceMatrix matrix= request.await();
			if(after){
				getAfterRoute(fare, matrix , routeOrigins);
				return bestRoute;
			}
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
								
								tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds);
								
								bestRoute = DirectionsApi.getDirections(context, location,
										routeDest[positionTracker.get(positionTracker.size() - 1)]);
								updatedWay = new String[positionTracker.size() - 1];
								for (int n = 0; n < positionTracker.size() - 1; n++) {
									tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
									         									.get(n + 1)].durationInTraffic.inSeconds);
									updatedWay[n] = routeOrigins[positionTracker.get(n)];
								}

							}
						}
						else if( time < shortestTime && withinBounds(timeToFinishFirstFare)){
							shortestTime = time;
							//update length of legs of trip
							
							tempLegs.clear();
							
							tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds);
							
							bestRoute = DirectionsApi.getDirections(context, location,
									routeDest[positionTracker.get(positionTracker.size() - 1)]);
							updatedWay = new String[positionTracker.size() - 1];
							for (int n = 0; n < positionTracker.size() - 1; n++) {
								tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
								         									.get(n + 1)].durationInTraffic.inSeconds);
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
							
							tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds);
							
							bestRoute = DirectionsApi.getDirections(context, location,
									routeDest[positionTracker.get(positionTracker.size() - 1)]);
							updatedWay = new String[positionTracker.size() - 1];
							for (int n = 0; n < positionTracker.size() - 1; n++) {
								tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
								         									.get(n + 1)].durationInTraffic.inSeconds);
								updatedWay[n] = routeOrigins[positionTracker.get(n)];
							}
							
							
						}
					}
					else if( time < shortestTime && withinBounds(timeToFinishFirstFare)){
						shortestTime = time; 
						//update length of legs of trip
						
						tempLegs.clear();
						
						tempLegs.add(matrix.rows[routeOrigins.length - 1].elements[positionTracker.get(0)].durationInTraffic.inSeconds);
						
						bestRoute = DirectionsApi.getDirections(context, location,
								routeDest[positionTracker.get(positionTracker.size() - 1)]);
						updatedWay = new String[positionTracker.size() - 1];
						for (int n = 0; n < positionTracker.size() - 1; n++) {
							tempLegs.add(matrix.rows[positionTracker.get(n)].elements[positionTracker
							         									.get(n + 1)].durationInTraffic.inSeconds);
							updatedWay[n] = routeOrigins[positionTracker.get(n)];
						}
						
						
					}
				}
				//resets position tracker to original destination points
				positionTracker.remove( positionTracker.size() -1);
				positionTracker.add(destPoints.get(i), temp);
			}
			
			if(shortestTime == null){
				getAfterRoute(fare, matrix , routeOrigins);
				return bestRoute;
			}
			
			
			
			bestRoute.waypoints(updatedWay);
			return bestRoute;
		}
		
	}
	
	public void getAfterRoute(Fare fare, DistanceMatrix matrix ,String[] routeOrigins){
		bestRoute = DirectionsApi.getDirections(context, location,
				fare.getDestination());
		tempLegs.clear();
		String[] updatedWay;
		
		
		if(Fares.get(0).isPassedOrigin()){
			updatedWay = new String[]{Fares.get(0).getDestination() ,fare.getOrigin()};
			tempLegs.add(matrix.rows[routeOrigins.length-1].elements[3].durationInTraffic.inSeconds);
			tempLegs.add(matrix.rows[3].elements[0].durationInTraffic.inSeconds);
			tempLegs.add(matrix.rows[0].elements[1].durationInTraffic.inSeconds);
		}
		else{
			updatedWay = new String[]{Fares.get(0).getOrigin(),
					Fares.get(0).getDestination(),fare.getOrigin()};
			tempLegs.add(matrix.rows[routeOrigins.length-1].elements[2].durationInTraffic.inSeconds);
			tempLegs.add(matrix.rows[2].elements[3].durationInTraffic.inSeconds);
			tempLegs.add(matrix.rows[3].elements[0].durationInTraffic.inSeconds);
			tempLegs.add(matrix.rows[0].elements[1].durationInTraffic.inSeconds);
		}
		
		bestRoute.waypoints(updatedWay);
	}
	
	public long getExtraRoutePickupTime(Fare fare) throws Exception{
		tempLegs.clear();
		long distanceToEnd;
		distanceToEnd = legLengths.get(currentLeg) - legTime;
		tempLegs.add(distanceToEnd);
		for (int i = currentLeg + 1; i < legLengths.size(); i++) {
			distanceToEnd += legLengths.get(i);
			tempLegs.add((long) legLengths.get(i));
		}
		//get distance between dropoff of current fares and new fare
		DirectionsRoute r = routeResult.routes[0];
		DirectionsLeg l = r.legs[r.legs.length-1];
		String[] origin = new String[]{l.endAddress};
		String[] destination = new String[]{fare.getOrigin()};
		
		DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, origin, destination);
		
		
		request.departureTime(fare.getTime());
		if(fare.getTrafficModel().equals("PESSIMISTIC"))
			request.trafficModel(TrafficModel.PESSIMISTIC);
		else if(fare.getTrafficModel().equals("OPTIMISTIC"))
			request.trafficModel(TrafficModel.OPTIMISTIC);
		else
			request.trafficModel(TrafficModel.BEST_GUESS);
		
		request.mode(TravelMode.DRIVING);
		
		DistanceMatrix matrix= request.await();
		
		long bridge =  matrix.rows[0].elements[0].durationInTraffic.inSeconds;
		tempLegs.add(bridge);
		distanceToEnd += bridge;
		return distanceToEnd;
		
		
	}
	
	public void getExtraRoute(Fare fare) throws Exception{
		bestRoute = DirectionsApi.getDirections(context, location,
				fare.getDestination());
		DirectionsRoute r = routeResult.routes[0];
		String[] points = new String[(r.legs.length - currentLeg) + 1];
		for(int i = 0; i + currentLeg < r.legs.length;i++){
			points[i] = r.legs[i+currentLeg].endAddress;
		}
		points[points.length-1] = fare.getOrigin();
		
		bestRoute.waypoints(points);
		
		String[] origin = new String[]{fare.getOrigin()};
		String[] destination = new String[]{fare.getDestination()};
		
		DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, origin, destination);
		
		
		request.departureTime(fare.getTime());
		if(fare.getTrafficModel().equals("PESSIMISTIC"))
			request.trafficModel(TrafficModel.PESSIMISTIC);
		else if(fare.getTrafficModel().equals("OPTIMISTIC"))
			request.trafficModel(TrafficModel.OPTIMISTIC);
		else
			request.trafficModel(TrafficModel.BEST_GUESS);
		
		request.mode(TravelMode.DRIVING);
		
		DistanceMatrix matrix= request.await();
		tempLegs.add(matrix.rows[0].elements[0].durationInTraffic.inSeconds);
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
		travelBack = false;
		
		
		isActive = true;
		passengers += fare.getNumberOfPassengers();
		
		
		
		//update route of taxi to use both routes
		if(empty){
			getBestRoute(fare);
			empty = false;
			tempResult = bestRoute.await();
		}
		System.out.print(after);
		if(after)
			tempResult = bestRoute.await();
		
		route = bestRoute;
		routeResult = tempResult;
		
		fare.makeStats(travelTime, fare.getNumberOfPassengers());
		Fares.add(fare);
		
		//gets length of each leg
		legLengths = new ArrayList<Integer>();
		for (int i = 0; i < tempLegs.size(); i++) {
			long legLength = tempLegs.get(i);
			legLengths.add((int) legLength);
		}
		
		polyLine = routeResult.routes[0].overviewPolyline.getEncodedPath();
		for(int d = 0;d < routeResult.routes[0].legs.length;d++ )
			System.out.println("Waypoints:" + routeResult.routes[0].legs[d].endAddress + "\n");
		currentStep = 0;
		currentLeg = 0;
		legTime = 0;
		after = false;
		System.out.println("Waypoint number = " +routeResult.geocodedWaypoints.length);
	
	}
	
	
	//used to calculate distance between two coordinates
	public double haversine(String location1, String location2) {
        
		double R = 6372.8;
		
		String[] points = location1.split(",");
		double lat1 = Double.parseDouble(points[0]);
		double lon1 = Double.parseDouble(points[1]);
		
		points = location2.split(",");
		double lat2 = Double.parseDouble(points[0]);
		double lon2 = Double.parseDouble(points[1]);
		  
		double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
	
	
	//distance between taxi and Fare
	public double getTaxiDistance(LatLng pickupLatLng){
		//fare
		double distance = -1;
		if(after){
			String dest = getDestLatLng();
			distance = haversine(location , dest);
			distance += haversine(dest , pickupLatLng.toString());
		}
		else{
			distance = haversine(location , pickupLatLng.toString());
		}
		distanceBenchmark = distance;
		return distance;
	}
	
	//gets lat lng of taxi destination
	public String getDestLatLng(){
		DirectionsLeg leg= routeResult.routes[0].legs[routeResult.routes[0].legs.length - 1];
		LatLng farePoint = leg.endLocation;
		return farePoint.toString();
		
	}

	//closest point at the time of check so its the benchmark for others
	//to compare
	public double getBenchmark(){
		return distanceBenchmark;
	}
	
	public boolean between(LatLng pickupLatLng){
		//sees if pickup location between where taxi already going
		String[] destPoints = getDestLatLng().split(",");
		String[] locationPoints = location.split(",");
		double locLat = Double.parseDouble(locationPoints[0]);
		double destLat = Double.parseDouble(destPoints[0]);
		double maxLat , minLat , maxLng, minLng;
		//between latitudes
		if(destLat > locLat){
			maxLat = destLat;
			minLat = locLat;
		}
		else{
			maxLat = locLat;
			minLat = destLat;
		}
		if(pickupLatLng.lat < maxLat && pickupLatLng.lat > minLat)
			return true;
		
		//between longitudes
		double locLng = Double.parseDouble(locationPoints[1]);
		double destLng = Double.parseDouble(destPoints[1]);
		if(destLng > locLng){
			maxLng = destLng;
			minLng = locLng;
		}
		else{
			maxLng = locLng;
			minLng = destLng;
		}
		
		if(pickupLatLng.lng < maxLng && pickupLatLng.lng > minLng)
			return true;
		
		return false;
	}
	
	public void removeFare(Fare fare ){
		Fares.remove(fare);
		FareAnalytics.add(fare.getFareStats());
		passengers -= fare.getNumberOfPassengers();
		if(passengers == 0)
			empty = true;
		legTime = 0;
	}
	
	public boolean isEmpty(){
		return empty;
	}
	
	public boolean available(Fare fare){
		if(Fares.size() >= 2){
			after = true;
			return true;
		}
		else if(size - passengers < fare.getNumberOfPassengers()){
			after = true;
			return true;
		}
		else if (!empty && (!fare.isWillingToShare() || !(Fares.get(0).isWillingToShare()))){
			after = true;
			return true;
		}
		else{
			after = false;
			return true;
		}
	}
	
	public void update(int secondsMoved) throws Exception {
		// move location along based on route
		//moving it 2.5 minutes along as 180 seconds = 2.5 minutes
		DirectionsRoute r = routeResult.routes[0];
		DirectionsLeg l;
		boolean foundPoint = false;
		if(isActive){
			if(Fares.size() == 0 && travelBack == false){
				isActive = false;
				foundPoint = true;
			} else {
				secondsWithoutMovement += secondsMoved;
				travelTime += secondsMoved;
				legTime += secondsMoved;
			}
		}
		
		int  stepMovement = 0;
		
		if(travelBack){
			System.out.println("TravelBack LegTime = " + legTime + " of " + legLengths.get(0) );
		}
		
		//System.out.println("At leg " + currentLeg + " of " + r.legs.length +
			//	"At step " + currentStep + " of " + r.legs[currentLeg].steps.length);

		for (int i = currentLeg; i < r.legs.length && !foundPoint; i++) {
				l = r.legs[i];
				for (int j = currentStep; j < l.steps.length; j++) {
					if(legTime > legLengths.get(i)){
						
						//as long as it isn't the last leg
						while(i < r.legs.length - 1 && legTime > legLengths.get(i) ){
							System.out.println(i + ": " + legTime + " Leg Time: " + legLengths.get(i));
							passengerUpdate(r.legs[i].endAddress);
							currentLeg = i + 1;
							currentStep = 0;
							
							secondsWithoutMovement = (int) (legTime - legLengths.get(i));
							legTime = (int) (legTime - legLengths.get(i));
							//check if  remaining legTime smaller than next 
							//leg. If not keep checking
							if(legTime < legLengths.get(i+1))
								foundPoint = true;
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
				
		
		if (!foundPoint){
			
			location = r.legs[(r.legs.length - 1)].endLocation.toString();
			System.out.println(secondsWithoutMovement + "Finished");
			if(!travelBack)
				passengerUpdate(r.legs[(r.legs.length - 1)].endAddress);
			else
				isActive = false;
			polyLine = "";
			
		}
			

	}
	
	public void passengerUpdate(String point) throws Exception{
		System.out.println("CHECKING PASSENGERS");
		int  correctTime , offset;
		
		offset = legTime - legLengths.get(currentLeg);
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
	
	public long evaluateFare(Fare fare) throws Exception {  
		//quickPick is used so taxi knows if they're looking for best
		//overall route or quickest pickup to avoid duplicate code
		quickPick = true;
		
		String[] destinations;
		String[] origins;
		long shortest;
		long time = 0;
		
		if(after){
			long pickupTime = getExtraRoutePickupTime(fare);
			getExtraRoute(fare);
			return pickupTime;
		}
			// if no current fare, just checks from location
		  
		  if (empty){
			  origins = new String[] {location};
			  destinations = new String[1];
			  destinations[0] = fare.getOrigin();
			  
			  DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations);
				
				
				request.departureTime(fare.getTime());
				if(fare.getTrafficModel().equals("PESSIMISTIC"))
					request.trafficModel(TrafficModel.PESSIMISTIC);
				else if(fare.getTrafficModel().equals("OPTIMISTIC"))
					request.trafficModel(TrafficModel.OPTIMISTIC);
				else
					request.trafficModel(TrafficModel.BEST_GUESS);
				
				request.mode(TravelMode.DRIVING);
				
				DistanceMatrix matrix= request.await();
				
			  shortest = matrix.rows[0].elements[0].duration.inSeconds;
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
						time += tempLegs.get(i);
					else 
						found = true;
				}
			  
			  shortest = time;
			  System.out.println(shortest);
		  //passenger gets taxi thats closest to them and doesn't intrude on current fares
		  //Note: Not the shortest overall trip
		  System.out.println("Taxi Num: " + number + "  Time: " + shortest);
		  return shortest;
		}
	
	public Long evaluateTotalLength(Fare fare) throws Exception {
		
		quickPick = false;
		  
		if(after){
			long timeToPickup = getExtraRoutePickupTime(fare);
			getExtraRoute(fare);
			return timeToPickup + tempLegs.get(tempLegs.size() - 1);
		}
		getBestRoute(fare);

		tempResult = bestRoute.await();
		boolean found = false;
		Long shortest;
		shortest = (long) 0;
		for (int i = 0; i < tempResult.routes[0].legs.length && !found; i++){
			if(!(fare.checkForDestination(tempResult.routes[0].legs[i].startAddress, context)))
				shortest += tempLegs.get(i);
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
				distanceToEnd += (int)legLengths.get(i) - legTime;
				//sets it up so there should be no offset so time is accurate
				travelTime += (int)legLengths.get(i) - legTime;
				secondsWithoutMovement += (int)legLengths.get(i) - legTime;
				legTime = legLengths.get(i);
				currentLeg = i;
				if(!travelBack)
					passengerUpdate(l.endAddress);
				secondsWithoutMovement = 0;
				currentStep = 0;
				legTime = 0;
			}
			
			isActive = false;
			update(0);
		}
		System.out.println("DTE" + distanceToEnd);
		return (int)distanceToEnd;
		
	}
	
	public void moveToHub(String hubLocation , DateTime time) throws Exception{
		routeResult = DirectionsApi.getDirections(context, location,
				hubLocation).await();
		travelBack = true;
		String[] origins = new String[]{location};
		String[] destinations = new String[]{hubLocation};
		DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations);
		
		
		request.departureTime(time);
		request.trafficModel(TrafficModel.BEST_GUESS);
		
		request.mode(TravelMode.DRIVING);
		
		DistanceMatrix matrix= request.await();
		
		long distanceToHub =  matrix.rows[0].elements[0].durationInTraffic.inSeconds;
		legTime = 0;
		currentLeg = 0;
		currentStep = 0;
		legLengths.clear();
		legLengths.add((int) distanceToHub);
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
	
	public int getNumberOfFares(){
		return Fares.size();
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

	public boolean getIsActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public int getPassengerNum(){
		return passengers;
	}
	public boolean getTravelBack(){
		return travelBack;
	}
}
