package kr.kau.yym7079.Ver_Binary;

import kr.kau.yym7079.Common.Cplex;
import kr.kau.yym7079.Common.ProbDataSet;
import ilog.concert.IloException;

import java.util.LinkedList;
import java.util.Random;

class Solution implements Comparable<Solution>, Cloneable {
/**Variables(Field)**/
//Class Variables
    // Problem Data
    private static int numDepart;
    private static double[] length;
    private static int[][] flow;
    // LP Model
    private static Cplex LPModel;
//Instance Variables
    // DRLP Representation Data
    public boolean[] gammaSeq;
    public boolean[][] alphaSeq;
    public double OFV;

    private LinkedList<Depart> upperSeq;
    private LinkedList<Depart> lowerSeq;

/**Constructor**/
    // Generate random solution
    Solution() throws IloException {
        initGammaSeq();
        initAlphaSeq();
        LPModel.solveLPModel(gammaSeq,alphaSeq);
        setOFV(LPModel.objValue);

        /*upperSeq = new LinkedList<>();
        lowerSeq = new LinkedList<>();
        for (int i = 0; i < gammaSeq.length; i++) {
            boolean isUpperSeq = gammaSeq[i];
            if(isUpperSeq) upperSeq.add(new Depart(i+1,LPModel.getCx()[i]));
            else lowerSeq.add(new Depart(i+1,LPModel.getCx()[i]));
        }

        Collections.sort(upperSeq);
        Collections.sort(lowerSeq);
        out.print("upper sequence: ");
        for(int i=0; i<upperSeq.size();i++)out.print(upperSeq.get(i).index +"\t");
        out.println();

        out.print("lower sequence: ");
        for(int i=0; i<lowerSeq.size();i++)out.print(lowerSeq.get(i).index +"\t");
        out.println();*/
    }
    Solution(boolean[] gammaSeq, boolean[][] alphaSeq) throws Exception {
        setGammaSeq(gammaSeq);
        setAlphaSeq(alphaSeq);
        LPModel.solveLPModel(gammaSeq,alphaSeq);
        setOFV(LPModel.objValue);
    }
    Solution(boolean[] gammaSeq, boolean[][] alphaSeq, double OFV) throws Exception {
        setGammaSeq(gammaSeq);
        setAlphaSeq(alphaSeq);
        setOFV(OFV);
    }
/**Methods**/
    // static methods
    static void setProblemData(ProbDataSet problem){
        numDepart = problem.numDepart;
        length = problem.length;
        flow = problem.flow;
    }
    static void setLPModel(Cplex lpModel){
        LPModel = lpModel;
    }
//-----------------------------------------------------
    // generate initial solution
    private void initGammaSeq() {
        gammaSeq = new boolean[numDepart];
        gammaSeq[0] = true;
        for (int i = 1; i < numDepart; i++) {
            gammaSeq[i] = new Random().nextBoolean();
        }
    }

    private void initAlphaSeq() {
        alphaSeq = new boolean[numDepart][numDepart];
        for (int i = 0; i < numDepart; i++) {
            for (int j = i+1; j < numDepart; j++) {
                if(gammaSeq[i] == gammaSeq[j]){// 같은 Row의 depart만 고려
                    if (alphaSeq[i][j] || alphaSeq[j][i]) continue;// 이미 위치가 결정된 depart는 제외
                    alphaSeq[i][j] = new Random().nextBoolean();
                    alphaSeq[j][i] = !alphaSeq[i][j];
                }
            }
            for (int j = i+1; j < numDepart; j++) {
                if((gammaSeq[i] != gammaSeq[j]) || alphaSeq[i][j])continue;
                for (int k = i+1; k < numDepart; k++) {
                    if((gammaSeq[i] != gammaSeq[k]) || !alphaSeq[i][k])continue;
                    alphaSeq[j][k] = true;
                }
            }
        }
    }

    // set methods
    private void setGammaSeq(boolean[] gammaSeq) {
        this.gammaSeq = gammaSeq;
    }
    private void setAlphaSeq(boolean[][] alphaSeq) {
        this.alphaSeq = alphaSeq;
    }
    private void setOFV(double OFV) {
        this.OFV = OFV;
    }

    // get methods
/*    public boolean[] getGammaSeq(){
        return gammaSeq;
    }
    public boolean[][] getAlphaSeq() {
        return alphaSeq;
    }
    public double getOFV() {
        return OFV;
    }*/

    //-----------------------------------------------------
    @Override
    public int compareTo(Solution s) {
        if(this.OFV > s.OFV){
            return 1;
        }else if(this.OFV < s.OFV){
            return -1;
        }else{
            return 0;
        }
    }
    public Object clone() {
        Object obj = null;
        try{
            obj = super.clone();
        }catch (CloneNotSupportedException e){}
        return obj;
    }

/**Sub Class**/
    class Depart implements Comparable<Depart>{
        private double centroid;
        private int index;

        Depart(int index, double centroid){
            this.centroid = centroid;
            this.index = index;
        }
        public int compareTo(Depart d){
            if(this.centroid > d.centroid){
                return 1;
            }else if(this.centroid < d.centroid){
                return -1;
            }else{
                return 0;
            }
        }
    }
}

