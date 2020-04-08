import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
	private int port;
	private ServerSocket server;
	
	public ChatServer(int port) {
		this.port = port;
	}

	
	//Start listening for new users
	//New thread for each user
	public void start() throws IOException {
		ServerSocket server = new ServerSocket(port);

		while(true) {
			Socket socket = server.accept();
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();
		
			User connection = new User(input, output, "test");
			
			System.out.println("Created a new user");
			new Thread(connection, "test");
		}
	}
	
}
