import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Scanner;


public class BuildDecisionTree {

	int [][] training_set;
	int [][] validation_set;
	int data_length = 0;
	int train_zero_count=0;
	int train_one_count=0;
	String[] metadata = null;
	String train_filePath;
	String test_filePath;
	String valid_filePath;
	String toPrintString="yes";
	DecisionTreeNode igRoot;
	DecisionTreeNode olaRoot;
	DecisionTreeNode viRootPP;
	DecisionTreeNode igRootPP;
	DecisionTreeNode olaRootPP;
	DecisionTreeNode viRoot;
	DecisionTreeNode nodeList[];
	int [] igNodeDepths;
	int zero_count=0;
	int one_count=0;
	int pruneL;
	int pruneK;
	int printHu =1;
	InputStreamReader streamReader = new InputStreamReader(System.in);
	BufferedReader bufReader = new BufferedReader(streamReader);

	public static void main(String[] args){
		try{
			long startTime = System.currentTimeMillis();
			if(args.length<7){
				System.out.println("Few argume please execute the program again with all the 7 arguments");
				return;
			}
			BuildDecisionTree mainClass =new  BuildDecisionTree();
			mainClass.BuildDeciscionTrees(args);


			long endTime = System.currentTimeMillis();

			System.out.println("Total time taken to execute the program in ms : " +(endTime - startTime) );
		}catch(RuntimeException exp){
			exp.printStackTrace();
		}
	}


	public void BuildDeciscionTrees(String args[]){

		System.out.println("Exection Started");

		//program Takes first argument to main function as file path for training set 
		//if there is no argument it assumes current directory as file path of training set 

		try{
			pruneL = Integer.parseInt(args[0]);
		}catch (NumberFormatException e) {
			System.out.println("Error:Could not convert '"+ args[0] + "' into integer");
			return;
		} 

		try{
			pruneK = Integer.parseInt(args[1]);
		}catch (NumberFormatException e) {
			System.out.println("Error:Could not convert '"+ args[1] + "' into integer");
			return;
		} 

		if(null!=args[2] && args[2].length() > 1){
			train_filePath = args[2];
		}else{
			train_filePath = System.getProperty("user.dir")+"//training_set.csv";
		}

		if(null!=args[3] && args[3].length() > 1){
			valid_filePath = args[3];
		}else{
			valid_filePath = System.getProperty("user.dir")+"//validation_set.csv";
		}

		if(null!=args[4] && args[4].length() > 1){
			test_filePath = args[4];
		}else{
			test_filePath = System.getProperty("user.dir")+"//test_set.csv";
		}

		if(null!=args[5]){
			toPrintString = args[5];
		}

		try{
			if(null!= args[6]){
				printHu = Integer.parseInt(args[6]);
				if(printHu>3 || printHu<1){
					System.out.println(" invalid heuristic number "+ args[6]+" Please enter 1 or 2 or 3 instead");
				}
			}
		}catch (NumberFormatException e) {
			System.out.println("Error:Could not convert '"+ args[6] + "' into integer");
			return;
		} 

		long startScanTime = System.currentTimeMillis();

		training_set = readFileData(train_filePath);
		train_zero_count = zero_count;
		train_one_count = one_count;

		long endScanTime = System.currentTimeMillis();

		//System.out.println("Total time taken to process training data ms : " +(endScanTime - startScanTime) );
		//printTrainData();

		//Information gain heuristic
		buildIGtree();
		//testIGTree();

		//one step look ahead heuristic
		buildOLAtree();
		//testOlaTree();

		//Variance impurity heuristic
		buildVItree();
		//testVITree();
		//copyTreeTest = getTreeCopy(igRoot);

		if(pruneK>0 && pruneL>0){
			validation_set =  readFileData(valid_filePath);
			igRootPP = postPruneTree(igRoot);
			olaRootPP = postPruneTree(olaRoot);
			viRootPP = postPruneTree(viRoot);

			/*
			System.out.println("Testing ig PP tree with training data");
			testTreeTrain(igRootPP);
			System.out.println("Testing ig PP tree with Validation data");
			testTreeValid(igRootPP);


			System.out.println("Testing ola PP tree with training data");
			testTreeTrain(olaRootPP);
			System.out.println("Testing ola PP tree with Validation data");
			testTreeValid(olaRootPP);

			System.out.println("Testing vi PP tree with training data");
			testTreeTrain(viRootPP);
			System.out.println("Testing vi PP tree with Validation data");
			testTreeValid(viRootPP);


			System.out.println("Testing ig tree with Validation data");
			testTreeValid(igRoot);

			System.out.println("Testing ola tree with Validation data");
			testTreeValid(olaRoot);

			System.out.println("Testing vi tree with Validation data");
			testTreeValid(viRoot);*/

		}

		testTrees();
		if("yes".equalsIgnoreCase(toPrintString)){
			switch(printHu){
			case 1:	printIGTree();
			if(null!=igRootPP)
				printIGPPTree();
			break;

			case 2: printOlaTree();
			if(null!=olaRootPP)
				printOlaPPTree();
			break;

			case 3:	printVITree();
			if(null!=viRootPP)
				printVIPPTree();
			break;
			default: System.out.println("Could not understant heuristic "+printHu);
			break;
			}
		}

		/*System.out.println("Printing copy of VI Tree");
		printTreeNode(getTreeCopy(viRoot));

		/*try
		{

			System.out.println("Print tree built using Information gain heuristc yes/no ?");
			if(bufReader.readLine().equalsIgnoreCase("yes")){
				printIGTree();
			}

			System.out.println("Print tree built using One step look ahead heuristc yes/no ?");
			if(bufReader.readLine().equalsIgnoreCase("yes")){
				printOlaTree();
			}

			System.out.println("Print tree built using Variance impurity heuristc yes/no ?");
			if(bufReader.readLine().equalsIgnoreCase("yes")){
				printVITree();
			}

		}catch(IOException exp){
			exp.printStackTrace();
		}*/
	}

	public DecisionTreeNode getTreeCopy(DecisionTreeNode node){
		DecisionTreeNode copy = new DecisionTreeNode();

		copy.setCurrentEntropy(node.getCurrentEntropy());
		copy.setAttrInd(node.getAttrInd());
		copy.setCountZero(node.getCountZero());
		copy.setCountOne(node.getCountOne());
		copy.setInfGain(node.getInfGain());
		copy.setZeroPositive(node.getZeroPositive());
		copy.setZeroNegative(node.getZeroNegative());
		copy.setOnePositive(node.getOnePositive());
		copy.setOneNegative(node.getOneNegative());
		copy.setData_ones(node.getData_ones());
		copy.setData_zeros(node.getData_zeros());

		if(null !=node.getUsedAttrInd()){
			int[][] usedAttrInd = new int[node.getUsedAttrInd().length][2];
			for(int i=0; i <node.getUsedAttrInd().length;i++){
				usedAttrInd[i][0] = node.getUsedAttrInd()[i][0];
				usedAttrInd[i][1] = node.getUsedAttrInd()[i][1];
			}
			copy.setUsedAttrInd(usedAttrInd);
		}
		copy.setUsedAttrNo(node.getUsedAttrNo());
		copy.setOlaError(node.getOlaError());
		copy.setLeftPureClass(node.getLeftPureClass());
		copy.setRightPureClass(node.getRightPureClass());
		copy.setNodeNo(node.getNodeNo());

		if(null != node.getLeftChild()){
			copy.setLeftChild(getTreeCopy(node.getLeftChild()));
		}
		if(null != node.getRightChild()){
			copy.setRightChild(getTreeCopy(node.getRightChild()));
		}
		return copy;		
	}

	public double getPPAccuracy(DecisionTreeNode node){
		double accuracy=0;
		double correct=0;
		for(int i =0;i<validation_set.length;i++){
			if(getResult(validation_set[i],node)){
				correct++;
			}
		}

		accuracy = correct/((double)validation_set.length);
		accuracy *= 100;

		return accuracy;
	}

	public void testTrees(){
		//System.out.println("Looking for Test Set in the path "+test_filePath+"");
		File testFile = new File(test_filePath);
		String line;
		int noDataIems =0;
		int igCorrect=0;
		int olaCorrect=0;
		int viCorrect=0;
		int igPPCorrect=0;
		int olaPPCorrect=0;
		int viPPCorrect=0;
		double accuracy;
		Scanner reader;
		String [] temp;
		int [] input = new int[metadata.length];
		try{
			reader = new Scanner(testFile);
			if(reader.hasNextLine()){
				reader.nextLine(); // bypassing meta data
			}

			while(reader.hasNextLine()){
				line = reader.nextLine();
				noDataIems++;
				temp = line.split(",");
				//System.out.println("meta data : "+ line);
				for(int j=0;j<temp.length;j++){
					try{
						input[j] = Integer.parseInt(temp[j]);
					}catch(NumberFormatException exp){
						System.out.println("Could not convert token '" +temp[j] + "' into number in line '" + line+"' assigned 0 instead" );
						exp.printStackTrace();
						input[j] = Integer.parseInt(temp[j]);
					}

				}

				if(getResult(input,igRoot)){
					igCorrect++;
				}

				if(getResult(input,olaRoot)){
					olaCorrect++;
				}

				if(getResult(input,viRoot)){
					viCorrect++;
				}


				if(null!=igRootPP){
					if(getResult(input, igRootPP)){
						igPPCorrect++;
					}
				}

				if(null!=olaRootPP){
					if(getResult(input, olaRootPP)){
						olaPPCorrect++;
					}
				}

				if(null!=viRootPP){
					if(getResult(input, viRootPP)){
						viPPCorrect++;
					}
				}

			}


			accuracy = (double)igCorrect/((double) noDataIems);
			accuracy = accuracy * 100 ;
			accuracy = roundTwoDecimals(accuracy);
			System.out.println("Accuracy of Information gain heuristic tree with test set is "+accuracy + "%");

			accuracy = (double)olaCorrect/((double) noDataIems);
			accuracy = accuracy * 100 ;
			accuracy = roundTwoDecimals(accuracy);
			System.out.println("Accuracy of One step look ahead heuristic tree with test set is "+accuracy + "%");

			accuracy = (double)viCorrect/((double) noDataIems);
			accuracy = accuracy * 100 ;
			accuracy = roundTwoDecimals(accuracy);
			System.out.println("Accuracy of Variance impurity heuristic tree with test set is "+accuracy + "%");

			if(null!=igRootPP){
				accuracy = (double)igPPCorrect/((double) noDataIems);
				accuracy = accuracy * 100 ;
				accuracy = roundTwoDecimals(accuracy);
				System.out.println("Accuracy of Post Pruned Information gain heuristic tree with test set is "+accuracy + "%");
			}

			if(null!=olaRootPP){
				accuracy = (double)olaPPCorrect/((double) noDataIems);
				accuracy = accuracy * 100 ;
				accuracy = roundTwoDecimals(accuracy);
				System.out.println("Accuracy of Post Pruned One step look ahead heuristic tree with test set is "+accuracy + "%");
			}

			if(null!=viRootPP){
				accuracy = (double)viPPCorrect/((double) noDataIems);
				accuracy = accuracy * 100 ;
				accuracy = roundTwoDecimals(accuracy);
				System.out.println("Accuracy of Post Pruned Variance impurity heuristic tree with test set is "+accuracy + "%");
			}

		}catch(FileNotFoundException exp){
			System.out.println("could not find Test set file in path " + test_filePath);
		}
	}

	public void testIGTree(){
		int success =0;
		double accuracy=0;
		for(int i=0;i<training_set.length;i++){
			if(getResult(training_set[i],igRoot)){
				success++;
			}
		}
		accuracy = (double)success/((double) training_set.length);
		accuracy = accuracy * 100 ;
		accuracy = roundTwoDecimals(accuracy);
		System.out.println("Tested IG tree accuracy "+accuracy + "%");
	}

	public void testVITree(){
		int success =0;
		double accuracy=0;
		for(int i=0;i<training_set.length;i++){
			if(getResult(training_set[i],viRoot)){
				success++;
			}
		}
		accuracy = (double)success/((double) training_set.length);
		accuracy = accuracy * 100 ;
		accuracy = roundTwoDecimals(accuracy);
		System.out.println("Tested IG tree accuracy "+accuracy + "%");
	}


	public void testTreeTrain(DecisionTreeNode root){
		int success =0;
		double accuracy=0;
		for(int i=0;i<training_set.length;i++){
			if(getResult(training_set[i],root)){
				success++;
			}
		}
		accuracy = (double)success/((double) training_set.length);
		accuracy = accuracy * 100 ;
		accuracy = roundTwoDecimals(accuracy);
		System.out.println("Tree accuracy against Training set "+accuracy + "%");
	}

	public void testTreeValid(DecisionTreeNode root){
		int success =0;
		double accuracy=0;
		for(int i=0;i<validation_set.length;i++){
			if(getResult(validation_set[i],root)){
				success++;
			}
		}
		accuracy = (double)success/((double) validation_set.length);
		accuracy = accuracy * 100 ;
		accuracy = roundTwoDecimals(accuracy);
		System.out.println("Tree accuracy against Validation set "+accuracy + "%");
	}

	public void testOlaTree(){
		int success =0;
		double accuracy=0;
		for(int i=0;i<training_set.length;i++){
			if(getResult(training_set[i],olaRoot)){
				success++;
			}
		}
		accuracy = (double)success/((double) training_set.length);
		accuracy = accuracy * 100 ;
		accuracy = roundTwoDecimals(accuracy);
		System.out.println("Tested OLA tree accuracy "+accuracy + "%");
	}

	public boolean getResult(int[] input,DecisionTreeNode root){
		DecisionTreeNode currNode;
		int value=-1;
		boolean ret = false;
		currNode = root;

		for(int i=0;value==-1;i++){
			if(input[currNode.getAttrInd()]==0){
				if(currNode.getLeftPureClass() !=-1){
					value=currNode.getLeftPureClass();
				}else{
					currNode = currNode.getLeftChild();
				}
			}else{
				if(currNode.getRightPureClass() !=-1){
					value=currNode.getRightPureClass();
				}else{
					currNode = currNode.getRightChild();
				}
			}

			if(i> input.length-1){
				System.out.println("Infinite loop for data " + input);
			}
		}

		if(value == input[input.length-1]){
			ret = true;
		}


		return ret;
	}

	public void buildOLAtree(){
		nodeList = new DecisionTreeNode[metadata.length-1];

		DecisionTreeNode temp;
		int minOlaError=training_set.length;
		int minErrorInd=0;
		for(int i=0;i<metadata.length-1;i++){
			temp = new DecisionTreeNode();
			temp.setAttrInd(i);
			temp.setUsedAttrNo(0);
			calculateclassCounts(temp);
			temp.setData_zeros(zero_count);
			temp.setData_ones(one_count);
			calculateOlaError(temp);
			nodeList[i]=temp;
			if(temp.getOlaError()<minOlaError){
				minErrorInd = i;
				minOlaError = temp.getOlaError();

			}
		}


		olaRoot = nodeList[minErrorInd];

		//System.out.println("OLA error is min for attribute "+metadata[minErrorInd]+" - "+minOlaError);
		growOlaTree(olaRoot);

		//printOlaTree();

	}

	public void buildVItree(){
		nodeList = new DecisionTreeNode[metadata.length-1];

		DecisionTreeNode temp;
		double entrophy;
		double maxInfGain=0;
		int maxGainInd=0;
		entrophy = calculateVI(zero_count,one_count);
		for(int i=0;i<metadata.length-1;i++){
			temp = new DecisionTreeNode();
			temp.setCurrentEntropy(entrophy);
			temp.setAttrInd(i);
			temp.setUsedAttrNo(0);
			calculateclassCounts(temp);
			temp.setData_zeros(zero_count);
			temp.setData_ones(one_count);
			nodeList[i]=temp;
			calculateVIGain(temp);
			if(temp.getInfGain()>maxInfGain){
				maxGainInd = i;
				maxInfGain = temp.getInfGain();

			}
		}


		viRoot = nodeList[maxGainInd];

		//System.out.println("Inf gain is max for attribute "+metadata[maxGainInd]+"-"+maxInfGain);
		growVITree(viRoot);

		//printVITree();

	}

	public void buildIGtree(){
		nodeList = new DecisionTreeNode[metadata.length-1];

		DecisionTreeNode temp;
		double entrophy;
		double maxInfGain=0;
		int maxGainInd=0;
		entrophy = calculateEntrophy(zero_count,one_count);
		for(int i=0;i<metadata.length-1;i++){
			temp = new DecisionTreeNode();
			temp.setCurrentEntropy(entrophy);
			temp.setAttrInd(i);
			temp.setUsedAttrNo(0);
			calculateclassCounts(temp);
			temp.setData_zeros(zero_count);
			temp.setData_ones(one_count);
			nodeList[i]=temp;
			calculateInfGain(temp);
			if(temp.getInfGain()>maxInfGain){
				maxGainInd = i;
				maxInfGain = temp.getInfGain();

			}
		}


		igRoot = nodeList[maxGainInd];

		//System.out.println("Inf gain is max for attribute "+metadata[maxGainInd]+"-"+maxInfGain);
		growIGTree(igRoot);

		//printIGTree();

	}

	public void growOlaTree(DecisionTreeNode node){
		//System.out.println("Inside Grow Tree attribute " +metadata[node.getAttrInd()]);
		if(node.getUsedAttrNo() == metadata.length-2 ){
			//System.out.println("No more attributes to classify");
			if(node.getZeroNegative() > node.getZeroPositive()){
				node.setLeftPureClass(0);
			}else{
				node.setLeftPureClass(1);
			}

			if(node.getOneNegative() > node.getOnePositive()){
				node.setRightPureClass(0);
			}else{
				node.setRightPureClass(1);
			}
			return;
		}else{
			//System.out.println("Data not pure so growing tree");
			DecisionTreeNode leftNode;
			DecisionTreeNode rightNode;
			int[][] usedAttrInd;
			int[] unusedAttrInd;
			int minOlaError=training_set.length;
			int minErrorInd=0;

			if(node.getLeftPureClass() == -1){
				//System.out.println("searching for left child no of attr used "+node.getUsedAttrNo());
				nodeList = new DecisionTreeNode[metadata.length-1-node.getUsedAttrNo()-1];
				//left child

				//System.out.println("Got current entrophy "+ currentEntropy);
				if(null != node.getUsedAttrInd()){
					//System.out.println("There are "+node.getUsedAttrInd().length+" used attributes");
					usedAttrInd = new int[node.getUsedAttrInd().length+1][2];
					for(int j=0;j<node.getUsedAttrInd().length;j++){
						usedAttrInd[j][0] = node.getUsedAttrInd()[j][0];
						usedAttrInd[j][1] = node.getUsedAttrInd()[j][1];
					}
					usedAttrInd[node.getUsedAttrInd().length][0] = node.getAttrInd();
					usedAttrInd[node.getUsedAttrInd().length][1] = 0;
				}else{
					//System.out.println("There are no used attributes");
					usedAttrInd = new int[1][2];
					usedAttrInd[0][0] = node.getAttrInd();
					usedAttrInd[0][1] = 0;
				}

				//System.out.println("Before calclulating unused attributes");
				unusedAttrInd = getUnusedAttr(usedAttrInd);

				//System.out.println("unused attriburtes are " + unusedAttrInd.length);

				for(int i=0;i<unusedAttrInd.length;i++){
					//System.out.println("making node for unused attribute"+unusedAttrInd[i]);
					leftNode = new DecisionTreeNode();
					leftNode.setUsedAttrInd(usedAttrInd);
					leftNode.setUsedAttrNo(usedAttrInd.length);
					leftNode.setParent(node);
					//leftNode.setCountZero(node.getZeroNegative());
					//leftNode.setCountOne(node.getZeroPositive());
					leftNode.setAttrInd(unusedAttrInd[i]);
					calculateclassCounts(leftNode);
					calculateOlaError(leftNode);
					nodeList[i]=leftNode;
					calculateInfGain(leftNode);
					if(leftNode.getOlaError()<minOlaError){
						minErrorInd = i;
						minOlaError = leftNode.getOlaError();
					}

				}

				node.setLeftChild(nodeList[minErrorInd]);
				//System.out.println("Inf gain is max for attribute "+metadata[nodeList[minErrorInd].getAttrInd()]+"-"+minOlaError);
				if(nodeList[minErrorInd].getZeroNegative()== 0 || nodeList[minErrorInd].getZeroPositive()==0){
					if(nodeList[minErrorInd].getZeroNegative() ==0){
						nodeList[minErrorInd].setLeftPureClass(1);
					}else{
						nodeList[minErrorInd].setLeftPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[minErrorInd].getAttrInd()]+"' is zero class is "+nodeList[minErrorInd].getLeftPureClass() + " Zeros "+nodeList[minErrorInd].getZeroNegative() + " Ones "+nodeList[minErrorInd].getZeroPositive());
				}

				if(nodeList[minErrorInd].getOneNegative()== 0 || nodeList[minErrorInd].getOnePositive()==0){
					if(nodeList[minErrorInd].getOneNegative() ==0){
						nodeList[minErrorInd].setRightPureClass(1);
					}else{
						nodeList[minErrorInd].setRightPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[minErrorInd].getAttrInd()]+"' is one class is "+nodeList[minErrorInd].getRightPureClass() + " Zeros "+nodeList[minErrorInd].getOneNegative() + " Ones "+nodeList[minErrorInd].getOnePositive());
				}

				if( nodeList[minErrorInd].getLeftPureClass() ==-1 || nodeList[minErrorInd].getRightPureClass() ==-1){
					growOlaTree(nodeList[minErrorInd]);
				}

			}


			if(node.getRightPureClass() == -1){
				//System.out.println("Searching for right child no of attr used "+node.getUsedAttrNo());
				nodeList = new DecisionTreeNode[metadata.length-1-node.getUsedAttrNo()-1];
				//right child

				if(null != node.getUsedAttrInd()){
					usedAttrInd = new int[node.getUsedAttrInd().length+1][2];
					for(int j=0;j<node.getUsedAttrInd().length;j++){
						usedAttrInd[j][0] = node.getUsedAttrInd()[j][0];
						usedAttrInd[j][1] = node.getUsedAttrInd()[j][1];
					}
					usedAttrInd[node.getUsedAttrInd().length][0] = node.getAttrInd();
					usedAttrInd[node.getUsedAttrInd().length][1] = 1;
				}else{
					usedAttrInd = new int[1][2];
					usedAttrInd[0][0] = node.getAttrInd();
					usedAttrInd[0][1] = 1;
				}

				unusedAttrInd = getUnusedAttr(usedAttrInd);

				for(int i=0;i<unusedAttrInd.length;i++){
					rightNode = new DecisionTreeNode();
					rightNode.setUsedAttrInd(usedAttrInd);
					rightNode.setUsedAttrNo(usedAttrInd.length);
					rightNode.setParent(node);
					//rightNode.setCountZero(node.getOneNegative());
					//rightNode.setCountOne(node.getOnePositive());
					rightNode.setAttrInd(unusedAttrInd[i]);
					calculateclassCounts(rightNode);
					calculateOlaError(rightNode);
					nodeList[i]=rightNode;
					calculateInfGain(rightNode);
					if(rightNode.getOlaError()<minOlaError){
						minErrorInd = i;
						minOlaError = rightNode.getOlaError();
					}
				}

				node.setRightChild(nodeList[minErrorInd]);
				//System.out.println("Inf gain is max for attribute "+metadata[nodeList[minErrorInd].getAttrInd()]+"-"+minOlaError);
				if(nodeList[minErrorInd].getZeroNegative()== 0 || nodeList[minErrorInd].getZeroPositive()==0){
					if(nodeList[minErrorInd].getZeroNegative() ==0){
						nodeList[minErrorInd].setLeftPureClass(1);
					}else{
						nodeList[minErrorInd].setLeftPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[minErrorInd].getAttrInd()]+"' is zero class is "+nodeList[minErrorInd].getLeftPureClass() + " Zeros "+nodeList[minErrorInd].getZeroNegative() + " Ones "+nodeList[minErrorInd].getZeroPositive());
				}

				if(nodeList[minErrorInd].getOneNegative()== 0 || nodeList[minErrorInd].getOnePositive()==0){
					if(nodeList[minErrorInd].getOneNegative() ==0){
						nodeList[minErrorInd].setRightPureClass(1);
					}else{
						nodeList[minErrorInd].setRightPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[minErrorInd].getAttrInd()]+"' is one class is "+nodeList[minErrorInd].getRightPureClass() + " Zeros "+nodeList[minErrorInd].getOneNegative() + " Ones "+nodeList[minErrorInd].getOnePositive());
				}

				if( nodeList[minErrorInd].getLeftPureClass() ==-1 || nodeList[minErrorInd].getRightPureClass() ==-1){
					growOlaTree(nodeList[minErrorInd]);
				}


			}

		}
	}



	public void growVITree(DecisionTreeNode node){
		//System.out.println("Inside Grow Tree attribute " +metadata[node.getAttrInd()]);
		if(node.getUsedAttrNo() == metadata.length-2 ){
			//System.out.println("No more attributes to classify");
			if(node.getZeroNegative() > node.getZeroPositive()){
				node.setLeftPureClass(0);
			}else{
				node.setLeftPureClass(1);
			}

			if(node.getOneNegative() > node.getOnePositive()){
				node.setRightPureClass(0);
			}else{
				node.setRightPureClass(1);
			}
			return;
		}else{
			//System.out.println("Data not pure so growing tree");
			DecisionTreeNode leftNode;
			DecisionTreeNode rightNode;
			double currentEntropy;
			int[][] usedAttrInd;
			int[] unusedAttrInd;
			double maxInfGain=0;
			int maxGainInd=0;

			if(node.getLeftPureClass() == -1){
				//System.out.println("searching for left child no of attr used "+node.getUsedAttrNo());
				nodeList = new DecisionTreeNode[metadata.length-1-node.getUsedAttrNo()-1];
				//left child
				currentEntropy = calculateVI(node.getZeroNegative(), node.getZeroPositive());

				//System.out.println("Got current entrophy "+ currentEntropy);
				if(null != node.getUsedAttrInd()){
					//System.out.println("There are "+node.getUsedAttrInd().length+" used attributes");
					usedAttrInd = new int[node.getUsedAttrInd().length+1][2];
					for(int j=0;j<node.getUsedAttrInd().length;j++){
						usedAttrInd[j][0] = node.getUsedAttrInd()[j][0];
						usedAttrInd[j][1] = node.getUsedAttrInd()[j][1];
					}
					usedAttrInd[node.getUsedAttrInd().length][0] = node.getAttrInd();
					usedAttrInd[node.getUsedAttrInd().length][1] = 0;
				}else{
					//System.out.println("There are no used attributes");
					usedAttrInd = new int[1][2];
					usedAttrInd[0][0] = node.getAttrInd();
					usedAttrInd[0][1] = 0;
				}

				//System.out.println("Before calclulating unused attributes");
				unusedAttrInd = getUnusedAttr(usedAttrInd);

				//System.out.println("unused attriburtes are " + unusedAttrInd.length);

				for(int i=0;i<unusedAttrInd.length;i++){
					//System.out.println("making node for unused attribute"+unusedAttrInd[i]);
					leftNode = new DecisionTreeNode();
					leftNode.setCurrentEntropy(currentEntropy);
					leftNode.setUsedAttrInd(usedAttrInd);
					leftNode.setUsedAttrNo(usedAttrInd.length);
					leftNode.setParent(node);
					//leftNode.setCountZero(node.getZeroNegative());
					//leftNode.setCountOne(node.getZeroPositive());
					leftNode.setAttrInd(unusedAttrInd[i]);
					calculateclassCounts(leftNode);
					nodeList[i]=leftNode;
					calculateVIGain(leftNode);
					if(leftNode.getInfGain()>maxInfGain){
						maxGainInd = i;
						maxInfGain = leftNode.getInfGain();
					}

				}

				node.setLeftChild(nodeList[maxGainInd]);
				//System.out.println("Inf gain is max for attribute "+metadata[nodeList[maxGainInd].getAttrInd()]+"-"+maxInfGain);
				if(nodeList[maxGainInd].getZeroNegative()== 0 || nodeList[maxGainInd].getZeroPositive()==0){
					if(nodeList[maxGainInd].getZeroNegative() ==0){
						nodeList[maxGainInd].setLeftPureClass(1);
					}else{
						nodeList[maxGainInd].setLeftPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is zero class is "+nodeList[maxGainInd].getLeftPureClass() + " Zeros "+nodeList[maxGainInd].getZeroNegative() + " Ones "+nodeList[maxGainInd].getZeroPositive());
				}

				if(nodeList[maxGainInd].getOneNegative()== 0 || nodeList[maxGainInd].getOnePositive()==0){
					if(nodeList[maxGainInd].getOneNegative() ==0){
						nodeList[maxGainInd].setRightPureClass(1);
					}else{
						nodeList[maxGainInd].setRightPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is one class is "+nodeList[maxGainInd].getRightPureClass() + " Zeros "+nodeList[maxGainInd].getOneNegative() + " Ones "+nodeList[maxGainInd].getOnePositive());
				}

				if( nodeList[maxGainInd].getLeftPureClass() ==-1 || nodeList[maxGainInd].getRightPureClass() ==-1){
					growVITree(nodeList[maxGainInd]);
				}

			}


			if(node.getRightPureClass() == -1){
				//System.out.println("Searching for right child no of attr used "+node.getUsedAttrNo());
				nodeList = new DecisionTreeNode[metadata.length-1-node.getUsedAttrNo()-1];
				//right child
				currentEntropy = calculateVI(node.getOneNegative(), node.getOnePositive());

				if(null != node.getUsedAttrInd()){
					usedAttrInd = new int[node.getUsedAttrInd().length+1][2];
					for(int j=0;j<node.getUsedAttrInd().length;j++){
						usedAttrInd[j][0] = node.getUsedAttrInd()[j][0];
						usedAttrInd[j][1] = node.getUsedAttrInd()[j][1];
					}
					usedAttrInd[node.getUsedAttrInd().length][0] = node.getAttrInd();
					usedAttrInd[node.getUsedAttrInd().length][1] = 1;
				}else{
					usedAttrInd = new int[1][2];
					usedAttrInd[0][0] = node.getAttrInd();
					usedAttrInd[0][1] = 1;
				}

				unusedAttrInd = getUnusedAttr(usedAttrInd);

				for(int i=0;i<unusedAttrInd.length;i++){
					rightNode = new DecisionTreeNode();
					rightNode.setCurrentEntropy(currentEntropy);
					rightNode.setUsedAttrInd(usedAttrInd);
					rightNode.setUsedAttrNo(usedAttrInd.length);
					rightNode.setParent(node);
					//rightNode.setCountZero(node.getOneNegative());
					//rightNode.setCountOne(node.getOnePositive());
					rightNode.setAttrInd(unusedAttrInd[i]);
					calculateclassCounts(rightNode);
					nodeList[i]=rightNode;
					calculateVIGain(rightNode);
					if(rightNode.getInfGain()>maxInfGain){
						maxGainInd = i;
						maxInfGain = rightNode.getInfGain();
					}
				}

				node.setRightChild(nodeList[maxGainInd]);
				//System.out.println("Inf gain is max for attribute "+metadata[nodeList[maxGainInd].getAttrInd()]+"-"+maxInfGain);
				if(nodeList[maxGainInd].getZeroNegative()== 0 || nodeList[maxGainInd].getZeroPositive()==0){
					if(nodeList[maxGainInd].getZeroNegative() ==0){
						nodeList[maxGainInd].setLeftPureClass(1);
					}else{
						nodeList[maxGainInd].setLeftPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is zero class is "+nodeList[maxGainInd].getLeftPureClass() + " Zeros "+nodeList[maxGainInd].getZeroNegative() + " Ones "+nodeList[maxGainInd].getZeroPositive());
				}

				if(nodeList[maxGainInd].getOneNegative()== 0 || nodeList[maxGainInd].getOnePositive()==0){
					if(nodeList[maxGainInd].getOneNegative() ==0){
						nodeList[maxGainInd].setRightPureClass(1);
					}else{
						nodeList[maxGainInd].setRightPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is one class is "+nodeList[maxGainInd].getRightPureClass() + " Zeros "+nodeList[maxGainInd].getOneNegative() + " Ones "+nodeList[maxGainInd].getOnePositive());
				}

				if( nodeList[maxGainInd].getLeftPureClass() ==-1 || nodeList[maxGainInd].getRightPureClass() ==-1){
					growVITree(nodeList[maxGainInd]);
				}


			}

		}


	}
	public void growIGTree(DecisionTreeNode node){
		//System.out.println("Inside Grow Tree attribute " +metadata[node.getAttrInd()]);
		if(node.getUsedAttrNo() == metadata.length-2 ){
			//System.out.println("No more attributes to classify");
			if(node.getZeroNegative() > node.getZeroPositive()){
				node.setLeftPureClass(0);
			}else{
				node.setLeftPureClass(1);
			}

			if(node.getOneNegative() > node.getOnePositive()){
				node.setRightPureClass(0);
			}else{
				node.setRightPureClass(1);
			}
			return;
		}else{
			//System.out.println("Data not pure so growing tree");
			DecisionTreeNode leftNode;
			DecisionTreeNode rightNode;
			double currentEntropy;
			int[][] usedAttrInd;
			int[] unusedAttrInd;
			double maxInfGain=0;
			int maxGainInd=0;

			if(node.getLeftPureClass() == -1){
				//System.out.println("searching for left child no of attr used "+node.getUsedAttrNo());
				nodeList = new DecisionTreeNode[metadata.length-1-node.getUsedAttrNo()-1];
				//System.out.println("node list length should be " + (metadata.length-1-node.getUsedAttrNo()-1) + " used attr no " + node.getUsedAttrNo());
				//left child
				currentEntropy = calculateEntrophy(node.getZeroNegative(), node.getZeroPositive());

				//System.out.println("Got current entrophy "+ currentEntropy);
				if(null != node.getUsedAttrInd()){
					//System.out.println("There are "+node.getUsedAttrInd().length+" used attributes");
					usedAttrInd = new int[node.getUsedAttrInd().length+1][2];
					for(int j=0;j<node.getUsedAttrInd().length;j++){
						usedAttrInd[j][0] = node.getUsedAttrInd()[j][0];
						usedAttrInd[j][1] = node.getUsedAttrInd()[j][1];
					}
					usedAttrInd[node.getUsedAttrInd().length][0] = node.getAttrInd();
					usedAttrInd[node.getUsedAttrInd().length][1] = 0;
				}else{
					//System.out.println("There are no used attributes");
					usedAttrInd = new int[1][2];
					usedAttrInd[0][0] = node.getAttrInd();
					usedAttrInd[0][1] = 0;
				}

				//System.out.println("Before calclulating unused attributes");
				unusedAttrInd = getUnusedAttr(usedAttrInd);

				//System.out.println("unused attriburtes are " + unusedAttrInd.length);

				for(int i=0;i<unusedAttrInd.length;i++){
					//System.out.println("making node for unused attribute"+unusedAttrInd[i]);
					leftNode = new DecisionTreeNode();
					leftNode.setCurrentEntropy(currentEntropy);
					leftNode.setUsedAttrInd(usedAttrInd);
					leftNode.setUsedAttrNo(usedAttrInd.length);
					leftNode.setParent(node);
					//leftNode.setCountZero(node.getZeroNegative());
					//leftNode.setCountOne(node.getZeroPositive());
					leftNode.setAttrInd(unusedAttrInd[i]);
					calculateclassCounts(leftNode);
					nodeList[i]=leftNode;
					calculateInfGain(leftNode);
					if(leftNode.getInfGain()>maxInfGain){
						maxGainInd = i;
						maxInfGain = leftNode.getInfGain();
					}

				}

				node.setLeftChild(nodeList[maxGainInd]);
				//System.out.println("Inf gain is max for attribute "+metadata[nodeList[maxGainInd].getAttrInd()]+"-"+maxInfGain);
				if(nodeList[maxGainInd].getZeroNegative()== 0 || nodeList[maxGainInd].getZeroPositive()==0){
					if(nodeList[maxGainInd].getZeroNegative() ==0){
						nodeList[maxGainInd].setLeftPureClass(1);
					}else{
						nodeList[maxGainInd].setLeftPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is zero class is "+nodeList[maxGainInd].getLeftPureClass() + " Zeros "+nodeList[maxGainInd].getZeroNegative() + " Ones "+nodeList[maxGainInd].getZeroPositive());
				}

				if(nodeList[maxGainInd].getOneNegative()== 0 || nodeList[maxGainInd].getOnePositive()==0){
					if(nodeList[maxGainInd].getOneNegative() ==0){
						nodeList[maxGainInd].setRightPureClass(1);
					}else{
						nodeList[maxGainInd].setRightPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is one class is "+nodeList[maxGainInd].getRightPureClass() + " Zeros "+nodeList[maxGainInd].getOneNegative() + " Ones "+nodeList[maxGainInd].getOnePositive());
				}

				if( nodeList[maxGainInd].getLeftPureClass() ==-1 || nodeList[maxGainInd].getRightPureClass() ==-1){
					growIGTree(nodeList[maxGainInd]);
				}

			}


			if(node.getRightPureClass() == -1){
				//System.out.println("Searching for right child no of attr used "+node.getUsedAttrNo());
				nodeList = new DecisionTreeNode[metadata.length-1-node.getUsedAttrNo()-1];
				//right child
				currentEntropy = calculateEntrophy(node.getOneNegative(), node.getOnePositive());

				if(null != node.getUsedAttrInd()){
					usedAttrInd = new int[node.getUsedAttrInd().length+1][2];
					for(int j=0;j<node.getUsedAttrInd().length;j++){
						usedAttrInd[j][0] = node.getUsedAttrInd()[j][0];
						usedAttrInd[j][1] = node.getUsedAttrInd()[j][1];
					}
					usedAttrInd[node.getUsedAttrInd().length][0] = node.getAttrInd();
					usedAttrInd[node.getUsedAttrInd().length][1] = 1;
				}else{
					usedAttrInd = new int[1][2];
					usedAttrInd[0][0] = node.getAttrInd();
					usedAttrInd[0][1] = 1;
				}

				unusedAttrInd = getUnusedAttr(usedAttrInd);

				for(int i=0;i<unusedAttrInd.length;i++){
					rightNode = new DecisionTreeNode();
					rightNode.setCurrentEntropy(currentEntropy);
					rightNode.setUsedAttrInd(usedAttrInd);
					rightNode.setUsedAttrNo(usedAttrInd.length);
					rightNode.setParent(node);
					//rightNode.setCountZero(node.getOneNegative());
					//rightNode.setCountOne(node.getOnePositive());
					rightNode.setAttrInd(unusedAttrInd[i]);
					calculateclassCounts(rightNode);
					nodeList[i]=rightNode;
					calculateInfGain(rightNode);
					if(rightNode.getInfGain()>maxInfGain){
						maxGainInd = i;
						maxInfGain = rightNode.getInfGain();
					}
				}

				node.setRightChild(nodeList[maxGainInd]);
				//System.out.println("Inf gain is max for attribute "+metadata[nodeList[maxGainInd].getAttrInd()]+"-"+maxInfGain);
				if(nodeList[maxGainInd].getZeroNegative()== 0 || nodeList[maxGainInd].getZeroPositive()==0){
					if(nodeList[maxGainInd].getZeroNegative() ==0){
						nodeList[maxGainInd].setLeftPureClass(1);
					}else{
						nodeList[maxGainInd].setLeftPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is zero class is "+nodeList[maxGainInd].getLeftPureClass() + " Zeros "+nodeList[maxGainInd].getZeroNegative() + " Ones "+nodeList[maxGainInd].getZeroPositive());
				}

				if(nodeList[maxGainInd].getOneNegative()== 0 || nodeList[maxGainInd].getOnePositive()==0){
					if(nodeList[maxGainInd].getOneNegative() ==0){
						nodeList[maxGainInd].setRightPureClass(1);
					}else{
						nodeList[maxGainInd].setRightPureClass(0);
					}
					//System.out.println("Data pure when '"+metadata[nodeList[maxGainInd].getAttrInd()]+"' is one class is "+nodeList[maxGainInd].getRightPureClass() + " Zeros "+nodeList[maxGainInd].getOneNegative() + " Ones "+nodeList[maxGainInd].getOnePositive());
				}

				if( nodeList[maxGainInd].getLeftPureClass() ==-1 || nodeList[maxGainInd].getRightPureClass() ==-1){
					growIGTree(nodeList[maxGainInd]);
				}


			}

		}
	}

	public void printIGTree(){
		System.out.println("Printing Information Gain tree");
		printTreeNode(igRoot);
	}
	public void printIGPPTree(){
		System.out.println("Printing Post Pruned Information Gain tree");
		printTreeNode(igRootPP);
	}

	public void printOlaTree(){
		System.out.println("Printing One step Look Ahead tree");
		printTreeNode(olaRoot);
	}
	public void printOlaPPTree(){
		System.out.println("Printing Post Pruned One step Look Ahead tree");
		printTreeNode(olaRootPP);
	}
	public void printVITree(){
		System.out.println("Printing Variance Impurity tree");
		printTreeNode(viRoot);
	}
	public void printVIPPTree(){
		System.out.println("Printing Post Pruned Variance Impurity tree");
		printTreeNode(viRootPP);
	}

	public void printTreeNode(DecisionTreeNode node){
		//printing when node attribute = 0
		for(int i=0;i<node.getUsedAttrNo();i++){
			System.out.print("|");
		}
		System.out.print(metadata[node.getAttrInd()] + " = 0 : ");
		if(node.getLeftChild()!=null){
			if((node.getLeftChild().getLeftPureClass() == node.getLeftChild().getRightPureClass()) && (node.getLeftChild().getLeftPureClass()!=-1) ){
				System.out.println(node.getLeftChild().getLeftPureClass());
			}else{
				System.out.println();
				printTreeNode(node.getLeftChild());
			}
		}else{
			System.out.println(node.getLeftPureClass());
		}

		//printing when node attribute = 1
		for(int i=0;i<node.getUsedAttrNo();i++){
			System.out.print("|");
		}
		System.out.print(metadata[node.getAttrInd()] + " = 1 : ");
		if(node.getRightChild()!=null){
			if((node.getRightChild().getLeftPureClass() == node.getRightChild().getRightPureClass()) && (node.getRightChild().getLeftPureClass()!=-1) ){
				System.out.println(node.getRightChild().getLeftPureClass());
			}else{
				System.out.println();
				printTreeNode(node.getRightChild());
			}
		}else{
			System.out.println(node.getRightPureClass());
		}		
	}

	public int[] getUnusedAttr(int[][] usedAttrInd){
		//System.out.println("inside getUnusedAttr metadata.length-1-usedAttrInd.length is "+ (metadata.length-1-usedAttrInd.length) );
		int [] unusedAttrInd = new int[metadata.length-1-usedAttrInd.length];
		boolean used=false;
		int k=0;
		for(int i=0;i<metadata.length-1;i++){
			//System.out.println("Checking status of attribute "+i);
			used=false;
			for(int j=0;j<usedAttrInd.length;j++){
				if(usedAttrInd[j][0] == i){
					used = true;
					//System.out.println("Attribute " + i +" is used");
					break;
				}
			}
			if(!used){
				unusedAttrInd[k] = i;
				//System.out.println("Attribute " + i +" is unused");
				k++;
			}
		}

		return unusedAttrInd;
	}

	public void calculateclassCounts(DecisionTreeNode node){

		int chkAttrInd;
		int chkAttrClass;
		boolean rowQualify;

		for(int i=0;i<training_set.length;i++){
			rowQualify = true;

			if(node.getUsedAttrInd() !=null){
				for(int j=0;j<node.getUsedAttrInd().length;j++){
					chkAttrInd = node.getUsedAttrInd()[j][0];
					chkAttrClass = node.getUsedAttrInd()[j][1];
					if(training_set[i][chkAttrInd] != chkAttrClass){
						rowQualify = false;
						break;
					}
				}
			}

			if(rowQualify){

				if(training_set[i][node.getAttrInd()] ==0){
					node.setCountZero(node.getCountZero()+1);

					if(training_set[i][metadata.length-1] ==0){
						node.setZeroNegative(node.getZeroNegative()+1);
					}else{
						node.setZeroPositive(node.getZeroPositive()+1);
					}

				}else{
					node.setCountOne(node.getCountOne()+1);

					if(training_set[i][metadata.length-1] ==0){
						node.setOneNegative(node.getOneNegative()+1);
					}else{
						node.setOnePositive(node.getOnePositive()+1);
					}
				}

			}
		}

		//System.out.println("For attribute " + metadata[node.getAttrInd()] + " \nZero count:"+node.getCountZero() + "("+node.getZeroNegative()+","+node.getZeroPositive()+")");
		//System.out.println("One Count:"+node.getCountOne()+"("+node.getOneNegative()+","+node.getOnePositive()+")");

	}

	public double calculateVI(double zeros,double ones){
		double ret=1;

		if(zeros+ones >0){
			ret = zeros/(zeros+ones);
			ret *= ones/(zeros+ones); 

		}
		//System.out.println("For input values "+zeros+" zeros "+ones +" ones Entrophy is "+ret);
		return ret;

	}
	public double calculateEntrophy(double zeros,double ones){
		double ret=0;

		if(zeros>0 && ones >0){

			//			ret += ((double)zeros/(double)(zeros+ones))*Math.log10((double)(zeros+ones)/(double)zeros);
			//			ret += ((double)ones/(double)(zeros+ones))*Math.log10((double)(zeros+ones)/(double)ones);
			ret += (zeros/(zeros+ones))*Math.log10((zeros+ones)/zeros);
			ret += (ones/(zeros+ones))*Math.log10((zeros+ones)/ones);
			ret = ret/Math.log10(2);
		}
		//System.out.println("For input values "+zeros+" zeros "+ones +" ones Entrophy is "+ret);
		return ret;
	}

	public void calculateVIGain(DecisionTreeNode node){

		double newEntrophy=0;
		double zeros = node.getCountZero();
		double ones = node.getCountOne();


		newEntrophy += (zeros/(zeros+ones))*calculateVI(node.getZeroNegative(), node.getZeroPositive());
		newEntrophy += (ones/(zeros+ones))*calculateVI(node.getOneNegative(), node.getOnePositive());

		node.setInfGain(node.getCurrentEntropy()- newEntrophy);
		//System.out.println("VI gain is:"+node.getInfGain());

	}
	public void calculateInfGain(DecisionTreeNode node){

		double newEntrophy=0;
		double zeros = node.getCountZero();
		double ones = node.getCountOne();


		newEntrophy += (zeros/(zeros+ones))*calculateEntrophy(node.getZeroNegative(), node.getZeroPositive());
		newEntrophy += (ones/(zeros+ones))*calculateEntrophy(node.getOneNegative(), node.getOnePositive());

		node.setInfGain(node.getCurrentEntropy()- newEntrophy);
		//System.out.println("Inf gain is:"+node.getInfGain());

	}

	public void calculateOlaError(DecisionTreeNode node){
		int olaError=0;

		if(node.getZeroNegative()>node.getZeroPositive()){
			olaError += node.getZeroPositive();
		}else{
			olaError += node.getZeroNegative();
		}


		if(node.getOneNegative()>node.getOnePositive()){
			olaError += node.getOnePositive();
		}else{
			olaError += node.getOneNegative();
		}

		node.setOlaError(olaError);
	}

	public void printTrainData(){

		System.out.println("Printing Training data");
		for(int i=0;i<metadata.length;i++){
			System.out.print(metadata[i]);
			if(i!=metadata.length-1)
				System.out.print(",");
		}
		System.out.println();

		for(int i=0;i<training_set.length;i++){
			for(int j=0;j<training_set[0].length;j++){
				System.out.print(training_set[i][j]);
				if(j!=metadata.length-1)
					System.out.print(",");
			}
			System.out.println();
		}

	}

	public int[][] readFileData(String filePath){
		Scanner reader;
		String[] temp;
		int i=0;
		int [][] data = null;
		data_length =0;
		zero_count =0;
		one_count=0;
		try{
			//System.out.println("Looking for Data file in the path: "+filePath);
			File trainFile = new File(filePath);
			String line;

			reader = new Scanner(trainFile); // reading data length
			while(reader.hasNextLine()) {
				data_length++;
				reader.nextLine();
			}

			//System.out.println("total number of lines"+data_length);
			data_length--;

			trainFile = new File(filePath);	
			reader = new Scanner(trainFile);
			if(reader.hasNextLine()){
				line = reader.nextLine();
				if(null == metadata){
					metadata = line.split(",");
				}
				//System.out.println("meta data : "+ line);
			}

			if(data_length>0 && metadata!=null){
				data = new int[data_length][metadata.length];


				while(reader.hasNextLine()){
					line = reader.nextLine();
					//System.out.println("line: "+i+" :" +  line);
					temp = line.split(",");

					for(int j=0;j<temp.length;j++){
						try{
							data [i][j] = Integer.parseInt(temp[j]);
						}catch(NumberFormatException exp){
							System.out.println("Could not convert token " +temp[j] + " into number in line '" + line+"' assigned 0 instead" );
							exp.printStackTrace();
							data [i][j] = Integer.parseInt(temp[j]);
						}

					}
					if(data [i][metadata.length-1] ==0){
						zero_count++;
					}else{
						one_count++;
					}

					i++;
				}	

				//System.out.println("total "+i + " training data items processed");
				//System.out.println("zero count:"+zero_count + " one count:"+one_count);
			}

			//System.out.println("Reading Data set successful");


		}catch(FileNotFoundException e){
			System.out.println("Not able to open Data file in the path: "+filePath);
			e.printStackTrace();
		}

		return data;
	}


	public DecisionTreeNode postPruneTree(DecisionTreeNode root){
		DecisionTreeNode bestTree = root;
		int rand;
		int noNodes;
		int pInd;
		double bestAccuracy = getPPAccuracy(bestTree);
		//System.out.println("Printing tree before Post pruning");
		//printTreeNode(root);
		double currAccuracy;
		boolean deleted;
		for(int i=0;i<pruneL;i++){
			DecisionTreeNode  copy = getTreeCopy(root);
			rand = (int)(Math.random()*(pruneK-1));
			rand++;
			for(int j=0;j<rand;j++){
				noNodes = numberTreeNodes(copy,1);
				pInd = (int)(Math.random()*(noNodes-1));
				pInd++;
				deleted = removeNode(copy,pInd);
				if(!deleted){
					//System.out.println("already removed "+j+" nodes could not remove node " + pInd + " max nodes present " +noNodes);
				}
			}
			currAccuracy = getPPAccuracy(copy);
			//System.out.println("Accuracy of intermediate post pruning tree is :"+currAccuracy + "%");
			//System.out.println("Printing intermediate post pruning tree");
			//printTreeNode(copy);
			if(currAccuracy>bestAccuracy){
				bestAccuracy = currAccuracy;
				bestTree = copy;
			}
		}
		//System.out.println("Accuracy of tree after post pruning is tree :"+bestAccuracy+"%");
		//System.out.println("Printing intermediate post pruning tree");
		//printTreeNode(bestTree);
		return bestTree;
	}

	public boolean removeNode(DecisionTreeNode node,int nodeNo){

		boolean deleted = false;
		DecisionTreeNode parent;
		if(node!=null){
			//System.out.println("Inside remove Node "+nodeNo + " current nodeNo" + node.getNodeNo());
			if(node.getNodeNo() == nodeNo){
				parent = node.getParent();
				if(parent!=null){
					//System.out.println("left child " + parent.getLeftChild() + " node "+node + " right child " + parent.getRightChild());

					//System.out.println("Current Node Attribute Ind" + node.getAttrInd());


					if(parent.getLeftChild() == node){
						//System.out.println("Left Child Attribute Ind" + parent.getLeftChild().getAttrInd());	
						if(parent.getZeroNegative()>parent.getZeroPositive()){
							parent.setLeftPureClass(0);
							parent.setLeftChild(null);
							node.setParent(null);
							deleted=true;
						}else{
							parent.setLeftPureClass(1);
							parent.setLeftChild(null);
							node.setParent(null);
							deleted=true;
						}
					}


					if(parent.getRightChild() == node){
						//System.out.println("Right Child Attribute Ind" + parent.getRightChild().getAttrInd());	
						if(parent.getOneNegative()>parent.getOnePositive()){
							parent.setRightPureClass(0);
							parent.setRightChild(null);
							node.setParent(null);
							deleted = true;
						}else{
							parent.setRightPureClass(1);
							parent.setRightChild(null);
							node.setParent(null);
							deleted=true;
						}
					}
				}else{
					if(node.getData_zeros()>node.getData_ones()){
						node.setRightPureClass(0);
						node.setLeftPureClass(0);
					}else{
						node.setRightPureClass(1);
						node.setLeftPureClass(1);
					}
					node.setLeftChild(null);
					node.setRightChild(null);
					deleted=true;
				}
			}


			if(!deleted){
				deleted = removeNode(node.getLeftChild(),nodeNo);
			}

			if(!deleted){
				deleted = removeNode(node.getRightChild(),nodeNo);
			}
		}

		//if(deleted)
		//	System.out.println("Removed Node " + nodeNo+ " curent root no "+node.getNodeNo());
		return deleted;
	}

	public int numberTreeNodes(DecisionTreeNode curNode,int num){

		curNode.setNodeNo(num);

		if(curNode.getLeftChild() !=null){
			num = numberTreeNodes(curNode.getLeftChild(), num+1);
		}

		if(curNode.getRightChild() !=null){
			num = numberTreeNodes(curNode.getRightChild(), num+1);
		}

		return num;
	}


	double roundTwoDecimals(double d) {
		DecimalFormat threeDForm = new DecimalFormat("00.00");
		return Double.valueOf(threeDForm.format(d));
	}

}
