package kr.kau.yym7079.Ver_4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/** Single-row facility layout problem(SRFLP) with Discrete Cuckoo Search Algorithm(DCS)
    Double-row layout problem(DRLP) with Discrete Cuckoo Search Algorithm(DCS) **/
/** Discrete Method : Random-Key + Local Search(2-Opt) encoding scheme **/
class RK_Main {
    public static LinkedList<Solution> bestSolution;
    public static LinkedList<Solution> bestParamNest;
////instance 관련 parameter
    static private String layoutProbType = "SR";
    static private String probType = "Classical"; //instance 문제 유형
    static private boolean isClassical;
    static private String instanceName = "H20"; //instance 문제 이름
    static private double optimalOFV = 15549;
////Dummy 관련 parameter
    static private boolean isDummy = false; // dummy를 만들지 여부
    static private int numDummy = 10; // dummy department 수
    static private double lengthDummy = 0.5; // dummy department 길이
//Experiment 관련 parameter
    static private double alpha = 0.4;

    static public int stopRep = 5;
    static private int stopIter =  2000;
    static public int numIter;

    /**Main Method**/
    public static void main(String[] args) throws Exception {
        createDataset();
        //int configNum; // configuration (= parameter set) number

        ArrayList<String> probName_set = new ArrayList<>();
        ArrayList<Integer> HostNestNum_set = new ArrayList<>(); // number of host nests (or the population size n)
        ArrayList<Double> ProbA_set = new ArrayList<>(); // probability Pa

        bestSolution = new LinkedList<>();

        OutputWriter.setDirName(isClassical,probType);
        OutputWriter report = new OutputWriter();

        readProbNameSet(probName_set);// 각 문제들의 파일명을 읽어들이는 메소드
        for(String probName : probName_set) {
            if (probName.contains(instanceName)) {

                OutputWriter summary = new OutputWriter(probName);
                summary.writeln_clock();

                System.out.println("############################");
                System.out.println("Problem Name: " + probName);
                System.out.println("############################");

                ProbDataSet.init(probName,isDummy);
                readParameterSet(probName, HostNestNum_set, ProbA_set,isClassical);

                bestParamNest = new LinkedList<>();

                for (int HostNestNum : HostNestNum_set) {
                    for (double Pa : ProbA_set) {
                        if(Pa == 0.2||Pa == 0.25/*||Pa ==0.35*/) {
                        System.out.println("population size: " + HostNestNum + ",probability a: " + Pa);
                        summary.writeln_parameters(HostNestNum, Pa);
                        CS_Algorithm(summary, HostNestNum, Pa, alpha);
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
        System.out.println("Successfully finished.\n");
    }
    /**Create the Name of Problem Instance or the Value of Cuckoo Search Algorithm **/
    private static void createDataset() throws Exception {
        CreatingDataset.createProbNameDir();

        if (probType == "Classical") isClassical = true;
        else {
            isClassical = false;
            String problemNameFilePath = CreatingDataset.probNameFolderDir+"/prob"+probType; //파일 경로
            // 해당 파일(문제유형)이 없을 경우(새로운 문제유형인 경우), 관련 폴더 및 데이터 파일들을 생성함
            if(!new File(problemNameFilePath).exists()){
                String probName= "";
                CreatingDataset.createDataset(probType, probName);
            }
        }
    }
    /**Read the Name of Problem Instance or the Value of Cuckoo Search Algorithm **/
    private static void readProbNameSet(ArrayList<String>probName_set) throws Exception {
        File file = new File("dataset/Problems/prob"+probType);
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
        else readParameterSet(probName,HostNestNum_set,ProbA_set,probType);
    }
    private static void readParameterSet(String probName, ArrayList<Integer> HostNestNum_set, ArrayList<Double> ProbA_set,String probType) {
        /** File Usage
         *  file path -> dataset/Parameters/
         *  file name -> %probName%.parameter
         *  contents are comma-seperate
         *  contenst order -> HostNestNum, P_a (one for each line)
         */
        HostNestNum_set.clear(); ProbA_set.clear();
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
        return numDummy;
    }
    public static double getLengthDummy() { return lengthDummy; }
    /** The Body of Random-Key Discrete Cuckoo Search Algorithm **/
    private static void CS_Algorithm(OutputWriter summary, int HostNestNum, double Pa, double alpha)throws Exception{
        Experiment exp;
        OutputWriter result;
        exp = new Experiment(HostNestNum,Pa,alpha);
        result = new OutputWriter(HostNestNum,Pa);
        double bestOFV = 0;

        LinkedList<Solution_SR> Nest_Set = new LinkedList<>();
        LinkedList<Solution_DR> nestSet_DR = new LinkedList<>();
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        /** Replication Part ===========================================================================================================**/
        int numRep = 0;
        while (numRep++ < stopRep){
            //Cuckoo Search Algorithm part =================================================================================================
          /**The first phase:  Random initial solutions */
            //Generating Initial Population of n host nests X_i(초기 해집단 생성)
            exp.initHostNest(layoutProbType);
            //exp.initHostNest();
            Solution_SR currBestNest;
            Solution_DR currBestNest_DR;

          /**Starting iterations ================================================================================================*/
            numIter = 0;
            long startTime = System.currentTimeMillis();	// algorithm start time
            long startIterTime = 0;
            while (numIter++ < stopIter){
                //if(instanceName == "H20" && exp.currBestCuckoo.OFV == 15549) break;
                if(numIter % 100 == 0 && numIter >= 100){
                    if(layoutProbType =="SR")exp.initHostNest(exp.currBestCuckoo);
                    else if(layoutProbType=="DR")exp.initHostNest(exp.currBestCuckoo_DR);
                }
              /**The second phase: Start Searching with a fraction Pc of Smart Cuckoos */
              //smart cuckoos begin by exploring new areas from the current solutions ==> diversification
                exp.searchWithSmartCuckoos(layoutProbType);
                /*Collections.sort(exp.HostNest);
                bestIdx = 0;*/
              /***The third phase: Employ one cuckoo to search for a new good solution,starting from the best solution of the population
                                   (The global explorative random walk) */
                exp.employOneCuckoo(layoutProbType);

                if(Pa == 0.0) {
                    continue;
                }
                //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
              /***The last phase: Abandon a fraction Pa of worse solutions that will be replaced by new ones 2. The local random walk */
                // A fraction (Pa) of worse nests are abandoned and completely new nests(with new random solutions) are built
                exp.abandonWorseNests(layoutProbType);
                // Sorting New Population of n host nests
                if(numIter == 0)startIterTime = System.currentTimeMillis();
            }/** End of iterations ===============================================================================================================================*/
            //Cuckoo Search Algorithm part
            //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
          /** Post-optimization processing ========================================================================================*/
            //Time spent searching for answers
            long endTime = System.currentTimeMillis(); // algorithm end time
            System.out.println(stopIter+"iteration process time: "+(endTime-startIterTime)/1000.0);
            double CPU_time = endTime - startTime;
            CPU_time /= 1000.0;
            System.out.println("Total Process Time : "+CPU_time+"sec");

            if(layoutProbType =="SR"){
                //Best OFV update
                currBestNest = exp.currBestCuckoo;
                currBestNest.CPUTime = CPU_time;
                bestOFV = currBestNest.OFV;
                // record the OFV & CPU time for each Replication
                Nest_Set.add(currBestNest);
                // write to result OutputFile
                result.writeln_result(bestOFV,CPU_time,currBestNest.departSeq);
                //Best OFV print
                System.out.print(currBestNest.departSeq+"\t");
                System.out.println("best OFV: "+ currBestNest.OFV+"\tIteration: "+currBestNest.iterNum);
            }else if(layoutProbType=="DR"){
                currBestNest_DR = exp.currBestCuckoo_DR;
                currBestNest_DR.CPUTime = CPU_time;
                bestOFV = currBestNest_DR.OFV;
                // record the OFV & CPU time for each Replication
                nestSet_DR.add(currBestNest_DR);
                // write to result OutputFile
                result.writeln_result(bestOFV,CPU_time,currBestNest_DR.departSeq);
                //Best OFV print
                System.out.print(currBestNest_DR.departSeq+"\t");
                System.out.println("best OFV: "+ currBestNest_DR.OFV+"\tIteration: "+currBestNest_DR.iterNum);
            }
        }/** End of Replication Part ===========================================================================================**/
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        System.out.println("Avg OFV/\tTotal Process Time/\toptimal 횟수");
        double sumOFV = 0.0;
        double sumTime = 0.0;
        int numOptimal = 0;
        Solution bestNest = null;
        //Solution_DR bestNest_DR;
        if(layoutProbType =="SR"){
            bestNest = Collections.min(Nest_Set); // 현재 파라미터 설정에서 가장 좋은 해를 저장
            for(Solution_SR sol: Nest_Set){
                sumOFV += sol.OFV;
                sumTime += sol.CPUTime;
                if(sol.OFV == optimalOFV) numOptimal++;
            }
            bestOFV = bestNest.OFV;
        }
        else if(layoutProbType == "DR"){
            bestNest = Collections.min(nestSet_DR); // 현재 파라미터 설정에서 가장 좋은 해를 저장
            for(Solution_DR sol: nestSet_DR){
                sumOFV += sol.OFV;
                sumTime += sol.CPUTime;
                if(sol.OFV == optimalOFV) numOptimal++;
            }
            bestOFV = bestNest.OFV;
        }
        System.out.print(sumOFV/stopRep + "/\t");
        System.out.print(sumTime/stopRep + "/\t");
        System.out.println(numOptimal);

        //Best OFV update & print

        System.out.println("bestOFV = "+ bestOFV+"\n");
        result.close();

        bestNest.HostNestNum = HostNestNum;
        bestNest.Pa = Pa;
        bestParamNest.add(bestNest);
        if(layoutProbType=="SR")summary.writeln_summary_SR(Nest_Set/*OFV_Set,CPUTime_Set*/,bestParamNest.size()-1);
        else if(layoutProbType=="DR")summary.writeln_summary_DR(nestSet_DR/*OFV_Set,CPUTime_Set*/,bestParamNest.size()-1);
    }//CS Algorithm
}//Main Class

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

abstract class Solution implements Comparable<Solution>, Cloneable {
    // Solution
    LinkedList<Integer> departSeq;
    LinkedList<Double> keySeq = new LinkedList<>();
    LinkedList<Depart> departs = new LinkedList<>();
    double OFV;
    double CPUTime;
    int iterNum;
    // Generator
    Generator g = new Generator();
    // Instance Solution Data
    static int numDepart;
    static double[] length;
    static int[][] flow;
    // Parameters
    int HostNestNum;
    double Pa;
    // Solution 생성자
// ---------------------------------------------------------------------------------------------------------------------------
//	Method : FLP Solution 생성
// ---------------------------------------------------------------------------------------------------------------------------
    void representSol() throws Exception {

        this.departSeq = new LinkedList<>(g.randomKeyPermute(this.keySeq));
        for (int index : this.departSeq){
            departs.add(new Depart(index));
        }
    }
    void representSol(LinkedList<Integer> departSeq) throws Exception {
        this.departSeq = departSeq;
        for (int index : this.departSeq){
            departs.add(new Depart(index));
        }
    }
    void evaluateSol() throws Exception {
        double flowValue;
        double distance;
        //초기화
        this.OFV = 0; // Objective Function Value (OFV) = Distance*Flow
        for (int i=0; i<numDepart; i++){
            for(int j=0; j<numDepart; j++){
                if(i>=j)continue;
                flowValue = (double)flow[departSeq.get(i)-1][departSeq.get(j)-1];
                distance = Math.abs(departs.get(i).centroid-departs.get(j).centroid);
                this.OFV += flowValue*distance; // OFV = flow × total distance
            }
        }
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

        Depart(int index){
            this.length = Solution.length[index-1];
            this.start = 0.0;
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
    public int compareTo(Solution_SR s) {
        if(this.OFV > s.OFV){
            return 1;
        }else if(this.OFV < s.OFV){
            return -1;
        }else{
            return 0;
        }
    }//compareTo()
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
    ArrayList<Boolean> upDownSet = new ArrayList<>();

    // Solution 생성자
    Solution_DR() throws Exception {
        representSol();
        upDown = new Random().nextBoolean();
        setDepartCentroid(upDown);
        evaluateSol();
    }
    Solution_DR(LinkedList<Integer> departSeq) throws Exception {
        representSol(departSeq);
        upDown = new Random().nextBoolean();
        setDepartCentroid(upDown);
        evaluateSol();
    }
    Solution_DR(LinkedList<Integer> departSeq, LinkedList<Double> keySeq) throws Exception {
        representSol(departSeq);
        this.keySeq = keySeq;
        upDown = new Random().nextBoolean();
        setDepartCentroid(upDown);
        evaluateSol();
    }
// --------------------------------------------------------
//	Method : Double Row FLP Solution 생성
// --------------------------------------------------------
    private void setDepartCentroid(int cutOrder) throws Exception{
        double temp = 0.0;
        for (Depart depart : departs){
            if (depart == departs.get(cutOrder)) {
                temp = 0;
                depart.start = 0;
            }
            depart.start += temp;
            depart.updateCentroid();
            temp += depart.length;
        }
    }
    private void setDepartCentroid(boolean upDown) throws Exception{
        double temp_upper = 0.0;
        double temp_lower = 0.0;

        for (Depart depart : departs){
            upDownSet.add(upDown);
            if (upDown) {
                depart.start += temp_upper;
                depart.updateCentroid();
                temp_upper += depart.length;
            }
            else {
                depart.start += temp_lower;
                depart.updateCentroid();
                temp_lower += depart.length;
            }

            if(temp_upper > temp_lower) upDown = false;
            else upDown = true;
        }
    }

    public int compareTo(Solution_DR s) {
        if(this.OFV > s.OFV){
            return 1;
        }else if(this.OFV < s.OFV){
            return -1;
        }else{
            return 0;
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