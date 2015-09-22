package com.alpine.hadoop.utily.conversion;


import java.util.Map;

public class Stringifier<T1,T2> {
    public static final String PairSeperator = "_alp_";

    public String mapToString(Map<T1,T2> m)
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<T1, T2> entry : m.entrySet()) {
            T1 key = entry.getKey();
            T2 value = entry.getValue();
            if (builder.length() == 0){
                builder.append(key.toString()).append(PairSeperator).append(value.toString());
            } else {
                builder.append(PairSeperator).append(key.toString()).append(PairSeperator).append(value.toString());
            }

        }


        return builder.toString();

    }

   public void stringToMap(String s, Map<String,String>m)
   {
       String[] keyvaluepairs = s.split(PairSeperator);

       for (int i=0;i<keyvaluepairs.length;i+=2)
       {
            m.put(keyvaluepairs[i],keyvaluepairs[i+1]);
       }

   }
}
