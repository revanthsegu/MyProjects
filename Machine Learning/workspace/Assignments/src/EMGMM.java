import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class EMGMM {
	int data_length = 0;
	int MAX_ITR = 5;
	int itr_Count = 0;
	String filePath;
	double[] nums;


	public static void main(String[] args){
		try{
			long startTime = System.currentTimeMillis();
			if(args.length<1){
				System.out.println("Please enter the full file path as argument");
				return;
			}
			EMGMM mainClass =new  EMGMM();
			mainClass.calculateEMGMM(args);


			long endTime = System.currentTimeMillis();

			System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );
		}catch(RuntimeException exp){
			exp.printStackTrace();
		}
	}

	void calculateEMGMM(String[] args){
		if(null!=args[0] && args[0].length() > 2){
			filePath = args[0];
		}else{
			filePath = System.getProperty("user.dir")+"//em_data.txt";
		}

		double [][] meanVariance = new double [3][4];
		double [][] newMeanVariance = new double [3][4];
		double [][] probs;

		try{
			Scanner reader;
			data_length =0;
			int data_fill=0;


			File trainFile = new File(filePath);
			String line;

			reader = new Scanner(trainFile); // reading data length
			while(reader.hasNextLine()) {
				data_length++;
				reader.nextLine();
			}

			//System.out.println("total number of lines"+data_length);
			nums = new double[data_length];

			trainFile = new File(filePath);	
			reader = new Scanner(trainFile);
			while(reader.hasNextLine()){
				line = reader.nextLine();
				nums[data_fill] = Double.parseDouble(line);
				//System.out.println("reading value " + nums[data_fill]);
				data_fill++;
			}



		}catch(FileNotFoundException e){
			System.out.println("Not able to open Data file in the path: "+filePath);
			e.printStackTrace();
		}

		probs = new double [data_length][3];

		for(int i=0;i<probs.length;i++){
			probs[i][0] =0;
			probs[i][1] =0;
			probs[i][2] =0;

		}

		for(int i=0;i<3;i++){
			meanVariance[i][0]=0;
			meanVariance[i][1]=0;
			meanVariance[i][2]=0;
			meanVariance[i][3]=1;
		}

		//randInitialise(meanVariance);

		randProb(probs,meanVariance);
		boolean converged =false;

		for(int itr_Count =0 ; itr_Count<MAX_ITR&&(!converged) ; itr_Count++){	
			for(int i=0;i<data_length;i++){

				probs[i][0] = getProb(nums[i],meanVariance[0][0],meanVariance[0][1])*meanVariance[0][3];
				probs[i][1] = getProb(nums[i],meanVariance[1][0],meanVariance[1][1])*meanVariance[1][3];
				probs[i][2] = getProb(nums[i],meanVariance[2][0],meanVariance[2][1])*meanVariance[2][3];
				double totalProb = probs[i][0] + probs[i][1] + probs[i][2];

				if(totalProb!=0){
					probs[i][0] = probs[i][0]/totalProb;
					probs[i][1] = probs[i][2]/totalProb;
					probs[i][2] = probs[i][2]/totalProb;
				}


				newMeanVariance[0][0] += probs[i][0] * nums[i];
				newMeanVariance[1][0] += probs[i][1] * nums[i];
				newMeanVariance[2][0] += probs[i][2] * nums[i];

			}

			newMeanVariance[0][0] = newMeanVariance[0][0] /data_length;
			newMeanVariance[1][0] = newMeanVariance[1][0] /data_length;
			newMeanVariance[2][0] = newMeanVariance[2][0] /data_length;

			for(int i=0;i<data_length;i++){
				newMeanVariance[0][1] += Math.pow((newMeanVariance[0][0]-nums[i]), 2)*probs[i][0];
				newMeanVariance[1][1] += Math.pow((newMeanVariance[1][0]-nums[i]), 2)*probs[i][1];
				newMeanVariance[2][1] += Math.pow((newMeanVariance[2][0]-nums[i]), 2)*probs[i][2];

				newMeanVariance[0][3] +=probs[i][0];
				newMeanVariance[1][3] +=probs[i][1];
				newMeanVariance[2][3] +=probs[i][2];
			}

			newMeanVariance[0][1] = newMeanVariance[0][1] /data_length;
			newMeanVariance[1][1] = newMeanVariance[1][1] /data_length;
			newMeanVariance[2][1] = newMeanVariance[2][1] /data_length;


			newMeanVariance[0][3] = newMeanVariance[0][3] /data_length;
			newMeanVariance[1][3] = newMeanVariance[1][3] /data_length;
			newMeanVariance[2][3] = newMeanVariance[2][3] /data_length;

			converged=true;

			for(int l=0;l<3;l++){
				for(int j=0;j<4;j++){

					if(meanVariance[l][j] != newMeanVariance[l][j] && j!=3){
						converged=false;
					}

					meanVariance[l][j] = newMeanVariance[l][j];
					newMeanVariance[l][j] = 0;
				}
			}


			//System.out.println("After iteration " + itr_Count+ " Means are " + meanVariance[0][0] +","+meanVariance[1][0]+","+meanVariance[2][0] + " Variances are " + meanVariance[0][1] +","+meanVariance[1][1]+","+meanVariance[2][1]+ " Avg Probs are " + meanVariance[0][3]+","+meanVariance[1][3]+","+meanVariance[2][3]);

		}	

		System.out.println("Converged Means are " + meanVariance[0][0] +","+meanVariance[1][0]+","+meanVariance[2][0] + " Variances are " + meanVariance[0][1] +","+meanVariance[1][1]+","+meanVariance[2][1]);

	}


	double getProb(double num,double mean,double var){
		double ret;

		double numerator = -(Math.pow((num-mean), 2))/(2*var);


		//ret = (1/(Math.pow((2*Math.PI*var), 0.5)))*( Math.exp(numerator)  );

		ret = ( Math.exp(numerator)  );

		//System.out.println(" for values "+num+","+mean+","+var+" Prob is "+ ret);

		if(ret< Math.pow(10, -100))
			ret = Double.MIN_VALUE;

		return ret;
	}


	public void randProb(double[][] probs,double [][] meanVariance){

		for(int i=0;i<data_length;i++){

			probs[i][0] = Math.random();
			probs[i][1] = Math.random();
			probs[i][2] = Math.random();
			double totalProb = probs[i][0] + probs[i][1] + probs[i][2];

			if(totalProb!=0){	
				probs[i][0] = probs[i][0]/totalProb;
				probs[i][1] = probs[i][2]/totalProb;
				probs[i][2] = probs[i][2]/totalProb;
			}	


			meanVariance[0][0] += probs[i][0] * nums[i];
			meanVariance[1][0] += probs[i][1] * nums[i];
			meanVariance[2][0] += probs[i][2] * nums[i];

		}

		meanVariance[0][0] = meanVariance[0][0] /data_length;
		meanVariance[1][0] = meanVariance[1][0] /data_length;
		meanVariance[2][0] = meanVariance[2][0] /data_length;

		for(int i=0;i<data_length;i++){
			meanVariance[0][1] += Math.pow((meanVariance[0][0]-nums[i]), 2)*probs[i][0];
			meanVariance[1][1] += Math.pow((meanVariance[1][0]-nums[i]), 2)*probs[i][1];
			meanVariance[2][1] += Math.pow((meanVariance[2][0]-nums[i]), 2)*probs[i][2];
		}

		meanVariance[0][1] = meanVariance[0][1] /data_length;
		meanVariance[1][1] = meanVariance[1][1] /data_length;
		meanVariance[2][1] = meanVariance[2][1] /data_length;

	}

	public void randInitialise(double [][] meanVariance){
		int[] initCl= new int [data_length]; 


		for(int i=0;i<data_length;i++){
			/*initCl[i] = (int)(Math.random()*10);
			//System.out.println("rand out is " + initCl[i]);

			if(initCl[i]<5)
				initCl[i]=0;

			if(initCl[i]<7)
				initCl[i]=1;
			if(initCl[i]>2)
				initCl[i]=2;*/	

			initCl[i] = (int)(Math.random()*3);

		}


		for(int i=0;i<initCl.length;i++){
			meanVariance[initCl[i]][0] += nums[i]; 
			meanVariance[initCl[i]][2] += 1; 
		}


		//System.out.println("Sums are "+ meanVariance[0][0] +","+meanVariance[1][0]+","+meanVariance[2][0]);


		if(meanVariance[0][2]!=0){
			meanVariance[0][0] = meanVariance[0][0]/meanVariance[0][2];
			//meanVariance[0][0] =5;
		}

		if(meanVariance[1][2]!=0){
			meanVariance[1][0] = meanVariance[1][0]/meanVariance[1][2];
			//meanVariance[1][0] = 15;
		}

		if(meanVariance[2][2]!=0){
			meanVariance[2][0] = meanVariance[2][0]/meanVariance[2][2];
			//meanVariance[2][0] = 25;
		}

		for(int i=0;i<initCl.length;i++){
			meanVariance[initCl[i]][1] += Math.pow((meanVariance[initCl[i]][0]-nums[i]), 2); 
		}

		if(meanVariance[0][2]!=0){
			meanVariance[0][1] = meanVariance[0][1]/meanVariance[0][2];
			//meanVariance[0][1] = 8.2;
		}

		if(meanVariance[1][2]!=0){
			meanVariance[1][1] = meanVariance[1][1]/meanVariance[1][2];
			//meanVariance[1][1] = 8.2;
		}

		if(meanVariance[2][2]!=0){
			meanVariance[2][1] = meanVariance[2][1]/meanVariance[2][2];
			//meanVariance[2][1] = 8.2;
		}


		//System.out.println("After Initialization  Means are " + meanVariance[0][0] +","+meanVariance[1][0]+","+meanVariance[2][0] + " Variances are " + meanVariance[0][1] +","+meanVariance[1][1]+","+meanVariance[2][1]);

	}

}
