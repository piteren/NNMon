/**
 * 2018 (c) piteren
 */

package rowDM.sampleDataCreator;

import utilities.UFileOperator;
import utilities.URand;

import java.util.LinkedList;
import java.util.List;

public class NormTestDataCreator {

    //constructor(file)
    public NormTestDataCreator(String pth){

        UFileOperator file = new UFileOperator(pth);
        file.writeFile(textToRowLines(1000));
    }

    public List<String> textToRowLines(int num){

        LinkedList<String> lines = new LinkedList();

        String  fLine,
                cLine;
        int mark,
            sum = 0;

        int width = 3;
        for(int ix=0; ix<num; ix++){
            double[] data = new double[width];
            data[0] = URand.gauss();
            data[1] = URand.gauss();
            data[2] = URand.gauss();
            fLine = "";
            for(Double D: data){
                //fLine += Double.toString(Math.tanh(D/15)) + " ";
                fLine += Double.toString(D) + " ";
            }
            fLine = fLine.substring(0, fLine.length()-1);
            mark = 0;
            ///*
            if(     data[0] < 0.1 &&
                    data[0]*data[1]>-0.3 &&
                    //data[1]>-0.3 &&
                    data[2]>-0.8 &&
                    data[2]<0.7)
            {
                mark = 1;
                sum++;
            }
            //*/
            cLine = Integer.toString(mark);
            lines.add(fLine);
            lines.add(cLine);
        }
        System.out.println(sum);
        return lines;
    }

    public static void main(String[] args) {
        NormTestDataCreator nData = new NormTestDataCreator("RDMdata/normTData.txt");
    }
}
