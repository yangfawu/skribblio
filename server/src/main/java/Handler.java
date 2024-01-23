import java.util.ArrayList;
import java.util.Set;

public abstract class Handler {
	
	private static ArrayList<Handler> handlers = new ArrayList<>();
	
	private static Handler get(String type) {
		for (int i=0; i<handlers.size(); i++) {
			Handler handler = handlers.get(i);
			if (handler.type.equals(type))
				return handler;
		}
		return null;
	}
	
	public static boolean hasHandler(String type) {
		return get(type) != null;
	}
	
	public static void add(Handler handler) {
		if (hasHandler(handler.type)) return;
		handlers.add(handler);
	}
	
	public static void handleRequest(Request request) {
		if (!hasHandler(request.getType())) {
			if (request.isWantResponse()) {
				Response response = Response.from(request);
				response.turnBad("Cannot find a handler for this type of request.");
				response.send();
			}
			return;
		}
		Handler handler = get(request.getType());
		handler.handle(request);
	}
	
	private String type;
	private String[] payloadNeeds;
	private boolean needGreet;
	
	public Handler(String type, boolean needGreet, String... payloadNeeds) {
		this.type = type;
		this.payloadNeeds = payloadNeeds;
		this.needGreet = needGreet;
	}

	public String getType() {
		return type;
	}
	
	public void handle(Request request) {
		if (!request.getType().equals(type)) return;
		
		Response response = Response.from(request);
		Set<String> requestPayloadKeys = request.getPayload().keySet();
		for (String key: payloadNeeds)
			if (!requestPayloadKeys.contains(key)) {
				if (request.isWantResponse()) {
					response.turnBad("Payload is missing a value for the %s key.".formatted(key));
					response.send();
				}
				System.out.println(response);
				return;
			}
		
		User client = User.get(request.getFrom());
		if (needGreet && client == null) {
			if (request.isWantResponse()) {
				response.turnBad("Client does not exist on server. Greeting is required before making this type of request.");
				response.send();
			}
			System.out.println(response);
			return;
		}
		
		attend(client, request, response);
		
		if (request.isWantResponse())
			response.send();
		
		System.out.println(response);
	}
	
	public abstract void attend(User client, Request request, Response response);

}
