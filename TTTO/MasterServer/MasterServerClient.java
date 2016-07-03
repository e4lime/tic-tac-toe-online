package TTTO.MasterServer;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The client used to communicate with the Master server
 * @author emi-lind
 *
 */
public class MasterServerClient
{
	public static final String 	DEFAULT_HOST = "130.237.177.85"; // Default Host (Can be Localhost)
	public static final int 	DEFAULT_PORT = 27777;
	
	private Socket connection;
	
	private BufferedReader 	in;
	private PrintWriter 	out;
	private ServerReader 	serverReader;
	
	private Room[] lastUpdatedRoomList = null;
	
	/**
	 * Asks master server for the latest game room list. 
	 * The method locks until the game list is received.
	 * @return latest updated game list. 
	 */
	public Room[] getGameRoomList()
	{
		lastUpdatedRoomList = null;
		sendMessage(MasterServer.RETRIEVE_GAMELIST, null);
		return lastUpdatedRoomList;
	}
	
	/**
	 * Registers a game to the master server so other users can see it.
	 * Make sure client is connected by calling isConnected() before invoking
	 * this method.
	 * @param aGameName Desired game name
	 * @param aGameName on which port the game server is located
	 */
	public void registerGameRoom(String aGameName,int aPort)
	{
		sendMessage(MasterServer.REGISTER_GAMEROOM, new String[]{aGameName, aPort+""});
	}
	
	/**
	 * Unregisters a game from from the master server
	 * @param aGameName The rooms name
	 * @param aGameName the rooms port
	 */
	public void unregisterGameRoom(String aGameName, int aPort)
	{
		sendMessage(MasterServer.UNREGISTER_GAMEROOM, new String[]{aGameName, aPort+""});
	}
	
	/**
	 * Sends a message to the master server with array of data. Data can be null in 
	 * case you just want to invoke a command.
	 * Valid commands are those found in MasterServer.
	 * '#' and ':' chars will get removed from the data
	 * @param aCommand The command
	 * @param aData The data to be sent. Can be null
	 */
	private void sendMessage(String aCommand, String[] aData)
	{
		//Validate arguments
		if (!MasterServer.isValidCommand(aCommand))
			throw new IllegalArgumentException("aCommand is an invalid command: " + aCommand);
		
		if (aData != null)
		{
			
			CharSequence illegalChar0 = "#", illegalChar1 = ":";
			for (String dataPart : aData)
			{
				dataPart = dataPart.replace(":", "");
				dataPart = dataPart.replace("#", "");
				
				if (dataPart.contains(illegalChar0) || dataPart.contains(illegalChar1))
				{
					throw new IllegalArgumentException("aData cannot contain: '" + illegalChar0 + "' or '" + illegalChar1+ "': " + dataPart);
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
	
	public String getConnectionStatus()
	{
		String output;
		if (isConnected())
		{
			String msIp = connection.getInetAddress().getHostAddress();
			int msPort = connection.getPort();
			if (msIp.equals(DEFAULT_HOST) && msPort == DEFAULT_PORT)
			{
				output = "Connected to: default Master Server";
			}
			else
			{
				output = "Connected to Master Server: " + msIp + ":" + msPort;
			}
		}
		else output = "NO CONNECTION";
		
		return output;
	}
	
	/**
	 * Connects to a server.
	 * @param aHost target server
	 * @param aPort target port
	 * @return A socket if connection could be made. Else null
	 */
	public boolean connectToMasterServer(String aHost, int aPort)
	{
		if (isConnected())
		{
			serverReader.kill();		
		}
		Socket toHost = null;
		BufferedReader toIn = null;
		PrintWriter toOut = null;
		ServerReader toSrvReader = null;
		try
		{
			toHost = new Socket(aHost, aPort);
			toIn = new BufferedReader(new InputStreamReader( toHost.getInputStream()));
			toOut = new PrintWriter(new OutputStreamWriter( toHost.getOutputStream(), "ISO-8859-1"), true);
			toSrvReader = new ServerReader();
			connection = toHost;
			in = toIn;
			out = toOut;
			serverReader = toSrvReader;
		} 
		catch (UnknownHostException e)
		{
			System.err.println(e);
		} 
		catch (IOException e)
		{
			System.err.println(e);
		}
		
		
		return toHost != null;
	}
	
	/**
	 * Checks if connected
	 * @return true if connected
	 */
	public boolean isConnected()
	{
		
		boolean connected = false;
		if (connection != null)
			connected  = connection.isConnected();
		return connected;
	}
	
	/**
	 * Gets the master servers ip-address
	 * @return address as string
	 */
	public String getMasterServerIP()
	{
		if (connection == null)
			throw new IllegalStateException("No Connection");
		return connection.getInetAddress().toString();
	}
	
	/**
	 * Gets the master servers port
	 * @return the port
	 */
	public int getMasterServerPort()
	{
		if (connection == null)
			throw new IllegalStateException("No Connection");
		return connection.getPort();
	}
	
	/**
	 * Transform a string array of room fields into Room objects and
	 * fill the roomList with new rooms. If aRooms is null then the list will stay null
	 * @param aRooms The rooms to fill
	 */
	private void populateRoomList(String[] aRooms)
	{
		if (aRooms == null) // No rooms registered
		{	
			lastUpdatedRoomList = new Room[]{(new Room("NO ROOMS AVAILABLE"))};
			return;
		}
		
		LinkedList<Room> newRoomList = new LinkedList<Room>(); 
		for (int i = 0; i < aRooms.length; i += Room.AMOUNT_OF_FIELDS)
		{
			String roomName = aRooms[i];
			String roomAddress = aRooms[i+1];
			int roomPort = Integer.parseInt(aRooms[i+2]);
			Room r = new Room(roomName, roomAddress, roomPort);
			newRoomList.add(r);
		}
		Room[] newRooms = new Room[newRoomList.size()];
		lastUpdatedRoomList = newRoomList.toArray(newRooms);
	}
	
	private class ServerReader implements Runnable
	{
		boolean alive = true;
		public ServerReader()
		{
			Thread t = new Thread(this);
			t.start();
		}
		
		@Override
		public void run()
		{
			
			while (alive)
			{
				try
				{
					String readed = in.readLine();
					// Split message into command and a data array
					String[] commandAndDataSeparated = readed.split("#");
					String command = commandAndDataSeparated[0];
					String[] data = null;
					if (commandAndDataSeparated.length > 1)
						data = commandAndDataSeparated[1].split(":");
					if (MasterServer.isValidCommand(command))
					{
						if (command.equals(MasterServer.RETRIEVE_GAMELIST))
						{
							populateRoomList(data);
						}
					}
					
				}
				catch (IOException e)
				{
					kill();
				}
			}
		}
		
		public void kill()
		{
			alive = false;
			try
			{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (connection != null)
					connection.close();
				
				out = null;
				in = null;
				connection = null;
			}
			catch (IOException e)
			{
				System.err.println(e);
			}
		}
	}
}
