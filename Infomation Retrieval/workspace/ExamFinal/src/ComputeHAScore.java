import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class ComputeHAScore {
	HashMap<String,ArrayList<String>> adjMatrix = new HashMap<String,ArrayList<String>>();
	HashMap<String,ArrayList<String>> invLinks = new HashMap<String,ArrayList<String>>();



	public static void main(String[] args){


		ComputeHAScore mainClass= new ComputeHAScore();

		long startTime = System.currentTimeMillis();
		//System.out.println("args are:"+ args[0]+ args[1]+ args[2]);
		mainClass.computeScore();
		long endTime = System.currentTimeMillis();

		System.out.println("");

		System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );

	}

	public void computeScore(){
		this.initalise();
		invertLinks();

		int docNo = adjMatrix.keySet().size();

		System.out.println("Adjacency Matris is:");
		printList(adjMatrix);
		System.out.println("Inverted Links are:");
		printList(invLinks);

		float [] hubScore  = new float[docNo+1];

		float [] tempHubScore  = new float[docNo+1];



		float [] authScore  = new float[docNo+1];

		float [] tempauthScore  = new float[docNo+1];


		for(int i=1;i<docNo+1 ; i++){
			hubScore[i] = 1.0f;
			tempHubScore[i] = 0.0f;

			authScore[i] =1.0f;
			tempauthScore[i]=0.0f;
		}

		int n=0;

		do
		{	
			InputStreamReader streamReader = new InputStreamReader(System.in);
			BufferedReader bufReader = new BufferedReader(streamReader);

			System.out.println("Please Enter Number of Iterations to run (Enter 0 to quit):");
			String input=null;

			try{

				input = bufReader.readLine();

			}
			catch(IOException exp){
				exp.printStackTrace();
			}

			n = Integer.parseInt(input);
			System.out.println("[Iteration No]:Hub Score:Auth Score");
			System.out.print("[0]");
			for(int k=1;k<docNo+1 ; k++){
				System.out.print("D"+k+ ":"+(int)hubScore[k]+":"+(int)authScore[k]+", ");
			}
			System.out.println();
			for (int i =0;i<n;i++){


				for(int k=1; k<docNo+1;k++){
					ArrayList<String> invRow;
					ArrayList<String> adjRow;
					float sumHub =0.0f;
					float sumAuth =0.0f;
					invRow =  invLinks.get(k+""); 
					String tok;

					for(int j=0;j<invRow.size();j++){
						tok = invRow.get(j);

						sumAuth = sumAuth +( hubScore[Integer.parseInt(tok)] );

					}

					adjRow =  adjMatrix.get(k+""); 

					for(int j=0;j<adjRow.size();j++){
						tok = adjRow.get(j);

						sumHub = sumHub +( authScore[Integer.parseInt(tok)] );
					}




					tempHubScore[k] = sumHub;
					tempauthScore[k] = sumAuth;


				}



				System.out.print("["+(i+1)+ "]");
				for(int k=1;k<docNo+1 ; k++){
					authScore[k] = tempauthScore[k];
					hubScore[k] = tempHubScore[k];
					System.out.print("D"+k+ ":"+(int)hubScore[k]+":"+(int)authScore[k]+", ");
				}
				System.out.println();

			}


		}while(n!=0);

	}


	public void invertLinks(){

		Set<String> keySet = adjMatrix.keySet();
		String key="";
		ArrayList<String> row;
		ArrayList<String> invRow;


		int docNo = adjMatrix.keySet().size();

		for(int i=1;i<docNo+1;i++){

			Iterator<String> itr = keySet.iterator();

			invRow = new ArrayList<String>();

			while(itr.hasNext()){
				key = itr.next();
				row = adjMatrix.get(key);

				if(row.contains( ""+(i) ) ){
					invRow.add(key);
				}

			}

			invLinks.put(""+(i), invRow);



		}
	}


	public void initalise(){
		ArrayList<String> row;

		row = new ArrayList<String>();
		row.add("2");
		row.add("4");
		row.add("6");
		row.add("15");

		adjMatrix.put("1", row);
		row = new ArrayList<String>();

		row.add("1");
		row.add("3");

		adjMatrix.put("2", row);
		row = new ArrayList<String>();

		row.add("4");
		row.add("8");
		row.add("9");
		row.add("10");

		adjMatrix.put("3", row);
		row = new ArrayList<String>();

		row.add("5");
		row.add("11");
		row.add("15");

		adjMatrix.put("4", row);
		row = new ArrayList<String>();

		row.add("1");
		row.add("7");
		row.add("11");

		adjMatrix.put("5", row);
		row = new ArrayList<String>();

		row.add("5");
		row.add("15");

		adjMatrix.put("6", row);
		row = new ArrayList<String>();

		row.add("4");
		row.add("10");

		adjMatrix.put("7", row);
		row = new ArrayList<String>();

		row.add("1");
		row.add("3");

		adjMatrix.put("8", row);
		row = new ArrayList<String>();

		row.add("12");
		row.add("13");
		row.add("14");

		adjMatrix.put("9", row);
		row = new ArrayList<String>();

		row.add("7");
		row.add("9");
		row.add("13");

		adjMatrix.put("10", row);
		row = new ArrayList<String>();

		row.add("2");
		row.add("6");

		adjMatrix.put("11", row);
		row = new ArrayList<String>();

		row.add("13");
		row.add("14");

		adjMatrix.put("12", row);
		row = new ArrayList<String>();

		row.add("2");
		row.add("8");

		adjMatrix.put("13", row);
		row = new ArrayList<String>();

		row.add("3");
		row.add("12");

		adjMatrix.put("14", row);
		row = new ArrayList<String>();

		row.add("6");
		row.add("12");

		adjMatrix.put("15", row);

	}

	public void printList(HashMap<String,ArrayList<String>> map){

		Set<String> keySet = map.keySet();
		String key;
		Iterator<String> itr = keySet.iterator();
		System.out.println("D.No:List(Da,Db..)");
		while(itr.hasNext()){
			key = itr.next();

			ArrayList<String> row = map.get(key);

			System.out.print("D"+key+":");

			for(int j=0;j<row.size();j++){
				String docNo = row.get(j);
				System.out.print("D"+docNo);

				if(j!=row.size()-1){
					System.out.print(",");
				}
			}

			System.out.println("");
		}
		System.out.println("\n");
		return;
	}


}
