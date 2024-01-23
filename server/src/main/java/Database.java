import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executor;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * @author Yangfa Wu
 *
 */
public class Database {

	/**
	 * Authorizes the Admin SDK so we can use FBase services
	 */
	public static void start() {
		try {
			FileInputStream serviceAccount = new FileInputStream(
					"./src/main/resources/service/pogchamp-e27a7-firebase-adminsdk-n3y1a-42ae4a0df2.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setServiceAccountId("firebase-adminsdk-n3y1a@pogchamp-e27a7.iam.gserviceaccount.com")
					.setDatabaseUrl("https://pogchamp-e27a7.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * @return a randomly generated ID
	 */
	public static String randomId() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * A modified printing method that timestamps lines and take in an infinite number of objects
	 * @param objs	Objects to be printed in the specified order
	 */
	public static void print(Object... objs) {
		String out = "";
		if (objs.length > 0)
			for (Object obj: objs)
				out+= obj.toString() + " ";
		System.out.println("[" + new Date().toString() + "] " + out);
	}
	
	/**
	 * @param path	a file path on the database
	 * @return a reference to the path on the database
	 */
	private static DatabaseReference ref(String path) {
		return FirebaseDatabase.getInstance().getReference(path);
	}
	
	/**
	 * Reads the value at the specified path on the database and uses the onComplete callback to work with the value
	 * @param path	a file path on the database
	 * @param onComplete	a callback function that works with the acquired value
	 */
	public static void get(String path, ValueEventListener onComplete) {
		ref(path).addListenerForSingleValueEvent(onComplete);
	}
	
	/**
	 * Sets the specified path on the database to the passed value and runs on the onComplete callback on completion
	 * @param <T>	any type of data that can be uploaded onto the database
	 * @param path	a file path on the database
	 * @param data	the data of Class T to be uploaded
	 * @param onComplete	a callback function that runs after a successful upload
	 */
	public static <T> void set(final String path, T data, Runnable onComplete) {
		ref(path).setValueAsync(data)
			.addListener(onComplete, new Executor() {
				
				public void execute(Runnable command) {
					// TODO Auto-generated method stub
					command.run();
				}
			});
	}
	
	/**
	 * Waits for a value at the specified path on the database and then ambushes it so the provided onComplete callback function can work with it
	 * @param path	a file path on the database
	 * @param onComplete	a callback function that runs after a successful upload
	 */
	public static void ambush(String path, final ValueEventListener onComplete) {
		final DatabaseReference ref = ref(path);
		ref.addValueEventListener(new ValueEventListener() {
			
			public void onDataChange(DataSnapshot snapshot) {
				// TODO Auto-generated method stub
				onComplete.onDataChange(snapshot);
				ref.removeEventListener(onComplete);
			}
			
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				onComplete.onCancelled(error);
			}
		});
	}
	
	/**
	 * @author Yangfa Wu
	 * A custom event listener that overrides methods I don't need so I can type less
	 */
	public static abstract class CustomCEL implements ChildEventListener {
		
		public void onCancelled(DatabaseError error) {
			// TODO Auto-generated method stub
			print(error.getMessage());
		}
		
		public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			// TODO Auto-generated method stub
			
		}

		public void onChildRemoved(DataSnapshot snapshot) {
			// TODO Auto-generated method stub
			
		}

		public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * @param path	a file path on the database
	 * @param listener	a child-event-listener that handles data added as children to the specified path on the database
	 * @return a database reference that can be used to cancel the listener
	 */
	public static DatabaseReference watch(String path, CustomCEL listener) {
		DatabaseReference ref = ref(path);
		ref.addChildEventListener(listener);
		return ref;
	}
	
	public static void remove(String path) {
		set(path, null, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
