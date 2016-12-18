import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wang on 2016/12/17.
 */
public class ReciteWordsForm extends JFrame{
    private JProgressBar progressBar1;
    private JButton 跳过Button;
    private JComboBox 模式ComboBox;
    private JSpinner 速度Spinner;
    private JPanel WordPanel;
    private JPanel RootPanel;
    private JButton 开始Button;
    private char status = 0;
    public ReciteWordsForm() {
        开始Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(status == 0){
                    WordPanel.setLayout(new FlowLayout());
                    WordPanel.add(lblWord);
                    WordPanel.add(lblMeaning);
                    status = 1;
                }
                if(status != 2){
                    start();
                    status = 2;
                }
                else
                {
                    stop();
                    status = 1;
                }
            }
        });
    }

    private void stop() {
    }

    public static void main(String[] args) {
        ReciteWordsForm frame = new ReciteWordsForm();
        frame.setTitle("背单词");
        frame.setContentPane(new ReciteWordsForm().RootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    JLabel lblWord = new JLabel("word");
    JLabel lblMeaning = new JLabel("meaning");

    List<String> words = new ArrayList<>();
    List<String> meanings = new ArrayList<>();
    int current = 0;
    public void start() {
        new Thread(()->{
            try{
                readAll();
            }catch(IOException ex){}
            new javax.swing.Timer(1000,(e)->{
                lblWord.setText( words.get(current) );
                lblMeaning.setText( meanings.get(current) );
                current++;
            }).start();
        }).start();
    }

    public void readAll( ) throws IOException{
        String fileName = "College_Grade4.txt";
        String charset = "GB2312";
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName), charset));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if( line.length() == 0 ) continue;
            int idx = line.indexOf("\t");
            words.add( line.substring(0, idx ));
            meanings.add( line.substring(idx+1));
        }
        reader.close();
    }
}
