import Service.MIDIService;
import ServiceImpl.MIDIServiceImpl;

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

        for(int i = 0; i < tracks.size(); i++){

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

                if(command.equals("ff") && leftEvents.get(deltaTimeLen+1).equals("51")){
                    String str = leftEvents.get(deltaTimeLen+3) + leftEvents.get(deltaTimeLen+4) +
                            leftEvents.get(deltaTimeLen+5);
                    microsecondsPerQuarterNote = Integer.valueOf(str,16);
                    microsecondsPerTick = microsecondsPerQuarterNote/ticksPerQuarterNote;
                }                                                              //get the microsecondsPerTick
                //get the delta-time
                currentTime += deltaTime;
                if(microsecondsPerTick != 0)
                    System.out.print("当前时间(s)：" + String.format("%.2f",(microsecondsPerTick*currentTime)/Math.pow(10,6)) + " ");
                else
                    System.out.print("当前tick：" + currentTime + " ");

                //get the channel
                int channelNum = Integer.valueOf(command.charAt(1)+"",16) + 1;  //通道
                if(!command.equals("ff") && !command.equals("f0") && Integer.valueOf(command,16) >= 128)
                    System.out.print("使用通道：" + channelNum + " ");            //meta事件和系统事件没有通道
                else if(Integer.valueOf(command,16) < 128 && !lastCommand.equals("ff") && !lastCommand.equals("f0"))
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
