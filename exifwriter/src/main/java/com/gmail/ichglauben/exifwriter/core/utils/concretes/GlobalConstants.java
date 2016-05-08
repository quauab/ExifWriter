package com.gmail.ichglauben.exifwriter.core.utils.concretes;

import java.nio.file.FileSystems;

public class GlobalConstants {
	public final static String OS = System.getProperty("os.name").toLowerCase();
	public final static String OS_ARCH = System.getProperty("os.arch").toLowerCase();
	public final static String OS_VERSION = System.getProperty("os.version").toLowerCase();
	public final static String FILESEPARATOR = FileSystems.getDefault().getSeparator();
	public final static String LINESEPARATOR = System.getProperty("line.separator");
	public final static String USRDIR = System.getProperty("user.dir") + FILESEPARATOR;
	public final static String USRHOME = System.getProperty("user.home") + FILESEPARATOR;
}
