package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerBackground {
	// GUI연동시키면서 서버 GUI에 메시지 띄운다.
	// GUI상에서 1:1 채팅을 하려고 한다.
	private ServerSocket serverSocket;
	private Socket socket;
	private ServerGui gui;
	private String msg;

	// 사용자들의 정보를 저장하는 맵.
	private Map<String, DataOutputStream> clientsMap = new HashMap<String, DataOutputStream>();

	public final void setGui(ServerGui gui) {
		this.gui = gui;
	}

	public void setting() throws IOException {
		Collections.synchronizedMap(clientsMap); // 교통정리
		serverSocket = new ServerSocket(7777);
		while (true) {
			// 서버가 할 일 : 계속 접속받는다.
			System.out.println("서버 대기중...");
			socket = serverSocket.accept(); // 반복해서 계속 사용자를 받는다.
			System.out.println(socket.getInetAddress() + " 에서 접속했습니다.");
			// 새로운 사용자가 쓰레드 클래스를 생성해서 소켓정보를 넣어준다.
			Receiver receiver = new Receiver(socket);
			receiver.start();
		}
	}

	public static void main(String[] args) throws IOException {
		ServerBackground serverBackground = new ServerBackground();
		serverBackground.setting();
	}

	// 맵의 내용(클라이언트) 저장과 삭제
	public void addClient(String nick, DataOutputStream out) throws IOException {
		sendMessage(nick + "님이 접속하셨습니다.");
		clientsMap.put(nick, out);
	}

	public void removeClient(String nick) {
		sendMessage(nick + "님이 나가셨습니다.");
		clientsMap.remove(nick);
	}

	// 메세지 내용 전파
	public void sendMessage(String msg) {
		Iterator<String> it = clientsMap.keySet().iterator();
		String key = "";
		while (it.hasNext()) {
			key = it.next();
			try {
				clientsMap.get(key).writeUTF(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// --------------------------------------------------------------
	class Receiver extends Thread {
		private DataInputStream in;
		private DataOutputStream out;
		private String nick;

		// 리시버가 할일 : 네트워크 처리... 듣기... >> 계속 반복
		public Receiver(Socket socket)throws IOException{
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			nick=in.readUTF();
			addClient(nick,out);
		}
		
		public void run() {
			try {
				while(in!=null) {
					msg=in.readUTF();
					sendMessage(msg);
					gui.appendMsg(msg);
				}
			}catch(IOException e) {
				// 사용접속 종료시 여기서 에러 발생.
				removeClient(nick);
			}
		}
	}

}
