package kr.kau.yym7079.Ver_SA;

import kr.kau.yym7079.Common.Cplex;
import kr.kau.yym7079.Common.Generator;
import kr.kau.yym7079.Common.ProbDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.out;

class Main {
    //instance 관련 parameter
    private static final String PROB_TYPE = "Am"; //instance 문제 유형
    private static final boolean IS_CLASSICAL = (Objects.equals(PROB_TYPE, "Classical"));
    private static final String INSTANCE_NAME = "11a"; //instance 문제 이름
    private static final double OPTIMAL_OFV = 5559;

    //SA 관련 parameter
    public static final double TEMPERATURE = 10000;
    public static final double COOLING_FACTOR = 0.9;

    //Experiment parameter
    private static final int NUM_STOP_REP = 10;

    private static double bestOFV;
    private static int numOptimalOFV;

/**Main Method of Program**/
    public static void main(String[] args) throws Exception {
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
                LPModel.initDRLPModel();
                Solution.setLPModel(LPModel);

                //experiment part
                doExpr(probDataSet);
            }
        }
    }

    private static void doExpr(ProbDataSet problem)throws Exception {
        numOptimalOFV = 0;
        for (int numRep = 0; numRep < NUM_STOP_REP; numRep++) {
            doSimulatedAnnealing(problem, TEMPERATURE);
            if (bestOFV <= OPTIMAL_OFV){
                numOptimalOFV ++;
            }
        }
        out.println("The number of times SA found the optimal solution: "+numOptimalOFV);
    }

    private static void doSimulatedAnnealing(ProbDataSet problem, double initTemperature) throws Exception{
        double temperature;
        int nOver;
        int nLimit;
        int totalNumberOfTemperatureSteps;
        Solution currSol;
        Solution currBestSol;
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        //set initial parameter value
        temperature = initTemperature;
        nOver = 100*problem.numDepart;
        nLimit = 10*problem.numDepart;
        totalNumberOfTemperatureSteps = 100;

        int nCount = 0;
        int nSuccess = 0;
        int no_temperature_steps = 0;
        //Random initial solution
        currSol = new Solution();
        currBestSol = new Solution(currSol);

        long startTime = System.currentTimeMillis();	// algorithm start time
        while (true){// iteration start

            while(true) {
                //=================================================
                //new Solution generation
                LinkedList<Integer> newDepartSeq = new Generator().swapOperator(currSol.departSeq);
                Solution newSol = new Solution(newDepartSeq);

                double probAccepted;
                if (newSol.compareTo(currSol) < 0) {
                    probAccepted = 1.0;
                } else {
                    probAccepted = Math.exp((currSol.OFV - newSol.OFV) / temperature);
                }

                double rn = ThreadLocalRandom.current().nextDouble();
                if (probAccepted > rn) {
                    //current solution update
                    currSol = newSol.clone();

                    nSuccess ++;

                    if (probAccepted != 1.0) continue;
                    if (currSol.compareTo(currBestSol) < 0) {
                        currBestSol = currSol.clone();
                        out.println(temperature + "\t\t" + currBestSol.OFV);
                        //printSeq(currBestSol);
                    }
                }

                nCount ++;

                if ((nCount == nOver) || (nSuccess == nLimit)) break;
            }
            no_temperature_steps ++;
            temperature *= COOLING_FACTOR;

            if((no_temperature_steps == totalNumberOfTemperatureSteps)||(nSuccess == 0)) break;
            else{
                nCount = 0;
                nSuccess = 0;
            }
        }
        //Time spent searching for answers
        long endTime = System.currentTimeMillis(); // algorithm end time
        double CPU_time = (endTime - startTime)/ 1000.0;
        out.println("Total Process Time : "+CPU_time+"sec");


        out.println("====================결과값 출력=====================");
        out.println("==================================================");
        out.println("best solution : "+currBestSol.departSeq);
        out.println("best OFV : " + currBestSol.OFV);
        printSeq(currBestSol);
        bestOFV = currBestSol.OFV;
        out.println("==================================================");
        out.println("==================================================");

    }
    public static void printSeq(Solution solution){
        out.println(solution.departSeq);

        out.print("- Upper Row: ");
        for (Integer integer : solution.upperDepartSeq) {
            out.print(integer+"\t");
        }
        out.println();
        out.print("- Lower Row: ");
        for (Integer integer : solution.lowerDepartSeq) {
            out.print(integer+"\t");
        }
        out.println();
    }
    private static void readProbNameSet(ArrayList<String>probName_set) {
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
