package poker;

public class PKPlayerStats{
        //general
        public double WinTot;                                  //total win
        //preflop
        public long nHandsP;                                   //number of hands total (preflop) played
        public long nHandsPC;                                  //number of hands preflop called
            public long nHandsPlimp;                           //number of hands preflop limped - included in called
        public long nHandsPB;                                  //number of hands preflop bet
            public long nHandsPR;                              //number of hands preflop raised - included in bet
            public long nHandsPS;                              //number of hands preflop reraised - included in bet
            public long nHandsPA;                              //number of hands preflop allin - included in bet
        //F
        public long nHandsF;                                   //number of hands flop played
        public long nHandsFC;                                  //number of hands flop called (without bet)
        public long nHandsFB;                                  //number of hands flop bet
            public long nHandsFcbIP;                           //number of hands flop cbet ip - included in bet
            public long nHandsFcbOP;                           //number of hands flop cbet op - included in bet
            public long nHandsFR;                              //number of hands flop raised - included in bet    
            public long nHandsFS;                              //number of hands flop reraised - included in bet    
            public long nHandsFA;                              //number of hands flop allined - included in bet        
        //T
        public long nHandsT;
        public long nHandsTC;
        public long nHandsTB;
            public long nHandsTcbIP;
            public long nHandsTcbOP;
            public long nHandsTR;
            public long nHandsTS;
            public long nHandsTA;
        //R
        public long nHandsR;
        public long nHandsRC;
        public long nHandsRB;
            public long nHandsRcbIP;
            public long nHandsRcbOP;
            public long nHandsRR;
            public long nHandsRS;
            public long nHandsRA;
        //postflop
        public long nHandsWon;
        
        public PKPlayerStats(){
            flush();
        }
        
        public float VPIP(){       //voluntarily put $ in pot                                                   
            float val=0;
            if(nHandsP!=0) val=(float)(nHandsPC+nHandsPB)/nHandsP;
            return val;    
        }
        public float PFR(){        //preflop raise
            float val=0;
            if(nHandsP!=0) val=(float)nHandsPB/nHandsP;
            return val;
        }
        public float PF3bet(){        //preflop 3bet
            float val=0;
            if(nHandsP!=0) val=(float)nHandsPR/nHandsP;
            return val;
        }
        public float PF4bet(){        //preflop 4bet
            float val=0;
            if(nHandsP!=0) val=(float)nHandsPS/nHandsP;
            return val;
        }
        public float PFAll(){        //preflop allin
            float val=0;
            if(nHandsP!=0) val=(float)nHandsPA/nHandsP;
            return val;
        }
        public float PFlimp(){        //preflop limp
            float val=0;
            if(nHandsP!=0) val=(float)nHandsPlimp/nHandsP;
            return val;
        }
        public float FAR(){
            float val=99;
            if(nHandsFC!=0) val=(float)nHandsFB/nHandsFC;
            return val;
        }
        public float FcbOP(){
            float val=0;
            if(nHandsF!=0) val=(float)nHandsFcbOP/nHandsF;
            return val;
        }
        public float FcbIP(){
            float val=0;
            if(nHandsF!=0) val=(float)nHandsFcbIP/nHandsF;
            return val;
        }
        public float TAR(){
            float val=99;
            if(nHandsTC!=0) val=(float)nHandsTB/nHandsTC;
            return val;
        }
        public float TcbOP(){
            float val=0;
            if(nHandsT!=0) val=(float)nHandsTcbOP/nHandsT;
            return val;
        }
        public float TcbIP(){
            float val=0;
            if(nHandsT!=0) val=(float)nHandsTcbIP/nHandsT;
            return val;
        }
        public float RAR(){
            float val=99;
            if(nHandsRC!=0) val=(float)nHandsRB/nHandsRC;
            return val;
        }
        public float RcbOP(){
            float val=0;
            if(nHandsR!=0) val=(float)nHandsRcbOP/nHandsR;
            return val;
        }
        public float RcbIP(){
            float val=0;
            if(nHandsR!=0) val=(float)nHandsRcbIP/nHandsR;
            return val;
        }
        
        public void flush(){
            WinTot=0;                               
            //preflop
            nHandsP=0;  
            nHandsPC=0; 
                nHandsPlimp=0;                        
            nHandsPB=0;                          
                nHandsPR=0;                              
                nHandsPS=0;                             
                nHandsPA=0;                           
            //F
            nHandsF=0;
            nHandsFC=0;
            nHandsFB=0;    
                nHandsFcbIP=0;
                nHandsFcbOP=0;
                nHandsFR=0;
                nHandsFS=0;
                nHandsFA=0;
            //T
            nHandsT=0;
            nHandsTC=0;
            nHandsTB=0;
                nHandsTcbIP=0;
                nHandsTcbOP=0;
                nHandsTR=0;
                nHandsTS=0;
                nHandsTA=0;
            //R
            nHandsR=0;
            nHandsRC=0;
            nHandsRB=0;
                nHandsRcbIP=0;
                nHandsRcbOP=0;
                nHandsRR=0;
                nHandsRS=0;
                nHandsRA=0;
            //postflop
            nHandsWon=0;
        }
        public void merge(PKPlayerStats addSt){
            if(addSt!=null){
                WinTot+=addSt.WinTot;                               
                //preflop
                nHandsP+=addSt.nHandsP;  
                nHandsPC+=addSt.nHandsPC; 
                    nHandsPlimp+=addSt.nHandsPlimp;                        
                nHandsPB+=addSt.nHandsPB;                          
                    nHandsPR+=addSt.nHandsPR;                              
                    nHandsPS+=addSt.nHandsPS;                             
                    nHandsPA+=addSt.nHandsPA;                           
                //F
                nHandsF+=addSt.nHandsF;
                nHandsFC+=addSt.nHandsFC;
                nHandsFB+=addSt.nHandsFB;
                    nHandsFcbIP+=addSt.nHandsFcbIP;
                    nHandsFcbOP+=addSt.nHandsFcbOP;
                    nHandsFR+=addSt.nHandsFR;
                    nHandsFS+=addSt.nHandsFS;
                    nHandsFA+=addSt.nHandsFA;
                //T
                nHandsT+=addSt.nHandsT;
                nHandsTC+=addSt.nHandsTC;
                nHandsTB+=addSt.nHandsTB;
                    nHandsTcbIP+=addSt.nHandsTcbIP;
                    nHandsTcbOP+=addSt.nHandsTcbOP;
                    nHandsTR+=addSt.nHandsTR;
                    nHandsTS+=addSt.nHandsTS;
                    nHandsTA+=addSt.nHandsTA;
                //R
                nHandsR+=addSt.nHandsR;
                nHandsRC+=addSt.nHandsRC;
                nHandsRB+=addSt.nHandsRB;
                    nHandsRcbIP+=addSt.nHandsRcbIP;
                    nHandsRcbOP+=addSt.nHandsRcbOP;
                    nHandsRR+=addSt.nHandsRR;
                    nHandsRS+=addSt.nHandsRS;
                    nHandsRA+=addSt.nHandsRA;
                //postflop
                nHandsWon+=addSt.nHandsWon;
            }
        }
        @Override
        public String toString(){
            String outString = "not played any hand";
            if(nHandsP>0){
                outString =   " bb: "
                            + String.format("%2d",(int)( (WinTot/5) / ((double)nHandsP/100) ))  //bb/100
                            + " PF:(" 
                            + String.format("%2d",(int)(100*PFR()))                             //PFR
                            + ","
                            + String.format("%1d",(int)(100*(VPIP()-PFR())))                    //VPIP-PFR
                            + ") L34A:(" 
                            + String.format("%1d",(int)(100*PFlimp()))                          //PFlimp
                            + ","
                            + String.format("%1d",(int)(100*PF3bet()))                          //PF3bet
                            + ","
                            + String.format("%1d",(int)(100*PF4bet()))                          //PF4bet
                            + ","
                            + String.format("%1d",(int)(100*PFAll()))                           //PFAll
                            + ") F:" 
                            + String.format("%1.1f",FAR())                                      //FAR
                            + " T:" 
                            + String.format("%1.1f",TAR())                                      //TAR
                            + " R:" 
                            + String.format("%1.1f",RAR());                                     //RAR
            }    
            return outString;
        }
    }//PlayerStats
