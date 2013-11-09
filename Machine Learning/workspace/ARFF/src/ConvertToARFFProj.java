import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class ConvertToARFFProj {
	int totalFileNo=0;
	int spamFileNo=0;
	int hamFileNo=0;
	int classCount=0;
	String trainPath = "";
	String outFileName = "";
	File[] files;
	File[] inner_files;

	long spamTokens=0;
	long hamTokens=0;
	long allTokens=0;
	long testSpam=0;
	long testHam=0;
	Object[] keys;
	int [][] filePostings; 

	String stopWordsPath = "";
	ArrayList<String> StopWordList = new ArrayList<String>();
	ArrayList<String> execWordList = new ArrayList<String>();
	ArrayList<String> execLines = new ArrayList<String>();
	HashMap<String,TokenFrequency> allTokemMap = new HashMap<String,TokenFrequency>(); // to store tokens and frequency of token 
	HashMap< String,ArrayList<Integer>> fileClass = new HashMap< String,ArrayList<Integer>>();

	public static void main(String args[]){

		long startTime = System.currentTimeMillis();
		ConvertToARFFProj maincalss;

		if(args.length<3){
			System.out.println("Please enter all arguments and execute the program again");
			return;
		}

		if(args.length>2){
			System.out.println("Executing with smoothing feature");
			maincalss = new ConvertToARFFProj();
			maincalss.buildARFFConvertor(args, true,true);

		}

		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time taken to execute the program in ms : " +(endTime - startTime) );
	}



	ConvertToARFFProj(){
		execWordList.add("subject ");
		execWordList.add("subject :");
		//execWordList.add("size");
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

		execLines.add("Message-ID");
		execLines.add("message-ID");
		execLines.add("message-id");
		execLines.add("Date");
		execLines.add("From:");
		execLines.add("from:");
		execLines.add("To:");
		execLines.add("to:");
		execLines.add("Mime-Version:");
		execLines.add("Content-Type:");
		execLines.add("Content-Transfer-Encoding:");
		execLines.add("X-From:");
		execLines.add("X-To:");
		execLines.add("X-cc:");
		execLines.add("X-bcc:");
		execLines.add("X-Folder:");
		execLines.add("X-Origin:");
		execLines.add("X-FileName:");
		execLines.add("-----original message-----");


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
			outFileName = System.getProperty("user.dir")+"//outputProj.arff";
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

				if( file!=null && file.isDirectory()){
					//System.out.println("Processing Directory " + file.getName());

					fileClass.put(file.getName(), new ArrayList<Integer>());

					inner_files = file.listFiles();

					for(File innerFile: inner_files){
						if(innerFile!=null && innerFile.isFile()){
							processFile(innerFile,file.getName(),stopWords,smoothing);
							totalFileNo++;
						}

						if(totalFileNo>0 && totalFileNo%100==0){
							System.out.println("Processed "+totalFileNo+ " Files.");
						}
					}

					//System.out.println("Processing Ham files completed. Total number of files processed: "+hamFileNo);
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

					//System.out.println("Line is " + line);
					if(shouldBeRemoved(line)){
						//System.out.println("Removing line " + line);
						continue;
					}

					//System.out.println("Not removing line " + line);
					if(smoothing){
						line = removeExcludedWords(line);
					}
					if(null!=line && line.length()>0){

						String tokens[] = line.split(" ");

						for(String token : tokens ){

							if(token.endsWith(".")){
								token = token.substring(0, token.length()-1);

							}


							token = token.trim();

							while(token.contains(" ")){
								token.replaceAll(" ", "");
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

									while(token.endsWith("\\")){
										token = token.substring(0, token.length()-1);

									}
									while(token.endsWith(")")){
										token = token.substring(0, token.length()-1);

									}

									while(token.endsWith(">")){
										//System.out.println("before replacing ending >" + token);
										token = token.substring(0, token.length()-1);
										//System.out.println("After replacing ending >" + token);

									}

									while(token.endsWith("<")){
										token = token.substring(0, token.length()-1);

									}

									while(token.endsWith("?")){
										token = token.substring(0, token.length()-1);

									}

									while(token.startsWith("?")){
										token = token.substring(1);

									}

									while(token.startsWith("<")){
										token = token.substring(1);

									}

									while(token.startsWith(">")){
										token = token.substring(1);

									}


									while(token.startsWith("\\")){
										token = token.substring(1);

									}

									while(token.startsWith("(")){
										token = token.substring(1);

									}

									boolean removed = true;
									while(removed){
										removed=false;
										for(int i=0;i<token.length();i++){
											if(token.charAt(i) == '\\' || token.charAt(i) == '='){
												String firstHalf = token.substring(0, i);
												String secondHalf = token.substring(i+1);
												//System.out.println("Encountered \\ Original String "+ token+" after removing: "+firstHalf + secondHalf);
												token = firstHalf + secondHalf;
												removed=true;
											}
										}
									}



									Stemmer s = new Stemmer();

									for(int i=0; i <token.length(); i++)
									{
										s.add(token.charAt(i));
									}

									s.stem();

									//System.out.println("Token before Stemming: "+token);
									token = s.toString();
									//System.out.println("Token After Stemming: "+token);




									if(token.equalsIgnoreCase("class")){
										token = "_" + token;
									}


								}	

								if( token.length() <=2){
									continue;
								}


								//checking if map contains token

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


			if(fileClass.containsKey(classText)){
				ArrayList<Integer> classPost = fileClass.get(classText);
				classPost.add(totalFileNo);
				fileClass.put(classText, classPost);
			}else{
				ArrayList<Integer> classPost = new ArrayList<Integer>();
				classPost.add(totalFileNo);
				fileClass.put(classText, classPost);
			}

		}catch(IOException exp){
			exp.printStackTrace();
		}

		return;

	}

	public String removeExcludedWords(String line){


		//line.replaceAll("\\\\", ",");


		//System.out.println("line is: "+ line);
		for(int i=0;i<execWordList.size();i++){
			//System.out.println("replacing "+ execWordList.get(i) + " with space");
			line=line.replaceAll(execWordList.get(i), " ");
			//System.out.println("line after replacing: " + line);
		}
		return line;
	}

	public boolean shouldBeRemoved(String line){
		boolean ret = false;
		for(int i=0;i<execLines.size();i++){
			if(line.startsWith(execLines.get(i))){
				ret=true;
				break;
			}
		}

		return ret;
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
			//if(createStatus){
			FileOutputStream out = new FileOutputStream(outFileName);
			String strContent  = "";


			strContent = "@RELATION Enron\n\n";
			out.write(strContent.getBytes());

			for(int i=0;i<keys.length;i++){
				strContent = "@ATTRIBUTE \""+keys[i]+"\" NUMERIC\n";
				out.write(strContent.getBytes());
			}



			strContent = "@ATTRIBUTE class {";

			int i=0;
			for( String key: fileClass.keySet()){
				strContent = strContent + key;
				//System.out.println("Writing Attribute " + key);
				if(i!=fileClass.keySet().size()-1)
					strContent += ",";
				i++;
			}

			strContent += "}\n\n@DATA\n";
			out.write(strContent.getBytes());

			for(i=0;i<filePostings.length;i++){
				strContent = "";
				for(int j=0;j<=keys.length;j++){
					//System.out.println("i "+i + " j "+j);
					if(j<keys.length){
						//System.out.println(j);
						strContent += filePostings[i][j];
					}else{
						for(String key: fileClass.keySet()){
							if(fileClass.get(key).contains(i)){
								strContent += key;
								break;
							}
						}

					}

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
			/*}else{
		System.out.println("File :\""+outFileName + "\" already esists please move the file to some other location and execute the program again." );
	}*/

		}catch(IOException exp){
			System.out.println("Exception Occurred");
			exp.printStackTrace();
		}

	}


	void convertFileVectorToArray(){
		Set<String> keySet = allTokemMap.keySet();

		keys = keySet.toArray();
		HashMap<Integer,Integer> fileCounts;

		System.out.println("Keys Length is " + keys.length + " all tokens map size " + allTokemMap.size());

		filePostings = new int [totalFileNo][allTokemMap.size()];
		for(int i =0;i<allTokemMap.size();i++){
			for(int j=0;j<totalFileNo;j++){

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
