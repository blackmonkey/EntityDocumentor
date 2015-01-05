package com.darkstudio.entitydoc;

import javax.swing.JDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.darkstudio.entitydoc.data.Attribute;
import com.darkstudio.entitydoc.utils.TextUtils;

public class AttributeListDialog extends JDialog implements ActionListener {

    private static final String CMD_OK = "OK";
    private static final String CMD_CANCEL = "CANCEL";

    private static final Vector<Attribute> selectedAttrIds = new Vector<Attribute>();
    private static AttributeListDialog dialog;

    private Vector<Attribute> attrs;
    private JList<String> attrList;

    public static Vector<Attribute> showDialog(Component parent, Vector<DefaultMutableTreeNode> nodes) {
        dialog = new AttributeListDialog(parent, nodes);
        dialog.setVisible(true);
        return selectedAttrIds;
    }

    private AttributeListDialog(Component parent, Vector<DefaultMutableTreeNode> nodes) {
        super(JOptionPane.getFrameForComponent(parent));
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle(Messages.get("AttrDlg.Title"));
        setSize(600, 400);
        getContentPane().setLayout(new BorderLayout(0, 0));

        attrList = new JList<String>();

        JScrollPane scrollPane = new JScrollPane(attrList);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);

        JButton okBtn = new JButton(Messages.get("AttrDlg.Ok"));
        okBtn.setActionCommand(CMD_OK);
        okBtn.addActionListener(this);
        panel.add(okBtn);
        getRootPane().setDefaultButton(okBtn);

        JButton cancelBtn = new JButton(Messages.get("AttrDlg.Cancel"));
        cancelBtn.setActionCommand(CMD_CANCEL);
        cancelBtn.addActionListener(this);
        panel.add(cancelBtn);

        selectedAttrIds.clear();

        attrs = new Vector<Attribute>(nodes.size());
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (int i = 0; i < nodes.size(); i++) {
            DefaultMutableTreeNode node = nodes.get(i);

            attrs.add((Attribute) node.getUserObject());

            String path = TextUtils.escapeHtml(TextUtils.buildPath(node.getUserObjectPath()));
            model.addElement(String.format("<html><div style='background:%s;font:bold 10pt Verdana'>%s</div></html>",
                                           (i & 1) == 0 ? TextUtils.CELL_NORMAL_BG
                                                       : TextUtils.CELL_HIGHLIGHT_BG, path));
        }
        attrList.setModel(model);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (CMD_OK.equals(cmd)) {
            int[] idxes = attrList.getSelectedIndices();
            for (int i : idxes) {
                selectedAttrIds.add(attrs.get(i));
            }
            dialog.setVisible(false);
        } else if (CMD_CANCEL.equals(cmd)) {
            dialog.setVisible(false);
        }
    }
}
