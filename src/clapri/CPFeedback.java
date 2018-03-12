/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem Feedback interface
 * holds reward, correct_classification_index and eventually case_specific_additional_information
 */
public interface CPFeedback {

    // returns reward
    Double getReward();

    // returns correct classification index
    Integer getCorrClassfIX();

    // returns case specific additional information (or null if not present)
    default CPcsfaInfo getMyCPcsfaInfo(){
        return null;
    }
}