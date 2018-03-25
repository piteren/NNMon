/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;
import java.util.LinkedList;


/*
 * Poker Table, communicates with players using decisions, runs hand, holds current hand history and all other data to evaluate statistics
 */
public class PKTable implements Runnable{
    
    protected final PKDeck tableDeck;                  //table deck of cards
    public LinkedList<PKCard> tableCards;              //up to 5 cards
    
    public LinkedList<PKPlayer> players;                //all table players, sorted by actual position: 0-sb, 1-bb, ... n-dealer
    public ArrayList<PKPlayer> playersD;               //players with current ability to decide in hand, sorted to last deciding
    public ArrayList<PKPlayer> playersP;               //players fighting 4pot (here are also those who allined = no deciding but fighting)
    public ArrayList<PKPlayer> playersW;               //players that won pot (usually one...)
    
    public int TS;                                     //table state 0-ready 1-preflop(hands dealt, blinds posted), 2-flop, 3-turn, 4-river, 5-posthand
    
    int sb,bb;                                          //blind sizes
    public int maxStack;                               //table maximum stack
    public int actPot;                                 //actual pot amount
    int lastBet;                                        //last bet size
    int lastRaise;                                      //last raise size
        
    public int handsToRunAM;                           //number of hands left to run
    
    public LinkedList<PKDecision> handDecisions;       //list of (current hand) decisions
    
    PKTableStats myStats;
    
    //constructor
    public PKTable(){
        tableDeck = new PKDeck();
        tableCards = new LinkedList();
        
        players = new LinkedList();        
        
        actPot=0;
        lastBet=0;
        lastRaise=0;
        
        myStats = new PKTableStats();
    }
    
    //adds players to players list sb > bb > ... dealer last, sets starting values
    public void addPlayer(PKPlayer pl){  
        pl.myTable=this;
        pl.stack=maxStack;
        pl.vPut=0;
        pl.setMyCards(null,null);
        players.add(pl);
    }
    //
    public void removeAllPlayers(){
        players = new LinkedList();
    }
    //sets new blind sizes
    public final void setBilindsAndStack(int s, int b, int st){     
        sb=s;
        bb=b;
        maxStack=st;
    }
    //deal hands for players
    void dealHands(){                                   
        for(PKPlayer pl: players)
            pl.setMyCards(tableDeck.giveCard(),tableDeck.giveCard());
    }
    //evaluates actual possible decisions for given player
    LinkedList<PKDecision> setPossibleDecisions(PKPlayer pl){
        LinkedList<PKDecision> pDecs = new LinkedList<>();
        
        //calculate value_to_call and value_to_bet fot his player
        int valTC,valTB;                
        valTC=lastBet+lastRaise-pl.vPut;                                                            //to cal: bet+raise-actual_his_bet (if ==0 then there is no fold and u can check for free)
        if(lastBet==0) valTB=bb;                                                                    //to bet: at least bb
        else{
            if(lastRaise==0) valTB=2*lastBet-pl.vPut;                                               //to raise: double actual bet (minus sb situation)
            else valTB=lastBet+2*lastRaise-pl.vPut;                                                 //to reraise: bet+2raise-actual_his_bet
        }                                                   
        
        if(valTC==0)
            pDecs.add(new PKDecision(pl,this,'X',0,lastBet,lastRaise));                             //no fold - just call for free
        else{
            pDecs.add(new PKDecision(pl,this,'F',0,lastBet,lastRaise));                             //fold decision possible
            if(pl.stack<=valTC)                              
                pDecs.add(new PKDecision(pl,this,'A',pl.stack,lastBet,lastRaise));                  //allin call for stack
            else
                pDecs.add(new PKDecision(pl,this,'C',valTC,lastBet,lastRaise));                     //call decision
        }
        if(pl.stack>valTC){                                                                         //if has enough (more than call) to make bet or raise
            if(pl.stack<=valTB)                                                                     //but has not enough to make "normal" allin raise
                pDecs.add(new PKDecision(pl,this,'A',pl.stack,lastBet,pl.stack-lastBet+pl.vPut));   //allin raise for stack
            else{
                if(lastBet==0){                                                                     //first raise on FTR = first bet
                    lastBet=valTB;
                    pDecs.add(new PKDecision(pl,this,'B',valTB,lastBet,valTB-lastBet+pl.vPut));     //first bet
                }
                else{
                    if(lastBet==bb && TS==1)
                        pDecs.add(new PKDecision(pl,this,'B',valTB,lastBet,valTB-lastBet+pl.vPut)); //first bet on preflop
                    if(lastRaise==0)
                        pDecs.add(new PKDecision(pl,this,'R',valTB,lastBet,valTB-lastBet+pl.vPut)); //raise or 3bet
                    else
                        pDecs.add(new PKDecision(pl,this,'S',valTB,lastBet,valTB-lastBet+pl.vPut)); //reraise or 4bet
                }
            }
        }
        if(pl.stack>valTB)
            pDecs.add(new PKDecision(pl,this,'A',pl.stack,lastBet,pl.stack-lastBet+pl.vPut));       //allin for stack
        
        return pDecs;
    }
    //runs one hand
    protected void runHand(int mode){                                                     //mode: 0 normal, 1 preflop only, 2 preflop and river only
 
        TS=0;
        handsToRunAM--;
        handDecisions = new LinkedList();
        
        //add all players 4decisions  by now
        playersD = new ArrayList();
        for(PKPlayer pl: players) playersD.add(pl);                         
        //post sb + bb
        playersD.get(0).vPut=sb;                            
        playersD.get(0).stack-=sb;
        playersD.get(1).vPut=bb;
        playersD.get(1).stack-=bb;
        actPot=sb+bb;
        lastBet=bb;
        //add big blind player 4pot by now
        playersP = new ArrayList();
        playersP.add(playersD.get(1));                          
        //offset sb & bb for preflop decisions                        
        playersD.add(playersD.remove(0));                                       
        playersD.add(playersD.remove(0));                                       
        
        TS=1;
        //RUN RIVERS                                                          
        while(TS<5){                                        
            int indexP=0;                                                       //index of player (in playersD ArrayList) to make next decision
            
            //DEALING CARDS            
            if(TS==1)
                dealHands();                                      
            if(TS==2){
                tableCards.add(tableDeck.giveCard());
                tableCards.add(tableDeck.giveCard());
                tableCards.add(tableDeck.giveCard());
            }
            if(TS>2)
                tableCards.add(tableDeck.giveCard());

            boolean goBetting = false;
            if(playersD.size()>1) goBetting = true;                     //while there are players to decide
            if(mode==1) if(TS>2) goBetting = false;                     //for mod==1 there will be no betting on FTR
            if(mode==2) if(TS==2 || TS==3) goBetting = false;           //for mod==2 there will be no betting on FT
            
            //BETTING ROUND      
            while(goBetting){
                PKPlayer PlDeciding = playersD.get(indexP);
                PlDeciding.setMyPDecisions( setPossibleDecisions(PlDeciding) );
                //plyer makes decision from list of decisions (created by table method)
                PKDecision decP=PlDeciding.makeDecision();
                handDecisions.add(decP);
                PlDeciding.addHandStats(null);                                  //adds null hand sats (empty)
                        
                //update table and player cash
                actPot+=decP.val;
                lastBet=decP.bet;
                lastRaise=decP.raise;
                PlDeciding.stack-=decP.val;
                PlDeciding.vPut+=decP.val;   
                 
                //POST DECISION ACTION
                if(TS==1 && decP.T!='F') if(!playersP.contains(PlDeciding)) playersP.add(PlDeciding);   //add this player to playersP durring preflop
                switch (decP.T){
                    case 'F':                                   //player folds
                        playersP.remove(playersD.get(indexP));  //remove z pot
                        playersD.remove(indexP);                //remove z kolka (index nie rosnie - remove zwieksza index)
                        break;
                    case 'X':                                   //player checks
                    case 'C':                                   //or calls >> just go to next player
                        indexP++;                           
                        break;
                    case 'B':                                   //player bets
                    case 'R':                                   //or raises (3bet on preflop)
                    case 'S':                                   //or reraises (4bet on preflop)
                        for(int i=indexP; i>0; i--)             //przepisujemy kolko, betujący jeako pierwszy (...otrzyma index 0)
                            playersD.add(playersD.remove(0));
                        indexP=1;                               //index==1 (na pierwszego za betującym)
                        break;
                    case 'A':                                   //one player goes allin
                        playersD.remove(indexP);                //remove z kolka
                        for(int i=indexP; i>0; i--)             //przepisujemy kolko
                            playersD.add(playersD.remove(0));
                        indexP=0;                               //zerujemy index
                        break;
                }                 
                //check STOP BETTING CIRCLE conditions
                if(indexP==playersD.size()) goBetting=false;                    //end of betting circle
                if((playersD.isEmpty())&&(playersP.size()>1)) goBetting=false;  //allin run, no more betting needed
                if((playersP.size()==1)){                                       //everybody folded to one bet or allin, we have winner
                    goBetting=false;
                    TS=4;
                }
            }
            //POST CIRCLE ACTION  
            //sort deciding players from sb to dealer for naxt betting round
            int min=10;
            int offset=0;
            for(PKPlayer plD: playersD){
                if(players.indexOf(plD)<min){                                   //look for first player to act
                    min=players.indexOf(plD);
                    offset=playersD.indexOf(plD);
                }
            }
            for(int i=offset; i>0; i--) playersD.add(playersD.remove(0));
            //zero lastBet, lastRaise and value_put_to_pot for (all table) hand players   
            lastBet=0;
            lastRaise=0;
            for(PKPlayer pl: players) pl.vPut=0;                              
            TS++;
        }
        playersW = selectWinners();
        updatePlayersAndTableStats();        
        for(PKPlayer allplayers: players) allplayers.stack=maxStack;            //reset players stack                                                
        actPot=0;                                                               //reset table pot
        returnCards();
        players.add(players.remove(0));                                         //moves players to next position, sb to dealer (0 >> n), bb to sb (1 >> 0)... 
    }
    
    public ArrayList<PKPlayer> selectWinners(){
        ArrayList<PKPlayer> plWON = new ArrayList();
        
        // object durring construction evaluates strength of 7 cards given via list and (using best 5 cards from given 7) writes calculated values (evaluation rank) to its fields
        class B5Cards {    
            int lev;                        //poker specific levels: 0-highcard 1-pair 2-2pairs 3-3ofkind 4-straight 5-flush 6-FH 7-4ofkind 8-strflush(with poker)
            int val;                        //value of each combination calculated to be unique - stronger hand of same level will have higher val
            
            B5Cards(LinkedList<PKCard> a7Cards){  
                lev=0;
                val=0;
                evaluate7cards(a7Cards);
            }
            
            void evaluate7cards(LinkedList<PKCard> a7Cards){
                int h,i,j;                  //helper counter
        
                int isFlush = 0;            //flush, value means color
                int iStrFrom = 0;           //straight, value means highest card in str
                int iSFFrom = 0;            //strflush, value means highest card in strF
                int fourOf = 0;             //value means v cards
                int FH = 0;                 //1 means there is (rest in threeOf and pair)
                int threeOf = 0;            //value means v cards
                int tPairs = 0;             //value means highest pair v (rest in pair)
                int pair = 0;               //value means v cards        
        
                //helper array for cards, zero index is for sum of value and sum of color
                int[][] cArr = new int[14][5];
        
                //zero array        
                for(i=0; i<14; i++) for(j=0; j<5; j++) cArr[i][j]=0;
        
                //fill array for given cards
                for(PKCard card: a7Cards){
                    cArr[card.v()][card.c()]=1;
                    cArr[0][card.c()]++;
                    cArr[card.v()][0]++;
                }
        
                //test this array for each case
                testing:{
                    //is flush?
                    for(i=1; i<5; i++) if(cArr[0][i]>4) isFlush = i;     
                    //is straight?
                    h=0;
                    for(i=13; i>0; i--){
                        if(cArr[i][0]>0) h++;
                        else h=0;
                        if(h==5) iStrFrom = i+4;
                    }
                    //if staright and flush > maybe straightflush?        
                    h=0;
                    if(isFlush>0 && iStrFrom>0){
                        for(i=iStrFrom; i>0; i--){         
                            if(cArr[i][isFlush]>0) h++;        
                            else h=0;
                            if(h==5) iSFFrom = i+4; 
                        }        
                    }
                    if(isFlush>0 || iStrFrom>0) break testing;
            
                    //case 4of
                    for(i=13; i>0; i--){
                        if(cArr[i][0]==4){
                            fourOf = i;
                            break testing;
                        }
                    }
                    //case 3of
                    for(i=13; i>0; i--)if(cArr[i][0]==3) threeOf = i;
                    //case FH
                    if(threeOf>0){
                        for(i=13; i>0; i--){
                            if(i==threeOf) i--;
                            if(cArr[i][0]>1){
                                pair = i;
                                FH = 1;
                                break testing;      //FH
                            }
                        }
                        break testing;  //at least 3of
                    }

                    //pairs
                    for(i=13; i>0; i--){
                        if(cArr[i][0]>1){
                            if(pair==0) pair = i;
                            else{
                                tPairs = pair;
                                pair = i;
                                break testing;
                            }
                        }
                    }    
                }//end of TESTING

                //calculation of lev and val values
                write:{
                    if(iSFFrom>0){
                        lev = 8;
                        val = iSFFrom;
                        break write;
                    }
                    if(fourOf>0){   
                        lev = 7;
                        val = fourOf*100;
                        i=1;
                        j=13;
                        while(i>0){
                            if(cArr[j][0]>0){
                                if(j!=fourOf){
                                    val+=j;
                                    i--;
                                }
                            }
                            j--;
                        }
                        break write;
                    }
                    if(FH>0){           
                        lev = 6;
                        val = threeOf*100+pair;
                        break write;
                    }
                    if(isFlush>0){      
                        lev = 5;
                        h=100000000;
                        i=5;
                        j=13;
                        while(i>0){
                            if(cArr[j][isFlush]==1){
                                val+=h*j;
                                i--;
                                h=h/100;
                            }
                            j--;
                        }
                        break write;
                    }
                    if(iStrFrom>0){     
                        lev = 4;
                        val = iStrFrom;
                        break write;
                    }
                    if(threeOf>0){      
                        lev = 3;
                        i=2;
                        j=13;
                        val=10000*threeOf;
                        h=100;
                        while(i>0){
                            if(cArr[j][0]>0){
                                if(j!=threeOf){
                                    val+=j*h;
                                    h=h/100;
                                    i--;
                                }
                            }
                            j--;
                        }
                        break write;
                    }
                    if(tPairs>0){
                        lev = 2;
                        i=1;
                        j=13;
                        val=10000*tPairs+100*pair;
                        while(i>0){
                            if(cArr[j][0]>0){
                                if(j!=pair && j!=tPairs){
                                    val+=j;
                                    i--;
                                }
                            }
                            j--;
                        }
                        break write;
                    }
                    if(pair>0){         
                        lev = 1;
                        i=3;
                        j=13;
                        h=10000;
                        val=1000000*pair;
                        while(i>0){
                            if(cArr[j][0]>0){
                                if(j!=pair){
                                    val+=h*j;
                                    i--;
                                    h=h/100;
                                }
                            }
                            j--;
                        }
                        break write;
                    }
                    //consider highcard 
                    i=5;
                    j=13;
                    h=100000000;
                    while(i>0){
                        if(cArr[j][0]>0){
                            val+=h*j;
                            i--;
                            h=h/100;
                        }
                        j--;
                    }
                }
            }
            
        }//B5Cards
        
        //case one winning player after folds...
        if(playersP.size()==1) plWON.add(playersP.get(0));
        else{                                                                   //else choose best
            LinkedList<PKCard> a7Cards;                                         //list of 7 cards (5 table and 2 player)
            LinkedList<B5Cards> best = new LinkedList<>();                      //list of ranked 7(5) cards
            
            //evaluate cards
            for(int i=0; i<playersP.size(); i++){
                a7Cards = new LinkedList<>();
                for(PKCard object: tableCards) a7Cards.add(object);
             
                a7Cards.add(playersP.get(i).cA());
                a7Cards.add(playersP.get(i).cB());
        
                best.add(new B5Cards(a7Cards));
            }
            //compare hands
            long max = 0;
            long val;
            long ofs = 1000000000;
            for(int i=0; i<playersP.size(); i++){                   //look 4 max
                val = best.get(i).lev * ofs + best.get(i).val;
                if(val>max) max=val;
            }
            for(int i=0; i<playersP.size(); i++){                   //select max players
                val = best.get(i).lev * ofs + best.get(i).val;
                if(val==max) plWON.add(playersP.get(i));
            }
        }
        return plWON;
    }
    //creates and calculates (one) hand stats for every player and finally adds those to players stats with player method
    void updatePlayersAndTableStats(){
        //create empty hand stats
        LinkedList<PKPlayerStats> handStats = new LinkedList();
        for(PKPlayer sPl: players) handStats.add(new PKPlayerStats());
      
        //WinTot, nHandsWon ...winData
        double winning = (double)actPot/playersW.size();
        for(int i=0; i<players.size(); i++){
            PKPlayer sPl = players.get(i);
            if(playersW.contains(sPl)){
                handStats.get(i).WinTot=winning-maxStack+sPl.stack;
                handStats.get(i).nHandsWon=1;
            }
            else handStats.get(i).WinTot=sPl.stack-maxStack;
        }

        int[] statP = new int[players.size()];                                  //helper array, notes decision for player 0-not played that river 1-played 2-called 3,4,5,6-BRSA
        int decIX=0;                                                            //decision index
        PKPlayer decPL;                                                         //decision player
        char decT;                                                              //decision type
        boolean eofDecisions=false;                                             //reached decisions end 
        
        //P preflop
        boolean limper=true;
        for(int el: statP) el=0;
        while(handDecisions.get(decIX).dTS==1){
            decPL = handDecisions.get(decIX).dPlayer;                        
            decT = handDecisions.get(decIX).T;                               
            if(statP[players.indexOf(decPL)]==0)
                statP[players.indexOf(decPL)]=1;                                //played P
            if( decT=='C' )
                if(statP[players.indexOf(decPL)]<2){
                    statP[players.indexOf(decPL)]=2;
                    if(limper) handStats.get(players.indexOf(decPL)).nHandsPlimp=1;             //no bet before = limp
                }
            if( decT=='B' )
                if(statP[players.indexOf(decPL)]<3){
                    statP[players.indexOf(decPL)]=3;
                    limper=false;                                               //after bet there is no limp
                }
            if( decT=='R' )
                if(statP[players.indexOf(decPL)]<4) 
                    statP[players.indexOf(decPL)]=4;
            if( decT=='S' )
                if(statP[players.indexOf(decPL)]<5) 
                    statP[players.indexOf(decPL)]=5;
            if( decT=='A' )
                if(statP[players.indexOf(decPL)]<6){ 
                    statP[players.indexOf(decPL)]=6;
                    limper=false;                                               //after allin there is no limp
                }
            decIX++;
            if(decIX==handDecisions.size()){
                eofDecisions=true;
                break;
            }
        }
        for(int i=0; i<statP.length; i++){
            if(statP[i] >0) handStats.get(i).nHandsP=1;
            if(statP[i]==2) handStats.get(i).nHandsPC=1;
            if(statP[i] >2) handStats.get(i).nHandsPB=1;
            if(statP[i]==4) handStats.get(i).nHandsPR=1;
            if(statP[i]==5) handStats.get(i).nHandsPS=1;
            if(statP[i]==6) handStats.get(i).nHandsPA=1;
        }
        //F flop
        if(!eofDecisions){
            for(int el: statP) el=0;
            while(handDecisions.get(decIX).dTS==2){
                decPL = handDecisions.get(decIX).dPlayer;                    
                decT = handDecisions.get(decIX).T;                             
                if(statP[players.indexOf(decPL)]==0)
                    statP[players.indexOf(decPL)]=1;                            //played F
                if( decT=='C' )
                    if(statP[players.indexOf(decPL)]<2)
                        statP[players.indexOf(decPL)]=2;
                if( decT=='B' )
                    if(statP[players.indexOf(decPL)]<3){
                        statP[players.indexOf(decPL)]=3;
                        if(handDecisions.get(decIX-1).dTS==1) handStats.get(players.indexOf(decPL)).nHandsFcbOP=1;
                        else handStats.get(players.indexOf(decPL)).nHandsFcbIP=1;               //not to accurate - IP should be only last in circe?
                    }
                if( decT=='R' )
                    if(statP[players.indexOf(decPL)]<4) 
                        statP[players.indexOf(decPL)]=4;
                if( decT=='S' )
                    if(statP[players.indexOf(decPL)]<5) 
                        statP[players.indexOf(decPL)]=5;
                if( decT=='A' )
                    if(statP[players.indexOf(decPL)]<6)
                        statP[players.indexOf(decPL)]=6;
                decIX++;
                if(decIX==handDecisions.size()){
                    eofDecisions=true;
                    break;
                }
            }
            for(int i=0; i<statP.length; i++){
                if(statP[i] >0) handStats.get(i).nHandsF++;
                if(statP[i]==2) handStats.get(i).nHandsFC++;
                if(statP[i] >2) handStats.get(i).nHandsFB++;
                if(statP[i]==4) handStats.get(i).nHandsFR++;
                if(statP[i]==5) handStats.get(i).nHandsFS++;
                if(statP[i]==6) handStats.get(i).nHandsFA++;
            }
        }
        //T turn
        if(!eofDecisions){
            for(int el: statP) el=0;
            while(handDecisions.get(decIX).dTS==3){
                decPL = handDecisions.get(decIX).dPlayer;                   
                decT = handDecisions.get(decIX).T;                        
                if(statP[players.indexOf(decPL)]==0)
                    statP[players.indexOf(decPL)]=1;                            //played T
                if( decT=='C' )
                    if(statP[players.indexOf(decPL)]<2)
                        statP[players.indexOf(decPL)]=2;
                if( decT=='B' )
                    if(statP[players.indexOf(decPL)]<3){
                        statP[players.indexOf(decPL)]=3;
                        if(handDecisions.get(decIX-1).dTS==1) handStats.get(players.indexOf(decPL)).nHandsTcbOP=1;
                        else handStats.get(players.indexOf(decPL)).nHandsTcbIP=1;               //not to accurate - IP should be only last in circe?
                    }
                if( decT=='R' )
                    if(statP[players.indexOf(decPL)]<4) 
                        statP[players.indexOf(decPL)]=4;
                if( decT=='S' )
                    if(statP[players.indexOf(decPL)]<5) 
                        statP[players.indexOf(decPL)]=5;
                if( decT=='A' )
                    if(statP[players.indexOf(decPL)]<6)
                        statP[players.indexOf(decPL)]=6;
                decIX++;
                if(decIX==handDecisions.size()){
                    eofDecisions=true;
                    break;
                }
            }
            for(int i=0; i<statP.length; i++){
                if(statP[i] >0) handStats.get(i).nHandsT++;
                if(statP[i]==2) handStats.get(i).nHandsTC++;
                if(statP[i] >2) handStats.get(i).nHandsTB++;
                if(statP[i]==4) handStats.get(i).nHandsTR++;
                if(statP[i]==5) handStats.get(i).nHandsTS++;
                if(statP[i]==6) handStats.get(i).nHandsTA++;
            }
        }
        //R river
        if(!eofDecisions){
            for(int el: statP) el=0;
            while(handDecisions.get(decIX).dTS==4){
                decPL = handDecisions.get(decIX).dPlayer;                   
                decT = handDecisions.get(decIX).T;                        
                if(statP[players.indexOf(decPL)]==0)
                    statP[players.indexOf(decPL)]=1;                            //played R
                if( decT=='C' )
                    if(statP[players.indexOf(decPL)]<2)
                        statP[players.indexOf(decPL)]=2;
                if( decT=='B' )
                    if(statP[players.indexOf(decPL)]<3){
                        statP[players.indexOf(decPL)]=3;
                        if(handDecisions.get(decIX-1).dTS==1) handStats.get(players.indexOf(decPL)).nHandsRcbOP=1;
                        else handStats.get(players.indexOf(decPL)).nHandsRcbIP=1;               //not to accurate - IP should be only last in circe?
                    }
                if( decT=='R' )
                    if(statP[players.indexOf(decPL)]<4) 
                        statP[players.indexOf(decPL)]=4;
                if( decT=='S' )
                    if(statP[players.indexOf(decPL)]<5) 
                        statP[players.indexOf(decPL)]=5;
                if( decT=='A' )
                    if(statP[players.indexOf(decPL)]<6)
                        statP[players.indexOf(decPL)]=6;
                decIX++;
                if(decIX==handDecisions.size()){
                    eofDecisions=true;
                    break;
                }
            }
            for(int i=0; i<statP.length; i++){
                if(statP[i] >0) handStats.get(i).nHandsR++;
                if(statP[i]==2) handStats.get(i).nHandsRC++;
                if(statP[i] >2) handStats.get(i).nHandsRB++;
                if(statP[i]==4) handStats.get(i).nHandsRR++;
                if(statP[i]==5) handStats.get(i).nHandsRS++;
                if(statP[i]==6) handStats.get(i).nHandsRA++;
            }
        }
        for(int i=0; i<handStats.size(); i++) players.get(i).addHandStats( handStats.get(i) );
    }
    
    void resetGameCash(){
        for(PKPlayer allplayers: players) allplayers.stack=maxStack;                                                
        actPot=0;
    }
    //returns all table players and table cards to deck
    void returnCards(){
        tableCards = new LinkedList();
        for(PKPlayer pl: players)
            pl.setMyCards(null,null);
        tableDeck.resetDeckCards();
    }
    
    @Override
    public void run(){
        while(handsToRunAM>0) runHand(0);
    }
}//PKTable