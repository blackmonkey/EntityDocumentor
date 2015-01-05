package com.darkstudio.entitydoc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.darkstudio.entitydoc.data.ESItem;
import com.darkstudio.entitydoc.data.ESItemManager;

public class ESItemTree extends JTree implements ActionListener, MouseListener {

    private static final String CMD_ADD_ITEM = "ADD_ITEM";
    private static final String CMD_DEL_ITEM = "DEL_ITEM";

    private JPopupMenu menuPopup;
    private JMenuItem miDel;

    private DefaultMutableTreeNode contextNode;
    private ESItemManager varMgr;

    public ESItemTree(ESItemManager mgr) {
        varMgr = mgr;
        buildPopupMenu();
        addMouseListener(this);
    }

    private void buildPopupMenu() {
        menuPopup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem(Messages.get("VariableTree.MenuItemAdd"));
        miAdd.setActionCommand(CMD_ADD_ITEM);
        miAdd.addActionListener(this);
        menuPopup.add(miAdd);

        miDel = new JMenuItem(Messages.get("VariableTree.MenuItemDel"));
        miDel.setActionCommand(CMD_DEL_ITEM);
        miDel.addActionListener(this);
        menuPopup.add(miDel);
    }

    /**
     * Update tree view according to specified workspace.
     *
     * @param workspace folder of workspace.
     */
    public void update(File workspace) {
        varMgr.setWorkspace(workspace);
        varMgr.bindTree(this);
    }

    /**
     * Update tree view and expand the specified node.
     *
     * @param node the node the expand.
     * @param selectNode the node to select. {@code null} to keep current selection.
     */
    private void update(DefaultMutableTreeNode node, DefaultMutableTreeNode selectNode) {
        TreePath path = (selectNode == null ? getSelectionPath() : new TreePath(selectNode.getPath()));
        ((DefaultTreeModel) getModel()).reload();
        expandPath(new TreePath(node.getPath()));
        setSelectionPath(path);
    }

    public void showEditUI(ESItem item) {
        DefaultMutableTreeNode node = varMgr.getNodeByItem(item);
        if (node == null) {
            return;
        }
        setSelectionPath(new TreePath(node.getPath()));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (CMD_ADD_ITEM.equals(cmd)) {
            DefaultMutableTreeNode node = varMgr.add(contextNode);
            update(contextNode, node);
            requestFocus();
        } else if (CMD_DEL_ITEM.equals(cmd)) {
            int opt = JOptionPane.showConfirmDialog(getRootPane(),
                                                    String.format(Messages.get("DelDialog.Msg"),
                                                                  contextNode),
                                                    Messages.get("DelDialog.Title"),
                                                    JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) contextNode.getParent();
                varMgr.remove(contextNode);
                update(parent, null);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (!SwingUtilities.isRightMouseButton(evt)) {
            return;
        }

        Component comp = evt.getComponent();
        if (comp == null || !(comp instanceof ESItemTree)) {
            return;
        }

        int x = evt.getX();
        int y = evt.getY();
        ESItemTree tree = (ESItemTree) comp;
        TreePath path = tree.getPathForLocation(x, y);
        if (path == null) {
            return;
        }

        contextNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        miDel.setVisible(!contextNode.isRoot());
        menuPopup.show(tree, x, y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
