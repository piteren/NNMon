package deepLearn;

public interface DLrunFBlearnInterface{

    //passes data forward
    public void runFWD();
    //passes gradient backward
    public void runBWD(int h);
    //updates parameters with calculated gradients
    public void updateLearnableParams();
}