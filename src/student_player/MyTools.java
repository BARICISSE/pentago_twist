package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

import boardgame.Board;
import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

public class MyTools {
	// Winning nodes
	private static int pID; // current player ID
	private static Piece pPiece; // current player piece
	private static Piece auxPiece; // Opponent player piece
	private static TreeNode winnerNode; // TreeNode for winner

	// Unary Operators for Coord -> inspired from PentagoTwistBoardState class
	private static UnaryOperator<PentagoCoord> getNextHorizontal = point -> new PentagoCoord(point.getX(),
			point.getY() + 1);
	private static UnaryOperator<PentagoCoord> getNextVertical = point -> new PentagoCoord(point.getX() + 1,
			point.getY());
	private static UnaryOperator<PentagoCoord> getNextDiagonalRight = point -> new PentagoCoord(point.getX() + 1,
			point.getY() + 1);
	private static UnaryOperator<PentagoCoord> getNextDiagonalLeft = point -> new PentagoCoord(point.getX() + 1,
			point.getY() - 1);

	// Constants
	private static final Random random = new Random();
	private static final int MONTE_CARLO_START_NODE = 10;
	private static final int TIME_ALLOWED = 1500;

	/**
	 * gameConfiguration helps us identify the AI player_id and whether he is white
	 * or black
	 * 
	 * @param pentagoBoardState
	 * @param pID
	 */
	public static void gameConfiguration(PentagoBoardState pentagoBoardState, int pID) {
		MyTools.pID = pID;
		if (pentagoBoardState.firstPlayer() == pID) {
			pPiece = Piece.WHITE;
			auxPiece = Piece.BLACK;
		} else {
			pPiece = Piece.BLACK;
			auxPiece = Piece.WHITE;
		}
	}

	/**
	 * 
	 * @param pentagoBoardState
	 * @return
	 */
	public static Move chooseBestMove(PentagoBoardState pentagoBoardState) {
		winnerNode = null;
		// initialize root
		TreeNode rootNode = new TreeNode(null, null, pentagoBoardState);
		expandTreeNode(rootNode);
		getRidOfBadMoves(rootNode.getChildren());

		// if there is any good moves found while getting rid of bad moves
		// --> then return it
		if (winnerNode != null) {
			return winnerNode.pentagoMove;
		}
		// Ensure that Agent does not take more than 2s
		PentagoTwistTimer pentagoTimer = new PentagoTwistTimer(TIME_ALLOWED);

		if (pentagoBoardState.getTurnNumber() <= MONTE_CARLO_START_NODE) {
			while (!pentagoTimer.timeout) {
				TreeNode treeNode = Utils.findPromisingTreeNodeUsingUTC(rootNode);
				performRandomPlayoutFromTreeNode(treeNode, pentagoBoardState.getTurnNumber());
			}
		} else {
			while (!pentagoTimer.timeout) {
				TreeNode treeNode = Utils.findPromisingTreeNodeUsingUTC(rootNode);
				if (!treeNode.getPentagoBoardState().gameOver()) {
					if (treeNode.getChildren().size() == 0) {
						expandTreeNode(treeNode);
					}
					TreeNode nextNode = treeNode.pickARandomChild();
					performRandomPlayoutFromTreeNode(nextNode, pID);

				} else {
					performRandomPlayoutFromTreeNode(treeNode, pID);
				}
			}
		}
		// get best move from children
		TreeNode bestTreeNode = Utils.getBestTreeNode(rootNode);

		// if there is a winning node return it
		if (winnerNode != null) {
			bestTreeNode = winnerNode;
		}
		return bestTreeNode.getPentagoMove();
	}

	/**
	 * void method to iterate through the list of legal moves and expands the root
	 * tree.
	 * 
	 * @param treeNode
	 */
	private static void expandTreeNode(TreeNode treeNode) {

		for (PentagoMove pentagoMove : treeNode.getPentagoBoardState().getAllLegalMoves()) {
			PentagoBoardState boardStateClone = (PentagoBoardState) treeNode.getPentagoBoardState().clone();
			boardStateClone.processMove(pentagoMove);
			TreeNode child = new TreeNode(treeNode, pentagoMove, boardStateClone);
			treeNode.getChildren().add(child);
		}
	}

	/**
	 * 
	 * @param treeNode
	 * @param turnNumber
	 */
	private static void performRandomPlayoutFromTreeNode(TreeNode treeNode, int turnNumber) {

		PentagoBoardState boardStateClone = (PentagoBoardState) treeNode.getPentagoBoardState().clone();

		int count = 0;
		while (boardStateClone.getWinner() == Board.NOBODY) {
			Move move;
			if (turnNumber <= MONTE_CARLO_START_NODE) {
				move = boardStateClone.getAllLegalMoves().get(0);
			} else {
				int numOfLegalMoves = boardStateClone.getAllLegalMoves().size();
				move = boardStateClone.getAllLegalMoves().get(random.nextInt(numOfLegalMoves));
			}
			boardStateClone.processMove((PentagoMove) move);
			count++;
		}

		int winner = boardStateClone.getWinner();
		int payCheck = -1;

		if (winner == pID) {
			payCheck = (32 - 2 * treeNode.getPentagoBoardState().getTurnNumber() - count) * 1000 + 5000;

		} else if (winner == Board.DRAW) {
			payCheck = 1000;
		} else {
			payCheck = -(32 - 2 * treeNode.getPentagoBoardState().getTurnNumber() - count) * 1000 - 5000;
			if (count <= 1 && treeNode.parent.children.size() >= 2) {
				treeNode.parent.children.remove(treeNode); // remove bad moves
				treeNode.parent = null;

			}
		}
		TreeNode newTreeNode = treeNode;
		while (newTreeNode != null) {
			newTreeNode.seenNumber++;
			newTreeNode.w += payCheck;
			newTreeNode = newTreeNode.parent;
		}
	}

	/**
	 * void method to remove bad moves from the list of potential moves
	 * 
	 * @param treeNodes
	 */
	private static void getRidOfBadMoves(List<TreeNode> treeNodes) {

		for (int j = 0; j < treeNodes.size(); j++) {
			boolean isPromising = false;

			if (treeNodes.get(j).pentagoBoardState.getWinner() == pID
					|| treeNodes.get(j).pentagoBoardState.getWinner() == Board.DRAW) {
				winnerNode = treeNodes.get(j);
				return;
			} else if (treeNodes.get(j).pentagoBoardState.getWinner() == 1 - pID) {
				if (treeNodes.size() > 3) {
					treeNodes.remove(treeNodes.get(j));
					j--;
					continue;
				} else {
					return;
				}
			}

			if (winningPotential(treeNodes.get(j).pentagoBoardState, pPiece)) {
				isPromising = true;
			}

			List<PentagoMove> pentagoMoves = treeNodes.get(j).pentagoBoardState.getAllLegalMoves();

			for (PentagoMove pMove : pentagoMoves) {
				PentagoBoardState clonedState = (PentagoBoardState) treeNodes.get(j).getPentagoBoardState().clone();
				clonedState.processMove(pMove);

				if (clonedState.getWinner() == 1 - pID) {
					if (treeNodes.size() > 1) {
						treeNodes.remove(treeNodes.get(j));
					} else {
						return;
					}
					j--;
					isPromising = false;
					break;
				} else if (winningPotential(clonedState, auxPiece)) {
					if (!isPromising) {
						if (treeNodes.size() > 3) {
							treeNodes.remove(treeNodes.get(j));
						} else {
							return;
						}
						j--;
						break;
					}
				}
			}

			if (isPromising) {
				winnerNode = treeNodes.get(j);
			}
		}
	}

	/**
	 * Method to verify winning potential of the player
	 * 
	 * @param pentagoBoardState
	 * @param piece
	 * @return
	 */
	private static boolean winningPotential(PentagoBoardState pentagoBoardState, Piece piece) {

		PentagoCoord topLeft = new PentagoCoord(0, 0);
		PentagoCoord bottomRight = new PentagoCoord(0, 5);

		// Form black or white player in a row in both diagonals
		if (hasFourInARow(topLeft, piece, getNextDiagonalRight, pentagoBoardState)
				|| hasFourInARow(bottomRight, piece, getNextDiagonalLeft, pentagoBoardState)) {
			return true;
		}

		// Form in a column of the same color
		for (int i = 0; i < 5; i++) {
			if (hasFourInARow(new PentagoCoord(0, i), piece, getNextVertical, pentagoBoardState)) {
				return true;
			}
		}

		// Form in a row of the same color
		for (int i = 0; i < 5; i++) {
			if (hasFourInARow(new PentagoCoord(i, 0), piece, getNextHorizontal, pentagoBoardState)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param point
	 * @param piece
	 * @param unaryOperator
	 * @param pentagoBoardState
	 * @return
	 */
	private static boolean hasFourInARow(PentagoCoord point, Piece piece, UnaryOperator<PentagoCoord> unaryOperator,
			PentagoBoardState pentagoBoardState) {
		for (int i = 0; i < 4; i++) {
			point = unaryOperator.apply(point);
			if (pentagoBoardState.getPieceAt(point) != piece) {
				return false;
			}
		}
		point = unaryOperator.apply(point);

		return pentagoBoardState.getPieceAt(point) == Piece.EMPTY;
	}

	/**
	 * REFERENCES: 1) https://web.stanford.edu/~surag/posts/alphazero.html
	 * 
	 * 2) https://en.wikipedia.org/wiki/Monte_Carlo_algorithm
	 * 
	 * 3) Silver, D., et al., Mastering the game of go without human knowledge.
	 * 2017. 550(7676): p. 354.
	 * 
	 * 4) Pentago Swap AI player by Hao Li https://hao-li.me/html/pentago.html
	 * 
	 */

}