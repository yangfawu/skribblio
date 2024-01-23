
public class Player {
	
	private User user;
	private int points;
	
	/**
	 * A constructor for a Player instance.
	 * @param user the user to create the Player wrapper for
	 */
	public Player(User user) {
		this.user = user;
		this.points = 0;
	}

	/**
	 * A getter for the User instance that this Player instance represents
	 */
	public User getUser() {
		return user;
	}

	/**
	 * A getter for the number of points that the User instance currently has in a game
	 */
	public int getPoints() {
		return points;
	}
	
	/**
	 * Applies the specified change to the number of points the Player instance has.
	 * @param change the decrement / increment to the Player instance's current points
	 * @return the Player instance's points after the change
	 */
	public int changePoints(int change) {
		this.points+= change;
		return getPoints();
	}
	
	/**
	 * Resets the Player instance's points back to 0.
	 */
	public void resetPoints() {
		this.points = 0;
	}

}
