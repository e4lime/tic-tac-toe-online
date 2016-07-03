package TTTO.MasterServer;

import java.net.Socket;

/**
 * Various common methods
 * @author emi-lind
 *
 */
public class HelpMethods
{
	private HelpMethods(){};

	
	/**
	 * Validates an dot-decimal notated IPv4 
	 * @param aIp The ip to validate
	 * @return true if it got validated
	 */
	public static boolean validateIPv4(String aIp)
	{
		
		String[] ipParts = aIp.split("\\.");
		
		if(ipParts.length != 4)
			return false;
		
		for (String part : ipParts)
		{
			try
			{
				int partAsInt = Integer.parseInt(part);
				if (partAsInt < 0 || partAsInt > 255)
					throw new NumberFormatException();
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Validates a port. Checks for digits and if the port is between 0 and 65535
	 * @param aPort the port to check
	 * @return true if 0 - 65535
	 */
	public static boolean validatePort(String aPort)
	{
		try
		{
			int portToValidate = Integer.parseInt(aPort);
			
			if (portToValidate < 0 || portToValidate > 65535)
			{
		
				return false;
			}
		}
		catch (NumberFormatException nfe)
		{
		
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Creates a string representation of a socket.
	 * Format:   ip:port
	 * @param aSocket The socket to get the representation from
	 * @return the representation
	 */
	public static String socketAsString(Socket aSocket)
	{
		return aSocket.getInetAddress().getHostAddress() + ":"  + aSocket.getPort();
	}
	
	/**
	 * Checks if a strings length is bigger then aSize. 
	 * If it is, the string will get shrinked. Else it won't do anything.
	 * @param aString The string to check
	 * @param aSize Max length you want on the string
	 * @return the new checked string. 
	 */
	public static String limitString(String aString, int aLimitSize)
	{
		String newString = aString;
		if (newString.length() > aLimitSize)
		{
			newString = newString.substring(0, aLimitSize);
		}
		
		return newString;
	}
}
