package kr.kau.yym7079.Ver_5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import static java.lang.System.out;
import static org.apache.commons.math3.special.Gamma.gamma;

class Test {
    private static final double ALPHA = 0.3;
    static final double LAMBDA = 3.0/2.0; // λ
    static final double SIGMA_U = Math.pow(((gamma(1.0+ LAMBDA) * Math.sin((Math.PI* LAMBDA)/2.0)) / (gamma((1.0+ LAMBDA)/2.0) * LAMBDA * Math.pow(2.0,(LAMBDA -1)/2))),(1/ LAMBDA));
    static final double SIGMA_V = 1.0;
    public static void main(String[] args) throws Exception {
        String probNameFolderDir = "dataset/Problems";
        String probTypes[] = {"DRClassical","Am"};
        for (String probType : probTypes) {
            File probTypeFile = new File(probNameFolderDir+"/prob"+probType);
            BufferedReader br = new BufferedReader(new FileReader(probTypeFile)); // FileReader(): 파일 읽어오기
            String[] strings = br.readLine().split(",");

            String INSTANCE_NAME = "Small";
            for (String probName : strings) {
                if (probName.contains(INSTANCE_NAME)) {
                    probType = INSTANCE_NAME;
                }else if (probType == "DRClassical") probType = "";
                String pathName = "dataset/Parameters/"+probType+"/";
                String fileDir = pathName;
                File directory = new File(fileDir);
                if(!directory.exists()) directory.mkdirs();
                File paramFile = new File(pathName + probName + ".parameter");
                if(paramFile.exists()) continue;
                FileWriter fw = new FileWriter(paramFile, false);
                fw.write("10\n");// number of host nests (or the population size n)
                fw.write("0.0,0.2,0.25,0.35\n"); // probability Pa
                fw.close();
            }


        }
        /*int numDepart = 16;
        String probName = "P"+numDepart+"b";
        String fileDir = "dataset/Instance_Data/"+probName;
        File directory = new File(fileDir);
        directory.mkdirs();

        File flowData = new File(fileDir + "/flow.csv");
        FileWriter fw_flow = new FileWriter(flowData,false);
        fw_flow.close();

        File departLengthData = new File(fileDir + "/length.csv");
        FileWriter fw_length = new FileWriter(departLengthData,false);

        fw_length.write(numDepart+"\n");
        fw_length.write("10,5,6,9,5,5,13,11,13,12,12,5,8,7,5,5");
        fw_length.close();*/
      /*for (int j = 11; j < 14; j++) {
            String probName = "Am"+j;
            for (char i = 'a'; i <= 'f'; i++) {
                String fileDir = "dataset/Instance_Data/"+probName+i;
                File directory = new File(fileDir);
                directory.mkdirs();

                File flowData = new File(fileDir + "/flow.csv");
                FileWriter fw_flow = new FileWriter(flowData,false);
                fw_flow.close();

                File departLengthData = new File(fileDir + "/length.csv");
                FileWriter fw_length = new FileWriter(departLengthData,false);

                fw_length.write(j+"\n");
                if (j==12){
                    if(i == 'a') {
                        fw_length.write("20,3,9,3,7,3,7,5,9,6,5,3");
                    }else if (i == 'b'){
                        fw_length.write("9,3,9,3,7,3,7,3,9,6,9,3");
                    }else if (i == 'c'){
                        fw_length.write("5,4,7,8,9,3,11,6,13,5,9,4");
                    }else if (i == 'd'){
                        fw_length.write("7,9,10,7,7,6,10,6,11,10,6,12");
                    }else if (i == 'e'){
                        fw_length.write("7,9,10,7,7,6,10,6,11,10,6,12");
                    }else if (i == 'f'){
                        fw_length.write("10,9,4,7,11,12,5,5,9,8,3,7");
                    }
                }else if(j == 11){
                    if(i == 'a') {
                        fw_length.write("21,9,10,9,19,9,8,5,6,5,7");
                    }else if (i == 'b'){
                        fw_length.write("5,4,5,5,9,9,7,7,5,8,7");
                    }else if (i == 'c'){
                        fw_length.write("5,4,5,5,9,9,7,7,5,8,7");
                    }else if (i == 'd'){
                        fw_length.write("4,6,11,11,5,3,6,9,7,5,12");
                    }else if (i == 'e'){
                        fw_length.write("4,6,11,11,5,3,6,9,7,5,12");
                    }else if (i == 'f'){
                        fw_length.write("9,9,10,5,11,7,5,11,3,3,9");
                    }
                }else{
                    if(i == 'a') {
                        fw_length.write("20,3,9,3,7,3,7,5,9,6,5,3,9");
                    }else if (i == 'b'){
                        fw_length.write("20,5,3,9,3,7,15,7,5,9,6,21,3");
                    }else if (i == 'c'){
                        fw_length.write("20,13,3,18,7,17,15,7,11,9,13,21,3");
                    }else if (i == 'd'){
                        fw_length.write("12,11,11,5,6,11,7,11,5,7,9,10,3");
                    }else if (i == 'e'){
                        fw_length.write("5,10,11,9,3,12,5,11,11,5,8,5,9");
                    }else if (i == 'f'){
                        fw_length.write("8,9,11,10,4,9,7,11,11,10,10,3,6");
                    }
                }

                fw_length.close();
            }
        }*/

    }
    private static double stepValue(){
        double step;
        //==================================================================================================================
        // u,v value 구하기 (u~N(0,sigmaU^2), v~N(0,sigmaV^2))
        // u는 평균이 0, 표준편차가 sigmaU인 정규분포 확률변수
        // v는 평균이 0, 표준편차가 sigmaV인 정규분포 확률변수
        double u = new Random().nextGaussian()* SIGMA_U;
        double v = new Random().nextGaussian()* SIGMA_V;

        // generate stepSize => u~N(0, sigma) / v~N(0, 1)^(1/lambda)
        // stepSize = u / (|v|)^(1/Lambda)
        step = u / Math.pow((Math.abs(v)),(1.0/ LAMBDA));
        //step = new Random().nextGaussian()*(u / Math.pow((Math.abs(v)),(1/lambda))); => 수민 closedLoop 논문 version

        return step;
    }
}
