/*
 * 2017 (c) piteren
 */
package trainer.toCase;

/**
 * interface of case_specific_feedback - object that keeps information about solver performance in case_specific_language
 * used for statistics and visualization
 * enables basic operations to perform by trainer/solver (merge, flush, toString)
 */
public abstract interface NTCaseSpecificFeedback{
    
    //merges (adds) given feedback to this
    public abstract void merge(NTCaseSpecificFeedback feedbackToAdd);
    //flushes (...clears, zeroes)
    public abstract void flush();
    //prepares string summary of feedback
    @Override
    public abstract String toString();
}