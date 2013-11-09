import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class ConvertToARFFnoDup {
	int totalFileNo=0;
	int spamFileNo=0;
	int hamFileNo=0;
	String trainPath = "";
	String outFileName = "";
	String testPath = "";
	File[] files;
	File[] inner_files;

	long spamTokens=0;
	long hamTokens=0;
	long allTokens=0;
	long testSpam=0;
	long testHam=0;
	Object[] keys;
	ArrayList<Integer> spamList = new ArrayList<Integer>();
	int [][] filePostings; 
	Set<String> bkp_keySet;

	String stopWordsPath = "";

	ArrayList<String> StopWordList = new ArrayList<String>();
	ArrayList<String> execWordList = new ArrayList<String>();
	HashMap<String,Long> spamTokemMap = new HashMap<String,Long>(); // to store tokens and frequency of token 
	HashMap<String,Long> hamTokemMap = new HashMap<String,Long>(); // to store tokens and frequency of token 
	HashMap<String,TokenFrequency> allTokemMap = new HashMap<String,TokenFrequency>(); // to store tokens and frequency of token 

	public static void main(String args[]){

		long startTime = System.currentTimeMillis();
		ConvertToARFF maincalss;

		if(args.length<3){
			System.out.println("Please enter all arguments and execute the program again");
			return;
		}

		if(args.length>2){
			System.out.println("Executing with smoothing feature");
			maincalss = new ConvertToARFF();
			maincalss.buildARFFConvertor(args, false,false);

		}

		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time taken to execute the program in ms : " +(endTime - startTime) );
	}



	ConvertToARFFnoDup(){
		execWordList.add("subject ");
		execWordList.add("subject :");
		execWordList.add("re :");
		execWordList.add("re ");
		execWordList.add("original message");
		execWordList.add("forwarded by");
		execWordList.add(",");
		execWordList.add("#");
		execWordList.add("!");
		execWordList.add("/");
		//execWordList.add("\\");
		execWordList.add("@");
		execWordList.add(";");
		execWordList.add(":");
		execWordList.add("-");
		execWordList.add("_");
		//execWordList.add("?");
		//execWordList.add("(");
		//execWordList.add(")");
		execWordList.add("$");
		execWordList.add("\"");
		execWordList.add("'s");
		execWordList.add("\'");
		execWordList.add("%");
		execWordList.add("^");
		//execWordList.add("&");
		//execWordList.add("*");
		//execWordList.add("{");
		execWordList.add("}");
		execWordList.add("~");
	}



	void buildARFFConvertor(String args[],boolean stopWords,boolean smoothing){
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

		if(args.length >1 && null!=args[1] && args[1].length() > 2){
			stopWordsPath = args[1];
		}else{
			stopWordsPath = System.getProperty("user.dir")+"//stopwords";
		}


		if(args.length >2 && null!=args[2] && args[2].length() > 2){
			outFileName = args[2];
		}else{
			outFileName = System.getProperty("user.dir")+"//outputTrain.arff";
		}


		if(stopWords){
			//reading stop words
			try{
				//System.out.println("Looking for Stopwords file in the path: "+stopWordsPath);
				File stopWordsFile = new File(stopWordsPath);
				reader = new Scanner(stopWordsFile);
				String stopWord;
				while(reader.hasNextLine()) {
					stopWord = reader.nextLine();
					StopWordList.add(stopWord);
				}

				//System.out.println("Processed Stop Words");
				//System.out.println("Stop Words are: "+StopWordList);

			}catch(FileNotFoundException e){
				System.out.println("Not able to open stopword file in the path: "+stopWordsPath);
				e.printStackTrace();
			}
		}		

		//System.out.println("Looking for Training data in: "+trainPath);


		File dir = new File(trainPath);

		//checking directory
		if(dir.isDirectory()){
			files = dir.listFiles();

			//processing files
			for(File file: files){

				if( file!=null && file.isDirectory() && file.getName().equalsIgnoreCase("ham")){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							processFile(innerFile,"ham",stopWords,smoothing);
							hamFileNo++;
							totalFileNo++;
						}

						//						if(hamFileNo>0 && hamFileNo%100==0){
						//							System.out.println("Processed "+hamFileNo+ " Ham files.");
						//						}
					}

					//System.out.println("Processing Ham files completed. Total number of files processed: "+hamFileNo);
				}

				if( file!=null && file.isDirectory() && file.getName().equalsIgnoreCase("spam")){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							processFile(innerFile,"spam",stopWords,smoothing);
							spamFileNo++;
							totalFileNo++;
						}

						//						if(spamFileNo>0 && spamFileNo%100==0){
						//							System.out.println("Processed "+spamFileNo+ " Spam files.");
						//						}
					}

					//System.out.println("Processing Spam files completed. Total number of files processed: "+spamFileNo);
				}


			}


			System.out.println("Processing files completed. Total number of files processed: "+totalFileNo);


			long endScanTime = System.currentTimeMillis();

			System.out.println("Total time taken to scan all files in ms : " +(endScanTime - startScanTime) );


		}else{
			System.out.println(trainPath+" is not a valid directory path.");
		}



		saveARFF();

		buildTestARFFConvertor(stopWords,smoothing,bkp_keySet);

		//		System.out.println("Spam tokens: " + spamTokens);
		//		System.out.println("Ham tokens: " + hamTokens);
		//		System.out.println("Total tokens: " + allTokens);
		//		System.out.println("Total Vocab size : " + allTokemMap.size());


		//System.out.println("Looking for Test files in: "+testPath);

	}

	void buildTestARFFConvertor(boolean stopWords,boolean smoothing,Set<String> bkp_keySet){

		long startScanTime = System.currentTimeMillis();

		File dir = new File(testPath);

		//checking directory
		if(dir.isDirectory()){
			files = dir.listFiles();

			//processing files
			for(File file: files){

				if( file!=null && file.isDirectory() && file.getName().equalsIgnoreCase("ham")){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							processFile(innerFile,"ham",stopWords,smoothing,bkp_keySet);
							hamFileNo++;
							totalFileNo++;
						}

						//						if(hamFileNo>0 && hamFileNo%100==0){
						//							System.out.println("Processed "+hamFileNo+ " Ham files.");
						//						}
					}

					//System.out.println("Processing Ham files completed. Total number of files processed: "+hamFileNo);
				}

				if( file!=null && file.isDirectory() && file.getName().equalsIgnoreCase("spam")){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							processFile(innerFile,"spam",stopWords,smoothing,bkp_keySet);
							spamFileNo++;
							totalFileNo++;
						}

						//						if(spamFileNo>0 && spamFileNo%100==0){
						//							System.out.println("Processed "+spamFileNo+ " Spam files.");
						//						}
					}

					//System.out.println("Processing Spam files completed. Total number of files processed: "+spamFileNo);
				}


			}


			System.out.println("Processing files completed. Total number of files processed: "+totalFileNo);


			long endScanTime = System.currentTimeMillis();

			System.out.println("Total time taken to scan all files in ms : " +(endScanTime - startScanTime) );


		}else{
			System.out.println(trainPath+" is not a valid directory path.");
		}



		saveARFF();

		//		System.out.println("Spam tokens: " + spamTokens);
		//		System.out.println("Ham tokens: " + hamTokens);
		//		System.out.println("Total tokens: " + allTokens);
		//		System.out.println("Total Vocab size : " + allTokemMap.size());


		//System.out.println("Looking for Test files in: "+testPath);

	}

	public void processFile(File file,String classText,boolean stopWords,boolean smoothing,Set<String> bkp_keySet){
		//System.out.println("Processing file:"+file.getName());
		Scanner reader;
		TokenFrequency tokfreq;
		long freqLong=0;
		//String docId = file.getName();


		try {
			reader = new Scanner(file);
			String line;
			//HashMap<String,Integer> fileVector = new HashMap<String,Integer>();
			while(reader.hasNextLine()) {
				line = reader.nextLine();

				if(line!=null &&line.length()>0 ){

					line = line.trim();
					line = line.toLowerCase();
					if(smoothing){
						line = removeExcludedWords(line);
					}
					if(null!=line && line.length()>0){

						String tokens[] = line.split(" ");

						for(String token : tokens ){

							if(token.endsWith(".")){
								token = token.substring(0, token.length()-1);

							}

							if(null!=token && token.length()>0){


								if(stopWords){
									// checking if token is a stop word
									if(StopWordList.contains(token)){
										//System.out.println("Ignoring stop word: " + token);
										continue;
										//System.out.println("after continue");
									}
								}		

								if( token.length() <=2){
									continue;
								}
								if(smoothing){
									Stemmer s = new Stemmer();

									for(int i=0; i <token.length(); i++)
									{
										s.add(token.charAt(i));
									}

									s.stem();

									//System.out.println("Token before Stemming: "+token);
									token = s.toString();
									//System.out.println("Token After Stemming: "+token);

								}	
								//checking if map contains token


								if(!bkp_keySet.contains(token)){
									continue;
								}

								if(classText.equalsIgnoreCase("spam")){
									spamTokens++;

									if(spamTokemMap.containsKey(token)){
										freqLong = spamTokemMap.get(token);
										spamTokemMap.put(token, freqLong+1);

									}else{
										tokfreq = new TokenFrequency(token, 1);
										spamTokemMap.put(token, new Long(1));
									}

									spamList.add(totalFileNo);


								}else if(classText.equalsIgnoreCase("ham")){
									hamTokens++;

									if(hamTokemMap.containsKey(token)){
										freqLong = hamTokemMap.get(token);
										hamTokemMap.put(token, freqLong+1);

									}else{
										tokfreq = new TokenFrequency(token, 1);
										hamTokemMap.put(token, new Long(1));
									}
								}else{
									System.out.println("Error: Only two clases spam amd ham allowed for this method");
								}

								allTokens++;
								if(allTokemMap.containsKey(token)){
									tokfreq = allTokemMap.get(token);
									tokfreq.setFrequency(tokfreq.getFrequency()+1);
									allTokemMap.put(token, tokfreq);

								}else{
									tokfreq = new TokenFrequency(token, 1);
									allTokemMap.put(token, tokfreq);
								}

								HashMap<Integer,Integer> fileCounts;
								if(tokfreq.getFileCounts() == null){
									fileCounts = new HashMap<Integer,Integer>();
								}else{
									fileCounts = tokfreq.getFileCounts();
								}

								if(fileCounts.containsKey(totalFileNo)){
									int count = fileCounts.get(totalFileNo);
									count ++;
									fileCounts.put(totalFileNo,count);
								}else{
									fileCounts.put(totalFileNo,1);
								}

								tokfreq.setFileCounts(fileCounts);

							}


						}


					}
				}

			}


		}catch(IOException exp){
			exp.printStackTrace();
		}

		return;

	}



	public void processFile(File file,String classText,boolean stopWords,boolean smoothing){
		//System.out.println("Processing file:"+file.getName());
		Scanner reader;
		TokenFrequency tokfreq;
		long freqLong=0;
		//String docId = file.getName();


		try {
			reader = new Scanner(file);
			String line;
			//HashMap<String,Integer> fileVector = new HashMap<String,Integer>();
			while(reader.hasNextLine()) {
				line = reader.nextLine();

				if(line!=null &&line.length()>0 ){

					line = line.trim();
					line = line.toLowerCase();
					if(smoothing){
						line = removeExcludedWords(line);
					}
					if(null!=line && line.length()>0){

						String tokens[] = line.split(" ");

						for(String token : tokens ){

							if(token.endsWith(".")){
								token = token.substring(0, token.length()-1);

							}

							if(null!=token && token.length()>0){


								if(stopWords){
									// checking if token is a stop word
									if(StopWordList.contains(token)){
										//System.out.println("Ignoring stop word: " + token);
										continue;
										//System.out.println("after continue");
									}
								}		

								if( token.length() <=2){
									continue;
								}
								if(smoothing){
									Stemmer s = new Stemmer();

									for(int i=0; i <token.length(); i++)
									{
										s.add(token.charAt(i));
									}

									s.stem();

									//System.out.println("Token before Stemming: "+token);
									token = s.toString();
									//System.out.println("Token After Stemming: "+token);

								}	
								//checking if map contains token

								if(classText.equalsIgnoreCase("spam")){
									spamTokens++;

									if(spamTokemMap.containsKey(token)){
										freqLong = spamTokemMap.get(token);
										spamTokemMap.put(token, freqLong+1);

									}else{
										tokfreq = new TokenFrequency(token, 1);
										spamTokemMap.put(token, new Long(1));
									}

									spamList.add(totalFileNo);


								}else if(classText.equalsIgnoreCase("ham")){
									hamTokens++;

									if(hamTokemMap.containsKey(token)){
										freqLong = hamTokemMap.get(token);
										hamTokemMap.put(token, freqLong+1);

									}else{
										tokfreq = new TokenFrequency(token, 1);
										hamTokemMap.put(token, new Long(1));
									}
								}else{
									System.out.println("Error: Only two clases spam amd ham allowed for this method");
								}

								allTokens++;
								if(allTokemMap.containsKey(token)){
									tokfreq = allTokemMap.get(token);
									tokfreq.setFrequency(tokfreq.getFrequency()+1);
									allTokemMap.put(token, tokfreq);

								}else{
									tokfreq = new TokenFrequency(token, 1);
									allTokemMap.put(token, tokfreq);
								}

								HashMap<Integer,Integer> fileCounts;
								if(tokfreq.getFileCounts() == null){
									fileCounts = new HashMap<Integer,Integer>();
								}else{
									fileCounts = tokfreq.getFileCounts();
								}

								if(fileCounts.containsKey(totalFileNo)){
									int count = fileCounts.get(totalFileNo);
									count ++;
									fileCounts.put(totalFileNo,count);
								}else{
									fileCounts.put(totalFileNo,1);
								}

								tokfreq.setFileCounts(fileCounts);

							}


						}


					}
				}

			}


		}catch(IOException exp){
			exp.printStackTrace();
		}

		return;

	}

	public String removeExcludedWords(String line){

		//System.out.println("line is: "+ line);
		for(int i=0;i<execWordList.size();i++){
			//System.out.println("replacing "+ execWordList.get(i) + " with space");
			line=line.replaceAll(execWordList.get(i), " ");
			//System.out.println("line after replacing: " + line);
		}
		return line;
	}




	double roundTwoDecimals(double d) {
		DecimalFormat threeDForm = new DecimalFormat("00.00");
		return Double.valueOf(threeDForm.format(d));
	}


	void saveARFF(){
		System.out.println("Saving to ARFF");
		HashMap<Integer,Integer> fileCounts = new HashMap<Integer,Integer>();
		for(int i=0;i<totalFileNo;i++){
			fileCounts.put(i, 1);
		}
		allTokemMap.put("", new TokenFrequency("", 1, 0,fileCounts ));

		convertFileVectorToArray();
		saveasARFF();

		//printtokenWs();
	}

	void printtokenWs(){
		for(int i=0;i<keys.length;i++){
			System.out.println("Token: "+ allTokemMap.get(keys[i]).getToken() + "  Weight: "  + allTokemMap.get(keys[i]).getwValue());
		}
	}

	void saveasARFF(){
		long startTime = System.currentTimeMillis();

		System.out.println("Saving to ARFF file");

		File index =  new File(outFileName);

		try{
			boolean createStatus = index.createNewFile();
			if(createStatus){
				FileOutputStream out = new FileOutputStream(outFileName);
				String strContent  = "";


				strContent = "@RELATION Spam_Ham\n\n";
				out.write(strContent.getBytes());

				for(int i=0;i<keys.length;i++){
					strContent = "@ATTRIBUTE term_"+keys[i]+" NUMERIC\n";
					out.write(strContent.getBytes());
				}


				strContent = "@ATTRIBUTE class {1,-1}\n\n@DATA\n";
				out.write(strContent.getBytes());


				for(int i=0;i<filePostings.length;i++){
					strContent = "";
					for(int j=0;j<=keys.length;j++){
						//System.out.println("i "+i + " j "+j);
						strContent += filePostings[i][j]; 
						if(j!= keys.length){
							strContent +=",";	
						}else{
							strContent +="\n";
						}
					}
					out.write(strContent.getBytes());
				}


				out.close();
				System.out.println("File successfully saved to:"+outFileName);


				long endTime = System.currentTimeMillis();

				System.out.println("Total time taken to Save File(in ms) :" +(endTime - startTime) );
				System.out.println("Size of ARFF Filed(in bytes): "+index.length());
			}else{
				System.out.println("File :\""+outFileName + "\" already esists please move the file to some other location and execute the program again." );
			}

		}catch(IOException exp){
			System.out.println("Exception Occurred");
			System.out.println(exp.getMessage());
			exp.printStackTrace();
		}

	}



	void convertFileVectorToArray(){
		Set<String> keySet = allTokemMap.keySet();
		bkp_keySet = allTokemMap.keySet();

		keys = keySet.toArray();
		HashMap<Integer,Integer> fileCounts;

		//System.out.println("Keys Length is " + keys.length + " all tokens map size " + allTokemMap.size());

		filePostings = new int [totalFileNo][allTokemMap.size()+1];
		for(int i =0;i<allTokemMap.size()+1;i++){
			for(int j=0;j<totalFileNo;j++){

				if(i==allTokemMap.size()){
					if(!spamList.contains(j)){
						filePostings[j][i] = 1;
					}else{
						filePostings[j][i] = -1;
					}
				}else{
					fileCounts = allTokemMap.get(keys[i]).getFileCounts();
					if(fileCounts.containsKey(j)){
						filePostings[j][i] = fileCounts.get(j);
					}else{
						filePostings[j][i] = 0;
					}
				}

			}
		}
	}
}
