import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class QueryReqults {

	//Program Configuration variables
	int QUIT_AFTER = 5000;
	//Program Configuration variables
	HashMap<String,ArrayList<TermFrequency>> tokemMap = new HashMap<String,ArrayList<TermFrequency>>(); // to store tokens and frequency of token 
	ArrayList<TokenFrequency> fileFreqList = new ArrayList<TokenFrequency>(); // to store frequency of the most frequent stem,total number of word occurrences,doclen 
	ArrayList<String> StopWordList = new ArrayList<String>();
	String quriesPath ="";
	ArrayList<String> queryList = new ArrayList<String>();
	int collectionSize=0;
	long totalDoclen = 0;
	float avgDoclent =0;
	String dirPath = "";
	public static void main(String[] args){

		QueryReqults mainClass= new QueryReqults();

		long startTime = System.currentTimeMillis();
		//System.out.println("args are:"+ args[0]+ args[1]+ args[2]);
		mainClass.CreateIndex(args);
		long endTime = System.currentTimeMillis();

		System.out.println("");

		System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );

	}



	public void CreateIndex(String[] args){

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
			quriesPath = args[2];
		}else{
			quriesPath = System.getProperty("user.dir")+"\\hw3.queries";
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

		try{
			System.out.println("Looking for Quries file in the path: "+quriesPath);
			File QuriesFile = new File(quriesPath);
			reader = new Scanner(QuriesFile);
			String line;
			String query = "";
			while(reader.hasNextLine()) {

				line = reader.nextLine();

				if(line.length()!=0){
					if(line.startsWith("Q") && line.endsWith(":")){
						if(query.length()>0){
							queryList.add(query);
							query = "";
						}
					}else{
						query = query+ " "+line;
					}
				}
			}

			if(query.length()>0){
				queryList.add(query);
				query = "";
			}
			System.out.println("Quriey List:"+queryList);

			System.out.println("Processed Quries");

		}catch(FileNotFoundException e){
			System.out.println("Not able to open Quries file in the path: "+quriesPath);
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
			//saveIndex();
			//saveCompressedIndex();
			//System.out.println("Total number of inverted lists in the index are: "+tokemMap.size());

			//			printTokenMap("Reynolds");
			//			printTokenMap("NASA");
			//			printTokenMap("prandtl");
			//			printTokenMap("flow");
			//			printTokenMap("pressure");
			//			printTokenMap("boundary");
			//			printTokenMap("shock");
		}else{
			System.out.println(dirPath+" is not a valid directory path.");
		}

		collectionSize = noFiles;
		avgDoclent = totalDoclen / collectionSize;



		for(int i=0; i<queryList.size();i++)
		{
			processQuery(queryList.get(i));

		}

		String input=null;
		while(true){
			System.out.println("Please Enter Query(Press 0 to quit):");


			InputStreamReader streamReader = new InputStreamReader(System.in);
			BufferedReader bufReader = new BufferedReader(streamReader);


			try
			{

				input = bufReader.readLine();

			}
			catch(IOException exp){
				exp.printStackTrace();
			}
			if(input == null || input.length()==0 ||input.equalsIgnoreCase("0"))
				break;
			System.out.println("********** USER QURIES **********");
			processQuery(input);
			System.out.println("Query Executed successfully!! ");
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
			totalDoclen = totalDoclen +doclen;

		}catch(IOException exp){
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


	void processQuery(String line){
		for(int c=0; c<fileFreqList.size();c++){
			fileFreqList.get(c).setW1Score(0);
			fileFreqList.get(c).setW2Score(0);
		}
		ArrayList<String> indexedQuery = new ArrayList<String>();
		long tf=0;
		long maxTf=0;
		long df=0;
		long doclen=0;

		line = line.trim();
		line = line.toLowerCase();
		line = removeSGMLTags(line);
		line = removePossessives(line);
		line = removeCommas(line);


		if(null!=line && line.length()>0){
			String toekns[] = line.split(" ");
			//			System.out.println("Tokems:");
			//			for(int p=0;p<toekns.length;p++)
			//				System.out.print(toekns[p]+" " );
			//			
			//			System.out.println();
			for(String token : toekns ){

				if(token.endsWith(".")){
					token = token.substring(0, token.length()-1);

				}



				if(!StopWordList.contains(token)){
					//System.out.println("Token Before Stemming:"+token);

					Stemmer s = new Stemmer();

					for(int i=0; i <token.length(); i++)
					{
						s.add(token.charAt(i));
					}

					s.stem();
					//System.out.println("Token After Stemming:"+s.toString());
					if(s.toString().length()>0)
						indexedQuery.add( s.toString());
				}
			}

			//System.out.println("Indexed Query is : "+indexedQuery);
			for(int k=0;k<indexedQuery.size();k++){
				String searchToken = indexedQuery.get(k);
				ArrayList<TermFrequency> tokenPostings = tokemMap.get(searchToken);
				for(int j=0;j<fileFreqList.size();j++){
					long docId = fileFreqList.get(j).getDocId();
					tf=0;
					maxTf=fileFreqList.get(j).getFrequency();

					doclen = fileFreqList.get(j).getDoclen();
					if (tokenPostings!=null){
						df=tokenPostings.size();
						for(int l=0;l<tokenPostings.size();l++){
							if(tokenPostings.get(l).getDocId() == docId){
								tf = tokenPostings.get(l).getFrequency();
							}
						}
					}
					double  w1Score = getW1Score(tf,maxTf,df);
					double w2Score = getW2Score(tf,maxTf,df,doclen);

					fileFreqList.get(j).setW1Score(fileFreqList.get(j).getW1Score() + w1Score );
					//System.out.println("Adding w1Score" + w1Score);
					fileFreqList.get(j).setW2Score(fileFreqList.get(j).getW2Score() + w2Score );
				}


			}


			TokenFrequency [] w1Top = fetchTopByW1();
			TokenFrequency [] w2Top = fetchTopByW2();
			String processedQuery = "";
			for(int k=0;k<indexedQuery.size();k++){
				processedQuery=processedQuery + " "+ indexedQuery.get(k);
			}

			System.out.println("Query:"+line);
			System.out.println("Indexed Query:"+processedQuery);
			System.out.println("Top 10 results by TW1 Standard");
			System.out.println("Rank : W1Score : DocId : Headline");
			for(int k=0;k<10;k++){
				String Headline = "";
				String cranfieldName = getCranfieldname(w1Top[k].getDocId());
				String filePath = dirPath + "/"+cranfieldName;
				try{
					File hitFile = new File(filePath);
					Scanner reader = new Scanner(hitFile);
					String fileLine;

					boolean record = false;
					while(reader.hasNextLine()) {
						fileLine = reader.nextLine();
						if(fileLine.equalsIgnoreCase("</TITLE>")){
							record = false;
							break;
						}

						if(record){
							Headline = Headline + " " + fileLine;
						}


						if(fileLine.equalsIgnoreCase("<TITLE>")){
							record = true;
						}



					}



				}catch(IOException exp){
					exp.printStackTrace();
				}

				System.out.println(""+(k+1) + " : "+ w1Top[k].getW1Score()+" : "+cranfieldName +" : " + Headline );
			}

			System.out.println("Top 10 results by TW2 Standard");
			System.out.println("Rank : W2Score : DocId: Headline");
			for(int k=0;k<10;k++){
				String Headline = "";
				String cranfieldName = getCranfieldname(w2Top[k].getDocId());
				String filePath = dirPath + "/"+cranfieldName;
				try{
					File hitFile = new File(filePath);
					Scanner reader = new Scanner(hitFile);
					String fileLine;

					boolean record = false;
					while(reader.hasNextLine()) {
						fileLine = reader.nextLine();
						if(fileLine.equalsIgnoreCase("</TITLE>")){
							record = false;
							break;
						}

						if(record){
							Headline = Headline + " " + fileLine;
						}


						if(fileLine.equalsIgnoreCase("<TITLE>")){
							record = true;
						}



					}



				}catch(IOException exp){
					exp.printStackTrace();
				}
				System.out.println(""+(k+1) + " : "+ w2Top[k].getW2Score()+" : "+cranfieldName +" : "+ Headline );
			}


		}


	}

	TokenFrequency [] fetchTopByW1(){
		TokenFrequency[] top = new TokenFrequency[10];
		int filled=0;
		for(int j=0;j<fileFreqList.size();j++){
			if(filled>0){
				int index = checkW1(0,filled-1,fileFreqList.get(j).getW1Score(),top);

				for(int i = filled-1;i	> index;i-- ){
					top[i] = top[i-1];
				}
				top[index] = new TokenFrequency(fileFreqList.get(j).getDocId(), fileFreqList.get(j).getW1Score(),fileFreqList.get(j).getW2Score());
			}else{
				top[0] = new TokenFrequency(fileFreqList.get(j).getDocId(), fileFreqList.get(j).getW1Score(),fileFreqList.get(j).getW2Score());
			}

			if(filled<10)
				filled++;
		}

		return top;
	}


	public int checkW1(int begInd, int endInd,double w1Score,TokenFrequency[] top){

		int retVal=-1;

		if(endInd==0){
			return 0;
		}

		int midIndex = begInd + (endInd-begInd)/2;

		TokenFrequency tokFreq =  top[midIndex];

		if (endInd==begInd ){
			return midIndex;

		}else if(tokFreq.getW1Score() == w1Score){

			return midIndex;

		}else if(tokFreq.getW1Score() < w1Score){
			return checkW1(begInd,midIndex,w1Score,top);

		}else if(tokFreq.getW1Score() > w1Score){
			return checkW1(midIndex+1,endInd,w1Score,top);
		}

		return retVal;
	}


	TokenFrequency [] fetchTopByW2(){
		TokenFrequency[] top = new TokenFrequency[10];
		int filled=0;
		for(int j=0;j<fileFreqList.size();j++){

			if(filled>0){
				int index = checkW2(0,filled-1,fileFreqList.get(j).getW2Score(),top);

				for(int i = filled-1;i	> index;i-- ){
					top[i] = top[i-1];
				}
				top[index] = new TokenFrequency(fileFreqList.get(j).getDocId(), fileFreqList.get(j).getW1Score(),fileFreqList.get(j).getW2Score());
			}else{
				top[0] = new TokenFrequency(fileFreqList.get(j).getDocId(), fileFreqList.get(j).getW1Score(),fileFreqList.get(j).getW2Score());
			}

			if(filled<10)
				filled++;
		}

		return top;
	}


	public int checkW2(int begInd, int endInd,double w2Score,TokenFrequency[] top){

		int retVal=-1;
		if(endInd==0){
			return 0;
		}

		int midIndex = begInd + (endInd-begInd)/2;

		TokenFrequency tokFreq =  top[midIndex];

		if (endInd==begInd){
			return midIndex;

		}else if(tokFreq.getW2Score() == w2Score ){

			return midIndex;

		}else if(tokFreq.getW2Score() < w2Score){
			return checkW2(begInd,midIndex,w2Score,top);

		}else if(tokFreq.getW2Score() > w2Score){
			return checkW2(midIndex+1,endInd,w2Score,top);
		}

		return retVal;
	}

	double getW1Score(long tf,long maxTf,long df){

		if(df==0)
			return 0;

		double w1Score=  ( 0.4 + 0.6*(Math.log(tf+0.5)/Math.log(maxTf+1)) ) * ( Math.log(collectionSize/df) / Math.log(collectionSize));

		return w1Score;
	}

	double getW2Score(long tf,long maxTf,long df,long docLen){

		if(df==0)
			return 0;

		double w2Score=  ( 0.4 + 0.6*(tf/(tf+0.5+1.5*(docLen/avgDoclent))) ) * ( Math.log(collectionSize/df) / Math.log(collectionSize));

		return w2Score;
	}


	String getCranfieldname(long docid){
		String docName = ""+docid;

		switch(4-docName.length()){
		case 0 : docName = "cranfield"+docid; break;
		case 1 : docName = "cranfield"+"0"+docid; break;
		case 2 : docName = "cranfield"+"00"+docid; break;
		case 3 : docName = "cranfield"+"000"+docid; break;
		default: docName = "cranfield"+docid; break;
		}


		return docName;
	}

}
