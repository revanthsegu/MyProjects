import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/*
0 - Request
1 - commit
2 - Withdraw
3 - grant
4 - Ack
5 - Sync
 */


public class ProcessSteps {
	Scanner reader;
	Integer clock= new Integer(0);
	int processNo;
	String[] netIps;
	int [] netPorts; 
	int totalProcess;
	HashMap<Integer, ArrayList<String>> messageBuff = new HashMap<Integer, ArrayList<String>>();
	HashMap<Integer, ArrayList<String>> outMessages = new HashMap<Integer, ArrayList<String>>();
	int[] statuses;
	int succAccess=0;
	int failAcess=0;
	int holdTime=1;
	TreeNode root;
	int pauseCount=0;
	ArrayList<Long> accessTime = new ArrayList<Long>();


	ConnectionManager manager;
	String inputFilePath;


	boolean syncCompl = true;
	ArrayList<Integer> sendUpdNext = new ArrayList<Integer>();

	boolean pauseNext = false;
	int servNo;
	int cliNo;
	int totReq;
	int timeUnit;
	ArrayList<Integer> failingNodes; 

	int totMessSent=0;
	int totMessRecv=0;
	int [] dataObjs = new int[4];
	boolean [] status = new boolean[4];
	int [] versions = new int[4];
	boolean [] grants;
	int acksRecd=0;
	HashMap<Integer, ArrayList<String>> objReqs = new HashMap<Integer, ArrayList<String>>();

	int totReqSent=0;
	int totRepRec=0;
	int totRelSent=0;
	int totFailRec=0;
	int totEnqRec=0;
	int totYldSent =0;

	int doBjNo=-1;
	int incrVal=-1;

	public ProcessSteps(String[] netIps, int[]netPorts,int processNo, int totalProcess,String inputFile) {
		try{
			//System.out.println("Inside Process Steps construct");
			this.netIps = netIps;
			this.netPorts = netPorts;
			this.processNo=processNo;
			this.totalProcess=totalProcess;
			this.inputFilePath=inputFile;


			for(int i=0;i<totalProcess;i++){

				messageBuff.put(new Integer(i), new ArrayList<String>());
				outMessages.put(new Integer(i), new ArrayList<String>());

			}
			messageBuff.put(new Integer(processNo), outMessages.get(processNo));

			manager = new ConnectionManager(netIps,netPorts,processNo,totalProcess,messageBuff,outMessages,clock); 

			//System.out.println("Thread sleeping for waiting connections to complete");
			Thread.currentThread().sleep(1000);
			//System.out.println("Thread wake up after sleeping");

			initFileRead();

			buildSrvrTree();

			grants= new boolean[servNo];

			for(int i=0;i<status.length;i++){
				status[i] = false;
				versions[i] = 0;
				objReqs.put(i, new ArrayList<String>());
				dataObjs[i] = 0;
			}

			for(int i=0;i<grants.length;i++){
				grants[i]=false;
			}

		}catch (InterruptedException exp) {
			exp.printStackTrace();
		}
	}



	public void terminate(){
		manager.terminate();
	}

	void buildSrvrTree(){

		//System.out.println("building server tree");

		root = new TreeNode();

		root.setIndex(0);

		buildChildren(root);

	}


	void buildChildren(TreeNode node){

		if( (2*node.getIndex() +1)<servNo ){
			TreeNode leftChild = new TreeNode();
			leftChild.setIndex((2*node.getIndex() +1));
			leftChild.setParent(node);
			node.setLeftChild(leftChild);
			//System.out.println("Added child "+leftChild.getIndex());
			buildChildren(leftChild);
		}


		if( (2*node.getIndex() +2)<servNo ){
			TreeNode rightChild = new TreeNode();
			rightChild.setIndex((2*node.getIndex() +2));
			rightChild.setParent(node);
			node.setRightChild(rightChild);
			//System.out.println("Added child "+rightChild.getIndex());
			buildChildren(rightChild);

		}

	}

	private void initFileRead() {

		try{
			//System.out.println("Looking for properties file in the path: "+inputFilePath + "\n\n\n");
			File file = new File(inputFilePath);
			reader = new Scanner(file);
			String line;

			while(reader.hasNextLine()){
				line = reader.nextLine();
				line = line.trim();
				//System.out.println("read line of input: " + line);
				if(!line.startsWith("#")){
					if(line.startsWith("NS=")){
						servNo = Integer.parseInt(line.substring(line.indexOf('=')+1).trim());
					}else if(line.startsWith("NC=")){
						cliNo = Integer.parseInt(line.substring(line.indexOf('=')+1).trim());
					}else if(line.startsWith("M=")){
						totReq = Integer.parseInt(line.substring(line.indexOf('=')+1).trim());
					}else if(line.startsWith("TIME_UNIT=")){
						timeUnit = Integer.parseInt(line.substring(line.indexOf('=')+1).trim());
					}else if(line.startsWith("FAILINGNODES:")){
						line = reader.nextLine();

						String [] failNodes = line.split(" ");
						failingNodes = new ArrayList<Integer>();
						for(int i=0;i<failNodes.length;i++)
							failingNodes.add( Integer.parseInt(failNodes[i]));
					}else if(line.startsWith("HOLD_TIME=")){
						holdTime = Integer.parseInt(line.substring(line.indexOf('=')+1).trim());
					}
				}
			}

			if(totalProcess != servNo+cliNo ){
				System.out.println("Error: input file incosistent with config file !!");
				System.exit(0);
			}

			System.out.println("Reading input file succesful");

		}catch(FileNotFoundException exp){
			System.out.println("Not able to open  properties file in the path: "+inputFilePath);
			exp.printStackTrace();
		}catch (NumberFormatException exp) {
			exp.printStackTrace();
		}		
	}


	public void checkInputMessages(){



		String line="";
		String [] tokens;
		int pid=-1;
		int reqType =-1;
		for(int i=0;i<servNo;i++){
			//System.out.println("checking input messages");
			while(messageBuff.get(i).size()>0){
				//printQuorumSts();

				pid=-1;
				reqType =-1;

				line =messageBuff.get(i).remove(0);

				//System.out.println("Recieved message from process " + i + " msg is : '" + line + "' ");

				if(line!=null && line.length()>0){

					tokens = line.split(" ");

					if(line.equalsIgnoreCase("PAUSE")){
						System.out.println(" Recieved PAUSE from "+i+ " line is '"+line+"'");
						pauseCount++;
						pauseNext = true;
						if(pauseCount==1){
							System.out.println(" ====== Pausing requests ===== ");
						}
					}else if(line.equalsIgnoreCase("UNPAUSE")){
						pauseCount--;
						System.out.println(" Recieved UNPAUSE from "+i+ " line is '"+line+"' pause count " + pauseCount);
						if(pauseCount==0){
							pauseNext = false;
							System.out.println(" ====== Unpausing requests ====== ");
						}
					}else{
						pid = Integer.parseInt(tokens[0]);
						reqType = Integer.parseInt(tokens[1]);

						totMessRecv++;

						if(reqType == 3){
							System.out.println("Recieved grant from "+pid);
							grants[pid] = true;
						}else if(reqType == 4){
							acksRecd++;
						}
					}

				}
			}

		}
	}


	public void printOutMessage(){
		System.out.println("Printing out put message buffer");
		for(int i=0;i<messageBuff.size();i++){

			ArrayList<String> list = messageBuff.get(i);
			System.out.println("Printing Output Message buffer for process "+i + " size is "+ list.size());
			for(int j=0;j<list.size();i++){
				System.out.println(list.get(j));
			}
			System.out.println();
		}
	}






	void sendReqs(){
		doBjNo = (int)(Math.random()*4);
		incrVal =1;
		//incrVal = (int)(Math.random()*5)+1;
		System.out.println("Attempting to add "+incrVal +" to data Object "+doBjNo);
		for(int i=0;i<servNo;i++){

			String line =processNo +" 0 "+doBjNo+" "+incrVal;
			outMessages.get(i).add(line);
			System.out.println("Sent Meassge - '"+line+"' to process "+i);
			totMessSent++;
		}

	}



	void sendCommits(){
		System.out.println("Sending commits");
		sendMessages(1);
	}

	void sendWithdraws(){
		System.out.println("Sending withdraw");
		sendMessages(2);
	}

	void sendMessages(int reqId){

		for(int i=0;i<servNo;i++){

			String line =processNo +" "+reqId+" "+doBjNo;
			outMessages.get(i).add(line);
			System.out.println("Sent Meassge - '"+line+"' to process "+i);
			totMessSent++;
		}

	}

	public void strtExec(){

		if(processNo <servNo){
			servNode();
		}else{
			clientNode();
		}

	}

	void clientNode(){

		System.out.println("##### Starting  programm execution #####\n");
		boolean gotallPerms =false;
		pauseNext =false;

		acksRecd=0;

		for(int i=0;i<totReq;i++){
			System.out.println("Executing update request "+i);			

			gotallPerms = false;
			doBjNo = -1;
			incrVal = -1;
			for(int p=0;p<grants.length;p++){
				grants[p]=false;
			}


			sendReqs();
			long timerStrt = System.currentTimeMillis();
			System.out.println("Sent all requests for update request "+i);

			while( (System.currentTimeMillis() <timerStrt+(20*timeUnit))){
				long endTime = System.currentTimeMillis();	
				checkInputMessages();
				if(checkPerms()){
					gotallPerms = true;
					accessTime.add(endTime-timerStrt);
					break;
				}
			}


			if(gotallPerms){
				try {
					Thread.sleep(holdTime*timeUnit);
					sendCommits();

					succAccess++;
					System.out.print("Got Perms:");
					for(int z=0;z<grants.length;z++){
						System.out.print(z+"-"+grants[z]+" ");
					}
					System.out.println("\n******** Got all permissions for request "+i+" ********");


				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				sendWithdraws();
				failAcess++;

				System.out.print("Got Perms:");
				for(int z=0;z<grants.length;z++){
					System.out.print(z+"-"+grants[z]+" ");
				}
				System.out.println("\n******** Timed out for request "+i+" ********");
			}
			while(acksRecd<servNo){
				checkInputMessages();
			}
			System.out.println("=== Completed update request "+i+" ===\n");

			if(processNo ==servNo && (i== ((3*totReq)/5) -1)){
				sendDeActivates();
			}

			if(processNo ==servNo && (i== ((4*totReq)/5) -1)){
				sendReActivates();
			}

			if(pauseNext){
				System.out.println("\n******** PAUSED *******");
				System.out.println("******** PAUSED *******");
				System.out.println("******** PAUSED *******\n");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			while(pauseNext){
				checkInputMessages();
			}

		}


		printStats();

		System.out.println("\n##### Completed Replica Consistency algorithm succesfully #####");

	}

	void sendDeActivates(){

		for(int i=0;i<failingNodes.size();i++){
			outMessages.get(failingNodes.get(i)).add("DEACTIVATE");
			System.out.println("*************** Sent Deactivate to  node "+failingNodes.get(i)+" **************");
		}
	}

	void sendReActivates(){
		for(int i=0;i<failingNodes.size();i++){
			outMessages.get(failingNodes.get(i)).add("REACTIVATE");
			System.out.println("*************** Sent Reactivate to  node "+failingNodes.get(i)+" **************");
		}
	}




	void printStats(){


		Long minTime=10000000000000L;
		Long maxTime=-10000000000000L;
		Long avgTime=0L;
		Long sumTime=0L;
		Long stdDev=0L;


		System.out.println("Printing stat messages");

		System.out.println("Successful Access: "+succAccess);
		System.out.println("Failed Access: "+failAcess);
		System.out.println("Total Meassages exchanged:"+(totMessSent+totMessRecv));

		for(int i=0;i<accessTime.size();i++){
			if(accessTime.get(i)<minTime){
				minTime=accessTime.get(i);
			}

			if(accessTime.get(i)>maxTime){
				maxTime=accessTime.get(i);
			}

			sumTime += accessTime.get(i);

		}

		if(accessTime.size()>0){
			avgTime=sumTime/accessTime.size();
		}

		sumTime =0L;

		for(int i=0;i<accessTime.size();i++){
			sumTime= (long) (sumTime +  Math.pow(avgTime - accessTime.get(i), 2));
		}

		if(accessTime.size()>0){
			sumTime = sumTime/accessTime.size();
		}

		stdDev = (long) (Math.pow(sumTime, 0.5));


		System.out.println("Min Grant time :"+minTime);
		System.out.println("Max Grant time :"+maxTime);
		System.out.println("Avg Grant time :"+avgTime);
		System.out.println("Standard Deviation of Grant time :"+stdDev);

	}



	boolean checkPerms(){

		//System.out.println("Checking Perms:");
		//		for(int i=0;i<grants.length;i++){
		//			System.out.print(grants[i]+" ");
		//		}
		//		System.out.println();


		boolean nodeperm = grants[root.getIndex()];

		boolean leftperm = gotAllPerms(root.getLeftChild());
		//System.out.println("root left child is "+root.getLeftChild().getIndex());
		boolean rightperm = gotAllPerms(root.getRightChild());
		//System.out.println("root right child is "+root.getRightChild().getIndex());


		return ((nodeperm && leftperm) || (nodeperm&&rightperm)||(leftperm&&rightperm));
	}


	boolean gotAllPerms(TreeNode node){

		boolean ret = true;

		if(node!=null){
			//System.out.println("Inside gotAllPerms for node "+node.getIndex());
			boolean nodeperm = grants[node.getIndex()];
			boolean leftperm = false;
			boolean rightperm = false;
			if(null != node.getLeftChild() && null != node.getRightChild()){

				//System.out.println("Checking permission for left child "+node.getLeftChild().getIndex());
				leftperm = gotAllPerms(node.getLeftChild());
				//System.out.println("Checking permission for right child "+node.getRightChild().getIndex());
				rightperm = gotAllPerms(node.getRightChild());

				ret = ((nodeperm && leftperm) || (nodeperm&&rightperm)||(leftperm&&rightperm));

			}else{
				//System.out.println("Node "+node.getIndex() + " has no children");
				ret = nodeperm;
			}
			//System.out.println("Inside gotAllPerms for node "+node.getIndex() +" returning "+ret);
		}

		return ret;
	}


	void servNode(){
		boolean dead = false;
		while(true){
			try {

				String line="";
				String [] tokens;
				int pid=-1;
				int reqId =-1;
				int dObjId =-1;

				boolean recMsg =false;
				for(int i=0;i<messageBuff.size();i++){
					//System.out.println("checking input messages");
					while(messageBuff.get(i).size()>0){
						recMsg = true;
						line = messageBuff.get(i).remove(0);
						if(null!=line && line.length()>0)	{
							tokens = line.split(" ");
							if(!dead && line.equalsIgnoreCase("DEACTIVATE")){
								System.out.println("=====****** DEACTIVATED !! ********=====");
								dead =true;
							}else if( dead && line.equalsIgnoreCase("REACTIVATE")){
								dead =false;
								synchronise();
								System.out.println("=====****** ACTIVATED !! ********=====");	
							}else if(!dead){
								System.out.println("Recieved message from process " + i + " msg is : '" + line + "' ");
								pid = Integer.parseInt(tokens[0]);
								reqId = Integer.parseInt(tokens[1]);
								dObjId = Integer.parseInt(tokens[2]);

								if(reqId == 1){
									performCommit(line);
									String outLine = processNo +" 4";
									outMessages.get(pid).add(outLine);
									System.out.println("Seding ack to pid "+pid+" '"+outLine+"'");
									totMessSent++;
								}else if(reqId == 2){
									System.out.println("********** Withdraw request from process "+ pid +" '"+ line + "' **********");
									removeReq(line);
									String outLine = processNo +" 4";
									outMessages.get(pid).add(outLine);
									System.out.println("Seding ack to pid "+pid+" '"+outLine+"'");
									totMessSent++;
								}else if(reqId==5){
									System.out.println("********** Recieved Sync request from "+pid+" **********");
									sendUpdNext.add(pid);
								}else{
									//System.out.println("Recieved request from pid"+ pid +"'"+line+"'");
									objReqs.get(dObjId).add(line);
								}
							}
						}
					}
				}


				//Synchronisations

				if(sendUpdNext.size()==failingNodes.size()){
					boolean doUpd = true;

					for(int i=0;i<4;i++){
						boolean openReq = true;

						if(objReqs.get(i).size()==0){
							openReq = false;
						}

						doUpd = doUpd &&(!status[i]) && (!openReq);
					}

					if(doUpd){
						System.out.println("           ********** Sending Synchronisation Messages ********");

						while(sendUpdNext.size()>0){
							int srvNo = sendUpdNext.remove(0);
							String dataLine="sync";

							for(int k=0;k<4;k++){
								dataLine+= " "+dataObjs[k]+","+versions[k];
							}


							outMessages.get(srvNo).add(dataLine);
							System.out.println("Sent synchronise message '"+dataLine+"' to srvr "+srvNo);
							totMessSent++;


							while(true){
								if(messageBuff.get(srvNo).size()>0){
									String msg = messageBuff.get(srvNo).remove(0);
									totMessRecv++;
									System.out.println("Recieved message '"+msg+"' from srvr "+srvNo);
									if(msg.equalsIgnoreCase("sync-ack")){
										break;
									}
								}
							}


						}

						System.out.println("           *************** Synchronisation complete!! ********************");
						System.out.println("           *************** Synchronisation complete!! ********************");
					}
				}


				if(recMsg){
					prntReqQuees();
				}
				sendGrants();

			}catch (NumberFormatException exp) {
				exp.printStackTrace();
			} 
		}

	}

	void synchronise(){
		System.out.println("****** SYNCHRONISING !! ********");
		int syncSrvNo=-1;
		for(int i=servNo;i<totalProcess;i++){
			String line ="PAUSE";
			outMessages.get(i).add(line);
			System.out.println(" Sending pause to client "+i + " '"+line+"'");
		}

		for(int i=0;i<servNo;i++){
			if(!failingNodes.contains(new Integer(i))){
				String line = processNo+" 5 0";
				outMessages.get(i).add(line);
				syncSrvNo = i;
				System.out.println(" Attempting to synchronise from "+syncSrvNo+" msg is '"+line+"'");
				syncCompl = false;
				break;
			}

		}

		String line="";
		while(!syncCompl){
			if(messageBuff.get(syncSrvNo).size()>0){

				line = messageBuff.get(syncSrvNo).remove(0);

				if(line.startsWith("sync")){

					System.out.println(" ----- Recieved Synchronise message '"+line+"' from "+ syncSrvNo +" ------ ");

					String tokens[] = line.split(" ");

					for(int i=1;i<5;i++){
						String [] data = tokens[i].split(",");
						dataObjs[i-1] = Integer.parseInt(data[0]);
						versions[i-1] = Integer.parseInt(data[1]);
					}

					syncCompl = true;
					outMessages.get(syncSrvNo).add("sync-ack");
				}


			}
		}


		for(int i=0;i<4;i++){
			objReqs.put(i, new ArrayList<String>());
		}

		for(int p=servNo;p<totalProcess;p++){
			messageBuff.put(p, new ArrayList<String>());
		}

		for(int i=servNo;i<totalProcess;i++){
			String unpLine = "UNPAUSE";
			outMessages.get(i).add(unpLine);
			System.out.println(" Sending unpause to client "+i + " '"+unpLine+"'");
		}

		System.out.println("****** Synchronise complete !! ********");
	}



	void sendGrants(){

		//prntReqQuees();

		int pid=-1;
		int reqId =-1;
		int dObjId =-1;


		for(int i=0;i<4;i++){

			if(!status[i] &&sendUpdNext.size()==0){
				if(objReqs.get(i).size()>0){
					String line = objReqs.get(i).get(0);
					String [] tokens = line.split(" ");

					pid = Integer.parseInt(tokens[0]);
					reqId = Integer.parseInt(tokens[1]);
					dObjId = Integer.parseInt(tokens[2]);

					String grntLin =  processNo+" 3";
					outMessages.get(pid).add(grntLin);
					System.out.println("Sending grant to pid: "+pid +" on DOBJ:"+dObjId+" '"+grntLin+"'");
					status[i] = true;
					totMessSent++;
				}


			}
		}
	}

	void performCommit(String line){

		String reqStr = removeReq(line);

		String [] tokens = reqStr.split(" ");
		int pid = Integer.parseInt(tokens[0]);
		int reqId = Integer.parseInt(tokens[1]);
		int dObjId = Integer.parseInt(tokens[2]);
		int incrVal = Integer.parseInt(tokens[3]);

		dataObjs[dObjId] = dataObjs[dObjId] + incrVal;
		versions[dObjId] = versions[dObjId] +1;

		System.out.println("********** committed request from process "+ pid +" '"+ line + "' **********");
		System.out.println("Data of "+ dObjId +" value,version is{"+dataObjs[dObjId]+","+versions[dObjId]+"}");

	}

	void prntReqQuees(){

		System.out.println("Printing request quees:");

		for(int i=0;i<4;i++){
			System.out.print("Data Obj"+ i +"value,version ["+dataObjs[i]+","+versions[i]+"]Request Quee of:"+i+"{");
			ArrayList<String> reqs = objReqs.get(i);
			for(int p=0;p<reqs.size();p++){
				System.out.print(reqs.get(p));
				if(p!=reqs.size())
					System.out.print(",");
			}
			System.out.println("}");
		}
	}

	String removeReq(String line){

		//prntReqQuees();

		String ret="";

		String [] tokens = line.split(" ");
		int pid = Integer.parseInt(tokens[0]);
		int reqId = Integer.parseInt(tokens[1]);
		int dObjId = Integer.parseInt(tokens[2]);

		ArrayList<String> reqs = objReqs.get(dObjId);

		//System.out.print("Printing reqs for dObjId"+dObjId);
		for(int i=0;i<reqs.size();i++){
			//System.out.println("Does this req strt with"+pid);
			if(reqs.get(i).startsWith(""+pid)){
				//System.out.println("yes");
				ret = reqs.remove(i);
				if(i==0){
					status[dObjId] = false;
				}
			}
		}

		System.out.println("Removes request from process "+ pid +" '"+ ret + "'");


		return ret;

	}

}
