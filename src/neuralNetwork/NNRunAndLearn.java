/**
 * 2017 (c) piteren
 */

package neuralNetwork;

/**
 * main interface of forward-backward run-learn objects
 * we assume that data for FWD or BWD run is present (ready), otherwise run not starts (without error)
 */
public interface NNRunAndLearn {

    // runs data forward
    void runFWD();

    // runs gradient backward on the h history level
    void runBWD(int h);

    // updates internal parameters with calculated gradients
    void updateLearnableParams();

    // connects this_ralObj with next_ralObj with time_offset
    void connectWithNext(NNRunAndLearn nextRalObj, int tOff);
}