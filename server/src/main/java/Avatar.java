
public class Avatar {
	
	private int color, eyes, mouth;

	/**
	 * Constructs a Avatar instance.
	 * @param color
	 * @param eyes
	 * @param mouth
	 */
	public Avatar(int color, int eyes, int mouth) {
		this.color = color;
		this.eyes = eyes;
		this.mouth = mouth;
	}

	/**
	 * A getter for the Avatar instance's color index
	 */
	public int getColor() {
		return color;
	}

	/**
	 * A setter for the Avatar instance's color index
	 */
	public void setColor(int color) {
		this.color = color;
	}

	
	/**
	 * A getter for the Avatar instance's eyes index
	 */
	public int getEyes() {
		return eyes;
	}

	/**
	 * A setter for the Avatar instance's eyes index
	 */
	public void setEyes(int eyes) {
		this.eyes = eyes;
	}

	/**
	 * A getter for the Avatar instance's mouth index
	 */
	public int getMouth() {
		return mouth;
	}

	/**
	 * A setter for the Avatar instance's mouth index
	 */
	public void setMouth(int mouth) {
		this.mouth = mouth;
	}
	
	/**
	 * Checks if this Avatar instance is equal to another Avatar instance
	 * @param other the other Avatar instance to check against
	 * @return whether or not both instances are the same
	 */
	public boolean equals(Avatar other) {
		return other != null ?
				this.color == other.color &&
				this.eyes == other.eyes &&
				this.mouth == other.mouth :
				false;
	}

}
