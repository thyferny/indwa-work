package com.alpine.hadoop.util;

import java.util.ArrayList;

/**
 *
 * @author adehghani (original version was borrowed from
 * http://weblogs.java.net/)
 */

public class KnuthMorrisPratt {

    public static int[] prekmp(String pattern) {
        int[] next = new int[pattern.length()];
        int i = 0, j = -1;
        next[0] = -1;
        while (i < pattern.length() - 1) {
            while (j >= 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = next[j];
            }
            i++;
            j++;
            next[i] = j;
        }
        return next;
    }

    /**
     * Returns an array that contains the positions of occurrences of
     * pattern in text up to the occurrence index
     *
     * @param text
     * @param pattern
     * @param occurrence_indx
     * @return
     */
    public static ArrayList<Integer> kmp(String text, String pattern, int occurrence_indx) {
        int[] next = prekmp(pattern);
        ArrayList pos = new ArrayList(); // positions of occurrences
        int i = 0;
        int indx = 1;
        int j = 0;
        while (i < text.length()) {
            while (j >= 0 && text.charAt(i) != pattern.charAt(j)) {
                j = next[j];
            }
            i++;
            j++;
            if (j == pattern.length()) {
                {
                    if (indx == occurrence_indx) {
                        pos.add(i - pattern.length());
                        return pos;
                    } else {
                        pos.add(i - pattern.length());
                        indx++;
                        j = 0;
                    }
                }
            }
        }
        return null;
    }
}