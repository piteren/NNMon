/**
 * 2018 (c) piteren
 */

package rowDM.sampleDataCreator;

import diffUtils.UFileOperator;
import diffUtils.URand;

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
            for(int z=0; z<width; z++)
                data[z] = URand.gauss();
            fLine = "";
            for(Double D: data)
                fLine += Double.toString(Math.tanh(D)) + " ";
            fLine = fLine.substring(0, fLine.length()-1);
            mark = 0;
            if(     //data[0] <  0.3 &&
                    //data[0] > -0.2 &&
                    data[0]*data[1]*data[2] > 0)
            {
                mark = 1;
                sum++;
            }
            /*
            if(     data[0] < 10 &&
                    data[0]*data[1]>-3 &&
                    data[1]>-3 &&
                    data[2]>-10 &&
                    data[2]<7)
            {
                mark = 1;
                sum++;
            }
            */
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
