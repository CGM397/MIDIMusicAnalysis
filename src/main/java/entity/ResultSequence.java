package entity;

public class ResultSequence {

    private double currentTime;

    private String message;

    public ResultSequence(){ }

    public ResultSequence(double currentTime, String message){
        this.currentTime = currentTime;
        this.message = message;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
