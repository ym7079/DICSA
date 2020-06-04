package kr.kau.yym7079.Ver_5;

import java.io.BufferedReader;
import java.io.FileReader;

class ProbDataSet {
    static String dataDir, csvFile;

    public static int numDepart;
    public static double totalLength;
    public static double[] length;
    public static int[][] flow;

    public static void init(String probName, boolean isDummy) throws Exception {
        dataDir = "dataset/Instance_Data/" + probName + "/";
        setDepartLength();
        setFlow();

        if(isDummy){
            int numDummy = RK_Main.getNumDummy();
            double lenDummy = RK_Main.getLengthDummy();
            reviseData(numDummy,lenDummy);
        }
    }

    private static void setDepartLength() throws Exception {
        csvFile = dataDir + "length.csv";
        BufferedReader br = new BufferedReader(new FileReader(csvFile));

        numDepart =Integer.parseInt(br.readLine().split(",")[0]);

        length = new double[numDepart];
        totalLength = 0;
        String[] line = br.readLine().split(",");
        for(int i=0; i<line.length; i++) {
            length[i] = Double.parseDouble(line[i]);
            totalLength += length[i];
        }
        br.close();
    }
    private static void setFlow() throws Exception {
        flow = new int[numDepart][numDepart];

        csvFile = dataDir + "flow.csv";
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        for(int i=0; i<numDepart; i++) {
            String[] line = br.readLine().split(",");
            for(int j=0; j<numDepart; j++) {
                flow[i][j] = Integer.parseInt(line[j]);
            } // for i
        } // for j
        br.close();
    }
    private static void reviseData(int numDummy,double lenDummy) throws Exception {
        numDepart = numDepart + numDummy;

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
        flow = revisedFlow;
    }

    /*public static int getNumDepart(){return numDepart;}
    public static double[] getLength(){return length;}
    public static int[][] getFlow(){return flow;}
    public static double getTotalLength() { return totalLength;}*/
}
