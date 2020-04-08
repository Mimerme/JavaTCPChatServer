import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class User implements Runnable{
	private String nick;
	private InputStream input;
	private OutputStream output;
	private ChatServer server;
	
	public User(InputStream sock_in, OutputStream sock_out, String nickname, ChatServer server) {
		this.nick = nickname;
		this.input = sock_in;
		this.output = sock_out;
		this.server = server;
	}
	
	public void sendMessage(String message, boolean success) throws IOException {
		//Add one for the opcode
		int len = message.length() + 1;
		byte returnCode;
		
		if(success)
			returnCode = 0x00;
		else
			returnCode = 0x01;
		
		//Add 1 to account for the return code
		//2 for 0x0416
		//4 for the length
		ByteBuffer b = ByteBuffer.allocate(len + 1 + 2 + 4);
		b.putInt(len);
		b.putShort((short) 0x0417);
		b.put((byte) 0xFE);
		b.put(returnCode);
		b.put(message.getBytes());
		output.write(b.array());
	}
	
	public void parseRequest(byte opcode, byte[] message) throws IOException {
        System.out.println(String.format("%02X ", opcode));
        
        switch(opcode) {
        	//Handhskae
        	case (byte) 0xFF:
			try {
				System.out.println(new String(message, "ASCII"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        		sendMessage(this.nick, true);
        		break;
        	case 0x0A:
        		break;
        	default:
        		System.out.println("Missing opcode");
        		break;
     
        }	
	}
	
	public void run() {
		System.out.println("Running");
/*		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	synchronized (input) {
			        try {
						input.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		    }
		}));*/
		
		
		int c;
		byte[] commandBuf = new byte[7];
		try {
			while(input.read(commandBuf, 0, 7) != -1) {
				System.out.println("hello");
	            int length = ByteBuffer.wrap(commandBuf, 0, 4).getInt();
	            short version = ByteBuffer.wrap(commandBuf, 4, 2).getShort();
	            byte opcode = commandBuf[6];
	            
	            if(version != 0x417) {
	            	System.out.println("Incorrect Version");
	            	continue;
	            }
	            
	            byte[] msg = new byte[length - 1];
	            input.read(msg);
	            
	            parseRequest(opcode, msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	
}
