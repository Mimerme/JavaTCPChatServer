import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {

	static int CONNECTION_COUNT = 0;
	
	
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(5000);
		Socket socket = server.accept();
		System.out.println("New connection");
		
		//First signlethreaded, then multithread
		
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		
		int c;
		byte[] commandBuf = new byte[7];
        while (input.read(commandBuf, 0, 7) != -1) {
        	
        	
            StringBuilder sb = new StringBuilder();
            for (byte b : commandBuf) {
                sb.append(String.format("%02X ", b));
            }
            
            int length = ByteBuffer.wrap(commandBuf, 0, 4).getInt();
            short version = ByteBuffer.wrap(commandBuf, 4, 2).getShort();
            byte opcode = commandBuf[6];
            
            if(version != 0x417) {
            	//System.out.println("Incorrect Version");
            }
            //System.out.println(length + " " + version + " ");

            byte[] msg = new byte[length];
            input.read(msg);
            		
            String m = new String(ByteBuffer.wrap(msg).array(), "ASCII");
            
        }
        
        output.close();
        input.close();
		
	}
	
	public static void ParseRequest(short opcode, String message) {
        System.out.println(opcode + ":" + message);
        
        switch(opcode) {
        	case 0xFF:
        		System.out.println(message);
        		break;
        	default:
        		break;
     
        }
	}

}
