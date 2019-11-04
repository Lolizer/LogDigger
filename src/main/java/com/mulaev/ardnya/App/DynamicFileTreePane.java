package com.mulaev.ardnya.App;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class DynamicFileTreePane extends JScrollPane {
    private JTree tree;
    private int fileCount;

    public DynamicFileTreePane(File dirForShowing, String fileType) {
        tree = new JTree(addNodes(dirForShowing, fileType));
        setupModel();
        setInternMouseListener();
    }

    public DynamicFileTreePane(FTPClient client, String remotePath, String fileType) { //FTP
        tree = new JTree(addNodes(client, remotePath, fileType));
        setupModel();
        setFTPMouseListener();
    }

    private void setupModel() {
        tree.setShowsRootHandles(true);
        //tree.setBorder(new LineBorder(new Color(0, 0, 0)));
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

        tree.setToolTipText("Use double click to open file.");
        setViewportView(tree);

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel,
                                                          boolean expanded,
                                                          boolean leaf,
                                                          int row,
                                                          boolean hasFocus) {
                //intern file
                if (value instanceof DefaultMutableTreeNode) {
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                    if (value instanceof File)
                        value = ((File) value).getName();
                    //FTP
                    if (value instanceof FTPFile)
                        value = ((FTPFile) value).getName();
                }

                return super.getTreeCellRendererComponent
                        (tree, value, sel, expanded, leaf, row, hasFocus);
            }
        });
    }
    //inner file
    private DefaultMutableTreeNode addNodes(File dir, String fileType) {
        DefaultMutableTreeNode node = null;

        if (dir.exists())
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                DefaultMutableTreeNode temp = addNodes(file, fileType);
                if (temp != null) {
                    if (node == null)
                        node = new DefaultMutableTreeNode(dir);
                    node.add(temp);
                }
            } else if (file.getName().matches(".*" + Pattern.quote(fileType))) { //Метод проверки файла на содержание
                if (!OccurrencesInFile.checkFile(file,
                        Main.frame.searchArea.getText()).isEmpty()) {
                    if (node == null)
                        node = new DefaultMutableTreeNode(dir);
                    node.add(new DefaultMutableTreeNode(file));

                    fileCount++;
                }
            }
        }
        return node;
    }

    public int getFileCount() {
        return fileCount;
    }

    private void setInternMouseListener() {
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File nodeInfo;
                DefaultMutableTreeNode node;
                ArrayList<Integer> occurrences;
                GUI frame = Main.frame;
                BufferedReader reader;
                String doc = "";

                if (e.getClickCount() == 2) {
                    node = (DefaultMutableTreeNode)
                            tree.getLastSelectedPathComponent();

                    if (node == null) return;

                    nodeInfo = (File) node.getUserObject();

                    if (nodeInfo.isDirectory() || !nodeInfo.exists())
                        return;

                    occurrences = OccurrencesInFile.checkFile(nodeInfo,
                            frame.searchArea.getText());
                    frame.foundText.occurrences = occurrences;
                    frame.foundText.iterator = occurrences.listIterator();

                    try {
                        reader = new BufferedReader
                                (new InputStreamReader(new FileInputStream(nodeInfo)));

                        for (String line; (line = reader.readLine()) != null;) {
                            doc += doc.equals("") ? line : "\n" + line;
                        }

                        reader.close();
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    frame.foundText.setText(doc);
                    frame.fileOpenedLabel.setText(nodeInfo.getAbsolutePath());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                GUI frame = Main.frame;
                JPanel mainPanel = (JPanel) frame.getContentPane();
                //resize JScrollPane by click
                mainPanel.repaint();
                mainPanel.revalidate();
            }
        });
    }
    //ftp
    private DefaultMutableTreeNode addNodes(FTPClient client,
                                            String previous,
                                            String fileType) {
        DefaultMutableTreeNode node = null;
        FTPFile[] fileList;
        String[] split = null;
        HashMap<FTPFile, String> paths = ((GUI.FindButton) Main.frame.findButton).paths;

        try {
            if (previous == null)
                fileList = client.listFiles();
            else fileList = client.listFiles(previous);

            for (FTPFile file : fileList) {
                if (file.isDirectory()) {
                    DefaultMutableTreeNode temp =
                            addNodes(client, previous == null ? file.getName() :
                                    previous + "/" + file.getName(), fileType);

                    if (temp != null) {
                        //if (previous != null)
                        //    split = previous.split("/");

                        if (node == null)
                            node = new DefaultMutableTreeNode(
                                    previous == null ? "root" :
                                            file);

                        node.add(temp);
                    }
                } else if (file.getName().matches(".*" + Pattern.quote(fileType))) {
                    if (!OccurrencesInFile.checkFile(client, previous == null ?
                                    file.getName() : previous + "/" + file.getName(),
                            Main.frame.searchArea.getText()).isEmpty()) {

                        if (previous != null)
                            split = previous.split("/");

                        if (node == null)
                            node = new DefaultMutableTreeNode(
                                    previous == null ? "root" :
                                            split[split.length - 1]);

                        node.add(new DefaultMutableTreeNode(file));

                        paths.put(file, previous + "/" + file.getName());

                        fileCount++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }

    private void setFTPMouseListener() {
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FTPFile nodeInfo;
                DefaultMutableTreeNode node;
                ArrayList<Integer> occurrences;
                GUI frame = Main.frame;
                BufferedReader reader;
                String doc = "";
                HashMap<FTPFile, String> paths = ((GUI.FindButton) Main.frame.findButton).paths;
                String path = null;
                FTPClient client = ((GUI.FindButton)frame.findButton).client;

                if (e.getClickCount() == 2) {
                    node = (DefaultMutableTreeNode)
                            tree.getLastSelectedPathComponent();

                    if (node == null || node.getUserObject() instanceof String) return;

                    nodeInfo = (FTPFile) node.getUserObject();

                    if (nodeInfo.isDirectory() || !nodeInfo.isValid())
                        return;

                    for (FTPFile tempPath : paths.keySet())
                        if (tempPath == nodeInfo)
                            path = paths.get(tempPath);

                    occurrences = OccurrencesInFile.checkFile(
                            client,
                            path, frame.searchArea.getText());

                    frame.foundText.occurrences = occurrences;
                    frame.foundText.iterator = occurrences.listIterator();

                    try {
                        InputStream inputStream =
                                client.retrieveFileStream(path);

                        reader = new BufferedReader
                                (new InputStreamReader(inputStream));

                        for (String line; (line = reader.readLine()) != null;) {
                            doc += doc.equals("") ? line : "\n" + line;
                        }

                        client.completePendingCommand();
                        inputStream.close();
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                        return;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        return;
                    }

                    frame.foundText.setText(doc);
                    frame.fileOpenedLabel.setText(path);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                GUI frame = Main.frame;
                JPanel mainPanel = (JPanel) frame.getContentPane();
                //resize JScrollPane by click
                mainPanel.repaint();
                mainPanel.revalidate();
            }
        });
    }
}
