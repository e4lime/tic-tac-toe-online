package TTTO;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import TTTO.Game.*;


/**
 * The Graphics of the TicTacToe game
 */
public class GameClientDisplay extends JPanel implements ActionListener, ITTTGameListener
{
	
	private ArrayList<JButton> allSquares = new ArrayList<JButton>(); // The squares in Tic Tac Toe
	
	private JLabel gameStatus;
	
	private GameClient gClient;
	/**
	 * Creates a client that controls the Gui of the game
	 */
	public GameClientDisplay(GameClient aClient)
	{
		if (aClient == null)
			throw new NullPointerException();
		gClient = aClient;
		createGUI();
	}
	
	
	
	/**
	 * Creates GUI
	 */
	private void createGUI()
	{
		setLayout(new BorderLayout());
	
		
		// Top
		JPanel northPanel = new JPanel();
		gameStatus = (JLabel)northPanel.add(new JLabel("Waiting for players..."));
		add(northPanel, BorderLayout.NORTH);
		
		// Center
		JPanel gameBoard = new JPanel();
		gameBoard.setLayout(new GridLayout(3,3));
		for (int i = 0; i < 9; ++i)
		{
			JButton button = new JButton();
			allSquares.add(button);
			button.addActionListener(this);
			gameBoard.add(button);
		}
		add(gameBoard, BorderLayout.CENTER);
		setSquaresEnabled(false);
		
	}
	
	/**
	 * Sets all squares (JButtons) enabled or disabled
	 * @param aEnable True to enable, false to disable
	 */
	public void setSquaresEnabled(boolean aEnable)
	{
		for (JButton square : allSquares)
			square.setEnabled(aEnable);
	}
	
	/**
	 * Enable squares that aren't taken
	 */
	public void enableFreeSquares()
	{
		for (JButton square : allSquares)
		{
			if (square.getText().isEmpty())
			{
				square.setEnabled(true);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent aev)
	{
		if (aev.getSource() instanceof JButton)
		{
			JButton pressedSquare = (JButton)aev.getSource();
			int pressedPosition = allSquares.indexOf(pressedSquare);
			gClient.sendMove(pressedPosition);
			setSquaresEnabled(false);
		}
	}

	@Override
	public void youLose()
	{
		setSquaresEnabled(false);
		JOptionPane.showMessageDialog(this, "You Lose... :(");
	
	}



	@Override
	public void youWin()
	{
		setSquaresEnabled(false);
		JOptionPane.showMessageDialog(this, "You Win! :D");
		
	}


	@Override
	public void youDraw()
	{
		setSquaresEnabled(false);
		JOptionPane.showMessageDialog(this, "Draw");
		
	}



	@Override
	public void playerMove(String aPlayer, int aPosition)
	{
		JButton square = allSquares.get(aPosition);
		square.setText(playerIDAsSymbol(aPlayer));
		if (gClient.isMyTurn())
		{
			gameStatus.setText("Your Turn!");
			enableFreeSquares();
		}
		else
		{
			gameStatus.setText("The enemy is making his move...");
			setSquaresEnabled(false);
		}
	}



	@Override
	public void gameConnected()
	{
		gameStatus.setText("Connected with player. You are "+
				playerIDAsString(gClient.getPlayerID()) + ". Please wait...");
		
	}



	@Override
	public void newGame()
	{
		if(gClient.isMyTurn())
			setSquaresEnabled(true);
		//TODO: Reset everything
		for (JButton square : allSquares)
			square.setText("");
		
		gameStatus.setText("Game Started. " + (gClient.isMyTurn()?"Make your move!":"Your Opponent is thinking..."));
	}

	/**
	 * Converts a playerID to an easier format
	 * @param aPlayerID the ID
	 * @return "Player One" or "Player Two"
	 */
	private String playerIDAsString(String aPlayerID)
	{
		if (!GameServer.isValidPlayerID(aPlayerID))
			throw new IllegalArgumentException("Invalid PlayerID: " + aPlayerID);
		else 
			return (aPlayerID.equals(GameServer.PLAYER_ONE)?"Player One":"Player Two");
	}
	
	/**
	 * Converts a player id to symbol X or O
	 * @param aPlayerID the player id
	 * @return X if player one, O if player two
	 */
	private String playerIDAsSymbol(String aPlayerID)
	{
		if (!GameServer.isValidPlayerID(aPlayerID))
			throw new IllegalArgumentException("Invalid PlayerID: " + aPlayerID);
		else
			return (aPlayerID.equals(GameServer.PLAYER_ONE)?"X":"O");
	}



	
}
