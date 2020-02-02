package norn;

import java.util.ArrayList;
import java.util.List;

public class VisualizerTree {
    private final String data;
    private final List<VisualizerTree> children;
    
    // AF(data, children) = a tree to visualize the nested structure in data,
    //                          where children is a list of all of the
    //                          VisualizerTree children of this VisualizerTree node.
    //
    // RI:
    // - true
    //
    // SRE:
    // - fields are private and final
    // - data is immutable
    // - no reference to children is passed by any methods
    //
    // TSA:
    // - multiple threads will not be mutating the same VisualizerTree
    //      (threadsafety by confinement)

    /**
     * Public constructor for VisualizerTree.
     * @param data data to visualize
     */
    public VisualizerTree(String data) {
        this.data = data;
        children = new ArrayList<>();
    }

    /**
     * Add a child to the tree.
     * @param child child to add
     */
    public void addChild(VisualizerTree child) {
        children.add(child);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("<li>");
        sb.append("<p>").append(data).append("</p>");
        if (!children.isEmpty()) {
            sb.append("<ul>");
            for (VisualizerTree child : children) {
                sb.append(child.toString());
            }
            sb.append("</ul>");
        }
        sb.append("</li>");
        return sb.toString();
    }

    /**
     * Get the toString in HTML format.
     * @return returns the tree in HTML format
     */
    public String asHTML() {
        return String.format("<div class=\"ul-tree horizontal\"> <ul>%s</ul> </div>", toString());
    }
}
