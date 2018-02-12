/*
 * 2017 (c) piteren
 */
package utilities;

import javafx.beans.property.SimpleDoubleProperty;

/**
 * double simple property with interface to calculate linear value
 * for holding LOGARITHMIC VALUE
 * has methods to return_values or format_them_as_string
 * in both INT and DOUBLE types
 */
public class ULogDoubleProperty extends SimpleDoubleProperty{
    
    private final TSform myTsForm;
    
    //constructor(tsForm, initVal)
    public ULogDoubleProperty(TSform myF, double val){
        super(val);
        myTsForm = myF;
    }
    
    //to string option
    public enum TSform{
        INT,
        DBL
    }
    
    public TSform getTsForm(){
        return myTsForm;
    }

    //returns string with integer linear value
    public String linIntegerValueToString(){
        return String.valueOf((int)Math.pow(10,getValue()));
    }
    //returns formatted string with double linear value
    public String linDoubleValueToString(){
        return String.format("%1.1e",Math.pow(10,getValue()));
    }
    //returns integer linear value
    public int getLinIntegerValue(){
        return (int)Math.pow(10,getValue());
    }
    //returns double linear value
    public double getLinDoubleValue(){
        return Math.pow(10,getValue());
    }
}