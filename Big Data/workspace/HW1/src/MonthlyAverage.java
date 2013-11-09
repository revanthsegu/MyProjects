import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;



public class MonthlyAverage {


	public MonthlyAverage() {
		// Default constructor
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(MonthlyAverage.class); // Create a JobConf object
		conf.setJobName("WordCount"); // job name

		conf.setOutputKeyClass(Text.class); // see output.collect in map --> this and the following line are reduce input pair so reduce should have Text-IntWritable pair as input
		conf.setOutputValueClass(IntWritable.class); // see output.collect

		conf.setMapperClass(Map.class); // specifies the mapper class
		conf.setReducerClass(Reduce.class); // specifies the reducer


		conf.setInputFormat(TextInputFormat.class); // input type is text, key is line no., value is the line words
		conf.setOutputFormat(TextOutputFormat.class); // output type is text

		FileInputFormat.setInputPaths(conf, new Path(args[0])); // specifies the Input directory /home/<anyname_or_netid>/input
		FileOutputFormat.setOutputPath(conf, new Path(args[1])); // specifies the Output directory /home/<anyname_or_netid>/output

		JobClient.runJob(conf); // calling the JobClient.runJob to submit the job and monitor progress

	}




	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {


		Map(){
			//Default Constructor
		}

		public void map(LongWritable key, Text value,OutputCollector<Text, IntWritable> collector, Reporter reporter ) throws IOException {
			String line = value.toString();
			String[] tokens = line.split(","); 
			String month;

			int monthNo=1;
			int yearNo=2010;

			if(tokens.length>11){
				try{

					String[] intoks = tokens[11].split("/");

					if(intoks.length>0)
						monthNo =Integer.parseInt(intoks[0].trim()); 
					yearNo =Integer.parseInt(intoks[2].trim().split(" ")[0]);


					month =  new DateFormatSymbols().getMonths()[monthNo-1];

					Text word = new Text();
					word.set(month);

					collector.collect(word, new IntWritable(yearNo)); 
				}catch(NumberFormatException exp){
					//throw new IOException(exp);
					//return;
				}

			}

		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text,DoubleWritable>{


		Reduce(){
			//Default Constructor
		}

		Set<Integer> years;// = new HashSet<Integer>();

		@Override
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, DoubleWritable> output, Reporter reporter)
						throws IOException {
			years = new HashSet<Integer>();
			Double sum=0d;
			while(values.hasNext()){
				years.add(values.next().get());
				sum++;
			}

			output.collect(key, new DoubleWritable((sum/years.size())));
		}

	}

	public static class Combine extends MapReduceBase implements Reducer<Text, IntWritable, Text,IntWritable>{


		Combine(){
			//Default Constructor
		}

		@Override
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
						throws IOException {

			int sum=0;
			while(values.hasNext()){
				sum+= values.next().get();
			}

			output.collect(key, new IntWritable(sum));
		}

	}


}