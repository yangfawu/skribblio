import java.util.ArrayList;

public class Cheese {
	
	private String data;

	public Cheese() {
		data = "Wow! You love cheese? Me, too!\n\n";
		
		ArrayList<Integer> sample = Util.randomArrayList(5);
		println("sample: " + sample);
		
		ArrayList<Integer> seleSorted = selectionSort(sample);
		println("selection-sorted: " + seleSorted);
		
		ArrayList<Integer> sinsSorted = insertionSort(sample);
		println("insertion-sorted: " + sinsSorted);
		
		int rows = Util.randomIndex(3) + 2;
		int cols = Util.randomIndex(3) + 2;
		println("\n2D Array Dimensions: rows=%d cols=%d".formatted(rows, cols));
		
		Word[][] tdArr = new Word[rows][cols];
		for (int row=0; row<tdArr.length; row++)
			for (int col=0; col<tdArr[row].length; col++)
				tdArr[row][col] = new Word();
		
		println("row-major order:");
		for (Word[] row : tdArr) {
			for (Word word : row)
				print(word.expose() + " ");
			println("");
		}
		
		println("\ncol-major order:");
		for (int i=0; i<cols; i++) {
			for (int j=0; j<rows; j++)
				print(tdArr[j][i].expose() + " ");
			println("");
		}
	}
	
	public ArrayList<Integer> selectionSort(ArrayList<Integer> input) {
		ArrayList<Integer> copy = new ArrayList<>(input);
		int count = 0;
		for (int i=0; i<copy.size()-1; i++) {
            int min = i;
            for (int j=i+1; j<copy.size(); j++)
                if (copy.get(j) < copy.get(min))
                	min = j;
  
            if (min == i) continue;
            Integer temp = copy.get(min);
            copy.set(min, copy.get(i));
            copy.set(i, temp);
            count++;
		}
		println("selection-sort exec counter: " + count);
		return copy;
	}
	
	public ArrayList<Integer> insertionSort(ArrayList<Integer> input) {
		ArrayList<Integer> copy = new ArrayList<>(input);
		int count = 0;
		for (int i=1; i<copy.size(); ++i) {
            Integer key = copy.get(i);
            int j = i - 1;
 
            while (j >= 0 && copy.get(j) > key) {
                copy.set(j + 1, copy.get(j--));
                count++;
            }
            copy.set(j + 1, key);
        }
		println("insertion-sort exec counter: " + count);
		return copy;
	}
	
	public void print(Object data) {
		this.data+=data.toString();
	}
	
	public void println(Object data) {
		print(data);
		print('\n');
	}
	
	public String get() {
		return data;
	}

}
