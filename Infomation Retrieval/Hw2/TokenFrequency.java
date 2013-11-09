public class TokenFrequency{

String token;
long frequency;
long docId;
long doclen;



TokenFrequency(String token,long frequency){
	this.token = token;
	this.frequency = frequency;
}

TokenFrequency(long docId,String token,long frequency,long doclen){
	this.docId = docId;
	this.token = token;
	this.frequency = frequency;
	this.doclen = doclen;
}

public String getToken() {
	return token;
}
public void setToken(String token) {
	this.token = token;
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

public long getDoclen() {
	return doclen;
}

public void setDoclen(long doclen) {
	this.doclen = doclen;
}




}