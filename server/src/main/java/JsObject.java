import java.util.HashMap;

public class JsObject {
	
	private HashMap<String, Object> data;

	public JsObject() {
		this.data = new HashMap<>();
	}
	
	public void write(String key, Object value) {
		data.put(key, value);
	}
	
	public void remove(String key) {
		data.remove(key);
	}

	public HashMap<String, Object> get() {
		return data;
	}
	
}
