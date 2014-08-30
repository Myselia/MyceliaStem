package cms.databank.structures;


public class EchoedValueStorage {
	private double[][] mem;
	private int[] count;
	private static final int level = 4;
	private static final int size = 30;
	private static final int echo = 8;
	
	public EchoedValueStorage(){
		mem = new double[level][size];
		count = new int[level];
	}
	
	public void add(double value){
		shiftForward(0);
		mem[0][0] = value;

		for(int i = 0; i < (level - 1); i++){
			if(count[i] == echo){
				pushUp(i);
			}
		}
		
	}
	
	/**
	 * TODO: return the storage as a reassembled linear array
	 * @return
	 */
	public double[][] getmem(){
		return mem;
	}
	
	/**
	 * TODO: rewrite the storage as a circular array
	 * @param num
	 */
	private void shiftForward(int num){
		for(int i = (size - 1); i > 0; i--){
			mem[num][i] = mem[num][i - 1];
		}
		//incrementing the counter
		count[num]++;
	}
	
	private void pushUp(int num){
		shiftForward(num+1);
		
		double average = 0;
		for(int i = 0; i < echo; i++){
			average += (mem[num][i] / echo);
		}
		mem[num+1][0] = average;
		
		//resetting the counter
		count[num] = 0;
	}
	
	public static int getSize(){
		return size;
	}

}