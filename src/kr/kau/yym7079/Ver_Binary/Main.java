package kr.kau.yym7079.Ver_Binary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import kr.kau.yym7079.Common.Cplex;
import kr.kau.yym7079.Common.CreatingDataset;
import kr.kau.yym7079.Common.ProbDataSet;

import static java.lang.System.out;

class Main {
/**Constant Value Variables(Parameters of problem and algorithm)**/
    //instance 관련 parameter
    private static final String LAYOUT_PROB_TYPE = "DR";
    private static final String PROB_TYPE = "AnKeVa"; //instance 문제 유형
    private static final boolean IS_CLASSICAL = (PROB_TYPE == "Classical");
    private static final String INSTANCE_NAME = "60-1"; //instance 문제 이름
    private static final double OPTIMAL_OFV = 739233.0;

    //Experiment 관련 parameter
    public static final double ALPHA = 0.2;

    private static final int NUM_STOP_REP = 5;
    private static final int NUM_STOP_ITER =  2000;
    private static int numIter;
    private static int tempIter = 0;
/**Main Method of Program**/
    public static void main(String[] args) throws Exception {
        createDataset();

        ArrayList<String> probNameSet = new ArrayList<>();
        readProbNameSet(probNameSet);// 각 문제들의 파일명을 읽어들이는 메소드

        for(String probName : probNameSet) {
            if (probName.contains(INSTANCE_NAME)) {

                out.println("############################");
                out.println("Problem Name: " + probName);
                out.println("############################");

                //set instance data
                ProbDataSet probDataSet = new ProbDataSet(probName);
                Solution.setProblemData(probDataSet);

                //set CPLEX LP model
                Cplex LPModel = new Cplex(probDataSet);
                LPModel.setInitDRLPModel();
                Solution.setLPModel(LPModel);

                //read Cuckoo search algorithm parameters
                ArrayList<Integer> hostNestNumSet = new ArrayList<>(); // number of host nests (or the population size n)
                ArrayList<Double> probASet = new ArrayList<>(); // probability Pa
                readParameterSet(probName,hostNestNumSet,probASet, IS_CLASSICAL);

                //experiment part
                doExpr(probDataSet, LPModel, hostNestNumSet, probASet);
            }//if (probName.contains(INSTANCE_NAME))
        }//for(String probName : probNameSet)
        out.println("Successfully finished.\n");
    }
    private static void doExpr(ProbDataSet probDataSet,Cplex LPModel, ArrayList<Integer> hostNestNumSet, ArrayList<Double> probASet) throws Exception {
        for (Integer numNest : hostNestNumSet) {
            for (Double pA : probASet) {
                out.println("population size: " + numNest + ",probability a: " + pA);
                CuckooSearch cuckooSearch = new CuckooSearch(probDataSet, LPModel, numNest, pA, ALPHA);
                //Replication
                for (int numRep = 0; numRep < NUM_STOP_REP; numRep++) {
                    doCSAlgorithm(cuckooSearch, numNest, pA, ALPHA);
                }
            }
        }
    }
    private static void doCSAlgorithm(CuckooSearch cs, int numNest, double Pa, double alpha)throws Exception{
        //Random initial solutions
        cs.initNest();
        Solution bestSol; // CS best solution

        // CS Algorithm iteration
        long startTime = System.currentTimeMillis();	// algorithm start time
        int numIter = 0;
        while (numIter++ < NUM_STOP_ITER){// iteration start
            //The second phase: Start Searching with a fraction Pc of Smart Cuckoos
            cs.searchWithSmartCuckoos();
            //The third phase: Employ one cuckoo to search for a new good solution,starting from the best solution of the population
            //(The global explorative random walk)
            cs.employOneCuckoo();
            if(Pa == 0.0) {
                continue;
            }
            //The last phase: Abandon a fraction Pa of worse solutions that will be replaced by new ones 2. The local random walk
            cs.abandonWorseNests();
        }// iteration end
        //Post-iteration process
        //Time spent searching for answers
        long endTime = System.currentTimeMillis(); // algorithm end time
        double CPU_time = (endTime - startTime) / 1000.0;
        out.println("Total Process Time : "+CPU_time+"sec");
        //Best OFV update
        bestSol = new Solution(cs.currBestSol.gammaSeq,cs.currBestSol.alphaSeq,cs.currBestSol.OFV);
        //Best OFV print
        //out.print(bestSol.departSeq+"\t");
        //out.println("best OFV: "+ currBestNest_SR.OFV+"\tIteration: "+ currBestNest_SR.iterNum);


    }
    private static void createDataset() throws Exception {
        CreatingDataset.createProbNameDir();

        CreatingDataset newDataSet = new CreatingDataset();
        if (!IS_CLASSICAL){
            String problemNameFilePath = CreatingDataset.probNameFolderDir+"/prob"+ PROB_TYPE; //파일 경로
            // 해당 파일(문제유형)이 없을 경우(새로운 문제유형인 경우), 관련 폴더 및 데이터 파일들을 생성함
            if(!new File(problemNameFilePath).exists()){
                out.print("problem name list :");
                // 키보드로 입력받기
                String probName = new Scanner(System.in).next();
                newDataSet.createDataset(PROB_TYPE, probName);
            }
        }
    }
    private static void readProbNameSet(ArrayList<String>probName_set) throws Exception {
        File file = new File("dataset/Problems/prob"+ PROB_TYPE);
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
         *  contents order -> HostNestNum, P_a (one for each line)
         */
        probType = probType+"/";
        File file = new File("dataset/Parameters/"+ probType + probName + ".parameter");
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
}
