import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer {
	static int CONNECTION_COUNT = 0;

	private int port;
	private ServerSocket server;
	public ArrayList<User> users;
	public HashMap<Room, ArrayList<User>> rooms = new HashMap();
	
	class Room {
		public String name;
		public String password;
		public Room(String name, String pass) {
			this.name = name;
			this.password = pass;
		}
	}
	
	
	public ChatServer(int port) {
		this.port = port;
	}

	
	//Start listening for new users
	//New thread for each user
	public void start() throws IOException {
		ServerSocket server = new ServerSocket(port);
		System.out.println("Started listening on port: " + port);
		
		while(true) {
			Socket socket = server.accept();
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();
		
			User connection = new User(input, output, "rand" + CONNECTION_COUNT, this);
			users.add(connection);
			CONNECTION_COUNT++;
			
			System.out.println("Created a new user");
			new Thread(connection, "test").start();
		}
	}
	
}
