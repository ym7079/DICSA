package kr.kau.yym7079.Ver_4;

import java.util.*;

class Generator {
    final static private int numDepart = ProbDataSet.getNumDepart();
    final static public LinkedList<Double> normalizedKeySeq = getNormalizedKeySeq(numDepart);
    static private LinkedList<Double> getNormalizedKeySeq(int numDepart){
        LinkedList<Double> keySeq = new LinkedList<>();

        double interval = 1.0/(double)numDepart;
        double key = interval/2.0;

        for(double i= 0; i<numDepart; i++){
            keySeq.add(key);
            key += interval;
        }
        return keySeq;
    }

    final static public ArrayList<int[]> poolOfInsertingOperations = getPoolOfInsertingOperations(numDepart);
    static private ArrayList<int[]> getPoolOfInsertingOperations(int numDepart){
        ArrayList<int[]> result = new ArrayList<>();
        for(int i=0; i<numDepart-1; i++){
            for(int j=0; j<numDepart; j++){
                if(i==j)continue;
                else if(j-i == 1)continue;
                result.add(new int[]{i,j});
            }
        }
        return result;
    }

    public LinkedList<Integer> generateInitSeedPermutation(){
        LinkedList<Integer> result = new LinkedList<>();

        double[] lengthSet = ProbDataSet.getLength();
        LinkedList<Double> tmpLengthSet = new LinkedList<>();
        LinkedList<Double> nonDecreasingOrderLengthSet = new LinkedList<>();
        for(double length: lengthSet){
            tmpLengthSet.add(length);
            nonDecreasingOrderLengthSet.add(length);
        }
        Collections.sort(nonDecreasingOrderLengthSet);

        LinkedList<Integer> nonDecreasingSeq = new LinkedList<>();
        for(double length: nonDecreasingOrderLengthSet){
            nonDecreasingSeq.add(tmpLengthSet.indexOf(length));
            tmpLengthSet.set(tmpLengthSet.indexOf(length),0.0);
        }

        if(numDepart %2 == 0){//when n is even
            for(int i=nonDecreasingSeq.size()-1; i>=1; i-=2){
                result.add(nonDecreasingSeq.get(i)+1);
            }
            for(int i=0; i<nonDecreasingSeq.size(); i+=2){
                result.add(nonDecreasingSeq.get(i)+1);
            }
        }else{//when n is odd
            for(int i=nonDecreasingSeq.size()-1; i>=0; i-=2){
                result.add(nonDecreasingSeq.get(i)+1);
            }
            for(int i=1; i<nonDecreasingSeq.size(); i+=2){
                result.add(nonDecreasingSeq.get(i)+1);
            }
        }
        return result;
    }
    public void keySeqUpdate(LinkedList<Double>keySeq,LinkedList<Integer>departSeq){
        LinkedList<Double> tmpSeq = (LinkedList<Double>) keySeq.clone();

        for(int i=0; i<keySeq.size(); i++){
            keySeq.set(departSeq.get(i)-1,tmpSeq.get(i));
        }
    }
    public LinkedList<Double> keySeqUpdate(LinkedList<Integer>departSeq){
        LinkedList<Double> keySeq = new LinkedList<>(normalizedKeySeq);
        LinkedList<Double> tmpSeq = new LinkedList<>();
        tmpSeq.addAll(keySeq);

        for(int i=0; i<keySeq.size(); i++){
            keySeq.set(departSeq.get(i)-1,tmpSeq.get(i));
        }
        return keySeq;
    }
    public LinkedList<Integer> departSeqUpdate(LinkedList<Double>keySeq){
        LinkedList<Integer> result = new LinkedList<>();

        LinkedList<Double> tempSeq = new LinkedList<>();
        tempSeq.addAll(keySeq);
        Collections.sort(tempSeq);
        for(double key : tempSeq){
            int index = keySeq.indexOf(key);
            result.add(index+1);
        }
        return result;
    }

    public LinkedList<Integer> randomPermute() {
        LinkedList<Integer> result = new LinkedList<>();
        for(int i=1; i<=numDepart; i++) {
            result.add(i);
        }
        Collections.shuffle(result);
        return result;
    }
    public int[] randomPermute(int num, int upperbound, int bestIdx) {
        ArrayList<Integer> tmpResult = new ArrayList<>();
        for(int i=1; i<=upperbound; i++) {
            if(i-1 == bestIdx) continue;
            tmpResult.add(i-1);
        }
        Collections.shuffle(tmpResult);
        int[]result = new int[num];
        for(int i=0; i<num; i++){
            result[i] = tmpResult.get(i);
        }

        return result;
    }
    public LinkedList<Integer> randomKeyPermute(LinkedList<Double>keySeq){

        keySeq.addAll(normalizedKeySeq);
        LinkedList<Integer> result = randomPermute();
        keySeqUpdate(keySeq,result);

        return result;
    }

    public void inverseMoveOperator(LinkedList<Double> keySeq) throws Exception {
        List<Double> subSeq;

        int idx1 = new Random().nextInt(keySeq.size()+1);
        int idx2 = new Random().nextInt(keySeq.size()+1);
        while(idx1 == idx2 || Math.abs(idx1-idx2) == 1) {
            idx2 = new Random().nextInt(keySeq.size()+1);
            if(idx1 == 0 && idx2 == keySeq.size()) continue;
            else if (idx1 == keySeq.size() && idx2 == 0) continue;
        }
        int idxMin = Math.min(idx1,idx2);
        int idxMax = Math.max(idx1,idx2);

        subSeq = keySeq.subList(idxMin,idxMax);
        Collections.reverse(subSeq);

    }
    public LinkedList<Integer> inversionOperator(LinkedList<Integer> departSeq) throws Exception {
        /**
         * The purpose of this operator is to help the search to escape from local optima.
         * It tries to explore new search regions surrounding local optima.
         * The main idea of this operator is to change the positions of a few facilities in the permutation of the local optimum.
         * An inversion operator is used as the mutation operator.
         * It works by selecting a substring from a permutation and flipping it to form a new one.
         * The substring with a few facilities are randomly selected.
         */
        LinkedList<Integer> result = new LinkedList<Integer>(departSeq);
        List subSeq;

        int idx1 = new Random().nextInt(departSeq.size()+1);
        int idx2 = new Random().nextInt(departSeq.size()+1);
        while(idx1 == idx2 || Math.abs(idx1-idx2) == 1) {
            idx2 = new Random().nextInt(departSeq.size()+1);
            if(idx1 == 0 && idx2 == departSeq.size()) continue;
            else if (idx1 == departSeq.size() && idx2 == 0) continue;
        }
        int idxMin = Math.min(idx1,idx2);
        int idxMax = Math.max(idx1,idx2);

        subSeq = result.subList(idxMin,idxMax);
        Collections.reverse(subSeq);

        return result;
    }
    public void swapMoveOperator(LinkedList<Integer> departSeq) throws Exception {
        int idx1 = new Random().nextInt(departSeq.size());
        int idx2 = new Random().nextInt(departSeq.size());
        while(idx1 == idx2) {
            idx2 = new Random().nextInt(departSeq.size());
        }
        Collections.swap(departSeq,idx1,idx2);
    }
    public LinkedList<Integer> insertMoveOperator(LinkedList<Integer> departSeq) throws Exception {
        LinkedList<Integer> result = new LinkedList<>(departSeq);

        int removingIdx = new Random().nextInt(departSeq.size());
        int insertingIdx = new Random().nextInt(departSeq.size());
        while(removingIdx == insertingIdx) {
            insertingIdx = new Random().nextInt(departSeq.size());
        }

        int tempValue = departSeq.get(removingIdx);
        result.remove(removingIdx);
        result.add(insertingIdx,tempValue);

        return result;
    }
    public void insertMoveOperator(LinkedList<Integer> tmpSeq,int removingIdx, int insertingIdx) throws Exception {
        int tempValue = tmpSeq.get(removingIdx);
        tmpSeq.remove(removingIdx);
        tmpSeq.add(insertingIdx,tempValue);
    }
    public LinkedList<Integer> twoSwapMoveOperator(LinkedList<Integer> tmpDepartSeq) throws Exception{
        int idx = new Random().nextInt(tmpDepartSeq.size());
        boolean upDown = new Random().nextBoolean();
        if(idx == 0 && !(upDown)) upDown = true;
        else if(idx == tmpDepartSeq.size()-1 && upDown) upDown = false;

        if(upDown) Collections.swap(tmpDepartSeq,idx,idx+1);
        else Collections.swap(tmpDepartSeq,idx,idx-1);

        return tmpDepartSeq;
    }
    public LinkedList<Integer> bigJumpOperator(LinkedList<Integer> departSeq) throws Exception{
        LinkedList<Integer> result = new LinkedList<>(departSeq);

        int removingIdx;
        int insertingIdx;
        while(true){
            removingIdx = new Random().nextInt(departSeq.size());
            insertingIdx = new Random().nextInt(departSeq.size());
            while (removingIdx == insertingIdx) {
                insertingIdx = new Random().nextInt(departSeq.size());
            }
            if(Math.abs(removingIdx-insertingIdx) > numDepart/2)break;
        }
        int tempValue = departSeq.get(removingIdx);
        result.remove(removingIdx);
        result.add(insertingIdx,tempValue);

        return result;
    }

}// Generator Class