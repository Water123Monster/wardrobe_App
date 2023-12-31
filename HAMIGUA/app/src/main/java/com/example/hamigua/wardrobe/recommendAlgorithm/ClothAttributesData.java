package com.example.hamigua.wardrobe.recommendAlgorithm;

import android.util.Log;

public class ClothAttributesData {

    //事實標籤，衣服屬性
    public String ID = "";
    public int H_cloth = 0;
    public int S_cloth = 0;
    public int V_cloth = 0;
    public String BT_cloth  = "" ;//B/T
    public String SL_cloth  = "" ;//L/S
    //要存資料庫
    public double sum_score = 0;//目前尚未加上權重

    //計算佔存
    int H_num = 0; //色相歸屬序號
    String s_sring = "";//s高中低
    String v_sring = "";//v高中低
    int[] HSV = new int[3];//將hsv存進同一陣列，方便進行match喜好分數計算

    //衣服分數
    int H_score = 0;
    int BT_score = 0;
    int SL_score = 0;
    int SV_score = 0;
    int match_score = 0;

    /*
     * H 顏色{Red,Orange,Yellow,Chartreuse Grern,	Spring Green,Cyan,Azure,Blue,Violet,Magenta,Rose}
     * BT 鬆緊{BB,BT,TB,TT}
     * SL 長短{LL,LS,SL,SS}
     * SV 明暗{Hs_Hv,HS_Mv,Hs_Lv,Ms_Hv,MS_Mv,Ms_Lv,Ls_Hv,LS_Mv,Ls_Lv}
     * match 穿搭風格{Same,Contrasting,Achromatic,Anslgoous}
     * importance 重要程度{H,BT,SL,match}
     */

    public ClothAttributesData(String ID, int h_cloth, int s_cloth, int v_cloth, String BT_cloth, String SL_cloth) {
        this.ID = ID;
        this.H_cloth = h_cloth;
        this.S_cloth = s_cloth;
        this.V_cloth = v_cloth;
        this.BT_cloth = BT_cloth;
        this.SL_cloth = SL_cloth;
    }

    //進行演算法所需資料建置
    void get_data() {
        get_H();
        s_sring = get_s(S_cloth);
        v_sring = get_v(V_cloth);
        HSV = new int[3];
        HSV[0] = H_cloth;
        HSV[1] = S_cloth;
        HSV[2] = V_cloth;
    }

    //根據加權，進行成績加總
    void sum_score_other(int hsv_like,int bt_like, int sl_like) {

        sum_score = hsv_like*(H_score + SV_score + match_score)/3 + bt_like*BT_score + sl_like*SL_score ;
        Log.d("match_score",String.valueOf(sum_score));
    }
    void sum_score() {

        sum_score = H_score + SV_score + match_score+ BT_score + SL_score ;
        Log.d("65match_score",String.valueOf(sum_score));
    }

    void get_H() {
        int num = H_cloth/30;
        if(H_cloth%30 > 15)
            num++;
        H_num = num;
    }

    String get_s(int S_cloth) {
        if(S_cloth/33<1)
            return "Low";
        else if (S_cloth/33<2)
            return "Medium";
        else
            return "High";
    }
    String get_v(int V_cloth) {
        if(V_cloth/33<1)
            return "Low";
        else if (V_cloth/33<2)
            return "Medium";
        else
            return "High";
    }
}
