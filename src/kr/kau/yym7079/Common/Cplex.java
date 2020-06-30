package kr.kau.yym7079.Common;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;
public class Cplex { //TODO: 일단 Cplex 클래스는 이대로 Fix (clearance instance issue 해결)
//Variables
  //CPLEX variables
    private static IloCplex cplex;
    private IloNumVar[] x;
    private IloConstraint[][] overlappingPrevention;

  //Problem data variables
    private final int numDepart;
    private final double[][] flowSet;
    private final double[] lengthSet;
    private double[][] clearanceSet;
    private double L;
    private final boolean isSymmetricFlow;

  //Solution variables
    public double[] cx;
    public double objValue;
//Constructor
    public Cplex(ProbDataSet problem){
        this.numDepart = problem.numDepart;
        this.flowSet = problem.flow;
        this.lengthSet = problem.length;
        if(problem.clearance!= null){
            this.clearanceSet = problem.clearance;
            isSymmetricFlow = false;
        }else{
            this.L = problem.totalLength; //Total length
            isSymmetricFlow = true;
        }

    }
    public Cplex(int numDepart, double[][] flowSet, double[] lengthSet, double L, double[][] clearanceSet){
        this.numDepart = numDepart;
        this.flowSet = flowSet;
        this.lengthSet = lengthSet;
        if (clearanceSet != null) {
            this.clearanceSet = clearanceSet;
            isSymmetricFlow = false;
        }else {
            this.L = L; //Total length
            isSymmetricFlow = true;
        }
    }
//Methods
    public void initDRLPModel(){
        try{
        /*Model**/
        //set new cplex model
            cplex = new IloCplex();
        //--------------------------------------------------------------------------------------------------------------
        /*Variables**/
        //𝑥𝑖 = indicate the coordinate of the center of machine 𝑖
            x = new IloNumVar[numDepart];
            for(int i=0; i<numDepart; i++){
                if(isSymmetricFlow) x[i] = cplex.numVar(lengthSet[i]/2,L - (lengthSet[i]/2)); //(l_i/2) ≤ x_i ≤  L-(l_i/2)
                else x[i] = cplex.numVar(lengthSet[i]/2,Double.MAX_VALUE); //
            }

        //𝑑𝑖𝑗 = indicate distance between machines 𝑖 and 𝑗
            IloNumVar[][] distance = new IloNumVar[numDepart][]; // distance[i][j] ≥ 0;
            for(int i=0; i<numDepart; i++){
                distance[i] = cplex.numVarArray(numDepart,0, Double.MAX_VALUE);
                //Constraint : absolute value of distance
                for(int j=0; j< numDepart; j++){
                    if(i == j) continue;
                    if (clearanceSet == null) {
                        if(i > j) continue;
                    }
                    cplex.addGe(distance[i][j], cplex.diff(x[i],x[j])); // 𝑑𝑖𝑗 ≥ 𝑥𝑖 − 𝑥𝑗
                    cplex.addGe(distance[i][j], cplex.diff(x[j],x[i])); // 𝑑𝑖𝑗 ≥ 𝑥𝑗 − 𝑥𝑖
                }
            }
        //--------------------------------------------------------------------------------------------------------------
        /*Objective function**/
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for(int i =0; i < numDepart; i++){
                for (int j = 0; j < numDepart; j++){
                    if(i == j) continue;
                    if (isSymmetricFlow) {
                        if(i > j) continue;
                    }
                    objective.addTerm(flowSet[i][j],distance[i][j]);
                }
            }
            cplex.addMinimize(objective,"Min objective function");
        //--------------------------------------------------------------------------------------------------------------
        /*Constraint**/
            overlappingPrevention = new IloConstraint[numDepart][numDepart];
            for (int i = 0; i < numDepart; i++) {
                for (int j = 0; j < numDepart; j++) {
                    if (i == j) continue;
                    if (clearanceSet == null){
                        overlappingPrevention[i][j] = cplex.le(cplex.sum(x[i],0.5*(lengthSet[i]+lengthSet[j])),x[j]);
                    }else{
                        overlappingPrevention[i][j] = cplex.le(cplex.sum(x[i],0.5*(lengthSet[i]+lengthSet[j])+clearanceSet[i][j]),x[j]);
                    }
                }
            }
        }catch(IloException exc){
            exc.printStackTrace();
        }
    }
// TODO: Double Row 중 UpperRow/LowerRow에 대해 순서결정해 constant또는 constraint 추가
//       (결정되는 Row에 대해 x변수(center값)결정 const 및 distance관련 const Eq(=)로 const추가) --> 일단보류
    public void solveDRLPModel(ArrayList <Integer> upperDepartSeq, ArrayList<Integer> lowerDepartSeq) throws IloException {
        // fix alpha <- arrangement
        int left; int right;

        int sizeOfUpperSeq = upperDepartSeq.size();
        for (int i = 1; i < sizeOfUpperSeq; i++) {
            left = upperDepartSeq.get(i-1)-1;
            right = upperDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            cplex.add(overlappingPrevention[left][right]);
        }
        int sizeOfLowerSeq = lowerDepartSeq.size();
        for (int i = 1; i < sizeOfLowerSeq; i++) {
            left = lowerDepartSeq.get(i-1)-1;
            right = lowerDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            cplex.add(overlappingPrevention[left][right]);
        }
        //solve the model
        solveLP();

        // deactivate constraints
        for (int i = 1; i < sizeOfUpperSeq; i++) {
            left = upperDepartSeq.get(i-1)-1;
            right = upperDepartSeq.get(i)-1;
            cplex.remove(overlappingPrevention[left][right]);
        }
        for (int i = 1; i < sizeOfLowerSeq; i++) {
            left = lowerDepartSeq.get(i-1)-1;
            right = lowerDepartSeq.get(i)-1;
            cplex.remove(overlappingPrevention[left][right]);
        }
    }
    public void solveDRLPModel(boolean[] gammaSeq, boolean[][] alphaSeq) throws IloException {
        // fix alpha <- arrangement
        for (int i = 0; i < alphaSeq.length-1; i++) {
            for (int j = i+1; j < alphaSeq.length; j++) {
                if(gammaSeq[i] != gammaSeq[j]) continue;
                //activate overlapping prevention constraints
                if(alphaSeq[i][j])cplex.add(overlappingPrevention[i][j]);
                else cplex.add(overlappingPrevention[j][i]);
            }
        }
        //solve model
        solveLP();
        // deactivate constraints
        for (int i = 0; i < alphaSeq.length-1; i++) {
            for (int j = i+1; j < alphaSeq.length; j++) {
                if(gammaSeq[i] != gammaSeq[j]) continue;
                //activate overlapping prevention constraints
                if(alphaSeq[i][j])cplex.remove(overlappingPrevention[i][j]);
                else cplex.remove(overlappingPrevention[j][i]);
            }
        }
    }
    public void solveDRLPModel(LinkedList<Integer> departSeq, int cutPoint) throws IloException{
        // fix alpha <- arrangement
        int left; int right;
        for (int i = 1; i < cutPoint; i++) {
            left = departSeq.get(i-1)-1;
            right = departSeq.get(i)-1;
            //activate overlapping prevention constraints
            cplex.add(overlappingPrevention[left][right]);
        }
        for (int i = cutPoint+1; i < numDepart; i++) {
            left = departSeq.get(i-1)-1;
            right = departSeq.get(i)-1;
            //activate overlapping prevention constraints
            cplex.add(overlappingPrevention[left][right]);
        }
        // solve model
        solveLP();

        // deactivate constraints
        for (int i = 1; i < cutPoint; i++) {
            left = departSeq.get(i-1)-1;
            right = departSeq.get(i)-1;
            //deactivate overlapping prevention constraints
            cplex.remove(overlappingPrevention[left][right]);
        }
        for (int i = cutPoint; i < numDepart; i++) {
            left = departSeq.get(i-1)-1;
            right = departSeq.get(i)-1;
            //deactivate overlapping prevention constraints
            cplex.remove(overlappingPrevention[left][right]);
        }
    }

    private void solveLP() throws IloException{
        //solve the model
        cplex.setOut(null);
        cplex.setParam(IloCplex.Param.Simplex.Limits.Iterations,3000);
        cplex.setParam(IloCplex.Param.Barrier.Limits.Iteration,0);
        cplex.setParam(IloCplex.Param.Threads,1);

        boolean isSolved = cplex.solve();
        if(isSolved){
            //System.out.println("Solution status: " + cplex.getStatus());
            //System.out.println("LP Object values: " + cplex.getObjValue());
            objValue = cplex.getObjValue();
            cx = cplex.getValues(x);
        }else{
            cplex.output().println("Solution status = " + cplex.getStatus());
        }
    }
    /*public double getObjValue(){ return objValue; }
    public double[] getCx() { return cx; }*/
}
