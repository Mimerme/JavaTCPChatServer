import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class User implements Runnable{
	private String nick;
	private InputStream input;
	private OutputStream output;
	
	public User(InputStream sock_in, OutputStream sock_out, String nickname) {
		this.nick = nickname;
		this.input = sock_in;
		this.output = sock_out;
	}
	
	public void sendMessage(byte returncode, String message) {
		
	}
	
	public void parseRequest(short opcode, String message) {
        System.out.println(opcode + ":" + message);
        
        switch(opcode) {
        	case 0xFF:
        		System.out.println(message);
        		break;
        	default:
        		break;
     
        }	
	}
	
	public void run() {
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
	            int length = ByteBuffer.wrap(commandBuf, 0, 4).getInt();
	            short version = ByteBuffer.wrap(commandBuf, 4, 2).getShort();
	            byte opcode = commandBuf[6];
	            
	            if(version != 0x417) {
	            	System.out.println("Incorrect Version");
	            	continue;
	            }
	            
	            byte[] msg = new byte[length];
	            input.read(msg);
	            String m = new String(ByteBuffer.wrap(msg).array(), "ASCII");
	            
	            parseRequest(opcode, m);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	
}
