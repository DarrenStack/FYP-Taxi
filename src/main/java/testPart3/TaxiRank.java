package testPart3;

import java.util.ArrayList;
import java.util.List;

import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class TaxiRank {
	
	private List<Taxi> taxiRank;
	private GeoApiContext context;
	private ArrayList<LatLng> taxiStartLocations;
	
	public TaxiRank(int numTaxis , ArrayList<String> startList, String percentage) throws Exception{
		taxiRank = new ArrayList<Taxi>();
		taxiStartLocations = new ArrayList<LatLng>();
		context = new GeoApiContext();
		context.setApiKey("AIzaSyCpto6czmXSCmH6FzaiHsX1OmuTi96ZRLE"); 
		LatLng latLngLocation;
		for(int i = 0;i < startList.size();i++){
			GeocodingResult[] results = com.google.maps.GeocodingApi.geocode(context, startList.get(i)).await();
			latLngLocation = results[0].geometry.location;
			taxiStartLocations.add(latLngLocation);
		}
		int randNum;
		double percent = Double.parseDouble(percentage) / 100.0;
		for(int i = 0; i < numTaxis; i++){
			randNum = (int) (Math.random()*taxiStartLocations.size());
			Taxi t = new Taxi(4,i, taxiStartLocations.get(randNum).toString(), percent);
			taxiRank.add(t);
		}
			
	}
	
	public int getTaxiAmount(){
		return taxiRank.size();
	}
	
	public Taxi getTaxi(int taxiNum){
		return taxiRank.get(taxiNum);
	}
	
	public List<Taxi> getTaxis(){
		return taxiRank;
	}

}
