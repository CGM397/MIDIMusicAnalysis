import Service.MIDIService;
import ServiceImpl.MIDIServiceImpl;

import javax.sound.midi.*;
import java.io.*;
import java.util.ArrayList;

public class ReadMIDI {

    private String path = "D:\\3.mid";

    public void readUseAPI(){
        try{
            Sequence sequence = MidiSystem.getSequence(new File(path));

            long len = sequence.getMicrosecondLength();
            int trackCount = sequence.getTracks().length;
            float divType = sequence.getDivisionType();
            int resolution = sequence.getResolution();

            //tracks中包含所有轨道，一个track包含该轨道的所有事件，一个事件可以用getMessage方法来获取信息,
            //一个MidiMessage可以用getMessage方法来得到二进制文件
            Track[] tracks = sequence.getTracks();
            Track track = tracks[0];
            track.ticks();
            MidiEvent event = track.get(0);
            //System.out.println(track.size());
            //System.out.println(event.getTick());    //得到一个事件的时间差delta time
            MidiMessage midiMessage = event.getMessage();
            System.out.println(midiMessage.getStatus());    //得到一个事件的状态码，即delta time之后的一个字节
            //byte[] store = midiMessage.getMessage();
            //for(int i = 0; i < store.length; i++){
                //System.out.print(store[i]+" ");
                //System.out.print(Integer.valueOf(store[i]+"",2)+" ");
            //}
        }catch (InvalidMidiDataException e1){
            e1.printStackTrace();
        }catch (IOException e2){
            e2.printStackTrace();
        }
    }

    public void myRead(){
        MIDIService midiService = new MIDIServiceImpl();
        ArrayList<String> store = midiService.getSequence(path);
        ArrayList<ArrayList<String>> tracks = midiService.getTracks(store);
        int currentTime = 0;
        String lastCommand = "";

        for(int i = 0; i < tracks.size(); i++){
            //for(int j = 0; j < tracks.get(i).size(); j++){
                //System.out.print(tracks.get(i).get(j) + " ");
            //}
            //System.out.println();
            //System.out.println(tracks.get(i).size());
            //System.out.println(midiService.getDeltaTime(tracks.get(i)));

            ArrayList<String> oneTrack = tracks.get(i);
            int count = 0;
            currentTime = 0;        //假定是多轨同步
            while(count < oneTrack.size()){
                ArrayList<String> leftEvents = new ArrayList<String>();
                for(int m = count; m < oneTrack.size(); m++)
                    leftEvents.add(oneTrack.get(m));

                ArrayList<Integer> deltaTimeInfo = midiService.getDeltaTime(leftEvents);
                int deltaTimeLen = deltaTimeInfo.get(0);
                int deltaTime = deltaTimeInfo.get(1);
                String command = leftEvents.get(deltaTimeLen);                    //get the command of this event

                //get the delta-time
                currentTime += deltaTime;
                System.out.print("当前tick：" + currentTime + " ");

                //get the channel
                int channelNum = Integer.valueOf(command.charAt(1)+"",16) + 1;  //通道
                if(!command.equals("ff") && !command.equals("f0") && Integer.valueOf(command,16) >= 128)
                    System.out.print("使用通道：" + channelNum + " ");            //meta事件和系统事件没有通道
                else if(Integer.valueOf(command,16) < 128)
                    System.out.print("使用通道：" + (Integer.valueOf(lastCommand.charAt(1)+"",16) + 1) + " ");

                count = count + deltaTimeLen + midiService.getEventLen(command, lastCommand, deltaTimeLen, leftEvents);
                if(Integer.valueOf(command,16) >= 128){
                    count++;
                    lastCommand = command;
                }
            }
        }
    }
}
