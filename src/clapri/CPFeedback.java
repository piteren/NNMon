/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem Feedback interface
 * holds reward, correct_classification_index and eventually case_specific_additional_information
 */
public interface CPFeedback {

    Double getReward();

    Integer getCorrClassfIX();

    CPcsfaInfo getMyCPcsfInfo();
}