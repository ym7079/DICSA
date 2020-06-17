package kr.kau.yym7079.Ver_6;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

class OutputWriter {
    private static String instanceName;
    private static String dirName;
    private File resultFile;
    private FileWriter fw;
//생성자/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    OutputWriter() throws Exception {
        reportfw();
    }
    OutputWriter(String probName) throws Exception {
        summaryfw(probName);
    }
    OutputWriter(int HostNestNum, double Pa) throws Exception{
        resultfw(HostNestNum,Pa);
    }
//initial Method////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void setDirName(boolean isClassical,String probType) throws Exception {
        if(isClassical)OutputWriter.dirName = "output/fixed_alpha/";
        else OutputWriter.dirName = "output/fixed_alpha/"+probType+"/";
    }
    private void reportfw() throws Exception {
        File reportFile = new File(dirName+"[Report].out");
        fw = new FileWriter(reportFile, true);

        fw.write("The single-row facility layout problem(SRFLP) with Cuckoo Search Algorithm(CS)\n\n");
        fw.write("Summary of computational results with the fixed_alpha parameter set\n\n");
        fw.write("================================================================================\n");
        fw.write("Problem\tNest Number\tPa\t\tBest OFV\tCPU Time(second)\n");
        fw.flush();
    }
    private void summaryfw(String probName) throws Exception {
        OutputWriter.instanceName = probName;
        if(instanceName.contains("Small"))dirName = "output/fixed_alpha/Small/";
        String dirName = OutputWriter.dirName+probName+"/";

        File summaryFile = new File(dirName+"[Summary].out");
        fw = new FileWriter(summaryFile,true); //false로 설정하면 파일을 새로 만든다. (내용이 초기화 되어짐)
        fw.write(instanceName+" summary\n");
        fw.write("\n");
        fw.flush();
    }
    private void resultfw(int HostNestNum, double Pa) throws Exception {
        String dirName = OutputWriter.dirName+instanceName+"/";
        resultFile = new File(dirName+"HostNestNum"+HostNestNum+"_Pa"+Pa+"_result.out");
        fw = new FileWriter(resultFile,true); //false로 설정하면 파일을 새로 만든다. (내용이 초기화 되어짐)
        fw.write("-- "+instanceName+" result\n");
        writeln_parameters(HostNestNum,Pa);
        writeln_clock();
        fw.flush();
    }
//common Method//////////////////////////////////////////////////////////////////////////////////////////////////////////
    void writeln_parameters(int HostNestNum, double P_a) throws Exception{
        fw.write("--------------------------------------------\n");
        fw.write("HostNestNum = "+HostNestNum+", P_a = "+P_a+"\n");
        fw.write("--------------------------------------------\n");
        fw.flush();
    }
    void writeln_clock() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = format.format(new Date());
        fw.write("-- "+time+" result data\n");
    }
    void close() throws Exception {
        fw.close();
    }
//report Method/////////////////////////////////////////////////////////////////////////////////////////////////////////
    void writeln_report(Solution nest) throws Exception {
    writeln_clock();
    fw.write(instanceName+"\t\t"+nest.HostNestNum+"\t\t\t"+nest.Pa+"\t\t"+nest.OFV+"\t\t"+nest.CPUTime+"\n");
    fw.flush();
}
    void writeln_report(Solution_SR nest) throws Exception {
        writeln_clock();
        fw.write(instanceName+"\t\t"+nest.HostNestNum+"\t\t\t"+nest.Pa+"\t\t"+nest.OFV+"\t\t"+nest.CPUTime+"\n");
        fw.flush();
    }
    void writeln_report(Solution_DR nest) throws Exception {
        writeln_clock();
        fw.write(instanceName+"\t\t"+nest.HostNestNum+"\t\t\t"+nest.Pa+"\t\t"+nest.OFV+"\t\t"+nest.CPUTime+"\n");
        fw.flush();
    }
//summary Method/////////////////////////////////////////////////////////////////////////////////////////////////////////
    void writeln_summary(LinkedList<Solution> Nest_Set, int configNum) throws Exception {
        /*double bestOFV = Collections.min(OFV_Set);
        double AverageOFV = 0.0;
        for (double OFV : OFV_Set){
            AverageOFV += OFV/OFV_Set.size();
        }*/
        Solution bestNest = Collections.min(Nest_Set);
        double bestOFV;
        float AverOFV = 0.0f;
        double CPUTime;
        LinkedList<Double> bestCPUTime_Set = new LinkedList<>();
        double AverTime = 0.0;
        //-----------------------------------------------------------------------------------------------------------------
        bestOFV = bestNest.OFV;
        for (Solution Nest : Nest_Set){
            AverOFV += (float)Nest.OFV/(float)Nest_Set.size();
            AverTime += Nest.CPUTime/(double)Nest_Set.size();
            if(Nest.OFV == bestOFV) bestCPUTime_Set.add(Nest.CPUTime);
        }
        String str = String.format("%.3f",AverOFV);
    //        AverOFV = Math.round(AverOFV*1000.0) / 1000.0;
        AverTime = Math.round(AverTime*1000.0) / 1000.0;
        CPUTime = Collections.min(bestCPUTime_Set);
        RK_Main.bestParamNest.get(configNum).CPUTime = CPUTime;
        //-----------------------------------------------------------------------------------------------------------------
        fw.write("Best OFV : "+bestOFV+"\tCPU Time : "+CPUTime+" seconds (Processing time for the best solution)\n");
        fw.write("Average OFV : "+str+"\n");
        fw.write("Average CPU Time : "+AverTime+"\n");
        fw.flush();
    }
    void writeln_summary_SR(LinkedList<Solution_SR> Nest_Set, int configNum) throws Exception {
        /*double bestOFV = Collections.min(OFV_Set);
        double AverageOFV = 0.0;
        for (double OFV : OFV_Set){
            AverageOFV += OFV/OFV_Set.size();
        }*/
        Solution_SR bestNest = Collections.min(Nest_Set);
        double bestOFV;
        float AverOFV = 0.0f;
        double CPUTime;
        LinkedList<Double> bestCPUTime_Set = new LinkedList<>();
        double AverTime = 0.0;
     //-----------------------------------------------------------------------------------------------------------------
        bestOFV = bestNest.OFV;
        for (Solution_SR Nest : Nest_Set){
            AverOFV += (float)Nest.OFV/(float)Nest_Set.size();
            AverTime += Nest.CPUTime/(double)Nest_Set.size();
            if(Nest.OFV == bestOFV) bestCPUTime_Set.add(Nest.CPUTime);
        }
        String str = String.format("%.3f",AverOFV);
//        AverOFV = Math.round(AverOFV*1000.0) / 1000.0;
        AverTime = Math.round(AverTime*1000.0) / 1000.0;
        CPUTime = Collections.min(bestCPUTime_Set);
        RK_Main.bestParamNest.get(configNum).CPUTime = CPUTime;
     //-----------------------------------------------------------------------------------------------------------------
        fw.write("Best OFV : "+bestOFV+"\tCPU Time : "+CPUTime+" seconds (Processing time for the best solution)\n");
        fw.write("Average OFV : "+str+"\n");
        fw.write("Average CPU Time : "+AverTime+"\n");
        fw.flush();
    }
    void writeln_summary_DR(LinkedList<Solution_DR> Nest_Set, int configNum) throws Exception {
        /*double bestOFV = Collections.min(OFV_Set);
        double AverageOFV = 0.0;
        for (double OFV : OFV_Set){
            AverageOFV += OFV/OFV_Set.size();
        }*/
        Solution_DR bestNest = Collections.min(Nest_Set);
        double bestOFV;
        float AverOFV = 0.0f;
        double CPUTime;
        LinkedList<Double> bestCPUTime_Set = new LinkedList<>();
        double AverTime = 0.0;
        //-----------------------------------------------------------------------------------------------------------------
        bestOFV = bestNest.OFV;
        for (Solution_DR Nest : Nest_Set){
            AverOFV += (float)Nest.OFV/(float)Nest_Set.size();
            AverTime += Nest.CPUTime/(double)Nest_Set.size();
            if(Nest.OFV == bestOFV) bestCPUTime_Set.add(Nest.CPUTime);
        }
        String str = String.format("%.3f",AverOFV);
//        AverOFV = Math.round(AverOFV*1000.0) / 1000.0;
        AverTime = Math.round(AverTime*1000.0) / 1000.0;
        CPUTime = Collections.min(bestCPUTime_Set);
        RK_Main.bestParamNest.get(configNum).CPUTime = CPUTime;
        //-----------------------------------------------------------------------------------------------------------------
        fw.write("Best OFV : "+bestOFV+"\tCPU Time : "+CPUTime+" seconds (Processing time for the best solution)\n");
        fw.write("Average OFV : "+str+"\n");
        fw.write("Average CPU Time : "+AverTime+"\n");
        fw.flush();
    }
//result Method//////////////////////////////////////////////////////////////////////////////////////////////////////////
    void writeln_result(double bestOFV, double time, LinkedList<Integer> departSeq) throws Exception {
        fw.write("OFV: "+bestOFV+"\t/ ");
        float time_f = (float)time;
        String str = String.format("%.3f",time_f);
        fw.write("CPU: "+str+" seconds \t/ ");
        fw.write("department sequence: "+departSeq+"\n");
        fw.flush();
    }
}