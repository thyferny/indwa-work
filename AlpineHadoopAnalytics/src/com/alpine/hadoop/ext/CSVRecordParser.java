
package com.alpine.hadoop.ext;
 

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


 
public class CSVRecordParser extends AbstractRecordParser{

    final char separator;

    final char quotechar;

    final char escape;

    final boolean strictQuotes;

    private String pending;
    private boolean inField = false;

    final boolean ignoreLeadingWhiteSpace;

    final boolean ignoreQuotations;

    
    public static final char DEFAULT_SEPARATOR = ',';

    public static final int INITIAL_READ_SIZE = 128;

    
    public static final char DEFAULT_QUOTE_CHARACTER = '"';


    
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';

    
    public static final boolean DEFAULT_STRICT_QUOTES = false;

    
    public static final boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;

    
    public static final boolean DEFAULT_IGNORE_QUOTATIONS = false;

    
    static final char NULL_CHARACTER = '\0';

    
    public CSVRecordParser() {
        this(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    
    public CSVRecordParser(char separator) {
        this(separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }


    
    public CSVRecordParser(char separator, char quotechar) {
        this(separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
    }

    
    public CSVRecordParser(char separator, char quotechar, char escape) {
        this(separator, quotechar, escape, DEFAULT_STRICT_QUOTES);
    }

    
    public CSVRecordParser(char separator, char quotechar, char escape, boolean strictQuotes) {
        this(separator, quotechar, escape, strictQuotes, DEFAULT_IGNORE_LEADING_WHITESPACE);
    }

    
    public CSVRecordParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace) {
        this(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace, DEFAULT_IGNORE_QUOTATIONS);
    }

    
    public CSVRecordParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace,
                     boolean ignoreQuotations) {
        if (anyCharactersAreTheSame(separator, quotechar, escape)) {
            throw new UnsupportedOperationException("The separator, quote, and escape characters must be different!");
        }
        if (separator == NULL_CHARACTER) {
            throw new UnsupportedOperationException("The separator character must be defined!");
        }
        this.separator = separator;
        this.quotechar = quotechar;
        this.escape = escape;
        this.strictQuotes = strictQuotes;
        this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
        this.ignoreQuotations = ignoreQuotations;
    }

    private boolean anyCharactersAreTheSame(char separator, char quotechar, char escape) {
        return isSameCharacter(separator, quotechar) || isSameCharacter(separator, escape) || isSameCharacter(quotechar, escape);
    }

    private boolean isSameCharacter(char c1, char c2) {
        return c1 != NULL_CHARACTER && c1 == c2;
    }

    
    public boolean isPending() {
        return pending != null;
    }

   

    public String[] parseLine(String nextLine) throws IOException {
        return parseLine(nextLine, false);
    }

    
    private String[] parseLine(String nextLine, boolean multi) throws IOException {

        if (!multi && pending != null) {
            pending = null;
        }

        if (nextLine == null) {
            if (pending != null) {
                String s = pending;
                pending = null;
                return new String[]{s};
            } else {
                return null;
            }
        }

        List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
        boolean inQuotes = false;
        if (pending != null) {
            sb.append(pending);
            pending = null;
            inQuotes = !this.ignoreQuotations;//true;
        }
        for (int i = 0; i < nextLine.length(); i++) {

            char c = nextLine.charAt(i);
            if (c == this.escape) {
                if (isNextCharacterEscapable(nextLine, (inQuotes && !ignoreQuotations) || inField, i)) {
                    sb.append(nextLine.charAt(i + 1));
                    i++;
                }
            } else if (c == quotechar) {
                if (isNextCharacterEscapedQuote(nextLine, (inQuotes && !ignoreQuotations) || inField, i)) {
                    sb.append(nextLine.charAt(i + 1));
                    i++;
                } else {
                    inQuotes = !inQuotes;

                    // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                    if (!strictQuotes) {
                        if (i > 2 //not on the beginning of the line
                                && nextLine.charAt(i - 1) != this.separator //not at the beginning of an escape sequence
                                && nextLine.length() > (i + 1) &&
                                nextLine.charAt(i + 1) != this.separator //not at the	end of an escape sequence
                                ) {

                            if (ignoreLeadingWhiteSpace && sb.length() > 0 && isAllWhiteSpace(sb)) {
                                sb = new StringBuilder(INITIAL_READ_SIZE);  //discard white space leading up to quote
                            } else {
                                sb.append(c);
                            }

                        }
                    }
                }
                inField = !inField;
            } else if (c == separator && !(inQuotes && !ignoreQuotations)) {
                tokensOnThisLine.add(sb.toString());
                sb = new StringBuilder(INITIAL_READ_SIZE); // start work on next token
                inField = false;
            } else {
                if (!strictQuotes || (inQuotes && !ignoreQuotations)) {
                    sb.append(c);
                    inField = true;
                }
            }
        }
        // line is done - check status
        if ((inQuotes && !ignoreQuotations)) {
            if (multi) {
                // continuing a quoted section, re-append newline
                sb.append("\n");
                pending = sb.toString();
                sb = null; // this partial content is not to be added to field list yet
            } else {
                throw new IOException("Un-terminated quoted field at end of CSV line");
            }
        }
        if (sb != null) {
            tokensOnThisLine.add(sb.toString());
        }
        return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);

    }

    
    private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
        return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
                && nextLine.length() > (i + 1)  // there is indeed another character to check.
                && nextLine.charAt(i + 1) == quotechar;
    }

    
    protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
        return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
                && nextLine.length() > (i + 1)  // there is indeed another character to check.
                && (nextLine.charAt(i + 1) == quotechar || nextLine.charAt(i + 1) == this.escape);
    }

    
    protected boolean isAllWhiteSpace(CharSequence sb) {
        boolean result = true;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);

            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return result;
    }

	@Override
	public List<String[]> parse(String aRecordContent) throws Exception {
		List<String[]>  result= new ArrayList< String[]>(1) ;
		result.add( parseLine(aRecordContent));
		return result;
	}
}
