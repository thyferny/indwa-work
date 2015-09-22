package com.alpine.logparser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.pattern.ClassNamePatternConverter;
import org.apache.log4j.pattern.DatePatternConverter;
import org.apache.log4j.pattern.FileLocationPatternConverter;
import org.apache.log4j.pattern.FullLocationPatternConverter;
import org.apache.log4j.pattern.LevelPatternConverter;
import org.apache.log4j.pattern.LineLocationPatternConverter;
import org.apache.log4j.pattern.LineSeparatorPatternConverter;
import org.apache.log4j.pattern.LiteralPatternConverter;
import org.apache.log4j.pattern.LoggerPatternConverter;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.pattern.MessagePatternConverter;
import org.apache.log4j.pattern.MethodLocationPatternConverter;
import org.apache.log4j.pattern.NDCPatternConverter;
import org.apache.log4j.pattern.PatternParser;
import org.apache.log4j.pattern.PropertiesPatternConverter;
import org.apache.log4j.pattern.RelativeTimePatternConverter;
import org.apache.log4j.pattern.SequenceNumberPatternConverter;
import org.apache.log4j.pattern.ThreadPatternConverter;

public class Log4JLogParser implements IAlpineLogParser {
	private final static List<String> log4jKeyWords = new ArrayList<String>();

	private static final String PROP_START = "PROP(";
	private static final String PROP_END = ")";

	private static final String LOGGER = "LOGGER";
	private static final String MESSAGE = "MESSAGE";
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String NDC = "NDC";
	private static final String LEVEL = "LEVEL";
	private static final String THREAD = "THREAD";
	private static final String CLASS = "CLASS";
	private static final String FILE = "FILE";
	private static final String LINE = "LINE";
	private static final String METHOD = "METHOD";
	// all lines other than first line of exception begin with tab followed by
	// 'at' followed by text
	/*
	 	%c Logger, %c{2 } last 2 partial names
		%C Class name (full agony), %C{2 } last 2 partial names
		%d{dd MMM yyyy HH:MM:ss } Date, format see java.text.SimpleDateFormat
		%F File name
		%l Location (caution: compiler-option-dependently)
		%L Line number
		%m user-defined message
		%M Method name
		%p Level
		%r Milliseconds since program start
		%t Threadname
		%x, %X see Doku
		%% individual percentage sign
		Caution: %C, %F, %l, %L, %M slow down program run!
	 */
	
	private static final String REGEXP_DEFAULT_WILDCARD = ".*?";
	private static final String REGEXP_GREEDY_WILDCARD = ".*";
	private static final String PATTERN_WILDCARD = "*";
	private static final String NOSPACE_GROUP = "(\\S*\\s*?)";
	private static final String DEFAULT_GROUP = "(" + REGEXP_DEFAULT_WILDCARD
			+ ")";
	private static final String GREEDY_GROUP = "(" + REGEXP_GREEDY_WILDCARD
			+ ")";
	private static final String MULTIPLE_SPACES_REGEXP = "[ ]+";
	private SimpleDateFormat dateFormat;
	private String timestampFormat = "yyyy-MM-d HH:mm:ss,SSS";
	private String logFormat;
	private String customLevelDefinitions;
	private Pattern regexpPattern;
	private static final Pattern exceptionPattern;
	private static final String VALID_DATEFORMAT_CHARS = "GyMwWDdFEaHkKhmsSzZ";
	private static final String VALID_DATEFORMAT_CHAR_PATTERN = "["
			+ VALID_DATEFORMAT_CHARS + "]";
	private List<String> matchingKeywords;

	private static final String CLF_LOG_FORMAT = "%h %l %u %t \"%r\" %>s %b";
	private static final String CLF_LOG_FORMAT_REGEX = "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+.(\\S+\\s+\\S+).\\s+\"(\\S+)\\s+(\\S+)\\s+(\\S+.\\S+)\"\\s+(\\S+)\\s+(\\S+)$";

	private static final String NCSA_LOG_FORMAT = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";
	private static final String NCSA_LOG_FORMAT_REGEX = "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+.(\\S+\\s+\\S+).\\s+\"(\\S+)\\s+(.+?)\\s+(HTTP[^\"]+)\"\\s+(\\S+)\\s+(\\S+)\\s+\"([^\"]*)\"\\s+\"(.*)\"$";

	private String regexp;
	private String timestampPatternText;

	public static final int MISSING_FILE_RETRY_MILLIS = 10000;
	private static final String EXCEPTION_PATTERN = "^\\s+at.*";
	private final Map<String, Level> customLevelDefinitionMap = new HashMap<String, Level>();
	private boolean isPreDefinedRegex;
	private String conversionPattern;

	private String[] mkw;

	private String[] matchingTypes;
	private static final Map<String, String> preDefinedApacheRegexMap = new HashMap<String, String>();

	private static final String CHARARRAY = "chararray";
	private static final String INT = "int";

	static {
		initLog4jKeyWords();
		exceptionPattern = Pattern.compile(EXCEPTION_PATTERN);
		initPreApacheRegexMap();
	}

	private static void initPreApacheRegexMap() {
		preDefinedApacheRegexMap.put(CLF_LOG_FORMAT, CLF_LOG_FORMAT_REGEX);
		preDefinedApacheRegexMap.put(NCSA_LOG_FORMAT, NCSA_LOG_FORMAT_REGEX);
	}
	
	private static void initLog4jKeyWords() {
		log4jKeyWords.add(TIMESTAMP);
		log4jKeyWords.add(LOGGER);
		log4jKeyWords.add(LEVEL);
		log4jKeyWords.add(THREAD);
		log4jKeyWords.add(CLASS);
		log4jKeyWords.add(FILE);
		log4jKeyWords.add(LINE);
		log4jKeyWords.add(METHOD);
		log4jKeyWords.add(MESSAGE);
		log4jKeyWords.add(NDC);
	}

	public Log4JLogParser(String conversionPattern) {
		initialize(conversionPattern);
	}

	public static String getTimeStampFormat(String patternLayout) {
		int basicIndex = patternLayout.indexOf("%d");
		if (basicIndex < 0) {
			return null;
		}

		int index = patternLayout.indexOf("%d{");
		// %d - default
		if (index < 0) {
			return "yyyy-MM-dd HH:mm:ss,SSS";
		}

		int length = patternLayout.substring(index).indexOf("}");
		String timestampFormat = patternLayout.substring(
				index + "%d{".length(), index + length);
		if (timestampFormat.equals("ABSOLUTE")) {
			return "HH:mm:ss,SSS";
		}
		if (timestampFormat.equals("ISO8601")) {
			return "yyyy-MM-dd HH:mm:ss,SSS";
		}
		if (timestampFormat.equals("DATE")) {
			return "dd MMM yyyy HH:mm:ss,SSS";
		}
		return timestampFormat;
	}

	protected void initialize(String convertionPattern) {

		this.conversionPattern = convertionPattern;

		//if (!preDefinedApacheRegexMap.containsKey(convertionPattern)) {
			this.matchingKeywords = new ArrayList<String>();
			this.timestampFormat = getTimeStampFormat(conversionPattern);
			this.logFormat = getLogFormat(conversionPattern);

			if (timestampFormat != null) {
				dateFormat = new SimpleDateFormat(
						quoteTimeStampChars(timestampFormat));
				timestampPatternText = convertTimestamp();
			}
			// if custom level definitions exist, parse them
			updateCustomLevelDefinitionMap();

			regexp = buildRegexString(logFormat, matchingKeywords, timestampPatternText);
//		} else {
//			regexp = preDefinedApacheRegexMap.get(convertionPattern);
//		}
			
		createPattern();
		buildMatchingKeyWordsAndTypes();
	}

	private void updateCustomLevelDefinitionMap() {
		if (customLevelDefinitions != null) {
			StringTokenizer entryTokenizer = new StringTokenizer(
					customLevelDefinitions, ",");

			customLevelDefinitionMap.clear();
			while (entryTokenizer.hasMoreTokens()) {
				StringTokenizer innerTokenizer = new StringTokenizer(
						entryTokenizer.nextToken(), "=");
				customLevelDefinitionMap.put(innerTokenizer.nextToken(),
						Level.toLevel(innerTokenizer.nextToken()));
			}
		}
	}

	private String quoteTimeStampChars(String input) {
		// put single quotes around text that isn't a supported dateformat char
		StringBuffer result = new StringBuffer();
		// ok to default to false because we also check for index zero below
		boolean lastCharIsDateFormat = false;
		for (int i = 0; i < input.length(); i++) {
			String thisVal = input.substring(i, i + 1);
			boolean thisCharIsDateFormat = VALID_DATEFORMAT_CHARS
					.contains(thisVal);
			// we have encountered a non-dateformat char
			if (!thisCharIsDateFormat && (i == 0 || lastCharIsDateFormat)) {
				result.append("'");
			}
			// we have encountered a dateformat char after previously
			// encountering a non-dateformat char
			if (thisCharIsDateFormat && i > 0 && !lastCharIsDateFormat) {
				result.append("'");
			}
			lastCharIsDateFormat = thisCharIsDateFormat;
			result.append(thisVal);
		}
		// append an end single-quote if we ended with non-dateformat char
		if (!lastCharIsDateFormat) {
			result.append("'");
		}
		return result.toString();
	}

	private String convertTimestamp() {
		// some locales (for example, French) generate timestamp text with
		// characters not included in \w -
		// now using \S (all non-whitespace characters) instead of /w
		String result = timestampFormat.replaceAll(
				VALID_DATEFORMAT_CHAR_PATTERN + "+", "\\\\S+");
		// make sure dots in timestamp are escaped
		result = result.replaceAll(Pattern.quote("."), "\\\\.");
		return result;
	}

	private boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	private String buildRegexString(String logFormat, List<String> matchingKeywords, String timestampPatternText) {
		
		String newPattern = logFormat;
		List<String> buildingKeywords = new ArrayList<String>();
		
		
		// build a list of property names and temporarily replace the property
		// with an empty string,
		// we'll rebuild the pattern later
		int index = 0;
		String current = newPattern;
		List<String> propertyNames = new ArrayList<String>();
		while (index > -1) {
			if (current.indexOf(PROP_START) > -1
					&& current.indexOf(PROP_END) > -1) {
				index = current.indexOf(PROP_START);
				String longPropertyName = current.substring(
						current.indexOf(PROP_START),
						current.indexOf(PROP_END) + 1);
				String shortProp = getShortPropertyName(longPropertyName);
				buildingKeywords.add(shortProp);
				propertyNames.add(longPropertyName);
				current = current.substring(longPropertyName.length() + 1
						+ index);
				newPattern = singleReplace(newPattern, longPropertyName,
						new Integer(buildingKeywords.size() - 1).toString());
			} else {
				// no properties
				index = -1;
			}
		}

		/*
		 * we're using a treemap, so the index will be used as the key to ensure
		 * keywords are ordered correctly
		 * 
		 * examine pattern, adding keywords to an index-based map patterns can
		 * contain only one of these per entry...properties are the only
		 * 'keyword' that can occur multiple times in an entry
		 */
		Map<Integer,String> bm=new HashMap<Integer,String>();
		int bki=0;
		for (String keyword:log4jKeyWords) {
			int index2 = newPattern.indexOf(keyword);
			while (index2 > -1) {
				buildingKeywords.add(keyword);
				newPattern = singleReplace(newPattern, keyword, new Integer(
						buildingKeywords.size() - 1).toString());
				bm.put(bki++, keyword);
				index2 = newPattern.indexOf(keyword);
			}
		}

		String buildingInt = "";

		for (int i = 0; i < newPattern.length(); i++) {
			String thisValue = String.valueOf(newPattern.substring(i, i + 1));
			if (isInteger(thisValue)) {
				buildingInt = buildingInt + thisValue;
			} else {
				if (isInteger(buildingInt)) {
					matchingKeywords.add(bm.get(Integer
							.parseInt(buildingInt)));
				}
				// reset
				buildingInt = "";
			}
		}

		// if the very last value is an int, make sure to add it
		if (isInteger(buildingInt)) {
			matchingKeywords.add(bm.get(Integer
					.parseInt(buildingInt)));
		}

		newPattern = replaceMetaChars(newPattern);

		// compress one or more spaces in the pattern into the [ ]+ regexp
		// (supports padding of level in log files)
		newPattern = newPattern.replaceAll(MULTIPLE_SPACES_REGEXP,MULTIPLE_SPACES_REGEXP);
		newPattern = newPattern.replaceAll(Pattern.quote(PATTERN_WILDCARD),REGEXP_DEFAULT_WILDCARD);
		// use buildingKeywords here to ensure correct order
		
		for (int i = 0; i < buildingKeywords.size(); i++) {
			String keyword = (String) buildingKeywords.get(i);
			// make the final keyword greedy (we're assuming it's the message)
			if (i == (buildingKeywords.size() - 1)) {
				newPattern = singleReplace(newPattern, String.valueOf(i),GREEDY_GROUP);
			} else if (TIMESTAMP.equals(keyword)) {
				newPattern = singleReplace(newPattern, String.valueOf(i), "("+ timestampPatternText + ")");
			} else if (LOGGER.equals(keyword) || LEVEL.equals(keyword)) {
				newPattern = singleReplace(newPattern, String.valueOf(i),NOSPACE_GROUP);
			} else {
				newPattern = singleReplace(newPattern, String.valueOf(i),DEFAULT_GROUP);
			}
		}
		return newPattern;
	}

	private String singleReplace(String inputString, String oldString,
			String newString) {
		int propLength = oldString.length();
		int startPos = inputString.indexOf(oldString);
		if (startPos == -1) {
			return inputString;
		}
		if (startPos == 0) {
			inputString = inputString.substring(propLength);
			inputString = newString + inputString;
		} else {
			inputString = inputString.substring(0, startPos) + newString
					+ inputString.substring(startPos + propLength);
		}
		return inputString;
	}

	private String getShortPropertyName(String longPropertyName) {
		String currentProp = longPropertyName.substring(longPropertyName
				.indexOf(PROP_START));
		String prop = currentProp.substring(0,
				currentProp.indexOf(PROP_END) + 1);
		String shortProp = prop.substring(PROP_START.length(),
				prop.length() - 1);
		return shortProp;
	}

	private String replaceMetaChars(String input) {
		// escape backslash first since that character is used to escape the
		// remaining meta chars
		input = input.replaceAll("\\\\", "\\\\\\");

		// don't escape star - it's used as the wildcard
		input = input.replaceAll(Pattern.quote("]"), "\\\\]");
		input = input.replaceAll(Pattern.quote("["), "\\\\[");
		input = input.replaceAll(Pattern.quote("^"), "\\\\^");
		input = input.replaceAll(Pattern.quote("$"), "\\\\$");
		input = input.replaceAll(Pattern.quote("."), "\\\\.");
		input = input.replaceAll(Pattern.quote("|"), "\\\\|");
		input = input.replaceAll(Pattern.quote("?"), "\\\\?");
		input = input.replaceAll(Pattern.quote("+"), "\\\\+");
		input = input.replaceAll(Pattern.quote("("), "\\\\(");
		input = input.replaceAll(Pattern.quote(")"), "\\\\)");
		input = input.replaceAll(Pattern.quote("-"), "\\\\-");
		input = input.replaceAll(Pattern.quote("{"), "\\\\{");
		input = input.replaceAll(Pattern.quote("}"), "\\\\}");
		input = input.replaceAll(Pattern.quote("#"), "\\\\#");
		return input;
	}

	public static String getLogFormat(String patternLayout) {
		String input = OptionConverter.convertSpecialChars(patternLayout);
		List<LoggingEventPatternConverter> converters = new ArrayList<LoggingEventPatternConverter>();
		List<String> fields = new ArrayList<String>();
		Map converterRegistry = null;
		PatternParser.parse(input, converters, fields, converterRegistry,
				PatternParser.getPatternLayoutRules());
		return getFormatFromConverters(converters);
	}

	private static String getFormatFromConverters(
			List<LoggingEventPatternConverter> converters) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<LoggingEventPatternConverter> iter = converters
				.iterator(); iter.hasNext();) {
			LoggingEventPatternConverter converter = iter.next();
			if (converter instanceof DatePatternConverter) {
				buffer.append("TIMESTAMP");
			} else if (converter instanceof MessagePatternConverter) {
				buffer.append("MESSAGE");
			} else if (converter instanceof LoggerPatternConverter) {
				buffer.append("LOGGER");
			} else if (converter instanceof ClassNamePatternConverter) {
				buffer.append("CLASS");
			} else if (converter instanceof RelativeTimePatternConverter) {
				buffer.append("PROP(RELATIVETIME)");
			} else if (converter instanceof ThreadPatternConverter) {
				buffer.append("THREAD");
			} else if (converter instanceof NDCPatternConverter) {
				buffer.append("NDC");
			} else if (converter instanceof LiteralPatternConverter) {
				LiteralPatternConverter literal = (LiteralPatternConverter) converter;
				// format shouldn't normally take a null, but we're getting a
				// literal, so passing in the buffer will work
				literal.format(null, buffer);
			} else if (converter instanceof SequenceNumberPatternConverter) {
				buffer.append("PROP(log4jid)");
			} else if (converter instanceof LevelPatternConverter) {
				buffer.append("LEVEL");
			} else if (converter instanceof MethodLocationPatternConverter) {
				buffer.append("METHOD");
			} else if (converter instanceof FullLocationPatternConverter) {
				buffer.append("PROP(locationInfo)");
			} else if (converter instanceof LineLocationPatternConverter) {
				buffer.append("LINE");
			} else if (converter instanceof FileLocationPatternConverter) {
				buffer.append("FILE");
			} else if (converter instanceof PropertiesPatternConverter) {
				// PropertiesPatternConverter propertiesConverter =
				// (PropertiesPatternConverter) converter;
				// String option = propertiesConverter.getOption();
				// if (option != null && option.length() > 0) {
				// buffer.append("PROP(" + option + ")");
				// } else {
				buffer.append("PROP(PROPERTIES)");
				// }
			} else if (converter instanceof LineSeparatorPatternConverter) {
				// done
			}
		}
		return buffer.toString();
	}

	protected void createPattern() {
		regexpPattern = Pattern.compile(regexp);
	}


	private Map<Integer, Object> processEvent(MatchResult result) {
		Map<Integer, Object> list = new HashMap<Integer, Object>();
		// group zero is the entire match - process all other groups
		for (int i = 1; i < result.groupCount() + 1; i++) {
			Object value = result.group(i);
			list.put(i - 1, value);
		}
		return list;
	}
	
	@Override
	public String[] processTheLine(String line) {
		if (null==line.trim()) {
			return null;
		}
		
		Matcher matcher = regexpPattern.matcher(line);
		if (!matcher.matches()) {
			return new String[] {};
		}

		Map<Integer, Object> rm = processEvent(matcher.toMatchResult());
		if (null == rm || rm.isEmpty()) {
			return new String[] {};
		}

		String[] rLine = new String[rm.size()];
		int i = 0;
		for (Object r : rm.values()) {
			rLine[i++] = (String) r;
		}
		return rLine;

	}

	public String getTimestampFormat() {
		return timestampFormat;
	}
	
	
	private void buildMatchingKeyWordsAndTypes() {
		mkw=matchingKeywords.toArray(new String[]{});
		int i=0;
		matchingTypes=new String[mkw.length];
		i=0;
		for(String k:mkw){
			if(null==k){
				matchingTypes[i++]=CHARARRAY;
			}else if(k.contains(LINE)){
				matchingTypes[i++]=INT;
			}else{
				matchingTypes[i++]=CHARARRAY;
			}
		}
		
		renameRepeatedKeyWords();

	}
	
	private void renameRepeatedKeyWords() {
		Set<String> keys=new HashSet<String>();
		int j=1;
		for(int i=0;i<mkw.length;i++){
			String key=mkw[i];
			boolean alreadyExist=keys.contains(key);
			if(alreadyExist){
				mkw[i]=key+(j++);
			}
			
			if(!alreadyExist){
				keys.add(key);
			}
		}
	}

	@Override
	public String[] getMatchingKeywords() {
		return mkw;
	};
	
	@Override
	public String[] getMatchingTypes() {
		return matchingTypes;
	}
	

	@Override
	public String getRegexp() {
		return regexp;
	}
	
	@Override
	public void setMatchingKeywords(String[] nmkw) {
		if(null==nmkw||nmkw.length!=mkw.length){
			throw new IllegalArgumentException("Keword size is incostent with existing one");
		}
		mkw=nmkw;
		
	}

	@Override
	public void setMatchingTypes(String[] mt) {
		if(null==mt||mt.length!=matchingTypes.length){
			throw new IllegalArgumentException("Keword size is incostent with existing one");
		}
		matchingTypes=mt;
	}
	

}