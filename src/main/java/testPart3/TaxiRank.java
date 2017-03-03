package testPart3;

import java.util.ArrayList;
import java.util.List;

public class TaxiRank {
	
	private List<Taxi> taxiRank;
	
	public TaxiRank(int numTaxis , ArrayList<String> startList, String percentage) throws Exception{
		taxiRank = new ArrayList<Taxi>();
		int randNum;
		double percent = Double.parseDouble(percentage) / 100.0;
		for(int i = 0; i < numTaxis; i++){
			randNum = (int) (Math.random()*startList.size());
			Taxi t = new Taxi(4,i, startList.get(randNum), percent);
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
