package TTTO.MasterServer;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.JTextArea;
public class MasterServer
{
	
		
	private final int hostPort;
	private ExecutorService pool; // The thread pool
	private ServerSocket serverSocket;
	private LinkedList<ClientHandler> allClients = new LinkedList<ClientHandler>();
	private LinkedList<Room> allGameRooms = new LinkedList<Room>();
	private IMasterServerLog logArea = null; 
	
	
	
	public static String	REGISTER_GAMEROOM 	= "tttms0",
							RETRIEVE_GAMELIST 	= "tttms1",
							UNREGISTER_GAMEROOM	= "tttms2";
	
	/**
	 * Compares the string to all commands and checks if the string is a command
	 * @param aCommand The string to check
	 * @return true if the string is a command
	 */
	public static boolean isValidCommand(String aCommand)
	{
		return (aCommand.equals(REGISTER_GAMEROOM) || 
				aCommand.equals(RETRIEVE_GAMELIST) ||
				aCommand.equals(UNREGISTER_GAMEROOM));
	}
							
	
	/**
	 * Creates a master server keeping track of available games.
	 * 
	 * @param aPort The port the server will live on
	 * @param aThreads Number of threads in the thread pool
	 * @param aMaxRooms How many games that can be registered to the master server
	 */
	public MasterServer(int aPort, int aThreads)
	{
		hostPort = aPort;
		pool = Executors.newFixedThreadPool(aThreads);
	}
	
	/**
	 * Creates a master server keeping track of available games.
	 * 
	 * @param aPort The port the server will live on
	 * @param aThreads Number of threads in the thread pool
	 * @param aMaxRooms How many games that can be registered to the master server
	 * @param aLog Where the log messages will go
	 */
	public MasterServer(int aPort, int aThreads, IMasterServerLog aLog)
	{
		this(aPort, aThreads);
		logArea = aLog;
	}
	
	/**
	 * Starts the server.
	 */
	public void runServer() throws UnknownHostException, IOException
	{
	
			
			serverSocket = new ServerSocket(hostPort);
			log("Server running on:\t" + serverSocket.getInetAddress().getLocalHost().getHostAddress()+ ":" + serverSocket.getLocalPort());
			while (true)
			{
				Socket connection = serverSocket.accept(); // Waits  for connection
				log("Client Connected:\t" + HelpMethods.socketAsString(connection));
				
				ClientHandler ch = new ClientHandler(connection);
				pool.execute(ch);
				allClients.add(ch);
			}
	
	}
	
	/**
	 * Logs a message with a timestamp
	 * @param aMessage the message
	 */
	public void log(String aMessage)
	{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestampMsg = sdf.format(Calendar.getInstance().getTime());
		timestampMsg += ":     " + aMessage;
		if(logArea != null)
		{
			logArea.log(timestampMsg+"\n");
		}
	}
	
	/**
	 * Registers the game room to the master server. 
	 * @param Room the room to register
	 */
	private synchronized void registerGameRoom(Room aRoom)
	{
		if (aRoom == null)
			throw new NullPointerException();
		allGameRooms.add(aRoom);
		log("Registered Game:\t" + aRoom.toString());
	}
	
	/**
	 * Unregisters the game room from the master server
	 * @param aRoom the room to unregister
	 */
	private synchronized void unregisterGameRoom(Room aRoom)
	{
		if (aRoom == null)
			throw new NullPointerException();
		if (allGameRooms.remove(aRoom))
		{
			log("Unregistered Game:\t" +aRoom.toString());
		}
	}
	
	/**
	 * Return some server stats
	 */
	public String getServerStats()
	{
		String output = null;
		try
		{
			if (serverSocket != null)
			{
				output = "Server IP:\t\t" + serverSocket.getInetAddress().getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort()+ "\n"+
					"Connected Players: \t" + allClients.size() +"\n"
					+ "Game Rooms: \t\t" + allGameRooms.size() +"\n";
			}
		} 
		catch (UnknownHostException e)
		{
			
			e.printStackTrace();
		}
		return output;
	}
	
	/**
	 * Finds the game room created by a client
	 * @param aClient The client
	 * @return the game room created by the client. null if no room were created
	 */
	private Room findRoomCreatedBySocket(Socket aClient)
	{
		if (aClient == null)
			throw new NullPointerException();
		Room clientRoom = null;
		String clientAddress = aClient.getInetAddress().getHostAddress();
		int clientPort = aClient.getPort();
		System.out.println(clientAddress + " " + clientPort);
		for (Room r : allGameRooms)
		{
			System.out.println("Rooms: " + r.getHostAddress()+ " " + r.getHostPort());
			if (r.getHostAddress().equals(clientAddress)
					&& r.getHostPort() == clientPort)
			{
				clientRoom = r;
				break;
			}
				
		}
		return clientRoom;
	}
	
	public class ClientHandler implements Runnable
	{
		private Socket socket;
		private boolean alive;
		
		private BufferedReader in;
		private PrintWriter out;
		
		private Room hostingRoom = null; // The room this client hosted
		
		/**
		 * Listens for network data from a client.
		 * Should be added to a ThreadPool
		 * 
		 * @param aMasterServer The masterserver that created this clienthandler
		 * @param aSocket socket to the client/user
		 */
		public ClientHandler(Socket aSocket)
		{
			socket = aSocket;
			alive = true;
			try
			{
				in = new BufferedReader(new InputStreamReader( socket.getInputStream()));
				out = new PrintWriter(new OutputStreamWriter( socket.getOutputStream(), "ISO-8859-1"), true);
			} 
			catch (UnsupportedEncodingException e)
			{
				log(e.toString());
			} 
			catch (IOException e)
			{
				log(e.toString());
			}
			
		}
		
		/**
		 * Checks what to do with data received from the user
		 * @param aInData 
		 */
		private void readMessage(String aInData)
		{
			
			// Split message into command and a data array
			String[] commandAndDataSeparated = aInData.split("#");
			String command = commandAndDataSeparated[0];
			String[] data = null;
			if (commandAndDataSeparated.length > 1)
				data = commandAndDataSeparated[1].split(":");
			
			
			if (!isValidCommand(command))
			{
				log("Invalid Request:\t" + command + " from " + HelpMethods.socketAsString(socket));
			}
			
			if (command.equals(MasterServer.REGISTER_GAMEROOM))
			{
				String gameName = HelpMethods.limitString(data[0], 30);
				int targetPort = Integer.parseInt(data[1]);
				Room gameRoom = new Room(gameName , socket.getInetAddress().getHostAddress(), targetPort);
				hostingRoom = gameRoom;
				registerGameRoom(gameRoom);
			}
			else if (command.equals(MasterServer.RETRIEVE_GAMELIST))
			{
				sendGameRoomListToSocket();
			}
			else if (command.equals(MasterServer.UNREGISTER_GAMEROOM))
			{
				String gameName = data[0];
				int targetPort = Integer.parseInt(data[1]);
				Room gameRoom = new Room(gameName , socket.getInetAddress().getHostAddress(), targetPort);
				hostingRoom = null;
				unregisterGameRoom(gameRoom);
			}
		}
	
		/**
		 * Sends all registered game rooms to the given ip and port
		 * @param aToAddress the ip of the user
		 * @param aToPort the port of the user
		 */
		private void sendGameRoomListToSocket()
		{
			// Each room will get three slots in the array. One for each field
			int fields = Room.AMOUNT_OF_FIELDS;
			String[] dataToSend = new String[allGameRooms.size() * fields];
			int roomIndexer = 0;
			for (Room r : allGameRooms)
			{
				dataToSend[roomIndexer] 	= r.getGameRoomName();
				dataToSend[roomIndexer + 1]	= r.getHostAddress();
				dataToSend[roomIndexer + 2] = r.getHostPort()+"";
				roomIndexer += fields;
			}
			sendMessage(MasterServer.RETRIEVE_GAMELIST, dataToSend);
		}
		
		private void sendMessage(String aCommand, String[] aData)
		{
			//Validate arguments
			if (!MasterServer.isValidCommand(aCommand))
				throw new IllegalArgumentException("aCommand is an invalid command: " + aCommand);
			
			if (aData != null)
			{
				
				CharSequence illegalChar1 = "#", illegalChar2 = ":";
				for (String dataPart : aData)
				{
					dataPart = dataPart.replace(":", "");
					dataPart = dataPart.replace("#", "");
					
					if (dataPart.contains(illegalChar1) || dataPart.contains(illegalChar2))
					{
						throw new IllegalArgumentException("aData cannot contain: '" + illegalChar1 + "' or '" + illegalChar2+ "': " + dataPart);
					}
				}
			}
			// Build string
			StringBuilder msg = new StringBuilder();
			msg.append(aCommand);
			msg.append("#"); // Separator between Command and the Data
			
			if (aData != null)
			{
				for (String dataPart : aData)
				{
					msg.append(dataPart);
					msg.append(':'); //Separator between data
				}
			}
			out.println(msg); // Send data
		}
		
		@Override
		public void run()
		{
			
			try
			{
				String readed;
				while ((readed = in.readLine()) != null && alive)
				{
					readMessage(readed);
				}
				// If we get here, the client disconnected in a nice way
				in.close();
				out.close();
				out.close();
				
			}
			catch (IOException e)
			{
				// Do nothing
			}
			
			log("Client Disconnected:\t" + HelpMethods.socketAsString(socket));
			allClients.remove(this);
			if (hostingRoom != null)
				unregisterGameRoom(hostingRoom);
	
		}
		
		
		/**
		 * Kills the client
		 */
		public void kill()
		{
			alive = false;
		}
	}
}
