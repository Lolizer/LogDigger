package com.mulaev.ardnya.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FindButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        GUI frame = Main.frame;
        String input = frame.filePath.getText();
        String type = frame.fileType.getText();

        if(type == null || type.equals(""))
            type = ".log";

        if (frame.fileTree != null)
            frame.getContentPane().remove(frame.fileTree);

        if (type.matches("\\W\\..+"))
            type = type.substring(1);

        final String typeInThread = type;

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

        /*
        frame.fileTree = new DynamicFileTreePane(new File(input), type);

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
         */
    }
}
