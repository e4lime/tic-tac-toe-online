package TTTO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import TTTO.Game.GameClient;
import TTTO.Game.GameServer;
import TTTO.MasterServer.HelpMethods;
import TTTO.MasterServer.MasterServerClient;
import TTTO.MasterServer.Room;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.Random;

/**
 * The client side gui for server selection.
 * 
 * This class is a bit messy i think...
 * 
 * @author emi-lind
 *
 */
public class MasterServerClientDisplay extends JPanel implements ActionListener
{
	
	
	private JButton 	changeMasterServer,
						refreshGameList,
						joinGame,
						hostGame;
	
	private JList roomList; // Latest update of all game rooms
	private JLabel masterServerStatus; // Displays master server info
	
	private MasterServerClient msClient;
	
	public MasterServerClientDisplay()
	{
		msClient = new MasterServerClient();
		
		createGUI();
		enableRequiredConnectionComponents(false); // We don't have a connection yet
		
	
		// Init in own thread so GUI won't freeze
		new Thread(new Runnable() 
		{
			public void run()
			{	
				initConnectionToMasterServer(MasterServerClient.DEFAULT_HOST, MasterServerClient.DEFAULT_PORT, "default Master Server");
				if (msClient.isConnected())
					refreshGameRoomList();
			}
		}).start();
	}
	/**
	 * Creates GUI
	 */
	private void createGUI()
	{
		setVisible(true);
		setLayout(new BorderLayout());
		masterServerStatus = new JLabel();

 		roomList = new JList();
 		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		// Lock Join button if no room is selected
		roomList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent lse)
			{
				boolean enableJoinButton= false;
				if (!roomList.isSelectionEmpty() && ((Room)(roomList.getSelectedValue())).getHostPort() > -1)
					enableJoinButton = true;
				joinGame.setEnabled(enableJoinButton);
				
			}
		});
		JScrollPane scroll = new JScrollPane(roomList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll, BorderLayout.CENTER);
		
		
		// South
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(1, 1)); // In case we want to add more buttons to south in the future
		joinGame = new JButton("Join Game");
		joinGame.addActionListener(this);
		joinGame.setEnabled(false); // No game selected by default so we disable join
		southPanel.add(joinGame);
		add(southPanel, BorderLayout.SOUTH);
		
		// North Panel
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(2, 1));
		northPanel.add(masterServerStatus);
		JPanel buttonRow = new JPanel();
		buttonRow.setLayout(new FlowLayout(FlowLayout.RIGHT));
		changeMasterServer = new JButton("Change Master Server");
		changeMasterServer.addActionListener(this);
		buttonRow.add(changeMasterServer);
		hostGame = new JButton("Host Game");
		hostGame.addActionListener(this);
		buttonRow.add(hostGame);
		refreshGameList = new JButton("Refresh List");
		refreshGameList.addActionListener(this);
		buttonRow.add(refreshGameList);
		northPanel.add(buttonRow);
		add(northPanel, BorderLayout.NORTH);	
	}
	
	/**
	 * Creates a connection to a master server
	 * @param aHost The ip
	 * @param aPort the port
	 * @param aDisplayDestination name of the destination to be displayed for the user
	 */
	private void initConnectionToMasterServer(final String aHost, final int aPort, final String aDisplayDestination)
	{
		changeMasterServer.setEnabled(false);
		
		
		masterServerStatus.setText("  Connecting to " + aDisplayDestination + ". Please wait...");
		if (msClient.connectToMasterServer(aHost, aPort))
		{
			enableRequiredConnectionComponents(true);
			masterServerStatus.setText("  " + msClient.getConnectionStatus());
		}
		else
		{
			masterServerStatus.setText("  " + msClient.getConnectionStatus());
			JOptionPane.showMessageDialog(MasterServerClientDisplay.this, "No connection to " + aDisplayDestination +
					"\nPlease specify a new Master Server in " + "\""+ changeMasterServer.getText() + "\"");
		}
		changeMasterServer.setEnabled(true);

	}
	
	/**
	 * Enables or disables components on top row that ONLY requires a connection to the master server in order to works
	 */
	private void enableRequiredConnectionComponents(boolean aEnable)
	{
		refreshGameList.setEnabled(aEnable);
		roomList.setEnabled(aEnable);
		hostGame.setEnabled(aEnable);
	}

	/**
	 * Checks if user is connected to the master server and shows an errormessage if there's no connection
	 * @return true if connected, false if not
	 */
	private boolean validateMasterServerConnection()
	{
		if (!msClient.isConnected())
		{
			JOptionPane.showMessageDialog(this, "Not connected to master server. Please reconnect",
					"No Connection", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent aev)
	{
		if (aev.getSource() instanceof JButton)
		{
			JButton button = ((JButton)aev.getSource());
			if(button == this.changeMasterServer)
			{
				showSetMasterServerForm();
			}
			else if (button == this.joinGame)
			{
				joinSelectedGame();
			}
			else if (button == this.hostGame)
			{
				hostGame();
			}
			else if (button == this.refreshGameList)
			{
				refreshGameRoomList();
			}
		}
	}
	
	/**
	 * Joins the selected game
	 */
	private void joinSelectedGame()
	{
		if (roomList.isSelectionEmpty())
		{
			JOptionPane.showMessageDialog(this, "Select a game to join before pressing \"Join Game\"",
					"No Game to Join", JOptionPane.ERROR_MESSAGE);
		}
		else if(validateMasterServerConnection())
		{
			Room r = (Room)roomList.getSelectedValue();
			if (r.getHostPort() < 0) // Invalid game room
				return;
			GameClient gc = new GameClient();
			if (gc.connectToTTTGameServer(r.getHostAddress(), r.getHostPort()))
			{
				// Load the game
				GameClientDisplay clientDisplay = new GameClientDisplay(gc);
				gc.addTTTGameListener(clientDisplay);
				TTTOnlineStartup.getInstance().setContentPane(clientDisplay);
				TTTOnlineStartup.getInstance().validate();
				TTTOnlineStartup.getInstance().repaint();
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Can't connect to game.\nPlease press refresh and select a new game to join",
						"Can't connect to game", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Registers a game at the master server
	 */
	private void hostGame()
	{
		HostGameForm hgf = new HostGameForm();
		boolean quit = false; // If true, quits fom.
		do
		{
			if (JOptionPane.showConfirmDialog(this, hgf, "Host Game", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)
			{
				final String gameNameInput = hgf.getGameNameField();
				// Check input
				if (gameNameInput.isEmpty())
				{
					JOptionPane.showMessageDialog(this, "Game Name can't be empty", "Invalid Game Name", JOptionPane.ERROR_MESSAGE);
				}
				else if (validateMasterServerConnection())
				{
					// Attempt create server socket and pass to the Game Server class
					Random generator = new Random();
					int onPort = 27780 + generator.nextInt(15);
					do 
					{
						try
						{
							final ServerSocket ss = new ServerSocket(onPort); 
							final GameServer gameServer = new GameServer(ss, msClient, gameNameInput);
							final GameClient gameClient = new GameClient();
							GameClientDisplay clientDisplay = new GameClientDisplay(gameClient);
							gameClient.addTTTGameListener(clientDisplay);
							TTTOnlineStartup.getInstance().setContentPane(clientDisplay);
							TTTOnlineStartup.getInstance().validate();
							TTTOnlineStartup.getInstance().repaint();
							new Thread(new Runnable()
							{
								@Override
								public void run()
								{				
									gameServer.startServer();
								}
							}).start();
							
							//TODO: Make sure TTTGameServer started
							msClient.registerGameRoom(gameNameInput, ss.getLocalPort());
							gameClient.connectToTTTGameServer(ss.getInetAddress().getHostAddress(), ss.getLocalPort());
							quit = true;
						}
						catch (IOException ioe)
						{
							// Port is taken! Ask user for a new port
							boolean error = false;
							do
							{
								error = false;
								String inputValue = JOptionPane.showInputDialog(this, "Port "+ onPort + " is taken! Please specify a new port",
																				"Taken Port", JOptionPane.OK_OPTION);
								if (inputValue == null) // User didn't press ok
									return;
								try
								{
									if (!inputValue.isEmpty() && HelpMethods.validatePort(inputValue))
									{
										onPort = Integer.parseInt(inputValue);
									}
									else
									{
										throw new NumberFormatException();
									}
								}
								catch (NumberFormatException efe)
								{
									error = true;
									JOptionPane.showMessageDialog(this, "Invalid port. Must be 0 - 65535 ", "Invalid Port", JOptionPane.ERROR_MESSAGE);
								}
							} while (error);
						} //Catch IO exception
					}while (!quit);
				}	// End else if validateMasterServerConnection
			} // If JoptionShow Host Server
			else
			{
				quit = true;
			}
		} while(!quit);
	}
	
	/**
	 * Retrieves the latest game room list from the master server and updates the JList
	 * with the new information
	 */
	private void refreshGameRoomList()
	{
		if (validateMasterServerConnection())
		{
			refreshGameList.setEnabled(false);
			new Thread(new Runnable() // So GUI wont freeze
			{
				public void run()
				{
					Room[] retrievedRooms;
					do
					{
						retrievedRooms = msClient.getGameRoomList();
					} while (retrievedRooms == null);
					roomList.setListData(retrievedRooms);
					try
					{
						Thread.sleep(1500); // So user won't spam refresh
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					refreshGameList.setEnabled(true);
				}
			}).start();
		}
	}
	
	/**
	 * Shows the master server form
	 */
	private void showSetMasterServerForm()
	{
		SetMasterServerForm msf = new SetMasterServerForm();
		
		boolean quit = false; // IF true, quits form.
		do
		{
			if (JOptionPane.showConfirmDialog(this, msf, "Set Master Server", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)
			{
				String tmpGivenIP = msf.getIPField().trim();
				String tmpGivenPort = msf.getPortField().trim();
				
				// Test user input data
				//  Is data given?
				if (tmpGivenIP.isEmpty() || tmpGivenPort.isEmpty())
				{
					JOptionPane.showMessageDialog(this, "IP and Port can't be empty", "Empty Fields Error", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					boolean invalidFormat = false;
					// Test IP
					String validatedIP = "";
					if(HelpMethods.validateIPv4(tmpGivenIP))
					{
						validatedIP = tmpGivenIP;
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Invalid IP. Must be an IPv4 address. ", "Invalid IP", JOptionPane.ERROR_MESSAGE);
						invalidFormat = true;
					}
					
					
					// Test port
					int validatedPort = -1;
					if (HelpMethods.validatePort(tmpGivenPort))
					{
						validatedPort = Integer.parseInt(tmpGivenPort);
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Invalid Port. Must be a digit ranged 0 - 65535", "Invalid Port", JOptionPane.ERROR_MESSAGE);
						invalidFormat = true;
					}
					
					// Make connection
					if (!invalidFormat)
					{	
						initConnectionToMasterServer(validatedIP, validatedPort, validatedIP + " on port " + validatedPort);
						quit = true;
					}
					
				}
				
			}
			else
			{ // User pressed cancel
				quit = true;
			}
		} while (!quit);
	}
	
	
	/**
	 * form used with JOptionPane
	 */
	private class HostGameForm extends JPanel
	{
		private JTextField gameNameField;
		
		public HostGameForm()
		{
			add(new JLabel("Game Name:"));
			gameNameField = new JTextField(8);
			add(gameNameField);
		}
		
		/**
		 * Returns what is written in the Game Name Field.
		 * The user input is trimmed
		 * @return The input in the field, trimmed.
		 */
		public String getGameNameField()
		{
			return gameNameField.getText().trim();
		}
	}
	
	/**
	 * Form used with JOptionPane
	 */
	private class SetMasterServerForm extends JPanel
	{
		private JTextField addressField;
		private JTextField portField;
		
		/**
		 * Creates a form that asks for IP and PORT
		 */
		public SetMasterServerForm()
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel row1 = new JPanel();
			row1.add(new JLabel("IP:"));
			addressField =(JTextField) row1.add(new JTextField(9));
			add(row1);
			
			JPanel row2 = new JPanel();
			row2.add(new JLabel("PORT:"));
			portField = (JTextField) row2.add(new JTextField(4));
			add(row2);
		}
		
		/**
		 * Returns what is written in the IP/Host field.
		 * The user input is trimmed
		 * @return The input in the field, trimmed
		 */
		public String getIPField()
		{
			return addressField.getText().trim();
		}
		
		/**
		 * Returns what is written in the Port field.
		 * The user input is trimmed
		 * @return The input in the field, trimmed
		 */
		public String getPortField()
		{
			return portField.getText().trim();
		}
		
	}
}
