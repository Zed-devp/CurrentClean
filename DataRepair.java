import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class DataRepair {
	
	BufferedReader br_updated = null;
	
	
	public Map<String, Map<String, Double>> RepairProbability (Map <String, String> staleCells) {
		
		Map<String, Map<String, Double>> repairProbability = new HashMap<String, Map<String, Double>> ();		
		Map<String, String> updated = new HashMap<String, String> ();
//		List<String> singleRepProb = new ArrayList <String> ();
		
		//scan Updated file, save information to maps.
		try{
			br_updated = new BufferedReader(new FileReader("Input/updated.tsv"));
			String lineUpd = br_updated.readLine();			
			
			while (lineUpd != null){
//				System.out.println(lineUpd);
				String updCell = lineUpd.split("\t")[0];
				String updTime = lineUpd.split("\t")[1];
				String updValue = lineUpd.split("\t")[2];
				
				updated.put(updCell + "_" + updTime, updValue);
				
				lineUpd = br_updated.readLine();
			}
			
		}catch (Exception e){
			System.out.println("Failed!");
		}finally{
			if (br_updated != null){
				try{
					br_updated.close();
					br_updated = null;
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		
		// scan the Updated file to make statistics of each possible value
		for (Map.Entry<String, String> entry : staleCells.entrySet()) {
			
			// save each single repair value with its frequency
			Map<String, Integer> singleRepCount = new HashMap<String, Integer> ();
			
			// save each single repair value with its probability
			Map<String, Double> singleRepProb = new HashMap<String, Double> ();
			
			
			// if the cells are not identified by negative causality pattern
			if(!entry.getValue().equals("nc")) {
//				System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
				// the start attribute (of update pattern)
				String targetAttr = entry.getValue().split("_")[0];
				// the value of start attribute (of update pattern)
				String targetValue = entry.getValue().split("_")[1];
				

				int totalCount = 0;
			
//				System.out.println(targetAttr);
//				System.out.println(targetValue);
				
				for (Map.Entry<String, String> entryUpdated : updated.entrySet()){
					String testRelation = entryUpdated.getKey().split("_")[0];
					String testTuple = entryUpdated.getKey().split("_")[1];
					String testAttr = entryUpdated.getKey().split("_")[2];
					String testTime = entryUpdated.getKey().split("_")[3];
					String testValue = entryUpdated.getValue().split("_")[0];
					
					// if the record in the Updated file has the same target attribute and same target value
					// check whether the expected update-cell also exists in the Updated file
					if (testAttr.equals(targetAttr) && testValue.equals(targetValue)){
						// get the expected update-cell
						String expUpdCell = testRelation+"_"+testTuple+"_"+entry.getKey().split("_")[2]+"_"+testTime;
						
						String expValue = updated.get(expUpdCell);
						
						// if the expected update-cell exists in the Updated file
						// increase the number of each possible value
						if (expValue != null){
							totalCount ++;
							
							if (singleRepCount.get(expValue) == null){
								singleRepCount.put(expValue, 1);
							}
							else{
								singleRepCount.put(expValue, singleRepCount.get(expValue)+1);
							}
						
//							System.out.println(expUpdCell+" "+expValue);
						}
					
//						if (testRelation+"_"+testTuple+"_"+entry.getKey().split("_")[2]+"_"+testTime)
//						System.out.println("a");
					}
				}
//				System.out.println(totalCount);
				for (Map.Entry<String, Integer> temp : singleRepCount.entrySet()) {
//					System.out.println("Key = " + temp.getKey() + ", Value = " + temp.getValue());
					singleRepProb.put(temp.getKey(), new Double((double)temp.getValue())/totalCount);
				}
				
//				for (Map.Entry<String, Double> temp2 : singleRepProb.entrySet()) {
//					System.out.println("Key = " + temp2.getKey() + ", Value = " + temp2.getValue());
//				}
				
				repairProbability.put(entry.getKey(), singleRepProb);
				
//				for (Map.Entry<String, Map<String, Double>> temp2 : repairProbability.entrySet()) {
//					System.out.println("Key = " + temp2.getKey() + ", Value = " + temp2.getValue());
//				}	
				
			}
			
			// if the cells are identified by negative causality pattern
			else {
				try{
					br_updated = new BufferedReader(new FileReader("Input/updated.tsv"));
					String lineUpd = br_updated.readLine();	
					
//					System.out.println(entry.getKey());
					
					while (lineUpd != null){
//						System.out.println(lineUpd);
						String updCell = lineUpd.split("\t")[0];
//						String updTime = lineUpd.split("\t")[1];
						String updValue = lineUpd.split("\t")[2];
						
						String cell = entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1]+ "_" + entry.getKey().split("_")[2];
						
						if(cell.equals(updCell)) {
							singleRepProb.put(updValue, 1.0);
							repairProbability.put(entry.getKey(), singleRepProb);
							break;
						}
						
						lineUpd = br_updated.readLine();
					}
					
				}catch (Exception e){
					System.out.println("Failed!");
				}finally{
					if (br_updated != null){
						try{
							br_updated.close();
							br_updated = null;
						}catch (Exception e){
							e.printStackTrace();
						}
					}
				}
				
			}
		
	
		}
		
		return repairProbability;
		
	}

}
