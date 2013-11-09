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


public class LearnBN {

	int totParaNo = 0;
	int totSamples = 0;
	int testParas =0;
	int testSamples = 0;

	String [][] trainData;
	double functCounts [][];
	double [][] corrMat;
	HashMap<String, double[]> cpts  = new HashMap<String, double[]>();
	ArrayList<int[]> funcsLearned = new ArrayList<int[]>();

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

		LearnBN mainClass = new LearnBN();

		if(args.length<4){
			System.out.println("Error in executing program Usage: <uai> <train-f> <test-f> <output>");
			System.exit(0);
		}

		mainClass.readInputs(args);
		mainClass.execProg(args);

		long endTime = System.currentTimeMillis();

		System.out.println("Time taken to execute the program in ms "+ (endTime - startTime));
	}

	void calcCorr(int i,int j){

		double pi0 =1;
		double pj0 =1;
		double pi1 =1;
		double pj1 =1;
		double pij00 =1;
		double pij01 =1;
		double pij10 =1;
		double pij11 =1;

		for(int p=0;p<trainData.length;p++){
			int vali = Integer.parseInt(trainData[p][i]);
			int valj = Integer.parseInt(trainData[p][j]);

			if(vali == 0){
				pi0++;
			}

			if(valj == 0){
				pj0++;
			}

			if(vali == 1){
				pi1++;
			}

			if(valj == 1){
				pj1++;
			}

			if(vali == 0 && valj == 0){
				pij00++;
			}

			if(vali == 0 && valj == 1){
				pij01++;
			}

			if(vali == 1 && valj == 0){
				pij10++;
			}

			if(vali == 1 && valj == 1){
				pij11++;
			}

		}

		//System.out.println("("+i+","+j+") 0-"+pi0 +" 1-"+pi1+" 00-"+pij00+" 01-"+pij01+" 10-"+pij10+" 11-"+pij11+" "+trainData.length);

		pi0=pi0/(pi0+pi1);
		pi1 =1-pi0;
		pj0=pj0/(pj0+pj1);
		pj1=1-pj0;

		double tot = pij00+pij01+pij10+pij11;

		pij00 = pij00/tot;
		pij01 = pij01/tot;
		pij10 = pij10/tot;
		pij11 = pij11/tot;

		double cor =0;

		cor = pij00*Math.log(pij00/(pi0*pj0)) + pij01*Math.log(pij01/(pi0*pj1)) + pij10*Math.log(pij10/(pi1*pj0)) +pij11*Math.log(pij11/(pi1*pj1));

		corrMat[i][j] = cor;

		double[] cpt = new double[4];

		cpt[0] = pij00;
		cpt[1] = pij01;
		cpt[2] = pij10;
		cpt[3] = pij11;

		cpts.put(i+","+j, cpt);

		//System.out.println("("+i+","+j+") 0-"+pi0 +" 1-"+pi1+" 00-"+pij00+" 01-"+pij01+" 10-"+pij10+" 11-"+pij11+" "+cor);



	}



	public void execProg(String[] args){


		corrMat = new double[totParaNo][totParaNo];

		for(int i=0;i<totParaNo;i++){
			for(int j=0;j<totParaNo;j++){
				corrMat[i][j]=0;
			}
		}

		for(int i=0;i<totParaNo;i++){
			for(int j=i+1;j<totParaNo;j++){
				calcCorr(i,j);
			}
		}

		int maxi=0;
		int maxj=0;
		double max =0;
		for(int i=0;i<totParaNo;i++){
			for(int j=0;j<totParaNo;j++){
				if(corrMat[i][j]>max){
					max = corrMat[i][j];
					maxi = i;
					maxj = j;
				}
			}	
		}

		int[] newFunc =new int[2];
		newFunc[0] =maxi;
		newFunc[1] =maxj;
		funcsLearned.add(newFunc);

		HashSet<Integer> grpVar = new HashSet<Integer>();

		grpVar.add(maxi);
		grpVar.add(maxj);

		corrMat[maxi][maxj]=0;

		while(grpVar.size() != totParaNo){
			max=0;
			maxi =-1;
			maxj =-1;
			for(int i=0;i<totParaNo;i++){
				for(int j=i+1;j<totParaNo;j++){

					if( (grpVar.contains(i) && !grpVar.contains(j) ) || ( !grpVar.contains(i) && grpVar.contains(j) ) ){
						if(corrMat[i][j]>max){
							max = corrMat[i][j];
							maxi =i;
							maxj = j;
						}
					}

				}
			}

			corrMat[maxi][maxj]=0;
			grpVar.add(maxi);
			grpVar.add(maxj);

			newFunc =new int[2];
			newFunc[0] =maxi;
			newFunc[1] =maxj;
			funcsLearned.add(newFunc);


		}


		String outFile = printOutput(args);

		ParaLearnFOD claLogLike = new ParaLearnFOD(); 

		String[] paras = new String[3]; 
		paras[0] = outFile;
		paras[1] = args[1];
		paras[2] = args[2];		

		claLogLike.execLogLike(paras);

	}



	public String printOutput(String[] args){

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

			line+=funcsLearned.size();

			out.println(line);
			line="";

			for(int i=0;i<funcsLearned.size();i++){
				line=funcsLearned.get(i).length+" ";
				for(int p=0;p<funcsLearned.get(i).length;p++){
					line += funcsLearned.get(i)[p];
					if(p!=funcsLearned.get(i).length-1){
						line+=" ";
					}
				}
				out.println(line);
			}

			line="";

			for(int i=0;i<funcsLearned.size();i++){
				line ="";
				out.println();
				int[] func = funcsLearned.get(i);
				double[] cpt = cpts.get(func[0]+","+func[1]);
				line += cpt.length;
				out.println(line);
				line="";



				for(int p=0;p<cpt.length-1;p=p+2){
					//System.out.println(p+" "+functCounts[i].length);
					line ="";
					line += " "+cpt[p]+" "+ cpt[p+1];
					out.println(line);
					line="";
				}

			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file.getName();

	}


	public void instEvid(int funcNo){

		ArrayList<Double> oldData = data.get(funcNo);
		ArrayList<Integer> oldFunc = funcs.get(funcNo);


		ArrayList<Double> newData = new ArrayList<Double>();
		ArrayList<Integer> newFunc = new ArrayList<Integer>();

		int newSize = oldData.size();

		int remNodes = 0;
		for(int i=0;i<oldFunc.size();i++){
			if(!evidence.keySet().contains(oldFunc.get(i))){
				newFunc.add(oldFunc.get(i));
			}else{
				//System.out.println(newSize);
				newSize = newSize/paraDomainSize.get(oldFunc.get(i));
				//System.out.println(newSize);
				remNodes++;
			}
		}


		if(remNodes>0){
			//System.out.println("removed nodes count " +remNodes + " old data size " + oldData.size() +" new data size "+newSize);
			for(int i=0;i<newSize;i++){
				int id = getRelId(i,newFunc,oldFunc,newSize);
				//System.out.println(i+" "+id);
				newData.add(oldData.get(id));
			}

			//System.out.println("Instatiating func " + funcNo + " func before instatiating " + funcs.get(funcNo) +  " func after instatiating " + newFunc );	
			//System.out.println("Instatiating func " + funcNo + " data size before instatiating " + data.get(funcNo).size() +  " data size after instatiating " + newData.size() );

			funcs.set(funcNo, newFunc);
			data.set(funcNo, newData);


		}



	}

	int getRelId(int ipInd,ArrayList<Integer> newFunc,ArrayList<Integer> f,int newSize){

		HashMap<Integer, Integer> newVecMap= getVarMap(ipInd,newFunc,newSize);

		int ind=0;
		int mul=1;

		for(int i=f.size()-1;i>=0;i--){
			if(newFunc.contains(f.get(i))){
				ind += newVecMap.get(f.get(i))*mul;
				mul = mul * paraDomainSize.get(f.get(i));
			}else{
				ind += evidence.get(f.get(i))*mul;
				mul = mul * paraDomainSize.get(f.get(i));
			}

		}


		return ind;
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

			System.out.println("Looking for Fully observable data file in the path: "+args[1]);
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


			trainData = new String [totSamples][totParaNo];

			for(int i=0;i<totSamples;i++){
				line = reader.nextLine();

				tokens = line.split(" ");

				if(tokens.length <totParaNo){
					System.out.println("Problem reading data file, missing parameter at line: "+line);
					System.exit(0);
				}else{
					trainData[i] = tokens;
				}

			}


		}catch(FileNotFoundException exp){
			System.out.println("Error in opening file ");
			exp.printStackTrace();
		}catch (NumberFormatException exp) {
			exp.printStackTrace();
		}


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
