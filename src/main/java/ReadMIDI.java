import entity.ResultSequence;
import service.MIDIService;
import serviceImpl.MIDIServiceImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ReadMIDI {

    public void myRead(String path){
        MIDIService midiService = new MIDIServiceImpl();
        ArrayList<String> store = midiService.getSequence(path);
        ArrayList<ArrayList<String>> tracks = midiService.getTracks(store);
        int currentTime = 0;
        String lastCommand = "";
        int ticksPerQuarterNote = Integer.parseInt(store.get(12)+store.get(13),16);
        double microsecondsPerQuarterNote = 0;
        double microsecondsPerTick = 0;
        ArrayList<ResultSequence> sequences = new ArrayList<ResultSequence>();

        for(int i = 0; i < tracks.size(); i++){
            ArrayList<String> oneTrack = tracks.get(i);
            int count = 0;
            //假定是多轨同步
            currentTime = 0;

            while(count < oneTrack.size()){
                ResultSequence oneSequence = new ResultSequence();

                ArrayList<String> leftEvents = new ArrayList<String>();
                for(int m = count; m < oneTrack.size(); m++)
                    leftEvents.add(oneTrack.get(m));

                ArrayList<Integer> deltaTimeInfo = midiService.getDeltaTime(leftEvents);
                int deltaTimeLen = deltaTimeInfo.get(0);
                int deltaTime = deltaTimeInfo.get(1);
                //get the command of this event
                String command = leftEvents.get(deltaTimeLen);
                String printStr = "";

                //get the microsecondsPerTick
                if(command.equals("ff") && leftEvents.get(deltaTimeLen+1).equals("51")){
                    String str = leftEvents.get(deltaTimeLen+3) + leftEvents.get(deltaTimeLen+4) +
                            leftEvents.get(deltaTimeLen+5);
                    microsecondsPerQuarterNote = Integer.valueOf(str,16);
                    microsecondsPerTick = microsecondsPerQuarterNote/ticksPerQuarterNote;
                }
                //get the delta-time
                currentTime += deltaTime;
                if(microsecondsPerTick != 0)
                    System.out.print("当前时间(s): " + String.format("%.2f",(microsecondsPerTick*currentTime)/Math.pow(10,6)) + " ");
                else
                    System.out.print("当前tick: " + currentTime + " ");

                //get the channel
                int channelNum = Integer.valueOf(command.charAt(1)+"",16) + 1;
                //meta事件和系统事件没有通道
                if(!command.equals("ff") && !command.equals("f0") && Integer.valueOf(command,16) >= 128)
                    printStr = "使用通道: " + channelNum + " ";
                else if(Integer.valueOf(command,16) < 128 && !lastCommand.equals("ff") && !lastCommand.equals("f0"))
                    printStr = "使用通道: " + (Integer.valueOf(lastCommand.charAt(1)+"",16) + 1) + " ";
                System.out.print(printStr);

                ArrayList<String> eventInfo = midiService.getEventLen(command, lastCommand, deltaTimeLen, leftEvents);
                //assume that eventInfo.size() >= 1
                count = count + deltaTimeLen + Integer.parseInt(eventInfo.get(0));
                //save the music note
                if(eventInfo.size() == 2){
                    oneSequence.setCurrentTime(currentTime);
                    oneSequence.setMessage(eventInfo.get(1));
                    sequences.add(oneSequence);
                }
                //in case that the current command is lastCommand
                if(Integer.valueOf(command,16) >= 128){
                    count++;
                    lastCommand = command;
                }
            }
        }
        //sort the result sequence by time
        sequences = resultSequenceSort(sequences);
        //transform ticks into seconds
        for(ResultSequence sequence:sequences){
            double transToSec = (microsecondsPerTick * sequence.getCurrentTime())/Math.pow(10,6);
            BigDecimal bigDecimal = new BigDecimal(transToSec);
            sequence.setCurrentTime(bigDecimal.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        //save the result
        String savePath = "resultSequence/" +
                path.substring(path.indexOf('/')+1,path.indexOf('.'))+".txt";
        saveResultSequence(sequences,savePath);
    }

    private ArrayList<ResultSequence> resultSequenceSort(ArrayList<ResultSequence> sequences){
        for(int i = 0; i < sequences.size(); i++){
            for(int j = 0; j < sequences.size() - 1; j++){
                if(sequences.get(j).getCurrentTime() > sequences.get(j+1).getCurrentTime()){
                    double tempCurrentTime = sequences.get(j).getCurrentTime();
                    String tempMessage = sequences.get(j).getMessage();
                    sequences.get(j).setCurrentTime(sequences.get(j+1).getCurrentTime());
                    sequences.get(j).setMessage(sequences.get(j+1).getMessage());
                    sequences.get(j+1).setCurrentTime(tempCurrentTime);
                    sequences.get(j+1).setMessage(tempMessage);
                }
            }
        }
        return sequences;
    }

    private void saveResultSequence(ArrayList<ResultSequence> sequences, String path){
        try{
            File file = new File(path);
            FileWriter fileWriter = new FileWriter(file,false);
            for (ResultSequence sequence : sequences) {
                String str = "当前时间(s): " + sequence.getCurrentTime() + " " +
                        sequence.getMessage() + "\r\n";
                fileWriter.write(str);
            }
            fileWriter.flush();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
