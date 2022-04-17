import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CurrencyEstimation {
	BufferedReader br_updpat = null;
	BufferedReader br_lastupd = null;
	
	
	public Map<String, String> IdentifyStaleCell (ArrayList<String> updpat) {
		
		// <last update cell, last update time>
		Map<String, String> lastupdMap = new HashMap<String, String> ();
		// <last update cell, last update value>
		Map<String, String> lastupdValueMap = new HashMap<String, String> ();
		// <stale cells, start value>
		Map<String, String> staleCells = new HashMap<String, String> ();
		String currentTime = "0";		

		
		// scan LastUpdate file, save information to maps.
		try{
			br_lastupd = new BufferedReader(new FileReader("Input/lastupd.tsv"));
			String lineLastUpd = br_lastupd.readLine();			
			
			while (lineLastUpd != null){
				String lastupdCell = lineLastUpd.split("\t")[0];
				String lastupdTime = lineLastUpd.split("\t")[1];
				String lastupdValue = lineLastUpd.split("\t")[2];
				
				lastupdMap.put(lastupdCell, lastupdTime);		
				
				lastupdValueMap.put(lastupdCell, lastupdValue);
				
				currentTime = lastupdTime;
				
				lineLastUpd = br_lastupd.readLine();
			}
			
		}catch (Exception e){
			System.out.println("Failed!");
		}finally{
			if (br_lastupd != null){
				try{
					br_lastupd.close();
					br_lastupd = null;
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		
//		System.out.println(currentTime);
		
		// for each update pattern, test the record in LastUpdate
		for(String lineUpdPat:updpat){
			String updpatStartAttr = lineUpdPat.split(",")[0];
			String updpatEndAttr = lineUpdPat.split(",")[1];
			String updpatType = lineUpdPat.split(",")[2];
			String updpatTimeUnit = lineUpdPat.split(",")[3];
			
			//+ causality update pattern
			if(updpatType.equals("1")){
				for (Map.Entry<String, String> entry : lastupdMap.entrySet()) {
					if(entry.getKey().split("_")[2].equals(updpatStartAttr)){
						
						// timeunit = 0, updated in the same time unit
						if (updpatTimeUnit.equals("0")){
							//based on the update pattern, what is the expected update-cell
							String exp = entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1] + "_" + updpatEndAttr; 
							//whether the expected update-cell is updated
							boolean flag1 = lastupdMap.containsKey(exp);
							boolean flag2 = false;
							
//							System.out.println(entry.getKey()+"|"+entry.getValue()+"|"+exp);
//							System.out.println(flag1);
							if (flag1){							
//								if (Integer.parseInt(entry.getValue()) > Integer.parseInt(lastupdMap.get(exp))){
//									flag2 = true;
//								}
								if (entry.getValue().compareTo(lastupdMap.get(exp)) > 0){
									flag2 = true;
								}
								
							}
//							System.out.println(flag2);
//							System.out.println(flag);
//							System.out.println(lastupdMap.get(exp));
//							System.out.println(entry.getValue());
							
							// if the expected update-cell is not updated, or it is updated but is not updated at correct time unit, then it is out-of-date						
							if ((!flag1) || (flag1 && flag2)){
								
								if(!staleCells.containsKey(exp+ "_" + entry.getValue())){
//									System.out.println(entry.getKey());
									String startAttrValue = updpatStartAttr + "_" + lastupdValueMap.get(entry.getKey()) + "_" + "0";
//									System.out.println("Stale Cell: " + exp + " Start Attribute Value :" + startAttrValue);
									
									// key: expected update-cell; value: the start attribute (of update pattern) with its value in the LastUpdate 
									String expFinal = exp + "_" + entry.getValue();
//									System.out.println(expFinal);
									staleCells.put(expFinal, startAttrValue);
//									System.out.println(expFinal + ";;;" + startAttrValue);
								}
							}
						}
						
						
						// timeunit = 1, updated in the same time unit
						if (updpatTimeUnit.equals("1")){
							//based on the update pattern, what is the expected update-cell
							String exp = entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1] + "_" + updpatEndAttr; 
							//whether the expected update-cell is updated
							boolean flag1 = lastupdMap.containsKey(exp);
							boolean flag2 = false;
							
//							System.out.println(entry.getKey()+"|"+entry.getValue()+"|"+exp);
//							System.out.println(flag1);
//							System.out.println(entry.getValue());
//							System.out.println(lastupdMap.get(exp));
							
							if (flag1){	

								if (Integer.parseInt(entry.getValue()) > Integer.parseInt(lastupdMap.get(exp)) - 1 ){
									flag2 = true;
								}
								
							}
							
//							System.out.println(flag2);
							
//							System.out.println(flag);
//							System.out.println(lastupdMap.get(exp));
//							System.out.println(entry.getValue());
							
							// if the expected update-cell is not updated, or it is updated but is not updated at correct time unit, then it is out-of-date						
							if ((!flag1) || (flag1 && flag2)){
								
								if(!staleCells.containsKey(exp) && !entry.getValue().equals(currentTime)){
//									System.out.println(entry.getKey());
									String startAttrValue = updpatStartAttr + "_" + lastupdValueMap.get(entry.getKey()) + "_" + "1";
//									System.out.println("Stale Cell: " + exp + " Start Attribute Value :" + startAttrValue);
									
									// key: expected update-cell; value: the start attribute (of update pattern) with its value in the LastUpdate 
									String expFinal = exp + "_" + entry.getValue();
									staleCells.put(expFinal, startAttrValue);
//									System.out.println(expFinal + ";;;" + startAttrValue);
								}
							}
						}

					}
				}					
			}
			
			//- causality update pattern
			if(updpatType.equals("2")){
				for (Map.Entry<String, String> entry : lastupdMap.entrySet()) {
					if(entry.getKey().split("_")[2].equals(updpatStartAttr)){
//						System.out.println(entry.getKey().split("_")[2]);
						
						// timeunit = 0, updated in the same time unit
						if (updpatTimeUnit.equals("0")){
							//based on the update pattern, what is the expected update-cell
							String exp = entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1] + "_" + updpatEndAttr; 
//							System.out.println(exp);
							
							//whether the expected update-cell is updated
							boolean flag1 = lastupdMap.containsKey(exp);
							boolean flag2 = false;
							if (flag1){
//								System.out.println(entry.getValue()+"|"+lastupdMap.get(exp));
//								if (Integer.parseInt(entry.getValue()) == Integer.parseInt(lastupdMap.get(exp))){
//									flag2 = true;
//								}
								if (entry.getValue().compareTo(lastupdMap.get(exp)) == 0){
									flag2 = true;
								}
								
							}
//							System.out.println(flag2);
//							System.out.println(flag);
							
							if (flag2) {
								
								if(!staleCells.containsKey(exp+ "_" + entry.getValue())){								
									// key: expected update-cell; value: the start attribute (of update pattern) with its value in the LastUpdate 
									String expFinal = exp + "_" + entry.getValue();
//									System.out.println(expFinal);
									staleCells.put(expFinal, "nc");
//									System.out.println(expFinal + ";;;" + "nc");
								}
							}

						
						}
						
					}
				}
				
			}
			
		}
		
//		try{
//			br_updpat = new BufferedReader(new FileReader("Files/UpdPatterns"));
//			String lineUpdPat = br_updpat.readLine();
//			
//			while (lineUpdPat != null){
////				System.out.println(lineUpdPat);
//				String updpatID = lineUpdPat.split(",")[0];
//				String updpatStartAttr = lineUpdPat.split(",")[1];
//				String updpatEndAttr = lineUpdPat.split(",")[2];
//				String updpatType = lineUpdPat.split(",")[3];
//				String updpatTimeUnit = lineUpdPat.split(",")[4];
//				
//				//+ causality update pattern
//				if(updpatType.equals("1")){
//					for (Map.Entry<String, String> entry : lastupdMap.entrySet()) {
//						if(entry.getKey().split("_")[2].equals(updpatStartAttr)){	
//							
//							// timeunit = 0, updated in the same time unit
//							if (updpatTimeUnit.equals("0")){
//								//based on the update pattern, what is the expected update-cell
//								String exp = entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1] + "_" + updpatEndAttr; 
//								//whether the expected update-cell is updated
//								boolean flag1 = lastupdMap.containsKey(exp);
//								boolean flag2 = false;
//								
////								System.out.println(entry.getKey()+"|"+entry.getValue()+"|"+exp);
////								System.out.println(flag1);
//								if (flag1){							
//									if (Integer.parseInt(entry.getValue()) > Integer.parseInt(lastupdMap.get(exp))){
//										flag2 = true;
//									}
//									
//								}
////								System.out.println(flag2);
////								System.out.println(flag);
////								System.out.println(lastupdMap.get(exp));
////								System.out.println(entry.getValue());
//								
//								// if the expected update-cell is not updated, or it is updated but is not updated at correct time unit, then it is out-of-date						
//								if ((!flag1) || (flag1 && flag2)){
//									
//									if(!staleCells.containsKey(exp)){
////										System.out.println(entry.getKey());
//										String startAttrValue = updpatStartAttr + "_" + lastupdValueMap.get(entry.getKey()) + "_" + "0";
////										System.out.println("Stale Cell: " + exp + " Start Attribute Value :" + startAttrValue);
//										
//										// key: expected update-cell; value: the start attribute (of update pattern) with its value in the LastUpdate 
//										String expFinal = exp + "_" + entry.getValue();
//										staleCells.put(expFinal, startAttrValue);
////										System.out.println(expFinal + ";;;" + startAttrValue);
//									}
//								}
//							}
//							
//							
//							// timeunit = 1, updated in the same time unit
//							if (updpatTimeUnit.equals("1")){
//								//based on the update pattern, what is the expected update-cell
//								String exp = entry.getKey().split("_")[0] + "_" + entry.getKey().split("_")[1] + "_" + updpatEndAttr; 
//								//whether the expected update-cell is updated
//								boolean flag1 = lastupdMap.containsKey(exp);
//								boolean flag2 = false;
//								
////								System.out.println(entry.getKey()+"|"+entry.getValue()+"|"+exp);
////								System.out.println(flag1);
////								System.out.println(entry.getValue());
////								System.out.println(lastupdMap.get(exp));
//								
//								if (flag1){	
//
//									if (Integer.parseInt(entry.getValue()) > Integer.parseInt(lastupdMap.get(exp)) - 1 ){
//										flag2 = true;
//									}
//									
//								}
//								
////								System.out.println(flag2);
//								
////								System.out.println(flag);
////								System.out.println(lastupdMap.get(exp));
////								System.out.println(entry.getValue());
//								
//								// if the expected update-cell is not updated, or it is updated but is not updated at correct time unit, then it is out-of-date						
//								if ((!flag1) || (flag1 && flag2)){
//									
//									if(!staleCells.containsKey(exp) && !entry.getValue().equals(currentTime)){
////										System.out.println(entry.getKey());
//										String startAttrValue = updpatStartAttr + "_" + lastupdValueMap.get(entry.getKey()) + "_" + "1";
////										System.out.println("Stale Cell: " + exp + " Start Attribute Value :" + startAttrValue);
//										
//										// key: expected update-cell; value: the start attribute (of update pattern) with its value in the LastUpdate 
//										String expFinal = exp + "_" + entry.getValue();
//										staleCells.put(expFinal, startAttrValue);
////										System.out.println(expFinal + ";;;" + startAttrValue);
//									}
//								}
//							}
//
//						}
//					}					
//				}
//			
//				
//				lineUpdPat = br_updpat.readLine();
//			}
//					
//		}catch (Exception e){
//			System.out.println("Failed!");
//		}finally{
//			if (br_updpat != null){
//				try{
//					br_updpat.close();
//					br_updpat = null;
//				}catch (Exception e){
//					e.printStackTrace();
//				}
//			}
//		}
		
		return staleCells;
	}

}