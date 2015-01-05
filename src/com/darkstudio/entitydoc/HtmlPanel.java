package com.darkstudio.entitydoc;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.hexidec.ekit.EkitCore;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class HtmlPanel extends JPanel {

    private static final String TOOLBAR_BTNS = EkitCore.KEY_TOOL_CUT + "|"
                                               + EkitCore.KEY_TOOL_COPY + "|"
                                               + EkitCore.KEY_TOOL_PASTE + "|"
                                               + EkitCore.KEY_TOOL_PASTEX + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_UNDO + "|"
                                               + EkitCore.KEY_TOOL_REDO + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_FIND + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_BOLD + "|"
                                               + EkitCore.KEY_TOOL_ITALIC + "|"
                                               + EkitCore.KEY_TOOL_UNDERLINE + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_STRIKE + "|"
                                               + EkitCore.KEY_TOOL_SUPER + "|"
                                               + EkitCore.KEY_TOOL_SUB + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_ANCHOR + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_SOURCE + "|*|"
                                               + EkitCore.KEY_TOOL_ALIGNL + "|"
                                               + EkitCore.KEY_TOOL_ALIGNC + "|"
                                               + EkitCore.KEY_TOOL_ALIGNR + "|"
                                               + EkitCore.KEY_TOOL_ALIGNJ + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_ULIST + "|"
                                               + EkitCore.KEY_TOOL_OLIST + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_UNICODE + "|"
                                               + EkitCore.KEY_TOOL_UNIMATH + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_INSTABLE + "|"
                                               + EkitCore.KEY_TOOL_EDITTABLE + "|"
                                               + EkitCore.KEY_TOOL_EDITCELL + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_INSERTROW + "|"
                                               + EkitCore.KEY_TOOL_INSERTCOL + "|"
                                               + EkitCore.KEY_TOOL_DELETEROW + "|"
                                               + EkitCore.KEY_TOOL_DELETECOL + "|*|"
                                               + EkitCore.KEY_TOOL_STYLES + "|"
                                               + EkitCore.KEY_TOOL_SEP + "|"
                                               + EkitCore.KEY_TOOL_FONTS;

    private EkitCore ekit;

    public HtmlPanel(String title) {
        setBorder(new TitledBorder(title));
        setLayout(new FormLayout(new ColumnSpec[] {FormFactory.GROWING_BUTTON_COLSPEC,},
                                 new RowSpec[] {FormFactory.DEFAULT_ROWSPEC,
                                                FormFactory.DEFAULT_ROWSPEC,
                                                FormFactory.DEFAULT_ROWSPEC,
                                                FormFactory.GLUE_ROWSPEC,}));
        ekit = new EkitCore(false, // isParentApplet
                            null, // sDocument
                            null, // sStyleSheet
                            null, // sRawDocument
                            null, // sdocSource
                            null, // urlStyleSheet
                            true, // includeToolBar
                            false, // showViewSource
                            true, // showMenuIcons
                            true, // editModeExclusive
                            null, // sLanguage
                            null, // sCountry
                            false, // base64
                            false, // debugMode
                            true, // hasSpellChecker
                            true, // multiBar
                            TOOLBAR_BTNS, // toolbarSeq
                            true, // keepUnknownTags
                            true // enterBreak
        );

        add(ekit.getToolBarMain(true), "1, 1");
        add(ekit.getToolBarFormat(true), "1, 2");
        add(ekit.getToolBarStyles(true), "1, 3");
        add(ekit, "1, 4, default, fill");
    }

    public void setDocumentText(String html) {
        ekit.setDocumentText(html);
    }

    public String getDocumentText() {
        return ekit.getDocumentText();
    }

    public JMenuBar getMenuBar() {
        return ekit.getMenuBar();
    }
}
