import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class CountFiles {
	int totalFileNo=0;
	String trainPath = "";
	File[] files;
	File[] inner_files;

	public static void main(String args[]){

		long startTime = System.currentTimeMillis();
		CountFiles maincalss;

		if(args.length<1){
			System.out.println("Please enter all arguments and execute the program again");
			return;
		}

		System.out.println("Executing with smoothing feature");
		maincalss = new CountFiles();
		maincalss.countFiles(args);


		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time taken to execute the program in ms : " +(endTime - startTime) );
	}




	void countFiles(String args[]){
		Scanner reader;
		//System.out.println("Exection Started !!");
		long startScanTime = System.currentTimeMillis();

		//program Takes first argument to main function as dir path for training data
		//if there is no argument it assumes current directory as path for training data

		if(args.length >0 && null!=args[0] && args[0].length() > 2){
			trainPath = args[0];
		}else{
			System.out.println("Please enter all arguments and execute the program again");
			return;
		}


		//System.out.println("Looking for Training data in: "+trainPath);


		File dir = new File(trainPath);

		//checking directory
		if(dir.isDirectory()){
			files = dir.listFiles();

			//processing files
			for(File file: files){

				if( file!=null && file.isDirectory()){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							totalFileNo++;
						}

						//						if(hamFileNo>0 && hamFileNo%100==0){
						//							System.out.println("Processed "+hamFileNo+ " Ham files.");
						//						}
					}

					//System.out.println("Processing Ham files completed. Total number of files processed: "+hamFileNo);
				}



			}


			System.out.println("Counting files completed. Total number of files are: "+totalFileNo);


			long endScanTime = System.currentTimeMillis();

			System.out.println("Total time taken to Count all files in ms : " +(endScanTime - startScanTime) );


		}else{
			System.out.println(trainPath+" is not a valid directory path.");
		}




		//		System.out.println("Spam tokens: " + spamTokens);
		//		System.out.println("Ham tokens: " + hamTokens);
		//		System.out.println("Total tokens: " + allTokens);
		//		System.out.println("Total Vocab size : " + allTokemMap.size());


		//System.out.println("Looking for Test files in: "+testPath);

	}


}
