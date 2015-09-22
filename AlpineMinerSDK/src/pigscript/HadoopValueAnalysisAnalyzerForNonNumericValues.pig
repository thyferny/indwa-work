-- For alpine pig templates you may add comments with -- in the beginning of the line
-- Do not use Tabs anywhere but in the beginning of the file
aColumnNameColumn  = FOREACH pigVariable generate ColumnName as colColumnName;
gGroupedColumnName = GROUP aColumnNameColumn  ALL;
charAggregatedColumnName = FOREACH gGroupedColumnName{ \	
	distinctColumnName = DISTINCT aColumnNameColumn.colColumnName; \
	generate COUNT(aColumnNameColumn.colColumnName) as countColumnName,\
	COUNT(distinctColumnName) as distColumnName,\
	COUNT_STAR(aColumnNameColumn.colColumnName) as countAllColumnName;\
	};