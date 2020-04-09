import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {

	
	
	public static void main(String[] args) throws IOException {
		int port = 5000;
		if(args.length == 1)
			port = Integer.parseInt(args[0]);
		new ChatServer(port).start();
	}

}
