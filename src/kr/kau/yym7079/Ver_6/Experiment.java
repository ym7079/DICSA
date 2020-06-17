package kr.kau.yym7079.Ver_6;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.console;
import static java.lang.System.out;
import static org.apache.commons.math3.special.Gamma.gamma;

class Experiment {
    private static final Generator g = new Generator();
    private static final Random rd = new Random();

    private final int numDepart;
    private final int HostNestNum;
    private final double Pa;
    private final double Pc;
    private final double alpha;

    ArrayList<Solution_SR> HostNest_SR = new ArrayList<>();
    ArrayList<Solution_DR> HostNest_DR = new ArrayList<>();

    Solution currBestSolution;
    Solution_SR currBestCuckoo_SR;
    Solution_DR currBestCuckoo_DR;
    int bestIdx;

    LinkedList<Integer> newDepartSeq = new LinkedList<>();
    LinkedList<Double> newKeySeq = new LinkedList<>();
    Solution_SR newCuckoo_SR;
    Solution_DR newCuckoo_DR;
    //####################################################################################################################################
// 생성자
    Experiment(int HostNestNum, double Pa, double Pc, double alpha){
        this.numDepart = ProbDataSet.numDepart;
        this.HostNestNum = HostNestNum;
        this.Pa = Pa;
        this.Pc = Pc;
        this.alpha = alpha/(numDepart);
    }
    //####################################################################################################################################
// Method : The 1st phase -> Generate initial population of n host nests
    void initHostNest() throws Exception { // 완전 랜덤한 초기해 생성
        HostNest_DR.clear();
        for(int i=0; i< HostNestNum; i++){
            HostNest_DR.add(new Solution_DR());
        }
        Collections.sort(HostNest_DR);
        bestIdx = 0;
        //currBestCuckoo_DR = HostNest_DR.get(bestIdx);
    }
    void initHostNest(String layoutProbType) throws Exception {
        LinkedList<Integer> Π = g.generateInitSeedPermutation(); // An initial seed permutation

        // HostNest의 초기화
        if(layoutProbType == "SR"){
            HostNest_SR.clear();
            HostNest_SR.add(new Solution_SR(Π,g.keySeqUpdate(Π)));
        }else if(layoutProbType == "DR"){
            HostNest_DR.clear();
            HostNest_DR.add(new Solution_DR(Π,g.keySeqUpdate(Π)));
        }
        for(int i=1; i<HostNestNum; i++){
            LinkedList<Integer> tmpDepartSeq = new LinkedList<>(Π);
            for(int j=0; j<=numDepart/2; j++){
                double rn = ThreadLocalRandom.current().nextDouble();
                if(rn < 0.5){
                    Collections.swap(tmpDepartSeq,j,numDepart-j-1);
                }
            }
            if(layoutProbType == "SR"){
                HostNest_SR.add(new Solution_SR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
            }
            else if(layoutProbType == "DR"){
                HostNest_DR.add(new Solution_DR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
            }
        }
        if(layoutProbType == "SR"){
            Collections.sort(HostNest_SR);
            currBestCuckoo_SR = HostNest_SR.get(0);
        }
        else if(layoutProbType == "DR"){
            Collections.sort(HostNest_DR);
            bestIdx = 0;
            //currBestCuckoo_SR = HostNest_SR.get(bestIdx);
        }
    }
    void initHostNest(Solution_SR currBestCuckoo) throws Exception {
        LinkedList<Integer> Π = new LinkedList<>(currBestCuckoo.departSeq); // An initial seed permutation
        LinkedList<Integer> tmpDepartSeq;

        // HostNest의 초기화
        HostNest_SR.clear();
        ArrayList<Integer> tmpOverlapSet = new ArrayList<>();
        for(int i=0; i<HostNestNum/2; i++){
            tmpDepartSeq = new LinkedList<>(Π);
            for (int j=0; j<HostNestNum/2; j++) {
                double rn = Math.random();
                if(rn <= 0.5){
                    //Collections.swap(tmpDepartSeq,j,numDepart-j-1);
                    //tmpDepartSeq = g.insertMoveOperator(tmpDepartSeq);
                    int tmp=0;
                    int removingIdx;
                    int insertingIdx;
                    rn = Math.random();
                    if(rn <= 0.5){
                        removingIdx = j;
                        insertingIdx = numDepart-j-1;
                    }
                    else{
                        removingIdx = numDepart-j-1;
                        insertingIdx = j;
                    }
                    tmp += 100*removingIdx + insertingIdx;
                    boolean isContained = tmpOverlapSet.contains(tmp);
                    if(isContained) continue;
                    g.insertMoveOperator(tmpDepartSeq,removingIdx,insertingIdx);
                    tmpOverlapSet.add(tmp);
                    break;
                }
            }
            HostNest_SR.add(new Solution_SR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
        }
        /*for(int i=HostNestNum/2; i<HostNestNum; i++){
            HostNest_SR.add(new Solution());
        }*/
        Π = g.generateInitSeedPermutation();
        HostNest_SR.add(new Solution_SR(Π,g.keySeqUpdate(Π)));
        for(int i=(HostNestNum/2)+1; i<HostNestNum; i++){
            tmpDepartSeq = new LinkedList<>(Π);
            for(int j=0; j<=numDepart/2; j++){
                double rn = Math.random();
                if(rn <= 0.5){
                    //Collections.swap(tmpDepartSeq,j,numDepart-j-1);
                    //g.swapMoveOperator(tmpDepartSeq);
                    if(new Random().nextDouble() <= 0.5){
                        g.insertMoveOperator(tmpDepartSeq,j,numDepart-j-1);
                    }
                    else{
                        g.insertMoveOperator(tmpDepartSeq,numDepart-j-1,j);
                    }
                }
            }
            HostNest_SR.add(new Solution_SR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
        }
        /*int i = 0;
        while(true){
            if(i==HostNestNum) break;
            Solution nest = new Solution();
            int sameNest = 0;
            for(Solution n : HostNest_SR){
                if (n.OFV == nest.OFV) sameNest += 1;
            }
            if(sameNest <= 0){
                nest.HostNestNum = HostNestNum;
                nest.Pa = Pa;
                HostNest_SR.add(nest);
                i++;
            }
        }*/
        Collections.sort(HostNest_SR);
        bestIdx = 0;
    }
    void initHostNest(Solution_DR currBestCuckoo) throws Exception {
        LinkedList<Integer> Π = new LinkedList<>(currBestCuckoo.departSeq); // An initial seed permutation
        LinkedList<Integer> tmpDepartSeq;

        // HostNest의 초기화
        HostNest_DR.clear();
       /* for(int i=0; i<HostNestNum/2; i++){
            tmpDepartSeq = new LinkedList<>(Π);
            for(int j=0; j<=numDepart/2; j++){
                double rn = Math.random();
                if(rn <= 0.5){
                    Collections.swap(tmpDepartSeq,j,numDepart-j-1);
                }
            }
            HostNest_DR.add(new Solution_DR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
        }*/
        ArrayList<Integer> tmpOverlapSet = new ArrayList<>();
        for(int i=0; i<HostNestNum/2; i++){
            tmpDepartSeq = new LinkedList<>(Π);
            for (int j=0; j<HostNestNum/2; j++) {
                double rn = ThreadLocalRandom.current().nextDouble();
                if(rn < 0.5){
                    //Collections.swap(tmpDepartSeq,j,numDepart-j-1);
                    //tmpDepartSeq = g.insertMoveOperator(tmpDepartSeq);
                    int tmp=0;
                    int removingIdx;
                    int insertingIdx;
                    rn = ThreadLocalRandom.current().nextDouble();
                    if(rn < 0.5){
                        removingIdx = j;
                        insertingIdx = numDepart-j-1;
                    }
                    else{
                        removingIdx = numDepart-j-1;
                        insertingIdx = j;
                    }
                    tmp += 100*removingIdx + insertingIdx;
                    boolean isContained = tmpOverlapSet.contains(tmp);
                    if(isContained) continue;
                    g.insertMoveOperator(tmpDepartSeq,removingIdx,insertingIdx);
                    tmpOverlapSet.add(tmp);
                    break;
                }
            }
            HostNest_DR.add(new Solution_DR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
        }

        Π = g.generateInitSeedPermutation();
        HostNest_DR.add(new Solution_DR(Π,g.keySeqUpdate(Π)));
        for(int i=(HostNestNum/2)+1; i<HostNestNum; i++){
            tmpDepartSeq =  new LinkedList<>(Π);
            for(int j=0; j<=numDepart/2; j++){
                double rn = ThreadLocalRandom.current().nextDouble();
                if(rn < 0.5){
                    //Collections.swap(tmpDepartSeq,j,numDepart-j-1);
                    if(ThreadLocalRandom.current().nextDouble() < 0.5){
                        g.insertMoveOperator(tmpDepartSeq,j,numDepart-j-1);
                    }
                    else{
                        g.insertMoveOperator(tmpDepartSeq,numDepart-j-1,j);
                    }
                }
            }
            HostNest_DR.add(new Solution_DR(tmpDepartSeq,g.keySeqUpdate(tmpDepartSeq)));
        }
        /*for(int i=(HostNestNum/2); i<HostNestNum; i++){
            HostNest_DR.add(new Solution_DR());
        }*/
        Collections.sort(HostNest_DR);
        bestIdx = 0;
    }
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// Method : The 2nd phase -> Triggering smart cuckoos
    void searchWithSmartCuckoos(String layoutProbType) throws Exception {
        /**A fraction Pc of cuckoos search for solutions from the current position and try to improve them.
         They move from one region to another via Lévy flights to locate the best solution
         in each region without being trapped in a local optimum.**/
        if(layoutProbType == "SR") searchWithSmartCuckoos_SR();
        else if(layoutProbType =="DR") searchWithSmartCuckoos_DR();
    }
    void searchWithSmartCuckoos_SR() throws Exception {
        int numSmartCuckoo = (int)Math.round(0.6*HostNestNum);
        int i = 1;
        int[] smartCuckooIndex = g.randomPermute(numSmartCuckoo,HostNestNum,bestIdx);
        while(true){
            if(i == numSmartCuckoo) break;
            // Generating New Solution
            int j = smartCuckooIndex[i-1];
            generateNewSolutionByLévyFlights(HostNest_SR.get(j));
            newCuckoo_SR = new Solution_SR(newDepartSeq,newKeySeq);

            // Prevent Overlap Solution
            boolean sameSolution = preventOverlap(newCuckoo_SR);
            if (!(sameSolution)) {
                if(newCuckoo_SR.compareTo(HostNest_SR.get(j)) <= -1) {
                    HostNest_SR.set(j, newCuckoo_SR);//==> ofv 값이 안 좋더라도 무조건 update
                    if (newCuckoo_SR.compareTo(HostNest_SR.get(bestIdx)) <= -1) {
                        bestIdx = j;//--> population 에서 ofv 가 가장 좋은 solution 보다 더 좋으면
                        if (newCuckoo_SR.compareTo(currBestCuckoo_SR) <= -1) {
                            currBestCuckoo_SR = newCuckoo_SR;
                            out.print(currBestCuckoo_SR.departSeq+"\t");
                            out.println("best OFV update_s : "+ currBestCuckoo_SR.OFV+"\tIteration: "+ RK_Main.numIter);
                            currBestCuckoo_SR.iterNum = RK_Main.numIter;
                            RK_Main.numIter = 0;
                        }
                    }
                }
                i++;
            }

        }
    }
    void searchWithSmartCuckoos_DR() throws Exception {
        int numSmartCuckoo = (int)Math.round(Pc*HostNestNum);
        int i = 1;
        int[] smartCuckooIndex = g.randomPermute(numSmartCuckoo,HostNestNum,bestIdx);
        while (i != numSmartCuckoo) {
            // Generating New Solution
            int j = smartCuckooIndex[i - 1];
            generateNewSolutionByLévyFlights(HostNest_DR.get(j));
            newCuckoo_DR = new Solution_DR(newDepartSeq, newKeySeq);

            // Prevent Overlap Solution
            boolean sameSolution = preventOverlap(newCuckoo_DR);
            if (!(sameSolution)) {
                if (newCuckoo_DR.compareTo(HostNest_DR.get(j)) <= -1) {
                    HostNest_DR.set(j, newCuckoo_DR);
                    if (newCuckoo_DR.compareTo(HostNest_DR.get(bestIdx)) <= -1) {
                        bestIdx = j;//--> population 에서 ofv 가 가장 좋은 solution 보다 더 좋으면
                    }
                }
                i++;
            }

        }
    }
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// Method : The 3rd phase -> Employ one cuckoo to search for a new good solution
    Solution_SR getCuckooI() throws Exception {
        Solution_SR newCuckoo;
        while(true) {
            generateNewSolutionByLévyFlights(HostNest_SR.get(bestIdx));
            newCuckoo = new Solution_SR(newDepartSeq, newKeySeq);
            // Prevent Overlap Solution
            boolean sameSolution = preventOverlap(newCuckoo);
            if (!(sameSolution)) {
                break;
            }
        }
        return newCuckoo;
    }
    Solution_DR getCuckooI_DR() throws Exception {
        Solution_DR newCuckoo;
        while(true) {
            generateNewSolutionByLévyFlights(HostNest_DR.get(bestIdx));
            newCuckoo = new Solution_DR(newDepartSeq, newKeySeq);
            // Prevent Overlap Solution
            boolean sameSolution = preventOverlap(newCuckoo);
            if (!(sameSolution)) {
                break;
            }
        }
        return newCuckoo;
    }
    void employOneCuckoo(String layoutProbType) throws Exception {
        if(layoutProbType=="SR") employOneCuckoo_SR();
        else if(layoutProbType=="DR")employOneCuckoo_DR();
    }
    void employOneCuckoo_SR() throws Exception {
        //Generating New Solution (Get a cuckoo "i" randomly by Lévy flights)
        newCuckoo_SR = getCuckooI();
        //Choose an individual nest "j" randomly in the population/nests (avoid the current best nest)
        Solution_SR nestJ;
        int j = rd.nextInt(HostNestNum);
        while(j == bestIdx) j = rd.nextInt(HostNestNum);//(current best 는 제외시킴)
        nestJ = HostNest_SR.get(j);
        //Compare both solutions and Update the best one
        if(newCuckoo_SR.compareTo(nestJ) <= -1) {
            //Replace j by i(new one)
            HostNest_SR.set(j, newCuckoo_SR);
            if (newCuckoo_SR.compareTo(HostNest_SR.get(bestIdx)) <= -1){
                bestIdx = j;
                if (newCuckoo_SR.compareTo(currBestCuckoo_SR) <= -1) {
                    out.print(newCuckoo_SR.departSeq+"\t");
                    currBestCuckoo_SR = new Solution_SR(newCuckoo_SR.departSeq, newCuckoo_SR.keySeq);
                    out.println("best OFV update_b : "+ currBestCuckoo_SR.OFV+"\tIteration: "+ RK_Main.numIter);
                    currBestCuckoo_SR.iterNum = RK_Main.numIter;
                    RK_Main.numIter = 0;
                }
            }
            if(Pa != 0.0) {
                Collections.sort(HostNest_SR); // Nest j 가 업데이트 되었으므로 정렬
                bestIdx = 0;
            }
        }
    }
    void employOneCuckoo_DR() throws Exception {
        //Generating New Solution (Get a cuckoo "i" randomly by Lévy flights)
        newCuckoo_DR = getCuckooI_DR();
        //Choose an individual nest "j" randomly in the population/nests (avoid the current best nest)
        Solution_DR nestJ;
        int j = rd.nextInt(HostNestNum);
        while(j == bestIdx) j = rd.nextInt(HostNestNum);//(current best 는 제외시킴)
        nestJ = HostNest_DR.get(j);
        //Compare both solutions and Update the best one
        if(newCuckoo_DR.compareTo(nestJ) <= -1) {
            //Replace j by i(new one)
            HostNest_DR.set(j, newCuckoo_DR);
            if (newCuckoo_DR.compareTo(HostNest_DR.get(bestIdx)) <= -1){
                bestIdx = j;
            }
            if(Pa != 0.0) {
                Collections.sort(HostNest_DR); // Nest j 가 업데이트 되었으므로 정렬
                bestIdx = 0;
            }
        }
    }
    // Method : The 4th phase -> Abandon a fraction Pa of worse solutions that will be replaced by new ones
    void abandonWorseNests(String layoutProbType) throws Exception {
        if(layoutProbType=="SR")abandonWorseNests_SR();
        else if(layoutProbType=="DR")abandonWorseNests_DR();
    }
    void abandonWorseNests_SR() throws Exception {
        int numAbandonNest = (int)Math.round(Pa*HostNestNum);
        for (int i=0; i< numAbandonNest; i++) {
            //Find the current worst nest & Replace some of the worst nests by constructing new solutions/nests
            int worstIndex = HostNestNum-i-1;
            newCuckoo_SR = replaceNest(HostNest_SR.get(bestIdx));
            HostNest_SR.set(worstIndex, newCuckoo_SR);

            if (newCuckoo_SR.compareTo(HostNest_SR.get(i)) <= -1){
                bestIdx = worstIndex;
                if (newCuckoo_SR.compareTo(currBestCuckoo_SR) <= -1) {
                    out.print(newCuckoo_SR.departSeq+"\t");
                    currBestCuckoo_SR =  (Solution_SR) newCuckoo_SR.clone();
                    out.println("best OFV update_w : "+ currBestCuckoo_SR.OFV+"\tIteration: "+ RK_Main.numIter);
                    currBestCuckoo_SR.iterNum = RK_Main.numIter;
                    RK_Main.numIter = 0;
                }
            }
        }
    }
    void abandonWorseNests_DR() throws Exception {
        int numAbandonNest = (int)Math.round(Pa*HostNestNum);
        for (int i=0; i< numAbandonNest; i++) {
            //Find the current worst nest & Replace some of the worst nests by constructing new solutions/nests
            int worstIndex = HostNestNum-i-1;
            newCuckoo_DR = replaceNest(HostNest_DR.get(bestIdx));
            HostNest_DR.set(worstIndex, newCuckoo_DR);

            if (newCuckoo_DR.compareTo(HostNest_DR.get(bestIdx)) <= -1){
                bestIdx = worstIndex;
            }
        }
    }
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// Method : Get random walk value
    private static final double LAMBDA = 3.0/2.0; // λ
    private static final double SIGMA_U = Math.pow(((gamma(1.0+ LAMBDA) * Math.sin((Math.PI* LAMBDA)/2.0)) / (gamma((1.0+ LAMBDA)/2.0) * LAMBDA * Math.pow(2.0,(LAMBDA -1)/2))),(1/ LAMBDA));
    private static final double SIGMA_V = 1.0;

    private static double stepValue(){
        double step;
        //==================================================================================================================
        // u,v value 구하기 (u~N(0,sigmaU^2), v~N(0,sigmaV^2))
        // u는 평균이 0, 표준편차가 sigmaU인 정규분포 확률변수
        // v는 평균이 0, 표준편차가 sigmaV인 정규분포 확률변수
        double u = ThreadLocalRandom.current().nextGaussian()* SIGMA_U;
        double v = ThreadLocalRandom.current().nextGaussian()* SIGMA_V;

        // generate stepSize => u~N(0, sigma) / v~N(0, 1)^(1/lambda)
        // stepSize = u / (|v|)^(1/Lambda)
        step = u / Math.pow((Math.abs(v)),(1.0/ LAMBDA));
        //step = new Random().nextGaussian()*(u / Math.pow((Math.abs(v)),(1/lambda))); => 수민 closedLoop 논문 version

        return step;
    } //s is step size drawn from a Lévy distribution.
    private static double Levy(double s, double λ){ return 1.0/(Math.pow(Math.abs(s),λ));}

// Method : Generate New Solution ///////////////////////////////////////////////
    private void generateNewSolutionByLévyFlights(Solution_SR nest) throws Exception {
        /** Some of the new solutions should be generated by Lévy walk around the best solution obtained so far,
         *  this will speed up the local search.
         *
         *  However, a substantial fraction of the new solutions should be generated by far field randomization
         *  and whose locations should be far enough from the current best solution,
         *  this will make sure the system will not be trapped in a local optimum **/
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        updateKeySeq(nest);
        steepestDescent_best("SR");
    }
    private void generateNewSolutionByLévyFlights(Solution_DR nest) throws Exception {
        /** Some of the new solutions should be generated by Lévy walk around the best solution obtained so far,
         *  this will speed up the local search.
         *
         *  However, a substantial fraction of the new solutions should be generated by far field randomization
         *  and whose locations should be far enough from the current best solution,
         *  this will make sure the system will not be trapped in a local optimum **/
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        updateKeySeq(nest);
        //newDepartSeq = g.departSeqUpdate(newKeySeq);
        steepestDescent_best("DR");
    }
    private void updateKeySeq(Solution_SR tmpSol) throws Exception {
        int rnIntDS = numDepart;
        //int rnIntDS = rd.nextInt(numDepart-1)+1;
        LinkedList<Double> tmpKeySeq = new LinkedList<>(tmpSol.keySeq);
        LinkedList<Integer> tmpDepartSeq;

        //KeySeq update
        for(int i = 0; i < rnIntDS; i++){
            double key = tmpKeySeq.get(i);
            double step = stepValue();
            double stepLength = step* new Random().nextGaussian();
            double newKey = key + alpha*stepLength;
            if(newKey > 1){
                //departSeq update
                if(key == Collections.max(tmpKeySeq)) continue;
                tmpDepartSeq = g.departSeqUpdate(tmpKeySeq);
                int tempValue = i+1;
                tmpDepartSeq.remove(tmpDepartSeq.indexOf(tempValue));
                tmpDepartSeq.addLast(tempValue);

                tmpKeySeq = g.keySeqUpdate(tmpDepartSeq);
            } else if (newKey < 0) {
                if(key == Collections.min(tmpKeySeq)) continue;
                tmpDepartSeq = g.departSeqUpdate(tmpKeySeq);
                int tempValue = i+1;
                tmpDepartSeq.remove(tmpDepartSeq.indexOf(tempValue));
                tmpDepartSeq.addFirst(tempValue);

                tmpKeySeq = g.keySeqUpdate(tmpDepartSeq);
            } else {
                tmpKeySeq.set(i,newKey);
            }
        }
        newKeySeq = tmpKeySeq;
        double currOFV = tmpSol.OFV;
        double newOFV = new Solution_SR(g.departSeqUpdate(newKeySeq),newKeySeq).OFV;
        if(currOFV == newOFV) {
            double rn = Math.random();
            if(rn < 0.5) tmpDepartSeq = g.twoSwapMoveOperator(g.departSeqUpdate(newKeySeq));
            else tmpDepartSeq = g.insertMoveOperator(g.departSeqUpdate(newKeySeq));
            newKeySeq = g.keySeqUpdate(tmpDepartSeq);
        }
    }
    private void updateKeySeq(Solution_DR tmpSol) throws Exception {
        //int rnIntDS = rd.nextInt(numDepart-1)+1;
        LinkedList<Double> tmpKeySeq = new LinkedList<>(tmpSol.keySeq);
        LinkedList<Integer> tmpDepartSeq = new LinkedList<>(tmpSol.departSeq);

        //KeySeq update
        for(int i = 0; i < numDepart; i++){
            double key = tmpKeySeq.get(i);
            double step = stepValue();
            double stepLength = step* ThreadLocalRandom.current().nextGaussian();
            double newKey = key + alpha*stepLength;
            if(newKey > 1){
                //departSeq update
                if(key == Collections.max(tmpKeySeq)) continue;
                int tempValue = i+1;
                tmpDepartSeq.remove((Integer) tempValue);
                tmpDepartSeq.addLast(tempValue);

                tmpKeySeq = g.keySeqUpdate(tmpDepartSeq);

            } else if (newKey < 0) {
                if(key == Collections.min(tmpKeySeq)) continue;
                int tempValue = i+1;
                tmpDepartSeq.remove((Integer) tempValue);
                tmpDepartSeq.addFirst(tempValue);

                tmpKeySeq = g.keySeqUpdate(tmpDepartSeq);
            } else {
                //newKeySeq.add(newKey);
                tmpKeySeq.set(i,newKey);
            }
        }
        newKeySeq = tmpKeySeq;
//        double currOFV = tmpSol.OFV;
//        double newOFV = new Solution_DR(g.departSeqUpdate(newKeySeq),newKeySeq).OFV;
        /*if(currOFV == newOFV) {
            double rn = Math.random();
            if(rn < 0.5) tmpDepartSeq = g.twoSwapMoveOperator(g.departSeqUpdate(newKeySeq));
            else tmpDepartSeq = g.bigJumpOperator(g.departSeqUpdate(newKeySeq));
            newKeySeq = g.keySeqUpdate(tmpDepartSeq);
        }*/
    }

// Method : Replace some nests by constructing new solutions/nests using Lévy flights (The local random walk) /////////////////////////
    private Solution_SR replaceNest(Solution_SR worstNest) throws Exception {
        Solution_SR newCuckoo;
        while (true){
            if(ThreadLocalRandom.current().nextDouble() < 0.5){
                newDepartSeq = g.inversionOperator(worstNest.departSeq);
            }
            else newDepartSeq = g.bigJumpOperator(worstNest.departSeq);
            newKeySeq = g.keySeqUpdate(newDepartSeq);
            //updateKeySeq(worstNest);
            steepestDescent_best("SR");
            newCuckoo = new Solution_SR(newDepartSeq,newKeySeq);
            // 새로운 Nest를 다른 모든 Nest의 OFV값과 비교
            boolean sameNest = preventOverlap(newCuckoo);
            // 새로운 Nest의 OFV가 기존의 다른 Nest의 OFV값과 같은 것이 없으면
            if(!(sameNest)){
                break;
            }
        }
        return newCuckoo;
    }
    private Solution_DR replaceNest(Solution_DR worstNest) throws Exception {
        Solution_DR newCuckoo;
        while (true){
            if(ThreadLocalRandom.current().nextDouble() < 0.5){
                newDepartSeq = g.inversionOperator(worstNest.departSeq);
            }
            else newDepartSeq = g.bigJumpOperator(worstNest.departSeq);
            newKeySeq = g.keySeqUpdate(newDepartSeq);
            //updateKeySeq(worstNest);
            steepestDescent_best("DR");
            newCuckoo = new Solution_DR(newDepartSeq,newKeySeq);
            // 새로운 Nest를 다른 모든 Nest의 OFV값과 비교
            boolean sameNest = preventOverlap(newCuckoo);
            // 새로운 Nest의 OFV가 기존의 다른 Nest의 OFV값과 같은 것이 없으면
            if(!(sameNest)){
                break;
            }
        }
        return newCuckoo;
    }
// Method : LocalSearch
    private void steepestDescent_first() throws Exception {
        /**Two kinds of neighborhood structures(move operator) are often used in local search procedure for the SRFLP
         * 1. Swap neighborhood search (move operator)
         * 2. Insertion neighborhood search(move operator)
         * ==> Insertion neighborhood search performs better than Swap neighborhood search
         *
         * the best-improvement local search obtains high quality solutions
         * However, due to the exhaustive neighborhood search, 'the best improvement' is very time consuming
         * To the flow shop problem, the first-improvement local search gives significantly better results than the best-improvement one, in the same amount of time
         * ==> local search procedure looks for solutions by probing the first-improvement solution in the insertion neighborhood.*/
        LinkedList<Integer> tempDepartSeq = new LinkedList<>();
        LinkedList<Integer> bestDepartSeq = new LinkedList<>();
        newDepartSeq = g.departSeqUpdate(newKeySeq);
        boolean stop;
        boolean isImproved = false;
        //--------------------------------------------------------------------------------------------------------
        stop = false;
        double currOFV = new Solution_SR(newDepartSeq,newKeySeq).OFV;
        double bestOFV = currOFV;
        while(!stop){ //stop = false 이면 계속 반복
            tempDepartSeq.clear();
            tempDepartSeq.addAll(newDepartSeq);

            g.insertMoveOperator(tempDepartSeq);
            double newOFV = new Solution_SR(tempDepartSeq,newKeySeq).OFV;

            if(newOFV < bestOFV){
                isImproved = true;
                bestOFV = newOFV;
                bestDepartSeq.clear();
                bestDepartSeq.addAll(tempDepartSeq);
            }
            else {
                stop = true;
            }
        }
        if(isImproved) {
            newDepartSeq.clear();
            newDepartSeq.addAll(bestDepartSeq);
            g.keySeqUpdate(newKeySeq,newDepartSeq);
        }
    }
    private void steepestDescent_best(String layoutProbType) throws Exception {
        /**Two kinds of neighborhood structures(move operator) are often used in local search procedure for the SRFLP
         * 1. Swap neighborhood search (move operator)
         * 2. Insertion neighborhood search(move operator)
         * ==> Insertion neighborhood search performs better than Swap neighborhood search
         *
         * the best-improvement local search obtains high quality solutions
         * However, due to the exhaustive neighborhood search, 'the best improvement' is very time consuming
         * To the flow shop problem, the first-improvement local search gives significantly better results than the best-improvement one, in the same amount of time
         * ==> local search procedure looks for solutions by probing the first-improvement solution in the insertion neighborhood.*/
        LinkedList<Integer> tempDepartSeq;
        boolean stop;
        //--------------------------------------------------------------------------------------------------------
        stop = false;
        newDepartSeq = g.departSeqUpdate(newKeySeq);

        double currOFV = 0;
        if(layoutProbType=="SR") currOFV = new Solution_SR(newDepartSeq).OFV;
        else if(layoutProbType=="DR") currOFV = new Solution_DR(newDepartSeq).OFV;
        while(!stop){ //stop = false 이면 계속 반복
            tempDepartSeq = new LinkedList<>(newDepartSeq);

            double rn = ThreadLocalRandom.current().nextDouble();
            if(rn<0.5)tempDepartSeq = g.insertMoveOperator(tempDepartSeq);
            else tempDepartSeq = g.twoSwapMoveOperator(tempDepartSeq);

            double newOFV = 0.0;
            if(layoutProbType=="SR") newOFV= new Solution_SR(tempDepartSeq).OFV;
            else if(layoutProbType=="DR")newOFV= new Solution_DR(tempDepartSeq).OFV;

            if(newOFV < currOFV){
                currOFV = newOFV;
                newDepartSeq = new LinkedList<>(tempDepartSeq);
            }
            else {
                stop = true;
            }
        }
        newKeySeq = g.keySeqUpdate(newDepartSeq);
    }
    private void FILS() throws Exception {
        /** The purpose of this Method is to seek a local optimum in the search space surrounding a given solution
         * The main idea of this local search is to look for a local optimum by using a first improvement strategy in the exchange neighborhood.
         * It is a simple and powerful algorithm for combinatorial optimizations.
         */
        LinkedList<Integer> improvedDepartSeq = new LinkedList<>();
        double currOFV = new Solution_SR(newDepartSeq,newKeySeq).OFV;
        boolean flag = true;
        while(flag){
            flag = false;
            ArrayList<int[]> tempPool = new ArrayList<>(g.poolOfInsertingOperations);
            int sizeOfPool = tempPool.size();
            for(int i=0; i<sizeOfPool; i++){
                improvedDepartSeq.clear();
                improvedDepartSeq.addAll(newDepartSeq);
                int[] tempOperation = tempPool.get(new Random().nextInt(tempPool.size()));
                g.insertMoveOperator(improvedDepartSeq,tempOperation[0],tempOperation[1]);
                tempPool.remove(tempOperation);
                double newOFV = new Solution_SR(improvedDepartSeq,newKeySeq).OFV;
                if(newOFV < currOFV){
                    currOFV = newOFV;
                    newDepartSeq.clear();
                    newDepartSeq.addAll(improvedDepartSeq);
                    g.keySeqUpdate(newKeySeq,newDepartSeq);
                    flag = true;
                    break;
                }
            }
        }
    }// First Improvement Local Search
// Method : Prevent Overlap Solution
    private boolean preventOverlap(Solution_SR newCuckoo){
        boolean sameSolution = false;
        for (Solution_SR s : HostNest_SR) {
            if (s.OFV == newCuckoo.OFV) {
                sameSolution = true;
                break;
            }
        }
        return sameSolution;
    }
    private boolean preventOverlap(Solution_DR newCuckoo){
        boolean sameSolution = false;
        for (Solution_DR s : HostNest_DR) {
            if (s.OFV == newCuckoo.OFV) {
                sameSolution = true;
                break;
            }
        }
        return sameSolution;
    }
}//Experiment Class