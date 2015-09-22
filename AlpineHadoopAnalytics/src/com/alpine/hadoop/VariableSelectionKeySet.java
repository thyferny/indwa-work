package com.alpine.hadoop;

public interface VariableSelectionKeySet extends AlpineHadoopConfKeySet{
    //input
    public static String dependent="alpine.varsel.dependent";
    public static String columns="alpine.varsel.columns";
    public static String interactionItems="alpine.varsel.interactionItems";

    //alpha beta job
    public static String alpha="alpine.varsel.alpha";
    public static String beta="alpine.varsel.beta";
    public static String dependent_avg="alpine.varsel.dependent.avg";

    //r2 job
    public static String r2="alpine.varsel.r2";

}