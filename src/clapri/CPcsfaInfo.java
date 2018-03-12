/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem case specific feedback additional Information
 * holds actor performance information in case_specific_language
 * implements basic operations like merge, flush, toString
 * this interface is not obligatory - you may put null instead of class object (= no case_spec_feedback)
 */
interface CPcsfaInfo {

    // merges given info to this
    void merge(CPcsfaInfo csfIToMrg);

    // flushes (clears) information
    void flush();

    // string summary of object
    @Override
    String toString();
}