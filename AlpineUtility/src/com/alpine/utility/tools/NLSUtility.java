package com.alpine.utility.tools;

public class NLSUtility {
	
	private static final Object[] EMPTY_ARGS = new Object[0];
	
	public static String bind(String message, Object binding1, Object binding2) {
		return internalBind(message, null, String.valueOf(binding1), String.valueOf(binding2));
	}
	
	public static String bind(String message, Object binding) {
		return internalBind(message, null, String.valueOf(binding), null);
	}
	
	private static String internalBind(String message, Object[] args, String argZero, String argOne) {
		if (message == null)
			return "No message available."; //$NON-NLS-1$
		if (args == null || args.length == 0)
			args = EMPTY_ARGS;

		int length = message.length();
		//estimate correct size of string buffer to avoid growth
		int bufLen = length + (args.length * 5);
		if (argZero != null)
			bufLen += argZero.length() - 3;
		if (argOne != null)
			bufLen += argOne.length() - 3;
		StringBuffer buffer = new StringBuffer(bufLen < 0 ? 0 : bufLen);
		for (int i = 0; i < length; i++) {
			char c = message.charAt(i);
			switch (c) {
				case '{' :
					int index = message.indexOf('}', i);
					// if we don't have a matching closing brace then...
					if (index == -1) {
						buffer.append(c);
						break;
					}
					i++;
					if (i >= length) {
						buffer.append(c);
						break;
					}
					// look for a substitution
					int number = -1;
					try {
						number = Integer.parseInt(message.substring(i, index));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException();
					}
					if (number == 0 && argZero != null)
						buffer.append(argZero);
					else if (number == 1 && argOne != null)
						buffer.append(argOne);
					else {
						if (number >= args.length || number < 0) {
							buffer.append("<missing argument>"); //$NON-NLS-1$
							i = index;
							break;
						}
						buffer.append(args[number]);
					}
					i = index;
					break;
				case '\'' :
					// if a single quote is the last char on the line then skip it
					int nextIndex = i + 1;
					if (nextIndex >= length) {
						buffer.append(c);
						break;
					}
					char next = message.charAt(nextIndex);
					// if the next char is another single quote then write out one
					if (next == '\'') {
						i++;
						buffer.append(c);
						break;
					}
					// otherwise we want to read until we get to the next single quote
					index = message.indexOf('\'', nextIndex);
					// if there are no more in the string, then skip it
					if (index == -1) {
						buffer.append(c);
						break;
					}
					// otherwise write out the chars inside the quotes
					buffer.append(message.substring(nextIndex, index));
					i = index;
					break;
				default :
					buffer.append(c);
			}
		}
		return buffer.toString();
	}
}
