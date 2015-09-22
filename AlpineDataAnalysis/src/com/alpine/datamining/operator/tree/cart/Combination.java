package com.alpine.datamining.operator.tree.cart;


public class Combination {
	 public static void ifNeedMove(int[] combin, int j) {
		if (combin[0] == 1)
			return;
		else {
			int k = j - firstOne(combin);
			initCombin(combin, k, j);
		}
	}

	 public static void swap(int[] combin, int j) {
		combin[j] = combin[j] + combin[j + 1];
		combin[j + 1] = combin[j] - combin[j + 1];
		combin[j] = combin[j] - combin[j + 1];
	}

	 public static int firstOne(int[] combin) {
		int index = 0;
		for (int i = 0; i < combin.length; i++) {
			if (combin[i] == 1)
				break;
			else
				index++;
		}
		return index;
	}

	 public static  int firstOneZero(int[] combin) {
		int index = 0;
		for (int i = 0; i < combin.length - 1; i++) {
			if (combin[i] == 1 && combin[i + 1] == 0)
				break;
			else
				index++;
		}
		return index;
	}

	 public static  void initCombin(int[] combin, int m, int n) {
		for (int i = 0; i < n; i++) {
			if (i < m) {
				combin[i] = 1;
			} else {
				combin[i] = 0;
			}
		}
	}	
}
