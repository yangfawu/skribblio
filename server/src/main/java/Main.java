import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Timer();
		Database.start();
		Word.init();
		start();
		try {
			HashMap<String, Object> dirtyPaths = new HashMap<String, Object>();
			dirtyPaths.put("ws", null);
			dirtyPaths.put("games", null);
			dirtyPaths.put("rooms", null);
			FirebaseDatabase.getInstance().getReference()
				.updateChildrenAsync(dirtyPaths).get();
			Database.watch("/ws/toServer", new Database.CustomCEL() {
				
				public boolean isValidRequest(DataSnapshot snapshot) {
					String[] reqs = { "id", "type", "timestamp", "from", "wantResponse" };
					for (String attr: reqs)
						if (!snapshot.hasChild(attr))
							return false;
					return true;
				}
				
				public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
					// TODO Auto-generated method stub
					snapshot.getRef().removeValueAsync();
					if (!isValidRequest(snapshot)) return;
					Request req = Request.from(snapshot);
					System.out.println(req);
					Handler.handleRequest(req);
				}
				
			});
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void start() {
		Handler.add(new Handler("ping", false) {
			
			@Override
			public void attend(User client, Request request, Response response) { }
			
		});
		Handler.add(new Handler("bye", true) {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				Room.remove(client);
			}
		});
		Handler.add(new Handler("got-response", false, "response-id") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String responseId = String.valueOf(request.get("response-id"));
				Database.remove("/ws/toClient/" + responseId);
			}
			
		});
		Handler.add(new Handler("greet", false, "uid", "name", "color", "eyes", "mouth") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String uid = String.valueOf(request.get("uid"));
				String name = String.valueOf(request.get("name"));
				Integer color = Integer.valueOf(String.valueOf(request.get("color")));
				Integer eyes = Integer.valueOf(String.valueOf(request.get("eyes")));
				Integer mouth = Integer.valueOf(String.valueOf(request.get("mouth")));
				User test = User.get(uid);
				if (test == null) {
					test = new User(uid, name, new Avatar(color, eyes, mouth));
				} else {
					test.setName(name);
					test.getAvatar().setColor(color);
					test.getAvatar().setEyes(eyes);
					test.getAvatar().setMouth(mouth);
				}
			}
			
		});
		Handler.add(new Handler("create-room", true) {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				response.write("room-id", new Room(client).getId());
			}
			
		});
		Handler.add(new Handler("join-room", true, "room-id") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String roomId = String.valueOf(request.get("room-id"));
				Room room = Room.get(roomId);
				if (room == null) {
					response.turnBad("Room does not exist.");
					return;
				}
				if (!room.addUser(client)) {
					response.turnBad("You don't have permission to join this room");
				}
			}
			
		});
		Handler.add(new Handler("update-room", true, "room-id", "max-rounds", "max-time") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String roomId = String.valueOf(request.get("room-id"));
				Room room = Room.get(roomId);
				if (room == null) {
					response.turnBad("Room does not exist.");
					return;
				}
				
				if (!client.equals(room.getOwner())) {
					response.turnBad("Only the owner of the room can make changes.");
					return;
				}
				
				Integer maxRounds = Integer.valueOf(String.valueOf(request.get("max-rounds")));
				maxRounds = Math.max(maxRounds, 2);
				maxRounds = Math.min(maxRounds, 10);
					
				Integer maxTime = Integer.valueOf(String.valueOf(request.get("max-time")));
				maxTime = Math.max(maxTime, 30);
				maxTime = Math.min(maxTime, 180);
				
				room.setMaxRounds(maxRounds);
				room.setMaxTime(maxTime);
				
				room.update();
			}
		});
		Handler.add(new Handler("start-game", true, "room-id") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String roomId = String.valueOf(request.get("room-id"));
				Room room = Room.get(roomId);
				
				if (room == null) {
					response.turnBad("Room does not exist.");
					return;
				}
				
				if (!client.equals(room.getOwner())) {
					response.turnBad("Only the owner of the room can start a game.");
					return;
				}
				
				try {
					room.startGame();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			
		});
		Handler.add(new Handler("send-message", true, "room-id", "message") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String roomId = String.valueOf(request.get("room-id"));
				Room room = Room.get(roomId);
				if (room == null) {
					response.turnBad("Room does not exist.");
					return;
				}
				
				Game game = room.getGame();
				if (game == null) {
					response.turnBad("There is no ongoing game in this room to show your message.");
					return;
				}
				
				Player player = null;
				ArrayList<Player> players = room.getGame().getPlayers();
				for (Player other: players)
					if (other.getUser().equals(client)) {
						player = other;
						break;
					}
				
				if (player == null) {
					response.turnBad("You are currently not found in this game.");
					return;
				}
				
				String message = String.valueOf(request.get("message")).trim();
				if (message.length() > 0)
					game.sendNormalMessage(player, message);
			}
		});
		Handler.add(new Handler("update-drawing", true, "room-id", "data") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String roomId = String.valueOf(request.get("room-id"));
				Room room = Room.get(roomId);
				if (room == null) {
					response.turnBad("Room does not exist.");
					return;
				}
				
				Game game = room.getGame();
				if (game == null) {
					response.turnBad("There is no ongoing game in this room to show your message.");
					return;
				}
				
				Player drawer = game.getDrawer();
				if (!drawer.getUser().equals(client)) {
					response.turnBad("You are not the drawer.");
					return;
				}
				
				String data = String.valueOf(request.get("data")).trim();
				game.setPicture(data);
			}
		});
		Handler.add(new Handler("select-word", true, "room-id", "choice") {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				String roomId = String.valueOf(request.get("room-id"));
				Room room = Room.get(roomId);
				if (room == null) {
					response.turnBad("Room does not exist.");
					return;
				}
				
				Game game = room.getGame();
				if (game == null) {
					response.turnBad("There is no ongoing game in this room to show your message.");
					return;
				}
				
				Player drawer = game.getDrawer();
				if (!drawer.getUser().equals(client)) {
					response.turnBad("You are not the drawer.");
					return;
				}
				
				Integer choice = Integer.valueOf(String.valueOf(request.get("choice")));
				game.selectWord(choice);
			}
		});
		Handler.add(new Handler("join-random-room", true) {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				Room test = Room.findRoom(client);
				if (test == null)
					test = new Room(client);
				response.write("room-id", test.getId());
			}
		});
		Handler.add(new Handler("cheese", false) {
			
			@Override
			public void attend(User client, Request request, Response response) {
				// TODO Auto-generated method stub
				response.write("stdout", new Cheese().get());
			}
		});
	}

}
