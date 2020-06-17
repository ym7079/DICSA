package kr.kau.yym7079.Ver_5;

import kr.kau.yym7079.Common.Cplex;
import kr.kau.yym7079.Common.Gurobi;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.out;

public class Test_Gurobi {
    public static void main(String[] args) throws Exception {
        String probName = "Am13a";
        Integer upperList[] = {1,4,3,5,9};
        Integer lowerList[] = {2,8,13,12,11,6,7,10};

        ProbDataSet.init(probName,false);
        Solution.setProblemData();
        Generator.setProblemData();

        Cplex tempModel = new Cplex(ProbDataSet.numDepart, ProbDataSet.flow,ProbDataSet.length,ProbDataSet.totalLength,ProbDataSet.clearance);
        tempModel.initDRLPModel();
        tempModel.solveLPModel(new ArrayList<Integer>(Arrays.asList(upperList)),new ArrayList<Integer>(Arrays.asList(lowerList)));

        out.println(tempModel.objValue);
        for (double cx : tempModel.cx) {
            out.print(cx + "\t");
        }
        out.println();

        Gurobi GRBModel = new Gurobi(ProbDataSet.numDepart, ProbDataSet.flow,ProbDataSet.length,ProbDataSet.totalLength,ProbDataSet.clearance);
        GRBModel.initDRLPModel();
        GRBModel.solveDRLPModel(new ArrayList<Integer>(Arrays.asList(upperList)),new ArrayList<Integer>(Arrays.asList(lowerList)));
        GRBModel.disposeDRLPModel();

        out.println(GRBModel.objValue);
        for (double cx : GRBModel.cx) {
            out.print(cx + "\t");
        }
        out.println();
    }
}
