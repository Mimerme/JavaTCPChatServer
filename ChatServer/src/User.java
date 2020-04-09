import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

	public synchronized boolean inRoom() {
		return !room.equals("");
	}

	public synchronized String getRoom() {
		return room;
	}

	public synchronized void setRoom(String room) {
		this.room = room;
	}

	public synchronized String getNick() {
		return nick;
	}

	public synchronized void sendChatMessage(byte opcode, byte[] message) throws IOException {
		int len = message.length;

		//Add 1 to account for the return code
		//2 for 0x0416
		//4 for the length
		ByteBuffer b = ByteBuffer.allocate(len + 1 + 2 + 4);
		b.putInt(len);
		b.putShort((short) 0x0417);
		b.put(opcode);
		b.put(message);
		output.write(b.array());
	}

	public synchronized void sendByteMessage(byte[] message, boolean success) throws IOException {
		//Add one for the status code
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

	public synchronized void sendMessage(String message, boolean success) throws IOException {
		//Add one for the status code
		int len = message.length() + 1;
		byte returnCode;

		if(success)
			returnCode = 0x00;
		else
			returnCode = 0x01;

		//Add 1 to account for the return code
		//2 for 0x0416
		//4 for the length
		System.out.println("len: " + len);
		ByteBuffer b = ByteBuffer.allocate(len + 1 + 2 + 4);
		b.putInt(len);
		b.putShort((short) 0x0417);
		b.put((byte) 0xFE);
		b.put(returnCode);
		b.put(message.getBytes());
		output.write(b.array());
	}

	//Returns continue?
	public boolean parseRequest(byte opcode, byte[] message) throws IOException {
		System.out.println(String.format("%02X ", opcode));

		switch(opcode) {
		//Handhskae
		case (byte) 0xFF:
			System.out.println(new String(message, "ASCII"));
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
			if(inRoom()) {
				server.leaveRoom(this);
				sendMessage("", true);
			}
			else {
				//Update the server
				server.disconnect(this);

				//Close the connections
				input.close();
				output.close();
				return false;
			}
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
			String newNick = new String(Arrays.copyOfRange(message, 1, message.length), "ASCII");
			System.out.println(newNick);
			if(server.validNick(newNick, this)) {
				this.nick = newNick;
				sendMessage("", true);
			}
			else
				sendMessage("This nick has been nicked by someone else.", false);
			break;
			//Direct Message
		case 0x0F:
			byte destLen = message[0];
			short msgLen = ByteBuffer.wrap(message, destLen + 1, 2).getShort();

			String total1 = new String(message, "ASCII");
			String dest = total1.substring(1, 1 + destLen);
			String msg = total1.substring(3 + destLen, 3 + destLen + msgLen);

			System.out.println("dest: " + dest + " msg: " + msg);
			if(server.sendDirectMessage(dest, msg, this.getNick()))
				sendMessage("", true);
			else
				sendMessage("Nick not present", false);
			break;
		case 0x10:
			byte roomLen = message[0];
			short msgLen2 = ByteBuffer.wrap(message, roomLen + 1, 2).getShort();

			String total2 = new String(message, "ASCII");
			String room = total2.substring(1, 1 + roomLen);
			String roommsg = total2.substring(3 + roomLen, 3 + roomLen + msgLen2);

			System.out.println("room: " + room);
			if(room.equals(this.room) && !room.equals("")) {
				server.sendRoomMessage(room, roommsg, this.getNick());
				sendMessage("", true);
			}
			else {
				sendMessage("You shout into the void and hear nothing.", false);	
			}

			break;
		default:
			System.out.println("Missing opcode");
			break;

		}
		
		return true;
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

		System.out.println("User joined");
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

				if(parseRequest(opcode, msg) == false)
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("User unexpectedly disconnected");
		try {
			//Update the server
			server.disconnect(this);

			//Close the connections
			input.close();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
