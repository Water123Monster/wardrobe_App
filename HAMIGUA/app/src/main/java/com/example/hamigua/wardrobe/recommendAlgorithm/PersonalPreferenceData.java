package com.example.hamigua.wardrobe.recommendAlgorithm;

import android.util.Log;

public class PersonalPreferenceData {

    public String Red;
    public String Orange;
    public String Yellow;
    public String ChartreuseGreen;
    public String Green;
    public String SpringGreen;
    public String Cyan;
    public String Azure;
    public String Blue;
    public String Violet;
    public String Magenta;
    public String Rose;

    public String BB;
    public String Bt;
    public String TB;
    public String TT;

    public String LL;
    public String LS;
    public String Sl;
    public String SS;

    public String Hs_Hv;
    public String Hs_Mv;
    public String Hs_Lv;
    public String Ms_Hv;
    public String Ms_Mv;
    public String Ms_Lv;
    public String Ls_Hv;
    public String LS_Mv;
    public String Ls_Lv;

    public String Same;
    public String Contrasting;
    public String Achromatic;
    public String Analogous;
    public String importance_HSV;
    public String importance_BT;
    public String importance_SL;

    /*
     * H 顏色{Red,Orange,Yellow,Chartreuse Green, Spring Green,Cyan,Azure,Blue,Violet,Magenta,Rose} BT 鬆緊{BB,BT,TB,TT} SL
     * 長短{LL,LS,SL,SS} SV 明暗{Hs_Hv,HS_Mv,Hs_Lv,Ms_Hv,MS_Mv,Ms_Lv,Ls_Hv,LS_Mv,Ls_Lv}
     * match 穿搭風格{Same,Contrasting,Achromatic,Anslgoous} importance
     * 重要程度{H,BT,SL}
     */
    static int[] H = new int[12];//色相
    static int[] BT = new int[4];//寬窄
    static int[] SL = new int[4];//長短
    static int[] SV = new int[9];//明暗與飽和度
    static int[] match = new int[4];//顏色搭配
    static int[] importance = new int[3];//重要性

    public PersonalPreferenceData(String Red, String Orange, String Yellow, String ChartreuseGreen,
                                  String Green, String SpringGreen, String Cyan, String Azure,
                                  String Blue, String Violet, String Magenta, String Rose, String BB,
                                  String Bt, String TB, String TT, String LL, String LS, String Sl,
                                  String SS, String Hs_Hv, String Hs_Mv, String Hs_Lv, String Ms_Hv,
                                  String Ms_Mv, String Ms_Lv, String Ls_Hv, String LS_Mv, String Ls_Lv,
                                  String Same, String Contrasting, String Achromatic, String Analogous,
                                  String importance_HSV, String importance_BT, String importance_SL) {
        this.Red = Red;
        this.Orange = Orange;
        this.Yellow = Yellow;
        this.ChartreuseGreen = ChartreuseGreen;
        this.Green = Green;
        this.SpringGreen = SpringGreen;
        this.Cyan = Cyan;
        this.Azure = Azure;
        this.Blue = Blue;
        this.Violet = Violet;
        this.Magenta = Magenta;
        this.Rose = Rose;
        this.BB = BB;
        this.Bt = Bt;
        this.TB = TB;
        this.TT = TT;
        this.LL = LL;
        this.LS = LS;
        this.Sl = Sl;
        this.SS = SS;
        this.Hs_Hv = Hs_Hv;
        this.Hs_Mv = Hs_Mv;
        this.Hs_Lv = Hs_Lv;
        this.Ms_Hv = Ms_Hv;
        this.Ms_Mv = Ms_Mv;
        this.Ms_Lv = Ms_Lv;
        this.Ls_Hv = Ls_Hv;
        this.LS_Mv = LS_Mv;
        this.Ls_Lv = Ls_Lv;
        this.Same = Same;
        this.Contrasting = Contrasting;
        this.Achromatic = Achromatic;
        this.Analogous = Analogous;
        this.importance_HSV = importance_HSV;
        this.importance_BT = importance_BT;
        this.importance_SL = importance_SL;

        H[0] = Integer.parseInt(Red);
        H[1] = Integer.parseInt(Orange);
        H[2] = Integer.parseInt(Yellow);
        H[3] = Integer.parseInt(ChartreuseGreen);
        H[4] = Integer.parseInt(Green);
        H[5] = Integer.parseInt(SpringGreen);
        H[6] = Integer.parseInt(Cyan);
        H[7] = Integer.parseInt(Azure);
        H[8] = Integer.parseInt(Blue);
        H[9] = Integer.parseInt(Violet);
        H[10] = Integer.parseInt(Magenta);
        H[11] = Integer.parseInt(Rose);

        BT[0] = Integer.parseInt(BB);
        BT[1] = Integer.parseInt(Bt);
        BT[2] = Integer.parseInt(TB);
        BT[3] = Integer.parseInt(TT);

        SL[0] = Integer.parseInt(LL);
        SL[1] = Integer.parseInt(LS);
        SL[2] = Integer.parseInt(Sl);
        SL[3] = Integer.parseInt(SS);

        SV[0] = Integer.parseInt(Hs_Hv);
        SV[1] = Integer.parseInt(Hs_Mv);
        SV[2] = Integer.parseInt(Hs_Lv);
        SV[3] = Integer.parseInt(Ms_Hv);
        SV[4] = Integer.parseInt(Ms_Mv);
        SV[5] = Integer.parseInt(Ms_Lv);
        SV[6] = Integer.parseInt(Ls_Hv);
        SV[7] = Integer.parseInt(LS_Mv);
        SV[8] = Integer.parseInt(Ls_Lv);

        match[0] = Integer.parseInt(Same);
        match[1] = Integer.parseInt(Contrasting);
        match[2] = Integer.parseInt(Achromatic);
        match[3] = Integer.parseInt(Analogous);

        importance[0] = Integer.parseInt(importance_HSV);
        importance[1] = Integer.parseInt(importance_BT);
        importance[2] = Integer.parseInt(importance_SL);

    }

    static int get_H(int num) {
        Log.d("test","??????");
        return H[num];
    }

    //待修
    static int get_BT(String type) {
        String[] bT_typeString = { "BB", "BT", "TB", "TT" };
        //if (type)
        return 0;
    }

    //待修
    static int get_SL(String type) {
        String[] SL_typeString = { "LL", "LS", "SL", "SS" };
        for (int i = 0; i < SL_typeString.length; i++) {
            if (SL_typeString[i].equals(type)) {
                return SL[i];
            }
        }
        return 0;
    }

    //Hs_Hv,HS_Mv,Hs_Lv,Ms_Hv,MS_Mv,Ms_Lv,Ls_Hv,LS_Mv,Ls_Lv
    static int get_SV(String S_level, String V_level) {
        if (S_level.equals("High")) {
            if (V_level.equals("High")) {
                return SV[0];
            } else if (V_level.equals("Medium")) {
                return SV[1];
            } else {
                return SV[2];
            }
        } else if (S_level.equals("Medium")) {
            if (V_level.equals("High")) {
                return SV[3];
            } else if (V_level.equals("Medium")) {
                return SV[4];
            } else {
                return SV[5];
            }
        } else {
            if (V_level.equals("High")) {
                return SV[6];
            } else if (V_level.equals("Medium")) {
                return SV[7];
            } else {
                return SV[8];
            }
        }
    }

    static int get_match(int[] HSV_1,int[] HSV_2) {
        int[] Temp = new int[3];
        if(HSV_1[0]>HSV_2[0]) {
            Temp = HSV_1;
            HSV_1 = HSV_2;
            HSV_2 = Temp;
        }
        // TODO Auto-generated method stub
        boolean cloth1_Anslgoous = (HSV_1[1]<11)||(HSV_1[2]<15);
        boolean cloth2_Anslgoous = (HSV_2[1]<11)||(HSV_2[2]<15);
        if (cloth1_Anslgoous&&cloth2_Anslgoous) {
            return match[3];
        }
        int HSV_1_H_360 = HSV_1[0]+360;
        boolean Same =  ((Math.abs( HSV_2[0]-HSV_1[0]))<15||(Math.abs(HSV_1_H_360-HSV_2[0])<15));
        boolean Contrasting = ((Math.abs( HSV_2[0]-HSV_1[0]))<30||(Math.abs(HSV_1_H_360-HSV_2[0])<30));
        boolean Achromatic = ((Math.abs( HSV_2[0]-HSV_1[0]))<60||(Math.abs(HSV_1_H_360-HSV_2[0])<60));
        if(Same)
            return match[0];
        else if(Contrasting)
            return match[1];
        else if(Achromatic)
            return match[2];

        return 0;
    }
}
