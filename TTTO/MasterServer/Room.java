package TTTO.MasterServer;

public class Room
{
	public static final int AMOUNT_OF_FIELDS = 3;
	
	private String gameRoomName;
	private String hostAddress;  // Address to user who created this room
	private int hostPort;
	
	/**
	 * Container class for rooms. 
	 * @param aGameRoomName
	 */
	public Room(String aGameRoomName)
	{
		this(aGameRoomName, null, -1);
	}
	
	/**
	 * Container class for rooms
	 * @param aGameRoomName Name of room
	 * @param aHostAddress Host who created the room
	 * @param aHostPort Hosts port
	 */
	public Room(String aGameRoomName, String aHostAddress, int aHostPort)
	{
		gameRoomName = aGameRoomName;
		hostAddress = aHostAddress;
		hostPort = aHostPort;
	}
	
	/**
	 * Get the Game Name
	 * @return The name
	 */
	public String getGameRoomName()
	{
		return gameRoomName;
	}
	
	/**
	 * Get the owner of this rooms ip-address
	 * Returns null if there is no owner
	 * @return the owner or null
	 */
	public String getHostAddress()
	{
		return hostAddress;
	}
	
	/**
	 * Get the port.
	 * Returns -1 if there is no port
	 * @return port number or -1
	 */
	public int getHostPort()
	{
		return hostPort;
	}
	
	@Override
	public String toString()
	{
		String s = gameRoomName;
		if (hostPort >= 0 && hostAddress != null)
			s += " @ " + hostAddress + ":" + hostPort;
		return s;
	}
	
	/**
	 * Equals when two rooms have the same IP, Port and game name.
	 */
	@Override
	public boolean equals(Object aOther)
	{
		if (aOther instanceof Room)
		{
			Room otherRoom = (Room)aOther;
			if (otherRoom.gameRoomName.equalsIgnoreCase(this.gameRoomName) &&
				otherRoom.hostAddress.equals(this.hostAddress) &&
				otherRoom.hostPort == this.hostPort)
			{
				return true;
			}
		}
		return false;
	}
}
