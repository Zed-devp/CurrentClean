import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StaleCellsProb {

	public Map<String, String> StaleCellsProbability(Map<String, String> staleCells, double beta) {
		
		Map<String, String> staleCellsProb = new HashMap<String, String> ();
		Random ra =new Random();
		DecimalFormat df = new DecimalFormat("0.00");
		
		// TODO Auto-generated method stub
		for (Map.Entry<String, String> entry : staleCells.entrySet()) {
			double probability = ra.nextDouble() * (0.95-beta)+beta;
			staleCellsProb.put(entry.getKey(), df.format(probability));
		}
		
		return staleCellsProb;
	}
	
}