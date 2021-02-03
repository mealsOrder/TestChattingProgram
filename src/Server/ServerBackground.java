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
	// 지금까지 한일 : GUI연동시키면서 서버GUI에 메시지 띄운다.
	// 다음 이슈 : Gui 상에서 1:1 채팅을 진행
	private ServerSocket serverSocket;
	private Socket socket;
	private ServerGui gui;
	private String msg;

	// 사용자들의 정보를 저장하는 맵
	private Map<String, DataOutputStream> clientsMap = new HashMap<String, DataOutputStream>();

	public final void setGui(ServerGui gui) {
		this.gui = gui;
	}
	
	public void setting() throws IOException {
		
		// 교통정리
		Collections.synchronizedMap(clientsMap); 
		serverSocket = new ServerSocket(7777);
		while (true) {
			// 서버가 할일 : 계속 접속받는것.
			System.out.println("서버 대기중...");
			socket = serverSocket.accept(); // �ݺ��ؼ� ��� ����ڸ� �޴´�.
			System.out.println(socket.getInetAddress() + "에서 접속했습니다.");
			// 여기서 새로운 사용자 쓰레드 클래스 생성해서 소켓정보를 넣어준다.
			Receiver receiver = new Receiver(socket);
			receiver.start();
		}
	}

	public static void main(String[] args) throws IOException {
		ServerBackground serverBackground = new ServerBackground();
		serverBackground.setting();
	}

	// 맵의내용(클라이언트) 저장과 삭제
	public void addClient(String nick, DataOutputStream out) throws IOException {
		sendMessage(nick + "님이 접속하셨습니다.");
		clientsMap.put(nick, out);
	}

	public void removeClient(String nick) {
		sendMessage(nick + "님이 나가셨습니다.");
		clientsMap.remove(nick);
	}

	// 매시지 내용 전송
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

		// 리시버가 한일 : 자기 혼자서 네트워크 처리 계속... 듣기... 처리
		public Receiver(Socket socket)throws IOException{
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			nick=in.readUTF();
			addClient(nick,out);
		}
		
		public void run() {
			try {// 계속 듣기만!
				while(in!=null) {
					msg=in.readUTF();
					sendMessage(msg);
					gui.appendMsg(msg);
				}
			}catch(IOException e) {
				// 사용접속종료시 여기서 에러발생.
				removeClient(nick);
			}
		}
	}

}
