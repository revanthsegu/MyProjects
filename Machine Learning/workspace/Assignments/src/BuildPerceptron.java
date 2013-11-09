import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class BuildPerceptron {
	int totalFileNo=0;
	int spamFileNo=0;
	int hamFileNo=0;
	String trainPath = "";
	String testPath = "";
	File[] files;
	File[] inner_files;

	long spamTokens=0;
	long hamTokens=0;
	long allTokens=0;
	long testSpam=0;
	long testHam=0;
	long correctSpamClassifyPR=0;
	long inCorrectSpamClassifyPR=0;
	long correctHamClassifyPR=0;
	long inCorrectHamClassifyPR=0;
	final double SPAM_VALUE = -1;
	final double HAM_VALUE = 1;
	final double DEC_VALUE = 0;
	double neta=0.01;
	int hardLimit = 100;
	Object[] keys;
	ArrayList<Integer> spamList = new ArrayList<Integer>();
	double [] pout;
	int [][] filePostings; 

	String stopWordsPath = "";

	ArrayList<String> StopWordList = new ArrayList<String>();
	ArrayList<String> execWordList = new ArrayList<String>();
	HashMap<String,Long> spamTokemMap = new HashMap<String,Long>(); // to store tokens and frequency of token 
	HashMap<String,Long> hamTokemMap = new HashMap<String,Long>(); // to store tokens and frequency of token 
	HashMap<String,TokenFrequency> allTokemMap = new HashMap<String,TokenFrequency>(); // to store tokens and frequency of token 

	public static void main(String args[]){

		long startTime = System.currentTimeMillis();
		BuildPerceptron maincalss;
		if(args.length<3){
			System.out.println("Please enter all arguments and execute the program again");
			return;
		}

		if(args.length>3 && args[3]!=null && args[3].equalsIgnoreCase("yes")){
			System.out.println("Executing with smoothing feature");
			maincalss = new BuildPerceptron();
			maincalss.buildTextClassfier(args, false,true);

			maincalss = new BuildPerceptron();
			System.out.println("\nExecuting with smoothing feature and removing stop words");
			maincalss.buildTextClassfier(args, true,true);

		}else{

			maincalss = new BuildPerceptron();
			maincalss.buildTextClassfier(args, false,false);

			maincalss = new BuildPerceptron();
			System.out.println("\nExecuting removing stop words");
			maincalss.buildTextClassfier(args, true,false);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time taken to execute the program in ms : " +(endTime - startTime) );
	}



	BuildPerceptron(){
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
		//execWordList.add("%");
		execWordList.add("^");
		//execWordList.add("&");
		//execWordList.add("*");
	}

	void buildTextClassfier(String args[],boolean stopWords,boolean smoothing){
		Scanner reader;
		//System.out.println("Exection Started !!");
		long startScanTime = System.currentTimeMillis();

		//System.out.println("Dec Value :"+ DEC_VALUE);
		//program Takes first argument to main function as dir path for training data
		//if there is no argument it assumes current directory as path for training data

		if(args.length >0 && null!=args[0] && args[0].length() > 2){
			trainPath = args[0];
		}else{
			trainPath = System.getProperty("user.dir")+"//train";
		}

		if(args.length >1 && null!=args[1] && args[1].length() > 2){
			stopWordsPath = args[1];
		}else{
			stopWordsPath = System.getProperty("user.dir")+"//stopwords";
		}

		if(args.length >2 && null!=args[2] && args[2].length() > 2){
			testPath = args[2];
		}else{
			testPath = System.getProperty("user.dir")+"//test";
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

		calculatePerceptronWeights();

		//		System.out.println("Spam tokens: " + spamTokens);
		//		System.out.println("Ham tokens: " + hamTokens);
		//		System.out.println("Total tokens: " + allTokens);
		//		System.out.println("Total Vocab size : " + allTokemMap.size());


		//System.out.println("Looking for Test files in: "+testPath);


		dir = new File(testPath);
		//String nbClass;
		//checking directory
		if(dir.isDirectory()){
			files = dir.listFiles();

			//processing files
			for(File file: files){

				if( file!=null && file.isDirectory() && file.getName().equalsIgnoreCase("ham")){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							testHam++;
							classifyFile(innerFile,stopWords,smoothing,"ham"); 

						}

						//						if(hamFileNo>0 && hamFileNo%100==0){
						//							System.out.println("Processed "+hamFileNo+ " Ham files.");
						//						}
					}

				}

				if( file!=null && file.isDirectory() && file.getName().equalsIgnoreCase("spam")){

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							testSpam++;
							classifyFile(innerFile,stopWords,smoothing,"spam");
						}

						//						if(spamFileNo>0 && spamFileNo%100==0){
						//							System.out.println("Processed "+spamFileNo+ " Spam files.");
						//						}
					}

				}



			}

			double percentage=0;

			/*percentage = (double)(correctHamClassifyPR)/(double)(testHam);

			percentage *= 100;
			percentage = roundTwoDecimals(percentage);

			System.out.println("\nAccuracy of Ham for Perceptron Classifier: "+percentage + "%");

			percentage = (double)(correctSpamClassifyPR)/(double)(testSpam);

			percentage *= 100;
			percentage = roundTwoDecimals(percentage);

			System.out.println("Accuracy of Spam for Perceptron Classifier: "+percentage + "%");*/


			percentage = (double)(correctHamClassifyPR+correctSpamClassifyPR)/(double)(testHam+testSpam);

			percentage *= 100;
			percentage = roundTwoDecimals(percentage);

			System.out.println("Accuracy of Perceptron Classifier: "+percentage + "%");

			long endScanTime = System.currentTimeMillis();

			System.out.println("Total time taken to calssify all files in ms : " +(endScanTime - startScanTime) );


		}else{
			System.out.println(testPath+" is not a valid directory path.");
		}


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



	public void classifyFile(File file,boolean stopWords,boolean smoothing,String classifyText){
		double PRscore=0;
		Scanner reader;

		try {
			reader = new Scanner(file);
			String line;

			Set<String> keySet = allTokemMap.keySet();
			String key;
			Iterator<String> itr;

			itr = keySet.iterator();
			//resetting counts
			while(itr.hasNext()){
				key = itr.next();
				allTokemMap.get(key).setCurFilecount(0);
			}

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


								if(allTokemMap.containsKey(token)){
									allTokemMap.get(token).setCurFilecount(allTokemMap.get(token).getCurFilecount()+1);
								}


							}


						}

					}
				}

			}


			//PR Classification
			itr = keySet.iterator();
			//resetting counts
			while(itr.hasNext()){
				key = itr.next();
				PRscore += (double)(allTokemMap.get(key).getCurFilecount()*allTokemMap.get(key).wValue);
			}
			//System.out.println("PR Score is " + PRscore );
			if(PRscore <DEC_VALUE ){
				if(classifyText.equalsIgnoreCase("spam")){
					correctSpamClassifyPR++;
				}else{
					inCorrectSpamClassifyPR++;
				}
			}else{
				if(classifyText.equalsIgnoreCase("ham")){
					correctHamClassifyPR++;
				}else{
					inCorrectHamClassifyPR++;
				}
			}


		}catch(IOException exp){
			exp.printStackTrace();
		}

	}

	double roundTwoDecimals(double d) {
		DecimalFormat threeDForm = new DecimalFormat("00.00");
		return Double.valueOf(threeDForm.format(d));
	}

	void calculatePerceptronWeights(){
		double wi;
		//double recTime=0;
		TokenFrequency tokFreq; 


		HashMap<Integer,Integer> fileCounts = new HashMap<Integer,Integer>();
		for(int i=0;i<totalFileNo;i++){
			fileCounts.put(i, 1);
		}
		allTokemMap.put("", new TokenFrequency("", 1, 0,fileCounts ));

		convertFileVectorToArray();

		pout = new double[totalFileNo];

		Set<String> keySet = allTokemMap.keySet();
		String key;
		Iterator<String> itr;


		itr = keySet.iterator();
		//System.out.println("size of dictionary: "+allTokemMap.size());
		double wini = (double)1/(allTokemMap.size());
		//System.out.println("wini is" + wini);
		while(itr.hasNext()){
			key = itr.next();
			tokFreq = allTokemMap.get(key);
			tokFreq.setwValue(wini);
			allTokemMap.put(key,tokFreq);
		}


		for(int i=0;i<hardLimit;i++){
			//System.out.print("Iteration "+ i + "- ");
			updatePouts();

			//System.out.println("Running iteration "+ i);
			//recTime = System.currentTimeMillis();

			itr = keySet.iterator();

			for(int j=0;j<keys.length;j++){
				//while(itr.hasNext()){
				//System.out.println("Starting new token");	
				key = itr.next();
				tokFreq = allTokemMap.get(key);
				wi = tokFreq.getwValue();
				//System.out.println(" initial wi is "+wi+"  should be  " + neta * condAPostr(tokFreq));
				wi = wi + (double)(neta * multiFactor(j));
				tokFreq.setwValue(wi);
				allTokemMap.put(key, tokFreq);


				//System.out.println("updated w for token " + tokFreq.getToken() + " is " +tokFreq.getwValue() );
			}


			//System.out.println("time taken to run an iteration in ms : "+ (System.currentTimeMillis()-recTime));
			//printtokenWs();
		}



	}

	void printtokenWs(){
		for(int i=0;i<keys.length;i++){
			System.out.print("(Token:Weight) "+ allTokemMap.get(keys[i]).getToken() + " : "  + allTokemMap.get(keys[i]).getwValue());
		}
		System.out.println();
	}

	void updatePouts(){
		double sum=0;
		double xi=0;
		TokenFrequency tokFreq;
		HashMap<Integer,Integer> fileCounts;

		Set<String> keySet = allTokemMap.keySet();
		String key;
		Iterator<String> itr;

		for(int i=0;i<pout.length;i++){
			sum=0;
			itr = keySet.iterator();

			while(itr.hasNext()){
				xi=0;
				key = itr.next();

				tokFreq = allTokemMap.get(key);

				fileCounts = tokFreq.getFileCounts();

				if(fileCounts!=null && fileCounts.containsKey(i)){
					xi = fileCounts.get(i);
				}else{
					xi = 0;
				}

				//System.out.println("w value is "+tokFreq.getwValue() + " xi is" + xi);

				sum += (double)(tokFreq.getwValue()*xi);
			}

			if(sum<DEC_VALUE){
				pout[i] = SPAM_VALUE;
			}else{
				pout[i] = HAM_VALUE;
			}
		}

		//printPouts();
	}

	void printPouts(){
		System.out.print("Updated Pouts ");
		for(int i=0;i<pout.length;i++){
			System.out.print(i +":" + pout[i]+" ");
		}
		System.out.println();
	}


	double multiFactor(int index){
		double sum = 0;
		double yj = 0;
		double xi = 0;

		for(int i=0;i<totalFileNo;i++){

			if(filePostings[i][allTokemMap.size()] == 1 ){
				yj =  HAM_VALUE;
			}else{
				yj =  SPAM_VALUE;
			}
			xi =  filePostings[i][index];

			sum += xi*(yj - pout[i]);

		}

		return (sum);

	}

	double getexpRatio(double sum){

		double ratio =0;

		ratio = -1*sum;

		ratio = Math.exp(ratio);

		ratio = (double)1/(double)(1+ratio);

		//System.out.println("sum is " + sum + " ratio is "+ratio);

		return ratio;
	}
	void convertFileVectorToArray(){
		Set<String> keySet = allTokemMap.keySet();

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
						filePostings[j][i] = 0;
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
