package com.darkstudio.entitydoc;

import com.darkstudio.entitydoc.data.Attribute;
import com.darkstudio.entitydoc.data.AttributeManager;
import com.darkstudio.entitydoc.data.ESItem;
import com.darkstudio.entitydoc.utils.TextUtils;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;

public class AttributePanel extends JPanel implements ActionListener {
    private static final String CMD_COMBO_EDIT = "COMBO_MANUAL_INPUT";

    private AttributeManager mgr;
    private ESItem item;

    private NamePanel namePanel;
    private HtmlPanel descPanel;
    private JComboBox<String> dataTypes = new JComboBox<String>();

    public AttributePanel(AttributeManager mgr) {
        this.mgr = mgr;

        FormLayout layout = new FormLayout(new ColumnSpec[] {
                FormFactory.GROWING_BUTTON_COLSPEC,
                FormFactory.BUTTON_COLSPEC,},
            new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.GLUE_ROWSPEC,
                FormFactory.PREF_ROWSPEC,});
        setLayout(layout);

        namePanel = new NamePanel(Messages.get("AttrPane.NameGrp"));
        add(namePanel, "1, 1");

        JPanel dataTypePanel = createDataTypePanel();
        add(dataTypePanel, "2, 1, default, top");

        descPanel = createDescPanel();
        add(descPanel, "1, 2, 2, 1, default, fill");

        JPanel btnPanel = new SaveButtonsPanel(this);
        add(btnPanel, "2, 3");
    }

    private JPanel createDataTypePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(Messages.get("AttrPane.DataTypeGrp")));
        panel.setLayout(new BorderLayout());

        dataTypes.setEditable(true);
        dataTypes.setModel(new DefaultComboBoxModel<String>(mgr.getDataTypes()));
        dataTypes.setActionCommand(CMD_COMBO_EDIT);
        dataTypes.addActionListener(this);
        panel.add(dataTypes, BorderLayout.CENTER);
        return panel;
    }

    private HtmlPanel createDescPanel() {
        // TODO: initialized with CSS
        return new HtmlPanel(Messages.get("AttrPane.DescGrp"));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (SaveButtonsPanel.CMD_SAVE.equals(cmd)) {
            save();
        } else if (SaveButtonsPanel.CMD_CANCEL.equals(cmd)) {
            bindEditorsWithItem(item);
        } else if (CMD_COMBO_EDIT.equals(cmd) && evt.getModifiers() == 0) {
            /*
             * FIXME: every inputed type was cached into AttributeManager.types while the code flow
             * does only cache it in save().
             */
            String input = (String) dataTypes.getSelectedItem();
            if (TextUtils.isEmpty(input)) {
                return;
            }
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dataTypes.getModel();
            int pos = model.getIndexOf(input);
            if (pos == -1) {
                dataTypes.addItem(input);
            }
        }
    }

    private void bindEditorsWithItem(ESItem item) {
        namePanel.setChineseName(item.getChineseName());
        namePanel.setEnglishName(item.getEnglishName());
        descPanel.setDocumentText(item.getDescription());

        String curSel = (String) dataTypes.getSelectedItem();
        dataTypes.setModel(new DefaultComboBoxModel<String>(mgr.getDataTypes()));
        dataTypes.setSelectedItem(curSel);

        if (item instanceof Attribute) {
            Attribute attr = (Attribute) item;
            dataTypes.setSelectedItem(attr.getDataType());
        }
    }

    /**
     * Bind with the specified {@link ESItem} and show its content.
     *
     * @param path the path string of {@link ESItem}.
     * @param var the {@link ESItem} to show.
     */
    public void bind(String path, ESItem item) {
        this.item = item;
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
        if (item instanceof Attribute) {
            Attribute attr = (Attribute) item;
            attr.setDataType((String) dataTypes.getSelectedItem());
        }
        mgr.onUpdateItem(item);
    }

    public JMenuBar getMenuBar() {
        return descPanel.getMenuBar();
    }
}
