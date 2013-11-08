import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;



public class ProcessSteps {
	Scanner reader;
	Integer clock= new Integer(0);
	int processNo;
	String[] netIps;
	int [] netPorts; 
	int totalProcess;
	HashMap<Integer, ArrayList<String>> messageBuff = new HashMap<Integer, ArrayList<String>>();
	HashMap<Integer, ArrayList<String>> outMessages = new HashMap<Integer, ArrayList<String>>();
	ConnectionManager manager;
	String inputFilePath;
	ArrayList<Integer> waitinResps = new ArrayList<Integer>();

	int parent=-1;
	boolean inTree = false;



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

			manager = new ConnectionManager(netIps,netPorts,processNo,totalProcess,messageBuff,outMessages,clock); 

			//System.out.println("Thread sleeping for waiting connections to complete");
			Thread.currentThread().sleep(1000);
			//System.out.println("Thread wake up after sleeping");

			initFileRead();
		}catch (InterruptedException exp) {
			exp.printStackTrace();
		}
	}


	public void terminate(){
		manager.terminate();
	}

	private void initFileRead() {

		try{
			System.out.println("Looking for properties file in the path: "+inputFilePath + "\n\n\n");
			File file = new File(inputFilePath);
			reader = new Scanner(file);
			String line;
			if(reader.hasNextLine()){
				line = reader.nextLine();
				//System.out.println("First line of commands: " + line);
				if(totalProcess != Integer.parseInt(line.trim())){
					System.out.println("Error: Command file incosistent with properties file !!");
					System.exit(0);
				}
			}

		}catch(FileNotFoundException e){
			System.out.println("Not able to open  properties file in the path: "+inputFilePath);
			e.printStackTrace();
		}catch (RuntimeException exp) {
			exp.printStackTrace();
		}		
	}

	public void checkInputMessages(){
		String line="";
		String [] tokens;
		int pid=-1;
		int clockInt =-1;
		for(int i=0;i<messageBuff.size();i++){
			//System.out.println("checking input messages");
			while(messageBuff.get(i).size()>0){

				line = messageBuff.get(i).get(0);
				messageBuff.get(i).remove(0);
				System.out.println("Recieved message from process " + i + " msg is : '" + line + "' ");
				tokens = line.split(" ");

				pid = Integer.parseInt(tokens[0]);
				clockInt = Integer.parseInt(tokens[1]);

				incrClock(clockInt);

				//System.out.println("Clock value set to "+ clock);
				if(waitinResps.remove(new Integer(pid))){
					System.out.println("Removed " + pid + " from waiting for replys list ");
				}else{
					if(!inTree ){
						inTree = true;
						parent = pid;
						System.out.println("Joined tree with parent as "+ i);
					}else{
						System.out.println("Replying to process "+pid);
						processInstruction("SEND",pid,false);
					}

				}

				return;

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

	void processInstruction( String inst, int optVal,boolean trackResp){
		String line;

		if(inst.equalsIgnoreCase("INIT")){
			inTree = true;
			parent = -100; // ROOT or INIT node
			incrClock(clock);
			System.out.println("Encountered instruction 'INIT' starting protocol");
		}else if(inst.equalsIgnoreCase("SEND")){

			incrClock(clock);

			line = processNo+" "+clock;
			//System.out.println("Writing message '"+line+"' to out going messages queue of process " + optVal);
			ArrayList<String> list = outMessages.get(optVal);
			list.add(line);

			/*System.out.println("printing buffer of process " + optVal);
			for(int i=0;i<list.size();i++){
				System.out.print(list.get(i)+" ");
			}
			System.out.println();*/

			outMessages.put(optVal, list);
			if(trackResp){
				waitinResps.add(new Integer(optVal));
				System.out.println("Added " + optVal + " to waiting for replys list ");

			}
			System.out.println("Added message '"+line+"' to out going messages queue of process " + optVal);
			//printOutMessage();


		}else if(inst.equalsIgnoreCase("TICK")){
			try {
				Thread.currentThread().sleep(optVal);
				incrClock(clock);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}else if(inst.equalsIgnoreCase("IDLE")){
			incrClock(clock);
			printWaitingArray();
			if(inTree){
				if(parent==-100){
					if(waitinResps.size()==0){
						System.out.println("Completed Termination detection algorithm succesfully");
					}
				}
				else if(waitinResps.size()==0){
					bye();
				}

			}



		}else{
			System.out.println("Exiting !! NOT ABLE TO PERFROM THE INSTRUCTION " + inst);
		}

	}


	public void incrClock(int clockInt) {

		if(clockInt>clock){
			clock = new Integer(clockInt+1);
		}else{
			clock = new Integer(clock+1);
		}

		System.out.println("Clock value set to: "+ clock);
	}



	public void printWaitingArray(){
		System.out.print("Waiting for "+ waitinResps.size()+" processes: ");

		for(int i=0;i<waitinResps.size();i++){
			System.out.print(waitinResps.get(i));
		}
		System.out.println();
	}

	public void strtExec(){



		System.out.println("##### Starting  programm execution #####\n");
		String line="";
		String tokens[];
		int pid;
		int clockVal;
		int optVal=-1;
		while(reader.hasNextLine()){
			try{
				optVal=-1;
				clockVal =-1;
				line = reader.nextLine();
				//System.out.println(" Read line '" + line+"'");

				tokens = line.split(" ");

				pid = Integer.parseInt(tokens[0]);
				clockVal = Integer.parseInt(tokens[1]);

				if(tokens.length>3)
				{
					optVal = Integer.parseInt(tokens[3]);
				}

				if(processNo == pid){

					while(clock < clockVal){
						checkInputMessages();
					}
					System.out.println("Executing instruction '" + line+"'");
					if(clock == clockVal){

						processInstruction(tokens[2].trim(),optVal,true);


					}else{
						System.out.println("incorrect clock val waiting for " + clockVal + " current val " + clock);
					}
					//System.out.println("Executed instruction '"+line+"'");
				}



			}catch (NumberFormatException exp) {
				//System.out.println("Could not parse int from "+ line);
				exp.printStackTrace();
				return;
			}	
		}

		while(waitinResps.size()>0){
			checkInputMessages();
		}


		if(inTree){
			if(parent!=-100){
				bye();
			}
		}

		System.out.println("\n##### Completed Termination detection algorithm succesfully #####");



	}


	public void bye(){
		System.out.println("Replying to parent process and leaving tree with parent "+parent);
		processInstruction("SEND", parent,false);
		inTree =false;
		parent =-1;
	}

}
