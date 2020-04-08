import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer {
	static int CONNECTION_COUNT = 0;

	private int port;
	private ServerSocket server;
	private ArrayList<User> users = new ArrayList<User>();
	private HashMap<String, ArrayList<User>> rooms = new HashMap();
	private HashMap<String, String> roomPass = new HashMap<String, String>();
	
	
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
	
	public synchronized User getUser(String nick) {
		for (User u : users) {
			if(u.getNick().equals(nick))
				return u;
		}
		return null;
	}
	
	//Returns false if nickname is taken by someone else
	public synchronized boolean validNick(String newnick, User user) {
		for (User u : users) {
			if(u.getNick().equals(newnick) && u != user)
				return false;
		}
		return true;
	}
	
	//0 = success
	//1 = already in room
	//2 = wrong pass
	public synchronized int joinRoom(User user, String name, String password) {
		//Have the user leave their existing room
		if(user.inRoom()) {
			leaveRoom(user);
		}
		
		//If the room exists
		if(rooms.containsKey(name)) {
			ArrayList<User> users = rooms.get(name);
			
			if(users.contains(user)) 
				return 1;
			
			
			if(!password.equals(roomPass.get(name)))
				return 2;
			
			users.add(user);
		}
		//If the room doesn't exist create it
		else {
			
			ArrayList<User> userList = new ArrayList();
			userList.add(user);
			
			//Create the room
			rooms.put(name, userList);
			roomPass.put(name, password);
		}
		
		user.setRoom(name);
		return 0;
	}
	
	public synchronized void leaveRoom(User user) {
		if(user.inRoom()) {
			String leavingRoom = user.getRoom();
			ArrayList<User> usrList = rooms.get(leavingRoom);
			usrList.remove(user);
			
			if(usrList.size() == 0) {
				rooms.remove(leavingRoom);
				roomPass.remove(leavingRoom);
			}
			
			user.setRoom("");
		}
	}
	
	public synchronized byte[] getRooms() {
		int totalLen = 0;
		for (String r : rooms.keySet()) {
			totalLen += r.length();
		}
		
		ByteBuffer b = ByteBuffer.allocate(totalLen + rooms.keySet().size());
		for (String r : rooms.keySet()) {
			b.put((byte) r.length());
			b.put(r.getBytes());
		}
		
		return b.array();
	}
	
	public synchronized byte[] getRoomUserList(String room) {
		int totalLen = 0;
		for (User u : rooms.get(room)) {
			totalLen += u.getNick().length();
		}
		
		ByteBuffer b = ByteBuffer.allocate(totalLen + users.size());
		for (User u : rooms.get(room)) {
			b.put((byte) u.getNick().length());
			b.put(u.getNick().getBytes());
		}
		
		return b.array();
	}
	
	public synchronized byte[] getUserList() {
		int totalLen = 0;
		for (User u : users) {
			totalLen += u.getNick().length();
		}
		
		ByteBuffer b = ByteBuffer.allocate(totalLen + users.size());
		for (User u : users) {
			b.put((byte) u.getNick().length());
			b.put(u.getNick().getBytes());
		}
		
		return b.array();
	}
	
}
