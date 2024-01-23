import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class Game {
	
	private Room room;
	private String picture, announcement;
	private int MAX_ROUNDS, MAX_TIME, round, time;
	private ArrayList<Player> players;
	private Player drawer;
	private ArrayList<Player> passers;
	private Word word;
	private ArrayList<Word> options;
	private ArrayList<Message> messages;
	private ExecutorService service;
	private boolean isOver;
	
	/**
	 * Constructs a game instance.
	 * @param MAX_ROUNDS the number of rounds in the game
	 * @param MAX_TIME the amount of time in seconds for each drawer
	 * @param users the users that will be playing
	 */
	public Game(Room room, int MAX_ROUNDS, int MAX_TIME, ArrayList<User> users) {
		this.room = room;
		this.picture = "";
		this.MAX_ROUNDS = MAX_ROUNDS;
		this.MAX_TIME = MAX_TIME;
		this.round = 0;
		this.time = 0;
		this.options = new ArrayList<>();
		for (int i=0; i<3; i++)
			options.add(new Word());
		
		this.messages = new ArrayList<>();
		this.service = null;
		
		// populate players
		this.players = new ArrayList<Player>();
		for (int i=0; i<users.size(); i++)
			this.players.add(new Player(users.get(i)));
		
		this.drawer = this.players.get(users.size()-1);
		this.announcement = this.drawer.getUser().getName() + " is choosing a word.";
		
		this.passers = new ArrayList<Player>();
		this.passers.add(this.drawer);
		
		this.word = null;
		
		update();
		
		start();
	}
	
	public void refreshOptions() {
		word = null;
		for (int i=0; i<3; i++)
			options.get(i).next();
		update();
	}
	
	public void selectWord(int index) {
		if (index < 0) index = 0;
		if (index > 2) index = 2;
		this.word = options.get(index);
		this.announcement = "";
		update();
	}

	public String getAnnouncement() {
		return announcement;
	}

	/**
	 * A getter for the game picture string data
	 */
	public String getPicture() {
		return picture;
	}

	/**
	 * A getter for the max number of rounds in this game
	 */
	public int getMAX_ROUNDS() {
		return MAX_ROUNDS;
	}

	/**
	 * A getter for the max duration of each drawer's turn
	 */
	public int getMAX_TIME() {
		return MAX_TIME;
	}

	/**
	 * A getter for the current round of the game
	 */
	public int getRound() {
		return round;
	}

	/**
	 * A getter for the current time left in the game
	 */
	public int getTime() {
		return time;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}

	/**
	 * A getter for all the players in the game
	 */
	public ArrayList<Player> getPlayers() {
		return players;
	}

	/**
	 * A getter for the current drawer in the game
	 */
	public Player getDrawer() {
		return drawer;
	}

	/**
	 * A getter for all the players that has guessed the word correctly
	 */
	public ArrayList<Player> getPassers() {
		return passers;
	}
	
	public void clearPassers() {
		passers.clear();
		update();
	}

	/**
	 * A getter for the word the drawer is currently drawing
	 */
	public Word getWord() {
		return word;
	}
	
	public JsObject get() {
		JsObject obj = new JsObject();
		obj.write("max-rounds", MAX_ROUNDS);
		obj.write("round", round);
		obj.write("time", time);
		obj.write("players", players);
		
		String passersString = "";
		for (Player player: passers)
			passersString+= ":%s:".formatted(player.getUser().getUid());
		obj.write("passers", passersString);
		
		obj.write("drawer", drawer);
		obj.write("picture", picture);
		obj.write("announcement", announcement);
		
		if (word != null) {
			obj.write("word", word.get());
			obj.write("secret", word.expose());
		}
		
		ArrayList<String> optionsList = new ArrayList<>();
		for (int i=0; i<options.size(); i++)
			optionsList.add(options.get(i).expose());
		obj.write("options", optionsList);
		
		obj.write("messages", messages);
		
		obj.write("isOver", isOver);
		
		return obj;
	}
	
	public void update() {
		JsObject data = get();
		Database.set("/rooms/%s/game".formatted(room.getId()), data.get(), new Runnable() {
			
			@Override
			public void run() { }
		});
	}
	
	public void die() {
		if (service != null)
			service.shutdownNow();
		room.killGame();
		room.update();
	}
	
	public boolean hasUser(User user) {
		for (Player player: players)
			if (player.getUser().equals(user))
				return true;
		return false;
	}
	
	public boolean remove(User user) {
		if (!hasUser(user)) return false;
		Player target = null;
		for (Player player: players)
			if (player.getUser().equals(user)) {
				target = player;
				break;
			}
		assert(target != null);
		players.remove(target);
		update();
		return true;
	}
	
	public void sendServerMessage(String type, String msg) {
		Message message = new Message("server", type, msg);
		message.allow(players);
		messages.add(message);
		update();
	}
	
	public void sendPlayerMessage(Player target, String type, String msg) {
		Message message = new Message("server", type, msg);
		message.allow(target);
		messages.add(message);
		update();
	}
	
	public void sendNormalMessage(Player client, String msg) {
		Message message = new Message(client.getUser().getName(), "normal", msg);
		message.allow(drawer);
		message.allow(client);
		if (passers.contains(client)) {
			message.allow(passers);
			message.setType("passers");
			messages.add(message);
		} else {
			// sender is not a passer
			if (word != null && time > 0) {
				if (word.check(msg)) {
					passers.add(client);
					sendServerMessage("server-good", client.getUser().getName() + " guessed the word!");
					
					double ratio = (double) time / MAX_TIME;
					
					int points = Math.max((int) (1000 * ratio), 50);
					client.changePoints(points);
					sendPlayerMessage(client, "server-good", "You gained %d points!".formatted(points));
					
					points/= 4;
					points = Math.max(points, 100);
					drawer.changePoints(points);
					sendPlayerMessage(drawer, "server-good", "You gained %d points!".formatted(points));
				} else {
					message.allow(players);
					messages.add(message);
					
					String diff = StringUtils.difference(msg.toLowerCase(), word.expose().toLowerCase());
					if (diff.length() < 3) {
						sendPlayerMessage(client, "server-yellow", "\"%s\" is close!".formatted(msg));
					}
				}
			} else {
				message.allow(players);
				messages.add(message);
			}
		}
		update();
	}
	
	public void setPicture(String picture) {
		this.picture = picture;
		update();
	}
	
	public void start() {
		this.isOver = false;
		this.service = Executors.newSingleThreadExecutor();
		
		final Game GAME = this;
		synchronized (GAME) {
			this.service.submit(new Runnable() {
				
				public ArrayList<Player> order;
				
				public void init() {
					// init the order
					order = copy(GAME.players);
					Player newFirst = order.remove(players.size()-1);
					order.add(0, newFirst);
				}
				
				public Player nextTurn() {
					// move current drawer to the back of the list
					Player currentDrawer = order.remove(0);
					order.add(currentDrawer);
					
					// assign a new drawer
					Player newDrawer = order.get(0);
					GAME.drawer = newDrawer;
					
					// announce player is chooising word
					GAME.announcement = newDrawer.getUser().getName() + " is choosing a word.";
					
					// clear the people that got the previous word
					GAME.passers.clear();
					
					// add the drawer to the next list of drawers
					GAME.passers.add(newDrawer);
					
					// refresh word choices and UPDATE
					GAME.refreshOptions();
					
					return newDrawer;
				}
				
				public ArrayList<Player> copy(ArrayList<Player> players) {
					ArrayList<Player> copy = new ArrayList<>();
					for (Player player: players)
						copy.add(player);
					return copy;
				}
				
				public ArrayList<Player> selectionSort(ArrayList<Player> players) {
					ArrayList<Player> copy = copy(players);
					for (int i=0; i<copy.size()-1; i++) {
			            int min = i;
			            for (int j=i+1; j<copy.size(); j++)
			                if (copy.get(j).getPoints() < copy.get(min).getPoints())
			                	min = j;
			  
			            if (min == i) continue;
			            Player temp = copy.get(min);
			            copy.set(min, copy.get(i));
			            copy.set(i, temp);
					}
					return copy;
				}
				
				public ArrayList<Player> insertionSort(ArrayList<Player> players) {
					ArrayList<Player> copy = copy(players);
					for (int i=1; i<copy.size(); ++i) {
			            Player key = copy.get(i);
			            int j = i - 1;
			 
			            while (j >= 0 && copy.get(j).getUser().getName().compareToIgnoreCase(key.getUser().getName()) < 0) {
			                copy.set(j + 1, copy.get(j--));
			            }
			            copy.set(j + 1, key);
			        }
					return copy;
				}
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					try {
						init();
						
						// keep looping while the game round has not met the max round criteria
						while (GAME.round < GAME.MAX_ROUNDS) {
							// increment round
							GAME.round++;
							GAME.update();
							
							// init a turn counter
							int turnCount = 0;
							
							// key looping while not everyone has been a drawer in this round
							while (turnCount < order.size()) {
								// trigger next turn
								final String drawerName = nextTurn().getUser().getName();
								turnCount++;
								
								// wait for the drawer to pick a word
								// or pick one randomly in 10 seconds
								final Semaphore selectingLock = new Semaphore(0);
								final Timer selectingWorker = new Timer();
								
								GAME.time = 10;
								GAME.update();
								
								selectingWorker.scheduleAtFixedRate(new TimerTask() {
									
									public void release() {
										selectingLock.release();
										selectingWorker.cancel();
									}
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										// decrement grace time
										GAME.time--;
										GAME.update();
										
										if (GAME.word != null) {
											GAME.sendServerMessage("server-good", drawerName + " has picked a word.");
											release();
										} else if (GAME.time < 1) {
											int word = Util.randomIndex(3);
											GAME.selectWord(word);
											GAME.sendServerMessage("server-bad", drawerName + " did not pick a word in time so a word was randomly chosen.");
											release();
										}
									}
								}, 1000, 1000);
								
								// on sempahore acquire
								try {
									selectingLock.acquire();
								} catch (InterruptedException e) {
									// TODO: handle exception
//									e.printStackTrace();
									selectingWorker.cancel();
									GAME.die();
								}
								
								GAME.sendServerMessage("server-good", GAME.drawer.getUser().getName() + " is now drawing.");
								
								
								// after a word has been selected
								final Semaphore drawingLock = new Semaphore(0);
								final Timer drawingWorker = new Timer();
								
								// set up game timer
								GAME.time = GAME.MAX_TIME + 0;
								GAME.update();
								
								drawingWorker.scheduleAtFixedRate(new TimerTask() {
									
									public int hints = 0;
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										// decrement time
										GAME.time--;
										GAME.update();
										
										// calculate portion of max time left
										final double progress = 1.0 - ((double) GAME.time / GAME.MAX_TIME);
										if (hints == 0 && progress > 0.3) {
											hints++;
											GAME.getWord().revealLetter();
											GAME.update();
										}
										if (hints == 1 && progress > 0.6) {
											hints++;
											GAME.getWord().revealLetter();
											GAME.update();
										}
										
										// check if everyone has gotten the word
										// if yes, release semaphore
										// check if time is 0
										// release semphamore
										assert(GAME.players.size() == order.size());
										if (GAME.passers.size() == order.size() || GAME.time < 1) {
											drawingLock.release();
											drawingWorker.cancel();
										}
									}
								}, 1000, 1000);
								
								// on sempahore acquire
								try {
									drawingLock.acquire();
								} catch (InterruptedException e) {
									// TODO: handle exception
//									e.printStackTrace();
									drawingWorker.cancel();
									GAME.die();
								}
								
								// announce what the word was through chat
								assert(GAME.getWord() != null);
								GAME.sendServerMessage("server-good", "The word was \"%s\".".formatted(GAME.getWord().expose()));
								
								// wait 2 seconds before next turn
								TimeUnit.SECONDS.sleep(2);
							}
							
							// everyone has gone once
						}
						
						// the max rounds has been achieved
						// make isOver true
						GAME.isOver = true;
						
						// client will sort playerlist themselves
						// find winner(s)
						// announce winner in the chat
						ArrayList<Player> sorted = selectionSort(order);
						int highscore = sorted.get(sorted.size()-1).getPoints();
						
						ArrayList<Player> winners = new ArrayList<>();
						for (int i=sorted.size()-1; i>=0; i--) {
							Player target = sorted.get(i);
							if (target.getPoints() == highscore)
								winners.add(target);
						}
						
						String gratz = "Congratulations to the following players for achieving the higest score in %d rounds:".formatted(GAME.MAX_ROUNDS);
						for (Player player : insertionSort(winners))
							gratz+= " " + player.getUser().getName() + ",";
						gratz = gratz.substring(0, gratz.length()-1) + ".";
						GAME.sendServerMessage("server-good", gratz);
						GAME.update();
						
						// announce game will close in 15 seconds
						
						TimeUnit.SECONDS.sleep(1);
						
						GAME.sendServerMessage("server-bad", "You will all be returning to the room in 10 seconds. Thank you for playing.");
						GAME.update();
						
						// wait 10 seconds
						TimeUnit.SECONDS.sleep(10);
						GAME.die();
					} catch (Exception e) {
						// TODO: handle exception
//						e.printStackTrace();
						GAME.die();
					}
				
				}
				
			});
		}
	}
	
}
