package com.darkstudio.entitydoc.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.darkstudio.entitydoc.Messages;
import com.darkstudio.entitydoc.utils.TextUtils;

public abstract class ESItemManager {

    private static final String EMPTY_LINE = "";

    private Path storage;
    private DefaultMutableTreeNode root;
    private int newCount = 0;

    /**
     * Create {@code TreeVariableManager} instance.
     *
     * @param name name of the manager instance, which is also shown as root node in tree view.
     */
    public ESItemManager(String name) {
        root = new DefaultMutableTreeNode(name);
    }

    /**
     * Get the filename storing {@link ESItem}s.
     *
     * @return storage filename.
     */
    abstract protected String getStorageFilename();

    /**
     * Create a new {@link ESItem} instance.
     *
     * @return {@link ESItem} instance
     */
    protected ESItem createNewVariable() {
        return new ESItem();
    }

    /**
     * Update workspace location and load items.
     *
     * @param folder the location of workspace.
     */
    public void setWorkspace(File folder) {
        if (folder == null) {
            root.removeAllChildren();
            return;
        }

        storage = Paths.get(folder.getAbsolutePath(), getStorageFilename());
        try {
            // create the file if it doesn't exist.
            Files.createFile(storage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            List<String> lines = Files.readAllLines(storage);
            parseStorage(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get changed internal items which are not saved yet.
     *
     * @return list of unsaved items.
     */
    public Vector<ESItem> getUnsavedChanges() {
        Vector<ESItem> vars = new Vector<ESItem>();
        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            Object obj = node.getUserObject();
            if (obj instanceof ESItem) {
                ESItem var = (ESItem) obj;
                if (var.isChanged()) {
                    vars.add(var);
                }
            }
        }
        return vars;
    }

    /**
     * Save internal items
     */
    public void saveWorkspace() {
        Vector<String> lines = new Vector<String>();
        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            Object obj = node.getUserObject();
            if (obj instanceof ESItem) {
                ESItem var = (ESItem) obj;
                lines.addAll(var.getRawData());
                lines.add(EMPTY_LINE);
            }
        }

        try {
            Files.write(storage, lines);
            en = root.depthFirstEnumeration();
            while (en.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
                Object obj = node.getUserObject();
                if (obj instanceof ESItem) {
                    ESItem var = (ESItem) obj;
                    var.markUnchanged();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseStorage(List<String> lines) {
        root.removeAllChildren();

        if (lines.size() == 0) {
            return;
        }

        if (!TextUtils.isEmpty(lines.get(lines.size() - 1))) {
            // Variable items are separated with an empty line.
            // Add an empty line if the last line is not empty, so that all items end with empty
            // line.
            lines.add(EMPTY_LINE);
        }

        LinkedHashMap<Integer, DefaultMutableTreeNode> vars = new LinkedHashMap<Integer, DefaultMutableTreeNode>();
        List<String> itemLines = new Vector<String>();
        for (String l : lines) {
            if (TextUtils.isEmpty(l)) {
                ESItem var = createNewVariable();
                var.parseRawData(itemLines);
                vars.put(var.getId(), new DefaultMutableTreeNode(var));
                onParsedItem(var);
                itemLines.clear();
            } else {
                itemLines.add(l);
            }
        }

        for (Entry<Integer, DefaultMutableTreeNode> entry : vars.entrySet()) {
            DefaultMutableTreeNode node = entry.getValue();
            ESItem var = (ESItem) node.getUserObject();
            if (var.hasParent()) {
                vars.get(var.getParentId()).add(node);
            } else {
                root.add(node);
            }
        }
    }

    /**
     * Notified after parsed an item.
     *
     * @param item the parsed {@link ESItem}.
     */
    protected void onParsedItem(ESItem item) {
    }

    /**
     * Update specified {@link JTree} with internal items.
     *
     * @param tree the {@link JTree} to update.
     */
    public void bindTree(JTree tree) {
        tree.setModel(new DefaultTreeModel(root));
    }

    /**
     * Remove specified {@link JTree} node.
     *
     * @param node the node to remove.
     */
    public void remove(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
        int parentId = ((ESItem) node.getUserObject()).getParentId();
        int pos = parentNode.getIndex(node);
        while (node.getChildCount() > 0) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(0);
            ESItem child = (ESItem) childNode.getUserObject();
            parentNode.insert(childNode, pos++);
            child.setParentId(parentId);
        }
        parentNode.remove(pos); // remove node itself.
    }

    /**
     * Add a new {@link JTree} node as child of the specified node.
     *
     * @param node the node to add a child.
     * @return the added new node.
     */
    public DefaultMutableTreeNode add(DefaultMutableTreeNode node) {
        ESItem var = createNewVariable();
        var.setChineseName(String.format(Messages.get("TVMgr.NewItem"), ++newCount ));

        Object obj = node.getUserObject();
        if (obj instanceof ESItem) {
            ESItem parent = (ESItem) obj;
            var.setParentId(parent.getId());
        }

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(var);
        node.add(newNode);
        return newNode;
    }

    /**
     * Get list of {@link ESItem}s specified by IDs.
     *
     * @param ids the IDs of the {@link ESItem}s.
     * @return the list of {@link ESItem}s.
     */
    public Vector<ESItem> getItems(Vector<Integer> ids) {
        Vector<ESItem> items = new Vector<ESItem>(ids.size());
        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            Object obj = node.getUserObject();
            if (obj instanceof ESItem) {
                ESItem var = (ESItem) obj;
                if (ids.contains(var.getId())) {
                    items.add(var);
                }
            }
        }
        return items;
    }

    /**
     * Get list of all {@link ESItem}s.
     *
     * @return the list of {@link ESItem}s.
     */
    public Vector<DefaultMutableTreeNode> getNodes() {
        Vector<DefaultMutableTreeNode> nodes = new Vector<DefaultMutableTreeNode>();
        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            Object obj = node.getUserObject();
            if (obj instanceof ESItem) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * Get the {@link ESItemTree} node with the specified user object.
     *
     * @param item the user object to search.
     * @return the found {@link ESItemTree} node, or null if not found.
     */
    public DefaultMutableTreeNode getNodeByItem(ESItem item) {
        Enumeration en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            Object obj = node.getUserObject();
            if (obj.equals(item)) {
                return node;
            }
            if (obj instanceof ESItem) {
                ESItem var = (ESItem) obj;
                if (var.getId() == item.getId()) {
                    return node;
                }
            }
        }
        return null;
    }
}
