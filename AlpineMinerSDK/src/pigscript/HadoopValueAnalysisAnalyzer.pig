-- For alpine pig templates you may add comments with -- in the beginning of the line
-- Do not use Tabs anywhere but in the beginning of the file
--aColumnNameColumn  = FOREACH pigVariable generate ColumnName as colColumnName;
--gGroupedColumnName = GROUP aColumnNameColumn  ALL;
--aggregatedColumnName = FOREACH gGroupedColumnName{ \	
--	distinctColumnName = DISTINCT aColumnNameColumn.colColumnName; \
--	generate MAX(aColumnNameColumn.colColumnName) as maxColumnName, \
--	MIN(aColumnNameColumn.colColumnName) as minColumnName, \
--	AVG(aColumnNameColumn.colColumnName) as avgColumnName, \
--	COUNT(aColumnNameColumn.colColumnName) as countColumnName,\
--	COUNT(distinctColumnName) as distColumnName,\
--	AlpineNegativePositiveZeroNull(aColumnNameColumn.colColumnName) as alpineAvgs;\
--	};
--deviationIndividualsColumnNames  = FOREACH aColumnNameColumn generate ((colColumnName-aggregatedColumnName.avgColumnName)*(colColumnName-aggregatedColumnName.avgColumnName)/aggregatedColumnName.countColumnName) as sqrValue;
--grpdDeviationsColumnName =  GROUP deviationIndividualsColumnNames  ALL;
--deviationColumnName = foreach grpdDeviationsColumnName generate SQRT(SUM(deviationIndividualsColumnNames.sqrValue));


--Make sure not to add ; to the end of next line
projectedColumnsForPigVariableName  	= FOREACH PigVariableName generate 
-- we will add "ColumnName as ColumnNameXi" for each column and will also add ";" to the end

gropuedProjectionsForPigVariableName   	= GROUP projectedColumnsForPigVariableName  ALL;

aggregatedProjectionsForPigVariableName 	 	= FOREACH gropuedProjections{									\n
		distinctColumnNameXi 	= DISTINCT projectedColumnsForPigVariableName.ColumnNameXi; 					\n 
		zeroColumnNameXi 		= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi == 0; 		\n
		positiveColumnNameXi 	= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi <  0; 		\n
		negativeColumnNameXi 	= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi >  0; 		\n
		nullColumnNameXi 		= FILTER projectedColumnsForPigVariableName 		BY ColumnNameXi IS NULL ; 	\n 
																												\n
		GENERATE 																								\n 
																												\n
		AVG(projectedColumnsForPigVariableName.ColumnNameXi)			as avgColumnNameXi, 					\n
		COUNT(projectedColumnsForPigVariableName.ColumnNameXi) 			as countColumnNameXi,					\n
		MAX(projectedColumnsForPigVariableName.ColumnNameXi) 			as maxColumnNameXi, 					\n
		MIN(projectedColumnsForPigVariableName.ColumnNameXi) 			as minColumnNameXi,  					\n
		COUNT(positiveColumnNameXi) 									as biggerColumnNameXi,					\n
		COUNT(negativeColumnNameXi) 									as smallerColumnNameXi,					\n
		COUNT(zeroColumnNameXi) 										as zerosColumnNameXi,					\n
		COUNT(distinctColumnNameXi) 									as distinctColumnNameXi;				\n
																												\n
	};	