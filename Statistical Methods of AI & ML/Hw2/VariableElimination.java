import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


public class VariableElimination {
double MAX = 10000000;
double MIN = 0.00000001;
double logMin;
double logMax;
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
double finalResult=1d;
long totalNetSize =Long.MAX_VALUE;


	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		
		VariableElimination mainClass = new VariableElimination();
		mainClass.readInputs(args);
		mainClass.execProg();
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time taken to execute the program in ms "+ (endTime - startTime));
	}
	
	void execProg(){
		
		
		
		if(evidCount>0){
			ArrayList<Integer> curFunc;
			for(int i=0;i<funcs.size();i++){
				/*curFunc = funcs.get(i);
				ArrayList<Integer> prevRedFunc = new ArrayList<Integer>();
				
				for(int j=0;j<curFunc.size();j++){
					prevRedFunc.add(curFunc.get(j));
				}
				
				for(int j=0;j<curFunc.size();j++){
					if( evidence.keySet().contains(curFunc.get(j))){
						int redInd = prevRedFunc.indexOf(curFunc.get(j));
						int redVar = curFunc.get(j);
						int evidVal = evidence.get(redVar);
						
						if(evidVal>=paraDomainSize.get(redVar)){
							System.out.println("Error: Can not assign evidence "+evidVal+" to variable "+redVar + " domain size is "+paraDomainSize.get(redVar));
							return;
						}
						
						prevRedFunc = redData(i,redInd,evidVal,prevRedFunc);
						prevRedFunc.remove(curFunc.get(j));
					}
				}
				
				funcs.set(i, prevRedFunc);*/
				
				//nstantiating evidence on func
				
				instEvid(i);
				
				if(networkType.equals("BAYES")){
					normalise(data.get(i));
				}
				
				
				if(data.get(i).size()<2){
					//System.out.println("Found an orphan function " + i + " size "+ funcs.get(i).size() + " data size " + data.get(i).size() +" val " + data.get(i).get(0) );
					orphanData.add(data.get(i));
					//funcs.remove(i);
					//data.remove(i);
				}
			}
		}
		
		
		
		minOrder =totalNetSize;
		for(int i=0;i<totalNetPara;i++){
			//System.out.println("building adjList for node "+ i);
			HashSet<Integer> adjList = new HashSet<Integer>();
			HashSet<Integer> bktFunc = new HashSet<Integer>();
			for(int j=0;j<funcs.size();j++){
				//System.out.println("checking for func " +j);
				ArrayList<Integer> func = funcs.get(j);
				if(func.contains(i)){
					//System.out.println(" node " + i + " present in function "+ j);
					bktFunc.add(j);
					for(int k=0;k<func.size();k++){
						if(func.get(k)!= i){
							adjList.add(func.get(k));
						}
					}
				}
			}
			
			if(adjList.size()>0){
				graphAdj.put(i,adjList);
				long compl=1;
				for(Iterator<Integer> itr = adjList.iterator();itr.hasNext();){
					compl =compl * paraDomainSize.get(itr.next());
				}
				if(compl<minOrder){
					minOrder = compl;
					minOrderNode =i;
				}
			}
			
			if(bktFunc.size()>0){
				graphBkt.put(i, bktFunc);
			}
		}
		
		strtBucketElim();
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
		
		Double sum= input.get(0);
		for(int j=1;j<input.size();j++){
			sum = sumLogs(sum,input.get(j));
		}
		
		for(int j=0;j<input.size();j++){
			input.set(j,input.get(j)-sum);
		}
	}
	
	
	
	
	public void strtBucketElim(){
		
		System.out.println("Running bucket elimination");
		
		int remcount=0;
		
		while(graphAdj.keySet().size()>1){
			System.out.println("Removed "+ remcount+ " nodes Attempting to remove node "+minOrderNode + " with complexity "+minOrder);
			remNode(minOrderNode);
			remcount++;
			//printResultData();
		}
		
		
		//System.out.println("Order is "+minOrder+" only one node left " + minOrderNode + " No of values in it "+resultData.size() +" vals: "+resultData.get(0) +" "+resultData.get(1));
		
		//System.out.println(resultData);
		
		finalResult =resultData.get(0);
		for(int i=1;i<resultData.size();i++){
			finalResult =sumLogs(finalResult, resultData.get(i));
		}
		
		
		
		for(int i=0;i<orphanData.size();i++){
			finalResult = finalResult+orphanData.get(i).get(0);
			//System.out.println("Orphan data size and value "+ orphanData.get(i).size() + " value "+ orphanData.get(i).get(0));
		}
		
		finalResult = Math.exp(finalResult);
		
		if(networkType.equalsIgnoreCase("BAYES")){
			System.out.println(  "Probability of evidence = "+ finalResult );
		}else{
			System.out.println(  "Weight of evidence = "+  finalResult );
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
				File inputFile = new File(args[0]);
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
							
							if(dob>MAX){
								dob=MAX;
							}else if(dob<MIN){
								dob=MIN;
							}
							
							double val =Math.log(dob);
							//System.out.println("Before Log "+ tokens[j] + " after log "+val);
							funcData.add(val);
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
				
				System.out.println("Reading evidence from " + args[0]+".evid");
				inputFile = new File(args[0]+".evid");
				reader = new Scanner(inputFile);
				
				line = reader.nextLine();
					
				evidCount = Integer.parseInt(line.trim());
				if(evidCount>0){
				evidence = new HashMap<Integer, Integer>();
				
				for(int i=0;i<evidCount;i++){
					line = reader.nextLine();
					tokens = line.split(" ");
					
					StringTokenizer st = new StringTokenizer(line);
					//System.out.println(line);
					Integer evidVar = Integer.parseInt(st.nextToken());
					Integer evidVal = Integer.parseInt(st.nextToken());
					evidence.put(evidVar, evidVal);
				}
				
				System.out.println("Successfully read network properties from " + args[0]+".evid");
				
				
					
				}
				
		}catch(FileNotFoundException e){
			System.out.println("Not able to open Network parameters file in the path: "+args[0]+".evid");
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
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
		System.out.println(resultData);
	}
	
}
