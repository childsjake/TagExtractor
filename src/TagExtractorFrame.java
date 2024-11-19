import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractorFrame extends JFrame
{
  JPanel mainPnl;

  JPanel controlPnl;
  JButton displayBtn;
  JButton quitBtn;
  JButton saveBtn;

  JPanel displayPnl;
  JTextArea displayTA;
  JScrollPane scroller;

  JPanel optionPnl;
  JLabel fileLbl;
  JTextField fileTF;
  JButton openFileBtn;
  JLabel stopFileLbl;
  JTextField stopFileTF;
  JButton openStopFileBtn;

  Map<String, Integer> wordFrequency = new TreeMap<>();
  Set<String> stopWords = new HashSet<>();

  public TagExtractorFrame()
  {
      mainPnl = new JPanel();
      mainPnl.setLayout(new BorderLayout());

      CreateOptionsPanel();
      mainPnl.add(optionPnl, BorderLayout.NORTH);

      CreateDisplayPanel();
      mainPnl.add(displayPnl, BorderLayout.CENTER);

      CreateControlPanel();
      mainPnl.add(controlPnl, BorderLayout.SOUTH);

      add(mainPnl);
      Toolkit kit = Toolkit.getDefaultToolkit();
      Dimension screenSize = kit.getScreenSize();
      int screenWidth = (int) screenSize.getWidth();
      int screenHeight = (int) screenSize.getHeight();
      setSize(screenWidth * 2 / 4, screenHeight * 5 / 8);
      setLocation(screenWidth / 2, screenHeight / 8);
      setTitle("Tag Extractor Form");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);

  }

    private void CreateControlPanel()
    {
        controlPnl = new JPanel();
        controlPnl.setLayout(new GridLayout(1, 3));

        displayBtn = new JButton("Display Tags");
        displayBtn.addActionListener((ActionEvent ae) ->
        {
            for(Map.Entry<String, Integer> entry : wordFrequency.entrySet())
            {
                displayTA.append(entry.toString() + "\n");
            }


        });

        saveBtn = new JButton("Save Tags");
        saveBtn.addActionListener((ActionEvent ae) ->
        {
            String filename = "Tags.txt";
            String sourceDirectory = System.getProperty("user.dir");
            try
            {
                File file = new File(sourceDirectory + File.separator + filename);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for(Map.Entry<String, Integer> entry : wordFrequency.entrySet())
                {
                    writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
                }

                if (file.createNewFile())
                {
                    System.out.println("File created: " + file.getAbsolutePath());
                }
                else
                {
                    System.out.println("File already exists: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        });

        quitBtn = new JButton("Quit");
        quitBtn.addActionListener((ActionEvent ae) ->
        {
            int selectedOption = JOptionPane.showConfirmDialog(null, "Do you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
            if (selectedOption == JOptionPane.YES_OPTION)
            {
                System.exit(0);
            }
        });

        controlPnl.add(displayBtn);
        controlPnl.add(saveBtn);
        controlPnl.add(quitBtn);
    }

    private void CreateDisplayPanel()
    {
        displayPnl = new JPanel();
        displayPnl.setLayout(new GridLayout(1, 1));
        displayPnl.setBorder(new TitledBorder(new EtchedBorder(), "Tags"));
        displayTA = new JTextArea(10,15);
        displayTA.setEditable(false);
        scroller = new JScrollPane(displayTA);
        displayPnl.add(scroller);
    }

    private void CreateOptionsPanel()
    {
        optionPnl = new JPanel();
        optionPnl.setLayout(new GridLayout(1, 6));

        fileLbl = new JLabel("File Name:");
        fileTF = new JTextField(20);
        fileTF.setEditable(false);
        openFileBtn = new JButton("Choose File");
        openFileBtn.addActionListener((ActionEvent ae) ->
        {
            JFileChooser chooser = new JFileChooser();
            File selectedFile;
            String rec = "";
            try
            {
                // uses a fixed known path:
                //  Path file = Paths.get("c:\\My Documents\\data.txt");

                // use the toolkit to get the current working directory of the IDE
                // Not sure if the toolkit is thread safe...
                File workingDirectory = new File(System.getProperty("user.dir"));

                // Typiacally, we want the user to pick the file so we use a file chooser
                // kind of ugly code to make the chooser work with NIO.
                // Because the chooser is part of Swing it should be thread safe.
                chooser.setCurrentDirectory(workingDirectory);
                // Using the chooser adds some complexity to the code.
                // we have to code the complete program within the conditional return of
                // the filechooser because the user can close it without picking a file

                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = chooser.getSelectedFile();
                    Path file = selectedFile.toPath();
                    // Typical java pattern of inherited classes
                    // we wrap a BufferedWriter around a lower level BufferedOutputStream
                    InputStream in =
                            new BufferedInputStream(Files.newInputStream(file, CREATE));
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(in));

                    // Finally we can read the file LOL!
                    int line = 0;
                    while(reader.ready())
                    {
                        rec = reader.readLine();
                        String[] words = rec.split("\\s+");
                        line++;

                        // echo to screen

                        for (String word : words)
                        {
                            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                            {
                                if(!stopWords.contains(word))
                                {   if (wordFrequency.get(word) == null)
                                    {
                                        wordFrequency.put(word, 1);
                                    }
                                    else
                                    {
                                        wordFrequency.put(word, wordFrequency.get(word) + 1);
                                    }
                                }
                            }
                        }
                    }
                    reader.close(); // must close the file to seal it and flush buffer
                    System.out.println("\n\nData file read!");
                    fileTF.setText(selectedFile.getPath());
                }
            }
            catch (FileNotFoundException e)
            {
                System.out.println("File not found!!!");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


        });
        stopFileLbl = new JLabel("Stop File:");
        stopFileTF = new JTextField(20);
        stopFileTF.setEditable(false);
        openStopFileBtn = new JButton("Choose Stop File");
        openStopFileBtn.addActionListener((ActionEvent ae) ->
        {
            JFileChooser chooser = new JFileChooser();
            File selectedFile;
            String rec = "";
            try
            {
                // uses a fixed known path:
                //  Path file = Paths.get("c:\\My Documents\\data.txt");

                // use the toolkit to get the current working directory of the IDE
                // Not sure if the toolkit is thread safe...
                File workingDirectory = new File(System.getProperty("user.dir"));

                // Typiacally, we want the user to pick the file so we use a file chooser
                // kind of ugly code to make the chooser work with NIO.
                // Because the chooser is part of Swing it should be thread safe.
                chooser.setCurrentDirectory(workingDirectory);
                // Using the chooser adds some complexity to the code.
                // we have to code the complete program within the conditional return of
                // the filechooser because the user can close it without picking a file

                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = chooser.getSelectedFile();
                    Path file = selectedFile.toPath();
                    // Typical java pattern of inherited classes
                    // we wrap a BufferedWriter around a lower level BufferedOutputStream
                    InputStream in =
                            new BufferedInputStream(Files.newInputStream(file, CREATE));
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(in));

                    // Finally we can read the file LOL!
                    int line = 0;
                    while(reader.ready())
                    {
                        rec = reader.readLine();
                        String[] words = rec.split("\\s+");
                        line++;

                        // echo to screen

                        for (String word : words)
                        {
                            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                            {
                                stopWords.add(word);
                            }
                        }
                    }
                    reader.close(); // must close the file to seal it and flush buffer
                    System.out.println("\n\nData file read!");
                    stopFileTF.setText(selectedFile.getPath());
                }
            }
            catch (FileNotFoundException e)
            {
                System.out.println("File not found!!!");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


        });
        optionPnl.add(fileLbl);
        optionPnl.add(fileTF);
        optionPnl.add(openFileBtn);
        optionPnl.add(stopFileLbl);
        optionPnl.add(stopFileTF);
        optionPnl.add(openStopFileBtn);
    }
}


