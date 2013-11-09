import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ReduceSideJoin {

	public static class CustomPartitioner extends Partitioner<TextPair, TextPair> {

		@Override
		public int getPartition(TextPair key, TextPair value, int partitionCount) {
			return ( key.getFirstStr(). hashCode () & Integer . MAX_VALUE )
					% partitionCount ; 
		}		

	}
	public static class CustomGroupingComparator extends WritableComparator{

		protected CustomGroupingComparator(){
			super((Class<? extends WritableComparable<TextPair>>) TextPair.class, true);
		}
		@SuppressWarnings("rawtypes")
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			TextPair ip1=(TextPair)w1;
			TextPair ip2=(TextPair)w2;
			String str1=ip1.getFirstStr();
			String str2=ip2.getFirstStr();
			return str1.compareTo(str2);			
		}

	}
	public static class CompositeKeyComparator extends WritableComparator {

		protected CompositeKeyComparator() {
			super((Class<? extends WritableComparable<TextPair>>) TextPair.class, true);
		}
		@SuppressWarnings("rawtypes")
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			TextPair ip1=(TextPair)w1;
			TextPair ip2=(TextPair)w2;

			int cmp = ip1.getFirstStr().compareTo(ip2.getFirstStr());

			return cmp; 
		}

	}

	public static class JoinMapper extends  Mapper<Object, Text , TextPair, TextPair >{


		public void map(Object key,Text value,Context context) throws IOException,InterruptedException {

			String[] temp=value.toString().split("[ \t]",2);
			String joinKey=temp[0].trim();
			String tag="R";
			if(temp[1].trim().length()==2){	
				tag="S"; 
			}

			TextPair keyPair=	new TextPair(joinKey, tag);
			TextPair valuePair=new TextPair(temp[1], tag);
			context.write(keyPair,valuePair);

		}


	}
	public static class JoinReducer extends Reducer<TextPair, TextPair, Text, Text>{



		public void reduce(TextPair key,Iterable<TextPair> values,Context context)throws IOException,InterruptedException{

			String tag = key.getSecondStr();
			ArrayList <String> T1 = new ArrayList <String>();
			for( TextPair value:values)
			{
				if(value.getSecondStr().equalsIgnoreCase(tag))
				{	
					T1.add (value.getFirstStr());
				}
				else
				{	
					for( String val : T1 )
					{
						if(val.length()==2){
							context.write( new Text(val),new Text(value.getFirstStr()));
						}else{
							context.write(new Text(value.getFirstStr()), new Text(val));
						}
					}
				}
			}

		} 

	}


	public static void main(String[] args) {

		Configuration conf =new Configuration();
		Job job=null;
		try {
			FileSystem fs=FileSystem.get(conf);
			fs.delete(new Path(args[1]), true);
			job =new Job(conf, "Reduce Side Join");	

		} catch (IOException e) {
			e.printStackTrace();
		}

		job.setJarByClass(ReduceSideJoin.class);

		job.setMapperClass(JoinMapper.class);
		job.setReducerClass(JoinReducer.class);

		job.setMapOutputKeyClass(TextPair.class);
		job.setMapOutputValueClass(TextPair.class);

		job.setPartitionerClass(CustomPartitioner.class);

		job.setGroupingComparatorClass(CustomGroupingComparator.class);
		job.setSortComparatorClass(CompositeKeyComparator.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(1);


		try {
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}


		try {
			System.exit(job.waitForCompletion(true) ? 0 : 1);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}

}
