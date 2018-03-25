/*
 * 2017 (c) piteren
 */
package poker;

public class PKDecision {
    public PKPlayer     dPlayer;                        //player of decision
    public PKTable      dTable;                         //table of decision
    public int          dTS;                            //decision table state
    public char         T;                              //type: F-fold X-chceck (for free) C-call B-bet R-raise (or 3bet on preflop) S-reraise (or 4bet on preflop) A -allin
    public int          val;                            //min value of bet (for this type of decision)
    int                 bet,                            //new bet
                        raise;                          //and raise values (set by this decision)
    
    //decision(player,table,type,minbetvalue,bet,raise)
    PKDecision(PKPlayer pl,PKTable tab, char t, int v, int b, int r){
        dPlayer=pl;
        dTable=tab;
        dTS=dTable.TS;
        T=t;
        val=v;
        bet=b;
        raise=r;
    }
}