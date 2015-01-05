package com.darkstudio.entitydoc;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class NamePanel extends JPanel {

    private JTextField cnNameEditor = new JTextField();
    private JTextField enNameEditor = new JTextField();

    public NamePanel(String title) {
        setTitle(title);
        FormLayout layout = new FormLayout(new ColumnSpec[] {FormFactory.MIN_COLSPEC,
                                                             FormFactory.GROWING_BUTTON_COLSPEC,},
                                           new RowSpec[] {FormFactory.DEFAULT_ROWSPEC,
                                                          FormFactory.DEFAULT_ROWSPEC,});
        setLayout(layout);

        JLabel lblCn = new JLabel(Messages.get("NamePane.CnName"));
        JLabel lblEn = new JLabel(Messages.get("NamePane.EnName"));

        add(lblCn, "1, 1");
        add(cnNameEditor, "2, 1");
        add(lblEn, "1, 2");
        add(enNameEditor, "2, 2");
    }

    public void setTitle(String title) {
        setToolTipText(title);

        Border border = getBorder();
        if (!(border instanceof TitledBorder)) {
            setBorder(new TitledBorder(title));
        } else {
            ((TitledBorder) border).setTitle(title);
        }
    }

    public void setChineseName(String name) {
        cnNameEditor.setText(name);
    }

    public void setEnglishName(String name) {
        enNameEditor.setText(name);
    }

    public String getChineseName() {
        return cnNameEditor.getText();
    }

    public String getEnglishName() {
        return enNameEditor.getText();
    }
}
