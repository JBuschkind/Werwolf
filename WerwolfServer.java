import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.HashMap;
import java.io.*;
import java.lang.Math;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;


public class WerwolfServer extends WebSocketServer {

	public static String phase = "";
	public static String stage = "";
	public HashMap<String,LinkedList<WebSocket>> rollen = new HashMap<>();
	public static LinkedList<WebSocket> connections = new LinkedList<>();
	public static HashMap<WebSocket,String> names = new HashMap<>();
	public static String defaultNames[] = new String[]{"Anna","Bob","Manfred","Fritz","TinaToastbrot","Alice","MaxMustermann","Pascal","Johann","Torben","Emma","Manuel","Anni"};
	public static WebSocketServer server;
	public static String Verliebte ="";
	public static int nacht = 0;
	
	public WerwolfServer(){
		//phase = "";
		//Rollen = new HashMap<>();
		//connections = new LinkedList<>();
		//names = new HashMap<>();
		//defaultNames = new String[]{"Anna","Bob","Manfred","Fritz","TinaToastbrot","Alice","MaxMustermann","Pascal"};
	}
	
  
	public WerwolfServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {		
		connections.add(conn);	//Adds connection to List of all connections	
		names.put(conn,getRandomName()); //Gives the Player a random Name	
		String players = "";
		for (WebSocket key: names.keySet()) {
			players = players + "," + names.get(key);
		}
		//conn.send("[init]Players:"+players); //This method sends a message to the new client
		broadcast( "[refreshPlayers]:"+players  ); //This method sends a message to all clients connected
		System.out.println("new connection to " + conn.getRemoteSocketAddress() + " with the name " + names.get(conn)); //+ "with the name" + names.get(conn));
		//System.out.println(connections);	//Debug Output
		
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
		connections.remove(conn);	//Removes Connection from the List of all connections	
		String players = "";
		for (WebSocket key: names.keySet()) {
			players = players + "," + names.get(key);
		}		
		broadcast( "[refreshPlayers]:"+players  ); //This method sends a message to all clients connected
		names.remove(conn);
		//System.out.println(connections);	//Debug Output
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
		
		switch(phase){
		case "lobby":
			lobby(conn, message);
			break;
		case "game":	
			game(conn,message);
			break;
		}	
	}

	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
		System.out.println("received ByteBuffer from "	+ conn.getRemoteSocketAddress());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
		System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
	}
	
	@Override
	public void onStart() {
		System.out.println("server started successfully");
	}
	

	public static void main(String[] args){
		
		
		String host = "busch.click";
		phase = "lobby";
		int port = 3001;
		
		server = new WerwolfServer(new InetSocketAddress(host, port));
		
		SSLContext context = getContext();
		if (context != null) {
			server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(getContext()));
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() 
		{ 
			public void run()
			{ 
			System.out.println("Shutdown Hook is running !");
			try{
			server.stop(1000);
			} catch(InterruptedException e){
			} 
			}
		}); 
		server.run();
	}
	
	private static SSLContext getContext() {
    SSLContext context;
    String password = "thisisapassword";
    String pathname = "/etc/letsencrypt/live/busch.click";
    try {
      context = SSLContext.getInstance("TLS");

      byte[] certBytes = parseDERFromPEM(getBytes(new File(pathname + File.separator + "fullchain.pem")),
          "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
      byte[] keyBytes = parseDERFromPEM(
          getBytes(new File(pathname + File.separator + "privkey.pem")),
          "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

      X509Certificate cert = generateCertificateFromDER(certBytes);
      RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

      KeyStore keystore = KeyStore.getInstance("JKS");
      keystore.load(null);
      keystore.setCertificateEntry("cert-alias", cert);
      keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[]{cert});

      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(keystore, password.toCharArray());

      KeyManager[] km = kmf.getKeyManagers();

      context.init(km, null, null);
    } catch (Exception e) {
      context = null;
    }
    return context;
  }

  private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
    String data = new String(pem);
    String[] tokens = data.split(beginDelimiter);
    tokens = tokens[1].split(endDelimiter);
    return DatatypeConverter.parseBase64Binary(tokens[0]);
  }

  private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes)
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

    KeyFactory factory = KeyFactory.getInstance("RSA");

    return (RSAPrivateKey) factory.generatePrivate(spec);
  }

  private static X509Certificate generateCertificateFromDER(byte[] certBytes)
      throws CertificateException {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");

    return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
  }

  private static byte[] getBytes(File file) {
    byte[] bytesArray = new byte[(int) file.length()];

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      fis.read(bytesArray); //read file into bytes[]
      fis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bytesArray;
  }
  
  public static String getRandomName()
	{
		Random rand = new Random();
		String name = defaultNames[rand.nextInt(defaultNames.length)];
		rand = null;
		return name;      
		
	} 
	
	
	public void lobby(WebSocket conn, String message){
		String[] Befehl1 = message.split(";");
		for(String element : Befehl1){
			String[] Befehl2 = element.split(":");
			switch(Befehl2[0]){
			case "[changeName]":
				setName(conn,Befehl2[1]);	
				break;
			case "[startGame]":
				startGame(conn,Befehl2[1]);
				break;
			}
		}	
	
	}
	
	/*
			if(rollen.get("amor").size()>0){
						
				}
	*/			
	
	public void game(WebSocket conn, String message){
		switch(stage){
			case "Dorfbewohner_Nacht":
				server.broadcast("[displayText]:Das ganze Dorf schläft ein;");
				stage = "Amor_Setup";
				game(conn, message);
				break;
			case "Amor_Setup":
				if(rollen.get("amor").size()>0 && nacht == 0){
					server.broadcast("[displayText]:Amor erwacht, und sucht sich 2 Mitspieler aus;");
					for(WebSocket amor:rollen.get("amor")){
						amor.send("[displayText]:Du wählst 2 Spieler aus, und bestätigst dann;[activateButton]:confirm;");						
					}
				}
				stage="Amor_Auswahl";
				break;
			case "Amor_Auswahl":
				if(rollen.get("amor").size()>0 && nacht == 0){
					server.broadcast("[displayText]:Amor traf seine Wahl;");
				}	
		}
	}	
	
	public void setName(WebSocket conn, String name){
		names.remove(conn);
		names.put(conn,name);
		String players = "";
		for (WebSocket key: names.keySet()) {
			players = players + "," + names.get(key);
		}
		broadcast( "[refreshPlayers]:"+players  );
	}	
	
	//[startGame]:dorfbewohner_0,hexe_0,amor_0,seherin_0,leibwaechter_0,werwolf_0;
	
	public void startGame(WebSocket conn, String parameter) {
		LinkedList<WebSocket> connectionsCopy = connections;
		Random rand = new Random();
		for(String element: parameter.split(",")){
			String[] element2 = element.split("_");
			rollen.put(element2[0],new LinkedList<WebSocket>());
			if(Integer.parseInt(element2[1])>0){
			for(int i = 0; i < Integer.parseInt(element2[1]);i++){
				int n = rand.nextInt(connectionsCopy.size());
				rollen.get(element2[0]).add(connectionsCopy.get(n));
				connectionsCopy.remove(n);
			}
			}			
		}
		server.broadcast("[commenceGame];");
		Set<String> keys = rollen.keySet();
		for(String key:keys){
			for(WebSocket conn2 : rollen.get(key)){
				conn2.send("[setRole]:" + key +";");
			}	
		}			
		phase = "game";
		stage = "Dorfbewohner_Nacht";
		nacht = 0;
		game(conn,parameter);
		//System.out.println("HI");
		rand = null;
		/*for (String name: rollen.keySet()){
            String key = name.toString();
            String value = rollen.get(name).toString();  
            server.broadcast("[displayText]:" + key + " " + value + ";");  
		} */
	}
	
	public void refreshCircle(){
		float alpha = 360/connections.size();
		int n = 0
		String befehl = "[updateCircle]:";
		for(WebSocket conn: connections){
			befehl = befehl + "," +names.get(conn) + "_" + (Math.sin(Math.toRadians(alpha) * n) * 1) + "_" + (Math.cos(Math.toRadians(alpha) * n) * 1)
			n++
		}
		server.broadcast(befehl);
	}	
}
























