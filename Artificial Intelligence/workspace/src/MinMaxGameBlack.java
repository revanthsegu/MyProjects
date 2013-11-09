import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MinMaxGameBlack{

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

		MinMaxGameBlack gameOpening = new MinMaxGameBlack(); 
		long startTime = System.currentTimeMillis();

		if(args.length>1){
			gameOpening.moveCoin(args);
		}else{
			System.out.println("Please enter both input and output file names and re-execute the program");
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Total time taken to execute the program in sec : " +(double)(endTime - startTime)/1000 );
	}

	public void moveCoin(String [] args){
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

			if(inWhiteCount<3){
				System.out.println("Sorry dude you lost the game .. !!");
			}

			System.out.println("Printing input board");
			printBoard(inBoard);
			char [] tempBoard = getBoardCopy(inBoard);
			for(int i=0;i<23;i++){
				c = tempBoard[i];
				if(c=='W' ||c=='w' ){
					tempBoard[i] = 'B';
				}else if(c=='B' ||c=='b'){
					tempBoard[i] = 'W';
				}
			}


			root =new TreeNode();
			root.setDepth(0);
			root.setBoard(tempBoard);
			buildMoveTree(root);

			outBoard = getBoardCopy(root.getBoard());
			//System.out.println("Board before Swapping");
			//printBoard(tempBoard);
			for(int i=0;i<23;i++){
				c = outBoard[i];
				if(c=='W' ||c=='w' ){
					outBoard[i] = 'B';
				}else if(c=='B' ||c=='b'){
					outBoard[i] = 'W';
				}
			}


			//System.out.println("Printing Tree");
			//printTreeNode(root);

			System.out.print("Input board is: ");
			System.out.print(inBoard);
			System.out.println();

			System.out.print("Board Position: ");
			for(int i=0;i<23;i++){
				System.out.print(outBoard[i]);
			}
			System.out.println();

			System.out.println("Positions evaluated by static estimation:"+nodesEvaluated);
			System.out.println("MINMAX estimate: "+root.getStatEst());
			System.out.println("Output board is");

			printBoard(outBoard);

			writeToFile(outBoard);

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
	public void buildMoveTree(TreeNode node){
		char c;
		int whiteCount=0,blackCount=0;
		ArrayList<Integer> whiteInd = new ArrayList<Integer>();
		ArrayList<Integer> blackInd = new ArrayList<Integer>();
		ArrayList<Integer> itrInd = new ArrayList<Integer>();

		// System.out.println("looking for empty spaces");
		for(int i = 0; i < 23; i++) {
			c = node.getBoard()[i];
			//System.out.println("c is "+ c + " node value "+node.getBoard()[i]);
			if(c=='x' || c=='X' || c==' '){
			}else if(c=='w' || c=='W'){
				whiteCount++;
				whiteInd.add(i);
			}else if(c=='b' || c=='B'){
				blackCount++;
				blackInd.add(i);
			}

		}

		//System.out.println("Empty spaces are " + emptyInd);

		//System.out.println("inside buildTree depth " + node.getDepth());
		if(node.getDepth() == treeDepth || whiteCount <=2 || blackCount <=2){
			//System.out.println("inside if ");
			getMidStatEst(node);
			nodesEvaluated++;
			//System.out.println("Static Estimate is " + node.getStatEst());
		}else{
			double statEst;

			//System.out.println("inside else ");
			ArrayList<TreeNode> childs = new ArrayList<TreeNode>();
			ArrayList<char[]> allBoards = new ArrayList<char[]>();
			char[] bestBoard = null;


			if(node.getDepth()%2==0){
				c='W';
				itrInd = whiteInd;
				statEst=-1000000000;
			}else{
				c='B';
				itrInd=blackInd;
				statEst=1000000000;
			}


			for(int i=0;i<itrInd.size();i++){
				allBoards = new ArrayList<char[]>();


				generateMove(c,node.getBoard(),itrInd.get(i),allBoards);

				//System.out.println("all Boards size is " + allBoards.size());

				for(int j=0;j<allBoards.size();j++){

					//building tree
					TreeNode newNode = new TreeNode();
					newNode.setDepth(node.getDepth()+1);
					newNode.setParent(node);


					newNode.setBoard(allBoards.get(j));
					//System.out.println("Add new board to child " + allBoards.get(j));
					buildMoveTree(newNode);

					if(node.getDepth()%2==0){
						if(newNode.getStatEst() > statEst){
							statEst = newNode.getStatEst(); 
							bestBoard = newNode.getBoard();
						}

					}else{
						if(newNode.getStatEst() < statEst){
							statEst = newNode.getStatEst(); 
							bestBoard = newNode.getBoard();
						}
					}

					childs.add(newNode);

				}

			}


			if(childs.size() == 0 ){
				//System.out.println("No more boards to generate");
				statEst =  -10000;
				node.setStatEval(true);
			}

			node.setChilds(childs);
			node.setStatEst(statEst);
			if(node.getDepth() ==0){
				//node.setIndex(bestInd);
				node.setBoard(bestBoard);
			}
		}



	}

	public ArrayList<Integer> getNeighbour(char[] board,int ind){
		ArrayList<Integer> ret = new ArrayList<Integer>();


		switch(ind){

		case 0: ret.add(1);
		ret.add(3);
		ret.add(8);
		break;

		case 1: ret.add(0);
		ret.add(2);
		ret.add(4);
		break;

		case 2: ret.add(1);
		ret.add(5);
		ret.add(13);
		break;

		case 3: ret.add(0);
		ret.add(4);
		ret.add(6);
		ret.add(9);
		break;

		case 4: ret.add(1);
		ret.add(3);
		ret.add(5);
		break;

		case 5: ret.add(2);
		ret.add(4);
		ret.add(7);
		ret.add(12);
		break;

		case 6: ret.add(3);
		ret.add(7);
		ret.add(10);
		break;	

		case 7: ret.add(5);
		ret.add(6);
		ret.add(11);
		break;

		case 8: ret.add(0);
		ret.add(9);
		ret.add(20);
		break;

		case 9: ret.add(3);
		ret.add(8);
		ret.add(10);
		ret.add(17);
		break;

		case 10:ret.add(6);
		ret.add(9);
		ret.add(14);	
		break;

		case 11:ret.add(7);
		ret.add(12);
		ret.add(16);
		break;

		case 12:ret.add(5);
		ret.add(11);
		ret.add(13);
		ret.add(19);
		break;

		case 13:ret.add(2);
		ret.add(12);
		ret.add(22);
		break;

		case 14:ret.add(10);
		ret.add(15);
		ret.add(17);
		break;		

		case 15:ret.add(14);
		ret.add(16);
		ret.add(18);
		break;

		case 16:ret.add(11);
		ret.add(15);
		ret.add(19);
		break;

		case 17:ret.add(9);
		ret.add(14);
		ret.add(18);
		ret.add(20);
		break;

		case 18:ret.add(15);
		ret.add(17);
		ret.add(19);
		ret.add(21);
		break;	

		case 19:ret.add(12);
		ret.add(16);
		ret.add(18);
		ret.add(22);
		break;


		case 20:ret.add(8);
		ret.add(17);
		ret.add(21);
		break;

		case 21:ret.add(18);
		ret.add(20);
		ret.add(22);
		break;	

		case 22:ret.add(13);
		ret.add(19);
		ret.add(21);
		break;

		}



		return ret;
	}


	public void generateMove(char c,char[] board,int ind,ArrayList<char[]> allBoard){
		char[] newBoard;
		char[] tempBoard;
		ArrayList<Integer> NighInd;
		int tempWhite = 0;
		int tempBlack = 0;

		char temp;
		ArrayList<Integer> emptyInd = new ArrayList<Integer>();
		int whiteCount=0,blackCount=0;
		//System.out.println("looking for empty spaces");
		for(int i = 0; i < 23; i++) {
			temp = board[i];
			//System.out.println("c is "+ c + " node value "+node.getBoard()[i]);
			if(temp=='x' || temp=='X' || temp==' '){
				emptyInd.add(i);
			}else if(temp=='w' || temp=='W'){
				whiteCount++;
			}else if(temp=='b' || temp=='B'){
				blackCount++;
			}

		}

		//System.out.println("Empty spaces are " + emptyInd);

		if(whiteCount < 3){
			return;
		}else if((whiteCount == 3 && c =='W') || (blackCount == 3 && c =='B') ){
			//System.out.println("Entered into endgame");
			NighInd = emptyInd;
			//System.out.println("Empty Ind are : " + emptyInd);
		}else{

			NighInd = getNeighbour(board,ind);

		}
		for(int j=0;j<NighInd.size();j++){

			if(board[NighInd.get(j)] == 'x'){	
				newBoard = getBoardCopy(board);

				newBoard[NighInd.get(j)] = c;
				newBoard[ind] = 'x';
				//System.out.println("Moving " + c + " from " + ind + " to ind " +  NighInd.get(j)  );

				if(isCloseMill(NighInd.get(j),newBoard)){
					for(int i=0;i<23;i++){
						if(newBoard[i]!=c && newBoard[i]!='x'){
							//System.out.println("Mill Done");
							tempBoard = getBoardCopy(newBoard);
							if(!isCloseMill(i,tempBoard)){
								tempBoard[i] = 'x';
								//System.out.println("Removing ind " + i);
								allBoard.add(tempBoard);

							}else{

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

	public void getMidStatEst(TreeNode node){
		int whites =0;
		int blacks =0;
		char c;
		int statEst;
		int blackMovesNo=0;
		ArrayList<char[]> allBoards;

		ArrayList<Integer>blackInd = new ArrayList<Integer>();
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
				blackInd.add(i);
			}

		}

		allBoards = new ArrayList<char[]>();
		for(int i=0;i<blackInd.size();i++){
			generateMove('B',node.getBoard(),blackInd.get(i),allBoards);
		}	

		blackMovesNo = allBoards.size();

		if(blacks<=2){
			statEst = 10000;
		}else if(whites<=2){
			statEst = -10000; 
		}else if(blackMovesNo ==0){
			statEst = 10000;
		}else{
			statEst = 1000*(whites - blacks);
			statEst = statEst - blackMovesNo;
		}

		//System.out.println("Whites :"+whites + "Blacks :"+blacks);
		node.setStatEst(statEst);
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