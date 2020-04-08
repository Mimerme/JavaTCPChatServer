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
	private String room = "";

	public User(InputStream sock_in, OutputStream sock_out, String nickname, ChatServer server) {
		this.nick = nickname;
		this.input = sock_in;
		this.output = sock_out;
		this.server = server;
	}

	public boolean inRoom() {
		return !room.equals("");
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getNick() {
		return nick;
	}

	public void sendByteMessage(byte[] message, boolean success) throws IOException {
		//Add one for the opcode
		int len = message.length + 1;
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
		b.put(message);
		output.write(b.array());
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

	public void sendMessage() {

	}

	public void parseRequest(byte opcode, byte[] message) throws IOException {
		System.out.println(String.format("%02X ", opcode));

		switch(opcode) {
		//Handhskae
		case (byte) 0xFF:
			/*
			 * try { //System.out.println(new String(message, "ASCII")); } catch
			 * (UnsupportedEncodingException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
			sendMessage(this.nick, true);
		break;
		//Join room
		case 0x0A:
			byte roomNameLen = message[0];
			byte passwordLen = message[roomNameLen + 1];

			String total = new String(message, "ASCII");
			String roomname = total.substring(1, 1 + roomNameLen);
			String password = total.substring(2 + roomNameLen, 2 + roomNameLen + passwordLen);

			//System.out.println(roomname + " " + password);

			int ret = server.joinRoom(this, roomname, password);

			if(ret == 1) 
				sendMessage("You attempt to bend space and time to reenterwhere you already are. You fail", false);
			else if(ret == 2)
				sendMessage("Invalid password. You shall not pass.", false);
			else
				sendMessage("", true);


			break;
			//Leave room
		case 0x0B:
			server.leaveRoom(this);
			sendMessage("", true);
			break;
			//List rooms
		case 0x0C:
			sendByteMessage(server.getRooms(), true);
			break;
			//List users
		case 0x0D:
			//Gets the user list as a byte array;
			byte[] users;
			if(inRoom())
				users = server.getRoomUserList(this.room);
			else 
				users = server.getUserList();

			sendByteMessage(users, true);
			break;
			//Set nickname
		case 0x0E:
			String newNick = new String(message, "ASCII");
			if(server.validNick(newNick, this)) {
				this.nick = newNick;
				sendMessage("", true);
			}
			else
				sendMessage("This nick has been nicked by someone else.", false);
			break;
			//Direct Message
		case 0x0F:
			
			break;
		case 0x10:
			break;
		default:
			System.out.println("Missing opcode");
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

				parseRequest(opcode, msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



}
