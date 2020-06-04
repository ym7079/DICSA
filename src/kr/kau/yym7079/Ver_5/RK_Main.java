package kr.kau.yym7079.Ver_5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static java.lang.System.out;

import kr.kau.yym7079.Common.Cplex;


/** Single-row facility layout problem(SRFLP) with Discrete Cuckoo Search Algorithm(DCS)
    Double-row layout problem(DRLP) with Discrete Cuckoo Search Algorithm(DCS) **/
/** Discrete Method : Random-Key + Local Search(2-Opt) encoding scheme **/
class RK_Main {
    public static LinkedList<Solution> bestParamNest;
////instance 관련 parameter
    private static final String LAYOUT_PROB_TYPE = "DR";
    private static final String PROB_TYPE = "Classical"; //instance 문제 유형
    private static final boolean IS_CLASSICAL = (PROB_TYPE == "Classical");
    private static final String INSTANCE_NAME = "60-1"; //instance 문제 이름
    private static final double OPTIMAL_OFV = 739233.0;
////Dummy 관련 parameter
    private static final boolean IS_DUMMY = false; // dummy를 만들지 여부
    private static final int NUM_DUMMY = 10; // dummy department 수
    private static final double LENGTH_DUMMY = 0.5; // dummy department 길이
//Experiment 관련 parameter
    private static final double ALPHA = 0.3;

    private static final int STOP_REP = 5;
    private static final int STOP_ITER =  2000;
    static public int numIter;
    static public int tempIter = 0;

    /**Main Method**/
    public static void main(String[] args) throws Exception {
        createDataset();
        //int configNum; // configuration (= parameter set) number

        ArrayList<String> probNameSet = new ArrayList<>();
        ArrayList<Integer> HostNestNum_set = new ArrayList<>(); // number of host nests (or the population size n)
        ArrayList<Double> ProbA_set = new ArrayList<>(); // probability Pa

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
            //set LP model
                Cplex LPModel = new Cplex(ProbDataSet.numDepart,ProbDataSet.flow,ProbDataSet.length,ProbDataSet.totalLength);
                LPModel.setInitDRLPModel();
                Solution.setModel(LPModel);
            //read Cuckoo search algorithm parameters
                readParameterSet(probName, HostNestNum_set, ProbA_set, IS_CLASSICAL);

                bestParamNest = new LinkedList<>();

                for (int HostNestNum : HostNestNum_set) {
                    for (double Pa : ProbA_set) {
                        if(Pa == 0.2||Pa == 0.25/*||Pa ==0.35*/) {
                            out.println("population size: " + HostNestNum + ",probability a: " + Pa);
                            summary.writeln_parameters(HostNestNum, Pa);
                            CS_Algorithm(summary, HostNestNum, Pa, ALPHA);
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
    private static void createDataset() throws Exception {
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
    /**Read the Name of Problem Instance or the Value of Cuckoo Search Algorithm **/
    private static void readProbNameSet(ArrayList<String>probName_set) throws Exception {
        File file = new File("dataset/Problems/prob"+ LAYOUT_PROB_TYPE + PROB_TYPE);
        try{
            BufferedReader br = new BufferedReader(new FileReader(file)); // FileReader(): 파일 읽어오기
            String[] strings = br.readLine().split(",");//br의 내용을 ,에 따라 나눠 strs 문자열 배열에 저장
            Collections.addAll(probName_set, strings);// 문자열 배열strs 을 ArrayList에 그대로 옮기기
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void readParameterSet(String probName, ArrayList<Integer> HostNestNum_set, ArrayList<Double> ProbA_set,boolean isClassical){
        if(isClassical)readParameterSet(probName,HostNestNum_set,ProbA_set,"");
        else readParameterSet(probName,HostNestNum_set,ProbA_set, PROB_TYPE);
    }
    private static void readParameterSet(String probName, ArrayList<Integer> HostNestNum_set, ArrayList<Double> ProbA_set,String probType) {
        /** File Usage
         *  file path -> dataset/Parameters/
         *  file name -> %probName%.parameter
         *  contents are comma-seperate
         *  contenst order -> HostNestNum, P_a (one for each line)
         */
        HostNestNum_set.clear(); ProbA_set.clear();
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
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
    /** The Parameter values **/
    public static int getNumDummy() {
        return NUM_DUMMY;
    }
    public static double getLengthDummy() { return LENGTH_DUMMY; }
    /** The Body of Random-Key Discrete Cuckoo Search Algorithm **/
    private static void CS_Algorithm(OutputWriter summary, int HostNestNum, double Pa, double alpha)throws Exception{
        Experiment exp;
        OutputWriter result;
        exp = new Experiment(HostNestNum,Pa,alpha);
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
            //exp.initHostNest(layoutProbType);
            exp.initHostNest();
            Solution_SR currBestNest_SR;
            Solution_DR currBestNest_DR;
            Solution currBestSolution;

          /**Starting iterations ================================================================================================*/
            numIter = 0;
            int checkIter = 0;
            long startTime = System.currentTimeMillis();	// algorithm start time
            long startIterTime = 0;
            while (numIter++ < STOP_ITER){
                checkIter ++;
                if(numIter % 100 == 0 && numIter >= 100){
                    if(LAYOUT_PROB_TYPE =="SR")exp.initHostNest(exp.currBestCuckoo_SR);
                    else if(LAYOUT_PROB_TYPE =="DR")exp.initHostNest(exp.currBestCuckoo_DR);
                }
              /** The second phase: Start Searching with a fraction Pc of Smart Cuckoos **/
              //smart cuckoos begin by exploring new areas from the current solutions ==> diversification
                exp.searchWithSmartCuckoos(LAYOUT_PROB_TYPE);
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
            out.println("check Iteration : "+checkIter);
            out.println("temp Iteration : "+tempIter);
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
                currBestNest_DR = exp.currBestCuckoo_DR;
                currBestNest_DR.CPUTime = CPU_time;
                bestOFV = currBestNest_DR.OFV;
                // record the OFV & CPU time for each Replication
                nestSet_DR.add(currBestNest_DR);
                // write to result OutputFile
                result.writeln_result(bestOFV,CPU_time,currBestNest_DR.departSeq);
                //Best OFV print
                out.print(currBestNest_DR.departSeq+"\t");
                out.println("Final Best OFV: "+ currBestNest_DR.OFV+"\tIteration: "+currBestNest_DR.iterNum);
                out.println();
            }
        }/** End of Replication Part ===========================================================================================**/
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        out.println("Avg OFV/\tTotal Process Time/\toptimal 횟수");
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
        out.print(sumOFV/ STOP_REP + "/\t");
        out.print(sumTime/ STOP_REP + "/\t");
        out.println(numOptimal);

        //Best OFV update & print

        out.println("bestOFV = "+ bestOFV+"\n");
        result.close();

        bestNest.HostNestNum = HostNestNum;
        bestNest.Pa = Pa;
        bestParamNest.add(bestNest);
        if(LAYOUT_PROB_TYPE =="SR")summary.writeln_summary_SR(Nest_Set/*OFV_Set,CPUTime_Set*/,bestParamNest.size()-1);
        else if(LAYOUT_PROB_TYPE =="DR")summary.writeln_summary_DR(nestSet_DR/*OFV_Set,CPUTime_Set*/,bestParamNest.size()-1);
    }//CS Algorithm
}//Main Class
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
abstract class Solution implements Comparable<Solution>, Cloneable {
    // Solution
    LinkedList<Integer> departSeq;
    LinkedList<Double> keySeq;
    //LinkedList<Depart> departs;
    Depart[] departs;
    double OFV;
    static double bestOFV = Double.MAX_VALUE;
    double CPUTime;
    int iterNum;
    // Instance Solution Data
    private static int numDepart;
    static double[] length;
    static int[][] flow;
    // Parameters
    int HostNestNum;
    double Pa;
    // LPModel
    static Cplex model;
// ---------------------------------------------------------------------------------------------------------------------------
// Constructor
// ---------------------------------------------------------------------------------------------------------------------------
    Solution(String layoutProbType) throws Exception {
        if (layoutProbType == "SR") new Solution_SR();
        else if(layoutProbType == "DR") new Solution_DR();
    }

    public Solution() {

    }
    static void setProblemData(){
        numDepart = ProbDataSet.numDepart;
        length = ProbDataSet.length;
        flow = ProbDataSet.flow;
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
        for (int i=0; i<numDepart-1; i++){
            for(int j=i+1; j<numDepart; j++){
                flowValue = flow[departSeq.get(i)-1][departSeq.get(j)-1];
                //distanceValue = Math.abs(departs.get(i).centroid-departs.get(j).centroid);
                distanceValue = Math.abs(departs[i].centroid - departs[j].centroid);
                this.OFV += flowValue*distanceValue; // OFV = flow × total distance
            }
        }
        if(this.OFV < bestOFV) bestOFV = this.OFV;
    }
//---------------------------------------------------------
    public int compareTo(Solution s) {
    if(this.OFV > s.OFV){
        return 1;
    }else if(this.OFV < s.OFV){
        return -1;
    }else{
        return 0;
    }
}//compareTo()
    public Object clone() {
        Object obj = null;
        try{
            obj = super.clone();
        }catch (CloneNotSupportedException e){}
        return obj;
    }

    class Depart{
        double start;
        double centroid = 0.0;
        double length;
        int index;

        Depart(int index){
            this.length = Solution.length[index-1];
            this.start = 0.0;
            this.index = index;
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
    int cutOrder;
    boolean upDown;

    ArrayList<Integer> upperDepartSeq;
    ArrayList<Integer> lowerDepartSeq;

    // Solution 생성자
    Solution_DR() throws Exception {
        representSol();
        upDown = true;
        setDepartCentroid(upDown);
        evaluateSol();
        if(this.OFV <= bestOFV*1.2) {
            evaluateSol(model);
        }
    }
    Solution_DR(LinkedList<Integer> departSeq) throws Exception {
        representSol(departSeq);
        upDown = true;
        setDepartCentroid(upDown);
        evaluateSol();
        if(this.OFV <= bestOFV*1.2) {
            evaluateSol(model);
        }
    }
    Solution_DR(LinkedList<Integer> departSeq, LinkedList<Double> keySeq) throws Exception {
        representSol(departSeq);
        this.keySeq = keySeq;
        upDown = true;
        setDepartCentroid(upDown);
        evaluateSol();
        if(this.OFV <= bestOFV*1.2) {
            evaluateSol(model);
        }
    }
// --------------------------------------------------------
//	Method : Double Row FLP Solution 생성
// --------------------------------------------------------
    private void setDepartCentroid(int cutOrder) {
        double temp = 0.0;
        for (Depart depart : departs){
            /*if (depart == departs.get(cutOrder)) {*/
            if(depart == departs[cutOrder]){
                temp = 0;
                depart.start = 0;
            }
            depart.start += temp;
            depart.updateCentroid();
            temp += depart.length;
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
                temp_upper += depart.length;
            }
            else {
                lowerDepartSeq.add(depart.index);
                depart.start += temp_lower;
                depart.updateCentroid();
                temp_lower += depart.length;
            }

            if(temp_upper > temp_lower) upDown = false;
            else if(temp_upper < temp_lower) upDown = true;
            else upDown = true;
        }
    }
    private void evaluateSol(Cplex model) throws Exception {
        long beforeTime = System.currentTimeMillis();

        model.solveLPModel(upperDepartSeq,lowerDepartSeq);

        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime);
        out.println(secDiffTime);

        RK_Main.tempIter++;
        this.OFV = model.objValue;
        if(this.OFV < bestOFV) bestOFV = this.OFV;
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