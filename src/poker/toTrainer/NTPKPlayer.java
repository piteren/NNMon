/*
 * 2017 (c) piteren
 */
package poker.toTrainer;

import poker.PKDecision;
import poker.PKPlayer;
import poker.PKPlayerStats;
import trainer.NTaiSolver;
import trainer.NTCaseFeedback;
import utilities.UArr;

public class NTPKPlayer extends PKPlayer {
    
    NTaiSolver mySolver;                                                //aiSolver that decides for this player
    
    //constructor
    public NTPKPlayer(NTaiSolver solver){
        mySolver = solver;
    }
    
    //prepares solver input data
    public double[] prepareSolverIN(){
        //zeroes in
        double[] NNinData = new double[16];                                     //size does not matter a lot, net takes first N (N==number of input nodes)
        
        //card A
        NNinData[0]=(double)cA().v()/13;
        NNinData[1]=(double)cA().c()/4;
        //card B
        NNinData[2]=(double)cB().v()/13;
        NNinData[3]=(double)cB().c()/4;
        //5 table cards FTR
        if(myTable.tableCards.size()>2){
            NNinData[4]=(double)myTable.tableCards.get(0).v()/13;
            NNinData[5]=(double)myTable.tableCards.get(0).c()/4;
            NNinData[6]=(double)myTable.tableCards.get(1).v()/13;
            NNinData[7]=(double)myTable.tableCards.get(1).c()/4;
            NNinData[8]=(double)myTable.tableCards.get(2).v()/13;
            NNinData[9]=(double)myTable.tableCards.get(2).c()/4;
        }
        if(myTable.tableCards.size()>3){
            NNinData[10]=(double)myTable.tableCards.get(3).v()/13;
            NNinData[11]=(double)myTable.tableCards.get(3).c()/4;
        }
        if(myTable.tableCards.size()>4){
            NNinData[12]=(double)myTable.tableCards.get(4).v()/13;
            NNinData[13]=(double)myTable.tableCards.get(4).c()/4;
        }
        
        //table pot
        NNinData[14]=(double)myTable.actPot/(myTable.players.size()*myTable.maxStack);
        //my actual stack
        NNinData[15]=(double)stack/myTable.maxStack;
        
        /*
        //decision possibility and value
        for(PKDecision d: myPossibleDecisions()){
            if( d.T=='X' ){
                NNinData[16]=1.0;
                NNinData[17]=(double)d.val / myTable.maxStack;
            }
            if( d.T=='C' ){
                NNinData[18]=1.0;
                NNinData[19]=(double)d.val / myTable.maxStack;   
            }
            if( d.T=='B' ){
                NNinData[20]=1.0;
                NNinData[21]=(double)d.val / myTable.maxStack;   
            }
            if( d.T=='R' ){
                NNinData[22]=1.0;
                NNinData[23]=(double)d.val / myTable.maxStack;   
            }
            if( d.T=='S' ){
                NNinData[24]=1.0;
                NNinData[25]=(double)d.val / myTable.maxStack;   
            }
            if( d.T=='A' ){
                NNinData[26]=1.0;
                NNinData[27]=(double)d.val / myTable.maxStack;
            }
        }

        //my starting (preflop) position at table
        NNinData[28]=(double)myTable.playersD.indexOf(this)/(myTable.players.size()-1);  
        //amount of players 4 pot and my position among deciding players
        NNinData[29]=(double)myTable.playersP.size()/myTable.players.size();
        NNinData[30]=(double)(myTable.playersD.indexOf(this)+1)/(myTable.playersD.size());
        
        //last decision on that river (if present) type and value
        if(!this.myTable.handDecisions.isEmpty()){
            PKDecision lHD = this.myTable.handDecisions.getLast();
            if(lHD.dTS==myPossibleDecisions().get(0).dTS){
                int type = 0;
                if(lHD.T=='X') type = 1;
                if(lHD.T=='C') type = 2;
                if(lHD.T=='B') type = 3;
                if(lHD.T=='R') type = 4;
                if(lHD.T=='S') type = 5;
                if(lHD.T=='A') type = 6;
                NNinData[31] = (double)type/6;
                NNinData[32] = (double)lHD.val / myTable.maxStack;
            }
        }
        */

        //for(int i=0; i<NNinData.length; i++) NNinData[i] = 1.5*(NNinData[i]-0.33);  //TEMPORARY rescale to <-1,1>
        //for(int i=0; i<NNinData.length; i++) NNinData[i] = URand.one();

        return NNinData;
    }
    
    //interprets solver output data
    public PKDecision interpretSolverOUT(double[] solverOUT){
        //[0] - F = does not want to play (may also X for free)
        //[1] - C = wants to stay (flat) with no aggression (may also X for free)
        //[2] - B = wants to bet 2.2 (BRS)
        //[3] - A = wants to go allin
        PKDecision dNNchoosen=null;
        int selAtype = UArr.maxVix(solverOUT);                                                              //choose decision (by NN)
        //F or X for free
        if(selAtype==0){
            for(PKDecision d: myPossibleDecisions()) if(d.T=='X') dNNchoosen=d;                             //look for X
            if(dNNchoosen==null) for(PKDecision d: myPossibleDecisions()) if(d.T=='F') dNNchoosen=d;        //if there is no X go F
        }
        //C or X for free, if not then F (no aggression prefered)
        if(selAtype==1){
            for(PKDecision d: myPossibleDecisions()) if(d.T=='X') dNNchoosen=d;                             //look for X
            if(dNNchoosen==null) for(PKDecision d: myPossibleDecisions()) if(d.T=='C') dNNchoosen=d;        //if there is no X look for C
            if(dNNchoosen==null) for(PKDecision d: myPossibleDecisions()) if(d.T=='F') dNNchoosen=d;        //there is no X nor C go F
        }
        if(selAtype==2){                                                                                    //wants to bet/raise
            for(PKDecision d: myPossibleDecisions()) if(d.T=='B' || d.T=='R' || d.T=='S') dNNchoosen=d;     //return B R or S
            if(dNNchoosen==null){                                                                           //F,X,C or A only as option
                for(PKDecision d: myPossibleDecisions()) if(d.T=='A') dNNchoosen=d;                         //return A
            }
            else{
                double val=dNNchoosen.val*1.1;
                if(val>=stack) for(PKDecision d: myPossibleDecisions()) if(d.T=='A') dNNchoosen=d;          //wants to raise but sizes bet for allin
                else dNNchoosen.val=(int)val;
            }
        }
        if(selAtype==3)                                                                                     //wants to A
            for(PKDecision d: myPossibleDecisions()) if(d.T=='A') dNNchoosen=d;         
        
        return dNNchoosen;
    }

    /*
    // OLD VERSION with 7 classes
    // interprets solver output data 7 decisions old version
    public PKDecision interpretSolverOUT_7(double[] solverOUT){
        PKDecision dNNchoosen=null;
        int selAtype;                                                           //choose decision (by NN)
        
        //[0] - F = does not want to play (may also X for free)
        //[1] - C = wants to stay (flat) with no aggression (may also X for free)
        //[2] - B = wants to bet min (BRS)
        //[3] - B = wants to bet 2.5x (BRS)
        //[4] - B = wants to bet 3.2x (BRS)
        //[5] - B = wants to bet 5x (BRS)
        //[6] - A = wants to go allin
        selAtype = UArr.maxVix(solverOUT);                                  
        //F or X for free
        if(selAtype==0){
            for(PKDecision d: myPossibleDecisions()) if(d.T=='X') dNNchoosen=d;                             //look for X
            if(dNNchoosen==null) for(PKDecision d: myPossibleDecisions()) if(d.T=='F') dNNchoosen=d;        //if there is no X go F
        }
        //C or X for free, if not then F (no aggression prefered)
        if(selAtype==1){
            for(PKDecision d: myPossibleDecisions()) if(d.T=='X') dNNchoosen=d;                             //look for X
            if(dNNchoosen==null) for(PKDecision d: myPossibleDecisions()) if(d.T=='C') dNNchoosen=d;        //if there is no X look for C
            if(dNNchoosen==null) for(PKDecision d: myPossibleDecisions()) if(d.T=='F') dNNchoosen=d;        //there is no X nor C go F
        }
        if(selAtype==2 || selAtype==3 || selAtype==4 || selAtype==5){                                       //wants to bet/raise
            for(PKDecision d: myPossibleDecisions()) if(d.T=='B' || d.T=='R' || d.T=='S') dNNchoosen=d;     //return B R or S
            if(dNNchoosen==null){                                                                           //F,X,C or A only as option
                for(PKDecision d: myPossibleDecisions()) if(d.T=='A') dNNchoosen=d;                         //return A
            }
            else{
                double val=dNNchoosen.val;
                if(selAtype==3) val=val*1.25;
                if(selAtype==4) val=val*1.6;
                if(selAtype==5) val=val*2.5;
                if(val>=stack) for(PKDecision d: myPossibleDecisions()) if(d.T=='A') dNNchoosen=d;          //wants to raise but sizes bet for allin
                else dNNchoosen.val=(int)val;
            }
        }
        if(selAtype==6)                                                                                    //wants to A
            for(PKDecision d: myPossibleDecisions()) if(d.T=='A') dNNchoosen=d;         
        
        return dNNchoosen;
    }
    */
    
    @Override
    public PKDecision makeDecision(){                                           
        return interpretSolverOUT(mySolver.runFWD(prepareSolverIN()));
    }

    @Override
    protected void addHandStats(PKPlayerStats handStats){
        //prepare reward
        Double reward = null;
        if(handStats!=null)
            reward = handStats.WinTot /( myTable.maxStack * myTable.players.size() );

        //prepare envy specific feedback
        NTPKGameCaseSpecFeedback myEnvyFeedback = new NTPKGameCaseSpecFeedback();
        myEnvyFeedback.merge(handStats);
        
        NTCaseFeedback myFeedback = new NTCaseFeedback(null, reward, myEnvyFeedback);
        mySolver.takeCaseFeedback(myFeedback);
    }
}