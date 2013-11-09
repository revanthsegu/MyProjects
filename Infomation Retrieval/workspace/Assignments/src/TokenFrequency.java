public class TokenFrequency{

	private String token;
	private long frequency;
	private long docId;
	private long doclen;

	private double W1Score;
	private double W2Score;



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

	TokenFrequency(long docId,double w1Score,double w2Score){
		this.docId=docId;
		this.W1Score=w1Score;
		this.W2Score = w2Score;
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


	public double getW1Score() {
		return W1Score;
	}


	public void setW1Score(double w1Score) {
		W1Score = w1Score;
	}


	public double getW2Score() {
		return W2Score;
	}


	public void setW2Score(double w2Score) {
		W2Score = w2Score;
	}



}