package com.mulaev.ardnya.App;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FindButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        GUI frame = Main.frame;
        String input = frame.filePath.getText();
        String type = frame.fileType.getText();

        if (input == null || input.equals("")) {
            JOptionPane.showMessageDialog(frame, "Enter search path!");
            return;
        }

        if (type == null || type.equals(""))
            type = ".log";

        if (frame.fileTree != null) {
            frame.getContentPane().remove(frame.fileTree);
            frame.repaint();
            frame.revalidate();

            frame.pack();
        }

        if (type.matches("\\W\\..+"))
            type = type.substring(1);

        final String typeInThread = type;

        if (input.matches("ftp:.*")) {
            if (input.matches(
                    "ftp:\\w+:" + //login
                            "\\w+@" + //password
                            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(@" + //ip
                            "[\\w/]+)?")) { //directory
                //new Thread(() -> setFTPFileTree(frame, input, typeInThread)).start();
                setFTPFileTree(frame, input, typeInThread);
                return;
            }

            JOptionPane.showMessageDialog(frame,
                    "<html>\"ftp:login:password@ip[@remote_path]\" " +
                            "pattern failed!<br>" +
                            "login [a-zA-Z0-9_]<br>" +
                            "password [a-zA-Z0-9_]<br>" +
                            "ip [0-255].[0-255].[0-255].[0-255]<br>" +
                            "remote_path [a-zA-Z0-9_/] (optional)<html>");
            return;
        }

        new Thread(() -> {
            frame.fileTree = new DynamicFileTreePane(new File(input), typeInThread);

            if (frame.fileTree.getFileCount() == 0) {
                frame.fileTree = null;

                frame.repaint();
                frame.revalidate();
                JOptionPane.showMessageDialog(frame, "Files not found");

                return;
            }

            frame.getContentPane().add(frame.fileTree, BorderLayout.WEST);

            frame.repaint();
            frame.revalidate();
        }).start();
    }

    private void setFTPFileTree(GUI frame, String input, String typeInThread) {
        FTPClient client = new FTPClient();
        String[] firstSplit = input.split("@");
        String[] secondSplit = firstSplit[0].substring(4).split(":");
        String remotePath = firstSplit.length > 2 ? firstSplit[2] : null;
        String server = firstSplit[1];
        int port = 21;
        String user = secondSplit[0];
        String pass = secondSplit[1];

        try {
            client.connect(server, port);

            int replyCode = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                client.disconnect();
                return;
            }

            boolean success = client.login(user, pass);
            if (!success) {
                JOptionPane.showMessageDialog(frame, "Authentication failed!");
                client.disconnect();
                return;
            }

            ((GUI.FindButton) frame.findButton).client = client;
            ((GUI.FindButton) frame.findButton).paths = new HashMap<FTPFile, String>();

            frame.fileTree = new DynamicFileTreePane(client, remotePath,typeInThread);

            if (frame.fileTree.getFileCount() == 0) {
                frame.getContentPane().remove(frame.fileTree);
                frame.fileTree = null;

                frame.repaint();
                frame.revalidate();
                JOptionPane.showMessageDialog(frame, "Files not found");

                try {
                    if (client.isConnected()) {
                        client.logout();
                        client.disconnect();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                return;
            }

            frame.getContentPane().add(frame.fileTree, BorderLayout.WEST);

            frame.repaint();
            frame.revalidate();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connection to " +
                    server + " failed!");

            try {
                frame.getContentPane().remove(frame.fileTree);
                frame.repaint();
                frame.revalidate();

                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
