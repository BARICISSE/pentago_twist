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
	private static int pID;
	private static Piece pPiece;
	private static Piece auxPiece;
	private static TreeNode winnerNode;

	// Unary Operators for Coord
	private static UnaryOperator<PentagoCoord> getNextHorizontal = point -> new PentagoCoord(point.getX(),
			point.getY() + 1);
	private static UnaryOperator<PentagoCoord> getNextVertical = point -> new PentagoCoord(point.getX() + 1,
			point.getY());
	private static UnaryOperator<PentagoCoord> getNextDiagonalRight = point -> new PentagoCoord(point.getX() + 1,
			point.getY() + 1);
	private static UnaryOperator<PentagoCoord> getNextDiagonalLeft = point -> new PentagoCoord(point.getX() + 1,
			point.getY() - 1);

	private static final Random random = new Random();

	private static final int MONTE_CARLO_START_NODE = 10;

	// TreeNode
	public static class TreeNode {
		private TreeNode parent;
		private List<TreeNode> children;
		private PentagoMove pentagoMove;
		private PentagoBoardState pentagoBoardState;
		private int visited;
		private int win;
		private int score;

		public TreeNode(TreeNode parent, PentagoMove pentagoMove, PentagoBoardState pentagoBoardState) {
			super();
			this.pentagoBoardState = pentagoBoardState;
			this.pentagoMove = pentagoMove;
			this.parent = parent;
			this.children = new ArrayList<TreeNode>();
		}
		
		public TreeNode pickARandomChild() {
			int size = children.size();
			return children.get((int) Math.random()*size);
		}

		public TreeNode getParent() {
			return parent;
		}

		public List<TreeNode> getChildren() {
			return children;
		}

		public PentagoMove getPentagoMove() {
			return pentagoMove;
		}

		public PentagoBoardState getPentagoBoardState() {
			return pentagoBoardState;
		}

		public int getScore() {
			return score;
		}

		public int getVisisted() {
			return this.visited;
		}

		public int getWin() {
			return win;
		}

		public void setScore(int score) {
			score = score;
		}

		public void setWin(int win) {
			win = win;
		}

		public void setVisited(int visited) {
			visited = visited;
		}

	}

	// Initialisation
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
	
	public static Move chooseBestMove(PentagoBoardState pentagoBoardState) {
		winnerNode = null;
		
		// initialize root
		TreeNode rootNode = new TreeNode(null, null, pentagoBoardState); 
		
		//expand
		expandTreeNode(rootNode);
		
		//playout
		getRidOfBadMoves(rootNode.getChildren());
		
		// if there is any good moves found while getting rid of bad moves
		// --> then return it
		
		if(winnerNode != null) {
			return winnerNode.pentagoMove;
		}
		
		//Time the simulation
		
		PentagoTwistTimer pentagoTimer = new PentagoTwistTimer(1500);
		
		if(pentagoBoardState.getTurnNumber() <= MONTE_CARLO_START_NODE) {
			while(!pentagoTimer.timeout) {
				TreeNode treeNode = findPromisingTreeNodeUsingUTC(rootNode);
				performRandomPlayoutFromTreeNode(treeNode, pentagoBoardState.getTurnNumber());
			}
		} else {
			while(!pentagoTimer.timeout) {
				TreeNode treeNode = findPromisingTreeNodeUsingUTC(rootNode);
				if(!treeNode.getPentagoBoardState().gameOver()) {
					if(treeNode.getChildren().size() ==0) {
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
		TreeNode bestTreeNode = getBestTreeNode(rootNode);
		
		// if there is a winning node return it
		if(winnerNode != null) {
			bestTreeNode = winnerNode;
		}
		
		return bestTreeNode.getPentagoMove();
		
		
	}

	/*
	 * **********************
	 * **********************
	 *  UTILS WITHIN MY TOOLS
	 * **********************
	 * **********************
	 */
	
	private static TreeNode getBestTreeNode(TreeNode rootNode) {
		return Collections.max(rootNode.getChildren(), Comparator.comparing(node -> (double) node.win / node.visited));
	}
	// UCT
	private static double getUpperConfidenceTree(TreeNode treeNode) {
		
		return treeNode.visited == 0 ? Integer.MAX_VALUE : treeNode.win / treeNode.visited
				+ Math.sqrt(2 * Math.log(treeNode.getParent().visited) / treeNode.visited);
	}

	// decentWithUTC
	private static TreeNode findPromisingTreeNodeUsingUTC(TreeNode treeNode) {
		while (treeNode.getChildren().size() > 0) {
			treeNode = Collections.max(treeNode.getChildren(),
					Comparator.comparing(node -> getUpperConfidenceTree(node)));
		}

		return treeNode;
	}

	// expand Treenode

	private static void expandTreeNode(TreeNode treeNode) {
		
		for (PentagoMove pentagoMove : treeNode.getPentagoBoardState().getAllLegalMoves()) {
			PentagoBoardState boardStateClone = (PentagoBoardState) treeNode.getPentagoBoardState().clone();
			boardStateClone.processMove(pentagoMove);
			TreeNode child = new TreeNode(treeNode, pentagoMove, boardStateClone);
			treeNode.getChildren().add(child);
		}
	}

	// rollout
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

		// assign score based on winner

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

		// back propagate
		TreeNode newTreeNode = treeNode;
		while (newTreeNode != null) {
			newTreeNode.visited++;
			newTreeNode.win += payCheck;
			newTreeNode = newTreeNode.parent;
		}
	}
	
	// bad moves
	private static void getRidOfBadMoves(List<TreeNode> treeNodes) {
    	
    	for(int j = 0; j< treeNodes.size(); j++) {
    		boolean isPromising = false;
    		
    		
    		if(treeNodes.get(j).pentagoBoardState.getWinner() == pID || treeNodes.get(j).pentagoBoardState.getWinner() == Board.DRAW) {
    			winnerNode = treeNodes.get(j);
    			return; 
    		} 
    		else if(treeNodes.get(j).pentagoBoardState.getWinner() == 1 - pID) {
    			if(treeNodes.size() > 3) {
    				treeNodes.remove(treeNodes.get(j));
    				j--;
    				continue;
    			} else {
    				return;
    			}
    		}
    		// check if there is any promising pattern for the player
    		
        	if(checkPlayerPotentialToWin(treeNodes.get(j).pentagoBoardState, pPiece)) {
        		isPromising = true;
        	}
        	
        	// go to depth 2
        	List<PentagoMove> pentagoMoves = treeNodes.get(j).pentagoBoardState.getAllLegalMoves();
        	
        	for(PentagoMove pMove: pentagoMoves) {
        		PentagoBoardState clonedState = (PentagoBoardState) treeNodes.get(j).getPentagoBoardState().clone();
        		clonedState.processMove(pMove);
        		
        		//check there is a winner or promising pattern for the opponent
        		// and act accordingly
        		
        		if(clonedState.getWinner() == 1 - pID) {
        			if(treeNodes.size() > 1) {
        				treeNodes.remove(treeNodes.get(j));
        			} else {
        				return;
        			}
        			j--;
        			isPromising = false;
        			break;
        		} else if(checkPlayerPotentialToWin(clonedState, auxPiece)) {
        			if(!isPromising) {
        				if(treeNodes.size() > 3) {
        					treeNodes.remove(treeNodes.get(j));
        				} else {
        					return;
        				}
        				j--;
        				break;
        			}
        		}
        	}
        	
        	// if node is promising make it as winning node
        	if(isPromising) {
        		winnerNode = treeNodes.get(j);
        	}
    	}
    }
	
	private static boolean checkPlayerPotentialToWin(PentagoBoardState pentagoBoardState, Piece piece) {
		PentagoCoord topLeft = new PentagoCoord(0, 0);
		PentagoCoord bottomRight = new PentagoCoord(0, 5);
		
		if(hasFourInARow(topLeft, piece, getNextDiagonalRight, pentagoBoardState) || hasFourInARow(bottomRight, piece, getNextDiagonalLeft, pentagoBoardState )) {
			return true;
		}
		
		// check all verticals
		
		for (int i=0; i<5; i++) {
			if(hasFourInARow(new PentagoCoord(0, i), piece, getNextVertical, pentagoBoardState)) {
				return true;
			}
		}
		
		// check all horizontals
		
		for(int i=0; i<5; i++) {
			if(hasFourInARow(new PentagoCoord(i, 0), piece, getNextHorizontal, pentagoBoardState)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean hasFourInARow(PentagoCoord point, Piece piece, UnaryOperator<PentagoCoord> unaryOperator, PentagoBoardState pentagoBoardState) {
		for(int i=0; i<4; i++) {
			point = unaryOperator.apply(point);
			if(pentagoBoardState.getPieceAt(point) != piece) {
				return false;
			}
		}
		point  = unaryOperator.apply(point);
		
		return pentagoBoardState.getPieceAt(point) == Piece.EMPTY;
	}

}