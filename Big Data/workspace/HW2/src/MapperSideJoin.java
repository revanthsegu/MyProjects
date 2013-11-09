
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class MapperSideJoin extends Configured implements Tool {

	public static class MapClass extends MapReduceBase implements Mapper<Text, Text, Text, Text> {

		private Hashtable<String, String> joinData = new Hashtable<String, String>();

		@Override
		public void configure(JobConf conf) {
			try {


				Path [] cacheFiles = DistributedCache.getLocalCacheFiles(conf);
				System.out.println("ds"+DistributedCache.getLocalCacheFiles(conf));
				if (cacheFiles != null && cacheFiles.length > 0) {
					String line;
					String[] tokens;
					BufferedReader joinReader = new BufferedReader(new FileReader(cacheFiles[0].toString()));

					try {
						while ((line = joinReader.readLine()) != null) {
							tokens = line.split("\t", 2);
							joinData.put(tokens[0], tokens[1]);
						}
					} finally {
						joinReader.close();
					}
				}
				else
					System.out.println("joinreader not set" );
			} catch(IOException e) {
				System.err.println("Exception reading DistributedCache: " + e);
			}
		}

		public void map(Text key, Text value, OutputCollector<Text, Text> output,  Reporter reporter) throws IOException {
			String line = key.toString();
			if(line.indexOf(' ') !=-1)
			{
				String ip = line.substring(0,line.indexOf(' '));
				String joinValue = joinData.get(ip.trim());
				if (joinValue != null && joinValue.length()>0) {
					output.collect(new Text(joinValue),new Text(line.substring(line.indexOf('-'))));
				}

			}
		}
	}


	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, MapperSideJoin.class);

		DistributedCache.addCacheFile(new Path(args[0]).toUri(), job); 
		//System.out.println( DistributedCache.addCacheFile(new Path(args[0]).toUri(), conf));
		// FileInputFormat.setInputPaths(job, in);
		// FileOutputFormat.setOutputPath(job, out);
		FileInputFormat.setInputPaths(job, new Path(args[1])); 
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		job.setJobName("DataJoin with DistributedCache");
		job.setMapperClass(MapClass.class);
		job.setNumReduceTasks(0);
		//job.setInputFormat(TextInputFormat.class); // input type is text, key is line no., value is the line words
		//job.setOutputFormat(TextOutputFormat.class); // output type is text

		job.setInputFormat( KeyValueTextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);
		// job.set("key.value.separator.in.input.line", ",");

		JobClient.runJob(job);
		return 0;
	}

	public static void main(String[] args) throws Exception {

		int res = ToolRunner.run(new Configuration(), new MapperSideJoin(),args);
		System.exit(res);

	}
}
