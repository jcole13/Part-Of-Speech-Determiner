
/**
 * This class is the pair used in the trigram
 * @author Jared Cole, Aaron Lee
 *
 */

public class Pair {
	private String s1;
	private String s2;
	
	public String getS1(){
		return s1;
	}
	public String getS2(){
		return s2;
	}
	
	public Pair(String s1, String s2){
		this.s1 = s1;
		this.s2 = s2;
	}
	@Override
	/**
	 * override the hashcode of object so equals() can be used
	 */
	public int hashCode(){
		int sum = 0;
		for(char c : s1.toCharArray()){
			sum += c; 
		}
		for(char c2 : s2.toCharArray()){
			sum += c2;
		}
		return sum;
	}
	
	@Override
	/**
	 * override equals of object
	 */
	public boolean equals(Object p2){
		return (this.getS1().equals(((Pair) p2).getS1()) && this.getS2().equals(((Pair) p2).getS2()));
	}
	
	public String toString(){
		return "[" + s1 + " " + s2 + "]";
	}

}
