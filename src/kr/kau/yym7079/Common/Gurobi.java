/* Copyright 2020, Gurobi Optimization, LLC */

package kr.kau.yym7079.Common;

import gurobi.*;

import java.util.ArrayList;

import static java.lang.System.out;

public class Gurobi {
  //Gurobi variables
    private static GRBEnv env;
    private static GRBModel model;
    private GRBVar[] x;
    private GRBLinExpr[][] overlappingPrevention;
  //Problem data variables
    private int numDepart;
    private double[][] flowSet;
    private double[][] clearanceSet;
    private double[] lengthSet;
    private double L;

  //Solution variables
    public double[] cx;
    public double objValue;
  //Constructor
    public Gurobi(ProbDataSet problem){
        this.numDepart = problem.numDepart;
        this.flowSet = problem.flow;
        this.lengthSet = problem.length;
        if(problem.clearance!= null) this.clearanceSet = problem.clearance;
        this.L = problem.totalLength; //Total length
    }
    public Gurobi(int numDepart, double[][] flowSet, double[] lengthSet, double L, double[][] clearanceSet){
        this.numDepart = numDepart;
        this.flowSet = flowSet;
        this.lengthSet = lengthSet;
        this.L = L; //Total length
        if (clearanceSet != null) {
            this.clearanceSet = clearanceSet;
        }
    }

  //Method
    public void initDRLPModel(){
        try{
          // Create empty environment, set options, and start
            env = new GRBEnv(true);
            env.set("logFile", "DRLP.log");
            env.start();

          // Create empty model
            model = new GRBModel(env);

          // Create variables
            x = new GRBVar[numDepart];
            for (int i = 0; i < numDepart; i++) {
                x[i] = model.addVar(lengthSet[i]/2,L- lengthSet[i]/2,0,GRB.CONTINUOUS,"cx["+(i+1)+"]");
            }

            GRBVar[][] dist = new GRBVar[numDepart][];
            for(int i=0; i<numDepart; i++){
                dist[i] = model.addVars(null,null,null,null,null,0,numDepart);
                for (int j = 0; j < numDepart; j++) {
                    //dist[i][j] = model.addVar(lengthSet[i]/2,L- lengthSet[i]/2,0,GRB.CONTINUOUS,"dist["+i+1+"]["+j+1+"]");
                    if (i >= j) continue; // only for upper-triangle of distance[i][j]
                    GRBLinExpr distExpr = new GRBLinExpr();
                    distExpr.addTerm(1.0, x[i]); distExpr.addTerm(-1.0, x[j]); // -> 𝑥𝑖 − 𝑥𝑗
                    model.addConstr(dist[i][j],GRB.GREATER_EQUAL,distExpr,"distance Const["+(i+1)+"]["+(j+1)+"]"); // 𝑑𝑖𝑗 ≥ 𝑥𝑖 − 𝑥𝑗

                    distExpr = new GRBLinExpr();
                    distExpr.addTerm(1.0, x[j]); distExpr.addTerm(-1.0, x[i]); // -> 𝑥𝑗 − 𝑥𝑖
                    model.addConstr(dist[i][j],GRB.GREATER_EQUAL,distExpr,"distance Const["+(j+1)+"]["+(i+1)+"]"); // 𝑑𝑖𝑗 ≥ 𝑥𝑗 − 𝑥𝑖
                }
            }

          // Set objective: minimize fij×dij
            GRBLinExpr objectiveFunc = new GRBLinExpr();
            for (int i = 0; i < numDepart - 1; i++) {
                for (int j = i+1; j < numDepart; j++) { // only for upper-triangle of flow[i][j]*distance[i][j]
                    objectiveFunc.addTerm(flowSet[i][j],dist[i][j]);
                }
            }
            model.setObjective(objectiveFunc, GRB.MINIMIZE);

          // Add constraint: overlapping prevention
            overlappingPrevention = new GRBLinExpr[numDepart][];
            for (int i = 0; i < numDepart; i++) {
                overlappingPrevention[i] = new GRBLinExpr[numDepart];
                for (int j = 0; j < numDepart; j++) {
                    if (i==j) continue;
                    overlappingPrevention[i][j] = new GRBLinExpr();
                    overlappingPrevention[i][j].addTerm(1.0,x[i]);
                    overlappingPrevention[i][j].addConstant(0.5*(lengthSet[i]+lengthSet[j]));
                }
            }
        }catch (GRBException e){
            out.println("Error code" + e.getErrorCode() + ". " + e.getMessage());
        }
    }
    public void solveDRLPModel(ArrayList<Integer> upperDepartSeq, ArrayList<Integer> lowerDepartSeq) throws GRBException{
      // fix alpha <- arrangement
        int left; int right;
        GRBConstr[]OLPConstr = new GRBConstr[numDepart];
        int sizeOfUpperSeq = upperDepartSeq.size();
        for (int i = 1; i < sizeOfUpperSeq; i++) {
            left = upperDepartSeq.get(i-1)-1;
            right = upperDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            OLPConstr[left] = model.addConstr(overlappingPrevention[left][right],GRB.LESS_EQUAL,x[right],"const["+left+"]["+right+"]");
        }
        int sizeOfLowerSeq = lowerDepartSeq.size();
        for (int i = 1; i < sizeOfLowerSeq; i++) {
            left = lowerDepartSeq.get(i-1)-1;
            right = lowerDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            OLPConstr[left] = model.addConstr(overlappingPrevention[left][right],GRB.LESS_EQUAL,x[right],"const["+left+"]["+right+"]");
        }
      // Optimize model
        model.set(GRB.IntParam.OutputFlag,0);
        model.set(GRB.IntParam.LogToConsole,0);
        model.optimize();

        cx = new double[numDepart];
        int idx = 0;
        for (GRBVar grbVarX : x) {
            cx[idx] = grbVarX.get(GRB.DoubleAttr.X);
            idx++;
        }
        objValue = model.get(GRB.DoubleAttr.ObjVal);

      // deactivate constraints
        for (int i = 1; i < sizeOfUpperSeq; i++) {
            left = upperDepartSeq.get(i-1)-1;
            right = upperDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            model.remove(OLPConstr[left]);
        }
        for (int i = 1; i < sizeOfLowerSeq; i++) {
            left = lowerDepartSeq.get(i-1)-1;
            right = lowerDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            model.remove(OLPConstr[left]);
        }
    }
    public void disposeDRLPModel() throws GRBException {
        // Dispose of model and environment
        model.dispose();
        env.dispose();
    }
}
