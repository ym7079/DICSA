package kr.kau.yym7079.Ver_5;

import kr.kau.yym7079.Common.Cplex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static java.lang.System.out;

public class Test2 {
    /*String[] probTypeSet = {"Classical", "Am", "AnKeVa"};
        for(String PROB_TYPE : probTypeSet){
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
                Cplex LPModel = new Cplex(ProbDataSet.numDepart,ProbDataSet.flow,ProbDataSet.length,ProbDataSet.totalLength);
                LPModel.setInitDRLPModel();
                Solution.setModel(LPModel);
                //read Cuckoo search algorithm parameters
                readParameterSet(probName, hostNestNumSet, probASet, probCSet, IS_CLASSICAL);

                bestParamNest = new LinkedList<>();

                for (int HostNestNum : hostNestNumSet) {
                    for (double Pa : probASet) {
                        for(double Pc: probCSet) {
                            if (Pa == 0.0 && Pc != 0.0) continue;
                            if (Pa == 0.0 || Pa == 0.25*//*||Pa ==0.35*//*) {
                                out.println("population size: " + HostNestNum + ",probability a: " + Pa + ",probability c: " + Pc +"-------------------------");
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
    }*/
}
