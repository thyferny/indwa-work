/*
 * Pig script to generate a histogram with at most 100 bins.
 */

-- might need to specify full jar path
register '/AlpinePig/target/AlpinePig-0.0.1-SNAPSHOT.jar'; 
define histogram com.alpine.pig.udf.HistogrmBinWidth();

A = LOAD 'SampleSalesDataGaussian.csv' USING PigStorage(',') AS (sales:double, region:chararray, team:int);

total = GROUP A ALL;
M = foreach total generate MIN(A.sales) as minimum,  MAX(A.sales) as maximum, 10000.0 as delta, -1 as bins;
H = foreach M generate com.alpine.pig.udf.HistogramBinWidth(*) as delta;

hist = GROUP A BY ROUND(sales / H.$0 );

counts  = FOREACH hist generate group, COUNT(A); 
ordered = ORDER counts BY group; 

-- used to write output to a csv for quick analysis in MS Excel
Store (foreach (group ordered all) generate flatten($1)) into 'UDFOutput' using PigStorage(','); 