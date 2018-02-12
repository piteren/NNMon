/*
 * 2017 (c) piteren
 */
package genX;

/**
 * interface 4 gen crossing objects
 */
public interface GXgenXinterface{
    
    //estimates crossing compatibility; 0: not compatible, 1-100: compatible and gives rank of similarity (100 means identity?)
    //int compatible4genX(genX objB);
    
    //crosses given parents
    public void genX(GXgenXinterface parA, GXgenXinterface parB);
}