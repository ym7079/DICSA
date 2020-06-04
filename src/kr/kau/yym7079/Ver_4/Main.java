package kr.kau.yym7079.Ver_4;

public class Main {
    public static void main(String[] args) throws Exception {
        /**The second phase: Start Searching with a fraction Pc of Smart Cuckoos */
        //smart cuckoos begin by exploring new areas from the current solutions ==> diversification
        /**A fraction Pc of cuckoos search for solutions from the current position and try to improve them.
         They move from one region to another via Lévy flights to locate the best solution
         in each region without being trapped in a local optimum.**/
      /*int numSmartCuckoo = (int)Math.round(0.6*HostNestNum);
        int i = 1;
        while(true){
            if(i == numSmartCuckoo) break;
            // Generating New Solution
            int j = rd.nextInt(HostNestNum);
            *//*if (j == nestBestIdx){
                continue;
            }*//*
            newDepartSeq = new LinkedList<>();
            newKeySeq = new LinkedList<>();
            exp.generateNewSolutionByLévyFlights(exp.HostNest.get(j),newDepartSeq,newKeySeq);
            newCuckoo = new Solution(newDepartSeq,newKeySeq);
            // Prevent Overlap Solution
            boolean sameSolution = false;
            for (Solution s : exp.HostNest) {
                if (s.OFV == newCuckoo.OFV) {
                    sameSolution = true;
                    break;
                }
            }
            if (!(sameSolution)) {
                if(newCuckoo.compareTo(exp.HostNest.get(j)) <= -1) {
                    exp.HostNest.set(j, newCuckoo);//==> ofv 값이 안 좋더라도 무조건 update
                    if (newCuckoo.compareTo(exp.HostNest.get(nestBestIdx)) <= -1) {
                        nestBestIdx = j;//--> population 에서 ofv 가 가장 좋은 solution 보다 더 좋으면
                        if (newCuckoo.compareTo(currBestNest) <= -1) {
                            System.out.println("best OFV update : " + currBestNest.OFV + " ==> " + newCuckoo.OFV);
                            currBestNest = newCuckoo;
                            System.out.println(currBestNest.departSeq);
                            numIter = 0;
                        }
                    }
                }
                i++;
            }
        }*/
      /*if(Math.random()< 0.6){
            newDepartSeq = new LinkedList<>();
            newKeySeq = new LinkedList<>();
            int c = new Random().nextInt(HostNestNum);
            exp.getCuckooByLévyFlights(c,newDepartSeq,newKeySeq);
            newCuckoo = new Solution(newDepartSeq,newKeySeq);
            if(newCuckoo.compareTo(exp.HostNest.get(c)) <= -1){
                boolean sameSolution = false;
                for(Solution s : exp.HostNest) {
                    if (s.OFV == newCuckoo.OFV) {
                        sameSolution = true;
                        break;
                    }
                }
                if(!(sameSolution)){
                    exp.HostNest.set(c, newCuckoo);
                    if(newCuckoo.compareTo(exp.HostNest.get(0)) <= -1){
                        currBestNest = newCuckoo;
                        bestIdx = c;
                    }
                }
            }
        }*/
        /***The third phase: Employ one cuckoo to search for a new good solution,starting from the best solution of the population
         (The global explorative random walk) */

        //Generating New Solution (Get a cuckoo "i" randomly by Lévy flights)
        /*while(true) {
            newDepartSeq = new LinkedList<>();
            newKeySeq = new LinkedList<>();
            exp.generateNewSolutionByLévyFlights(currBestNest, newDepartSeq, newKeySeq);
            newCuckoo = new Solution(newDepartSeq, newKeySeq);
            // Prevent Overlap Solution
            boolean sameSolution = false;
            for (Solution s : exp.HostNest) {
                if (s.OFV == newCuckoo.OFV) {
                    sameSolution = true;
                    break;
                }
            }
            if (!(sameSolution)) {
                break;
            }
        }
        //Choose an individual nest "j" randomly in the population/nests (avoid the current best nest)
        Solution nestJ;
        int j = rd.nextInt(HostNestNum);
        *//*while(j == nestBestIdx) j = rd.nextInt(HostNestNum);*//*//(current best는 제외시킴)
        nestJ = exp.HostNest.get(j);
        //Compare both solutions and Update the best one
        if(newCuckoo.compareTo(nestJ) <= -1) {
            //Replace j by i(new one)
            exp.HostNest.set(j, newCuckoo);
            if (newCuckoo.compareTo(exp.HostNest.get(nestBestIdx)) <= -1){
                nestBestIdx = j;
                if (newCuckoo.compareTo(currBestNest) <= -1) {
                    System.out.println("best OFV update : "+currBestNest.OFV+" ==> "+newCuckoo.OFV);
                    currBestNest = newCuckoo;
                    System.out.println(currBestNest.departSeq);
                    numIter = 0;
                }
            }
            if(Pa != 0.0) {
                Collections.sort(exp.HostNest); // Nest j 가 업데이트 되었으므로 정렬
                nestBestIdx = 0;
            }
        }*/
        /***The last phase: Abandon a fraction Pa of worse solutions that will be replaced by new ones 2. The local random walk */
        // A fraction (Pa) of worse nests are abandoned and completely new nests(with new random solutions) are built
        /*Solution newNest;
        int numAbandonNest = (int)Math.round(Pa*HostNestNum);
        for (int k=0;k<numAbandonNest;k++) {

            int worstIndex = HostNestNum -k -1;

            // Find the current worst nest & Replace some of the worst nests by constructing new solutions/nests
            //newNest = new Solution();
            //newNest = exp.replaceNest(exp.HostNest.get(worstIndex).keySeq);
            newNest = exp.replaceNest(exp.HostNest.get(worstIndex));
            exp.HostNest.set(worstIndex, newNest);
            if (newNest.compareTo(exp.HostNest.get(nestBestIdx)) <= -1){
                nestBestIdx = worstIndex;
                if (newNest.compareTo(currBestNest) <= -1) {
                    System.out.println("best OFV update : "+currBestNest.OFV+" ==> "+newNest.OFV);
                    currBestNest = newNest;
                    System.out.println(currBestNest.departSeq);
                    numIter = 0;
                }
            }
        }*/
        // A fraction (Pa) of worse nests are abandoned and completely new nests(with new random solutions) are built
        /*if(Math.random() < Pa){
            newNest = exp.replaceNest(exp.HostNest.get(HostNestNum-1).keySeq);
            exp.HostNest.set(HostNestNum-1,newNest);
            if(newNest.OFV < currBestNest.OFV){
                currBestNest = newNest;
                numIter = 0;
                bestIdx = HostNestNum-1;
            }
        }*/
    }
}
