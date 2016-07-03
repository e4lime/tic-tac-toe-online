package TTTO;
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

import TTTO.MasterServer.*;

/**
 * The GUI of the MasterServer
 *
 */
public class MasterServerDisplay extends JPanel implements ActionListener, IMasterServerLog
{
	private static final int DEFAULT_PORT = 27777;
	
	private JTextArea logArea; // Logs appears here
	private JScrollPane logScroll;
	private JTextArea info;    // Status of the server. Amount of connections, registered games etc
	private JTextField portField;
	private JButton startServer;
	private int port;
	
	private MasterServer masterServer; // Ref to MasterServer.
	
	public MasterServerDisplay()
	{
		// Fix log and info areas
		setLayout(new BorderLayout());
		JPanel p = new JPanel();
		logArea = new JTextArea();
		info = new JTextArea();
		p.setLayout(new GridLayout(2, 1));
		
		logScroll = new JScrollPane(logArea);
		
		
		Border blackline = BorderFactory.createLineBorder(Color.BLACK);
		Border compound = BorderFactory.createTitledBorder(blackline, "Log");
		logArea.setBorder(compound);
		compound = BorderFactory.createTitledBorder(blackline, "Info");
		info.setBorder(compound);
		p.add(logScroll);
		p.add(info);
		logArea.setEditable(false);
		info.setEditable(false);
		add(p);
		// Fix buttons
		p = new JPanel();
		startServer = new JButton("Start Server");
		startServer.addActionListener(this);
		portField = new JTextField(4);
		portField.setText(DEFAULT_PORT + "");
		p.add(startServer);
		p.add(new JLabel("on port: "));
		p.add(portField);
		add(p, BorderLayout.SOUTH);		
		validate();
	}
	
	/**
	 * Starts the server
	 */
	private void startServer()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{		
				masterServer = new MasterServer(port, 8, MasterServerDisplay.this);
				startServer.setText("Stop Server");
				try
				{
					masterServer.runServer();
				} 
				catch (UnknownHostException e)
				{
					
					e.printStackTrace();
					return;
				} 
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(MasterServerDisplay.this, "Port taken. Try a new port", "Invalid port", JOptionPane.ERROR_MESSAGE);
					startServer.setText("Start Server");	
					masterServer = null;
				}
				
			}
		}).start();
		
		
		
		// Update info area 
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(500);
						if (masterServer != null)
							info.setText(masterServer.getServerStats());
					} 
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof JButton)
		{
			JButton b = (JButton)e.getSource();
			if (b.getText().equals("Start Server"))
			{
				String portInField = portField.getText().trim();
				if (HelpMethods.validatePort(portInField))
				{
					port = Integer.parseInt(portInField);
					startServer();
					
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Invalid Port. Must be a digit ranged 0 - 65535", "Invalid Port", JOptionPane.ERROR_MESSAGE);
				}
				
			}
			else
			{
				System.exit(0);
				b.setText("Start Server");
			}
		}
	}

	@Override
	public void log(String toLog)
	{
		logArea.append(toLog);
		// Set scroll to bottom
		logScroll.getViewport().setViewPosition(new Point(logArea.getSize().width, logArea.getSize().height));
	}
	
}
