import Service.MIDIService;
import ServiceImpl.MIDIServiceImpl;

import javax.sound.midi.*;
import java.io.*;
import java.util.ArrayList;

public class ReadMIDI {

    private String path = "D:\\1.mid";

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
            System.out.println(tracks.length);
            Track track = tracks[0];
            track.ticks();
            MidiEvent event = track.get(0);
            //System.out.println(track.size());
            //System.out.println(event.getTick());    //得到一个事件的时间差delta time
            MidiMessage midiMessage = event.getMessage();
            System.out.println(midiMessage.getStatus());    //得到一个事件的状态码，即delta time之后的一个字节
            byte[] store = midiMessage.getMessage();
            for(int i = 0; i < store.length; i++){
                System.out.print(store[i]+" ");
                //System.out.print(Integer.valueOf(store[i]+"",2)+" ");
            }
            System.out.println();
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
                for(int m = count; m < oneTrack.size(); m++){
                    leftEvents.add(oneTrack.get(m));
                }
                ArrayList<Integer> res = midiService.getDeltaTime(leftEvents);
                int deltaTimeLen = res.get(0);
                int deltaTime = res.get(1);
                currentTime += deltaTime;
                System.out.print("当前tick：" + currentTime + " ");
                count += deltaTimeLen;
                String command = leftEvents.get(deltaTimeLen);                    //get the command of this event
                count++;
                char leftNybble = command.charAt(0);
                int channelNum = Integer.valueOf(command.charAt(1)+"",16) + 1;  //通道
                System.out.print("使用通道：" + channelNum + " ");               //在meta事件个系统事件时没有通道
                if(leftNybble == '8'){
                    System.out.println("音符关闭："+midiService.getMusicalNote(leftEvents.get(deltaTimeLen+1))+"；力度："+leftEvents.get(deltaTimeLen+2));
                    count += 2;
                } else if(leftNybble == '9'){
                    System.out.println("音符打开："+midiService.getMusicalNote(leftEvents.get(deltaTimeLen+1))+"；力度："+leftEvents.get(deltaTimeLen+2));
                    count += 2;
                } else if(leftNybble == 'a'){
                    System.out.println("触后音符："+midiService.getMusicalNote(leftEvents.get(deltaTimeLen+1))+"；力度："+leftEvents.get(deltaTimeLen+2));
                    count += 2;
                } else if(leftNybble == 'b'){
                    System.out.println("调换控制，控制号："+leftEvents.get(deltaTimeLen+1)+"；新值："+leftEvents.get(deltaTimeLen+2));
                    count += 2;
                } else if(leftNybble == 'c'){
                    System.out.println("改变程序，新的程序号："+leftEvents.get(deltaTimeLen+1));
                    count += 1;
                } else if(leftNybble == 'd'){
                    System.out.println("在通道后接触，管道号："+leftEvents.get(deltaTimeLen+1));
                    count += 1;
                } else if(leftNybble == 'e'){
                    System.out.println("滑音，音高低位："+leftEvents.get(deltaTimeLen+1)+"；音高高位："+leftEvents.get(deltaTimeLen+2));
                    count += 2;
                } else if(command.equals("ff")){
                    System.out.println("Meta事件的类型："+leftEvents.get(deltaTimeLen+1));
                    int metaDataLen = Integer.valueOf(leftEvents.get(deltaTimeLen+2),16);
                    count += 2;
                    count += metaDataLen;
                } else if(command.equals("f0")){
                    System.out.println("系统码事件");
                } else if(Integer.valueOf(command,16) >= 0 && Integer.valueOf(command,16) <= 127 && !lastCommand.equals("")){

                } else{
                    System.out.println(command + " not found!");
                }
                lastCommand = command;
            }
        }
    }
}
