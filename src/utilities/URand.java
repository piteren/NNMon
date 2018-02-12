/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.Random;

/**
 * @author ppp
 */
public class URand{
    //random double gaussian 0 mean 1 sdDev
    public static double gauss(){                   
        Random generator = new Random();
        return generator.nextGaussian();
    }
    //random int <0..ix> 0 inclusive ix exclusive
    public static int i(int ix){                   
        Random generator = new Random();
        int val = 0;
        if(ix>0) val = generator.nextInt(ix);
        return val;
    }
    //random double <0..1> 0 inclusive 1 exclusive
    public static double one(){                     
        Random generator = new Random();
        return generator.nextDouble();
    }
}//URand
