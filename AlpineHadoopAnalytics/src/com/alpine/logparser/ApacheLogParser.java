package com.alpine.logparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheLogParser implements IAlpineLogParser {
	private static final String DASH = "-";
	private static final String CHARARRAY = "chararray";
	private static final String INT = "int";
	private static final String FLOAT = "float";
	private static final String DEFAULT_COLUMN_NAME = "Column";
	//https://support.net.com/display/VXDOC471/Understanding+Regular+Expressions#UnderstandingRegularExpressions-NonMarkingGrouping%28%3F%3A%29
	//. [ { ( ) * + ? | ^ $ \
	//^	 Matches the start of a line, not including the first character of the line
	//$	 Matches the end of a line, not including the last character of the line
	//( )	 Used to group expressions and to capture a set of characters for use in a back-reference
	//(?: )	 Used to group expressions without capturing them for a back-reference
	/*
	Pattern	 Description
	\d	 Matches a numeric digit (0 to 9)
	\w	 Matches a word character (letters, digits, underscores)
	\s	 Matches a whitespace character (space, tab, line breaks)
	\D	 Matches a non-numeric character (no number 0 to 9)
	\W	 Matches a non-word character (not a letter, digit or underscore)
	\S	 Matches a non-whitespace character (not a space, tab or line break)
	
	Repeaters *,+,?,{}
		The repeater characters ( *, +, ?, and {} ) enable matching of a character, expression or character class that is repeated.
		
		Pattern	 Description
		*	 Match the preceding character or expression zero to unlimited times.
		+	 Match the preceding character or expression one to unlimited times.
		{n}	 Match the preceding character or expression exactly n times
		{n,m}	 Match the preceding character or expression at least n times and at most m times
		{n,}	 Match the preceding character or expression at least n times and unlimited times
		?	 Optionally match the preceding character or expression
		
	Back References \1, \2, \n
		An escape character followed by a digit n, where n is in the range 1-9, matches the same string that was matched by Marked Group. Marked Groups are created with an open and close parenthesis pair ( ).
			
		Pattern	 Description
			\1	 Outputs the content of the first capturing marked group
			\2	 Outputs the content of the second capturing marked group
			\n	 Outputs the content of the n capturing marked group (n must be a number, not the character n)

	
	
	*/
	private static final String REGEX_DATE 						= "\\[([\\w:/]+\\s[+\\-]\\d{4})\\]";
	private static final String REGEX_THREE_DIGIT 				= "(\\d{3})";
	private static final String REGEX_ANYTHING_BETWEEN_QUOTES 	= "\"((?:[^\"]|(?:\\\\\\\"))+)\"";
	private static final String REGEX_ANYTHING_BUT_SPACE 		= "(\\S+)";
	private static final String QUOTE 							= "\"";
	private static final Map<String,String> regexMap;
	private static final Map<String,String> patternNameMap;
	private static final Map<String, String> apacheKeyWords;

	private static final String EMPTY_SPACE = " ";
	private static final String ZERO = "0";
	
	//Instance specific
	private Pattern regexpPattern;
	private String regexp;
	
	

	boolean DEBUG = false;
	private String converstionPattern;
	private Map<Integer,String> conversionKeys;
	private String[] matchingTypes;
	private String[] mkw;
	static{
		regexMap = new HashMap<String,String>();
		patternNameMap= new HashMap<String,String>();
		apacheKeyWords = new HashMap<String, String>();
		
		initRegexMaps();
		initRegexMaps();
		initApacheKeyWords();
	}
	private static void initRegexMaps() {
		regexMap.put("%a",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%A",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%B",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%b",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%c",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%D",REGEX_ANYTHING_BUT_SPACE);
		
		regexMap.put("%f",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%h",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%H",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%I",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%l",REGEX_ANYTHING_BUT_SPACE);
		
		regexMap.put("%m",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%p",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%P",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%q",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%r",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%>s",REGEX_THREE_DIGIT);
		regexMap.put("%t",REGEX_DATE);
		regexMap.put("%T",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%u",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%U",REGEX_ANYTHING_BUT_SPACE);
		
		regexMap.put("%v",REGEX_ANYTHING_BUT_SPACE);
		regexMap.put("%V",REGEX_ANYTHING_BUT_SPACE);
		
		regexMap.put("\\\"%{User-agent}i\\\""	, REGEX_ANYTHING_BETWEEN_QUOTES);
		regexMap.put("\\\"%{Referer}i\\\""		, REGEX_ANYTHING_BETWEEN_QUOTES);
		regexMap.put("\\\"%r\\\""				, REGEX_ANYTHING_BETWEEN_QUOTES);
		
		
		
		
		
		patternNameMap.put("%a", "c_ip");//RemoteIPAddress
		patternNameMap.put("%A", "s_ip");//LocalIPAddress
		patternNameMap.put("%b", "sc_bytes_exludingHTTPHeadersInCLFFormat");//BytesSentExludingHTTPHeadersInCLFFormat
		patternNameMap.put("%B", "sc_bytes_exludingHTTPHeaders");//BytesSentExludingHTTPHeaders
		patternNameMap.put("%c", "connection_status");//ConnectionStatus
		patternNameMap.put("%D", "time_taken");//TimeTakenProcessTheRequestInMillis
		
		patternNameMap.put("%f", "file_name");//FileName
		patternNameMap.put("%h", "remote_host");//RemoteHost
		patternNameMap.put("%H", "the_request_protocol");//TheRequestProtocol
		patternNameMap.put("%I", "current_request_thread_name");//CurrentRequestThreadName

		patternNameMap.put("%l", "remote_log_name");//RemoteLogName
		patternNameMap.put("%m", "cs_method");//TheRequestMethod
		patternNameMap.put("%p", "s_port_canonical");//CanonicalPort
		patternNameMap.put("%P", "processID");//CanonicalPort
		patternNameMap.put("%q", "cs_uri_query");//QueryString
		patternNameMap.put("%r", "firstLineOfRequest");//FirstLineOfRequest
		patternNameMap.put("\"%r\"", "firstLineOfRequest");//FirstLineOfRequest
		patternNameMap.put("%>s", "sc_status");//StatusCode
		patternNameMap.put("%s", "sc_status");//StatusCode
		patternNameMap.put("%S", "userSessionID");//UserSessionID
		patternNameMap.put("%t", "time_in_common_log_format");//TimeInCommonLogFormat
		patternNameMap.put("%T", "time_taken");//TheTimeTakenToServerTheRequest
		patternNameMap.put("%u", "cs_username");//RemoteUser
		patternNameMap.put("%U", "theURLPathRequested");//TheURLPathRequested
		patternNameMap.put("%v", "canonicalServerNameServingTheRequest");//CanonicalServerNameServingTheRequest
		patternNameMap.put("%V", "canonicalServerNameUseCanonicalName");//CanonicalServerNameUseCanonicalName
		
		patternNameMap.put("\\\"%{User-agent}i\\\""			, "cs_user_agent");//UserAgent
		patternNameMap.put("\"%{User-Agent}i\""				, "cs_user_agent");
		
		patternNameMap.put("\\\"%{Referer}i\\\""			, "cs_referrer");//Referrer
		patternNameMap.put("\"%{Referer}i\""				, "cs_referrer");
		patternNameMap.put("\\\"%r\\\""						, "cs_referrer");
		
	}
 
	private static void initApacheKeyWords() {
		apacheKeyWords.put("%a", "Remote IP address");
		apacheKeyWords.put("%A", "Local IP address");
		apacheKeyWords.put("%b",
				"Bytes sent, excluding HTTP headers, or '-' if zero");
		apacheKeyWords.put("%B", "Bytes sent, excluding HTTP headers.");

		apacheKeyWords
				.put("%c",
						"Connection status when response was completed  X = connection aborted before the response completed. + = connection may be kept alive after the response is sent.  - = connection will be closed after the response is sent.");
		// %{FOOBAR}e: The contents of the environment variable FOOBAR
		apacheKeyWords.put("%f", "Filename");
		apacheKeyWords.put("%h",
				"Remote host name (or IP address if resolveHosts is false)");
		// %{Foobar}i: The contents of Foobar: header line(s) in the request
		// sent to the server.
		apacheKeyWords.put("%H", "Request protocol");
		apacheKeyWords.put("%l",
				"Remote logical username from identd (always returns '-')");
		apacheKeyWords.put("%m", "Request method (GET, POST, etc.)");
		// %{Foobar}n: The contents of note "Foobar" from another module.
		// %{Foobar}o: The contents of Foobar: header line(s) in the reply.

		apacheKeyWords.put("%p",
				"Local port on which this request was received");
		apacheKeyWords.put("%P",
				"The process ID of the child that serviced the request");

		apacheKeyWords
				.put("%q",
						"The query string (prepended with a ? if a query string exists,otherwise an empty string)");
		apacheKeyWords.put("%r", "First line of request");
		apacheKeyWords.put("%s",
						" Status.  For requests that got internally redirected, this is the status of the *original* request");
		apacheKeyWords.put("%>s", "Status.  Status code of the response");
		apacheKeyWords.put("%S", "User session ID");
		apacheKeyWords.put("%t", "Date and time, in Common Log Format");
		apacheKeyWords.put("%T",
				"The time taken to serve the request, in seconds");
		// %...{format}t: The time, in the form given by format, which should be
		// in strftime(3) format. (potentially localized)
		apacheKeyWords.put("%u",
				"Remote user that was authenticated (if any), else '-'");
		apacheKeyWords.put("%U",
				"The URL path requested, not including any query string");

		apacheKeyWords.put("%v", "Local server name");
		apacheKeyWords.put("%V",
				"The server name according to the UseCanonicalName setting.");
		apacheKeyWords
				.put("%D", "Time taken to process the request, in millis");
		apacheKeyWords
				.put("%I",
						"current request thread name (can compare later with stacktraces)");

	}
			
	public ApacheLogParser(String conversionPattern) {
		this.converstionPattern=conversionPattern.replace("\'", "");
		initialize();
	}
	private void initialize() {
		buildConversionKeys();
		buildRegex();
		buildMatchingKeyWordsAndTypes();
		regexpPattern = Pattern.compile(regexp,Pattern.CASE_INSENSITIVE 
				//| Pattern.DOTALL
				);
	}

	
	private void buildConversionKeys() {
		String[] alp=converstionPattern.split(EMPTY_SPACE);
		conversionKeys = new TreeMap<Integer,String>();
		for(int i=0;i<alp.length;i++){
			conversionKeys.put(i, alp[i]);
		}
	}

	private void buildRegex() {
		
		StringBuilder sb=new StringBuilder("^");
		boolean first=true;
		for(String p:conversionKeys.values()){
			String regex=regexMap.get(p);
			if(null==regex){
				if(p.startsWith(QUOTE)&&p.endsWith(QUOTE)){
					regex= REGEX_ANYTHING_BETWEEN_QUOTES;
				}else{
					regex = REGEX_ANYTHING_BUT_SPACE;
				}
			}
			 
			sb.append(!first?"\\s+":"").append(regex);
			first=false;
		}
		sb.append("$");
		regexp = sb.toString();
	}
	
	private Map<Integer, Object> processEvent(MatchResult result) {
		Map<Integer, Object> breakDownsMap = new TreeMap<Integer, Object>();
		for (int i = 1; i < result.groupCount() + 1; i++) {
			Object value = result.group(i);
			if(null!=value&&!value.toString().isEmpty()&&value.toString().equals(DASH)){
				if(matchingTypes[i-1].equals(INT)){
					breakDownsMap.put(i - 1, ZERO);
					continue;
				}else if(matchingTypes[i-1].equals(FLOAT)){
					breakDownsMap.put(i - 1, null);
					continue;
				}
			}
			
			breakDownsMap.put(i - 1, value);
			
		}
		return breakDownsMap;
	}
	
	private void buildMatchingKeyWordsAndTypes() {
		mkw=new String[conversionKeys.size()];
		int i=0;
		String defColumnName=DEFAULT_COLUMN_NAME;
		int missed=1;
		for(String key:conversionKeys.values()){
			if(null==patternNameMap.get(key)){
				mkw[i++]=defColumnName+missed++;
			}else{
				mkw[i++]=patternNameMap.get(key);
			}
		}
		
		matchingTypes=new String[mkw.length];
		i=0;
		for(String k:conversionKeys.values()){
			if(null==k){
				matchingTypes[i++]=CHARARRAY;
			}else if(k.contains("%T")||k.contains("%D")){
				matchingTypes[i++]=FLOAT;
			}else if(k.contains("%b")||k.contains("%B")){
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
	public String[] getMatchingTypes() {
		return matchingTypes;
	}
	
	@Override
	public String[] getMatchingKeywords() {
		return mkw;
	}

	
	
	@Override
	public String getRegexp() {
		return regexp;
	}
	@Override
	public String[] processTheLine(String line){ 
		if(null==line||"".equals(line.trim())){
			return new String[]{};
		}
		
		Matcher matcher = regexpPattern.matcher(line);
		if (!matcher.matches()) {
			return new String[] {};
		}
    	
    	Map<Integer, Object> rm = processEvent(matcher.toMatchResult());
    	if(null==rm||rm.isEmpty()){
    		return new String[]{};
    	}
    	
    	String[] rLine=new String[rm.size()];
    	int i=0;
    	for(Object r:rm.values()){
    		rLine[i++]=(String)r;
    	}
    	if(rLine.length!=matchingTypes.length){
    		return new String[]{};
    	}
    	return rLine;
	
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