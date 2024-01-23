import java.util.ArrayList;

public class Util {

	public static int randomIndex(int size) {
		return (int)(Math.random()*size);
	}
	
	public static ArrayList<Integer> randomArrayList(int size) {
		ArrayList<Integer> out = new ArrayList<>();
		for (int i=0; i<size; i++)
			out.add(randomIndex(1000));
		return out;
	}
	
}
