import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class UpdPattern {
	
	public ArrayList<String> UpdPatPreprocess (double beta) {
		BufferedReader br_updpat = null;
		BufferedReader br_probabilistic = null;
		ArrayList <String> updpat = new ArrayList<String>();
		int attrcount = 0;
		
		try{
			br_probabilistic = new BufferedReader(new FileReader("Input/probabilistic.tsv"));
			String lineAttr = br_probabilistic.readLine();			
			while (lineAttr != null) {
				attrcount ++;
				lineAttr = br_probabilistic.readLine();
			}
			
		}catch (Exception e){
			System.out.println("Failed!");
		}finally{
			if (br_probabilistic != null){
				try{
					br_probabilistic.close();
					br_probabilistic = null;
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		
		try{
			br_updpat = new BufferedReader(new FileReader("Input/UpdPatterns"));
			String lineUpdPat = br_updpat.readLine();
			
			int upperbound = (int) Math.round(9*(attrcount - 1)*(1-beta)/2);
			int linecount = 0;
//			System.out.println(upperbound);

			
			while (lineUpdPat != null && linecount < upperbound){
				linecount ++;
//				System.out.println(lineUpdPat);
//				String updpatID = lineUpdPat.split(",")[0];
				String updpatStartAttr = lineUpdPat.split(",")[1];
				String updpatEndAttr = lineUpdPat.split(",")[2];
				String updpatType = lineUpdPat.split(",")[3];
				String updpatTimeUnit = lineUpdPat.split(",")[4];
				
				//- causality
				if(updpatType.equals("2")) {
					updpat.add(updpatStartAttr+","+updpatEndAttr+","+updpatType+","+updpatTimeUnit);
				}
				//+ causality
				if(updpatType.equals("1")) {
					if(!updpat.contains(updpatStartAttr+","+updpatEndAttr+","+updpatType+","+updpatTimeUnit)){
						updpat.add(updpatStartAttr+","+updpatEndAttr+","+updpatType+","+updpatTimeUnit);
					}
				}
				//cooccurrence
				if(updpatType.equals("0")){
					String s1 = updpatStartAttr+","+updpatEndAttr+","+"1,0";
					String s2 = updpatEndAttr+","+updpatStartAttr+","+"1,0";
					if(!updpat.contains(s1)){
						updpat.add(s1);
					}
					if(!updpat.contains(s2)){
						updpat.add(s2);
					}
				}
				
				lineUpdPat = br_updpat.readLine();
				
			}
				
			
		}catch (Exception e){
			System.out.println("Failed!");
		}finally{
			if (br_updpat != null){
				try{
					br_updpat.close();
					br_updpat = null;
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
				
		return updpat;
		
	}

}