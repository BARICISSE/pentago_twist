package student_player;

import java.util.Collections;
import java.util.Comparator;

public class Utils {
	/**
	 * Method to compute the upper tree confidence using formula described in
	 * report.
	 * 
	 * @param treeNode
	 * @return
	 */
	public static double getUpperConfidenceTree(TreeNode treeNode) {

		return treeNode.seenNumber == 0 ? Integer.MAX_VALUE
				: treeNode.w / treeNode.seenNumber
						+ Math.sqrt(2 * Math.log(treeNode.getParent().seenNumber) / treeNode.seenNumber);
	}

	/**
	 * method to find promising TreeNode using Upper confidence value of the
	 * treenode
	 * 
	 * @param treeNode
	 * @return
	 */
	public static TreeNode findPromisingTreeNodeUsingUTC(TreeNode treeNode) {
		while (treeNode.getChildren().size() > 0) {
			treeNode = Collections.max(treeNode.getChildren(),
					Comparator.comparing(node -> getUpperConfidenceTree(node)));
		}

		return treeNode;
	}

	/**
	 * method to get the treenode that is most promising
	 * 
	 * @param rootNode
	 * @return
	 */
	public static TreeNode getBestTreeNode(TreeNode rootNode) {
		return Collections.max(rootNode.getChildren(), Comparator.comparing(node -> (double) node.w / node.seenNumber));
	}

}
