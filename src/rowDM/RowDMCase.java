/*
 * 2018 (c) piteren
 */
package rowDM;

import utilities.UFileOperator;

import java.util.LinkedList;
import java.util.List;

/**
 * Row Data Modeling CASE
 * it has many rows of N data features and correct classification labels
 */

public class RowDMCase {

    protected int                   rowIX = 0;                          // current row index
    protected LinkedList<RowD>      myRowData = new LinkedList();       // case row data
    protected int                   numOfClasses = 0;                   // number of classes in case
    protected RowDMActor            myActor;                            // actor of this case

    protected class RowD {
        private LinkedList<Double>  inputFeatures = new LinkedList();   // data features
        private int                 cClassNum;                          // number of correct class for given data features

        protected RowD(LinkedList<Double> fLst, int cC){
            inputFeatures = fLst;
            cClassNum = cC;
        }

        protected List<Double> getInFeats(){
            return inputFeatures;
        }

        protected int getCClass(){
            return cClassNum;
        }
    }

    //constructor(row_data_filepath)
    public RowDMCase(String pth){

        UFileOperator file = new UFileOperator(pth);
        List<String> linie = file.readFile();

        int ix = 0;
        while(ix < linie.size()){
            String[]    fLine = linie.get(ix++).split("\\s+");
            String      cLine = linie.get(ix++);

            LinkedList<Double> fList = new LinkedList();
            for(String str: fLine) fList.add(Double.parseDouble(str));
            int cC = Integer.parseInt(cLine);

            if(numOfClasses < cC) numOfClasses = cC;

            myRowData.add(new RowD(fList, cC));
        }

    }

    //prepares and returns current state
    public List<Double> prepCurrentState(){
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
    protected void moveToNextRow(){
        rowIX++;
        if(rowIX == myRowData.size()) rowIX = 0;
    }
}