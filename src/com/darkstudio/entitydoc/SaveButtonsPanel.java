package com.darkstudio.entitydoc;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SaveButtonsPanel extends JPanel {
    public static final String CMD_SAVE = "SAVE";
    public static final String CMD_CANCEL = "CANCEL";

    public SaveButtonsPanel(ActionListener listener) {
        JButton btnSave = new JButton(Messages.get("SavePane.Save"));
        btnSave.setActionCommand(CMD_SAVE);
        btnSave.addActionListener(listener);
        add(btnSave);

        JButton btnCancel = new JButton(Messages.get("SavePane.Cancel"));
        btnCancel.setActionCommand(CMD_CANCEL);
        btnCancel.addActionListener(listener);
        add(btnCancel);
    }
}
