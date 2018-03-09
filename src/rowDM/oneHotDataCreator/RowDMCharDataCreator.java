/*
 * 2018 (c) piteren
 */

package rowDM.oneHotDataCreator;

import utilities.UFileOperator;

import java.util.LinkedList;
import java.util.List;

/**
 * creator of one_hot row data for compress test
 */

public class RowDMCharDataCreator {

    private static int numOfClases = 15;

    //constructor(file)
    public RowDMCharDataCreator(String pth){

        UFileOperator file = new UFileOperator(pth);
        file.writeFile(textToRowLines(RowDMCharDataCreator.numOfClases));

    }

    public List<String> textToRowLines(int num){

        LinkedList<String> lines = new LinkedList();

        String  fLine,
                cLine;

        for(int ix=0; ix<num; ix++){
            double[] binArr = new double[num];
            binArr[ix] = 1.0;
            fLine = "";
            for(Double D: binArr){
                fLine += Double.toString(D) + " ";
            }
            fLine = fLine.substring(0, fLine.length()-1);
            cLine = Integer.toString(ix);
            lines.add(fLine);
            lines.add(cLine);
        }
        return lines;
    }
}
