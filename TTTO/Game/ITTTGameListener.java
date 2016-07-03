package TTTO.Game;

public interface ITTTGameListener
{
	/**
	 * When two players connect.
	 */
	public void gameConnected();
	
	/**
	 * When a new game starts. 
	 * @param aStartingPlayer the player who makes the first move
	 */
	public void newGame();
	
	/**
	 * When someone makes a move
	 * @param aPlayer The player who moves (PLAYER_ONE or PLAYER_TWO)
	 * @param aPostion Where he moves
	 */
	public void playerMove(String aPlayer, int aPosition);
	
	/**
	 * When you lose
	 */
	public void youLose();
	
	/**
	 * When you win
	 */
	public void youWin();
	
	/**
	 * When a draw occurs
	 */
	public void youDraw();
}
