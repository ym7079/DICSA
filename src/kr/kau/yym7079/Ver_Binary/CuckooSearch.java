package kr.kau.yym7079.Ver_Binary;

import kr.kau.yym7079.Common.Cplex;
import kr.kau.yym7079.Common.Generator;
import kr.kau.yym7079.Common.ProbDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.apache.commons.math3.special.Gamma.gamma;

class CuckooSearch {
/** Variables **/
    private final int numDepart;
    private final int numNest;
    private final double Pa;
    private final double alpha;
//-----------------------------------
    ArrayList<Solution> population;
    Solution[] popArray;

    Solution currBestSol;
    Solution currBestSol2;
    int bestIdx;
//-----------------------------------
    private final Cplex model;
    private final Generator g;
/** Constructor **/
    CuckooSearch(ProbDataSet problem, Cplex LPModel, int numNest, double Pa, double alpha){
        this.g = new Generator(problem);
        this.model = LPModel;

        this.numDepart = problem.numDepart;
        this.numNest = numNest;
        this.Pa = Pa;
        this.alpha = alpha/(numDepart);
    }
/** Methods **/
// Method : The 1st phase -> Generate initial population of n host nests
    void initNest() throws Exception {
        //population initialization
        population = new ArrayList<>();
        popArray = new Solution[numNest];

        //generate initial solutions
        for (int i = 0; i < numNest; i++) {
            population.add(new Solution());
            popArray[i] = new Solution();
        }

        //sort population by OFV
        Collections.sort(population);
        Arrays.sort(popArray);

        //save best solution of initial population
        bestIdx = 0;
        currBestSol = population.get(bestIdx);
        currBestSol2 = popArray[bestIdx];
    }
// Method : The 2nd phase -> Triggering smart cuckoos
    void searchWithSmartCuckoos() throws Exception {
        int numSmartCuckoo = (int)Math.round(0.6*numNest);
        int[] smartCuckooIdxSet = g.randPermutationArray(numSmartCuckoo,numNest,bestIdx);
        int i = 1;
        while (i != numSmartCuckoo) {
            // Generating New Solution by LévyFlights
            int j = smartCuckooIdxSet[i - 1];
            Solution newCuckoo;
            generateNewSol(popArray[j]);
        }
    }
// Method : The 3rd phase -> Employ one cuckoo to search for a new good solution
    void employOneCuckoo() throws Exception{

    }
// Method : The 4th phase -> Abandon a fraction Pa of worse solutions that will be replaced by new ones
    void abandonWorseNests() throws Exception {

    }
// Method : Generate New Solution
    private static final double LAMBDA = 3.0/2.0; // λ
    private static final double SIGMA_U = Math.pow(((gamma(1.0+ LAMBDA) * Math.sin((Math.PI* LAMBDA)/2.0)) / (gamma((1.0+ LAMBDA)/2.0) * LAMBDA * Math.pow(2.0,(LAMBDA -1)/2))),(1/ LAMBDA));
    private static final double SIGMA_V = 1.0;
    private static double stepValue(){
        double step;
        //==================================================================================================================
        // u,v value 구하기 (u~N(0,sigmaU^2), v~N(0,sigmaV^2))
        // u는 평균이 0, 표준편차가 sigmaU인 정규분포 확률변수
        // v는 평균이 0, 표준편차가 sigmaV인 정규분포 확률변수
        double u = new Random().nextGaussian()* SIGMA_U;
        double v = new Random().nextGaussian()* SIGMA_V;

        // generate stepSize => u~N(0, sigma) / v~N(0, 1)^(1/lambda)
        // stepSize = u / (|v|)^(1/Lambda)
        step = u / Math.pow((Math.abs(v)),(1.0/ LAMBDA));
        //step = new Random().nextGaussian()*(u / Math.pow((Math.abs(v)),(1/lambda))); => 수민 closedLoop 논문 version

        return step;
    }

    private void generateNewSol(Solution cuckoo){
        double stepLength = alpha*stepValue()*new Random().nextGaussian();
        //if(stepLength < 1)


    }
    private void updateGammaSeq(boolean[] gammaSeq){

    }
    private void updateAlphaSeq(boolean[][] alphaSeq){

    }
    //-----------------------------------------------
    public int getNumNest() {
        return numNest;
    }
    public double getAlpha() {
        return alpha;
    }
//------------------------------------------------------------------------

}
