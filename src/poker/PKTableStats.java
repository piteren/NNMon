package poker;

public class PKTableStats{
        private long  tTotHands;                                                //total table hands
        private double tAvgWinPot;                                              //average final pot
        private double tAvgHandPlayers;                                         //average flop players

        PKTableStats(){
            tTotHands=0;                
            tAvgWinPot=0;                  
            tAvgHandPlayers=0;                 
        }

        long tableTotHands(int am){                     //updates & returns
            if(am>0) tTotHands+=am;
            return tTotHands;
        }
        long tableTotHands(){ return tTotHands; }

        double tableAvgWinPot(double p){                  //updates & returns
            tAvgWinPot = ((tAvgWinPot * tTotHands) + p) / (tTotHands + 1);
            return tAvgWinPot;
        }
        double tableAvgWinPot(){ return tAvgWinPot; }

        double tableAvgHandPlayers(int n){               //updates & returns
            tAvgHandPlayers = ((tAvgHandPlayers * tTotHands) + n) / (tTotHands + 1);
            return tAvgHandPlayers;
        }
        double tableAvgHandPlayers(){ return tAvgHandPlayers; }

        @Override
        public String toString(){
            return "TS: tTotHands: "+tTotHands+", tAvgWinPot: "+tAvgWinPot+", tAvgHandPlayers: "+tAvgHandPlayers;
        }
    }//TableStats
