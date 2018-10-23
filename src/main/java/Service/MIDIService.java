package Service;

import java.util.ArrayList;

public interface MIDIService {

    public ArrayList<String> getSequence(String path);

    public ArrayList<ArrayList<String>> getTracks(ArrayList<String> store);

    public ArrayList<Integer> getDeltaTime(ArrayList<String> event);

    public String getMusicalNote(String  note);

    /**
     * return the midi command data length
     * @param command current command
     * @param lasCommand last command
     * @param offset the pos pointing to current command in leftEvents
     * @param leftEvents start with the delta time of current MIDI event
     * @return the midi command data length of current MIDI event
     */
    public int getEventLen(String command, String lasCommand, int offset, ArrayList<String> leftEvents);
}
