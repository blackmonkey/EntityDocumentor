package com.darkstudio.entitydoc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.darkstudio.entitydoc.data.Attribute;
import com.darkstudio.entitydoc.data.AttributeManager;
import com.darkstudio.entitydoc.data.EntityManager;
import com.darkstudio.entitydoc.data.ESItem;
import com.darkstudio.entitydoc.utils.TextUtils;

public class MainFrame implements ActionListener, WindowListener, ComponentListener, TreeSelectionListener, FocusListener {
    private static final String CMD_NEW_WORKSPACE = "NEW_WORKSPACE";
    private static final String CMD_OPEN_WORKSPACE = "OPEN_WORKSPACE";
    private static final String CMD_EXIT = "EXIT";
    private static final String CMD_ABOUT = "ABOUT";
    private static final int MAIN_MENU_COUNT = 2;

    private double hDivLocation = 0.4;
    private double vDivLocation = 0.5;

    private boolean cacheHDivLoc = false;
    private boolean cacheVDivLoc = false;

    private File currentDir;
    private File workspace;

    private AttributeManager attrMgr = new AttributeManager();
    private EntityManager entyMgr = new EntityManager();
    private ESItemTree attrTree;
    private ESItemTree entyTree;

    private JFrame mainFrame;
    private JSplitPane mainPane;
    private JSplitPane leftPane;

    private IntroPanel introPanel = new IntroPanel();
    private AttributePanel attrPanel = new AttributePanel(attrMgr);
    private EntityPanel entyPanel = new EntityPanel(attrMgr);

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame window = new MainFrame();
                    window.mainFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainFrame() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mainFrame = new JFrame();
        mainFrame.setTitle(Messages.get("MainFrame.Title"));
        mainFrame.setBounds(100, 100, 800, 500);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(this);
        mainFrame.addComponentListener(this);

        JMenuBar menuBar = createMenuBar();
        mainFrame.setJMenuBar(menuBar);

        mainPane = new JSplitPane();
        mainPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                                                new PropertyChangeListener() {

                                                    @Override
                                                    public void propertyChange(
                                                            PropertyChangeEvent evt) {
                                                        if (cacheHDivLoc) {
                                                            Rectangle bound = mainPane.getBounds();
                                                            hDivLocation = (Integer) evt.getNewValue()
                                                                           / bound.getWidth();
                                                        }
                                                    }
                                                });
        mainFrame.getContentPane().add(mainPane, BorderLayout.CENTER);

        leftPane = new JSplitPane();
        leftPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                                                new PropertyChangeListener() {

                                                    @Override
                                                    public void propertyChange(
                                                            PropertyChangeEvent evt) {
                                                        if (cacheVDivLoc) {
                                                            Rectangle bound = mainPane.getBounds();
                                                            vDivLocation = (Integer) evt.getNewValue()
                                                                           / bound.getHeight();
                                                        }
                                                    }
                                                });
        leftPane.setMinimumSize(new Dimension(200, 400));
        mainPane.setLeftComponent(leftPane);
        mainPane.setRightComponent(introPanel);

        attrTree = new ESItemTree(attrMgr);
        attrTree.addTreeSelectionListener(this);
        attrTree.addFocusListener(this);
        leftPane.setLeftComponent(attrTree);

        entyTree = new ESItemTree(entyMgr);
        entyTree.addTreeSelectionListener(this);
        entyTree.addFocusListener(this);
        leftPane.setRightComponent(entyTree);

        entyPanel.setAttributeTree(entyTree);

        updateTrees();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu mnDoc = new JMenu(Messages.get("MainFrame.MenuDoc"));
        mnDoc.setMnemonic(KeyEvent.VK_D);
        menuBar.add(mnDoc);

        JMenuItem miNewWorkspace = new JMenuItem(Messages.get("MainFrame.NewWorkspace"));
        miNewWorkspace.setActionCommand(CMD_NEW_WORKSPACE);
        miNewWorkspace.setMnemonic(KeyEvent.VK_N);
        miNewWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        miNewWorkspace.addActionListener(this);
        mnDoc.add(miNewWorkspace);

        JMenuItem miOpenWorkspace = new JMenuItem(Messages.get("MainFrame.OpenWorkspace"));
        miOpenWorkspace.setActionCommand(CMD_OPEN_WORKSPACE);
        miOpenWorkspace.setMnemonic(KeyEvent.VK_O);
        miOpenWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        miOpenWorkspace.addActionListener(this);
        mnDoc.add(miOpenWorkspace);

        JMenuItem miExit = new JMenuItem(Messages.get("MainFrame.MenuItemExit"));
        miExit.setActionCommand(CMD_EXIT);
        miExit.setMnemonic(KeyEvent.VK_X);
        miExit.addActionListener(this);
        mnDoc.addSeparator();
        mnDoc.add(miExit);

        JMenu mnHelp = new JMenu(Messages.get("MainFrame.MenuHelp"));
        mnHelp.setMnemonic(KeyEvent.VK_H);
        menuBar.add(mnHelp);

        JMenuItem miAbout = new JMenuItem(Messages.get("MainFrame.MenuItemAbout"));
        miAbout.setActionCommand(CMD_ABOUT);
        miAbout.setMnemonic(KeyEvent.VK_A);
        miAbout.addActionListener(this);
        mnHelp.add(miAbout);

        return menuBar;
    }

    private void adjustComponents() {
        mainPane.setDividerLocation(hDivLocation);
        leftPane.setDividerLocation(vDivLocation);
        cacheHDivLoc = true;
        cacheVDivLoc = true;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (CMD_OPEN_WORKSPACE.equals(cmd) || CMD_NEW_WORKSPACE.equals(cmd)) {
            showOpenWorkspaceDialog(CMD_NEW_WORKSPACE.equals(cmd));
        } else if (CMD_EXIT.equals(cmd)) {
            tryExit();
        } else if (CMD_ABOUT.equals(cmd)) {
            showAboutDialog();
        }
    }

    private void showOpenWorkspaceDialog(boolean newWorkspace) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Messages.get("MainFrame.OpenWorkspace"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(currentDir);

        if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        workspace = chooser.getSelectedFile();
        currentDir = workspace.getParentFile();
        if (currentDir == null) {
            currentDir = workspace;
        }
        mainFrame.setTitle(String.format("%s - [%s]", Messages.get("MainFrame.Title"),
                                         workspace));
        updateTrees();
    }

    private void updateTrees() {
        attrTree.update(workspace);
        entyTree.update(workspace);
    }

    private void tryExit() {
        Vector<ESItem> unsavedAttrs = attrMgr.getUnsavedChanges();
        Vector<ESItem> unsavedEntys = entyMgr.getUnsavedChanges();
        if (unsavedAttrs.size() > 0 || unsavedEntys.size() > 0) {
            // TODO: warning unsaved changes:
            // 1. show prompt dialog with list of unsaved changes
            // * saving action should be performed in the dialog life cycle.
            // 2. exit after dialog is disposed.
//            return;
        }

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.dispose();
    }

    private void showAboutDialog() {
        AboutDialog dlg = new AboutDialog(mainFrame);
        dlg.setLocationRelativeTo(mainFrame);
        dlg.setVisible(true);
    }

    @Override
    public void windowActivated(WindowEvent evt) {
    }

    @Override
    public void windowClosed(WindowEvent evt) {
    }

    @Override
    public void windowClosing(WindowEvent evt) {
        tryExit();
    }

    @Override
    public void windowDeactivated(WindowEvent evt) {
    }

    @Override
    public void windowDeiconified(WindowEvent evt) {
    }

    @Override
    public void windowIconified(WindowEvent evt) {
    }

    @Override
    public void windowOpened(WindowEvent evt) {

    }

    @Override
    public void componentHidden(ComponentEvent evt) {
    }

    @Override
    public void componentMoved(ComponentEvent evt) {
    }

    @Override
    public void componentResized(ComponentEvent evt) {
        adjustComponents();
    }

    @Override
    public void componentShown(ComponentEvent evt) {
        adjustComponents();
    }

    @Override
    public void valueChanged(TreeSelectionEvent evt) {
        DefaultMutableTreeNode node = getContextTreeNode(evt.getSource());
        if (node == null) {
            // nothing selected, show intro panel and return
            Component pane = mainPane.getRightComponent();
            if (pane != introPanel) {
                if (pane == attrPanel) {
                    attrPanel.save();
                } else if (pane == entyPanel) {
                    entyPanel.save();
                }
                clearEditorMenus();
                mainPane.setRightComponent(introPanel);
            }
            return;
        }

        showTreeNodePanel(node);
    }

    @Override
    public void focusGained(FocusEvent evt) {
        DefaultMutableTreeNode node = getContextTreeNode(evt.getSource());
        if (node == null) {
            // nothing selected, return
            return;
        }

        showTreeNodePanel(node);
    }

    @Override
    public void focusLost(FocusEvent evt) {
    }

    /**
     * Get valid context {@link ESItemTree} node on event.
     *
     * @param src event source
     * @return {@code null} if no valid context node, {@link DefaultMutableTreeNode} instance otherwise.
     */
    private DefaultMutableTreeNode getContextTreeNode(Object src) {
        DefaultMutableTreeNode node = null;
        if (src == attrTree) {
            node = (DefaultMutableTreeNode) attrTree.getLastSelectedPathComponent();
        } else if (src == entyTree) {
            node = (DefaultMutableTreeNode) entyTree.getLastSelectedPathComponent();
        }

        if (node != null && !(node.getUserObject() instanceof ESItem)) {
            node = null;
        }
        return node;
    }

    /**
     * Show editing UI for specified {@link ESItemTree} node.
     *
     * @param node the node to edit.
     */
    private void showTreeNodePanel(DefaultMutableTreeNode node) {
        ESItem var = (ESItem) node.getUserObject();
        String path = TextUtils.buildPath(node.getUserObjectPath());
        if (var instanceof Attribute) {
            attrPanel.bind(path, var);
            mainPane.setRightComponent(attrPanel);
//            clearEditorMenus();
//            addEditorMenus(attrPanel.getMenuBar());
        } else { // select an entity node
            entyPanel.bind(path, var);
            mainPane.setRightComponent(entyPanel);
        }
    }

    private void clearEditorMenus() {
        JMenuBar menuBar = mainFrame.getJMenuBar();
        while (menuBar.getMenuCount() > MAIN_MENU_COUNT) {
            menuBar.remove(1);
        }
    }

    private void addEditorMenus(JMenuBar editorMenuBar) {
        JMenuBar menuBar = mainFrame.getJMenuBar();
        for (int i = 0; i < editorMenuBar.getMenuCount(); i++) {
            menuBar.add(editorMenuBar.getMenu(i));
        }
    }
}
