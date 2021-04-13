package student_player;

public class MyTools {
    // Winning nodes
    private static int pID;
    private static Piece pPiece;
    private static Piece auxPiece;
    private static TreeNode winnerNode;

    // Unary Operators for Coord
    private static UnaryOperator<PentagoCoord> getNextHorizontal = point -> new PentagoCoord(point.getX(), point.getY() + 1);
    private static UnaryOperator<PentagoCoord> getNextVertical = point -> new PentagoCoord(point.getX() + 1, point.getY());
    private static UnaryOperator<PentagoCoord> getNextDiagonalRight = point -> new PentagoCoord(point.getX() + 1, point.getY() + 1);
    private static UnaryOperator<PentagoCoord> getNextDiagonalLeft = point -> new PentagoCoord(point.getX() + 1, point.getY() -1);

    private static final Random r = new Random();

    private static final int MONTE_CARLO_START_NODE = 10;

    // TreeNode
    public static class TreeNode {
        private TreeNode parent;
        private List<TreeNode> children;
        private PentagoMove pentagoMove;
        private PentagoBoardState pentagoBoardState;
        private int visisted;
        private int win;
        private int score

        public TreeNode(TreeNode parent, PentagoMove pentagoMove, PentagoBoardState pentagoBoardState) {
            super();
            this.pentagoBoardState = pentagoBoardState;
            this.pentagoMove = pentagoMove;
            this.parent = parent;
            this.children = new ArrayList<TreeNode>();
        }
        public TreeNode getParent() {
            return this.parent;
        }
        public List<TreeNode> getChildren() {
            return this.children;
        }
        public PentagoMove getPentagoMove() {
            return this.pentagoMove;
        }
        public PentagoBoardState getPentagoBoardState() {
            return this.getPentagoBoardState();
        }
        public int getScore() {
            return this.score;
        }
        public int getVisisted() {
            return this.visited;
        }
        public int getWin() {
            return this.win;
        }
        public void setScore(int score) {
            this.score = score;
        }
        public void setWin(int win) {
            this.win = win;
        }
        public void setVisited(int visited) {
            this.visited = visited;
        }

    }
    // Initialisation
    public static void gameConfiguration(PentagoBoardState pentagoBoardState, int pID) {
        MyTools.pID  = pID;
        if(pentagoBoardState.firstPlayer() == pID) {
                MyTools.pPiece = Piece.WHITE;
                MyTools.auxPiece = Piece.BLACK;
        } else {
            MyTools.pPiece = Piece.BLACK;
            MyTools.auxPiece = Piece.WHITE;
        }
    }

    /* *********************
     * *********************
     * UTILS WITHIN MY TOOLS
     * *********************
     * *********************
     */

    // UCT
    private static double getUpperConfidenceTree(TreeNode treeNode) {
        double utcValue = treeNode.getWin() / treeNode.getVisisted() + Math.sqrt(2*Math.log(treeNode.getParent().getVisisted()) / treeNode.getVisisted());
        return treeNode.getVisisted() == 0 ? Integer.MAX_VALUE : utcValue;
    }

    // decentWithUTC
    private static findPromisingTreeNodeUsingUTC(TreeNode treeNode) {
        while(treeNodeHasChildren(treeNode)) {
            treeNode = Collections.max(treeNode.getChildren(), Comparator.comparing(node -> getUpperConfidenceTree(node)));
        }

        return treeNode;
    }

    //expand Treenode

    private static void expandTreeNode(TreeNode treeNode) {
        for(PentagoMove pentagoMove : treeNode.getPentagoBoardState().getAllLegalMoves()) {
            PentagoBoardState boardStateClone = (PentagoBoardState) treeNode.getPentagoBoardState().clone();
            boardStateClone.processMove(pentagoMove);
            TreeNode child = new TreeNode(treeNode, move, boardStateClone);
            treeNode.getChildren().add(child);
        }
    }

    // rollout
    private static performRandomPlayoutFromTreeNode(TreeNode treeNode, int turnNumber){
        PentagoBoardState boardStateClone = (PentagoBoardState) treeNode.getPentagoBoardState().clone();

        int count = 0;
        while(boardStateClone.getWinner() == Board.NOBODY) {
            Move move;
            if(turnNumber <= MONTE_CARLO_START_NODE) {
                move = boardStateClone.getAllLegalMovesgfggf()
            }
        }
    }

    private static boolean treeNodeHasChildren(TreeNode treeNode) {
        return treeNode.getChildren().size() > 0
    }


}