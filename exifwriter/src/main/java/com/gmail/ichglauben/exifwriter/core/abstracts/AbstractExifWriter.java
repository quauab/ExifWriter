package com.gmail.ichglauben.exifwriter.core.abstracts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import com.gmail.ichglauben.exifwriter.core.utils.concretes.FileExtensionExtractor;
import com.gmail.ichglauben.exifwriter.core.utils.concretes.GlobalConstants;
import com.gmail.ichglauben.exifwriter.core.utils.concretes.PathValidator;
import com.gmail.ichglauben.filecopier.core.concretes.FileCopier;
import com.gmail.ichglauben.filenameextractor.core.concretes.FileNameExtractor;

public abstract class AbstractExifWriter {
	private final static TagInfo[] tags = new TagInfo[] { TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL,
			TiffConstants.EXIF_TAG_XRESOLUTION, TiffConstants.EXIF_TAG_YRESOLUTION, TiffConstants.EXIF_TAG_ORIENTATION,
			TiffConstants.EXIF_TAG_RESOLUTION_UNIT, TiffConstants.EXIF_TAG_GPSINFO, TiffConstants.EXIF_TAG_ANNOTATIONS,
			TiffConstants.EXIF_TAG_APERTURE_VALUE, TiffConstants.EXIF_TAG_MAKE, TiffConstants.EXIF_TAG_MODEL,
			TiffConstants.EXIF_TAG_CAMERA_SERIAL_NUMBER, TiffConstants.GPS_TAG_GPS_LATITUDE_REF,
			TiffConstants.GPS_TAG_GPS_LATITUDE, TiffConstants.GPS_TAG_GPS_DEST_LATITUDE,
			TiffConstants.GPS_TAG_GPS_ALTITUDE_REF, TiffConstants.GPS_TAG_GPS_ALTITUDE,
			TiffConstants.GPS_TAG_GPS_LONGITUDE_REF, TiffConstants.GPS_TAG_GPS_LONGITUDE,
			TiffConstants.GPS_TAG_GPS_DEST_LONGITUDE, TiffConstants.EXIF_TAG_ARTIST };

	public AbstractExifWriter() {
		super();
	}

	public static boolean removeTags(Path jpegImageFile, ArrayList<TagInfo> removableTags) {
		OutputStream os = null;
		boolean succeeded = false;
		boolean copyDirExists = false;
		
		/**	STEPS:
		 * 			1. remove the removableTags
		 * 			2. save the edited file to the copy directory*/
		
		
		String fileName = jpegImageFile.getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;
		
		// 0. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();
		
		// 1. delete the file if it exist
		if (PathValidator.isAFile(fileCopy))
			try {
				Files.delete(Paths.get(fileCopy));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		// setup the outputSet
		try {
			TiffOutputSet outputSet = null;
			IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile.toFile());
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			if (null != jpegMetadata) {
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}

			if (null == outputSet)
				outputSet = new TiffOutputSet();

			// 1. remove the removableTags
			{
				TiffOutputField tof = null;
				for (TagInfo tagInfo : removableTags) {
					tof = outputSet.findField(tagInfo);
					if (null != tof) {
						outputSet.removeField(tagInfo);
					}
				}
			}

			// 2. now write the edited file to the copy directory
			
			os = new FileOutputStream(fileCopy);
			os = new BufferedOutputStream(os);
			new ExifRewriter().updateExifMetadataLossless(jpegImageFile.toFile(), os, outputSet);
			succeeded = true;
			return succeeded;
		} catch (IOException ioe) {
			return false;
		} catch (ImageReadException ire) {
			return false;
		} catch (ImageWriteException iwe) {
			return false;
		} finally {
			if (os != null)
				try {
					os.close();
					os = null;
				} catch (IOException ioe) {
					return false;
				}
		}
	}

	public static boolean editTags(Path jpegImageFile, HashMap<String, TagInfo> editableTags) {
		OutputStream os = null;
		boolean succeeded = false;
		boolean copyDirExists = false;
		
		/**	STEPS:
		 * 			1. remove the editableTags
		 * 			2. write the new tag values
		 * 			3. save the edited file to the copy directory*/
				
		String fileName = jpegImageFile.getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;
		
		// 0. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();
		
		// 1. delete the file if it exist
		if (PathValidator.isAFile(fileCopy))
			try {
				Files.delete(Paths.get(fileCopy));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		// setup the outputSet		
		try {
			TiffOutputSet outputSet = null;
			IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile.toFile());
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			if (null != jpegMetadata) {
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}

			if (null == outputSet)
				outputSet = new TiffOutputSet();
			
			// 1. first remove the current tags, if they exist ....
			{
				TiffOutputField tof = null;
				for (Entry<String, TagInfo> me : editableTags.entrySet()) {
					TagInfo val = ((TagInfo) me.getValue());
					tof = outputSet.findField(val);
					if (null != tof) {
						outputSet.removeField(val);
					}
				}
			}

			// 2. and write the new tags here
			{
				TiffOutputField tof = null;
				TiffOutputDirectory exifDir = null;

				for (Entry<String, TagInfo> me : editableTags.entrySet()) {
					String key = ((String) me.getKey());
					TagInfo val = ((TagInfo) me.getValue());
					tof = new TiffOutputField(val, TiffFieldTypeConstants.FIELD_TYPE_ASCII, key.length(),
							key.getBytes());
					if (val == TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL) {
						exifDir = outputSet.getOrCreateExifDirectory();
						exifDir.add(tof);
					} else if (val == TiffConstants.EXIF_TAG_GPSINFO) {
						try {
							String[] gps = key.split(",");
							final double longitude = Double.parseDouble(gps[0]);
							final double latitude = Double.parseDouble(gps[1]);
							outputSet.setGPSInDegrees(longitude, latitude);
						} catch (Exception e) {
							continue;
						}
					} else {
						exifDir = outputSet.getOrCreateRootDirectory();
						exifDir.add(tof);
					}
				}
			}

			// 3. now write the edited file to the copy directory
			os = new FileOutputStream(fileCopy);
			os = new BufferedOutputStream(os);
			new ExifRewriter().updateExifMetadataLossless(jpegImageFile.toFile(), os, outputSet);
			succeeded = true;
			return succeeded;
		} catch (IOException ioe) {
			return false;
		} catch (ImageReadException ire) {
			return false;
		} catch (ImageWriteException iwe) {
			return false;
		} finally {
			if (os != null)
				try {
					os.close();
					os = null;
				} catch (IOException ioe) {
					return succeeded;
				}
		}
	}

	public static boolean editAndRemoveTags(Path jpegImageFile, HashMap<String, TagInfo> editableTags,
			ArrayList<TagInfo> removableTags) {
		OutputStream os = null;
		boolean succeeded = false;
		boolean copyDirExists = false;
		
		/**	STEPS:
		 * 			1. remove the editableTags
		 * 			2. remove the removableTags
		 * 			3. write the new tag values
		 * 			4. save the edited file to the copy directory*/
		
		String fileName = jpegImageFile.getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;
		
		// 0. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();
		
		// 1. delete the file if it exist
		if (PathValidator.isAFile(fileCopy))
			try {
				Files.delete(Paths.get(fileCopy));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		// setup the outputSet		
		try {
			TiffOutputSet outputSet = null;
			IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile.toFile());
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			if (null != jpegMetadata) {
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}

			if (null == outputSet)
				outputSet = new TiffOutputSet();
			
			// 1. remove the editableTags
			{
				TiffOutputField tof = null;
				for (Entry<String, TagInfo> me : editableTags.entrySet()) {
					TagInfo val = ((TagInfo) me.getValue());
					tof = outputSet.findField(val);
					if (null != tof) {
						outputSet.removeField(val);
					}
				}
			}
			
			// 2. remove the removableTags
			{
				TiffOutputField tof = null;
				for (TagInfo tagInfo : removableTags) {
					tof = outputSet.findField(tagInfo);
					if (null != tof) {
						outputSet.removeField(tagInfo);
					}
				}
			}

			// 3. write the new tags here
			{
				TiffOutputField tof = null;
				TiffOutputDirectory exifDir = null;

				for (Entry<String, TagInfo> me : editableTags.entrySet()) {
					String key = ((String) me.getKey());
					TagInfo val = ((TagInfo) me.getValue());
					tof = new TiffOutputField(val, TiffFieldTypeConstants.FIELD_TYPE_ASCII, key.length(),
							key.getBytes());
					if (val == TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL) {
						exifDir = outputSet.getOrCreateExifDirectory();
						exifDir.add(tof);
					} else if (val == TiffConstants.EXIF_TAG_GPSINFO) {
						try {
							String[] gps = key.split(",");
							final double longitude = Double.parseDouble(gps[0]);
							final double latitude = Double.parseDouble(gps[1]);
							outputSet.setGPSInDegrees(longitude, latitude);
						} catch (Exception e) {
							continue;
						}
					} else {
						exifDir = outputSet.getOrCreateRootDirectory();
						exifDir.add(tof);
					}
				}
			}

			// 4. write the edited file to the copy directory
			os = new FileOutputStream(fileCopy);
			os = new BufferedOutputStream(os);

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile.toFile(), os, outputSet);
			succeeded = true;
			return succeeded;
		} catch (IOException ioe) {
			return false;
		} catch (ImageReadException ire) {
			return false;
		} catch (ImageWriteException iwe) {
			return false;
		} finally {
			if (os != null)
				try {
					os.close();
					os = null;
				} catch (IOException ioe) {
					return false;
				}
		}
	}

	public static boolean removeAllExifData(File jpegImageFile) {
		OutputStream os = null;
		boolean succeeded = false;
		boolean copyDirExists = false;
		
		/** STEPS:
		 * 		0. create the copy directory if it does not exist
		 * 		1. check and delete the file if it exist
		 * 		2. save the edited file to the copy directory
		 * 		3. remove all metadata from file*/

		String fileName = jpegImageFile.toPath().getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;

		// 0. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();

		// 1. delete the file if it exist
		if (PathValidator.isAFile(fileCopy))
			try {
				Files.delete(Paths.get(fileCopy));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		// 2. save file to edit directory
		try {
			os = new FileOutputStream(fileCopy);
			os = new BufferedOutputStream(os);
			
			// 3. remove all metadata from file
			new ExifRewriter().removeExifMetadata(jpegImageFile, os);
			succeeded = true;
			return succeeded;
		} catch (IOException ioe) {
			return false;
		} catch (ImageReadException ire) {
			return false;
		} catch (ImageWriteException iwe) {
			return false;
		} finally {
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (IOException ioe) {
					return false;
				}
			}
		}
	}

}
