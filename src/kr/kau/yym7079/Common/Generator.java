package kr.kau.yym7079.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Generator {
//Variables
    private int numDepart;
    private double[] length;
    private int[][] flow;


//Constructor
    public Generator(ProbDataSet problem){
        numDepart = problem.numDepart;
        length = problem.length;
        flow = problem.flow;
    }
//Methods
    public int[] randPermutationArray(int arrayLength, int upperbound, int idxOfBestSol){
        int[]result = new int[arrayLength];
        for(int i=0; i<upperbound; i++) {
            if(i == idxOfBestSol) continue;
            result[i] = i;
        }
        Collections.shuffle(Arrays.asList(result));

        return result;
    }
    //public boolean[] rand

}