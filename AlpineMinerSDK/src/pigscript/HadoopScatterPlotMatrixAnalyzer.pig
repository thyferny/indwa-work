-- This is the template pig script to generate for given column pairs the values to calculate correlation among 
-- pair of columns
aScatterPlotColumns = FOREACH pigVariable generate 1ColumnName1 as col1ColumnName1X,2ColumnName2 as col2ColumnName2Y;

1ColumnName1XColumName2YCalculation = FOREACH aScatterPlotColumns generate \
					col1ColumnName1X as columnX, \
					col2ColumnName2Y as columnY, \
					(col1ColumnName1X*col1ColumnName1X) as sqrValueX, \
					(col2ColumnName2Y*col2ColumnName2Y) as sqrValueY, \
					(col2ColumnName2Y*col1ColumnName1X) as colXY;
					
grpdCorr1ColumnName1X2ColumnName2YCalculation = GROUP 1ColumnName1XColumName2YCalculation ALL;

summationOf1ColumnName1X2ColumnName2YCorr = FOREACH grpdCorr1ColumnName1X2ColumnName2YCalculation generate \
	COUNT(1ColumnName1XColumName2YCalculation.sqrValueX) as countcol1ColumnName1X, \
	SUM(1ColumnName1XColumName2YCalculation.sqrValueX) as sumSqr1ColumnName1X, \
	SUM(1ColumnName1XColumName2YCalculation.sqrValueY) as sumSqr2ColumnName2Y, \
	SUM(1ColumnName1XColumName2YCalculation.colXY) as 1ColumnName1X2ColumnName2Y,\
	SUM(1ColumnName1XColumName2YCalculation.columnX) as sumX,\
	SUM(1ColumnName1XColumName2YCalculation.columnY) as sumY;
ordered1ColumnName1X= ORDER aScatterPlotColumns BY col1ColumnName1X;

grpg1ColumnName1X2ColumnName2Y = GROUP ordered1ColumnName1X ALL;
1ColumnName1X2ColumnName2YWithRowNumber = FOREACH grpg1ColumnName1X2ColumnName2Y \
	generate PigRowNumber(grpg1ColumnName1X2ColumnName2Y.ordered1ColumnName1X) as withrownumberColumnName1X2ColumnName2Y;
flat1ColumnName1X2ColumnName2WithRowNumber = FOREACH 1ColumnName1X2ColumnName2YWithRowNumber  \
	GENERATE flatten(withrownumberColumnName1X2ColumnName2Y);
filtered1ColumnName1X2ColumnName2Y = filter flat1ColumnName1X2ColumnName2WithRowNumber \
	 by ((withrownumberColumnName1X2ColumnName2Y::alpineid)*1MAXAllowedRows%summationOf1ColumnName1X2ColumnName2YCorr.countcol1ColumnName1X)<1MAXAllowedRows;

/*
-- Sample generated code
REGISTER lib/AlpineUtility.jar;
file204322302 = load 'hdfs://localhost:8020/x/golfnew.csv' USING PigStorage(',')  as (Column1:chararray,Humidity:int,Tempature:int,Column4:chararray,Column5:chararray);
aScatterPlotColumns = FOREACH file204322302 generate Humidity as colHumidityX,Tempature as colTempatureY;
HumidityXColumName2YCalculation = FOREACH aScatterPlotColumns generate colHumidityX as columnX,colTempatureY as columnY, (colHumidityX*colHumidityX) as sqrValueX, (colTempatureY*colTempatureY) as sqrValueY, (colTempatureY*colHumidityX) as colXY;
grpdCorrHumidityXTempatureYCalculation = GROUP HumidityXColumName2YCalculation ALL;
summationOfHumidityXTempatureYCorr = FOREACH grpdCorrHumidityXTempatureYCalculation generate COUNT(HumidityXColumName2YCalculation.sqrValueX) as countcolHumidityX, SUM(HumidityXColumName2YCalculation.sqrValueX) as sumSqrHumidityX, SUM(HumidityXColumName2YCalculation.sqrValueY) as sumSqrTempatureY, SUM(HumidityXColumName2YCalculation.colXY) as HumidityXTempatureY,SUM(HumidityXColumName2YCalculation.columnX) as sumX,SUM(HumidityXColumName2YCalculation.columnY) as sumY;
orderedHumidityX= ORDER aScatterPlotColumns BY colHumidityX;
grpgHumidityXTempatureY = GROUP orderedHumidityX ALL;
HumidityXTempatureYWithRowNumber = FOREACH grpgHumidityXTempatureY generate org.apache.pig.builtin.PigRowNumber(grpgHumidityXTempatureY.orderedHumidityX) as withrownumberColumnName1XTempatureY;
flatHumidityXTempatureWithRowNumber = FOREACH HumidityXTempatureYWithRowNumber  GENERATE flatten(withrownumberColumnName1XTempatureY);
filteredHumidityXTempatureY = filter flatHumidityXTempatureWithRowNumber  by withrownumberColumnName1XTempatureY::alpineid%(FLOOR(summationOfHumidityXTempatureYCorr.countcolHumidityX/200))==0;
*/