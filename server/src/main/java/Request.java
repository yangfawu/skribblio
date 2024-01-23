import java.util.Date;
import java.util.HashMap;

import com.google.firebase.database.DataSnapshot;

public class Request {
	
	public static Request from(DataSnapshot snapshot) {
		Request out = new Request();
		out.id = snapshot.child("id").getValue(String.class);
		out.type = snapshot.child("type").getValue(String.class);
		out.from = snapshot.child("from").getValue(String.class);
		out.wantResponse = snapshot.child("wantResponse").getValue(Boolean.class);
		out.timestamp = snapshot.child("timestamp").getValue(Long.class);
		out.payload = new HashMap<>();
		if (snapshot.hasChild("payload"))
			for (DataSnapshot child: snapshot.child("payload").getChildren())
				out.payload.put(child.getKey(), child.getValue());
		return out;
	}
	
	private String id, type, from;
	private boolean wantResponse;
	private long timestamp;
	private HashMap<String, Object> payload;

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getFrom() {
		return from;
	}

	public boolean isWantResponse() {
		return wantResponse;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public HashMap<String, Object> getPayload() {
		return payload;
	}
	
	public Object get(String key) {
		return this.payload.get(key);
	}
	
	public String toString() {
		return "[%s] (%s) Request %s from %s".formatted(
			new Date(timestamp).toString(),
			type, 
			id, 
			from
		);
	}
	
}
