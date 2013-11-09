import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class CreateIndex {

	//Program Configuration variables
	int QUIT_AFTER = 5000;
	//Program Configuration variables
	HashMap<String,ArrayList<TermFrequency>> tokemMap = new HashMap<String,ArrayList<TermFrequency>>(); // to store tokens and frequency of token 
	ArrayList<TokenFrequency> fileFreqList = new ArrayList<TokenFrequency>(); // to store frequency of the most frequent stem,total number of word occurrences,doclen 
	ArrayList<String> StopWordList = new ArrayList<String>();
	String IndexPath ="";
	public static void main(String[] args){

		CreateIndex mainClass= new CreateIndex();

		long startTime = System.currentTimeMillis();
		mainClass.createIndex(args);
		//			System.out.println(mainClass.addPadding(mainClass.getDeltaCode(5)));
		//			System.out.println(mainClass.addPadding(mainClass.getDeltaCode(9)));
		//			System.out.println(mainClass.addPadding(mainClass.getDeltaCode(1)));
		//			for (int i=1;i<256;i++) 
		//			{	
		//				char a = (char)(i);
		//				int j = Character.getNumericValue(a);
		//			System.out.println(i+":"+a+":"+j);
		//			}
		//			
		//			mainClass.getGammaString(5);
		//			mainClass.getGammaString(9);
		//			mainClass.getGammaString(60);
		long endTime = System.currentTimeMillis();

		System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );

	}



	public void createIndex(String[] args){
		String dirPath = "";
		File[] files;
		int noFiles=0;
		String stopWordsPath = "";
		Scanner reader;


		System.out.println("Exection Started !!");
		long startScanTime = System.currentTimeMillis();

		//program Takes first argument to main function as dir path for Cranfield collection 
		//if there is no argument it assumes current directory as dirpath of Cranfield collection

		if(args.length >0 && null!=args[0] && args[0].length() > 2){
			dirPath = args[0];
		}else{
			dirPath = System.getProperty("user.dir");
		}



		if(args.length >1 && null!=args[1] && args[1].length() > 2){
			stopWordsPath = args[1];
		}else{
			stopWordsPath = System.getProperty("user.dir")+"\\stopwords";
		}

		if(args.length >2 && null!=args[2] && args[2].length() > 2){
			IndexPath = args[2];
		}else{
			IndexPath = System.getProperty("user.dir");
		}


		//reading stop words
		try{
			System.out.println("Looking for Stopwords file in the path: "+stopWordsPath);
			File stopWordsFile = new File(stopWordsPath);
			reader = new Scanner(stopWordsFile);
			String stopWord;
			while(reader.hasNextLine()) {
				stopWord = reader.nextLine();
				StopWordList.add(stopWord);
			}

			System.out.println("Processed Stop Words");
			System.out.println("Stop Words are: "+StopWordList);

		}catch(FileNotFoundException e){
			System.out.println("Not able to open stopword file in the path: "+stopWordsPath);
			e.printStackTrace();
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



			System.out.println("Total time taken to scan all files from database in ms : " +(endScanTime - startScanTime) );


			//printTokenMap();
			//PrintFileFreqList();
			saveIndex();
			saveCompressedIndex();
			System.out.println("Total number of inverted lists in the index are: "+tokemMap.size());
			System.out.println("Printing Document Frequency and Inverted Lists");
			System.out.println("Token:Document frequency:Posting List(DocId,Frequency)");

			printTokenMap("Reynolds");
			printTokenMap("NASA");
			printTokenMap("prandtl");
			printTokenMap("flow");
			printTokenMap("pressure");
			printTokenMap("boundary");
			printTokenMap("shock");
		}else{
			System.out.println(dirPath+" is not a valid directory path.");
		}



	}


	public void processFile (File file){
		//System.out.println("Processing file:"+file.getName());
		Scanner reader;
		long doclen=0;
		long docId = Long.parseLong(file.getName().substring(9));
		//String docId = file.getName();
		long max_freq = 0;
		String max_freq_token = "";
		HashMap<String,Long> fileTokemMap = new HashMap<String,Long>(); // to store tokens and frequency of token 

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
								doclen++;

								// checking if token is a stop word
								if(!StopWordList.contains(token)){
									//System.out.println("Token Before Stemming:"+token);

									Stemmer s = new Stemmer();

									for(int i=0; i <token.length(); i++)
									{
										s.add(token.charAt(i));
									}

									s.stem();

									token = s.toString();
									//System.out.println("Token After Stemming:"+token);

									//checking if map contains token
									if(fileTokemMap.containsKey(token)){
										Long freq = fileTokemMap.get(token);
										fileTokemMap.put(token, freq+1);

									}else{

										fileTokemMap.put(token, new Long(1));
									}
								}else{
									//System.out.println("Slipping Stop Word:"+token);
								}


							}

						}
					}

				}
			}


			Set<String> keySet = fileTokemMap.keySet();
			String key;
			Iterator<String> itr = keySet.iterator();
			Long tokFreq=new Long(0);

			while(itr.hasNext()){
				key = itr.next();
				tokFreq = fileTokemMap.get(key);

				if(tokFreq > max_freq){
					max_freq = tokFreq;
					max_freq_token = key;
				}

				TermFrequency termFreqObj= new TermFrequency(docId,tokFreq);
				if(tokemMap.containsKey(key)){

					ArrayList<TermFrequency> tokList= tokemMap.get(key);

					tokList.add(termFreqObj);
					tokemMap.put(key, tokList);
				}else{

					ArrayList<TermFrequency> tokList= new ArrayList<TermFrequency>();

					tokList.add(termFreqObj);
					tokemMap.put(key, tokList);

				}


			}

			TokenFrequency filefreq = new TokenFrequency(docId, max_freq_token, max_freq,doclen);
			fileFreqList.add(filefreq);

		}catch(FileNotFoundException exp){
			exp.printStackTrace();
		}

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
		System.out.println("S.No:Token:Posting Len:Posting List(DocId,Frequency)");
		while(itr.hasNext()){
			key = itr.next();

			ArrayList<TermFrequency> termFreqList = tokemMap.get(key);
			System.out.print(i+":"+ key+":"+termFreqList.size()+":");
			for(int j=0;j<termFreqList.size();j++){
				TermFrequency termfreq = termFreqList.get(j);
				System.out.print("(" + termfreq.getDocId()+","+termfreq.getFrequency()+")");

				if(j!=termFreqList.size()-1){
					System.out.print("-->");
				}
			}

			i++;
			System.out.println("\n");
		}
		System.out.println("\n");
		return;
	}


	public void printTokenMap(String token){

		token = token.trim();
		token = token.toLowerCase();
		token = removeSGMLTags(token);
		token = removePossessives(token);
		token = removeCommas(token);



		Stemmer s = new Stemmer();

		for(int i=0; i <token.length(); i++)
		{
			s.add(token.charAt(i));
		}

		s.stem();

		token = s.toString();




		ArrayList<TermFrequency> termFreqList = tokemMap.get(token);
		System.out.print(token+":"+termFreqList.size()+":");
		for(int j=0;j<termFreqList.size();j++){
			TermFrequency termfreq = termFreqList.get(j);
			System.out.print("(" + termfreq.getDocId()+","+termfreq.getFrequency()+")");

			if(j!=termFreqList.size()-1){
				System.out.print("-->");
			}
		}

		System.out.println("\n");
		return;
	}





	public void PrintFileFreqList(){

		System.out.println("The Documents and their Max Frequency Stems are");
		System.out.println("DocId:DocLen:Freq Stem:Frequency");
		TokenFrequency tokFreq;

		for(int i=0;i<fileFreqList.size();i++){

			tokFreq = fileFreqList.get(i);

			if(null!=tokFreq)
			{
				System.out.println(tokFreq.getDocId()+ ":"+tokFreq.getDoclen() + ":"+tokFreq.getToken()+":" + tokFreq.getFrequency());
			}else{
				System.out.println("null : null : null : null");
			}
		}

		System.out.println("\n");

	}


	public void saveCompressedIndex(){

		long startTime = System.currentTimeMillis();

		System.out.println("Saving Compressed Index to file");
		File dir = new File(IndexPath);

		if(!dir.isDirectory()){
			System.out.println(IndexPath+" is not a valid directory creating index in "+System.getProperty("user.dir"));
			IndexPath = System.getProperty("user.dir"); 

		}
		String filepath = IndexPath + "/CompressedIndex";


		File index =  new File(filepath);
		try{
			boolean createStatus = index.createNewFile();
			if(createStatus){
				FileOutputStream out = new FileOutputStream(filepath);
				String strContent  = "";

				Set<String> keySet = tokemMap.keySet();
				String key;
				Iterator<String> itr = keySet.iterator();

				strContent = "(#(";
				out.write(strContent.getBytes());
				while(itr.hasNext()){
					key = itr.next();

					ArrayList<TermFrequency> termFreqList = tokemMap.get(key);
					strContent = "<"+key+"["+termFreqList.size()+"],(";
					out.write(strContent.getBytes());
					for(int j=0;j<termFreqList.size();j++){
						TermFrequency termfreq = termFreqList.get(j);
						writeDeltaString(termfreq.getDocId(), out);
						strContent = ":";
						out.write(strContent.getBytes());
						writeDeltaString(termfreq.getFrequency(), out);	
						if(j!=termFreqList.size()-1){
							strContent = ",";
							out.write(strContent.getBytes());
						}

					}
					strContent = ")>";
					out.write(strContent.getBytes());

				}
				strContent = ")#)";
				out.write(strContent.getBytes());

				//For each document, storing the frequency of the most frequent stem in the document
				strContent = "(#(";
				out.write(strContent.getBytes());
				TokenFrequency tokFreq;

				for(int i=0;i<fileFreqList.size();i++){

					tokFreq = fileFreqList.get(i);

					if(null!=tokFreq)
					{

						strContent = "<";
						out.write(strContent.getBytes());

						writeDeltaString(tokFreq.getDocId(), out);
						strContent =  ":"+tokFreq.getDoclen() + ":"+tokFreq.getToken()+":";
						out.write(strContent.getBytes());

						writeGammaString(tokFreq.getFrequency(), out);

						strContent = ">";
						out.write(strContent.getBytes());


						if(i!=fileFreqList.size()-1){
							strContent = ",";
							out.write(strContent.getBytes());
						}
					}
				}

				strContent = ")#)";
				out.write(strContent.getBytes());

				out.close();
				System.out.println("Index successfully saved to:"+filepath);


				long endTime = System.currentTimeMillis();

				System.out.println("Total time taken to Save Compressed Index(in ms) :" +(endTime - startTime) );
				System.out.println("Size of Index Compressed(in bytes): "+index.length());
			}else{
				System.out.println("File :\""+filepath + "\" already esists please move the file to some other location and execute the program again." );
			}

		}catch(FileNotFoundException exp){
			System.out.println("Exception Occurred");
			exp.printStackTrace();
		}catch(IOException exp){
			System.out.println("Exception Occurred");
			exp.printStackTrace();
		}
		return;
	}


	public void saveIndex(){

		long startTime = System.currentTimeMillis();

		System.out.println("Saving Index to file");
		File dir = new File(IndexPath);

		if(!dir.isDirectory()){
			System.out.println(IndexPath+" is not a valid directory creating index in "+System.getProperty("user.dir"));
			IndexPath = System.getProperty("user.dir"); 

		}
		String filepath = IndexPath + "/Index";


		File index =  new File(filepath);
		try{
			boolean createStatus = index.createNewFile();
			if(createStatus){
				FileOutputStream out = new FileOutputStream(filepath);
				String strContent  = "";

				Set<String> keySet = tokemMap.keySet();
				String key;
				Iterator<String> itr = keySet.iterator();

				strContent = "(#(";
				out.write(strContent.getBytes());
				while(itr.hasNext()){
					key = itr.next();

					ArrayList<TermFrequency> termFreqList = tokemMap.get(key);
					strContent = "<"+key+"["+termFreqList.size()+"],(";
					out.write(strContent.getBytes());
					for(int j=0;j<termFreqList.size();j++){
						TermFrequency termfreq = termFreqList.get(j);
						strContent = termfreq.getDocId()+":"+termfreq.getFrequency();
						out.write(strContent.getBytes());

						if(j!=termFreqList.size()-1){
							strContent = ",";
							out.write(strContent.getBytes());
						}

					}
					strContent = ")>";
					out.write(strContent.getBytes());

				}
				strContent = ")#)";
				out.write(strContent.getBytes());

				//For each document, storing the frequency of the most frequent stem in the document
				strContent = "(#(";
				out.write(strContent.getBytes());
				TokenFrequency tokFreq;

				for(int i=0;i<fileFreqList.size();i++){

					tokFreq = fileFreqList.get(i);

					if(null!=tokFreq)
					{
						strContent = "<"+tokFreq.getDocId()+ ":"+tokFreq.getDoclen() + ":"+tokFreq.getToken()+":" + tokFreq.getFrequency()+">" ;
						out.write(strContent.getBytes());
						if(i!=fileFreqList.size()-1){
							strContent = ",";
							out.write(strContent.getBytes());
						}
					}
				}

				strContent = ")#)";
				out.write(strContent.getBytes());

				out.close();
				System.out.println("Index successfully saved to:"+filepath);


				long endTime = System.currentTimeMillis();

				System.out.println("Total time taken to Save Index(in ms) :" +(endTime - startTime) );
				System.out.println("Size of Index uncompressed(in bytes): "+index.length());
			}else{
				System.out.println("File :\""+filepath + "\" already esists please move the file to some other location and execute the program again." );
			}

		}catch(FileNotFoundException exp){
			System.out.println("Exception Occurred");
			exp.printStackTrace();
		}catch(IOException exp){
			System.out.println("Exception Occurred");
			exp.printStackTrace();
		}
		return;
	}


	public String getGammaCode(long number){
		String by = Long.toBinaryString(number);
		String gammaCode = "";
		by=by.substring(1);
		for(int i=0;i<by.length();i++){
			gammaCode = gammaCode + "1";
		}
		gammaCode = gammaCode + "0";
		gammaCode = gammaCode + by;


		//System.out.println("Gamma Code of "+ number+" is:"+gammaCode);
		//System.out.println("Gamma Code of "+ number+" is:"+Long.parseLong(gammaCode, 2));
		return gammaCode;

	}



	public String writeGammaString(long number,FileOutputStream out)throws IOException{
		String gammaCode = getGammaCode(number);
		String retVal= "";

		gammaCode = addPadding(gammaCode);
		//System.out.println("Gamma code with padding: "+gammaCode);

		while(gammaCode.length()>0){
			String piece = "";
			if(gammaCode.length()>8){
				piece = gammaCode.substring(0, 9);
				gammaCode=gammaCode.substring(8);
			}else{
				piece = gammaCode;
				gammaCode="";
			}

			byte numberByte = (byte)Integer.parseInt(piece, 2);

			out.write(numberByte);

			retVal=retVal+numberByte;
			//System.out.println("piece "+piece+" Number byte: "+numberByte);

		}

		//System.out.println(number+" in byte string is" + retVal);

		return retVal;
	}


	public String writeDeltaString(long number,FileOutputStream out)throws IOException{
		String deltaCode = getGammaCode(number);
		String retVal= "";

		deltaCode = addPadding(deltaCode);
		//System.out.println("Delta code with padding: "+deltaCode);

		while(deltaCode.length()>0){
			String piece = "";
			if(deltaCode.length()>8){
				piece = deltaCode.substring(0, 9);
				deltaCode=deltaCode.substring(8);
			}else{
				piece = deltaCode;
				deltaCode="";
			}

			byte numberByte = (byte)Integer.parseInt(piece, 2);
			out.write(numberByte);


			retVal=retVal+numberByte;
			//System.out.println("piece "+piece+" Number byte: "+numberByte);

		}

		//System.out.println(number+" in byte string is" + retVal);

		return retVal;
	}

	public String getDeltaCode(long number){
		String deltaCode = "";
		String by = Long.toBinaryString(number);

		String gammaCode = getGammaCode(by.length());
		by=by.substring(1);
		deltaCode = gammaCode + by;

		//System.out.println("Delta Code of "+ number+" is:"+deltaCode);
		//System.out.println("Delta Code of "+ number+" is:"+Long.parseLong(deltaCode, 2));
		return deltaCode;

	}


	public String addPadding(String input){

		int missingLen = input.length()%8;
		String padding = "";
		if(missingLen>0){
			for(int i=0;i<8-missingLen;i++){
				padding = padding + "0";
			}
		}

		input = padding+input;


		return input;

	}

}
