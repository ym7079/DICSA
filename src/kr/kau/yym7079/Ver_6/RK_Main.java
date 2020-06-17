package kr.kau.yym7079.Ver_6;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.out;

import kr.kau.yym7079.Common.Cplex;


/** Single-row facility layout problem(SRFLP) with Discrete Cuckoo Search Algorithm(DCS)
    Double-row layout problem(DRLP) with Discrete Cuckoo Search Algorithm(DCS) **/
/** Discrete Method : Random-Key + Local Search(2-Opt) encoding scheme **/
class RK_Main {
    public static LinkedList<Solution> bestParamNest;
////instance 관련 parameter
    private static final String LAYOUT_PROB_TYPE = "DR";
    private static final String PROB_TYPE = "Am"; //instance 문제 유형
    //private static final boolean IS_CLASSICAL = (PROB_TYPE == "Classical");
    private static final String INSTANCE_NAME = "11a"; //instance 문제 이름
    private static final double OPTIMAL_OFV = 5559;
////Dummy 관련 parameter
    private static final boolean IS_DUMMY = false; // dummy를 만들지 여부
    private static final int NUM_DUMMY = 10; // dummy department 수
    private static final double LENGTH_DUMMY = 0.5; // dummy department 길이
//Experiment 관련 parameter
    private static final double ALPHA = 0.3;

    private static final int STOP_REP =  10;
    private static final int STOP_ITER =  2000;
    static public int numIter;
    static public int tempIter = 0;

    /**Main Method**/
    public static void main(String[] args) throws Exception {
        boolean IS_CLASSICAL = (PROB_TYPE == "Classical");
        //createDataset(IS_CLASSICAL);
        //int configNum; // configuration (= parameter set) number

        ArrayList<String> probNameSet = new ArrayList<>();
        ArrayList<Integer> hostNestNumSet = new ArrayList<>(); // number of host nests (or the population size n)
        ArrayList<Double> probASet = new ArrayList<>(); // probability Pa
        ArrayList<Double> probCSet = new ArrayList<>(); // probability Pc

        LinkedList<Solution> bestSolution = new LinkedList<>();

        OutputWriter.setDirName(IS_CLASSICAL, PROB_TYPE);
        OutputWriter report = new OutputWriter();

        readProbNameSet(probNameSet);// 각 문제들의 파일명을 읽어들이는 메소드
        for(String probName : probNameSet) {
            if (probName.contains(INSTANCE_NAME)) {

                OutputWriter summary = new OutputWriter(probName);
                summary.writeln_clock();

                out.println("############################");
                out.println("Problem Name: " + probName);
                out.println("############################");

            //set instance data
                ProbDataSet.init(probName, IS_DUMMY);
                Solution.setProblemData();
                Generator.setProblemData();
            //set LP model
                Cplex LPModel = new Cplex(ProbDataSet.numDepart,ProbDataSet.flow,ProbDataSet.length,ProbDataSet.totalLength,ProbDataSet.clearance);
                LPModel.initDRLPModel();
                Solution.setModel(LPModel);
            //read Cuckoo search algorithm parameters
                readParameterSet(probName, hostNestNumSet, probASet, probCSet, IS_CLASSICAL);

                bestParamNest = new LinkedList<>();

                for (int HostNestNum : hostNestNumSet) {
                    for (double Pa : probASet) {
                        for (double Pc : probCSet) {
                            if (Pa == 0.0 && Pc != 0.0) continue;
                            if (Pa == 0.0 || Pa == 0.25/*||Pa ==0.35*/) {
                                out.println("population size: " + HostNestNum + "/  probability a: " + Pa + "/  probability c: " + Pc + "-------------------------");
                                summary.writeln_parameters(HostNestNum, Pa);
                                CS_Algorithm(summary, HostNestNum, Pa, Pc, ALPHA);
                            }
                        }
                    }
                }
                summary.close();

                Solution bestInstanceNest = Collections.min(bestParamNest);
                bestSolution.add(bestInstanceNest);
            }
        }
        // Instance 별 best 값 기록
        for (Solution nest : bestSolution){
            report.writeln_report(nest);
        }
        report.close();
        out.println("Successfully finished.\n");
    }
    /**Create the Name of Problem Instance or the Value of Cuckoo Search Algorithm **/
    private static void createDataset(boolean IS_CLASSICAL) throws Exception {
        CreatingDataset.createProbNameDir();

        if (!IS_CLASSICAL){
            String problemNameFilePath = CreatingDataset.probNameFolderDir+"/prob"+ PROB_TYPE; //파일 경로
            // 해당 파일(문제유형)이 없을 경우(새로운 문제유형인 경우), 관련 폴더 및 데이터 파일들을 생성함
            if(!new File(problemNameFilePath).exists()){
                String probName= "";
                CreatingDataset.createDataset(PROB_TYPE, probName);
            }
        }
    }
    /** The Body of Random-Key Discrete Cuckoo Search Algorithm **/
    private static void CS_Algorithm(OutputWriter summary, int HostNestNum, double Pa, double Pc, double alpha)throws Exception{
        Experiment exp;
        OutputWriter result;
        exp = new Experiment(HostNestNum,Pa,Pc,alpha);
        result = new OutputWriter(HostNestNum,Pa);
        double bestOFV = 0;

        LinkedList<Solution_SR> Nest_Set = new LinkedList<>();
        LinkedList<Solution_DR> nestSet_DR = new LinkedList<>();
        LinkedList<Solution> nestSet = new LinkedList<>();
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        /** Replication Part ===========================================================================================================**/
        int numRep = 0;
        while (numRep++ < STOP_REP){
            //Cuckoo Search Algorithm part =================================================================================================
          /**The first phase:  Random initial solutions */
            //Generating Initial Population of n host nests X_i(초기 해집단 생성)
            Solution.bestOFV = Double.MAX_VALUE;
            numIter = 0;
            exp.initHostNest(LAYOUT_PROB_TYPE);
            //exp.initHostNest();

            Solution_SR currBestNest_SR;
            Solution_DR currBestNest_DR;
            Solution currBestSolution;

          /**Starting iterations ================================================================================================*/
            int checkIter = 0;
            long startTime = System.currentTimeMillis();	// algorithm start time
            long startIterTime = 0;
            Solution.isIterOver1500 = false;
            while (numIter++ < STOP_ITER){
                checkIter ++;
                if(numIter % 100 == 0 && numIter >= 100){
                    if(LAYOUT_PROB_TYPE =="SR")exp.initHostNest(exp.currBestCuckoo_SR);
                    else if(LAYOUT_PROB_TYPE =="DR")exp.initHostNest(new Solution_DR(Solution.bestDepartSeq));
                }
                if (numIter == 1500) {
                    Solution.isIterOver1500 = true;
                    double currBest = Solution.bestOFV;
                    Solution_DR tmpBest = new Solution_DR(Solution.bestDepartSeq);
                    if(tmpBest.OFV < currBest){
                        out.println("currBestOFV update by LP : "  + currBest + "->" + tmpBest.OFV);
                        exp.currBestCuckoo_DR = tmpBest;
                    }
                }
                else if(numIter < 1500){
                    Solution.isIterOver1500 = false;
                }
              /** The second phase: Start Searching with a fraction Pc of Smart Cuckoos **/
              //smart cuckoos begin by exploring new areas from the current solutions ==> diversification
                if(Pc != 0.0) exp.searchWithSmartCuckoos(LAYOUT_PROB_TYPE);
                /*Collections.sort(exp.HostNest_SR);
                bestIdx = 0;*/
              /***The third phase: Employ one cuckoo to search for a new good solution,starting from the best solution of the population
                                   (The global explorative random walk) */
                exp.employOneCuckoo(LAYOUT_PROB_TYPE);

                if(Pa == 0.0) {
                    continue;
                }
                //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
              /***The last phase: Abandon a fraction Pa of worse solutions that will be replaced by new ones 2. The local random walk */
                // A fraction (Pa) of worse nests are abandoned and completely new nests(with new random solutions) are built
                exp.abandonWorseNests(LAYOUT_PROB_TYPE);
                // Sorting New Population of n host nests
                if(numIter == 0)startIterTime = System.currentTimeMillis();
            }/** End of iterations ===============================================================================================================================*/
            //Cuckoo Search Algorithm part
            //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
          /** Post-optimization processing ========================================================================================*/
            //Time spent searching for answers
            long endTime = System.currentTimeMillis(); // algorithm end time
            out.println(STOP_ITER +"iteration process time: "+(endTime-startIterTime)/1000.0);
            double CPU_time = (endTime - startTime)/ 1000.0;
            out.println("Total Process Time : "+CPU_time+"sec");
            //out.println("check Iteration : "+checkIter);
            //out.println("temp Iteration : "+tempIter);
            tempIter = 0;
            if(LAYOUT_PROB_TYPE =="SR"){
                //Best OFV update
                currBestNest_SR = exp.currBestCuckoo_SR;
                currBestNest_SR.CPUTime = CPU_time;
                bestOFV = currBestNest_SR.OFV;
                // record the OFV & CPU time for each Replication
                Nest_Set.add(currBestNest_SR);
                // write to result OutputFile
                result.writeln_result(bestOFV,CPU_time, currBestNest_SR.departSeq);
                //Best OFV print
                out.print(currBestNest_SR.departSeq+"\t");
                out.println("best OFV: "+ currBestNest_SR.OFV+"\tIteration: "+ currBestNest_SR.iterNum);
            }else if(LAYOUT_PROB_TYPE =="DR"){
                //Best OFV update
                currBestNest_DR = new Solution_DR(Solution.bestDepartSeq);
                currBestNest_DR.CPUTime = CPU_time;
                bestOFV = Solution.bestOFV;
                // record the OFV & CPU time for each Replication
                nestSet_DR.add(currBestNest_DR);
                // write to result OutputFile
                result.writeln_result(bestOFV,CPU_time,currBestNest_DR.departSeq);
                //Best OFV print
                out.print(Solution.bestDepartSeq+"\t");
                out.println("Final Best OFV: "+ Solution.bestOFV+"\tIteration: "+Solution.iterNum);
                out.println("- Upper Row: ");
                for (Integer integer : Solution_DR.bestUpper) {
                    out.print(" ");
                    out.print(integer+"\t\t");
                }
                System.out.println();
                for (Integer integer : Solution_DR.bestUpper) {
                    out.print(" ");
                    out.print(currBestNest_DR.cx[integer-1]+"\t");
                }
                System.out.println();
                out.println("- Lower Row: ");
                for (Integer integer : Solution_DR.bestLower) {
                    out.print(" ");
                    out.print(integer+"\t\t");
                }
                System.out.println();
                for (Integer integer : Solution_DR.bestLower) {
                    out.print(" ");
                    out.print(currBestNest_DR.cx[integer-1]+"\t");
                }
                out.println();
            }
        }/** End of Replication Part ===========================================================================================**/
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        //out.println("Avg OFV/\tTotal Process Time/\toptimal 횟수");
        double sumOFV = 0.0;
        double sumTime = 0.0;
        int numOptimal = 0;
        Solution bestNest = null;
        //Solution_DR bestNest_DR;
        if(LAYOUT_PROB_TYPE =="SR"){
            bestNest = Collections.min(Nest_Set); // 현재 파라미터 설정에서 가장 좋은 해를 저장
            for(Solution_SR sol: Nest_Set){
                sumOFV += sol.OFV;
                sumTime += sol.CPUTime;
                if(sol.OFV == OPTIMAL_OFV) numOptimal++;
            }
            bestOFV = bestNest.OFV;
        }
        else if(LAYOUT_PROB_TYPE == "DR"){
            bestNest = Collections.min(nestSet_DR); // 현재 파라미터 설정에서 가장 좋은 해를 저장
            for(Solution_DR sol: nestSet_DR){
                sumOFV += sol.OFV;
                sumTime += sol.CPUTime;
                if(sol.OFV == OPTIMAL_OFV) numOptimal++;
            }
            bestOFV = bestNest.OFV;
        }
        double avgOFV = sumOFV/ STOP_REP;
        double sumDev = 0;
        for(Solution_DR sol: nestSet_DR){
            sumDev += Math.pow(avgOFV - sol.OFV,2);
        }
        double STD = Math.sqrt(sumDev / STOP_REP);
        out.print("average OFV = "+ avgOFV + "/\t");
        out.print("STD value = " +STD+ "/\t");
        out.print("Total Process Time = "+ sumTime/ STOP_REP + "/\t");
        out.println("optimal 횟수 = " +numOptimal);

        //Best OFV update & print

        out.println("bestOFV = "+ bestOFV+"\n");
        result.close();

        bestNest.HostNestNum = HostNestNum;
        bestNest.Pa = Pa;
        bestParamNest.add(bestNest);
        if(LAYOUT_PROB_TYPE =="SR")summary.writeln_summary_SR(Nest_Set/*OFV_Set,CPUTime_Set*/,bestParamNest.size()-1);
        else if(LAYOUT_PROB_TYPE =="DR")summary.writeln_summary_DR(nestSet_DR/*OFV_Set,CPUTime_Set*/,bestParamNest.size()-1);
    }//CS Algorithm
    /**Read the Name of Problem Instance or the Value of Cuckoo Search Algorithm **/
    private static void readProbNameSet(ArrayList<String>probName_set) throws Exception {
        File file;
        if(PROB_TYPE == "Classical") file = new File("dataset/Problems/prob"+ LAYOUT_PROB_TYPE + PROB_TYPE);
        else file = new File("dataset/Problems/prob"+ PROB_TYPE);
        try{
            BufferedReader br = new BufferedReader(new FileReader(file)); // FileReader(): 파일 읽어오기
            String[] strings = br.readLine().split(",");//br의 내용을 ,에 따라 나눠 strs 문자열 배열에 저장
            Collections.addAll(probName_set, strings);// 문자열 배열strs 을 ArrayList에 그대로 옮기기
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void readParameterSet(String probName, ArrayList<Integer> HostNestNum_set, ArrayList<Double> ProbA_set, ArrayList<Double> ProbC_set, boolean isClassical){
        if(isClassical){
            if (probName.contains("Small"))readParameterSet(probName,HostNestNum_set,ProbA_set,ProbC_set,"Small");
            else readParameterSet(probName,HostNestNum_set,ProbA_set,ProbC_set,"");
        }
        else readParameterSet(probName,HostNestNum_set,ProbA_set,ProbC_set, PROB_TYPE);
    }
    private static void readParameterSet(String probName, ArrayList<Integer> HostNestNum_set, ArrayList<Double> ProbA_set, ArrayList<Double> ProbC_set,String probType) {
        /** File Usage
         *  file path -> dataset/Parameters/
         *  file name -> %probName%.parameter
         *  contents are comma-seperate
         *  contenst order -> HostNestNum, P_a (one for each line)
         */
        HostNestNum_set.clear(); ProbA_set.clear(); ProbC_set.clear();
        if (probName.contains("Small")) probType = "Small";
        probType = probType+"/";
        File file = new File("dataset/Parameters/"+ probType+ probName + ".parameter");
        String[] strs;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            strs = br.readLine().split(",");
            for (String str : strs) {
                HostNestNum_set.add(Integer.parseInt(str));
            }
            strs = br.readLine().split(",");
            for (String str : strs) {
                ProbA_set.add(Double.parseDouble(str));
            }
            strs = br.readLine().split(",");
            for (String str : strs) {
                ProbC_set.add(Double.parseDouble(str));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
    /** The Parameter values **/
    public static int getNumDummy() {
        return NUM_DUMMY;
    }
    public static double getLengthDummy() { return LENGTH_DUMMY; }
}//Main Class
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
abstract class Solution implements Comparable<Solution>, Cloneable {
    // Solution
    LinkedList<Integer> departSeq;
    LinkedList<Double> keySeq;
    //LinkedList<Depart> departs;
    Depart[] departs;
    double OFV;
    //best Solution
    static LinkedList<Integer> bestDepartSeq;
    static double bestOFV;
    static int iterNum;
    //record data
    double CPUTime;
    public static boolean isIterOver1500;
    // Instance Solution Data
    static int numDepart;
    static double[] length;
    static double[][] flow;
    static double[][] clearance;
    static boolean isClearanceNull = true;
    // Parameters
    int HostNestNum;
    double Pa;
    // LPModel
    static Cplex model;
// ---------------------------------------------------------------------------------------------------------------------------
// Constructor
// ---------------------------------------------------------------------------------------------------------------------------
    Solution(String layoutProbType) throws Exception {
        if (layoutProbType.equals("SR")) new Solution_SR();
        else if(layoutProbType.equals("DR")) new Solution_DR();
    }
    public Solution() {

    }
// ---------------------------------------------------------------------------------------------------------------------------
//	Static Method : initial setting (초기화 작업)
// ---------------------------------------------------------------------------------------------------------------------------
    static void setProblemData(){
        numDepart = ProbDataSet.numDepart;
        length = ProbDataSet.length;
        flow = ProbDataSet.flow;

        if (ProbDataSet.clearance == null) {
            return;
        }
        isClearanceNull = false;
        clearance = ProbDataSet.clearance;
    }
    static void setModel(Cplex LPModel){
        model = LPModel;
    }
// ---------------------------------------------------------------------------------------------------------------------------
//	Method : FLP Solution 생성
// ---------------------------------------------------------------------------------------------------------------------------
    void representSol() throws Exception {
        keySeq = new LinkedList<>();
        //departs = new LinkedList<>();
        departs = new Depart[numDepart];

        this.departSeq = new LinkedList<>(Generator.randomKeyPermute(this.keySeq));
        /*for (int index : this.departSeq){
            departs.add(new Depart(index));
        }*/
        for (int i = 0; i < numDepart; i++) {
            departs[i] = new Depart(this.departSeq.get(i));
        }
    }
    void representSol(LinkedList<Integer> departSeq) throws Exception {
        keySeq = new LinkedList<>();
        //departs = new LinkedList<>();
        departs = new Depart[numDepart];

        this.departSeq = departSeq;
        /*for (int index : this.departSeq){
            departs.add(new Depart(index));
        }*/
        for (int i = 0; i < numDepart; i++) {
            departs[i] = new Depart(this.departSeq.get(i));
        }
    }
    void evaluateSol() throws Exception {
        double flowValue;
        double distanceValue;
        //초기화
        this.OFV = 0; // Objective Function Value (OFV) = Distance*Flow
        for (int i=0; i<numDepart; i++){
            for(int j=0; j<numDepart; j++){
                if (i == j) continue;
                if (isClearanceNull){
                    if(i > j) continue;
                }
                flowValue = departs[i].flow[departs[j].index-1];
                distanceValue = Math.abs(departs[i].centroid - departs[j].centroid);
                this.OFV += flowValue*distanceValue; // OFV = flow × total distance
            }
        }
    }
//---------------------------------------------------------
    public int compareTo(Solution s) {
        return Double.compare(this.OFV, s.OFV);
}//compareTo()
    public Object clone() {
        Object obj = null;
        try{
            obj = super.clone();
        }catch (CloneNotSupportedException e){}
        return obj;
    }

    static class Depart{
        double start;
        double centroid = 0.0;
        int index;
        double length;
        double[] flow;
        double[] clearance;
        String upperLower;

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
        void updateCentroid(){
            centroid = start + length/2.0;
        }
    }
}//Solution Class

class Solution_SR extends Solution {
    // Solution 생성자
    Solution_SR() throws Exception {
        representSol();
        setDepartCentroid();
        evaluateSol();
    }
    Solution_SR(LinkedList<Integer> departSeq, LinkedList<Double> keySeq) throws Exception {
        representSol(departSeq);
        this.keySeq = keySeq;
        setDepartCentroid();
        evaluateSol();
    }
    Solution_SR(LinkedList<Integer> departSeq) throws Exception {
        representSol(departSeq);
        setDepartCentroid();
        evaluateSol();
    }
// ---------------------------------------------------------------------------------------------------------------------------
//	Method : SR FLP Solution 생성
// ---------------------------------------------------------------------------------------------------------------------------
    private void setDepartCentroid() throws Exception{
        double temp = 0.0;
        for (Depart depart : departs){
            depart.start += temp;
            depart.updateCentroid();
            temp += depart.length;
        }
    }
//---------------------------------------------------------
    public Object clone() {
        Object obj;
        obj = super.clone();
        return obj;
    }
}//Single Row Solution Class

class Solution_DR extends Solution {
    // Double Row로 만들기 위한 요소
    int cutPoint;
    boolean upDown;
    static double[] cx;
    static ArrayList<Integer> bestUpper;
    static ArrayList<Integer> bestLower;

    ArrayList<Integer> upperDepartSeq;
    ArrayList<Integer> lowerDepartSeq;

    // Solution 생성자
    Solution_DR() throws Exception {
        representSol();

        // Set cutPoint
        double rn = ThreadLocalRandom.current().nextDouble();
        if(rn < 0.5){
            cutPoint = setCutPoint();
        }else{
            //cutPoint = ThreadLocalRandom.current().nextInt(numDepart);
            cutPoint = Math.round(numDepart/2.0f);
        }

        setDepartCentroid(cutPoint);

        /*double rd = ThreadLocalRandom.current().nextDouble();
        if(rd < 0.5){
            upDown = true;
            setDepartCentroid(upDown);
        }else{
            cutPoint = Math.round(numDepart/2.0f);
            setDepartCentroid(cutPoint);
        }*/
        if(isIterOver1500) {
            evaluateSol(model);
        }
        else evaluateSol();

        if(this.OFV < bestOFV) {
            bestUpper = new ArrayList<>(upperDepartSeq);
            bestLower = new ArrayList<>(lowerDepartSeq);
            bestOFV = this.OFV;
            bestDepartSeq = this.departSeq;
            if(model.cx != null) cx = model.cx.clone();
            iterNum = RK_Main.numIter;
            out.print(bestDepartSeq + "\t");
            out.println("best OFV update : " + bestOFV + "\tIteration: " + iterNum);
            RK_Main.numIter = 0;
        }
    }
    Solution_DR(LinkedList<Integer> departSeq) throws Exception {
        representSol(departSeq);

        // Set cutPoint
        double rn = ThreadLocalRandom.current().nextDouble();
        if(rn < 0.5){
            cutPoint = setCutPoint();
        }else{
            //cutPoint = ThreadLocalRandom.current().nextInt(numDepart);
            cutPoint = Math.round(numDepart/2.0f);
        }
        setDepartCentroid(cutPoint);
        /*double rd = ThreadLocalRandom.current().nextDouble();
        if(rd < 0.5){
            upDown = true;
            setDepartCentroid(upDown);
        }else{
            cutPoint = Math.round(numDepart/2.0f);
            setDepartCentroid(cutPoint);
        }*/
        /*evaluateSol();
        if(this.OFV <= bestOFV*1.2) {
            evaluateSol(model);
        }*/
        if(isIterOver1500) {
            evaluateSol(model);
        }
        else evaluateSol();

        if(this.OFV < bestOFV) {
            bestUpper = new ArrayList<>(upperDepartSeq);
            bestLower = new ArrayList<>(lowerDepartSeq);
            bestOFV = this.OFV;
            bestDepartSeq = this.departSeq;
            if(model.cx != null) cx = model.cx.clone();
            iterNum = RK_Main.numIter;
            out.print(bestDepartSeq + "\t");
            out.println("best OFV update : " + bestOFV + "\tIteration: " + iterNum);
            RK_Main.numIter = 0;
        }
    }
    Solution_DR(LinkedList<Integer> departSeq, LinkedList<Double> keySeq) throws Exception {
        representSol(departSeq);
        this.keySeq = keySeq;

        // Set cutPoint
        double rn = ThreadLocalRandom.current().nextDouble();
        if(rn < 0.5){
            cutPoint = setCutPoint();
        }else{
            //cutPoint = ThreadLocalRandom.current().nextInt(numDepart);
            cutPoint = Math.round(numDepart/2.0f);
        }

        setDepartCentroid(cutPoint);

        /*double rd = ThreadLocalRandom.current().nextDouble();
        if(rd < 0.5){
            upDown = true;
            setDepartCentroid(upDown);
        }else{
            cutPoint = Math.round(numDepart/2.0f);
            setDepartCentroid(cutPoint);
        }*/
        /*evaluateSol();
        if(this.OFV <= bestOFV*1.2) {
            evaluateSol(model);
        }*/
        if(isIterOver1500) {
            evaluateSol(model);
        }
        else evaluateSol();

        if(this.OFV < bestOFV) {
            bestUpper = new ArrayList<>(upperDepartSeq);
            bestLower = new ArrayList<>(lowerDepartSeq);
            bestOFV = this.OFV;
            bestDepartSeq = this.departSeq;
            if(model.cx != null) cx = model.cx.clone();
            iterNum = RK_Main.numIter;
            out.print(bestDepartSeq + "\t");
            out.println("best OFV update : " + bestOFV + "\tIteration: " + iterNum);
            RK_Main.numIter = 0;
        }
    }
// --------------------------------------------------------
//	Method : Double Row FLP Solution 생성
// --------------------------------------------------------
    private int setCutPoint(){
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
        boolean upperLower = true;
        for (int i = 0; i < numDepart; i++) {
            if(departs[i].index == 1){
                if (i < cutPoint){
                    upperLower = true;
                }else{
                    upperLower = false;
                }
                break;
            }
        }
    //======================================================================
        double startPoint = 0.0;
        upperDepartSeq = new ArrayList<>();
        lowerDepartSeq = new ArrayList<>();

        int lengthOfDeparts = departs.length;
        for (int i = 0; i < lengthOfDeparts; i++) {
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
    private void setDepartCentroid(boolean upDown) {
        double temp_upper = 0.0;
        double temp_lower = 0.0;
        upperDepartSeq = new ArrayList<>();
        lowerDepartSeq = new ArrayList<>();

        for (Depart depart : departs){
            if (upDown) {
                upperDepartSeq.add(depart.index);
                depart.start += temp_upper;
                depart.updateCentroid();
                depart.upperLower = "Upper";
                temp_upper += depart.length;
            }
            else {
                lowerDepartSeq.add(depart.index);
                depart.start += temp_lower;
                depart.updateCentroid();
                depart.upperLower = "Lower";
                temp_lower += depart.length;
            }

            if(temp_upper > temp_lower) upDown = false;
            else if(temp_upper < temp_lower) upDown = true;
            else upDown = ThreadLocalRandom.current().nextBoolean();
        }
    }
    private void evaluateSol(Cplex model) throws Exception {
        model.solveLPModel(upperDepartSeq,lowerDepartSeq);
        RK_Main.tempIter++;

        //OFV update
        this.OFV = model.objValue;

        //centroid point update
        for (Depart depart : departs) {
            depart.centroid = model.cx[depart.index-1];
        }
    }
    public Object clone() {
        Object obj;
        obj = super.clone();
        return obj;
    }
}//Double Row Solution Class
class Solution_UA extends Solution {

}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%