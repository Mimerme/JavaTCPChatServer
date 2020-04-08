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
		/*
		 * ServerSocket server = new ServerSocket(5000); System.out.println("Waiting");
		 * Socket socket = server.accept(); System.out.println("New connection");
		 * 
		 * //First signlethreaded, then multithread
		 * 
		 * InputStream input = socket.getInputStream(); OutputStream output =
		 * socket.getOutputStream();
		 * 
		 * 
		 * int c; byte[] commandBuf = new byte[7]; while (input.read(commandBuf, 0, 7)
		 * != -1) {
		 * 
		 * 
		 * StringBuilder sb = new StringBuilder(); for (byte b : commandBuf) {
		 * sb.append(String.format("%02X ", b)); }
		 * 
		 * int length = ByteBuffer.wrap(commandBuf, 0, 4).getInt(); short version =
		 * ByteBuffer.wrap(commandBuf, 4, 2).getShort(); byte opcode = commandBuf[6];
		 * 
		 * if(version != 0x417) { //System.out.println("Incorrect Version"); }
		 * //System.out.println(length + " " + version + " ");
		 * 
		 * byte[] msg = new byte[length]; input.read(msg); String m = new
		 * String(ByteBuffer.wrap(msg).array(), "ASCII");
		 * 
		 * }
		 * 
		 * output.close(); input.close();
		 */
		
	}

}
