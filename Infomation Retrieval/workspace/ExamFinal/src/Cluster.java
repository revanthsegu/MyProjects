import java.util.LinkedList;

public class Cluster{

	private double [] centroid;
	private LinkedList<Integer> docs= new LinkedList<Integer>();

	Cluster(double [] centroid,LinkedList<Integer> docs){
		this.centroid = centroid;
		this.docs = docs;
	}

	Cluster(){

	}

	public double [] getCentroid() {
		return centroid;
	}
	public void setCentroid(double [] centroid) {
		this.centroid = centroid;
	}
	public LinkedList<Integer> getDocs() {
		return docs;
	}
	public void setDocs(LinkedList<Integer> docs) {
		this.docs = docs;
	}



}