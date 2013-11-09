/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.json.JSONObject;


public class Test {

	Test(){};
	public static void main(String[] args) throws IOException {

		JobConf conf = new JobConf(Test.class); // Create a JobConf object
		conf.setJobName("wordcount"); // job name

		conf.setOutputKeyClass(Text.class); // see output.collect in map --> this and the following line are reduce input pair so reduce should have Text-IntWritable pair as input
		conf.setOutputValueClass(Text.class); // see output.collect

		conf.setMapperClass(Map.class); // specifies the mapper class

		conf.setInputFormat(TextInputFormat.class); // input type is text, key is line no., value is the line words
		conf.setOutputFormat(TextOutputFormat.class); // output type is text

		FileInputFormat.setInputPaths(conf, new Path(args[0])); // specifies the Input directory /home/<anyname_or_netid>/input
		FileOutputFormat.setOutputPath(conf, new Path(args[1])); // specifies the Output directory /home/<anyname_or_netid>/output


		JobClient.runJob(conf); // calling the JobClient.runJob to submit the job and monitor progress
		// if (conf.isSuccessful())
		//**************************************************************************************************************************
		/*		System.out.println("Hello from Tarfile program");
		String tarFile = "E:/examp.tar";
		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)));
		   TarEntry entry;
		   try {
			while((entry = tis.getNextEntry()) != null) {
			      int count;
			      byte data[] = new byte[2048];

			      try {
					while((count = tis.read(data)) != -1) {
						System.out.println("DATA: " + data);
					  }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			      }
			tis.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		 */	
/*
		try
		{
			@SuppressWarnings("resource")
			Scanner input= new Scanner (new File("E:/trial.json"));
			while(input.hasNextLine())
			{
				String jsontext= input.nextLine();
				JSONObject obj=new JSONObject(jsontext);
				String text=obj.getString("text");
				int idVal = obj.getInt("id");
				System.out.println(idVal+" "+text);

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}  	*/
	}   


	// Mapper implementation --------- processes one line at a time
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		Map(){
		}
		// map method
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			
			String msg = "";
			String idVal = "";
	//		calledClass obj = new calledClass();
			try
			{
					String jsontext= value.toString();
					JSONObject obj=new JSONObject(jsontext);
					 msg=obj.getString("text");
					 idVal = obj.getString("id");
					//msg=obj.getstringvalue(jsontext);
					//idVal = obj.getintvalue(jsontext);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}  	
				if(msg.length()>0 && idVal.length()>0)
				output.collect(new Text(idVal),new Text(msg));

		}
	}
}
