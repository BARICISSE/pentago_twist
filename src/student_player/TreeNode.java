package student_player;

import java.util.ArrayList;
import java.util.List;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

//TreeNode
public class TreeNode {
	TreeNode parent;
	List<TreeNode> children;
	PentagoMove pentagoMove;
	PentagoBoardState pentagoBoardState;
	int seenNumber;
	int w;
	int grade;

	public TreeNode(TreeNode parent, PentagoMove pentagoMove, PentagoBoardState pentagoBoardState) {
		super();
		this.pentagoBoardState = pentagoBoardState;
		this.pentagoMove = pentagoMove;
		this.parent = parent;
		this.children = new ArrayList<TreeNode>();
	}

	public TreeNode pickARandomChild() {
		int size = children.size();
		return children.get((int) Math.random() * size);
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

	public int getGrade() {
		return grade;
	}

	public int getSeenNumber() {
		return seenNumber;
	}

	public int getW() {
		return w;
	}

}
