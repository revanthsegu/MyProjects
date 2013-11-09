import java.util.HashMap;

public class TokenFrequency{

	private String token;
	private long frequency;
	private double hamScore;
	private double spamScore;
	private double wValue;
	private HashMap<Integer,Integer> fileCounts;
	private int curFilecount;



	TokenFrequency(String token,long frequency,double wValue,HashMap<Integer,Integer> fileCounts){
		this.token = token;
		this.frequency = frequency;
		this.wValue = wValue;
		this.fileCounts = fileCounts;
	}


	TokenFrequency(String token,long frequency){
		this.token = token;
		this.frequency = frequency;
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


	public double getHamScore() {
		return hamScore;
	}


	public void setHamScore(double hamScore) {
		this.hamScore = hamScore;
	}


	public double getSpamScore() {
		return spamScore;
	}


	public void setSpamScore(double spamScore) {
		this.spamScore = spamScore;
	}


	public double getwValue() {
		return wValue;
	}


	public void setwValue(double wValue) {
		this.wValue = wValue;
	}


	public HashMap<Integer, Integer> getFileCounts() {
		return fileCounts;
	}


	public void setFileCounts(HashMap<Integer, Integer> fileCounts) {
		this.fileCounts = fileCounts;
	}


	public int getCurFilecount() {
		return curFilecount;
	}


	public void setCurFilecount(int curFilecount) {
		this.curFilecount = curFilecount;
	}



}