/*
 * 2018 (c) piteren
 */

package rowDM;

import utilities.UFileOperator;
import java.util.LinkedList;
import java.util.List;

/**
 * Row Data Modeling Case
 * it has many rows of N data features and correct classification labels
 */

public class RowDMCase {

    private int                     rowIX = 0;                          // current row index
    private LinkedList<RowD>        myRowData = new LinkedList();       // case row data
    protected int                   numOfClasses = 0;                   // number of classes in case
    protected RowDMActor            myActor;                            // actor of this case

    protected class RowD {
        private LinkedList<Double>  inputFeatures = new LinkedList();   // data features
        private int                 cClassNum;                          // number of correct class for given data features

        protected RowD(LinkedList<Double> fLst, int cC){
            inputFeatures = fLst;
            cClassNum = cC;
        }

        protected LinkedList<Double> getInFeats(){ return inputFeatures; }

        protected int getCClass(){ return cClassNum; }
    }

    //constructor(row_data_filepath)
    public RowDMCase(String pth){

        UFileOperator file = new UFileOperator(pth);
        List<String> lines = file.readFile();

        int ix = 0;
        while(ix < lines.size()){
            String[]    fLine = lines.get(ix++).split("\\s+");
            String      cLine = lines.get(ix++);

            LinkedList<Double> fList = new LinkedList();
            for(String str: fLine) fList.add(Double.parseDouble(str));
            int cC = Integer.parseInt(cLine);

            if(numOfClasses < cC) numOfClasses = cC;

            myRowData.add(new RowD(fList, cC));
        }

    }

    //returns current state
    public LinkedList<Double> prepCurrentState(){

        return myRowData.get(rowIX).getInFeats();
    }

    //prepares feedback with reward and correct decision for given actor decision
    public RowMDFeedback prepFeedback(int actorChoosenClass){

        int corrC = myRowData.get(rowIX).getCClass();
        Double reward = 1.0;
        if( actorChoosenClass!=corrC ) reward = -1.0;

        return new RowMDFeedback(reward, corrC);
    }

    //moves case to next state
    public void moveToNextRow(){
        rowIX++;
        if(rowIX == myRowData.size()) rowIX = 0;
    }
}