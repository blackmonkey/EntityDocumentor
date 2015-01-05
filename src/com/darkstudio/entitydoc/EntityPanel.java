package com.darkstudio.entitydoc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import com.darkstudio.entitydoc.data.Attribute;
import com.darkstudio.entitydoc.data.AttributeManager;
import com.darkstudio.entitydoc.data.ESItem;
import com.darkstudio.entitydoc.data.Entity;
import com.darkstudio.entitydoc.utils.TextUtils;
import com.hexidec.ekit.component.JButtonNoFocus;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.ListSelectionModel;

public class EntityPanel extends JPanel implements ActionListener, MouseListener {

    private static final String CMD_NEW_ATTR = "NEW_ATTR";
    private static final String CMD_EDIT_ATTR = "EDIT_ATTR";
    private static final String CMD_DEL_ATTR = "DEL_ATTR";

    private AttributeManager mgr;
    private Entity item;

    private NamePanel namePanel;
    private HtmlPanel descPanel;
    private JList<String> attrList = new JList<String>();
    private Vector<ESItem> attrs;
    private ESItemTree attrTree;

    public EntityPanel(AttributeManager mgr) {
        this.mgr = mgr;

        FormLayout layout = new FormLayout(new ColumnSpec[] {FormFactory.GROWING_BUTTON_COLSPEC,
                                                             FormFactory.BUTTON_COLSPEC,},
                                           new RowSpec[] {FormFactory.DEFAULT_ROWSPEC,
                                                          FormFactory.GLUE_ROWSPEC,
                                                          FormFactory.PREF_ROWSPEC,});
        setLayout(layout);

        namePanel = new NamePanel(Messages.get("EntyPane.NameGrp"));
        add(namePanel, "1, 1");

        JPanel attrsPanel = createAttrbutesListPanel();
        add(attrsPanel, "2, 1, 1, 2, default, fill");

        descPanel = createDescPanel();
        add(descPanel, "1, 2, default, fill");

        JPanel btnPanel = new SaveButtonsPanel(this);
        add(btnPanel, "2, 3");
    }

    private HtmlPanel createDescPanel() {
        // TODO: initialized with CSS
        return new HtmlPanel(Messages.get("EntyPane.DescGrp"));
    }

    private JPanel createAttrbutesListPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(Messages.get("EntyPane.AttrList")));
        panel.setLayout(new FormLayout(new ColumnSpec[] {ColumnSpec.decode("default:grow"),},
                                       new RowSpec[] {FormFactory.DEFAULT_ROWSPEC,
                                                      FormFactory.GLUE_ROWSPEC,}));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        panel.add(toolBar, "1, 1");

        JButtonNoFocus btnAdd = new JButtonNoFocus(getIcon("new"));
        btnAdd.setToolTipText(Messages.get("EntyPane.NewAttr"));
        btnAdd.setActionCommand(CMD_NEW_ATTR);
        btnAdd.addActionListener(this);
        toolBar.add(btnAdd);

        JButtonNoFocus btnEdit = new JButtonNoFocus(getIcon("edit"));
        btnEdit.setToolTipText(Messages.get("EntyPane.EditAttr"));
        btnEdit.setActionCommand(CMD_EDIT_ATTR);
        btnEdit.addActionListener(this);
        toolBar.add(btnEdit);

        JButtonNoFocus btnDel = new JButtonNoFocus(getIcon("delete"));
        btnDel.setToolTipText(Messages.get("EntyPane.DelAttr"));
        btnDel.setActionCommand(CMD_DEL_ATTR);
        btnDel.addActionListener(this);
        toolBar.add(btnDel);

        attrList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attrList.addMouseListener(this);
        JScrollPane scrollPane = new JScrollPane(attrList);
        panel.add(scrollPane, "1, 2, fill, fill");
        return panel;
    }

    private ImageIcon getIcon(String name) {
        return new ImageIcon(getClass().getResource("icons/" + name + ".png"));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (SaveButtonsPanel.CMD_SAVE.equals(cmd)) {
            save();
        } else if (SaveButtonsPanel.CMD_CANCEL.equals(cmd)) {
            bindEditorsWithItem(item);
        }

        int idx = attrList.getSelectedIndex();
        if (CMD_NEW_ATTR.equals(cmd)) {
            addAttribute(idx);
        }
        if (idx == -1) {
            if (CMD_EDIT_ATTR.equals(cmd) || CMD_DEL_ATTR.equals(cmd)) {
                JOptionPane.showMessageDialog(getRootPane(), Messages.get("AttrList.NoContext"));
            }
        } else {
            if (CMD_EDIT_ATTR.equals(cmd)) {
                editAttribute(idx);
            } else if (CMD_DEL_ATTR.equals(cmd)) {
                int opt = JOptionPane.showConfirmDialog(getRootPane(),
                                                        String.format(Messages.get("AttrList.DelAttrConfirm"),
                                                                      item, attrs.get(idx)),
                                                        Messages.get("DelDialog.Title"),
                                                        JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    attrs.remove(idx);
                    DefaultListModel<String> model = (DefaultListModel<String>) attrList.getModel();
                    model.remove(idx);
                }
            }
        }
    }

    private void bindEditorsWithItem(ESItem item) {
        namePanel.setChineseName(item.getChineseName());
        namePanel.setEnglishName(item.getEnglishName());
        descPanel.setDocumentText(item.getDescription());

        if (item instanceof Entity) {
            DefaultListModel<String> model = new DefaultListModel<String>();
            Entity enty = (Entity) item;
            attrs = mgr.getItems(enty.getAttributeIds());
            for (int i = 0; i < attrs.size(); i++) {
                String html = getListItemText(attrs.get(i), i);
                model.addElement(html);
            }

            String curSel = attrList.getSelectedValue();
            attrList.setModel(model);
            attrList.setSelectedValue(curSel, true);
        }
    }

    private String getListItemText(ESItem esItem, int pos) {
        return esItem.getCellHtml((pos & 1) == 0 ? TextUtils.CELL_NORMAL_BG
                                              : TextUtils.CELL_HIGHLIGHT_BG);
    }

    /**
     * Bind with the specified {@link ESItem} and show its content.
     *
     * @param path the path string of {@link ESItem}.
     * @param var the {@link ESItem} to show.
     */
    public void bind(String path, ESItem item) {
        this.item = (Entity) item;
        namePanel.setTitle(path);
        bindEditorsWithItem(item);
    }

    /**
     * Save bound {@link ESItem}.
     */
    public void save() {
        item.setChineseName(namePanel.getChineseName());
        item.setEnglishName(namePanel.getEnglishName());
        item.setDescription(descPanel.getDocumentText());
        if (item instanceof Entity) {
            ((Entity) item).setAttributes(attrs);
        }
    }

    private void addAttribute(int idx) {
        Vector<DefaultMutableTreeNode> nodes = mgr.getNodes();
        if (nodes.size() == 0) {
            JOptionPane.showMessageDialog(getRootPane(), Messages.get("AttrList.NoAttrs"));
            return;
        }

        Vector<Attribute> selectedAttrs = AttributeListDialog.showDialog(this, nodes);
        if (idx == -1) {
            attrs.addAll(selectedAttrs);
        } else {
            attrs.addAll(idx, selectedAttrs);
        }

        DefaultListModel<String> model = (DefaultListModel<String>) attrList.getModel();
        model.removeAllElements();
        for (int i = 0; i < attrs.size(); i++) {
            String html = getListItemText(attrs.get(i), i);
            model.addElement(html);
        }
    }

    private void editAttribute(int idx) {
        ESItem attr = attrs.get(idx);
        attrTree.showEditUI(attr);
    }

    public void setAttributeTree(ESItemTree tree) {
        attrTree = tree;
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (!SwingUtilities.isLeftMouseButton(evt) || evt.getClickCount() != 2 || evt.getSource() != attrList) {
            return;
        }

        int idx = attrList.locationToIndex(evt.getPoint());
        if (idx == -1) {
            addAttribute(idx);
        } else {
            editAttribute(idx);
        }
    }

    @Override
    public void mousePressed(MouseEvent evt) {
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
    }
}
