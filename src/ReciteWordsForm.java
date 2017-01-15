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
import java.util.NoSuchElementException;
import java.util.Random;

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

        try{
            readAll();
        }catch(IOException ex){}
        InitLearnControls();

        StartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                        JumpOverButton.setEnabled(true);
                        ModeComboBox.setEnabled(false);
                        break;
                    }
                    case START: {
                        stop();
                        InitStopControls();
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
        ModeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
                if(ModeComboBox.getSelectedItem().toString().equals("Learn")){
                    ChangeToLearnControls();
                }else{
                    InitReciteControls();
                }
            }
        });
        btnMeaning.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblResult.setForeground(Color.green);
                lblResult.setText("Correct!");
            }
        });
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblResult.setForeground(Color.red);
                lblResult.setText("Wrong!");
            }
        };
        btnMeaning1.addActionListener(actionListener);
        btnMeaning2.addActionListener(actionListener);
        btnMeaning3.addActionListener(actionListener);
    }

    private void InitLearnControls() {
        lblWord.setHorizontalAlignment(JLabel.CENTER);
        lblMeaning.setHorizontalAlignment(JLabel.CENTER);
        WordPanel.setLayout(new GridLayout(6,1));
        WordPanel.add(lblWord);
        WordPanel.add(lblMeaning);
        status = PlayStatus.STOP;
        JumpOverButton.setEnabled(false);
        progressBar1.setValue(0);
    }
    private void InitStopControls(){
        heartBeat = 0;
        StartButton.setText("START");
        status = PlayStatus.STOP;
        PauseButton.setEnabled(false);
        PauseButton.setText("Pause");
        JumpOverButton.setEnabled(false);
        ModeComboBox.setEnabled(true);
    }
    private void ChangeToLearnControls(){
        WordPanel.remove(btnMeaning);
        WordPanel.remove(btnMeaning1);
        WordPanel.remove(btnMeaning2);
        WordPanel.remove(btnMeaning3);
        WordPanel.remove(lblResult);
        WordPanel.add(lblMeaning);
        status = PlayStatus.STOP;
        InitStopControls();
    }
    private void InitReciteControls() {
        btnMeaning.setHorizontalAlignment(JButton.CENTER);
        btnMeaning1.setHorizontalAlignment(JButton.CENTER);
        btnMeaning2.setHorizontalAlignment(JButton.CENTER);
        btnMeaning3.setHorizontalAlignment(JButton.CENTER);
        WordPanel.remove(lblMeaning);
        status = PlayStatus.STOP;
        InitStopControls();
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
    JButton btnMeaning = new JButton("meaning");
    JButton btnMeaning1 = new JButton("meaning1");
    JButton btnMeaning2 = new JButton("meaning2");
    JButton btnMeaning3 = new JButton("meaning3");
    JLabel lblResult = new JLabel("");

    private List<String> words = new ArrayList<>();
    private List<String> meanings = new ArrayList<>();
    private RandomInt randomIntIndex;
    private int current = 0;
    private int speed = MINSPEED;
    private javax.swing.Timer timer;
    private int heartBeat = 0;
    private void start() {
        if(status != PlayStatus.PAUSE && status!=PlayStatus.STOP) return;
        progressBar1.setMaximum(MAXSPEED-speed+1);
        new Thread(()->{
            MoveToNextWord();
            timer = new javax.swing.Timer( 1000,(e)->{
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
        current = randomIntIndex.getRandomIndex();
        lblWord.setText( words.get(current) );
        if(ModeComboBox.getSelectedItem().toString().equals("Learn")){
            lblMeaning.setText( meanings.get(current) );
        }else{
            lblResult.setText("");
            btnMeaning.setText(meanings.get(current));
            if(words.size()<4) {
                lblResult.setText("Not Enough Words!");
                WordPanel.add(lblResult);
                return;
            }
            RandomInt otherMeanings = new RandomInt(words.size());
            btnMeaning1.setText(meanings.get(otherMeanings.getRandomIndex(current)));
            btnMeaning2.setText(meanings.get(otherMeanings.getRandomIndex(current)));
            btnMeaning3.setText(meanings.get(otherMeanings.getRandomIndex(current)));
            RandomInt seeds = new RandomInt(4);
            for(int i = 0;i<4;i++){
                int now = seeds.getRandomIndex();
                switch (now){
                    case 0:{
                        WordPanel.add(btnMeaning);
                        break;
                    }
                    case 1:{
                        WordPanel.add(btnMeaning1);
                        break;
                    }
                    case 2:{
                        WordPanel.add(btnMeaning2);
                        break;
                    }
                    case 3:{
                        WordPanel.add(btnMeaning3);
                        break;
                    }
                }
            }
            WordPanel.add(lblResult);
        }
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
            String meaningsPlusPronunciation = line.substring(idx+1);
            int idx2 = meaningsPlusPronunciation.indexOf("\t");
            meanings.add( meaningsPlusPronunciation.substring(0,idx2));
        }
        randomIntIndex = new RandomInt(words.size());
        reader.close();
    }
    private class RandomInt{
        private int[] indexs;
        private int currentSize;
        public RandomInt(int size){
            if(size<0) {
                throw new NoSuchElementException();
            }
            indexs = new int[size];
            currentSize = size;
            for(int i =0;i<size;i++){
                indexs[i] = i;
            }
        }
        private boolean isEmpty(){
            return currentSize <=0 ;
        }
        public int getRandomIndex(){
            if(isEmpty()){
                throw new NoSuchElementException();
            }
            int targetid = new Random().nextInt(currentSize);
            int item = indexs[targetid];
            indexs[targetid] = indexs[currentSize - 1];
            currentSize--;
            return item;
        }
        public int getRandomIndex(int jumpElement){
            int result = getRandomIndex();
            if(result == jumpElement){
                result = getRandomIndex();
            }
            return result;
        }
    }
}
