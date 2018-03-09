/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem case specific feedback additional Information
 * holds actor performance information in case_specific_language
 * implements basic operations like merge, flush, toString
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