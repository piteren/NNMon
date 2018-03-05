/**
 * 2018 (c) piteren
 */

package clapri;

/**
 * Classification Problem case specific feedback Information
 * holds actor performance in case_specific_language
 * implements basic operations like merge, flush, toString
 */
interface CPcsfInfo{

    // merges given info to this
    void merge(CPcsfInfo csfIToMrg);

    // flushes (clears) information
    void flush();

    // string summary of object
    @Override
    String toString();
}