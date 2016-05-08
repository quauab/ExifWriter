package com.gmail.ichglauben.exifwriter.core.utils.concretes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileNameExtractor {
	public static String extract(String fileName) {
		if (null != fileName && fileName.length() > 0) {
			if (PathValidator.isAFile(fileName)) {
				String name = Paths.get(fileName).getFileName().toString();
				int index = name.lastIndexOf(".");
				if (index != -1) {
					return name.substring(0, index);
				}
			}
		}
		return null;
	}

	public static String extract(File fileName) {
		if (null != fileName && fileName.length() > 0) {
			if (PathValidator.isAFile(fileName.toPath().toAbsolutePath().toString())) {
				String name = fileName.toPath().getFileName().toString();
				int index = name.lastIndexOf(".");
				if (index != -1) {
					return name.substring(0, index);
				}
			}
		}
		return null;
	}

	public static String extract(Path fileName) {
		if (null != fileName) {
			if (PathValidator.isAFile(fileName.toAbsolutePath().toString())) {
				String name = fileName.getFileName().toString();
				int index = name.lastIndexOf(".");
				if (index != -1) {
					return name.substring(0, index);
				}
			}
		}
		return null;
	}
}
