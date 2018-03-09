/**
 * CLAPRI
 * Classification Problem Interfaces
 * Interfaces of objects building environment to solve classification problems
 * It contains:
 *      CPCase - case that holds state, takes actor decisions and prepares feedback
 *      CPActor - problem solver, using given state + other data and knowledge makes decision (correct class index)
 *      CPFeedback - feedback prepared by case for actor
 *      CPcsfaInfo - feedback in case_specific_language
 *      CPState - holds data describing case state
 *
 * 2018 (c) piteren
 */
package clapri;