package kr.kau.yym7079.Common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ProbDataSet {
//Variables
    private String dataDir, csvFile;
//-----------------------------
    public final int numDepart;
    public final double totalLength;
    public final double[] length;
    public final double[][] flow;
    public double[][] clearance;
//Constructor
    public ProbDataSet(String probName) throws Exception{
        BufferedReader br;
        String[] line;

        dataDir = "dataset/Instance_Data/" + probName + "/";
    //set department length
        csvFile = dataDir + "length.csv";
        br = new BufferedReader(new FileReader(csvFile));

        numDepart =Integer.parseInt(br.readLine().split(",")[0]);

        length = new double[numDepart];
        double totalLength = 0;
        line = br.readLine().split(",");
        for(int i=0; i<line.length; i++) {
            length[i] = Double.parseDouble(line[i]);
            totalLength += length[i];
        }
        this.totalLength = totalLength;
        br.close();
        //setDepartLength();

    //set flow matrix
        flow = new double[numDepart][numDepart];

        csvFile = dataDir + "flow.csv";
        br = new BufferedReader(new FileReader(csvFile));
        for(int i=0; i<numDepart; i++) {
            line = br.readLine().split(",");
            for(int j=0; j<numDepart; j++) {
                flow[i][j] = Integer.parseInt(line[j]);
            } // for i
        } // for j
        br.close();
        //setFlow();

    //set clearance matrix
        if (probName.contains("Murray")){
            csvFile = dataDir + "clearance.csv";
            File clearanceFile = new File(csvFile);
            if (clearanceFile.exists()){
                br = new BufferedReader(new FileReader(csvFile));
                for(int i=0; i<numDepart; i++) {
                    line = br.readLine().split(",");
                    for(int j=0; j<numDepart; j++) {
                        clearance[i][j] = Double.parseDouble(line[j]);
                    } // for i
                } // for j
                br.close();
            }
        }
    }
//Methods
    private void setDepartLength() throws Exception {
        /*BufferedReader br;
        String[] line;
        //set department length set
        br = new BufferedReader(new FileReader(csvFile));

        numDepart =Integer.parseInt(br.readLine().split(",")[0]);

        length = new double[numDepart];
        totalLength = 0;
        line = br.readLine().split(",");
        for(int i=0; i<line.length; i++) {
            length[i] = Double.parseDouble(line[i]);
            totalLength += length[i];
        }
        br.close();*/
    }
    private void setFlow() throws Exception {
        /*flow = new int[numDepart][numDepart];

        csvFile = dataDir + "flow.csv";
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        for(int i=0; i<numDepart; i++) {
            String[] line = br.readLine().split(",");
            for(int j=0; j<numDepart; j++) {
                flow[i][j] = Integer.parseInt(line[j]);
            } // for i
        } // for j
        br.close();*/
    }
//------------------------------
    private void reviseData(int numDummy,double lenDummy) throws Exception {
        /*numDepart = numDepart + numDummy;

        double[] orgLength = length;
        int[][] orgFlow = flow;

        double[] revisedLength = new double[numDepart];
        int i;
        for(i=0; i<revisedLength.length; i++){
            if(i < orgLength.length) revisedLength[i] = orgLength[i];
            else revisedLength[i] = lenDummy;
        }

        int[][] revisedFlow = new int[numDepart][numDepart];
        for(i=0; i<revisedFlow.length; i++){
            for(int j=0; j<revisedFlow[i].length; j++){
                if(i<orgFlow.length & j<orgFlow.length) revisedFlow[i][j] = orgFlow[i][j];
                else revisedFlow[i][j] = 0;
            }
        }

        length = revisedLength;
        flow = revisedFlow;*/
    }
//------------------------------
/*  public int getNumDepart(){return numDepart;}
    public double[] getLength(){return length;}
    public int[][] getFlow(){return flow;}
    public double getTotalLength() {return totalLength;}
*/
}
