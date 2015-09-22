package com.alpine.utilities;

import java.util.Map;
import java.util.Set;

public class AlpineMapUtility {
	public static StringBuilder printOutTheMap(Map<Object, Object> m) {
		if (null == m) {
			return new StringBuilder("NULL");
		}
		if (0 == m.size()) {
			return new StringBuilder("EmptyMap");
		}
		StringBuilder sb = new StringBuilder();
		Set<Object> keys = m.keySet();
		for (Object k : keys) {
			sb.append(k).append(",").append(m.get(k) + "\n\t");
		}

		return sb;

	}

}
