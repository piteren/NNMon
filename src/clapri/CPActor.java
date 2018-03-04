/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem Actor interface
 */
public interface CPActor {

    // takes given case_state and makes classification (returns class index)
    int makeDecision(CPState stat);

    // takes given case feedback and uses it to build knowledge
    void takeFeedback(CPFeedback feedb);
}