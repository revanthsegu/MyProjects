import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class ClusterDocs {
	ArrayList<String> dictionaryList =new ArrayList<String>();
	String [] dictionary;
	String [] input = new String [15];
	int [][] vecMatrix;


	public static void main(String[] args){

		ClusterDocs mainClass= new ClusterDocs();

		long startTime = System.currentTimeMillis();
		mainClass.clusterDocs(args);

		long endTime = System.currentTimeMillis();

		System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );

	}



	public void clusterDocs(String[] args){
		initialise();
		makeDictionary();
		makeDocVectors();

		Cluster[] clusters = new Cluster[3];

		Cluster cluster;
		LinkedList<Integer> docs= new LinkedList<Integer>();
		int randDoc=-4;
		double[][] oldCentroids =new double[3][dictionary.length];
		double[][] centroidDist =new double[3][10];
		double [] centroid;

		for(int i=0;i<3;i++){

			randDoc += 5;

			centroid =new double[dictionary.length];
			for(int z=0;z<dictionary.length;z++){
				centroid[z] = vecMatrix[randDoc][z];
				//System.out.println("i:"+i+"Rand doc: " + randDoc + " vectComp:" +vecMatrix[randDoc][z]);
				oldCentroids[i][z] = vecMatrix[randDoc][z];
			}



			cluster = new  Cluster();
			cluster.setCentroid(centroid);
			cluster.setDocs(docs);
			clusters[i]=cluster;
		}



		for(int m=0;m<10;m++){
			clusterDocs(clusters);

			for(int i=0;i<3;i++){

				for(int z=0;z<dictionary.length;z++){
					oldCentroids[i][z] = clusters[i].getCentroid()[z];
				}


			}

			//printClusters(clusters,centroidDist,m);
			getNewCentroids(clusters);

			for(int z=0;z<3;z++){

				centroidDist[z][m] = getdistance(oldCentroids,clusters,z);
			}

			printClusters(clusters,centroidDist,m);

		}





	}

	public void printClusters(Cluster [] clusters,double[][] centroidDist,int m){
		for(int i=0;i<3;i++){
			System.out.print(" Iteration "+m+": Cluster "+i+" Centroid is:[");
			for(int j=0;j<dictionary.length;j++){
				System.out.print(clusters[i].getCentroid()[j]+",");
			}System.out.println("]");

			if(m>0){
				System.out.print("Distance from old Centroid:" +centroidDist[i][m-1]);
			}

			System.out.print("; Docs in Cluster are:" );
			for(int l=0;l<clusters[i].getDocs().size();l++){
				System.out.print(clusters[i].getDocs().get(l)+",");
			}
			System.out.println();

		}

	}

	public void getNewCentroids(Cluster [] cluster){

		for(int i=0;i<3;i++){
			for(int j=0;j<dictionary.length;j++){
				double sum=0.0;
				for(int l=0;l<cluster[i].getDocs().size();l++){
					//System.out.print("Adding:"+vecMatrix[cluster[i].getDocs().get(l)][j]);
					sum += vecMatrix[cluster[i].getDocs().get(l)][j];
				}
				//System.out.print("Sum is:"+ sum + " doc size is:"+cluster[i].getDocs().size() + " divided value"+ sum/cluster[i].getDocs().size() );
				cluster[i].getCentroid()[j] = (Double)(sum/cluster[i].getDocs().size());
			}	
		}

	}

	public void clusterDocs(Cluster[] clusters){
		clusters[0].setDocs(new LinkedList<Integer>());
		clusters[1].setDocs(new LinkedList<Integer>());
		clusters[2].setDocs(new LinkedList<Integer>());
		double d1,d2,d3;
		for(int k=0;k<input.length;k++){

			d1 = getdistance(k,clusters[0].getCentroid());
			//System.out.println("Doc :"+k);
			//for(int p=0;p<dictionary.length;p++)
			//System.out.print(clusters[0].getCentroid()[p]+",");
			//System.out.println();
			d2 = getdistance(k,clusters[1].getCentroid());
			d3 = getdistance(k,clusters[2].getCentroid());



			if(d1<d2){
				if(d1<d3){
					clusters[0].getDocs().add(k);
					//System.out.println("Doc "+k + " Distances "+d1+" "+d2+" "+d3+"Will go to cluster:"+0);
				}else{
					clusters[2].getDocs().add(k);
					//System.out.println("Doc "+k + " Distances "+d1+" "+d2+" "+d3+"Will go to cluster:"+2);
				}

			}else{
				if(d2<d3){
					clusters[1].getDocs().add(k);
					//System.out.println("Doc "+k + " Distances "+d1+" "+d2+" "+d3+"Will go to cluster:"+1);
				}else{
					clusters[2].getDocs().add(k);
					//System.out.println("Doc "+k + " Distances "+d1+" "+d2+" "+d3+"Will go to cluster:"+2);
				}

			}

		}

	}


	double getdistance(int i, double[] v2){
		double sum = 0;
		for(int k=0;k<dictionary.length;k++){
			sum += Math.pow(vecMatrix[i][k]-v2[k],2);
			//System.out.println("sum is:"+sum);
		}

		return Math.sqrt(sum);
	}


	double getdistance(double[] v1, double[] v2){
		double sum = 0;
		for(int k=0;k<dictionary.length;k++){
			sum += Math.pow(v1[k]-v2[k],2);
		}

		return Math.sqrt(sum);
	}

	double getdistance(double[][] v1, Cluster[] cluster,int i){
		double sum = 0;
		for(int k=0;k<dictionary.length;k++){
			sum += Math.pow(v1[i][k]-cluster[i].getCentroid()[k],2);
		}

		return Math.sqrt(sum);
	}

	public void makeDictionary(){
		for(int i = 0; i<input.length;i++){

			String tokens[] = input[i].split(" ");
			for(int j=0;j<tokens.length;j++){
				if(!dictionaryList.contains(tokens[j])){
					dictionaryList.add(tokens[j]);
				}
			}

		}
	}

	public void makeDocVectors(){
		Object [] temp =  dictionaryList.toArray();

		dictionary = new String[temp.length];



		HashMap<String,Integer> dictMap = new HashMap<String,Integer>();
		for(int i = 0; i<dictionary.length;i++){
			dictionary[i] = (String)temp[i];
			dictMap.put(dictionary[i], new Integer(i));
		}


		System.out.print("Dictionary["+dictionary.length+"]:");
		for(int i = 0; i<dictionary.length;i++){
			System.out.print(dictionary[i]+":");
		}
		System.out.println();

		System.out.println("input.length is " + input.length + "dictionary.length is "+dictionary.length );
		vecMatrix = new int[input.length][dictionary.length];

		for(int i = 0; i<input.length;i++){

			for(int j=0;j<dictionary.length;j++){
				vecMatrix[i][j] =0;

			}

		}

		printVectors();

		for(int i = 0; i<input.length;i++){

			String tokens[] = input[i].split(" ");
			for(int j=0;j<tokens.length;j++){
				//System.out.println("Token:"+tokens[j]+",Ind:"+dictMap.get(tokens[j]));
				vecMatrix[i][dictMap.get(tokens[j])] +=1;

			}

		}


		printVectors();

	}

	public void printVectors(){
		System.out.print("Dictionary:");
		for(int i = 0; i<dictionary.length;i++){
			System.out.print(dictionary[i]+":");
		}
		System.out.println();

		for(int k = 0; k<vecMatrix.length;k++){
			System.out.print("D"+(k+1)+"[");
			for(int j = 0; j<dictionary.length;j++){

				System.out.print(vecMatrix[k][j]+",");
			}

			System.out.println("]");
		}
	}

	public void initialise(){
		input[0] ="house health wealth happiness family wealth Rome Italy health Paris France";
		input[1] ="medicine biology cells child health";
		input[2] ="science knowledge wise family";
		input[3] ="mother girl child family London Rome Italy";
		input[4] ="singing dancing shopping shopping shopping shopping";
		input[5] ="fitness Australia gym Italy shoes beach";
		input[6] ="computers TV internet football";
		input[7] ="chemistry substance science nature";
		input[8] ="museum opera singing dancing painting";
		input[9] ="physics nature Malibu Italy fashion art";
		input[10] ="mathematics calculus probabilities science";
		input[11] ="Sydney Australia Milan Italy Paris France wealth health";
		input[12] ="Malibu Australia Hawaii TV fitness fresh air palm trees";
		input[13] ="waves surfing physics nature beach";
		input[14] ="house park beach ocean Sydney Australia";
	}




}
