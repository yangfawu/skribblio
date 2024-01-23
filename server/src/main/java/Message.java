import java.util.ArrayList;

public class Message {
	
	private String data, from, type;
	private ArrayList<String> canSee;
	
	public Message(String from, String type, String data) {
		this.from = from;
		this.type = type;
		this.data = data;
		this.canSee = new ArrayList<>();
	}

	public String getData() {
		return data;
	}
	
	public String getFrom() {
		return from;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void allow(Player player) {
		final String playeruid = player.getUser().getUid();
		for (String uid: canSee)
			if (uid.equals(playeruid))
				return;
		canSee.add(playeruid);
	}
	
	public void allow(ArrayList<Player> players) {
		for (Player player : players)
			allow(player);
	}

	public String getCanSee() {
		String out = "";
		for (String uid: canSee)
			out+= ":" + uid + ":";
		return out;
	}

}
