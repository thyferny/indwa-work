-- From below liners we can calculate total number of the raws and if it is required we can count by number 
-- of the raws or percentage
--rows = FOREACH (GROUP pigVariable ALL) GENERATE COUNT($1) as totalCount;
--samplePlotColumnsByCount = SAMPLE pigVariable 1ID/rows.totalCount;
sampleByPercentage1ID = SAMPLE pigVariable YYY;

/*

FILE_1344359527488_0 = load 'hdfs://10.0.0.100:8200/NH/golfnew.csv' USING PigStorage(',')  as (outlook:chararray,temperature:int,humidity:int,wind:chararray,play:chararray);
sampleByPercentage1 = SAMPLE  FILE_1344359527488_0 40.0/100.0;
sampleByPercentage2 = SAMPLE  FILE_1344359527488_0 80.0/100.0;


FILE_1344380999882
/x/FilterByStateNotToBeCA
PigStorage(',')

FILE_1344379677528_0 = load 'hdfs://localhost:8020/x/election92.csv' USING PigStorage(',')  as (County:chararray,Abbreviation:chararray,c4:chararray,c5:chararray,Column5:chararray,Column6:chararray,Column7:chararray,Column8:chararray,Column9:chararray,Column10:chararray,Column11:chararray,Column12:chararray,Column13:chararray,Column14:chararray,Column15:chararray,Column16:chararray,Column17:chararray,Column18:chararray,Column19:chararray);
FILE_1344380999882 = FILTER FILE_1344379677528_0 by  Abbreviation != 'CA' ; 
*/