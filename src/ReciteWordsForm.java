import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
        JComponent comp = 速度Spinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
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
                    if((int)速度Spinner.getValue() < 0){
                        速度Spinner.setValue(1);
                    }
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
        速度Spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(status == 2){
                    if(timer.isRunning()){
                        timer.stop();
                    }
                    speed = (int)速度Spinner.getValue()>0?(int)速度Spinner.getValue():1;
                    timer.setDelay(speed * 1000);
                    timer.start();
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

    private List<String> words = new ArrayList<>();
    private List<String> meanings = new ArrayList<>();
    private int current = 0;
    private int speed = 1;
    private javax.swing.Timer timer;
    public void start() {
        new Thread(()->{
            try{
                readAll();
            }catch(IOException ex){}

            if((int)速度Spinner.getValue()>0){
                speed =(int)速度Spinner.getValue();
            }
            else
            {
                速度Spinner.setValue(1);
            }
            timer = new javax.swing.Timer( speed * 1000,(e)->{
                lblWord.setText( words.get(current) );
                lblMeaning.setText( meanings.get(current) );
                current++;
            });
            timer.start();
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
