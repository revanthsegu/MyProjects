
public class DecisionTreeNode {

	private double currentEntropy;
	private int attrInd;
	private int countZero;
	private int countOne;
	private double infGain;
	private int zeroPositive;
	private int zeroNegative;
	private int onePositive;
	private int oneNegative;
	private DecisionTreeNode leftChild;
	private DecisionTreeNode rightChild;
	private DecisionTreeNode parent;
	private int[][] usedAttrInd;
	private int usedAttrNo;
	private int olaError;
	private int leftPureClass; //0 or 1 which ever is pure or else -1
	private int rightPureClass;//0 or 1 which ever is pure or else -1
	private int nodeNo;
	private int data_zeros;
	private int data_ones;

	DecisionTreeNode(){
		leftChild = null;
		rightChild = null;
		parent = null;
		currentEntropy=0;
		attrInd = -1;
		countZero = 0;
		countOne = 0;
		infGain=0;
		zeroPositive=0;
		zeroNegative=0;
		onePositive=0;
		oneNegative=0;
		usedAttrInd =null;
		usedAttrNo=0;
		leftPureClass =-1;
		rightPureClass=-1;
		olaError=100000;
		nodeNo=0;
		data_zeros=0;
		data_ones=0;
	}

	public double getCurrentEntropy() {
		return currentEntropy;
	}
	public void setCurrentEntropy(double currentEntropy) {
		this.currentEntropy = currentEntropy;
	}
	public int getAttrInd() {
		return attrInd;
	}

	public void setAttrInd(int attrInd) {
		this.attrInd = attrInd;
	}

	public int getCountZero() {
		return countZero;
	}
	public void setCountZero(int countZero) {
		this.countZero = countZero;
	}
	public int getCountOne() {
		return countOne;
	}
	public void setCountOne(int countOne) {
		this.countOne = countOne;
	}
	public double getInfGain() {
		return infGain;
	}
	public void setInfGain(double infGain) {
		this.infGain = infGain;
	}
	public int getZeroPositive() {
		return zeroPositive;
	}
	public void setZeroPositive(int zeroPositive) {
		this.zeroPositive = zeroPositive;
	}
	public int getZeroNegative() {
		return zeroNegative;
	}
	public void setZeroNegative(int zeroNegative) {
		this.zeroNegative = zeroNegative;
	}
	public int getOnePositive() {
		return onePositive;
	}
	public void setOnePositive(int onePositive) {
		this.onePositive = onePositive;
	}
	public int getOneNegative() {
		return oneNegative;
	}
	public void setOneNegative(int oneNegative) {
		this.oneNegative = oneNegative;
	}
	public DecisionTreeNode getLeftChild() {
		return leftChild;
	}
	public void setLeftChild(DecisionTreeNode leftChild) {
		this.leftChild = leftChild;
	}
	public DecisionTreeNode getRightChild() {
		return rightChild;
	}
	public void setRightChild(DecisionTreeNode rightChild) {
		this.rightChild = rightChild;
	}

	public int[][] getUsedAttrInd() {
		return usedAttrInd;
	}

	public void setUsedAttrInd(int[][] usedAttrInd) {
		this.usedAttrInd = usedAttrInd;
	}

	public int getUsedAttrNo() {
		return usedAttrNo;
	}

	public void setUsedAttrNo(int usedAttrNo) {
		this.usedAttrNo = usedAttrNo;
	}

	public int getLeftPureClass() {
		return leftPureClass;
	}

	public void setLeftPureClass(int leftPureClass) {
		this.leftPureClass = leftPureClass;
	}

	public int getRightPureClass() {
		return rightPureClass;
	}

	public void setRightPureClass(int rightPureClass) {
		this.rightPureClass = rightPureClass;
	}

	public int getOlaError() {
		return olaError;
	}

	public void setOlaError(int olaError) {
		this.olaError = olaError;
	}

	public int getNodeNo() {
		return nodeNo;
	}

	public void setNodeNo(int nodeNo) {
		this.nodeNo = nodeNo;
	}

	public DecisionTreeNode getParent() {
		return parent;
	}

	public void setParent(DecisionTreeNode parent) {
		this.parent = parent;
	}

	public int getData_zeros() {
		return data_zeros;
	}

	public void setData_zeros(int data_zeros) {
		this.data_zeros = data_zeros;
	}

	public int getData_ones() {
		return data_ones;
	}

	public void setData_ones(int data_ones) {
		this.data_ones = data_ones;
	}


}
