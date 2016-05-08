package com.gmail.ichglauben.exifwriter.core.utils.concretes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	private static String capitalizeFirstChars(String string) {
		if (null != string) {
			String[] words = null;
			if (null != (words = string.split(" "))) {
				if (words.length > 0) {
					switch (words.length) {
					case 1:
						return (string.substring(0, 1).toUpperCase() + string.substring(1));

					default:
						String temp = "";
						for (int i = 0; i < words.length; i++) {
							if (i < (words.length - 1)) {
								temp += capitalizeFirstChar(words[i]) + " ";
							} else {
								temp += capitalizeFirstChar(words[i]);
							}
						}
						return temp;
					}
				}
			} else {
				return capitalizeFirstChar(string);
			}
		}
		return null;
	}

	private static String capitalizeFirstChar(String string) {
		return (string.substring(0, 1).toUpperCase() + string.substring(1));
	}

	private static String capitalizeString(String string) {
		if (null != string) {
			return string.toUpperCase();
		}
		return null;
	}

	private static String shrinkString(String string) {
		if (null != string) {
			return string.toLowerCase();
		}
		return null;
	}

	private static String reverseString(String string) {
		if (null != string) {
			String temp = "";
			for (int i = (string.length() - 1); i > -1; i--) {
				temp += string.charAt(i);
			}
			return temp;
		}
		return null;
	}

	public static String cfc(String string) {
		return capitalizeFirstChars(string);
	}

	public static String cs(String string) {
		return capitalizeString(string);
	}

	public static String ss(String string) {
		return shrinkString(string);
	}

	public static String rs(String string) {
		return reverseString(string);
	}

	public static String extractDomainName(String url) {
		Pattern p = Pattern.compile("^.*([a-z,A-Z{3,}]).*$");
		Pattern p1 = Pattern.compile("^(http:\\/\\/)?(\\w{3,})\\.\\w{3}{1}$");
		Pattern p2 = Pattern.compile(
				"^(http:\\/\\/|https:\\/\\/|www\\.|http:\\/\\/www\\.|https:\\/\\/www\\.)?(\\w{3,})\\.\\w{3}(\\/\\w)*$");
		Matcher m1 = p1.matcher(url);
		Matcher m2 = p2.matcher(url);
		if (m2.find()) {
			return m2.group(2);
		}
		if (m1.find()) {
			return m1.group(2);
		}
		return "__";
	}
}