import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReadMP3 {
    public void read(){
        double value = 80 / 100.0;
        String path = "D:\\music_00002.mp3";
        try{
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
            AudioFormat format = stream.getFormat();
            System.out.println("具有此格式的声音每秒播放或录制的样本数："+ format.getSampleRate());
            System.out.println("每个具有此格式的声音帧包含的字节数："+ format.getFrameSize());
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,format.getSampleRate(), 16, format.getChannels(),format.getChannels() * 2, format.getSampleRate(), false);
                stream = AudioSystem.getAudioInputStream(format, stream);
            }
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(stream.getFormat(), sourceDataLine.getBufferSize());
            sourceDataLine.start();
            List<Control> cons = new ArrayList<Control>();
            Control[] ss = sourceDataLine.getControls();
            Control t = null;
            for (int i=0;i<ss.length;i++){
                t = ss[i];
                cons.add(t);
                System.out.println("控件类型："+t.getType());
            }
            FloatControl adin = (FloatControl)sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            System.out.println("当前值："+ adin.getValue());
            System.out.println("分辨率或粒度："+ adin.getPrecision());
            System.out.println("中点值的标签："+ adin.getMidLabel());
            System.out.println("最大值得标签："+ adin.getMaxLabel());
            System.out.println("最大值："+ adin.getMaximum());
            System.out.println("最小值得标签："+ adin.getMinLabel());
            System.out.println("最小值："+ adin.getMinimum());
            System.out.println("单位："+ adin.getUnits());
            System.out.println("音量等级："+ sourceDataLine.getLevel());
            float dB = (float) (Math.log(value)/Math.log(10.0)*20.0);
            System.out.println("计算后："+ dB);
            adin.setValue(dB);
            int numRead = 0;
            byte[] buf = new byte[sourceDataLine.getBufferSize()];
            System.out.println("字节流："+ buf.toString());
            System.out.println("字节流长度："+ buf.length);
            while ((numRead = stream.read(buf, 0, buf.length)) >= 0) {
                int offset = 0;
                while (offset < numRead) {
                    offset += sourceDataLine.write(buf, offset, numRead - offset);
                }
                System.out.println("音频数据中的当前位置(样本帧/微秒)："+sourceDataLine.getFramePosition() + " " + sourceDataLine.getMicrosecondPosition());
            }
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
