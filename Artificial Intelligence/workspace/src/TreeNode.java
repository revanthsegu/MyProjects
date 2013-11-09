import java.util.ArrayList;


public class TreeNode {

	private int index;
	private int depth;
	private double statEst=0;
	private boolean statEval = false;
	private double statGreat = -10000000;
	private double statLess = 10000000;
	private ArrayList<TreeNode> childs = new ArrayList<TreeNode>();
	private char [] board = new char [23];
	private TreeNode parent;

	public boolean isStatEval() {
		return statEval;
	}
	public void setStatEval(boolean statEval) {
		this.statEval = statEval;
	}
	public double getStatGreat() {
		return statGreat;
	}
	public void setStatGreat(double statGreat) {
		this.statGreat = statGreat;
	}
	public double getStatLess() {
		return statLess;
	}
	public void setStatLess(double statLess) {
		this.statLess = statLess;
	}
	public ArrayList<TreeNode> getChilds() {
		return childs;
	}
	public void setChilds(ArrayList<TreeNode> childs) {
		this.childs = childs;
	}
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	public char[] getBoard() {
		return board;
	}
	public void setBoard(char[] board) {
		this.board = board;
	}
	public double getStatEst() {
		return statEst;
	}
	public void setStatEst(double statEst) {
		this.statEst = statEst;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}

}
