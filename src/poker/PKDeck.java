package poker;

import java.util.ArrayList;
import utilities.URand;

public class PKDeck {
    public ArrayList<PKCard> deckCards;                                                //list of cards

    //constructor
    PKDeck(){
        deckCards = new ArrayList();
        resetDeckCards();
    }
    //gives random card from deckCards
    public PKCard giveCard(){                        
        int sel = URand.i(deckCards.size());
        return deckCards.remove(sel);
    }
    //gives exact card from deckCards
    public PKCard giveCard(int v, int c){            
        PKCard cA = null;
        for(int i=0; i<deckCards.size(); i++)
            if(deckCards.get(i).v()==v && deckCards.get(i).c()==c)
                cA = deckCards.remove(i);
        return cA;
    }
    //
    public void getBackCard(PKCard cardB){
        deckCards.add(cardB);
    }
    //restets deck to initial state
    public void resetDeckCards(){
        ArrayList<PKCard> dCards;
        dCards = new ArrayList();

        for(int v=1; v<14; v++) for(int c=1; c<5; c++) dCards.add(new PKCard(v,c));
        deckCards=dCards;
    }
}//Deck
