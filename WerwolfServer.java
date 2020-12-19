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
	
	public static int index = 0;
	public static HashMap<Integer,WebSocket> ids= new HashMap<>();
	public static String phase = "";
	public static String stage = "";
	public HashMap<String,LinkedList<WebSocket>> rollen = new HashMap<>();
	public static LinkedList<WebSocket> connections = new LinkedList<>();
	public static HashMap<WebSocket,String> names = new HashMap<>();
	public static String defaultNames[] = new String[]{"Anna","Bob","Manfred","Fritz","TinaToastbrot","Alice","MaxMustermann","Pascal","Johann","Torben","Emma","Manuel","Anni"};
	public static WebSocketServer server;
	public static String Verliebte ="";
	public static int nacht = 0;
	public static HashMap<WebSocket,Integer> werwolfWahl = new HashMap<>();
	public int werwolfTarget = -1;
	public static HashMap<WebSocket,int> stimmen = new HashMap<>();
	
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
		if(phase=="lobby"){
		ids.put(index,conn);
		index++;
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
		}else{
			conn.send("[commenceGame]");
			refreshCircle();
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
		for (int id: ids.keySet()) {			
			if(ids.get(id) == conn)
				ids.remove(id);
		}
		connections.remove(conn);	//Removes Connection from the List of all connections	
		String players = "";
		for (WebSocket key: names.keySet()) {
			players = players + "," + names.get(key);
		}		
		broadcast( "[refreshPlayers]:"+players  ); //This method sends a message to all clients connected
		names.remove(conn);
		winTest();
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
				server.broadcast("[displayText]:Das ganze Dorf schläft ein;[deactivateButton]:players;");
				stage = "Amor_Setup";
				game(conn, message);
				break;
			case "Amor_Setup":
				if(rollen.get("amor").size()>0 && nacht == 0){
					server.broadcast("[displayText]:Amor erwacht, und sucht sich 2 Mitspieler aus;");
					for(WebSocket amor:rollen.get("amor")){
						amor.send("[displayText]:Du wählst 2 Spieler aus, und bestätigst dann;[activateButton]:buttonConfirm;[activateButton]:players;");						
					}
				}
				stage="Amor_Auswahl";
				break;
			case "Amor_Auswahl":
				if(rollen.get("amor").size()>0 && nacht == 0){
					String[] message2 = (message.split(":"))[1].substring(1).split(",");
					if(message2.length == 2){
						Verliebte = message2[0] + ":" + message2[1];
						server.broadcast("[displayText]:Amor traf seine Wahl;[deactivateButton]:buttonConfirm;[deactivateButton]:players;");
						stage = "Werwolf_Setup";
						game(conn,message);
					}else{
						conn.send("[displayText]:Bitte 2 Personen auswählen;");
					}	
				}
				break;
			case "Werwolf_Setup":
				if(rollen.get("werwolf").size()>0){
					server.broadcast("[displayText]:Die Werwölfe suchen sich ihr Opfer;");
					sendToWerwolf("[activateButton]:players;[activateButton]:buttonConfirm;");	
					for(WebSocket wolf:rollen.get("werwolf")){
						werwolfWahl.put(wolf,-1);
					}	
				}
				stage = "Werwolf_Wahl";				
				break;
			case "Werwolf_Wahl":
				if(rollen.get("werwolf").size()>0){
					String[] message2 = (message.split(":"))[1].substring(1).split(",");
					if(message2.length != 1){
						conn.send("[displayText]:Bitte genau ein Ziel angeben;");
					}else{
						werwolfWahl.put(conn,Integer.parseInt(message2[0]));
						boolean test = true;
						Integer[] temp2 = new Integer[werwolfWahl.size()];
						werwolfWahl.values().toArray(temp2);
						int temp = temp2[0];
						for(int i:temp2){
							if(i != temp || i == -1)
								test=false;
						}	
						if(test){
							werwolfTarget = temp;
							stage="Werwolf_Entschluss";
							game(conn,message);
						}	
					}	
				}
				break;
			case "Werwolf_Entschluss":
				if(rollen.get("werwolf").size()>0){
				sendToWerwolf("[displayText]:Ihr habt " + names.get(ids.get(werwolfTarget)) + " gewählt;[deactivateButton]:players;[deactivateButton]:buttonConfirm;");
				}
				stage="Leibwächter_Setup";
				game(conn,message);
			case "Leibwächter_Setup":
				if(rollen.get("leibwaechter").size()>0){
				server.broadcast("[displayText]:Der Leibwächter beschützt ein Haus;");
				rollen.get("leibwaechter")[0].send("[displayText]:Such eine Person zum beschützen aus;[activateButton]:players;[activateButton]:buttonConfirm;");
				}
				stage="Leibwächter_Wahl";
				break;
			case "Leibwaechter_Wahl":
				if(rollen.get("leibwaechter").size()>0){
					String[] message2 = (message.split(":"))[1].substring(1).split(",");
					if(message2.length != 1){
						conn.send("[displayText]:Bitte genau ein Ziel angeben;");
					}else{
						if(werwolfTarget == Integer.parseInt(message2[0])){
								werwolfTarget = -1;
						}
					conn.send("[deactivateButton]:players;[deactivateButton]:buttonConfirm;");
					}	
					
				}
				stage = "Tag_Setup";
			case "Tag_Setup":
				if(werwolfTarget != -1){
					death(ids.get(werwolfTarget));
				}else{
					server.broadcast("[displayText]:Niemand ist gestorben;");
				}
				server.broadcast("[displayText]:Wählt wen ihr erhängen wollt;[activateButton]:players;[activateButton]:buttonConfirm;"/*[activateButton]:buttonSkip;"*/);
				stage="Tag_Wahl";
				for(WebSocket sock:connections){
				stimmen.put(sock,0); 
				}
				int i = 0;
				break;
			case "Tag_Wahl":
				String[] message2 = (message.split(":"))[1].substring(1).split(",");
					if(message2.length != 1){
						conn.send("[displayText]:Bitte genau ein Ziel angeben;");
					}else{
						i++;
						stimmen.put(ids.get(Integer.parseInt(message2[0])),stimmen.get(ids.get(Integer.parseInt(message2[0])))+1);
						conn.send("[displayText]:Du stimmtest für " +names.get(ids.get(Integer.parseInt(mesage2[0]))) +";[deactivateButton]:players;[deactivateButton]:buttonConfirm;");
						if(i==connections.size()){
							stage="Tag_Abend";
							game(conn,message);
						}	
					}
				break;
			case "Tag_Abend":
				WebSocket maxEntry = null;
				for (WebSocket stimme : stimmen.keySet()){
					if(maxEntry == null || stimmen.get(stimme) > stimmen.get(maxEntry))
						maxEntry = stimme;
				}
				server.broadcast("[displayText]:"+names.get(maxEntry)+" ist gestorben;");
				death(maxEntry);
		}
	}	
	
	public void winTest(){
		if(rollen.get("werwolf").size() == 0 ){
			server.broadcast("[displayText]:Die Dorfbewoner haben gewonnen;");
			Thread.sleep(10000);
		server.broadcast("[reload];");
		}else if( rollen.get("dorfbewohner").size() == 0 && rollen.get("hexe").size() == 0 && rollen.get("leibwaechter").size() == 0 && rollen.get("seherin").size() == 0 && rollen.get("amor").size() == 0){
			server.broadcast("[displayText]:Die Werwölfe haben gewonnen;");
			Thread.sleep(10000);
			server.broadcast("[reload];");
		}
		
	}	
	
	public void death(WebSocket conn){
		server.broadcast("[displayText]:"+names.get(conn)+" ist gestorben;");
		conn.send("[reload];");
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
		LinkedList<WebSocket> connectionsCopy = (LinkedList<WebSocket>) connections.clone();
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
		for (WebSocket key: names.keySet()) {			
			key.send("[setName]:"+names.get(key));
		}		
		phase = "game";
		stage = "Dorfbewohner_Nacht";
		nacht = 0;
		refreshCircle();
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
		System.out.println(connections.size());
		float alpha = 360/connections.size();
		int n = 0;
		String befehl = "[updateCircle]:";
		for(WebSocket conn: connections){
			for (int id: ids.keySet()) {			
				if(ids.get(id) == conn){
					befehl = befehl + "," +names.get(conn) + "|" + id + "|" + (8.0 + (Math.sin(Math.toRadians(alpha) * n) * 7)) + "|" + (17.0 + (Math.cos(Math.toRadians(alpha) * n) * 11)) + "|dorfbewohner kreis.png";
					n++;
				}
			}
		}
		server.broadcast(befehl);
		
		System.out.println(connections.size());

		n = 0;
		befehl = "[updateCircle]:";
		for(WebSocket conn: connections){
			for (int id: ids.keySet()) {			
				if(ids.get(id) == conn){
					if(rollen.get("werwolf").contains(conn)){
						befehl = befehl + "," +names.get(conn) + "|" + id + "|" + (8.0 + (Math.sin(Math.toRadians(alpha) * n) * 7)) + "|" + (17.0 + (Math.cos(Math.toRadians(alpha) * n) * 11)) + "|werwolf kreis.png" ;
					}else{
						befehl = befehl + "," +names.get(conn) + "|" + id + "|" + (8.0 + (Math.sin(Math.toRadians(alpha) * n) * 7)) + "|" + (17.0 + (Math.cos(Math.toRadians(alpha) * n) * 11)) + "|dorfbewohner kreis.png";
					}
					n++;
				}
			}
		}
		sendToWerwolf(befehl);
		
	}	
	
	public void sendToWerwolf(String message){
	for(WebSocket wolf:rollen.get("werwolf"))	
		wolf.send(message);
	}	
}
























