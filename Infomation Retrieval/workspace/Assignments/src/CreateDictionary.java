import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class CreateDictionary {

	//Program Configuration variables
	int TOP_COUNT=30;
	int BUFFER_COUNT = 0;
	int QUIT_AFTER = 5000;
	//Program Configuration variables
	long totalTokens=0;
	long oneFreqTokens=0;
	HashMap<String,Long> tokemMap = new HashMap<String,Long>(); // to store tokens and frequency of token 
	TokenFrequency[] mostFreq;

	public static void main(String[] args){

		CreateDictionary mainClass= new CreateDictionary();

		long startTime = System.currentTimeMillis();
		mainClass.createDictionary(args);
		long endTime = System.currentTimeMillis();

		System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );

	}



	public void createDictionary(String[] args){
		String dirPath = "";
		File[] files;
		int noFiles=0;

		System.out.println("Exection Started !!");
		long startScanTime = System.currentTimeMillis();

		//program Takes first argument to main function as dir path for Cranfield collection 
		//if there is no argument it assumes current directory as dirpath of Cranfield collection

		if(args.length >0 && null!=args[0] && args[0].length() > 2){
			dirPath = args[0];
		}else{
			dirPath = System.getProperty("user.dir");
		}


		if(args.length >1 && null!=args[1]){
			try{
				int topcount = Integer.parseInt(args[1]);
				TOP_COUNT=topcount;
			}catch(NumberFormatException exp){
				System.out.println("Error:Could not convert "+args[1] + " into an Integer");
				//exp.printStackTrace();
			}
		}

		System.out.println("Looking for Cranfield collection in:\n"+dirPath+"\n");


		File dir = new File(dirPath);

		//checking directory
		if(dir.isDirectory()){
			files = dir.listFiles();

			//processing files
			for(File file: files){

				if( file!=null && file.isFile() && file.getName().toLowerCase().startsWith("cranfield")){

					processFile(file);
					noFiles++;
				}else{
					System.out.println("Skipping file:"+file.getName());
				}





				//if(noFiles==QUIT_AFTER) break;

				if(noFiles>0 && noFiles%100==0){
					System.out.println("Processed "+noFiles+ " files.");
				}

			}
			System.out.println("Processing files from database completed!!\nTotal number of files processed:"+noFiles);

			long endScanTime = System.currentTimeMillis();

			// initializing all values of most freq array to zeros 
			mostFreq = new TokenFrequency[TOP_COUNT+BUFFER_COUNT];
			for(int i=0; i <mostFreq.length;i++){
				mostFreq[i] = new TokenFrequency("", 0);
			}

			//System.out.println("initialised mostFreq array to zeros");
			//printMostFreq();


			scanTokenMap();
			long endSortTime = System.currentTimeMillis();

			System.out.println("Total time taken to scan all token from database in ms : " +(endScanTime - startScanTime) );
			System.out.println("Total time taken for Scanning Token map for unique words and words with top "+TOP_COUNT+" frequncy in ms : " +(endSortTime - endScanTime) );
			//printTokenMap();

			System.out.println("The number of tokens in the database: " + totalTokens);
			System.out.println("The number of unique words in the database are: " + tokemMap.size());
			System.out.println("The number of words that occur only once in the database are: "+oneFreqTokens);
			float avgtokens = (Float.valueOf(totalTokens)/Float.valueOf(noFiles));
			System.out.println("The average number of word tokens per document are: "+ avgtokens);
			printMostFreq();


		}else{
			System.out.println(dirPath+" is not a valid directory path.");
		}



	}


	public void processFile (File file){
		//System.out.println("Processing file:"+file.getName());
		Scanner reader;
		long fileTokens=0;

		try {
			reader = new Scanner(file);
			String line;
			while(reader.hasNextLine()) {
				line = reader.nextLine();

				if(line!=null &&line.length()>0 ){

					line = line.trim();
					line = line.toLowerCase();
					line = removeSGMLTags(line);
					line = removePossessives(line);
					line = removeCommas(line);


					if(null!=line && line.length()>0){

						String toekns[] = line.split(" ");

						for(String token : toekns ){

							if(token.endsWith(".")){
								token = token.substring(0, token.length()-1);

							}

							if(null!=token && token.length()>0){
								//checking if map contains token
								if(tokemMap.containsKey(token)){
									Long freq = tokemMap.get(token);
									tokemMap.put(token, freq+1);

								}else{
									tokemMap.put(token, new Long(1));
								}

								fileTokens++;
							}

						}
					}

				}
			}
		}catch(FileNotFoundException exp){
			System.out.println("Not able to open file "+file.getName());
			exp.printStackTrace();
		}

		totalTokens += fileTokens;
		return;

	}

	public String removeSGMLTags(String line){

		//System.out.println("Before removing SGML tags:"+line);

		while( line.length() >0 && line.indexOf('<') > -1 ){
			line = line.substring(0,line.indexOf('<')) + line.substring(line.indexOf('>')+1);
			//System.out.println("Removing SGML tags:" + line);
		}

		//System.out.println("After removing SGML tags:"+line);
		return line;
	}


	public String removePossessives(String line){

		line=line.replaceAll("'s", "");

		return line;
	}


	public String removeCommas(String line){
		//System.out.println("String before replacing commas: "+line);
		line=line.replaceAll(",", " ");
		//System.out.println("String after replacing commas: "+line);

		return line;
	}

	public void printTokenMap(){
		System.out.println("Printing Tokens and frequencies");

		int i=1;
		Set<String> keySet = tokemMap.keySet();
		String key;
		Iterator<String> itr = keySet.iterator();
		while(itr.hasNext()){
			key = itr.next();
			System.out.println(i+":"+ key+":"+tokemMap.get(key));
			i++;
		}
		System.out.println("\n");
		return;
	}

	public void scanTokenMap(){


		System.out.println("Scanning Token map for unique words and words with top "+TOP_COUNT+" frequncy");

		Set<String> tokens = tokemMap.keySet();
		String token;
		long freq;
		Iterator<String> itr = tokens.iterator();
		while(itr.hasNext()){
			token = itr.next();
			freq = tokemMap.get(token);

			if(freq==1){
				oneFreqTokens++;
			}

			checkForMostFreq(token,freq);

		}
		return;
	}


	public void checkForMostFreq(String token,long freq){

		if(freq <=mostFreq[mostFreq.length-1].getFrequency()){
			return;
		}


		int index = check(0,mostFreq.length-1,freq);
		if(index==-1){
			System.out.println("Failed to find position of freq " + freq + "in Most Fre Map ");
			printMostFreq();

			System.exit(0);

		}

		for(int i = mostFreq.length-1;i	> index;i-- ){
			mostFreq[i] = mostFreq[i-1];
		}
		mostFreq[index] = new TokenFrequency(token, freq);
	}

	public int check(int begInd, int endInd,long freq){

		int retVal=-1;

		int midIndex = begInd + (endInd-begInd)/2;

		TokenFrequency tokFreq =  mostFreq[midIndex];

		if (tokFreq.getFrequency() == freq ){
			return midIndex;

		}else if(endInd==begInd){

			return midIndex;

		}else if(tokFreq.getFrequency() < freq){
			return check(begInd,midIndex,freq);

		}else if(tokFreq.getFrequency() > freq){
			return check(midIndex+1,endInd,freq);
		}

		return retVal;
	}


	public void printMostFreq(){

		System.out.println("The frequencies of the "+TOP_COUNT+" most frequent words in the database are: ");
		System.out.println("Rank:Word:frequency");
		TokenFrequency tokFreq;

		for(int i=0;i<mostFreq.length;i++){

			tokFreq = mostFreq[i];

			if(null!=tokFreq)
			{
				if(tokFreq.getFrequency() >= mostFreq[TOP_COUNT-1].getFrequency()){
					System.out.println((i+1)+ ": "+tokFreq.getToken() + " : " + tokFreq.getFrequency());
				}	
			}else{
				System.out.println((i+1)+": null : null");
			}
		}

		System.out.println("\n");

	}

}
