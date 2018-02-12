package utilities;

/**
 * double array utilities
 */
public class UArr {
    
    //returns index of max value
    public static int maxVix(double[] arr){
        int maxIX = 0;
        double maxVal =arr[maxIX];
        for(int i=1; i<arr.length; i++)
            if(arr[i] > maxVal){
                maxVal = arr[i];
                maxIX = i;
            }
        return maxIX;
    }
    //returns one-dimensional array (flattened from two-dimensional)
    public static double[] flat(double[][] arr){
        double[] fArr = new double[arr.length*arr[0].length];
        int count = 0;
        for(int i=0; i<arr.length; i++)
            for(int j=0; j<arr[0].length; j++)
                fArr[count++] = arr[i][j];
        return fArr;
    }
}