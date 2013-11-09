public class TermFrequency{

long frequency;
long docId;


TermFrequency(long docId,long frequency){
	this.docId = docId;
	this.frequency = frequency;
}


public long getFrequency() {
	return frequency;
}
public void setFrequency(long frequency) {
	this.frequency = frequency;
}
public long getDocId() {
	return docId;
}
public void setDocId(long docId) {
	this.docId = docId;
}

}