/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem Feedback interface
 * holds reward, correct_classification_index and eventually additional_case_specific_information
 */
public interface CPFeedback {

    Double getReward();

    Integer getCorrClassfIX();

    CPcsfInfo getMyCPcsfInfo();
}