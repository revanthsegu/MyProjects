import java.io.IOException;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;



public class Top10Mapper extends  Mapper<LongWritable, Text, NullWritable, Text> {

	public Top10Mapper() {
		// default constructor 
	}

	private TreeMap<Integer, Text> repToRecordMap = new TreeMap<Integer, Text>();
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		String line = value.toString();
		StringTokenizer tokenizer = new StringTokenizer(line, "\t");
		String visitorName = "NullName";
		int visitorCount = 0;
		if (tokenizer.hasMoreTokens()) {
			visitorName = tokenizer.nextToken();
		}
		String secondVal = "";
		if (tokenizer.hasMoreTokens()) {
			secondVal = tokenizer.nextToken();
			try{
				visitorCount = Integer.parseInt(secondVal);
			}catch(NumberFormatException exp){
				visitorName = visitorName + " " + secondVal;
				if(tokenizer.hasMoreTokens()){
					visitorCount = Integer.parseInt(tokenizer.nextToken());
				}
			}
		}

		// Add this record to our map with the reputation as the key
		repToRecordMap.put(visitorCount, new Text(visitorCount + "\t" + visitorName));
		// If we have more than ten records, remove the one with the lowest rep
		// As this tree map is sorted in descending order, the user with
		// the lowest reputation is the last key.
		if (repToRecordMap.size() > 10) {
			repToRecordMap.remove(repToRecordMap.firstKey());
		}
	}
	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		// Output our ten records to the reducers with a null key
		for (Text t : repToRecordMap.values()) {
			context.write(NullWritable.get(), t);
		}
	}
}