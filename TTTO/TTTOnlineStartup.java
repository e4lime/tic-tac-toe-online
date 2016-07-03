package TTTO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 * TicTacToeOnline
 * 
 * Startup class.
 * 
 * 
 * @author Emil Lindberg
 *
 */
public class TTTOnlineStartup extends JFrame
{
	private static TTTOnlineStartup instance;
	
	public static void main(String[] args)
	{
		new TTTOnlineStartup();
	}
	
	private TTTOnlineStartup()
	{
		super("Tic Tac Toe Online v1.0 - Startup Manager");
		setSize(640, 480);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(2,1));
		
		JButton playTTT = new JButton("Play TTT");
		JButton createMaster = new JButton("Create Master Server");
		playTTT.addActionListener(createActionListenerSetContentPane("TTTO.MasterServerClientDisplay"));
		createMaster.addActionListener(createActionListenerSetContentPane("TTTO.MasterServerDisplay"));
		add(playTTT);
		add(createMaster);
		
		instance = this;
		validate();
	}
	
	/**
	 * Gets a ref to the TTTOnlineStartup
	 * @return the TTTOnlineStartup
	 */
	public static TTTOnlineStartup getInstance()
	{
		return instance;
	}
	

	/**
	 * Creates an ActionListener which updates the content pane of TTTOnlineStartup with given class that is a JPanel.
	 * @param aClassName The name of the JPanel
	 * @return The ActionListener
	 * @throws IllegalArgumentException if class isn't a JPanel
	 */
	private ActionListener createActionListenerSetContentPane(final String aJPanelName) throws IllegalArgumentException
	{
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent aev)
			{
				try
				{
					Class cls = Class.forName(aJPanelName);
					Object o = cls.newInstance(); // o = new
					if (!(o instanceof JPanel))
						throw new IllegalArgumentException("The Class must be a JPanel! Class is: " + o.getClass());
					setTitle("Tic Tac Toe Online");
					setContentPane((JPanel)o);
					((JPanel)o).validate();
					validate();
				}
				catch (Exception e)
				{
					System.err.println(e);
					System.exit(1);
				}
			}
		};
		return al;
	}
	
}

