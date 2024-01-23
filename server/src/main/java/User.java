import java.util.ArrayList;

public class User {
	
	private static ArrayList<User> users = new ArrayList<>();
	
	public static User get(String uid) {
		for (int i=0; i<users.size(); i++) {
			User test = users.get(i);
			if (test.uid.equals(uid))
				return test;
		}
		return null;
	}
	
	public static void remove(User user) {
		users.remove(user);
		// remove user from rooms
		// remove user from games
	}
	
	private String uid, name;
	private Avatar avatar;
	
	/**
	 * Constructs an User instance.
	 * @param uid the user id of the user
	 * @param avatar the avatar of the user
	 */
	public User(String uid, String name, Avatar avatar) {
		this.uid = uid;
		this.name = name;
		this.avatar = avatar;
		
		if (get(uid) == null)
			users.add(this);
	}

	/**
	 * A getter for the User user id.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * A getter for the User avatar.
	 */
	public Avatar getAvatar() {
		return avatar;
	}
	
	/**
	 * A getter for the User name.
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks if this User instance is equal to another User instance
	 * @param other the other User instance to check against
	 * @return whether or not both instances are the same
	 */
	public boolean equals(User other) {
		return other != null ? this.uid.equals(other.uid) && this.avatar.equals(other.avatar) : false;
	}

}
