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
		 * 			1. create the copy directory if it does not exist
		 * 			2. delete the file if it exist
		 * 			3. remove the removableTags
		 * 			4. save the edited file to the copy directory*/
		
		
		String fileName = jpegImageFile.getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;
		
		// 1. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();
		
		// 2. delete the file if it exist
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

			// 3. remove the removableTags
			{
				TiffOutputField tof = null;
				for (TagInfo tagInfo : removableTags) {
					tof = outputSet.findField(tagInfo);
					if (null != tof) {
						outputSet.removeField(tagInfo);
					}
				}
			}

			// 4. now write the edited file to the copy directory
			
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

	public static boolean editTags(Path jpegImageFile, HashMap<TagInfo, String> editableTags) {
		OutputStream os = null;
		boolean succeeded = false;
		boolean copyDirExists = false;
		
		/**	STEPS:
		 * 			1. create the copy directory if it does not exist
		 * 			2. delete the file if it exist
		 * 			3. remove the editableTags
		 * 			4. write the new tag values
		 * 			5. save the edited file to the copy directory*/
				
		String fileName = jpegImageFile.getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;
		
		// 1. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();
		
		// 2. delete the file if it exist
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
			
			// 3. first remove the current tags, if they exist ....
			{
				TiffOutputField tof = null;
				for (Entry<TagInfo, String> me : editableTags.entrySet()) {
					TagInfo key = ((TagInfo) me.getKey());
					tof = outputSet.findField(key);
					if (null != tof) {
						outputSet.removeField(key);
					}
				}
			}

			// 4. and write the new tags here
			{
				TiffOutputField tof = null;
				TiffOutputDirectory exifDir = null;

				for (Entry<TagInfo, String> me : editableTags.entrySet()) {
					String value = ((String) me.getValue());
					TagInfo key = ((TagInfo) me.getKey());
					tof = new TiffOutputField(key, TiffFieldTypeConstants.FIELD_TYPE_ASCII, value.length(),
							value.getBytes());
					if (key == TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL) {
						exifDir = outputSet.getOrCreateExifDirectory();
						exifDir.add(tof);
					} else if (key == TiffConstants.EXIF_TAG_GPSINFO) {
						try {
							String[] gps = value.split(",");
							final Double longitude = Double.parseDouble(gps[0]);
							final Double latitude = Double.parseDouble(gps[1]);
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

			// 5. now write the edited file to the copy directory
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

	public static boolean editAndRemoveTags(Path jpegImageFile, HashMap<TagInfo, String> editableTags,
			ArrayList<TagInfo> removableTags) {
		OutputStream os = null;
		boolean succeeded = false;
		boolean copyDirExists = false;
		
		/**	STEPS:
		 * 			1. create the copy directory if it does not exist
		 * 			2. delete the file if it exist
		 * 			3. remove the editableTags
		 * 			4. remove the removableTags
		 * 			5. write the new tag values
		 * 			6. save the edited file to the copy directory*/
		
		String fileName = jpegImageFile.getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;
		
		// 1. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();
		
		// 2. delete the file if it exist
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
			
			// 3. remove the editableTags
			{
				TiffOutputField tof = null;
				for (Entry<TagInfo, String> me : editableTags.entrySet()) {
					TagInfo key = ((TagInfo) me.getKey());
					tof = outputSet.findField(key);
					if (null != tof) {
						outputSet.removeField(key);
					}
				}
			}
			
			// 4. remove the removableTags
			{
				TiffOutputField tof = null;
				for (TagInfo tagInfo : removableTags) {
					tof = outputSet.findField(tagInfo);
					if (null != tof) {
						outputSet.removeField(tagInfo);
					}
				}
			}

			// 5. write the new tags here
			{
				TiffOutputField tof = null;
				TiffOutputDirectory exifDir = null;

				for (Entry<TagInfo, String> me : editableTags.entrySet()) {
					String value = ((String) me.getValue());
					TagInfo key = ((TagInfo) me.getKey());
					tof = new TiffOutputField(key, TiffFieldTypeConstants.FIELD_TYPE_ASCII, value.length(),
							value.getBytes());
					if (key == TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL) {
						exifDir = outputSet.getOrCreateExifDirectory();
						exifDir.add(tof);
					} else if (key == TiffConstants.EXIF_TAG_GPSINFO) {
						try {
							String[] gps = value.split(",");
							final Double longitude = Double.parseDouble(gps[0]);
							final Double latitude = Double.parseDouble(gps[1]);
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

			// 6. write the edited file to the copy directory
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
		 * 		1. create the copy directory if it does not exist
		 * 		2. check and delete the file if it exist
		 * 		3. save the edited file to the copy directory
		 * 		4. remove all metadata from file*/

		String fileName = jpegImageFile.toPath().getFileName().toString();
		String copyPath = GlobalConstants.USRHOME;
		String copyDestination = copyPath + GlobalConstants.EDITED_JPEGS;
		String fileCopy = copyDestination + fileName;

		// 1. create the copy directory, if it does not exist
		if (!PathValidator.pathExists(copyDestination))
			copyDirExists = new File(copyDestination).mkdir();

		// 2. delete the file if it exist
		if (PathValidator.isAFile(fileCopy))
			try {
				Files.delete(Paths.get(fileCopy));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		// 3. save file to edit directory
		try {
			os = new FileOutputStream(fileCopy);
			os = new BufferedOutputStream(os);
			
			// 4. remove all metadata from file
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
