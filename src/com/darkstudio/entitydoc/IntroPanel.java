package com.darkstudio.entitydoc;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;

public class IntroPanel extends JPanel {

    /**
     * Create the panel.
     */
    public IntroPanel() {
        setLayout(new BorderLayout(0, 0));

        JTextPane intro = new JTextPane();
        intro.setBackground(UIManager.getColor("Panel.background"));
        intro.setEditable(false);
        intro.setContentType("text/html");
        intro.setText(Messages.get("IntroPanel.Html"));

        add(intro, BorderLayout.CENTER);
    }
}
