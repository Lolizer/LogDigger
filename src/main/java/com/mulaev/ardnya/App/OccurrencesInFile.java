package com.mulaev.ardnya.App;

import org.apache.commons.net.ftp.FTPClient;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OccurrencesInFile {
    synchronized public static ArrayList<Integer> checkFile(File file, String searchText) {
        ArrayList<Integer> occurrences = null;
        LinkedHashSet<Integer> occurrencesSet = new LinkedHashSet<>();
        int rows = searchText.split("\n").length;
        BufferedReader reader;
        String buffer = "";
        String[] split;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(Pattern.quote(searchText));

        try {
            reader = new BufferedReader
                    (new InputStreamReader(new FileInputStream(file)));
            int count = 0;
            int charSum = 0;

            for (String line; (line = reader.readLine()) != null;) {
                buffer += buffer.equals("") ? line : "\n" + line;
                split = buffer.split("\n");
                int contraction = 0;

                if (count++ > rows) {
                    matcher = pattern.matcher(buffer);

                    for (int i = 0; i < split.length - 1; i++)
                        contraction += split[i].length() + 1;

                    while (matcher.find()) {
                        occurrencesSet.add(charSum -
                                contraction +
                                matcher.start());
                    }

                    buffer = (buffer.split("\n", 2))[1];
                }

                charSum += line.length() + 1;
            }

            occurrences = new ArrayList<>(occurrencesSet);

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return occurrences;
    }
    //ftp
    synchronized public static ArrayList<Integer> checkFile(FTPClient client, String file, String searchText) {
        ArrayList<Integer> occurrences = null;
        LinkedHashSet<Integer> occurrencesSet = new LinkedHashSet<>();
        int rows = searchText.split("\n").length;
        BufferedReader reader;
        String buffer = "";
        String[] split;
        Pattern pattern;
        Matcher matcher;
        InputStream inputStream = null;

        pattern = Pattern.compile(Pattern.quote(searchText));

        try {
            inputStream = client.retrieveFileStream(file);
            reader = new BufferedReader
                    (new InputStreamReader(inputStream));
            int count = 0;
            int charSum = 0;

            for (String line; (line = reader.readLine()) != null;) {
                buffer += buffer.equals("") ? line : "\n" + line;
                split = buffer.split("\n");
                int contraction = 0;

                if (count++ > rows) {
                    matcher = pattern.matcher(buffer);

                    for (int i = 0; i < split.length - 1; i++)
                        contraction += split[i].length() + 1;

                    while (matcher.find()) {
                        occurrencesSet.add(charSum -
                                contraction +
                                matcher.start());
                    }

                    buffer = (buffer.split("\n", 2))[1];
                }

                charSum += line.length() + 1;
            }

            occurrences = new ArrayList<>(occurrencesSet);

            if (!client.completePendingCommand()) {
                GUI frame = Main.frame;
                JOptionPane.showMessageDialog(null,
                        "File " + file + " hasn't been downloaded successfully.");

                frame.getContentPane().remove(frame.fileTree);
                frame.fileTree = null;
                frame.repaint();
                frame.revalidate();
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            GUI frame = Main.frame;

            JOptionPane.showMessageDialog(frame,
                    "File " + file + " hasn't been downloaded successfully. " +
                            "Connection closed.");

            if (client.isConnected()) {
                try {
                    client.logout();
                    client.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            frame.getContentPane().remove(frame.fileTree);
            frame.fileTree = null;
            frame.repaint();
            frame.revalidate();
        }

        return occurrences;
    }
}
