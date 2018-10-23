package Service;

import java.util.ArrayList;

public interface MIDIService {

    public ArrayList<String> getSequence(String path);

    public ArrayList<ArrayList<String>> getTracks(ArrayList<String> store);

    public ArrayList<Integer> getDeltaTime(ArrayList<String> event);

    public String getMusicalNote(String  note);

    public int getEventLen(String command, String lasCommand, int offset, ArrayList<String> leftEvents);
}
