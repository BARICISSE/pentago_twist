package student_player;

import boardgame.Move;

import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

	/**
	 * You must modify this constructor to return your student number. This is
	 * important, because this is what the code that runs the competition uses to
	 * associate you with your agent. The constructor should do nothing else.
	 */
	public StudentPlayer() {
		super("260683211");
	}

	/**
	 * This is the primary method that you need to implement. The ``boardState``
	 * object contains the current state of the game, which your agent must use to
	 * make decisions.
	 */
	public Move chooseMove(PentagoBoardState boardState) {
		// You probably will make separate functions in MyTools.
		// For example, maybe you'll need to load some pre-processed best opening
		// strategies...
//        MyTools.getSomething();

		// Is random the best you can do?
//        Move myMove = boardState.getRandomMove();

		// initialization
		if (boardState.getTurnNumber() == 0) {
			MyTools.gameConfiguration(boardState, this.player_id);
		}

		if (boardState.getTurnNumber() <= 1) {
			Move myMove = boardState.getRandomMove();
			if (boardState.isPlaceLegal(new PentagoCoord(1, 1))) {
				return new PentagoMove(1, 1, 0, 1, this.player_id);
			} else if (boardState.isPlaceLegal(new PentagoCoord(4, 1))) {
				return new PentagoMove(4, 1, 0, 1, this.player_id);
			} else if (boardState.isPlaceLegal(new PentagoCoord(1, 4))) {
				return new PentagoMove(1, 4, 0, 1, this.player_id);
			} else if (boardState.isPlaceLegal(new PentagoCoord(4, 4))) {
				return new PentagoMove(4, 4, 0, 1, this.player_id);
			}

		}

		Move myMove = MyTools.chooseBestMove(boardState);

		// Return your move to be processed by the server.
		return myMove;
	}
}