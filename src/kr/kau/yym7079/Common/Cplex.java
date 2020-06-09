package kr.kau.yym7079.Common;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;

import static java.lang.System.out;

public class Cplex {
//Variables
  //CPLEX variables
    private static IloCplex cplex;
    private IloNumVar[] x;
    private IloNumVar[][] α;
    //private IloNumVar[] 𝛾;
    private IloNumVar[][][] e;
    private Collection<IloConstraint> constraints = new Vector<>();
    private IloConstraint[][] overlappingPrevention;

  //Problem data variables
    private int numDepart;
    private int[][] flowSet;
    private double[] lengthSet;
    private double L;

  //Solution variables
    public double[] cx;
    public double objValue;
//Constructor
    public Cplex(ProbDataSet problem){
        this.numDepart = problem.numDepart;
        this.flowSet = problem.flow;
        this.lengthSet = problem.length;
        this.L = problem.totalLength; //Total length
    }
    public Cplex(int numDepart, int[][] flowSet, double[] lengthSet, double L){
        this.numDepart = numDepart;
        this.flowSet = flowSet;
        this.lengthSet = lengthSet;
        this.L = L; //Total length
    }
//Methods
    public void setLP(){
        try{
            //define new model
            cplex = new IloCplex();
            //--------------------------------------------------------------------------------------------------------------
            /**Variables**/

            //𝑥𝑖 = indicate the coordinate of the center of machine 𝑖
            x = new IloNumVar[numDepart];
            for(int i=0; i<numDepart; i++){
                x[i] = cplex.numVar(lengthSet[i]/2,L - (lengthSet[i]/2)); //constraint(11) (l_i/2) ≤ x_i ≤  L-(l_i/2)
            }

            //𝑑𝑖𝑗 = indicate distance between machines 𝑖 and 𝑗
            IloNumVar[][] distance = new IloNumVar[numDepart][]; // distance[i][j] ≥ 0;
            for(int i=0; i<numDepart; i++){
                distance[i] = cplex.numVarArray(numDepart,0, Double.MAX_VALUE);
                //Constraint(6),(7) -> absolute value of distance
                for(int j=i+1; j< numDepart; j++){ // only for upper-triangle of distance[i][j]
                    cplex.addGe(distance[i][j], cplex.diff(x[i],x[j])); // 𝑑𝑖𝑗 ≥ 𝑥𝑖 − 𝑥𝑗
                    cplex.addGe(distance[i][j], cplex.diff(x[j],x[i])); // 𝑑𝑖𝑗 ≥ 𝑥𝑗 − 𝑥𝑖
                }
            }

            //Set Binary Variables -------------------------------

            //𝛼𝑖𝑗 implicitly indicate the horizontal relative location of machines 𝑖 and 𝑗 in floor space
            α = new IloNumVar[numDepart][];
            for(int i=0; i<numDepart; i++){
                α[i] = cplex.boolVarArray(numDepart);
            }

            //𝛾𝑖 directly indicates the machine’s vertical location in floor space which is divided upper and lower side
            //𝛾 = cplex.boolVarArray(numDepart);

            //𝑒𝑘𝑖𝑗 was introduced to indicate the relative location of machines 𝑖,𝑗 and 𝑘 ;
            e = new IloNumVar[numDepart][numDepart][numDepart];
            for(int i=0; i< numDepart-1; i++){
                for(int j=i+1; j< numDepart; j++){ // 𝑖 < 𝑗
                    for(int k=0; k< numDepart; k++){
                        if(k==i || k==j) continue; // 𝑘 ≠ 𝑖, 𝑘 ≠ 𝑗
                        e[k][i][j] = cplex.boolVar(); // constraint(18) - 𝑒𝑘𝑖𝑗 ∈ {0,1}
                    }
                }
            }
            //--------------------------------------------------------------------------------------------------------------
            /**Objective function**/

            IloLinearNumExpr objective = cplex.linearNumExpr();
            for(int i =0; i < numDepart-1; i++){
                for (int j = i+1; j < numDepart; j++){
                    objective.addTerm(flowSet[i][j],distance[i][j]);
                }
            }
            cplex.addMinimize(objective,"Min objective function");
            //--------------------------------------------------------------------------------------------------------------
            /**Constraint**/
            // constraint(13)-(18)
            for(int i=0; i<numDepart-1; i++){
                for(int j=i+1; j<numDepart; j++){ // i < j
                    IloLinearNumExpr expr13 = cplex.linearNumExpr();
                    IloLinearNumExpr tempExpr1 = cplex.linearNumExpr();
                    IloLinearNumExpr tempExpr2 = cplex.linearNumExpr();
                    for(int k=0; k<numDepart; k++){
                        if(k == i || k == j) continue;
                        expr13.addTerm(lengthSet[k],e[k][i][j]);

                        cplex.addGe(e[k][i][j],cplex.diff(cplex.sum(α[i][k],α[k][j]),1),"constraint(16)_"+i+"_"+j+"_"+k); // constraint(16)
                        cplex.addGe(e[k][i][j],cplex.diff(cplex.sum(α[j][k],α[k][i]),1),"constraint(17)_"+i+"_"+j+"_"+k); // constraint(17)
                    }
                    tempExpr1.addTerm(-(lengthSet[i]+lengthSet[j])/2.0,α[i][j]);
                    tempExpr2.addTerm(-(lengthSet[i]+lengthSet[j])/2.0,α[j][i]);
                    cplex.addLe(expr13,cplex.sum(distance[i][j],tempExpr1,tempExpr2),"constraint(13)_"+i+"_"+j); // constraint(13)

                    cplex.addLe(cplex.sum(x[i],distance[i][j]),cplex.sum(x[j],cplex.prod(2*(L-(lengthSet[i]/2)-(lengthSet[j]/2)),cplex.sum(1,cplex.prod(-1,α[i][j])))),"constraint(14)_"+i+"_"+j); // constraint(14)
                }
            }
            for(int j=0; j<numDepart-1; j++){
                for(int i=j+1; i<numDepart; i++){
                    cplex.addLe(cplex.sum(x[i],distance[j][i]),cplex.sum(x[j],cplex.prod(2*(L-(lengthSet[i]/2)-(lengthSet[j]/2)),cplex.sum(1,cplex.prod(-1,α[i][j])))),"constraint(15)_"+i+"_"+j); // constraint(15)
                }
            }

            // constraint(20)-(23)
           /* for(int i=0; i<numDepart-1; i++){
                for(int j=i+1; j<numDepart; j++) { // i < j
                    cplex.addGe(cplex.sum(α[i][j],α[j][i],𝛾[i],𝛾[j]),1,"constraint(20)_"+i+"_"+j);                                     // constraint(20)
                    cplex.addLe(cplex.sum(α[i][j],α[j][i],𝛾[i],cplex.prod(-1,𝛾[j])),1,"constraint(21)_"+i+"_"+j);                   // constraint(21)
                    cplex.addLe(cplex.sum(α[i][j],α[j][i],cplex.prod(-1,𝛾[i]),𝛾[j]),1,"constraint(22)_"+i+"_"+j);                   // constraint(22)
                    cplex.addGe(cplex.sum(α[i][j],α[j][i],cplex.prod(-1,𝛾[i]),cplex.prod(-1,𝛾[j])),-1,"constraint(23)_"+i+"_"+j); // constraint(23)
                }
            }*/
        }catch(IloException exc){
            exc.printStackTrace();
        }
    }
    public void setInitDRLPModel(){
        try{
        /**Model**/
        //set new cplex model
            cplex = new IloCplex();
        //--------------------------------------------------------------------------------------------------------------
        /**Variables**/
        //𝑥𝑖 = indicate the coordinate of the center of machine 𝑖
            x = new IloNumVar[numDepart];
            for(int i=0; i<numDepart; i++){
                x[i] = cplex.numVar(lengthSet[i]/2,L - (lengthSet[i]/2)); //constraint(11) (l_i/2) ≤ x_i ≤  L-(l_i/2)
            }

        //𝑑𝑖𝑗 = indicate distance between machines 𝑖 and 𝑗
            IloNumVar[][] distance = new IloNumVar[numDepart][]; // distance[i][j] ≥ 0;
            for(int i=0; i<numDepart; i++){
                distance[i] = cplex.numVarArray(numDepart,0, Double.MAX_VALUE);
                //Constraint(6),(7) -> absolute value of distance
                for(int j=i+1; j< numDepart; j++){ // only for upper-triangle of distance[i][j]
                    cplex.addGe(distance[i][j], cplex.diff(x[i],x[j])); // 𝑑𝑖𝑗 ≥ 𝑥𝑖 − 𝑥𝑗
                    cplex.addGe(distance[i][j], cplex.diff(x[j],x[i])); // 𝑑𝑖𝑗 ≥ 𝑥𝑗 − 𝑥𝑖
                }
            }

        //set binary decision variables
        //𝛼𝑖𝑗 implicitly indicate the horizontal relative location of machines 𝑖 and 𝑗 in floor space
            /*α = new IloNumVar[numDepart][];
            for(int i=0; i<numDepart; i++){
                α[i] = cplex.numVarArray(numDepart,0.0,0.0);
            }*/

        //𝛾𝑖 directly indicates the machine’s vertical location in floor space which is divided upper and lower side
            //𝛾 = cplex.numVarArray(numDepart,0,1);
        //--------------------------------------------------------------------------------------------------------------
        /**Objective function**/
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for(int i =0; i < numDepart-1; i++){
                for (int j = i+1; j < numDepart; j++){
                    objective.addTerm(flowSet[i][j],distance[i][j]);
                }
            }
            cplex.addMinimize(objective,"Min objective function");
        //--------------------------------------------------------------------------------------------------------------
        /**Constraint**/
            overlappingPrevention = new IloConstraint[numDepart][numDepart];
            for (int i = 0; i < numDepart; i++) {
                for (int j = 0; j < numDepart; j++) {
                    if (i==j) continue;
                    overlappingPrevention[i][j] = cplex.le(cplex.sum(x[i],0.5*(lengthSet[i]+lengthSet[j])),x[j]);
                }
            }
        }catch(IloException exc){
            exc.printStackTrace();
        }
    }
    public void setConstraints(ArrayList <Integer> upperDepartSeq, ArrayList<Integer> lowerDepartSeq) throws IloException {
        if (!constraints.isEmpty()){
            cplex.remove(constraints.toArray(new IloConstraint[0]));
        }
        // 𝛼𝑖𝑗, 𝛾𝑖 값 결정
        constraints.clear();
        for(int i=0; i<upperDepartSeq.size(); i++){
           //constraints.add(cplex.addEq(𝛾[upperDepartSeq.get(i)-1],1,"constraint_decide Gamma_"+i)); // 𝛾𝑖 값 결정

            if(i == upperDepartSeq.size()-1)continue;
            constraints.add(cplex.addEq(α[upperDepartSeq.get(i)-1][upperDepartSeq.get(i+1)-1],1,"constraint_decide Alpha1_"+i+"_"+i+1));
            constraints.add(cplex.addEq(α[upperDepartSeq.get(i+1)-1][upperDepartSeq.get(i)-1],0,"constraint_decide Alpha2_"+i+"_"+i+1));

            constraints.add(cplex.addLe(x[upperDepartSeq.get(i)-1],x[upperDepartSeq.get(i+1)-1]));
            for(int j=i+1; j<upperDepartSeq.size(); j++){
                constraints.add(cplex.addEq(α[upperDepartSeq.get(i)-1][upperDepartSeq.get(j)-1],1,"constraint_decide Alpha1_"+i+"_"+j));
                constraints.add(cplex.addEq(α[upperDepartSeq.get(j)-1][upperDepartSeq.get(i)-1],0,"constraint_decide Alpha2_"+i+"_"+j));

                constraints.add(cplex.addLe(x[upperDepartSeq.get(i)-1],x[upperDepartSeq.get(j)-1]));

/*                for(int k=0; k<numDepart; k++){
                    if(k==i || k==j) continue;
                    if(i < k && k < j) cplex.addEq(e[k][i][j],1);
                    else cplex.addEq(e[k][i][j],0);
                }*/
            }
        }

        for(int i=0; i<lowerDepartSeq.size(); i++){
            //constraints.add(cplex.addEq(𝛾[lowerDepartSeq.get(i)-1],1)); // 𝛾𝑖 값 결정

            if(i == lowerDepartSeq.size()-1)continue;
            for(int j=i+1; j<lowerDepartSeq.size(); j++){
                constraints.add(cplex.addEq(α[lowerDepartSeq.get(i)-1][lowerDepartSeq.get(j)-1],1));
                constraints.add(cplex.addEq(α[lowerDepartSeq.get(j)-1][lowerDepartSeq.get(i)-1],0));

                constraints.add(cplex.addLe(x[lowerDepartSeq.get(i)-1],x[lowerDepartSeq.get(j)-1]));

/*                for(int k=0; k<numDepart; k++){
                    if(k==i || k==j) continue;
                    if(i < k && k < j) cplex.addEq(e[k][i][j],1);
                    else cplex.addEq(e[k][i][j],0);
                }*/
            }
        }
    }
    public void solveLP() throws IloException {
        //solve the model
        cplex.setOut(null);
        boolean isSolved = cplex.solve();
        if(isSolved){
            objValue = cplex.getObjValue();
        }else{
            cplex.output().println("Solution status = " + cplex.getStatus());
        }
    }
    public void solveLPModel(ArrayList <Integer> upperDepartSeq, ArrayList<Integer> lowerDepartSeq) throws IloException {
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
        cplex.setOut(null);
        boolean isSolved = cplex.solve();
        if(isSolved){
            //System.out.println("Solution status: " + cplex.getStatus());
            //System.out.println("LP Object values: " + cplex.getObjValue());
            objValue = cplex.getObjValue();

        }else{
            cplex.output().println("Solution status = " + cplex.getStatus());
        }

        // deactivate constraints
        for (int i = 1; i < sizeOfUpperSeq; i++) {
            left = upperDepartSeq.get(i-1)-1;
            right = upperDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            cplex.remove(overlappingPrevention[left][right]);
        }
        for (int i = 1; i < sizeOfLowerSeq; i++) {
            left = lowerDepartSeq.get(i-1)-1;
            right = lowerDepartSeq.get(i)-1;
            //activate overlapping prevention constraints
            cplex.remove(overlappingPrevention[left][right]);
        }
    }
    public void solveLPModel(boolean[] gammaSeq,boolean[][] alphaSeq) throws IloException {
        // fix alpha <- arrangement
        for (int i = 0; i < alphaSeq.length-1; i++) {
            for (int j = i+1; j < alphaSeq.length; j++) {
                if(gammaSeq[i] != gammaSeq[j]) continue;
                //activate overlapping prevention constraints
                if(alphaSeq[i][j])cplex.add(overlappingPrevention[i][j]);
                else cplex.add(overlappingPrevention[j][i]);
            }
        }
        //solve the model
        cplex.setOut(null);
        boolean isSolved = cplex.solve();
        if(isSolved){
            //System.out.println("Solution status: " + cplex.getStatus());
            //System.out.println("LP Object values: " + cplex.getObjValue());
            objValue = cplex.getObjValue();
            cx = cplex.getValues(x);
        }else{
            out.println("Solution status = " + cplex.getStatus());
        }

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
    /*public double getObjValue(){ return objValue; }
    public double[] getCx() { return cx; }*/
}
