package com.gmail.ichglauben.exifwriter.core.utils.concretes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathValidator {
	public static boolean pathExists(String filePath) {
		Path path = null;
		if (null != filePath) {
			if (null != (path = Paths.get(filePath))) {
				return (isADirectory(path.toAbsolutePath().toString()) || isAFile(path.toAbsolutePath().toString()));
			}
		}
		return false;
	}
	
	public static boolean isADirectory(String filePath) {
		Path path = null;
		if (null != filePath) {
			if (null != (path = Paths.get(filePath))) {
				return (path.toFile().exists() && path.toFile().isDirectory() && Files.isWritable(path));
			}
		}
		return false;
	}
	
	public static boolean isAFile(String filePath) {
		Path path = null;
		if (null != filePath) {
			if (null != (path = Paths.get(filePath))) {
				return (path.toFile().exists() && path.toFile().isFile());
			}
		}
		return false;
	}
	
	private PathValidator() {}
	
	public String toString() { return "Path (Directory or File) Validator"; }
}
