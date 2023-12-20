package structure;

import library.BinaryTreeNode;

public class BinaryTree {
    BinaryTreeNode root;

    public BinaryTree(BinaryTreeNode root) {
        this.root = root;
    }

    private String stringForm(BinaryTreeNode node, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(4 * i)).append("|---").append(node).append('\n');
        if (node.getLeft() != null) {
            sb.append(stringForm(node.getLeft(), i+1));
        }
        if (node.getRight() != null) {
            sb.append(stringForm(node.getRight(), i + 1));
        }
        return sb.toString();
    }

    public BinaryTreeNode getRoot() {
        return root;
    }

    @Override
    public String toString() {;
        return stringForm(root,0);
    }
}
