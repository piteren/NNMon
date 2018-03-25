/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import javafx.scene.paint.Color;

/**
 * java color utilities
 */
public class UColor {
    
    //returns Hex from RGB color
    public static String RGBtoHEX(Color c){
        int r = (int)(c.getRed()*255);
        int g = (int)(c.getGreen()*255);
        int b = (int)(c.getBlue()*255);
        return String.format("#%02x%02x%02x",r,g,b);
    }
    
}
