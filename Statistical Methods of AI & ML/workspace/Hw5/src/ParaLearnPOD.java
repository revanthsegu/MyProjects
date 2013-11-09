import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


public class ParaLearnPOD {

	int totParaNo = 0;
	int totSamples = 0;
	int testParas =0;
	int testSamples = 0;

	ArrayList<int[]> trainData;
	ArrayList<Double> trainWt = new ArrayList<Double>();
	double functCounts [][];	


	double MAX = 10000000;
	double MIN = 0.0000001;
	double logMin;
	double logMax;
	File inputFile;
	String networkType;
	Scanner reader;
	int totalNetPara=0;
	HashMap<Integer, Integer> paraDomainSize;
	int totFuncNo=0;
	int newFuncCount=0;
	ArrayList<ArrayList<Integer>> funcs = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
	HashMap<Integer, Integer> evidence;
	ArrayList<ArrayList<Double>> redData = new ArrayList<ArrayList<Double>>();
	int evidCount;
	HashMap<Integer, HashSet<Integer>> graphAdj = new HashMap<Integer, HashSet<Integer>>();
	HashMap<Integer, HashSet<Integer>> graphBkt = new HashMap<Integer, HashSet<Integer>>();
	long minOrder;
	int minOrderNode;
	ArrayList<Integer> resultFunc = new ArrayList<Integer>();
	ArrayList<Double> resultData = new ArrayList<Double>();
	ArrayList<ArrayList<Double>> orphanData = new ArrayList<ArrayList<Double>>();
	double finalResult=0d;
	long totalNetSize =Long.MAX_VALUE;


	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		ParaLearnPOD mainClass = new ParaLearnPOD();

		if(args.length<4){
			System.out.println("Error in executing program Usage: <uai> <train-f> <test-f> <output>");
			System.exit(0);
		}

		mainClass.readInputs(args);
		mainClass.execProg(args);

		long endTime = System.currentTimeMillis();

		System.out.println("Time taken to execute the program in ms "+ (endTime - startTime));
	}

	public void execProg(String[] args){

		functCounts = new double[totFuncNo][];

		for(int i=0;i<totFuncNo;i++){

			ArrayList<Integer> func = funcs.get(i);
			functCounts[i] = new double[(int)Math.pow(2, funcs.get(i).size())];

			for(int p=0;p<functCounts[i].length;p++){
				functCounts[i][p] =0;
			}

			for(int p=0;p<trainData.size();p++){
				int[] dataVect =  trainData.get(p);
				int ind =0;
				String indStr ="";
				for(int z=0;z<func.size();z++){
					indStr += dataVect[func.get(z)];
				}

				ind =Integer.parseInt(indStr,2);
				functCounts[i][ind] +=trainWt.get(p);
			}

			for(int p=0;p<functCounts[i].length;p++){
				if(p%2==0){
					if(functCounts[i][p]+functCounts[i][p+1] !=0){
						functCounts[i][p] = functCounts[i][p]/(functCounts[i][p]+functCounts[i][p+1]);
					}else{
						functCounts[i][p] = 0.5;
					}
				}else{
					functCounts[i][p] = 1- functCounts[i][p-1];
				}

			}

		}

		printOutput(args);

		compLogLike(args);

	}



	public void printOutput(String[] args){

		File file;

		if(args[3].endsWith(".uai")){
			file = new File(args[3]);
		}else{
			file = new File(args[3]+".uai");
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getName(), true)));
			String line="";
			out.println("BAYES");
			line="";
			line+=totalNetPara;

			out.println(line);
			line="";

			for(int i=0;i<totalNetPara;i++){
				line +="2 ";
			}
			out.println(line);
			line="";

			line+=totFuncNo;

			out.println(line);
			line="";

			for(int i=0;i<totFuncNo;i++){
				line= funcs.get(i).size()+" ";
				for(int p=0;p<funcs.get(i).size();p++){
					line += funcs.get(i).get(p);
					if(p!=funcs.get(i).size()-1){
						line+=" ";
					}
				}
				out.println(line);
			}

			line="";

			for(int i=0;i<totFuncNo;i++){
				line ="";
				out.println();
				line += functCounts[i].length;
				out.println(line);
				line="";

				for(int p=0;p<functCounts[i].length-1;p=p+2){
					//System.out.println(p+" "+functCounts[i].length);
					line ="";
					line += " "+functCounts[i][p]+" "+ functCounts[i][p+1];
					out.println(line);
					line="";
				}

			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}





	public void normalise(ArrayList<Double> input){
		if(input.size()>1){
			Double sum= input.get(0);
			for(int j=1;j<input.size();j++){
				sum = sumLogs(sum,input.get(j));
			}

			for(int j=0;j<input.size();j++){
				input.set(j,input.get(j)-sum);
			}
		}
	}






	public void remNode(int nodeNo){
		HashSet<Integer> bktFunc = graphBkt.get(nodeNo);

		if(bktFunc.size()>0){

			Iterator<Integer> itr = bktFunc.iterator();

			int fId;

			if(itr.hasNext()){
				fId = itr.next();
				resultFunc = funcs.get(fId);
				resultData = data.get(fId);
			}


			while(itr.hasNext()){
				fId = itr.next();
				multiplyFuncs(resultFunc,resultData,funcs.get(fId),data.get(fId));
			}



			//SUMOUT

			sumOut(nodeNo);

			graphBkt.remove(nodeNo);
			graphAdj.remove(nodeNo);

			funcs.add(resultFunc);
			data.add(resultData);

			newFuncCount++;

			minOrder=totalNetSize;
			minOrderNode=-1;

			updFuncMap(bktFunc,totFuncNo+newFuncCount-1,resultFunc);

			if(data.get(totFuncNo+newFuncCount-1).size()<2){
				//System.out.println("Found an orphan function " + i + " size "+ funcs.get(i).size() + " data size " + data.get(i).size() +" val " + data.get(i).get(0) );
				orphanData.add(data.get(totFuncNo+newFuncCount-1));
				//funcs.remove(i);
				//data.remove(i);
			}

			updOrder();

		}


	}


	public void updOrder(){
		Set<Integer> keyset = graphBkt.keySet();
		Iterator<Integer> keyItr =keyset.iterator();

		while(keyItr.hasNext()){
			int keyVal = keyItr.next();
			HashSet<Integer>  set =  graphBkt.get(keyVal);

			long compl=1;
			int count =0;
			//System.out.println(paraDomainSize);
			HashSet<Integer> bukElems =new HashSet<Integer>();
			for(Iterator<Integer> itr1 = set.iterator();itr1.hasNext();){
				//System.out.println("keyVal " +keyVal+" "+set.size() +" set "+set + " count " + count +" size "+ compl+" total funcs "+funcs.size());

				ArrayList<Integer> func = funcs.get(itr1.next());

				for(int i=0;i<func.size();i++){
					bukElems.add(func.get(i));
				}
			}


			//System.out.println(paraDomainSize);
			for(Iterator<Integer> itr1 = bukElems.iterator();itr1.hasNext();){
				//System.out.println("keyVal " +keyVal+" "+bukElems.size() +" bukElems "+bukElems + " count " + count +" size "+ compl);
				compl = compl*paraDomainSize.get(itr1.next());
			}
			if(compl< minOrder){
				minOrder = compl;
				minOrderNode =keyVal;
			}
		}
	}

	public void sumOut(int nodeNo){

		int curSize = resultData.size();

		int newSize = curSize/paraDomainSize.get(nodeNo);
		ArrayList<Double> newData = new ArrayList<Double>();
		ArrayList<Integer> newFunc = new ArrayList<Integer>();
		for(int i=0;i<resultFunc.size();i++){
			newFunc.add(resultFunc.get(i));
		}

		//System.out.println("Before sum out");
		//System.out.println(resultFunc);
		//System.out.println(resultData);

		newFunc.remove(new Integer(nodeNo));

		for(int i=0;i<newSize;i++){
			ArrayList<Integer> ids = getAllIds(i,nodeNo,newFunc,resultFunc,newSize);
			//System.out.println(i+" "+ids);
			Double sum= resultData.get(ids.get(0));
			for(int j=1;j<ids.size();j++){
				sum = (sumLogs(sum,resultData.get(ids.get(j))));
			}
			newData.add(sum);
		}

		resultFunc = newFunc;
		resultData = newData;

		if(networkType.equalsIgnoreCase("BAYES")){
			//System.out.println("Normalising !!");

			Double sum= resultData.get(0);
			for(int j=1;j<resultData.size();j++){
				sum = sumLogs(sum,resultData.get(j));
			}

			for(int j=0;j<resultData.size();j++){
				resultData.set(j,resultData.get(j)-sum);
			}
		}

		//System.out.println("After sum out");
		//System.out.println(resultFunc);
		//System.out.println(resultData);


	}

	ArrayList<Integer> getAllIds(int ipInd,int nodeNo,ArrayList<Integer> newFunc,ArrayList<Integer> f,int newSize){
		ArrayList<Integer> ids = new ArrayList<Integer>();

		HashMap<Integer, Integer> newVecMap= getVarMap(ipInd,newFunc,newSize);

		int ind=0;
		int mul=1;
		int addVal=1;

		for(int i=f.size()-1;i>=0;i--){
			if(newFunc.contains(f.get(i))){
				ind += newVecMap.get(f.get(i))*mul;
				mul = mul * paraDomainSize.get(f.get(i));
			}else{
				addVal = mul;
				mul = mul * paraDomainSize.get(f.get(i));
			}

		}

		for(int i=0;i<paraDomainSize.get(nodeNo);i++){
			ids.add(ind+i*addVal);
		}

		return ids;
	}

	public void updFuncMap(HashSet<Integer> bktFunc,int newFunNo,ArrayList<Integer> resultFunc){
		Iterator<Integer> itr = bktFunc.iterator();
		int remId;
		Set<Integer> keyset;
		Iterator<Integer> keyItr;
		if(resultFunc.size()!=0){
			while(itr.hasNext()){
				remId = itr.next();
				keyset = graphBkt.keySet();
				keyItr =keyset.iterator();

				while(keyItr.hasNext()){
					int keyVal = keyItr.next();
					HashSet<Integer>  set =  graphBkt.get(keyVal);
					set.remove(remId);
					if(resultFunc.contains(keyVal)){
						set.add(newFunNo);
					}

				}

				keyset = graphAdj.keySet();
				keyItr =keyset.iterator();

				while(keyItr.hasNext()){
					int keyVal = keyItr.next();
					HashSet<Integer>  set =  graphAdj.get(keyVal);
					set.remove(remId);
					if(resultFunc.contains(keyVal)){
						set.add(newFunNo);
					}

				}
			}





		}else{
			orphanData.add(resultData);
			System.out.println("Found an orphan data "+ resultFunc);
		}

	}

	public void multiplyFuncs(ArrayList<Integer> f1,ArrayList<Double> d1,ArrayList<Integer> f2,ArrayList<Double> d2){
		TreeSet <Integer>intSet = new TreeSet<Integer>();
		ArrayList<Integer> f3;
		ArrayList<Double> d3 = new ArrayList<Double>();
		int totalDataSize =1;
		for(int i=0;i<f1.size();i++){
			intSet.add(f1.get(i));
		}

		for(int i=0;i<f2.size();i++){
			intSet.add(f2.get(i));
		}


		f3 = new ArrayList<Integer>(intSet);
		for(int i=0;i<f3.size();i++){
			totalDataSize = totalDataSize * paraDomainSize.get(f3.get(i));
		}

		//System.out.println(intSet+" "+totalDataSize);

		for(int i=0;i<totalDataSize;i++){
			HashMap<Integer, Integer>varMap;
			varMap = getVarMap(i,f3,totalDataSize);

			int f1Ind = getInd(varMap, f1);
			int f2Ind = getInd(varMap, f2);

			double val = d1.get(f1Ind) + d2.get(f2Ind);  
			d3.add(val);
			//System.out.println("f1,"+f1Ind +" "+d1.get(f1Ind)+ " f2,"+f2Ind+" "+d2.get(f2Ind) +" result "+val);
		}

		resultFunc = f3;
		resultData =d3;

		//printMulti(f1,d1,f2,d2,f3,d3);
	}

	public void printMulti(ArrayList<Integer> f1,ArrayList<Double> d1,ArrayList<Integer> f2,ArrayList<Double> d2,ArrayList<Integer> f3,ArrayList<Double> d3){
		System.out.println(f1);
		System.out.println(d1);
		System.out.println(f2);
		System.out.println(d2);
		System.out.println(f3);
		System.out.println(d3);
	}

	public int getInd(HashMap<Integer, Integer> varVal,ArrayList<Integer> f){
		int ind=0;
		int mul=1;
		for(int i=f.size()-1;i>=0;i--){
			ind += varVal.get(f.get(i))*mul;
			mul = mul * paraDomainSize.get(f.get(i));
		}

		return ind;		
	}

	public HashMap<Integer, Integer> getVarMap(int ind,ArrayList<Integer> f,int datSize){
		HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();

		int quo;
		int rem =ind;

		for(int i=0;i<f.size();i++){
			datSize = datSize/paraDomainSize.get(f.get(i));
			quo = rem/datSize;
			rem = rem%datSize;
			//System.out.println("i "+" quo "+quo +" rem "+rem);

			if(quo >paraDomainSize.get(f.get(i))){
				System.out.println("Error: got val " + quo +" for param " + f.get(i) +" domain size is " + paraDomainSize.get(f.get(i)));
				return null;
			}
			ret.put(f.get(i),quo);

		}
		//ret.put(f.get(f.size()-1), rem);
		//System.out.println(ret);
		return ret;
	}


	public double sumLogs(double a, double b){
		double c;

		if(a>b){
			c=  a + Math.log(1+Math.exp(b-a));
		}else{
			c=  b + Math.log(1+Math.exp(a-b));
		}
		//System.out.println(a+"+"+b+"="+c);
		return c;
	}	


	public ArrayList<Integer> redData(int funcId,int redInd,int evidVal,ArrayList<Integer> prevRedFunc){



		ArrayList<Double> curData = data.get(funcId);
		System.out.println("func Id: "+funcId+" redInd:"+redInd+" evidVal:"+evidVal+" prevRedFunc:"+prevRedFunc +" curData.size "+curData.size());
		System.out.println(curData);
		int dataSize = curData.size();
		int remCount = 0;
		int multiVal =1;
		int varDomSize = paraDomainSize.get(prevRedFunc.get(redInd));


		for(int i=redInd;i<prevRedFunc.size();i++){
			multiVal = multiVal * paraDomainSize.get(prevRedFunc.get(i));
		}

		int firstval = evidVal*multiVal;
		int lastVal = firstval + multiVal;
		int incrVal = ((varDomSize-1)*multiVal);

		for(int i=0; i<dataSize;i++){
			System.out.println("i "+i+" firstval "+ (firstval)+" lastval "+(lastVal)+" multival "+multiVal+" incrval "+incrVal);
			if(i<firstval){
				data.remove(i-remCount);
				remCount++;
			}

			if(i==lastVal){
				firstval = lastVal+incrVal ;
				lastVal = firstval + incrVal;
			}
		}


		if( (dataSize/(varDomSize-1)) != curData.size()  ) {
			System.out.println("Error: data size discrepancy for func id " +funcId);
		}


		return prevRedFunc;
	}


	void readInputs(String args[]){
		try{

			logMax = Math.log(MAX);
			logMin = Math.log(MIN);
			System.out.println("Looking for Network parameters file in the path: "+args[0]);
			inputFile = new File(args[0]);
			reader = new Scanner(inputFile);
			String line="";
			String[] tokens;

			line = reader.nextLine();
			networkType = line;

			line = reader.nextLine();
			totalNetPara = Integer.parseInt(line);

			line = reader.nextLine();
			tokens =line.split(" ");

			paraDomainSize = new HashMap<Integer, Integer>();


			//reading domain sizes of vars
			for(int i=0;i<totalNetPara;i++){
				paraDomainSize.put(i, Integer.parseInt(tokens[i].trim()));
				//System.out.println("putting "+i +" " +tokens[i] );
			}
			//System.out.println(paraDomainSize.size());
			//System.out.println(paraDomainSize);

			line = reader.nextLine();
			totFuncNo = Integer.parseInt(line);


			//reading functions

			for(int i=0;i<totFuncNo;i++){
				line = reader.nextLine();
				tokens = line.split(" ");

				if(tokens.length<2)
					tokens = line.split("\t");

				int funcSize = Integer.parseInt(tokens[0].trim());
				ArrayList<Integer> func = new ArrayList<Integer>();
				//System.out.println(func.length +" "+tokens.length);

				for(int j=0;j<funcSize;j++){
					func.add(Integer.parseInt(tokens[j+1].trim()));
				}

				funcs.add(func);
			}
			System.out.println("Reading functions complete total functions read " + funcs.size());
			//System.out.println(paraDomainSize);
			//printFuncs();
			line =reader.nextLine(); // skipping the gap line between completion of functions and starting of data 
			//reading data
			for(int i=0;i<totFuncNo;i++){
				line="";

				while(line.length()==0){
					line =reader.nextLine();
				}
				int dataCountFile = Integer.parseInt(line.trim());

				int countFromFunc=1;
				ArrayList<Integer> func = funcs.get(i);
				for(int j=0;j<func.size();j++){
					countFromFunc = countFromFunc* paraDomainSize.get(func.get(j));
				}

				if(countFromFunc!=dataCountFile){
					System.out.println("Error: Expected " +  countFromFunc +" found " +dataCountFile);
					return;
				}

				int countDataRead =0;
				ArrayList<Double> funcData = new ArrayList<Double>();
				do{
					//System.out.println("line is " + line);
					line="";
					while(line.length()==0){
						line =reader.nextLine();
					}
					tokens = line.split(" ");


					for(int j=0;j<tokens.length;j++){
						if(tokens[j].trim().length()>0){
							double dob = Double.parseDouble(tokens[j].trim());

							//System.out.println("Before Log "+ tokens[j] + " after log "+val);
							funcData.add(dob);
							countDataRead++;
						}
					}

				}while(countDataRead<dataCountFile);
				data.add(funcData);
				//System.out.println(funcData);
				if(countDataRead!=dataCountFile){
					System.out.println("Error: present entries" +  countDataRead +" expected entries " +dataCountFile );
					return;
				}

			}


			System.out.println("Successfully read network properties from " + args[0]);

			////////////// 

			System.out.println("Looking for Patially observable data file in the path: "+args[1]);
			inputFile = new File(args[1]);


			reader = new Scanner(inputFile);

			line = reader.nextLine();
			tokens = line.split(" ");

			if(tokens.length !=2){
				System.out.println("Problem reading data file, missing parameter at line: "+line);
				System.exit(0);
			}


			totParaNo = Integer.parseInt(tokens[0]);
			totSamples = Integer.parseInt(tokens[1]);


			trainData = new ArrayList<int[]>();

			for(int i=0;i<totSamples;i++){
				line = reader.nextLine();

				tokens = line.split(" ");

				if(tokens.length <totParaNo){
					System.out.println("Problem reading data file, missing parameter at line: "+line);
					System.exit(0);
				}else{
					ArrayList<Integer> missVal = new ArrayList<Integer>();



					for(int k=0;k<totParaNo;k++){
						if(!tokens[k].equalsIgnoreCase("0") && !tokens[k].equalsIgnoreCase("1") ){
							missVal.add(k);
						}
					}

					if(missVal.size() ==0){
						int[] data  = new int[totParaNo];
						for(int k=0;k<totParaNo;k++){
							data[k] =  Integer.parseInt(tokens[k]);
						}
						trainData.add(data);
						trainWt.add(1D);
					}else{

						//DANCE BABY DANCE

						//System.out.println("Found a missing vals in input "+line);
						//System.out.println("missing ids "+ missVal);


						int newSamps [][] = new int[(int)Math.pow(2, missVal.size())][totParaNo];
						Double [] wts  = new Double[newSamps.length];



						VEPOD ve;

						long strtTime = System.currentTimeMillis();

						ve = new VEPOD();
						ve.exec(args[0], tokens,missVal);

						Double veVal = 1D;



						long endTime = System.currentTimeMillis();

						//System.out.println("Time taken to run BE code in sec "+((endTime-strtTime)/1000));


						for(int p=0;p<newSamps.length;p++){
							ArrayList<Integer> bin = getBinary(p,missVal.size());
							int [] datasamp =new int[totParaNo-2];

							for(int z=0;z<totParaNo;z++){

								if(tokens[z].equalsIgnoreCase("0") || tokens[z].equalsIgnoreCase("1") ){
									newSamps[p][z] = Integer.parseInt(tokens[z]);
								}else{
									newSamps[p][z] = bin.get(missVal.indexOf(z));
								}

								if(z< totParaNo-2){
									datasamp[z] = newSamps[p][z];
								}



							}


							//								 System.out.println("data sample is "+datasamp.length);
							wts[p] = veVal +i;

							//wts[p] = veVal;


						}

						Double totwt = 0D;
						for(int p=0;p<wts.length;p++){
							totwt+=wts[p];
						}

						for(int p=0;p<newSamps.length;p++){
							trainData.add(newSamps[p]);
							trainWt.add(wts[p]/totwt);
						}

					}


				}

			}


		}catch(FileNotFoundException exp){
			System.out.println("Error in opening file ");
			exp.printStackTrace();
		}catch (NumberFormatException exp) {
			exp.printStackTrace();
		}


	}

	ArrayList<Integer> getBinary(int num,int len){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		int rem =0;
		int inp =num;

		for(int i=len-1;i>=0;i--){
			int divVal = (int)Math.pow(2, i);
			rem = inp % divVal;
			int curVal = inp/divVal;
			ret.add(curVal);
			inp =rem;
		}

		return ret;
	}

	public void compLogLike(String[] args){

		System.out.println("Looking for test data file in the path: "+args[2]);
		inputFile = new File(args[2]);

		try {
			reader = new Scanner(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String  line="";
		String [] tokens;

		line = reader.nextLine();
		tokens = line.split(" ");

		if(tokens.length !=2){
			System.out.println("Problem reading data file, missing parameter at line: "+line);
			System.exit(0);
		}


		testParas = Integer.parseInt(tokens[0]);
		testSamples = Integer.parseInt(tokens[1]);

		if(totParaNo != testParas){
			System.out.println("traing data and test data are inconsistent with test data"+line);
			System.exit(0);
		}



		int [] testsamp = new int [testParas];

		double diffLog=0;

		for(int i=0;i<testSamples;i++){

			double oldProb=0;
			double newProb=0;


			line = reader.nextLine();

			tokens = line.split(" ");

			if(tokens.length <testParas){
				System.out.println("Problem reading data file, missing parameter at line: "+line);
				System.exit(0);
			}else{
				for(int p=0;p<testParas;p++){
					testsamp[p] = Integer.parseInt(tokens[p]);	
				}
			}

			String indStr="";
			for(int p=0;p<totFuncNo;p++){

				oldProb=0;
				newProb=0;
				indStr="";

				for(int z=0;z<funcs.get(p).size();z++){
					indStr +=testsamp[funcs.get(p).get(z)];
				}

				int ind = Integer.parseInt(indStr,2);
				//System.out.println(p +" "+ind);
				oldProb =  data.get(p).get(ind);
				newProb =  functCounts[p][ind];

				//System.out.println(" old prob "+oldProb +" new prob "+newProb);


				double logVal1 =0;
				if(oldProb!=0 && oldProb!=1){
					logVal1 = oldProb*Math.log(oldProb)+(1-oldProb)*Math.log(1-oldProb);
				}

				double logVal2=0;
				if(newProb!=0 && newProb!=1){
					logVal2 = newProb*Math.log(newProb)+(1-newProb)*Math.log(1-newProb);
				}

				//System.out.println(logVal1 +" "+logVal2);

				if(logVal1>logVal2){
					diffLog+= logVal1 - logVal2;
				}else{
					diffLog+= logVal2 - logVal1;
				}

			}



		}

		System.out.println("\n--- log likelihood difference = "+diffLog+" ---\n");

	}

	public void printFuncs(){
		System.out.println("Printing all functions");
		for(int i=0;i<funcs.size();i++){
			ArrayList<Integer> func = funcs.get(i);
			System.out.print("Printing func "+i +":");
			for(int j=0;j<func.size();j++){
				System.out.print(func.get(j)+" ");
			}
			System.out.println();
		}
	}

	public void printResultData(){
		System.out.println(resultFunc);
		//System.out.println(resultData);
	}

}
