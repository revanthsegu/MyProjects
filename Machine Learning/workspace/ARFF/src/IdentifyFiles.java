import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class IdentifyFiles {

	ArrayList<String> dirNames = new ArrayList<String>();
	File[] files;
	File[] inner_files;
	int totalFiles=0;
	int dirCount=0;
	int copiedFiles=0;
	int cutoffDIRCount=75;
	public static void main(String args[]){


		long startTime = System.currentTimeMillis();
		IdentifyFiles maincalss = new IdentifyFiles();;
		maincalss.identifyFiles(args);

		/*try{
		Runtime.getRuntime().exec("cmd /c copy won.jpg won_5.jpg" );
		}catch(IOException exp){
			exp.printStackTrace();
		}*/


		if(args.length<2){
			System.out.println("Please enter all arguments and execute the program again");
			return;
		}

		long endTime = System.currentTimeMillis();

		System.out.println("Scanned total of " + maincalss.totalFiles);
		System.out.println("Copied total of " + maincalss.copiedFiles);
		System.out.println("\nTotal time taken to execute the program in ms : " +(endTime - startTime) );
	}

	public void identifyFiles(String args[]){
		try{

			File dir = new File(args[0]);

			if(dir.isDirectory()){
				files = dir.listFiles();
				for(File file: files){

					if(dirCount==cutoffDIRCount)
						break;

					if(file.isDirectory()){
						dirNames.add(file.getName());	
					}
					dirCount++;
				}
			}

			dir = new File(args[0]);
			dirCount=0;

			if(dir.isDirectory()){
				files = dir.listFiles();
				for(File file: files){

					if(file.isDirectory() ){
						try
						{

							if(dirCount==cutoffDIRCount)
								break;
							//System.out.println("new dirpath is " + args[1]+"/"+dirNames.get(i));

							System.out.println("Creating directory " +  args[1]+"/"+file.getName());
							File newDir = new File(args[1]+"/"+file.getName());
							boolean success = false;
							success = newDir.mkdir();
							// if(success){
							//	 System.out.println("Created dir "+newDir.getName());
							//}


							inner_files = file.listFiles();

							for(File tempFile: inner_files){

								if(tempFile.isDirectory() && (tempFile.getName().equalsIgnoreCase("_sent_mail") || tempFile.getName().equalsIgnoreCase("sent") || tempFile.getName().equalsIgnoreCase("sent_items") )){
									//if(tempFile.isDirectory() && (tempFile.getName().equalsIgnoreCase("inbox"))){
									System.out.println("Coying files");

									File [] final_files = tempFile.listFiles();
									for(File final_file :final_files ){

										totalFiles++;

										if(isRelevantFile(final_file)){
											//System.out.println("Copying file " + final_file.getAbsoluteFile()+ " to" + newDir.getAbsolutePath()+"/"+final_file.getName() );
											copiedFiles++;
											Runtime.getRuntime().exec("cmd /c copy "+final_file.getAbsoluteFile() +" " +newDir.getAbsolutePath() );
										}

									}
								}
							}


						} catch (SecurityException Se) {
							System.out.println("Error while creating directory in Java:" + Se);
						}

						dirCount++;
					}

				}

			}else{
				System.out.println("input path is not a valid directory");
			}


			/*dir = new File(args[1]);

			if(dir.isDirectory()){
				for(int i=0;i<dirNames.size();i++){
					try
					{

						//System.out.println("new dirpath is " + args[1]+"/"+dirNames.get(i));

						File newDir = new File(args[1]+"/"+dirNames.get(i));
						boolean success = false;
						 success = newDir.mkdir();
//						 if(success){
//							 System.out.println("Created dir "+dirNames.get(i));
//						 }

					} catch (SecurityException Se) {
						System.out.println("Error while creating directory in Java:" + Se);
					}

				}

			}else{
				System.out.println("output path is not a valid directory");
			}*/


		}catch(IOException e){
			System.out.println("Not able to open Directory path: "+args[0]);
			e.printStackTrace();
		}


	}


	public boolean isRelevantFile(File file){
		boolean ret = false;
		Scanner reader;
		String backUp="";
		try{

			String line;

			reader = new Scanner(file);
			//HashMap<String,Integer> fileVector = new HashMap<String,Integer>();
			while(reader.hasNextLine()) {
				line = reader.nextLine();
				//if(line.startsWith("To:")){
				if(line.startsWith("From:")){	
					backUp = line;
					//System.out.println("line is:"+ line);

					if(line.split(" ").length>1){
						line = line.split(" ")[1];
					}else{
						break;
					}
					//System.out.println("line is-" +line+"-");
					//line = line.split("@")[0];
					//System.out.println("line after split @ is " + line );
					//System.out.println("line after replace of . is " + line );
					if(line.indexOf('.') >0){
						line = line.substring(0,line.indexOf('.'));
					}else{
						break;
					}
					//System.out.println("got the name finally:" + line );

					for(int i=0;i<dirNames.size();i++){
						if(  line.length()>2 &&   dirNames.get(i).toLowerCase().startsWith(line.toLowerCase()) ){
							ret = true;
							break;
						}
					}

					break;
				}

			}

		}catch(IOException exp){
			exp.printStackTrace();
		}


		/*if(ret)
			System.out.println("returning true for the line :" + backUp);*/

		return ret;
	}


}
