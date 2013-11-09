import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ABOpening {

	char [] inBoard = new char [23];
	char [] outBoard = new char [23];
	String inPath;
	String outPath;	
	int treeDepth=0;
	int inWhiteCount=0;
	int inBlackCount=0;
	TreeNode root;
	int nodesEvaluated=0;

	public static void main(String[] args){

		ABOpening gameOpening = new ABOpening(); 

		long startTime = System.currentTimeMillis();
		if(args.length>1){
			gameOpening.addCoin(args);
		}else{
			System.out.println("Please enter both input and output file names and re-execute the program");
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Total time taken to execute the program in sec : " + (double)(endTime - startTime)/1000 );

	}

	public void addCoin(String [] args){
		if(args.length >0 && null!=args[0] && args[0].length()>1){
			inPath = args[0];
		}else{
			inPath = System.getProperty("user.dir")+"//board1.txt";
		}

		if(args.length >1 && null!=args[1] &&  args[1].length()>1){
			outPath = args[1];
		}else{
			outPath = System.getProperty("user.dir")+"//board2.txt"; 
		}

		if(args.length >2 && null!=args[2]){
			treeDepth = Integer.parseInt(args[2]);
		}else{
			treeDepth = 2; 
		}

		try{
			File inFile = new File(inPath);
			Scanner reader = new Scanner(inFile);
			String line = reader.nextLine();
			char c;
			//sSystem.out.println("number of characters are "+ line.length());
			if(line.length()>=23){
				for(int i=0;i<23;i++){
					c = line.charAt(i);
					if(c=='W' ||c=='w' ){
						inBoard[i] = 'W';
						inWhiteCount++;
					}else if(c=='B' ||c=='b'){
						inBoard[i] = 'B';
						inBlackCount++;
					}else{
						inBoard[i] = 'x';
					}
				}
			}else{
				System.out.println("Please enter all places in board");
			}

			System.out.println("Printing input board");
			printBoard(inBoard);
			root =new TreeNode();
			root.setDepth(0);
			root.setBoard(getBoardCopy(inBoard));
			buildAddTree(root);


			//System.out.println("Printing Tree");
			//printTreeNode(root);

			applyABMinMax(root);

			System.out.print("Input board is: ");
			System.out.print(inBoard);
			System.out.println();

			System.out.print("Board Position: ");
			for(int i=0;i<23;i++){
				System.out.print(root.getBoard()[i]);
			}
			System.out.println();

			System.out.println("Positions evaluated by static estimation:"+nodesEvaluated);
			System.out.println("MINMAX estimate: "+root.getStatEst());
			System.out.println("Output board is");

			printBoard(root.getBoard());

			writeToFile(root.getBoard());

		}catch (IOException e){
			System.out.println("Exception Occurred !! ");
			e.printStackTrace();	
		}

	}


	public void writeToFile(char[] board){

		File outFile =  new File(outPath);
		String strContent  = "";


		for(int i=0;i<23;i++){
			strContent += board[i];
		}
		try{

			if(!outFile.exists())
				outFile.createNewFile();

			FileOutputStream outStream = new FileOutputStream(outPath);

			outStream.write(strContent.getBytes());
			outStream.close();

		}catch(IOException exp){
			System.out.println("Exception Occurred");
			exp.printStackTrace();
		}
	}

	public void applyABMinMax(TreeNode node){
		//System.out.println("applyABMinMax" + node.getDepth());

		if(node.getChilds() != null &&node.getChilds().size()>0){

			if(node.isStatEval()){

				if(node.getDepth()%2==0){
					if(node.getStatEst() < node.getParent().getStatLess() ){
						node.getParent().setStatLess(node.getStatEst());
					}
				}else{
					if(node.getStatEst() > node.getParent().getStatGreat() ){
						node.getParent().setStatGreat(node.getStatEst());
					}
				}


				node.setStatEval(true);
				return;
			}else{
				for(int i=0;i<node.getChilds().size();i++){


					applyABMinMax(node.getChilds().get(i));


					if(node.getDepth()!=0){
						if(node.getDepth()%2==0 ){

							//System.out.println("Node parent is " + node.getParent());

							if(node.getStatGreat()>node.getParent().getStatLess()){
								return;
							}
						}else{

							if(node.getStatLess()<node.getParent().getStatGreat()){
								return;
							}


						}
					}	
				}	


				if(node.getDepth()%2==0 ){

					node.setStatEst(node.getStatGreat());
					node.setStatEval(true);

					if(node.getDepth()!=0){
						if(node.getStatEst() < node.getParent().getStatLess() ){
							node.getParent().setStatLess(node.getStatEst());
						}
					}	

				}else{

					node.setStatEst(node.getStatLess());
					node.setStatEval(true);

					if(node.getDepth()!=0){
						if(node.getStatEst() > node.getParent().getStatGreat() ){
							node.getParent().setStatGreat(node.getStatEst());
						}
					}	

				}

			}

			if(node.getDepth()==0){
				for(int i=0;i<node.getChilds().size();i++){
					if(node.getChilds().get(i).getStatEst() == node.getStatEst()){
						node.setBoard(node.getChilds().get(i).getBoard());
						break;
					}
				}
			}



		}else{
			//System.out.println("Evaluating leaf");
			getOpenStatEst(node);
			if(node.getDepth()%2==0){
				if(node.getStatEst() < node.getParent().getStatLess() ){
					node.getParent().setStatLess(node.getStatEst());
				}
			}else{
				if(node.getStatEst() > node.getParent().getStatGreat() ){
					node.getParent().setStatGreat(node.getStatEst());
				}
			}


			nodesEvaluated++;
		}
	}

	char[] getBoardCopy(char[] board){
		char [] copy = new char [23];
		//System.out.println("inside print board printing original board");
		//printBoard(board);
		for(int i=0;i<23;i++){
			copy[i] = board[i];
		}

		//System.out.println("printing copy of board ");
		//printBoard(copy);
		return copy;
	}
	public void buildAddTree(TreeNode node){
		char c;
		int whiteCount=0,blackCount=0;
		ArrayList<Integer> emptyInd = new ArrayList<Integer>();


		// System.out.println("looking for empty spaces");
		for(int i = 0; i < 23; i++) {
			c = node.getBoard()[i];
			//System.out.println("c is "+ c + " node value "+node.getBoard()[i]);
			if(c=='x' || c=='X' || c==' '){
				emptyInd.add(i);
			}else if(c=='w' || c=='W'){
				whiteCount++;
			}else if(c=='b' || c=='B'){
				blackCount++;
			}

		}

		//System.out.println("Empty spaces are " + emptyInd);


		//System.out.println("inside buildTree depth " + node.getDepth());
		if(node.getDepth() == treeDepth || whiteCount <0 || blackCount <0 ){
			//System.out.println("inside if ");
			//getOpenStatEst(node);
			//nodesEvaluated++;
			//System.out.println("Static Estimate is " + node.getStatEst());
		}else{
			double statEst;
			//System.out.println("inside else ");
			ArrayList<TreeNode> childs = new ArrayList<TreeNode>();
			ArrayList<char[]> allBoards = new ArrayList<char[]>();

			if(node.getDepth()%2==0){
				c='W';
				statEst=-1000000000;
			}else{
				c='B';
				statEst=1000000000;
			}

			for(int i=0;i<emptyInd.size();i++){
				allBoards = new ArrayList<char[]>();

				generateAdd(c,node.getBoard(),emptyInd.get(i),allBoards);

				//System.out.println("all Boards size is " + allBoards.size());

				for(int j=0;j<allBoards.size();j++){

					//building tree
					TreeNode newNode = new TreeNode();
					newNode.setDepth(node.getDepth()+1);
					newNode.setParent(node);

					//System.out.println("Has set node parent " + newNode.getParent());
					//System.out.println(" node parent great value is " + newNode.getParent().getStatGreat());


					newNode.setBoard(allBoards.get(j));
					//System.out.println("Add new board to child " + allBoards.get(j));
					buildAddTree(newNode);

					/*if(node.getDepth()%2==0){
						if(newNode.getStatEst() > statEst){
							statEst = newNode.getStatEst(); 
							bestBoard = newNode.getBoard();
						}

					}else{
						if(newNode.getStatEst() < statEst){
							statEst = newNode.getStatEst(); 
							bestBoard = newNode.getBoard();
						}
					}*/

					childs.add(newNode);

				}
			}




			node.setChilds(childs);
			/*node.setStatEst(statEst);
		if(node.getDepth() ==0){
			//node.setIndex(bestInd);
			node.setBoard(bestBoard);
		}*/
		}



	}

	public void generateAdd(char c,char[] board,int ind,ArrayList<char[]> allBoard){
		char[] newBoard;
		char[] tempBoard;
		newBoard = getBoardCopy(board);

		newBoard[ind] = c;
		//System.out.println("Placing " + c + " at ind " + ind);

		if(isCloseMill(ind,newBoard)){
			for(int i=0;i<23;i++){
				if(newBoard[i]!=c && newBoard[i]!='x'){
					tempBoard = getBoardCopy(newBoard);
					if(!isCloseMill(i,tempBoard)){
						tempBoard[i] = 'x';
						allBoard.add(tempBoard);
					}else{

						int tempWhite = 0;
						int tempBlack = 0;
						char temp;

						tempWhite = 0;
						tempBlack = 0;

						for(int k = 0; k < 23; k++) {
							temp = tempBoard[k];
							if(temp=='w' || temp=='W'){
								tempWhite++;
							}else if(temp=='b' || temp=='B'){
								tempBlack++;
							}

						}

						if((tempBlack == 3 && c=='W')||(tempWhite == 3 && c=='B') ){
							tempBoard[i] = 'x';
							//System.out.println("Removing ind " + i);
							allBoard.add(tempBoard);
						}

						//System.out.println("mill encountered so not removing");

					}
				}
			}


		}else{
			allBoard.add(newBoard);
		}

	}

	public boolean isCloseMill(int ind,char[] board){
		boolean ret = false;
		switch(ind){

		case 0: if(board[ind] == board[1] && board[ind] == board[2] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[8] && board[ind] == board[20] && board[ind]!='x'){
			ret = true;
		}else if(board[ind] == board[3] && board[ind] == board[6] && board[ind]!='x'){
			ret = true;
		}
		break;

		case 1: if(board[ind] == board[0] && board[ind] == board[2] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 2: if(board[ind] == board[0] && board[ind] == board[1] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[13] && board[ind] == board[22] && board[ind]!='x'){
			ret = true;
		}else if(board[ind] == board[5] && board[ind] == board[7] && board[ind]!='x'){
			ret = true;
		}
		break;

		case 3: if(board[ind] == board[0] && board[ind] == board[6] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[4] && board[ind] == board[5] && board[ind]!='x'){
			ret = true;
		}else if(board[ind] == board[9] && board[ind] == board[17] && board[ind]!='x'){
			ret = true;
		}
		break;

		case 4: if(board[ind] == board[3] && board[ind] == board[5] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 5: if(board[ind] == board[7] && board[ind] == board[2] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[3] && board[ind] == board[4] && board[ind]!='x'){
			ret = true;
		}else if(board[ind] == board[12] && board[ind] == board[19] && board[ind]!='x'){
			ret = true;
		}
		break;

		case 6: if(board[ind] == board[10] && board[ind] == board[14] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[0] && board[ind] == board[3] && board[ind]!='x'){
			ret =true;
		}
		break;	

		case 7: if(board[ind] == board[11] && board[ind] == board[16] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[2] && board[ind] == board[5] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 8: if(board[ind] == board[9] && board[ind] == board[10] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[0] && board[ind] == board[20] && board[ind]!='x'){
			ret = true;
		}
		break;

		case 9: if(board[ind] == board[3] && board[ind] == board[17] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[8] && board[ind] == board[10] && board[ind]!='x'){
			ret = true;
		}
		break;

		case 10: if(board[ind] == board[8] && board[ind] == board[9] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[6] && board[ind] == board[14] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 11: if(board[ind] == board[12] && board[ind] == board[13] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[7] && board[ind] == board[16] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 12: if(board[ind] == board[11] && board[ind] == board[13] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[5] && board[ind] == board[19] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 13: if(board[ind] == board[11] && board[ind] == board[12] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[2] && board[ind] == board[22] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 14: if(board[ind] == board[15] && board[ind] == board[16] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[17] && board[ind] == board[20] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[6] && board[ind] == board[10] && board[ind]!='x'){
			ret =true;
		}
		break;		

		case 15: if(board[ind] == board[18] && board[ind] == board[21] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[14] && board[ind] == board[16] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 16: if(board[ind] == board[7] && board[ind] == board[11] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[19] && board[ind] == board[22] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[14] && board[ind] == board[15] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 17: if(board[ind] == board[3] && board[ind] == board[9] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[14] && board[ind] == board[20] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[18] && board[ind] == board[19] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 18: if(board[ind] == board[15] && board[ind] == board[21] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[17] && board[ind] == board[19] && board[ind]!='x'){
			ret =true;
		}
		break;	

		case 19: if(board[ind] == board[5] && board[ind] == board[12] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[16] && board[ind] == board[22] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[17] && board[ind] == board[18] && board[ind]!='x'){
			ret =true;
		}
		break;


		case 20: if(board[ind] == board[0] && board[ind] == board[8] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[14] && board[ind] == board[17] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[21] && board[ind] == board[22] && board[ind]!='x'){
			ret =true;
		}
		break;

		case 21: if(board[ind] == board[15] && board[ind] == board[18] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[20] && board[ind] == board[22] && board[ind]!='x'){
			ret =true;
		}
		break;	

		case 22: if(board[ind] == board[2] && board[ind] == board[13] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[16] && board[ind] == board[19] && board[ind]!='x'){
			ret =true;
		}else if(board[ind] == board[20] && board[ind] == board[21] && board[ind]!='x'){
			ret =true;
		}
		break;


		}
		return ret;
	}

	public void printTreeNode(TreeNode node){
		//printing when node attribute = 0
		//System.out.println("Printing Tree");
		for(int i=0;i<node.getDepth();i++){
			System.out.print("|");
		}
		System.out.print("StatEst:" +node.getStatEst() + ";Board is:");
		for(int i=0;i<23;i++){
			System.out.print(node.getBoard()[i]);
		}
		System.out.println();

		if(node.getChilds()!=null){
			//System.out.println("Printing Childs");
			for(int i=0;i<node.getChilds().size();i++)
				printTreeNode(node.getChilds().get(i));
		}	
	}

	public void getOpenStatEst(TreeNode node){
		int whites =0;
		int blacks =0;
		char c;
		//System.out.print("Inside static est board is " );
		/*for(int i=0;i<23;i++){
		System.out.print("*"+node.getBoard()[i]);
	}
	System.out.println();
		 */

		for(int i = 0; i < 23; i++) {
			c = node.getBoard()[i];
			//System.out.println("c is " +c);
			if(c=='W' ||c=='w' ){
				whites++;
			}else if(c=='B' ||c=='b'){
				blacks++;
			}

		}
		//System.out.println("Whites :"+whites + "Blacks :"+blacks);
		node.setStatEst(whites - blacks);
	}



	public void printBoard(char[] inBoard){
		String gap= "___";
		System.out.println("6"+inBoard[20]+gap+gap+gap+"_"+inBoard[21]+gap+gap+gap+"_"+inBoard[22]);
		System.out.println(" "+"|\\         |         /|");
		System.out.println(" "+"| \\        |        / |");
		System.out.println(" "+"|  \\       |       /  |");
		//System.out.println(" "+"|   \\        |        /   |");
		System.out.println("5"+"|   "+inBoard[17]+gap+gap+""+inBoard[18]+gap+"___"+inBoard[19]+"   |");
		System.out.println(" "+"|   |\\     |     /|   |");
		System.out.println(" "+"|   | \\    |    / |   |");
		System.out.println(" "+"|   |  \\   |   /  |   |");
		//System.out.println(" "+"|    |   \\   |   /   |    |");
		System.out.println("4"+"|   |   "+inBoard[14]+"__"+inBoard[15]+"__"+inBoard[16]+"   |   |");
		System.out.println(" "+"|   |   |     |   |   |");
		System.out.println(" "+"|   |   |     |   |   |");
		System.out.println(" "+"|   |   |     |   |   |");
		//System.out.println(" "+"|    |    |      |   |    |");
		System.out.println("3"+inBoard[8]+gap+inBoard[9]+gap+inBoard[10]+"     "+inBoard[11]+"___"+inBoard[12]+gap+inBoard[13]);
		System.out.println(" "+"|   |   |     |   |   |");
		System.out.println(" "+"|   |   |     |   |   |");
		System.out.println(" "+"|   |   |     |   |   |");
		//System.out.println(" "+"|    |    |      |   |    |");
		System.out.println("2"+"|   |   "+inBoard[6]+gap+"__"+inBoard[7]+"   |   |");
		//System.out.println(" "+"|    |   /       \\   |    |");
		System.out.println(" "+"|   |  /       \\  |   |");
		System.out.println(" "+"|   | /         \\ |   |");
		System.out.println(" "+"|   |/           \\|   |");
		System.out.println("1"+"|   "+inBoard[3]+gap+"___"+inBoard[4]+gap+"___"+inBoard[5]+"   |");
		//System.out.println(" "+"|   /        |        \\   |");
		System.out.println(" "+"|  /       |       \\  |");
		System.out.println(" "+"| /        |        \\ |");
		System.out.println(" "+"|/         |         \\|");
		System.out.println("0"+inBoard[0]+gap+gap+gap+"_"+inBoard[1]+gap+gap+gap+"_"+inBoard[2]);
		System.out.println(" a   b  c   d  e   f   g");
	}

}