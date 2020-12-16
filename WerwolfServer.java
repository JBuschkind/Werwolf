import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WerwolfServer{
	public static void main(String[] args){
		String host = "localhost";
		int port = 3001;
		
		WebSocketServer server = new SimpleServer(new InetSocketAddress(host, port));
		server.run();
	}
}