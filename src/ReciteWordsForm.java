import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
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
    private JButton JumpOverButton;
    private JComboBox ModeComboBox;
    private JSpinner SpeedSpinner;
    private JPanel WordPanel;
    private JPanel RootPanel;
    private JButton StartButton;
    private JButton PauseButton;
    private PlayStatus status = PlayStatus.INIT;
    public enum PlayStatus{
        INIT,
        START,
        PAUSE,
        STOP
    }
    private final int MAXSPEED = 10;
    private final int MINSPEED = 1;
    public ReciteWordsForm() {
        JComponent comp = SpeedSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        StartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(status == PlayStatus.INIT){
                    WordPanel.setLayout(new FlowLayout());
                    WordPanel.add(lblWord);
                    WordPanel.add(lblMeaning);
                    status = PlayStatus.STOP;
                    JumpOverButton.setEnabled(true);
                }
                switch (status) {
                    case STOP: {
                        if ((int) SpeedSpinner.getValue() < MINSPEED) {
                            SpeedSpinner.setValue(MINSPEED);
                        } else if ((int) SpeedSpinner.getValue() > MAXSPEED) {
                            SpeedSpinner.setValue(MAXSPEED);
                        }
                        start();
                        status = PlayStatus.START;
                        StartButton.setText("Stop");
                        PauseButton.setEnabled(true);
                        break;
                    }
                    case START: {
                        stop();
                        heartBeat = 0;
                        StartButton.setText("START");
                        status = PlayStatus.STOP;
                        PauseButton.setEnabled(false);
                        PauseButton.setText("Pause");
                        break;
                    }
                    case PAUSE: {
                        StartButton.setText("START");
                        status = PlayStatus.STOP;
                        PauseButton.setEnabled(false);
                        PauseButton.setText("Pause");
                        break;
                    }
                }
            }
        });
        SpeedSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if((int) SpeedSpinner.getValue() < MINSPEED){
                    SpeedSpinner.setValue(MINSPEED);
                }
                else if((int) SpeedSpinner.getValue() > MAXSPEED){
                    SpeedSpinner.setValue(MAXSPEED);
                }
                if(status == PlayStatus.START && timer != null){
                    speed = (int) SpeedSpinner.getValue();
                    progressBar1.setMaximum(MAXSPEED-speed+1);
                }
            }
        });
        PauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(status == PlayStatus.START){
                    stop();
                    status = PlayStatus.PAUSE;
                    PauseButton.setText("Resume");
                }
                else if(status == PlayStatus.PAUSE){
                    start();
                    status = PlayStatus.START;
                    PauseButton.setText("Pause");
                }
            }
        });
        JumpOverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MoveToNextWord();
                heartBeat = 0;
            }
        });
    }

    public static void main(String[] args) {
        ReciteWordsForm frame = new ReciteWordsForm();
        frame.setTitle("Recite Words");
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
    private int speed = MINSPEED;
    private javax.swing.Timer timer;
    private int heartBeat = 0;
    private void start() {
        if(status != PlayStatus.PAUSE && status!=PlayStatus.STOP) return;
        progressBar1.setMaximum(MAXSPEED-speed+1);
        new Thread(()->{
            try{
                readAll();
            }catch(IOException ex){}
            timer = new javax.swing.Timer( 100,(e)->{
                progressBar1.setValue(heartBeat);
                if(heartBeat >= (MAXSPEED-speed+1)){
                    MoveToNextWord();
                    heartBeat = 0;
                }else{
                    heartBeat++;
                }
            });
            timer.start();
        }).start();
    }
    private void stop(){
        if(status == PlayStatus.START && timer != null){
            timer.stop();
        }
    }
    private void MoveToNextWord() {
        lblWord.setText( words.get(current) );
        lblMeaning.setText( meanings.get(current) );
        current++;
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
