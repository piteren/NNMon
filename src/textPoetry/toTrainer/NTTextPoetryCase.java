package textPoetry.toTrainer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import textPoetry.TextPoetryCase;
import trainer.NetTrainerAISolver;
import trainer.toCase.NTActor;
import trainer.toCase.NTCase;
import utilities.UFileOperator;

/**
 * trained text poetry case
 */
public class NTTextPoetryCase extends TextPoetryCase implements NTCase {

    private final String        path;                                                   //file with text
    private final static int    CASE_NUM_OF_ACTORS = 1;
    protected final int         stratingCharNum;                                        //index of char where we start
    protected final int         numOfChars;                                             //number of chars in Envy (width of alphabet ..till last char)
    
    //constructor(filepath)
    public NTTextPoetryCase(String pth){
        super( listToString( (new UFileOperator(pth)).readFile() ) );
        path = pth;

        //calculate starting_char and number_of_chars
        int start = (int)text.charAt(0),
            end = start,
            tempVal;
        for(int i=1; i<text.length(); i++){
            tempVal = (int)text.charAt(i);
            if(tempVal<start) start = tempVal;
            if(tempVal>end) end = tempVal;
        }
        stratingCharNum = start;
        numOfChars = end-start+1;
        System.out.println("Num of chars: "+numOfChars);
    }
    
    private static String listToString(List<String> lSt){
        String sConnected = "";
        for(int i=0; i<lSt.size(); i++){
            sConnected += (lSt.get(i));
            sConnected += " ";
        }
        sConnected = sConnected.substring(0,sConnected.length()-1);
        return sConnected;
    }
    
    @Override
    public NTCase duplicate(){
        return new NTTextPoetryCase(path);
    }
    
    @Override
    public int caseNumOfActors(){
        return CASE_NUM_OF_ACTORS;
    }
    @Override
    public int caseNumOfClasses(){
        return numOfChars;
    }
    @Override
    public boolean actDecisionChangesState(){
        return false;
    }
    
    @Override
    public void takeSolvers(LinkedList<NetTrainerAISolver> solvers){
        myActor = new NTTextPoetryActor(this, solvers.get(0));
    }
    
    @Override
    public LinkedList<NTActor> getMyActors(){
        LinkedList<NTActor> myAct = new LinkedList();
        myAct.add( (NTActor)myActor );
        return myAct;
    }
    @Override
    public int[] currentPossibleDecisions(){
        int[] cPD = new int[numOfChars];
        Arrays.fill(cPD,1);
        return cPD;
    }
    
    @Override
    public void moveCaseToNextState(int decIX){
        moveToNextState();
    }       
    
    @Override
    public void sampleTestRun(){
        int dec = (int)prepCurrentState()-stratingCharNum;
        double[] solverIN = new double[numOfChars];
        double[] solverOUT;
        
        for(int i=0; i<100; i++){
            Arrays.fill(solverIN,0.0);
            solverIN[dec] = 1.0;
            solverOUT = ((NTActor)myActor).getMySolver().runFWD(solverIN);
            dec = ((NTActor)myActor).intpSolverOUT(solverOUT);
            System.out.print( (char)( stratingCharNum + dec ) );
        }
        System.out.println();
    }
}