import java.util.ArrayList;
import java.util.UUID;

public class Room {
	
	private static int MAX_USER_COUNT = 8;
	private static ArrayList<Room> rooms = new ArrayList<>();
	
	public static Room get(String id) {
		for (int i=0; i<rooms.size(); i++) {
			Room test = rooms.get(i);
			if (test.id.equals(id))
				return test;
		}
		return null;
	}
	
	public static void remove(Room room) {
		rooms.remove(room);
		// clear game
	}
	
	public static void remove(User user) {
		for (int i=0; i<rooms.size(); i++) {
			Room test = rooms.get(i);
			if (test.hasUser(user))
				test.removeUser(user);
		}
	}
	
	private static ArrayList<Room> copyRooms() {
		return new ArrayList<>(rooms);
	}
	
	public static Room findRoom(User user) {
		if (rooms.size() > 0) {
			ArrayList<Room> options = copyRooms();
			for (int i=options.size()-1; i>=0; i--)
				if (!options.get(i).canAddUser(user))
					options.remove(i);
			if (options.size() > 0) {
				for (int i=0; i<options.size()-1; i++) {
		            int min = i;
		            for (int j=i+1; j<options.size(); j++)
		                if (options.get(j).users.size() < options.get(min).users.size())
		                	min = j;
		  
		            if (min == i) continue;
		            Room temp = options.get(min);
		            options.set(min, options.get(i));
		            options.set(i, temp);
				}
				return options.get(0);
			}
		}
		return null;
	}
	
	private String id;
	private User owner;
	private ArrayList<User> users;
	private Game game;
	private int maxRounds, maxTime;
	
	/**
	 * Constructs a Room instance
	 * @param owner the user that will own this room
	 */
	public Room(User owner) {
		String sampleId = null;
		do {
			sampleId = String.join("", UUID.randomUUID().toString().split("-")).substring(0, 10);
		} while (get(sampleId) != null);
		this.id = sampleId;
		this.owner = null;
		this.users = new ArrayList<User>();
		this.game = null;
		this.maxRounds = 2;
		this.maxTime = 30;
		
		// load
		addUser(owner);
		
		rooms.add(this);
		
		update();
	}

	public void setMaxRounds(int maxRounds) {
		this.maxRounds = maxRounds;
	}

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}

	public int getMaxRounds() {
		return maxRounds;
	}

	public int getMaxTime() {
		return maxTime;
	}

	/**
	 * A getter for the room id
	 */
	public String getId() {
		return id;
	}

	/**
	 * A getter for the list of users currently in the room
	 */
	public ArrayList<User> getUsers() {
		return users;
	}

	/**
	 * A getter for the game connected to this room
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * A getter for the owner of the room
	 */
	public User getOwner() {
		return owner;
	}
	
	/**
	 * Determines if the room has a specified user already
	 * @param user the user to check for in the room
	 * @return whether or not the user is already in the room
	 */
	public boolean hasUser(User user) {
		return this.users.contains(user);
	}
	
	public boolean canAddUser(User user) {
		return !(hasUser(user) || game != null || users.size() == MAX_USER_COUNT);
	}
	
	/**
	 * Attempts to add an user to the room (user has to be not in the room and game needs to be not started)
	 * @param user the user to add to the room
	 * @return whether or not the attempt was successful
	 */
	public boolean addUser(User user) {
		if (!canAddUser(user)) return false;
		this.users.add(user);
		if (users.size() < 2 && owner == null)
			makeOwner(user);
		update();
		return true;
	}
	
	/**
	 * Makes the specified user the owner of the room (user has to be in the room)
	 * @param user the user to become an owner
	 * @return whether or not the operation was successful
	 */
	public boolean makeOwner(User user) {
		if (!hasUser(user)) return false;
		this.owner = user;
		update();
		return true;
	}
	
	/**
	 * Attempts to remove a specified user from the room (and the game as well)
	 * @param user the user to remove
	 * @return whether or not the operation was successful
	 */
	public boolean removeUser(User user) {
		if (!hasUser(user)) return false;
		this.users.remove(user);
		if (owner.equals(user)) 
			this.owner = users.size() > 0 ? users.get(0) : null;
		if (this.users.size() < 1)
			die();
		if (game != null) {
			game.die();
			update();
		}
//		else {
//			if (game != null)
//				game.remove(user);
//			if (this.users.size() < 2) {
//				game.die();
//				game = null;
//			}
//			if (game != null)
//				game.die();
//			System.out.println("hiiiiiiii");
//			update();
//		}
		return true;
	}
	
	/**
	 * Constructs a new game using this room and its user
	 */
	public void startGame() {
		this.game = new Game(this, maxRounds, maxTime, users);
	}
	
	public void killGame() {
		game = null;
	}
	
	public JsObject get() {
		JsObject obj = new JsObject();
		obj.write("id", id);
		obj.write("owner", owner);
		obj.write("users", users);
		
		String viewPerms = "";
		for (User user: users)
			viewPerms+= ":%s:".formatted(user.getUid());
		obj.write("viewPerms", viewPerms);
		
		obj.write("max-rounds", maxRounds);
		obj.write("max-time", maxTime);
		
		return obj;
	}
	
	public void update() {
		JsObject data = get();
		Database.set("/rooms/" + id, data.get(), new Runnable() {
			
			@Override
			public void run() { }
		});
	}
	
	public void die() {
		remove(this);
		if (game != null)
			game.die();
		Database.remove("/rooms/" + id);
	}
	
}
