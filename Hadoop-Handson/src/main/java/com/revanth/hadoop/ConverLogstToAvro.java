package com.revanth.hadoop;
import java.io.IOException;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroOutputFormat;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.mapreduce.Job;

import com.revanth.hadoop.avro.WhiteHouseLog;


public class ConverLogstToAvro extends Configured implements Tool {

	public ConverLogstToAvro() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		int res = ToolRunner.run(new Configuration(), new ConverLogstToAvro(),args);
		System.exit(res);

	}
	
	public int run(String[] args) throws Exception {
		Configuration conf =new Configuration();
		
		Job job =new Job(conf, "Avro Logs");

		job.setJarByClass(ConverLogstToAvro.class);
		job.setJobName("Avro Logs");
		job.setMapperClass(MapClass.class);

		
	        AvroJob.setOutputKeySchema(job, WhiteHouseLog.SCHEMA$);	
                AvroJob.setMapOutputKeySchema(job, WhiteHouseLog.SCHEMA$);	
		job.setNumReduceTasks(0);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(AvroKeyOutputFormat.class);

                FileInputFormat.setInputPaths(job, new Path(args[1])); 
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
                
		job.waitForCompletion(true);
		return 0;
	}
	
	public static class MapClass extends Mapper<LongWritable, Text, AvroKey<WhiteHouseLog>,NullWritable> {

		@Override
		public void map(LongWritable key, Text value, Mapper.Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String toks[] = line.split(" ");
			
			WhiteHouseLog log = new WhiteHouseLog(toks[1],toks[0]);
                        
                        context.write(new AvroKey<WhiteHouseLog>(log), NullWritable.get());
		}
	}
	
}
