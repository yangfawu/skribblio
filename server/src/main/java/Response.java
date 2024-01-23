import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Response {
	
	public static Response from(Request request) {
		Response out = new Response();
		out.id = UUID.randomUUID().toString();
		out.type = request.getType();
		out.replyTo = request.getId();
		out.to = request.getFrom();
		out.timestamp = new Date().getTime();
		out.isGood = true;
		out.confirmResponse = true;
		out.payload = new HashMap<>();
		return out;
	}
	
	private String id, type, replyTo, to;
	private long timestamp;
	private boolean confirmResponse, isGood;
	private HashMap<String, Object> payload;
	
	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public String getReplyTo() {
		return replyTo;
	}
	
	public String getTo() {
		return to;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public boolean isConfirmResponse() {
		return confirmResponse;
	}
	
	public boolean getIsGood() {
		return isGood;
	}
	
	public HashMap<String, Object> getPayload() {
		return payload;
	}
	
	public void turnBad(String reason) {
		this.isGood = false;
		write("reason", reason);
	}
	
	public void write(String key, Object val) {
		this.payload.put(key, val);
	}
	
	public void send() {
		Database.set("/ws/toClient/" + this.id, this, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// sent response
			}
			
		});
	}
	
	public String toString() {
		return "[%s] <%s> (%s) Response %s to %s".formatted(
			new Date(timestamp).toString(),
			isGood ? "good" : "bad",
			type,
			id,
			replyTo
		);
	}

}
