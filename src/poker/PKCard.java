package poker;

public class PKCard {    
    private final int   val,    //card value (1-13)
                        col;    //card color (1-4)
    
    PKCard(int v,int c){ 
        val = v;
        col = c;
    }
    //returns value
    public int v(){
        return this.val;
    }
    //returns color
    public int c(){
        return this.col;
    }
    @Override
    public String toString(){    
        String s = null;
        switch(val){
            case 1:  s="2"; break;
            case 2:  s="3"; break;
            case 3:  s="4"; break;
            case 4:  s="5"; break;
            case 5:  s="6"; break;
            case 6:  s="7"; break;
            case 7:  s="8"; break;
            case 8:  s="9"; break;
            case 9:  s="10";break;
            case 10: s="J"; break;
            case 11: s="D"; break;
            case 12: s="K"; break;
            case 13: s="A"; break;
        }
        switch(col){
            case 1: s+="H"; break;  //heart
            case 2: s+="D"; break;  //diamond
            case 3: s+="S"; break;  //spade
            case 4: s+="C"; break;  //club
        }
        return s;
    }
}//PKCard
