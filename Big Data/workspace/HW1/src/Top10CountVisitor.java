import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
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



public class Top10CountVisitor {


	public Top10CountVisitor() {
		// Default constructor
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(Top10CountVisitor.class); // Create a JobConf object
		conf.setJobName("WordCount"); // job name

		conf.setOutputKeyClass(Text.class); // see output.collect in map --> this and the following line are reduce input pair so reduce should have Text-IntWritable pair as input
		conf.setOutputValueClass(IntWritable.class); // see output.collect

		conf.setMapperClass(Map.class); // specifies the mapper class
		conf.setCombinerClass(Reduce.class); // specifies the combiner
		conf.setReducerClass(Reduce.class); // specifies the reducer

		conf.setInputFormat(TextInputFormat.class); // input type is text, key is line no., value is the line words
		conf.setOutputFormat(TextOutputFormat.class); // output type is text

		FileInputFormat.setInputPaths(conf, new Path(args[0])); // specifies the Input directory /home/<anyname_or_netid>/input
		FileOutputFormat.setOutputPath(conf, new Path(args[1])); // specifies the Output directory /home/<anyname_or_netid>/output

		JobClient.runJob(conf); // calling the JobClient.runJob to submit the job and monitor progress

		//if(((RunningJob) conf).isSuccessful())
		{

			Configuration confJob = new Configuration();
			org.apache.hadoop.mapreduce.Job job = new org.apache.hadoop.mapreduce.Job(confJob,"Top10Visitor");

			job.setJarByClass(Top10CountVisitor.class);
			job.setMapperClass(Top10Mapper.class);
			job.setReducerClass(Top10Reducer.class);
			job.setOutputKeyClass(NullWritable.class);
			job.setOutputValueClass(Text.class);
			job.setNumReduceTasks(1);

			job.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.TextInputFormat.class);
			job.setOutputFormatClass(org.apache.hadoop.mapreduce.lib.output.TextOutputFormat.class);
			org.apache.hadoop.mapreduce.lib.input.FileInputFormat.setInputPaths(job, new Path(args[1]));
			org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.setOutputPath(job, new Path(args[2]));
			job.waitForCompletion(true);
		}

	}




	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {


		Map(){
			//Default Constructor
		}

		public void map(LongWritable key, Text value,OutputCollector<Text, IntWritable> collector, Reporter reporter ) throws IOException {
			String line = value.toString();
			String[] tokens = line.split(","); 
			String visitorName = "";
			IntWritable one = new IntWritable(1);

			String[] names = {"",""};

			if(tokens.length>0){
				names[0] = tokens[0];
			}


			if(tokens.length>1){
				names[1] = tokens[1];
			}


			visitorName = names[0] + "-" + names[1];

			Text word = new Text();
			word.set(visitorName);

			collector.collect(word, one); 


		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text,IntWritable>{


		Reduce(){
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