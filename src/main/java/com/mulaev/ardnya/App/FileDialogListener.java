package com.mulaev.ardnya.App;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileDialogListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        GUI frame = Main.frame;
        String input;

        Shell shell = new Shell(Display.getDefault(),
                SWT.NO_TRIM | SWT.PRIMARY_MODAL);
        shell.setSize(0, 0);
        shell.open();

        DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.SHEET);
        directoryDialog.setText("Folder selection");
        shell.forceActive();
        shell.setActive();

        input = directoryDialog.open();
        shell.dispose();

        if(input == null)
            return;

        frame.filePath.setText(input);

        frame.repaint();
        frame.revalidate();
    }
}
