import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CurrenctClean {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		double beta = Double.parseDouble(args[0]);
		double beta = 0.6;
		FileWriter fw_stalecells = null;
		FileWriter fw_repair = null;
		
		// <stale cells, start value>
		Map<String, String> staleCells = new HashMap<String, String> ();
		// <stale cells, probability>
		Map<String, String> staleCellsProb = new HashMap<String, String> ();
		// <stale cells, <value, probability>> 
		Map<String, Map<String, Double>> repairProbability = new HashMap<String, Map<String, Double>> ();
		
		CurrencyEstimation ce = new CurrencyEstimation();
		StaleCellsProb scp = new StaleCellsProb();
		DataRepair dr = new DataRepair();		
		UpdPattern updpattern = new UpdPattern();
		
		// update pattern list
		ArrayList <String> updpat = updpattern.UpdPatPreprocess(beta);		
		for (String tmp: updpat) {
			System.out.println(tmp);
		}	
		staleCells = ce.IdentifyStaleCell(updpat);	
		staleCellsProb = scp.StaleCellsProbability(staleCells, beta);
		repairProbability = dr.RepairProbability(staleCells);
		
		try {
			fw_stalecells = new FileWriter("Output/stalecells");
			fw_repair = new FileWriter("Output/repairs");
			
			for (Map.Entry<String, Map<String, Double>> entry : repairProbability.entrySet()) {
//				System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//				Map<String, Double> temp = entry.getValue();
				
				List<Map.Entry<String, Double>> infoIds = new ArrayList<Map.Entry<String, Double>>( entry.getValue().entrySet()); 
				
		        Collections.sort(infoIds, new Comparator<Map.Entry<String, Double>>() {  
		            public int compare(Map.Entry<String, Double> o1,  
		                    Map.Entry<String, Double> o2) {  
		                return -(o1.getValue()).toString().compareTo(o2.getValue().toString());  
		            }  
		        }); 
					        

	            if(infoIds.size() > 0){
//		            System.out.println("The cell " + entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1]+ "_" + entry.getKey().split("_")[2] 
//            		+ " is out-of-date, the possible values with probabilities are shown as follows: ");
	            	String scprob = staleCellsProb.get(entry.getKey());

	            	fw_stalecells.write(entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1]+ "_" + entry.getKey().split("_")[2]+ ":" + scprob +"\r");
	            	fw_repair.write(entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1]+ "_" + entry.getKey().split("_")[2]);
            
			        for (int i = 0; i < infoIds.size(); i++) {  
			            String value = infoIds.get(i).getKey();  
			            Double probability = infoIds.get(i).getValue();		            
			            //System.out.println(value + ": " + probability);
			            
			            fw_repair.write("|" + value + ":" + probability);
			        }
			        fw_repair.write("\r");
	            }
			}
			
			fw_stalecells.flush();
			fw_repair.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for (Map.Entry<String, String> entry : staleCellsProb.entrySet()) {
//			System.out.println(entry.getKey()+"_"+entry.getValue());
//		}
		
		
//		List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>( singleRepCount.entrySet()); 
//
//		Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() { 	
//			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) { 
//				return (o2.getValue() - o1.getValue()); 
//			} 
//		}); 
//
//	    for (int i = 0; i < infoIds.size(); i++) { 
//		    Entry<String, Integer> id = infoIds.get(i); 
////		    System.out.println(id.getKey()+" "+ new Double((double)id.getValue())/totalCount);
//		    singleRepProb.add(id.getKey()+" "+ new Double((double)id.getValue())/totalCount);
//	    } 
		
		
//		for (Map.Entry<String, String> entry : staleCells.entrySet()) {
//			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//		}	

	}

}