/**
 * 2018 (c) piteren
 */

package neuralNetwork.test;

import neuralNetwork.NNDataProcessing;
import neuralNetwork.NNLearnParams;
import neuralNetwork.NNetwork;
import diffUtils.UFileOperator;

import java.util.LinkedList;
import java.util.List;

/**
 * Network testing class
 */
public class NetworkTest {



    public static void main(String[] args) {

        class InData{

            private LinkedList<double[]>    inF = new LinkedList<>();
            private LinkedList<Integer>     flag = new LinkedList<>();
            private int                     counter = 0;

            InData(String pth){

                UFileOperator   file = new UFileOperator(pth);
                List<String>    lines = file.readFile();

                int ix = 0;
                while(ix < lines.size()){
                    String[]    fLine = lines.get(ix++).split("\\s+");
                    String      cLine = lines.get(ix++);

                    LinkedList<Double> fList = new LinkedList();
                    for(String str: fLine) fList.add(Double.parseDouble(str));
                    int cC = Integer.parseInt(cLine);

                    double[] arr = new double[fList.size()];
                    for(int i=0; i<fList.size(); i++)
                        arr[i] = fList.get(i);
                    inF.add(arr);
                    flag.add(cC);
                }
            }

            double[] getData(){ return inF.get(counter); }

            Integer getFlag(){ return flag.get(counter); }

            void move(){ if(++counter == inF.size()) counter = 0; }
        }

        NNetwork net = new NNetwork(new NNLearnParams(), "NETs/netRDMnormTestffwd.txt");
        InData dat = new InData("RDMdata/normTData.txt");

        System.out.println("net : " + net.nParam() + "-" + net.nNodes());

        for(int i=0; i<10; i++){
            double[] datIn = dat.getData();
            double[] netOut = net.runFWD(datIn);

            System.out.println(NNDataProcessing.lossSVM(netOut, dat.getFlag(), 0.1));
            double[] gradSVM = NNDataProcessing.gradSVM(netOut, dat.getFlag(), 0.1);

            LinkedList<double[]> grList = new LinkedList<>();
            grList.add(gradSVM);
            net.runBWD(grList);

            dat.move();
        }
    }
}