package kr.kau.yym7079.Ver_4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

class CreatingDataset {
    static public String probNameFolderDir = "dataset/Problems";
    static public String probType;
    static private File probTypeFile;
    static private ArrayList<String> probName_set = new ArrayList<>();

    /*public static void main(String[] args) throws Exception {

        ArrayList<String> probName_set = new ArrayList<>();

        String probType = "AnKeVa";

        String pathName = "dataset/Parameters/"+probType+"/";
        String pathName_alpha = "dataset/Parameters_alpha/"+probType+"/";
        String pathName_fixed = "dataset/Parameters_fixed/"+probType+"/";
        String[] pathName_Set = new String[3];
        pathName_Set[0] = pathName;pathName_Set[1]=pathName_alpha;pathName_Set[2]=pathName_fixed;



//        createProbNameDir();
//        createProbNameSet();
//        readProbNameSet(probName_set,probType); // 각 문제들의 파일명을 읽어들이는 메소드
//        createInstanceDataSet(probName_set);
//        for(String path : pathName_Set){
//            createParameterDir(path);
//            createParameterSet(probName_set,path);
//            createOutputDir(probName_set,path,probType);
//        }
    }*/
    public static void createProbNameDir() throws Exception {
        File probNameFolder = new File(probNameFolderDir);
        if(!probNameFolder.exists()) {
            probNameFolder.mkdirs();
        }
    }
    public static void createDataset(String probType, String probNameData) throws Exception{
        CreatingDataset.probType = probType;
    //Instance 유형에 대한 문제이름 데이터 생성 및 저장
        createProbNameSet(probNameData);
        readProbNameSet(); // 각 문제들의 파일명을 읽어들이는 메소드
    //Instance Data 값 생성
        createInstanceDataSet();
    //parameter Data 값 생성
        String pathName = "dataset/Parameters/"+probType+"/";
        String pathName_alpha = "dataset/Parameters_alpha/"+probType+"/";
        String pathName_fixed = "dataset/Parameters_fixed/"+probType+"/";
        String[] pathName_Set = new String[3];
        pathName_Set[0] = pathName;pathName_Set[1]=pathName_alpha;pathName_Set[2]=pathName_fixed;
        for(String path : pathName_Set){
            createParameterDir(path);
            createParameterSet(path);
            createOutputDir(path);
        }
    }
    private static void createProbNameSet(String probNameData) throws Exception {
        probTypeFile = new File(probNameFolderDir+"/prob"+probType);
        FileWriter fw_probType = new FileWriter(probTypeFile,false);
        fw_probType.write(probNameData);
        fw_probType.close();
//        File probClassicalFile = new File(fileDir + "/probClassical");
//        FileWriter fw_Classical = new FileWriter(probClassicalFile,false);
//        fw_Classical.write("P4,LW5,S5,S8,S8H,S9,S9H,S10,S11,LW11,P15,P17,P18,H_20,H_30");
//        fw_Classical.close();

//        File probCLFile = new File(fileDir+"/probCL");
//        FileWriter fw_CL = new FileWriter(probCLFile,false);
//        fw_CL.write("CL5,CL6,CL7,CL8,CL12,CL15,CL20,CL30");
//        fw_CL.close();

//        File probNugentFile = new File(fileDir+"/probNugent");
//        FileWriter fw_Nugent = new FileWriter(probNugentFile,false);
//        fw_Nugent.write("N25,N30");
//        fw_Nugent.close();

        /*File probNugentFile = new File(fileDir+"/probNugent");
        FileWriter fw_Nugent = new FileWriter(probNugentFile,false);
        fw_Nugent.write("N25,N30");
        fw_Nugent.close();*/

        /*File probAnKeVaFile = new File(fileDir+"/probAnKeVa");
        FileWriter fw_AnKeVa = new FileWriter(probAnKeVaFile,false);
        fw_AnKeVa.write("AnKeVa60");
        fw_AnKeVa.close();*/
    }
    private static void readProbNameSet() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(probTypeFile)); // FileReader(): 파일 읽어오기
        String[] strings = br.readLine().split(",");//br의 내용을 ,에 따라 나눠 strs 문자열 배열에 저장
        Collections.addAll(probName_set, strings);// 문자열 배열strs 을 ArrayList에 그대로 옮기기
    }
    private static void createInstanceDataSet() throws Exception {

        for (String probName : probName_set) {

            String fileDir = "dataset/Instance_Data/"+probName;
            File directory = new File(fileDir);
            if (!directory.exists()){
                directory.mkdirs();
            }
            File flowData = new File(fileDir + "/flow.csv");
            FileWriter fw_flow = new FileWriter(flowData,false);
            fw_flow.close();
            File departLengthData = new File(fileDir + "/length.csv");
            FileWriter fw_length = new FileWriter(departLengthData,false);
//              fw.write("60\n");
//              fw.write("53,7,47,15,38,28,27,2,22,42,57,19,3,1,60,10,11,48,38,58,41,13,17,52,40,16,52,20,8,56,46,4,9,45,30,24,50,39,61,26,7,10,28,35,22,41,46,46,35,7,42,37,36,27,46,58,52,27,55,46");
            fw_length.close();


//            if (probName.equals("P4")){
//                fw.write("13,9,11,23");
//            }else if(probName.equals("LW5")){
//                fw.write("1,3,4,6,7");
//            }else if(probName.equals("S8")){
//                fw.write("2,3,4,5,6,3,7,4");
//            }else if(probName.equals("S8H")){
//                fw.write("5,6,5,4,5,4,6,5");
//            }else if(probName.equals("S9")){
//                fw.write("2,8,9,7,3,4,6,8,9");
//            }else if(probName.equals("S9H")){
//                fw.write("5,7,6,8,7,5,6,8,7");
//            }else if(probName.equals("S10")){
//                fw.write("6,3,9,4,2,6,8,9,6,7");
//            }else if(probName.equals("S11")){
//                fw.write("3,9,3,7,3,7,5,9,6,5,10");
//            }else if(probName.equals("LW11")){
//                fw.write("3,3,3,5,5,6,7,7,9,9,10");
//            }else if(probName.equals("P15")){
//                fw.write("20,3,9,3,7,3,7,5,9,6,5,3,9,3,7");
//            }

        }
    }
    private static void createParameterDir(String pathName) throws Exception {
        String fileDir = pathName;
        File directory = new File(fileDir);
        directory.mkdirs();
    }
    private static void createParameterSet(String pathName) throws Exception {

        for (String probName : probName_set) {
            if (probName.contains("AnKeVa")) {
            // alpha 값을 parameter 값으로 설정하는 경우
                if (pathName.contains("alpha")) {
                    File paramFile = new File(pathName + probName + ".parameter");
                    FileWriter fw = new FileWriter(paramFile, false);
                    fw.write("10,15,30,60,90\n");// number of host nests (or the population size n)
                    fw.write("0.0,0.1,0.2,0.25,0.35\n"); // probability Pa
                    fw.write("0.01,0.05,0.1,0.12");// alpha value(α)
                    fw.close();
                }
            // parameter 값을 고정하는 경우
                else if(pathName.contains("fixed")){
    //                String fileDir = "dataset/Parameters_fixed";
    //                File directory = new File(fileDir);
    //                directory.mkdirs();
                    String dataDir = "dataset/Instance_Data/" + probName + "/";
                    String csvFile = dataDir + "length.csv";
                    BufferedReader br = new BufferedReader(new FileReader(csvFile));
                    int numDepart =Integer.parseInt(br.readLine().split(",")[0]);

                    File paramFile = new File(pathName + probName + ".parameter");
                    FileWriter fw = new FileWriter(paramFile, false);
                    fw.write(""+numDepart+"\n");// number of host nests (or the population size n)
                    fw.write("0.35\n"); // probability Pa
                    fw.write("0.05");// alpha value(α)
                    fw.close();
                }else {
                // alpha 값을 0.01로 고정하는 경우
                        File paramFile = new File(pathName + probName + ".parameter");
                        FileWriter fw = new FileWriter(paramFile, false);
                    fw.write("10,15,30,60,90\n");// number of host nests (or the population size n)
                    fw.write("0.0,0.1,0.2,0.25,0.35"); // probability Pa
                        fw.close();
                }
            }
        }
    }
    private static void createOutputDir(String pathName) throws Exception {// 문제 Instance 에 대한 Output 파일을 출력하기 위한 directory 생성 및 설정
        for (String probName : probName_set) {
            if(pathName.contains("alpha")){
                String outputDir = "output/alpha/"+probType+"/" + probName;
                File outputDirectory = new File(outputDir);
                outputDirectory.mkdirs();
            }
            else if(pathName.contains("fixed")){
                String outputDir = "output/fixed/"+probType+"/" + probName;
                File outputDirectory = new File(outputDir);
                outputDirectory.mkdirs();
            }
            else{
//                if (probName.equals("S5")){
                String outputDir = "output/fixed_alpha/"+probType+"/" + probName;
                File outputDirectory = new File(outputDir);
                outputDirectory.mkdirs();
//                }
            }
        }
    }
}
