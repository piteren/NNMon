/*
 * 2017 (c) piteren
 */
package trainer.toCase;

/**
 * interface of case_specific_feedback class
 * it keeps information about solver performance in case_specific_language
 * used for statistics and visualization
 * enables basic operations to perform by trainer/solver (merge, flush, toString)
 */
public interface NTCaseSpecificFeedback{
    
    //merges (adds) given feedback to this
    void merge(NTCaseSpecificFeedback feedbackToAdd);

    //flushes feedback(...clears, zeroes)
    void flush();

    //prepares string summary of feedback
    @Override
    String toString();
}