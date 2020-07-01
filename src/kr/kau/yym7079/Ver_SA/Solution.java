package kr.kau.yym7079.Ver_SA;

import ilog.concert.IloException;
import kr.kau.yym7079.Common.Cplex;
import kr.kau.yym7079.Common.ProbDataSet;
import kr.kau.yym7079.Common.Generator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Solution implements Comparable<Solution>, Cloneable {
/**Variables(Field)**/
//Class Variables
    // Problem Data
    private static int numDepart;
    private static double[] length;
    private static double[][] flow;
    private static double[][] clearance;
    private static boolean isClearanceNull = true;
    // LP Model
    private static Cplex LPModel;
//Instance Variables
    // DRLP Representation Data
    LinkedList<Integer> departSeq;
    Depart[] departs;
    ArrayList<Integer> upperDepartSeq;
    ArrayList<Integer> lowerDepartSeq;
    int cutPoint;
    double[] cx;
    // Objective Function Value
    double OFV;
    double OFVByLength;
    double OFVByNumber;
/**Constructor**/
    Solution() throws IloException {
        representSol();

        Vector<ArrayList<Integer>> tmpSeq = setCutPoint();
        upperDepartSeq = tmpSeq.get(0);
        lowerDepartSeq = tmpSeq.get(1);

        //this.OFV = evaluateSol(LPModel);
    }
    Solution(LinkedList<Integer> departSeq) throws IloException {
        representSol(departSeq);

        Vector<ArrayList<Integer>> tmpSeq = setCutPoint();
        upperDepartSeq = tmpSeq.get(0);
        lowerDepartSeq = tmpSeq.get(1);

        //this.OFV = evaluateSol(LPModel);
    }
    Solution(Solution solution){ // object deep copy
        this.departSeq = new LinkedList<>(solution.departSeq);

        this.departs = new Depart[numDepart];
        for (int i = 0; i < numDepart; i++) this.departs[i] = new Depart(solution.departs[i]);

        this.upperDepartSeq = new ArrayList<>(solution.upperDepartSeq);
        this.lowerDepartSeq = new ArrayList<>(solution.lowerDepartSeq);

        this.cutPoint = solution.cutPoint;

        if(this.cx != null) this.cx = Arrays.copyOf(solution.cx,solution.cx.length);
        this.OFVByLength = solution.OFVByLength;
        this.OFVByNumber = solution.OFVByNumber;
        this.OFV = solution.OFV;
    } //deep copy constructor
/**Methods**/
//Static Methods
    public static void setProblemData(ProbDataSet problem) {
        numDepart = problem.numDepart;
        length = problem.length;
        flow = problem.flow;
        if (problem.clearance == null) {
            return;
        }
        isClearanceNull = false;
        clearance = problem.clearance;
    }
    public static void setLPModel(Cplex lpModel) {
        LPModel = lpModel;
    }
//Instance Methods
    // generate solution
    private void representSol(){
        departs = new Depart[numDepart];
        this.departSeq = new LinkedList<>(Generator.getRandomPermute(numDepart));
        for (int i = 0; i < numDepart; i++) {
            departs[i] = new Depart(this.departSeq.get(i));
        }
    }
    private void representSol(LinkedList<Integer> departSeq){
        departs = new Depart[numDepart];
        this.departSeq = new LinkedList<>();
        this.departSeq.addAll(departSeq); //deep copy
        for (int i = 0; i < numDepart; i++) {
            departs[i] = new Depart(this.departSeq.get(i));
        }
    }

    private Vector<ArrayList<Integer>> setCutPoint() throws IloException {
        int cutPointByNum = setCutPointByNumber();
        Vector<ArrayList<Integer>> seqByNum = cutDepartSeq(cutPointByNum);
        OFVByNumber = getOFVByLP(LPModel, seqByNum.get(0), seqByNum.get(1));

        int cutPointByLen = setCutPointByLength();
        if (cutPointByLen == cutPointByNum){
            cutPoint = cutPointByNum;
            this.OFV = OFVByNumber;
            return seqByNum;
        }else{
            Vector<ArrayList<Integer>> seqByLen = cutDepartSeq(cutPointByLen);
            OFVByLength = getOFVByLP(LPModel, seqByLen.get(0), seqByLen.get(1));

            if (OFVByLength < OFVByNumber){
                cutPoint = cutPointByLen;
                this.OFV = OFVByLength;
                return seqByLen;
            }else{
                cutPoint = cutPointByNum;
                this.OFV = OFVByNumber;
                return seqByNum;
            }
        }
    }
    private int setCutPointByNumber(){
        int cutPoint;
        if (numDepart % 2 == 0) cutPoint = numDepart / 2; // if numDepart is Even
        else{ // if numDepart is Odd
            cutPoint = Math.round(numDepart/2.0f);
        }
        return cutPoint;
    }
    private int setCutPointByLength(){
        int m = 1;
        int k = numDepart;
        double upperLength = departs[0].length;
        double lowerLength = departs[numDepart-1].length;

        for (int i = 0; i < (numDepart - 2); i++) {
            if(upperLength < lowerLength){
                m++; upperLength += departs[m-1].length;
                if (isClearanceNull) continue;
                upperLength += departs[m-1].clearance[departs[m-2].index-1];
            }else if(upperLength > lowerLength) {
                k--; lowerLength += departs[k-1].length;
                if (isClearanceNull) continue;
                upperLength += departs[k-1].clearance[departs[k-2].index-1];
            }else{
                double rn = ThreadLocalRandom.current().nextDouble();
                if(rn < 0.5){
                    m++; upperLength += departs[m-1].length;
                    if (isClearanceNull) continue;
                    upperLength += departs[m-1].clearance[departs[m-2].index-1];
                }else{
                    k--; lowerLength += departs[k-1].length;
                    if (isClearanceNull) continue;
                    upperLength += departs[k-1].clearance[departs[k-2].index-1];
                }
            }
        }
        return m;
    }

    private void setDepartCentroid(int cutPoint) {
        // 모든 department 중 한 개는 무조건 윗줄로 고정
        boolean upperLower;
        int index1 = departSeq.indexOf(1);
        upperLower = index1 < cutPoint;
        //======================================================================
        double startPoint = 0.0;
        upperDepartSeq = new ArrayList<>();
        lowerDepartSeq = new ArrayList<>();

        for (int i = 0; i < numDepart; i++) {
            Depart depart = departs[i];
            if (i == cutPoint){
                startPoint = 0;
                depart.start = 0;
                upperLower = !upperLower;
            }
            if(upperLower){
                upperDepartSeq.add(depart.index);
            }
            else{
                lowerDepartSeq.add(depart.index);
            }
            if(!isClearanceNull){ // clearance instance case
                if(startPoint != 0) startPoint += depart.clearance[departs[i-1].index-1];
            }
            depart.start += startPoint;
            depart.updateCentroid();
            startPoint += depart.length;
        }
    }
    private Vector<ArrayList<Integer>> cutDepartSeq(int cutPoint){
        // 모든 department 중 한 개는 무조건 윗줄로 고정
        boolean upperLower = true;
        /*int index1 = departSeq.indexOf(1);
        if (index1 < cutPoint){
            upperLower = true;
        }else{
            upperLower = false;
        }*/
        Vector<ArrayList<Integer>> result = new Vector<>(2);
        ArrayList<Integer> upperSeq = new ArrayList<>(cutPoint);
        ArrayList<Integer> lowerSeq = new ArrayList<>(numDepart-cutPoint);

        for (int i = 0; i < numDepart; i++) {
            if (i == cutPoint){
                upperLower = !upperLower;
            }
            if(upperLower){
                upperSeq.add(departs[i].index);
            }else{
                lowerSeq.add(departs[i].index);
            }
        }
        result.add(upperSeq);
        result.add(lowerSeq);
        return result;
    }

    private double evaluateSol(Cplex model) throws IloException {
        model.solveDRLPModel(departSeq,cutPoint);
        double OFV = model.objValue;
        //centroid point update
        for (Depart depart : departs) {
            depart.centroid = model.cx[depart.index-1];
        }
        cx = model.cx.clone();

        //ArrayList<Integer> reverseSeq = new ArrayList<>(lowerDepartSeq);
        LinkedList<Integer> reverseSeq = new Generator().inversionOperator(departSeq,cutPoint,numDepart);
        //Collections.reverse(reverseSeq);
        //model.solveDRLPModel(upperDepartSeq,reverseSeq);
        model.solveDRLPModel(reverseSeq,cutPoint);
        if (model.objValue < OFV) {
            OFV = model.objValue;
            //lower depart sequence update
            departSeq = new LinkedList<>(reverseSeq);
            //centroid point update
            for (Depart depart : departs) {
                depart.centroid = model.cx[depart.index-1];
            }
            cx = model.cx;
        }
        OFV = Math.round(OFV*1000000.0)/1000000.0;
        return OFV;
    }
    private double getOFVByLP(Cplex model, ArrayList<Integer> upperSeq, ArrayList<Integer> lowerSeq) throws IloException {
        model.solveDRLPModel(upperSeq,lowerSeq);
        double OFV = model.objValue;

        ArrayList<Integer> reverseSeq = new ArrayList<>(lowerSeq);
        //ArrayList<Integer> reverseSeq2 = new ArrayList<>(new Generator().inversionOperator(departSeq,cutPoint,numDepart));
        Collections.reverse(reverseSeq);
        model.solveDRLPModel(upperSeq,reverseSeq);

        return Math.min(model.objValue, OFV);
    }
    private void setOFV(boolean lenOrNum){
        if (lenOrNum){
            this.OFV = OFVByLength;
        }else{
            this.OFV = OFVByNumber;
        }
    }

    public int compareTo(Solution s) {
            return Double.compare(this.OFV, s.OFV);
            }
    public Solution clone() throws CloneNotSupportedException {
            return (Solution) super.clone();
    }

    static class Depart{
        double start;
        double centroid = 0.0;
        int index;
        double length;
        double[] flow;
        double[] clearance;

        Depart(int index){
            this.start = 0.0;
            this.index = index;
            this.length = Solution.length[index-1];
            this.flow = Solution.flow[index-1];
            if (Solution.isClearanceNull) {
                return;
            }
            this.clearance = Solution.clearance[index-1];
        }
        Depart(Depart department){
            this.start = department.start;
            this.centroid = department.centroid;
            this.index = department.index;
            this.length = department.length;
            this.flow = department.flow.clone();
            if (Solution.isClearanceNull) {
                return;
            }
            this.clearance = department.clearance.clone();
        } // object deep copy
        void updateCentroid(){
            centroid = start + length/2.0;
        }
    }
}
