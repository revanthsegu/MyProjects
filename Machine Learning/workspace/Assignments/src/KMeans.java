/*** Author :Revanth Segu
The University of Texas at Dallas
 *****/


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class KMeans {
	static int MAX_ITR =500;
	static boolean spec_Hu = false;
	public static void main(String [] args){
		if (args.length < 3){
			System.out.println("Usage: Kmeans <input-image> <k> <output-image>");
			return;
		}
		try{
			long startTime = System.currentTimeMillis();
			BufferedImage originalImage = ImageIO.read(new File(args[0]));
			int k=Integer.parseInt(args[1]);
			BufferedImage kmeansJpg = kmeans_helper(originalImage,k);
			ImageIO.write(kmeansJpg, "png", new File(args[2])); 

			if(args.length>3 && args[3].equalsIgnoreCase("true")){
				spec_Hu = true;
			}

			long endTime = System.currentTimeMillis();
			System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );
		}catch(IOException e){
			System.out.println(e.getMessage());
		}	
	}

	private static BufferedImage kmeans_helper(BufferedImage originalImage, int k){
		int w=originalImage.getWidth();
		int h=originalImage.getHeight();
		BufferedImage kmeansImage = new BufferedImage(w,h,originalImage.getType());
		Graphics2D g = kmeansImage.createGraphics();
		g.drawImage(originalImage, 0, 0, w,h , null);
		// Read rgb values from the image
		int[] rgb=new int[w*h];
		int count=0;
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				rgb[count++]=kmeansImage.getRGB(i,j);
			}
		}
		// Call kmeans algorithm: update the rgb values
		kmeans(rgb,k);

		// Write the new rgb values to the image
		count=0;
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				kmeansImage.setRGB(i,j,rgb[count++]);
			}
		}
		return kmeansImage;
	}

	// Your k-means code goes here
	// Update the array rgb by assigning each entry in the rgb array to its cluster center
	private static void kmeans(int[] rgb, int k){

		int rgbBroken [][] = new int [rgb.length][4]; 
		int steps=10;
		double divVal = 255/steps;

		int [][][] dist = new int[steps+1][steps+1][steps+1]; 
		int [][] topVals = new int[20*k][4];
		int usedtopVal=0;
		double [] clustDist = new double [k];
		double [][] clustMean = new double[k][4];
		double [][] prevClustMean = new double[k][3];
		double [][] prevPrevClustMean = new double[k][3];
		double minClustDist=-1;
		int minclustInd=-1;
		boolean converged=false;
		int converge_count=0;
		int minR=255,minG=255,minB=255,maxR=0,maxG=0,maxB=0;    	

		for(int i=0;i<dist.length;i++){
			for(int j=0;j<dist[i].length;j++){
				for(int p=0; p<dist[i][j].length;p++){
					dist [i][j][p] = 0;
				}
			}
		}

		for(int i=0;i<topVals.length;i++){
			topVals[i][0] =-1;
			topVals[i][1] =-1;
			topVals[i][2] =-1;
			topVals[i][3] =-1;
		}

		int curInt;

		System.out.println("breaking RGBs");
		for(int i=0;i<rgb.length;i++){
			curInt = rgb[i];
			int a=0,b=0,c=0;

			/*int two_24 = -(int)Math.pow(2, 24);
    		int two_16 = -(int)Math.pow(2, 16);
    		int two_8 = -(int)Math.pow(2, 8);

    		rgbBroken[i][0] = curInt/two_24;

    		curInt = curInt % two_24;

    		rgbBroken[i][1] = curInt/two_16;

    		curInt = curInt % two_16;

    		rgbBroken[i][2] = curInt/two_8;
    		a = (int)(rgbBroken[i][0]/divVal);
    		b = (int)(rgbBroken[i][1]/divVal);
    		c = (int)(rgbBroken[i][2]/divVal);

			 */

			Color col = new Color(rgb[i]);

			rgbBroken[i][0] = col.getRed();
			if(rgbBroken[i][0] < minR){
				minR = rgbBroken[i][0];
			}
			if(rgbBroken[i][0] > maxR){
				maxR = rgbBroken[i][0];
			}


			rgbBroken[i][1] = col.getGreen();

			if(rgbBroken[i][1] < minG){
				minG = rgbBroken[i][1];
			}
			if(rgbBroken[i][1] > maxG){
				maxG = rgbBroken[i][1];
			}

			rgbBroken[i][2] = col.getBlue();
			if(rgbBroken[i][2] < minB){
				minB = rgbBroken[i][2];
			}
			if(rgbBroken[i][2] > maxB){
				maxB = rgbBroken[i][2];
			}


			a = (int)(rgbBroken[i][0]/divVal);
			b = (int)(rgbBroken[i][1]/divVal);
			c = (int)(rgbBroken[i][2]/divVal);

			/*System.out.println();
    	    System.out.println(c.getGreen());
    	    System.out.println(c.getBlue());
    	    System.out.println(c.getAlpha());*/




			//System.out.println(" RGB unbroken" + rgb[i]+ " RGB Broken " + rgbBroken[i][0] + " " + rgbBroken[i][1] +  " " + rgbBroken[i][2] + " a is "+ a +" b is" + b+" c is " +c + " Before increment "+dist[a][b][c]);
			dist[a][b][c] = dist[a][b][c] +1;
			//System.out.println("After Increment " +dist[a][b][c] );

		}


		//System.out.println("Finding Tops");
		for(int i=0;i<dist.length;i++){
			for(int j=0;j<dist[i].length;j++){
				for(int p=0; p<dist[i][j].length;p++){

					for(int l=0;l<k;l++){
						if(dist[i][j][p] > topVals[l][3]){
							/*System.out.println("Before  Adding ");
    						for(int z=0;z<k;z++){
    							System.out.print( z+ ":" +topVals[z][3]+" ");
    						}
    						System.out.println();*/

							for(int s=k-1;s>l;s--){
								topVals[s][0] = topVals[s-1][0];
								topVals[s][1] = topVals[s-1][1];
								topVals[s][2] = topVals[s-1][2];
								topVals[s][3] = topVals[s-1][3];
							}


							topVals[l][0] = i;
							topVals[l][1] = j;
							topVals[l][2] = p;
							topVals[l][3] = dist[i][j][p];
							/*System.out.println("Adding " + " i " +i + " j "+j +" p "+p +" with count to " + dist[i][j][p] + " to position "+ l);

        					for(int z=0;z<k;z++){
    							System.out.print( z+ ":" +topVals[z][3]+" ");
    						}
    						System.out.println();*/
							break;
						}

					}

				}
			}
		}

		usedtopVal = k-1;
		for(int i=0;i<k;i++){
			clustMean[i][0] = roundThreeDecimals((topVals[i][0]+1.5)*(divVal));
			clustMean[i][1] = roundThreeDecimals((topVals[i][1]+1.5)*(divVal));
			clustMean[i][2] = roundThreeDecimals((topVals[i][2]+1.5)*(divVal));
			clustMean[i][3] = roundThreeDecimals(topVals[i][3]);
			//System.out.print(topVals[i][3] + " ");
		}

		//System.out.println();

		long itrStartTime;
		long itrEndTime;

		System.out.println("Clustering");
		for(converge_count=0;converge_count<MAX_ITR;converge_count++){
			itrStartTime = System.currentTimeMillis();
			for(int i=0;i<k;i++){
				prevPrevClustMean[i][0] = prevClustMean[i][0];
				prevPrevClustMean[i][1] = prevClustMean[i][1];
				prevPrevClustMean[i][2] = prevClustMean[i][2];

				prevClustMean[i][0] = clustMean[i][0];
				prevClustMean[i][1] = clustMean[i][1];
				prevClustMean[i][2] = clustMean[i][2];
			}

			/*if(converge_count>497)
    		{
	    		System.out.print("Before Iteration");

	    		for(int i=0;i<k;i++){
	    			System.out.print(i+":"+prevClustMean[i][0] + " "+ prevClustMean[i][1] + " "+prevClustMean[i][2]+ " "+ clustMean[i][3] +"; ");
	    		}
	    		System.out.println();
    		}*/


			for(int i=0;i<rgb.length;i++){
				minClustDist =100000000;
				minclustInd=-1;
				for(int j=0;j<k;j++){
					if(clustMean[j][0]+clustMean[j][1]+clustMean[j][2] >0){
						clustDist[j] =  Math.pow((clustMean[j][0]-rgbBroken[i][0]), 2) + Math.pow((clustMean[j][1]-rgbBroken[i][1]), 2) + Math.pow((clustMean[j][2]-rgbBroken[i][2]), 2);
					}else{
						clustDist[j] = 1000000000;	
					}
					if(clustDist[j]<=minClustDist){
						minclustInd =j;
						minClustDist = clustDist[j];
					}
				}

				rgbBroken[i][3] = minclustInd;

			}

			//converge_count++;


			for(int i=0;i<k;i++){
				clustMean[i][0] = 0;
				clustMean[i][1] = 0;
				clustMean[i][2] = 0;
				clustMean[i][3] = 0;
			}



			for(int i=0;i<rgbBroken.length;i++){
				clustMean[rgbBroken[i][3]][0] += rgbBroken[i][0];
				clustMean[rgbBroken[i][3]][1] += rgbBroken[i][2];
				clustMean[rgbBroken[i][3]][2] += rgbBroken[i][3];
				clustMean[rgbBroken[i][3]][3] += 1;
			}



			for(int i=0;i<k;i++){
				if(clustMean[i][3]!=0){
					clustMean[i][0] = roundThreeDecimals(clustMean[i][0]/clustMean[i][3]);
					clustMean[i][1] = roundThreeDecimals(clustMean[i][1]/clustMean[i][3]);
					clustMean[i][2] = roundThreeDecimals(clustMean[i][2]/clustMean[i][3]);
				}else{
					if(usedtopVal<topVals.length-1){
						usedtopVal++;
						clustMean[i][0] = roundThreeDecimals((topVals[i][0]+0.5)*(divVal));
						clustMean[i][1] = roundThreeDecimals((topVals[i][1]+0.5)*(divVal));
						clustMean[i][2] = roundThreeDecimals((topVals[i][2]+0.5)*(divVal));
						clustMean[i][3] =1;
					}else{
						//System.out.println("Error: For Iteration "+converge_count+" Ran Out of Clusters " + topVals.length );
					}
				}
			}

			/*if(converge_count>497)
	    	{
		    	System.out.print("After Iteration " +converge_count +" ");
		    	for(int i=0;i<k;i++){
	    			System.out.print(i+":"+clustMean[i][0] + " "+ clustMean[i][1] + " "+clustMean[i][2]+  " "+ clustMean[i][3] +"; ");
	    		}
	    		System.out.println();
	    	}*/

			converged = true;

			for(int i=0;i<k;i++){
				if( (prevClustMean[i][0] != clustMean[i][0] && prevPrevClustMean[i][0] !=  clustMean[i][0]) || (prevClustMean[i][1] != clustMean[i][1] && prevPrevClustMean[i][1] !=  clustMean[i][1]) || (prevClustMean[i][2] != clustMean[i][2] && prevPrevClustMean[i][2] !=  clustMean[i][2])){
					converged = false;
				}
			}

			itrEndTime = System.currentTimeMillis();
			if(converge_count>497 || converge_count%50==0){
				System.out.println("Total time taken to execute the iteration "+ converge_count+" in ms : " +(itrEndTime - itrStartTime) );
			}

			if(converged){
				break;
			}

		}
		//while(!converged);

		//System.out.println("Converged !! count is " +converge_count);

		/*for(int i=0;i<k;i++){
    		clustMean[i][0] = -clustMean[i][0]*Math.pow(2, 16) + clustMean[i][1]*Math.pow(2, 8)+clustMean[i][2];
	    	}*/

		System.out.println("Writing bck to Array");
		for(int i=0;i<rgb.length;i++){

			Color oldCol = new Color(rgb[i]);

			Color newCol = new Color((int)clustMean[rgbBroken[i][3]][0],(int)clustMean[rgbBroken[i][3]][1],(int)clustMean[rgbBroken[i][3]][2],oldCol.getAlpha());

			//rgb[i] = (int)(clustMean[rgbBroken[i][3]][0]*Math.pow(2, 8)  - rgb[i]%Math.pow(2, 8));

			rgb[i] = newCol.getRGB();
		}


	}

	static int roundThreeDecimals(double d) {
		/*DecimalFormat threeDForm = new DecimalFormat("00.000");
	return Double.valueOf(threeDForm.format(d));*/

		return (int)d;
	}


}
