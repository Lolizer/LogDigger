package com.mulaev.ardnya.App;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class GUI extends JFrame {
    JPanel upperPanel;
    JPanel centralPanel;
    volatile DynamicFileTreePane fileTree;

    //upper panel content
    JTextArea searchArea;
    JTextField filePath;
    JTextField fileType;
    JButton fileDialogButton;
    JButton findButton;


    //central panel content
    FoundTextArea foundText;
    JButton next;
    JButton previous;
    JButton selectAll;
    JButton clear;
    JLabel fileOpenedLabel;

    {
        //panels and file catalog
        upperPanel = new JPanel();
        centralPanel = new JPanel();
        fileTree = null;
            //upper
        searchArea = new JTextArea(2, 11);
        filePath = new JTextField(5);
        fileType = new JTextField(5);
        fileDialogButton = new JButton("Browse");
        findButton = new FindButton("Find");
            //central
        foundText = new FoundTextArea(5, 10);
        next = new JButton("Next");
        previous = new JButton("Previous");
        selectAll = new JButton("All");
        clear = new JButton("Clear");
        fileOpenedLabel = new JLabel("File: ");
    }

    public GUI() {
        setupFrame();
    }

    public GUI(String name) {
        super(name);
        setupFrame();
    }

    private void setupFrame() {
        getContentPane().add(upperPanel, BorderLayout.NORTH);
        getContentPane().add(centralPanel, BorderLayout.CENTER);

        setupComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 400));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupComponents() {
        setupUpperPanel();
        setupCentralPanel();
    }

    private void setupUpperPanel() {
        GroupLayout upperPanelLayout = new GroupLayout(upperPanel);
        JScrollPane searchAreaWrapper = new JScrollPane(searchArea);
        JLabel searchAreaLabel = new JLabel("Search field");
        JLabel filePathLabel = new JLabel("Search folder");
        JLabel fileTypeLabel = new JLabel("File type");

        upperPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setAutoCreateGaps(true);
        upperPanelLayout.setAutoCreateContainerGaps(true);

        upperPanelLayout.setHorizontalGroup(
                upperPanelLayout.createSequentialGroup()
                        .addGroup(upperPanelLayout.createParallelGroup()
                                .addComponent(searchAreaLabel)
                                .addComponent(searchAreaWrapper))
                        .addGroup(upperPanelLayout.createParallelGroup()
                                .addComponent(fileTypeLabel)
                                .addComponent(fileType,
                                              GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE * 2))
                        .addGroup(upperPanelLayout.createParallelGroup()
                                .addComponent(filePathLabel)
                                .addComponent(filePath))
                        .addComponent(fileDialogButton)
                        .addComponent(findButton)
        );
        upperPanelLayout.setVerticalGroup(
                upperPanelLayout.createSequentialGroup()
                        .addGroup(upperPanelLayout.createParallelGroup()
                                .addComponent(searchAreaLabel)
                                .addComponent(filePathLabel)
                                .addComponent(fileTypeLabel))
                        .addGroup(upperPanelLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(searchAreaWrapper,
                                        searchArea.getPreferredSize().height * 3,
                                              GroupLayout.PREFERRED_SIZE,
                                        searchArea.getPreferredSize().height * 5)
                                .addComponent(filePath)
                                .addComponent(fileType)
                                .addComponent(fileDialogButton)
                                .addComponent(findButton))
        );

        searchArea.setLineWrap(true);
        searchArea.setWrapStyleWord(true);
        filePath.setToolTipText("<html>FTP sample:<br>ftp:login:pass@ip[@remote_path]</html>");

        fileDialogButton.addActionListener(new FileDialogListener());
        findButton.addActionListener(new FindButtonListener());
    }

    private void setupCentralPanel() {
        GroupLayout centralPanelLayout = new GroupLayout(centralPanel);
        JScrollPane foundTextWrapper = new JScrollPane(foundText);

        //centralPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        centralPanel.setLayout(centralPanelLayout);
        centralPanelLayout.setAutoCreateGaps(true);
        centralPanelLayout.setAutoCreateContainerGaps(true);

        centralPanelLayout.setHorizontalGroup(
                centralPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(fileOpenedLabel)
                .addGroup(centralPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(foundTextWrapper)
                        .addGroup(centralPanelLayout.createSequentialGroup()
                                 .addComponent(clear)
                                 .addComponent(previous)
                                 .addComponent(next)
                                 .addComponent(selectAll))
                )
        );
        centralPanelLayout.setVerticalGroup(
                centralPanelLayout.createSequentialGroup()
                .addComponent(fileOpenedLabel)
                .addComponent(foundTextWrapper)
                .addGroup(centralPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                          .addComponent(clear)
                          .addComponent(previous)
                          .addComponent(next)
                          .addComponent(selectAll))
        );

        foundText.setLineWrap(true);
        foundText.setWrapStyleWord(true);
        foundText.setEditable(false);

        clear.addActionListener((e) -> {
            foundText.setText("");
            fileOpenedLabel.setText("File: ");
        });

        previous.addActionListener((e) -> {
            if (foundText.getText().equals(""))
                return;

            Integer start = foundText.getPrevious();

            if (start == null)
                return;

            foundText.grabFocus();
            foundText.select(start, start + searchArea.getText().length());
        });

        next.addActionListener((e) -> {
            if (foundText.getText().equals(""))
                return;

            Integer start = foundText.next();

            if (start == null)
                return;

            foundText.grabFocus();
            foundText.select(start, start + searchArea.getText().length());
        });

        selectAll.addActionListener((e) -> {
            foundText.grabFocus();
            foundText.selectAll();
        });
    }

    class FoundTextArea extends JTextArea {
        ArrayList<Integer> occurrences;
        ListIterator<Integer> iterator;

        private FoundTextArea() {
            super();
        }

        private FoundTextArea(int columns, int rows) {
            super(columns, rows);
        }

        Integer next() {
            if (iterator.hasNext())
                return iterator.next();
            return null;
        }

        Integer getPrevious() {
            if (iterator.hasPrevious())
                return iterator.previous();
            return null;
        }
    }

    class FindButton extends JButton {
        FTPClient client;
        HashMap<FTPFile,String> paths;

        private FindButton() {super();}
        private FindButton(String name) {super(name);}
    }
}
