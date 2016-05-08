package com.gmail.ichglauben.exifwriter.core.abstracts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import com.gmail.ichglauben.exifwriter.core.utils.concretes.GlobalConstants;

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

	public static Boolean removeTags(Path imagePath, ArrayList<TagInfo> tagsValuesToRemove) {
		OutputStream os = null;
		boolean succeeded = false;

		try {
			TiffOutputSet outputSet = null;
			IImageMetadata metadata = Sanselan.getMetadata(imagePath.toFile());
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			if (null != jpegMetadata) {
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}

			if (null == outputSet)
				outputSet = new TiffOutputSet();

			{
				TiffOutputField tof = null;
				for (TagInfo tagInfo : tagsValuesToRemove) {
					tof = outputSet.findField(tagInfo);
					if (null != tof) {
						outputSet.removeField(tagInfo);
					}
				}
			}

			String fileName = imagePath.getFileName().toString();
			String copyPath = GlobalConstants.USRHOME;
			String fileCopy = copyPath + fileName;
			boolean dirExists = false;

			if (Paths.get(fileCopy).toFile().exists()) {
				dirExists = new File(copyPath + "image_copy").mkdir();

				if (dirExists) {
					String newDir = copyPath + "image_copy" + GlobalConstants.FILESEPARATOR;
					String newFile = newDir + fileName;
					os = new FileOutputStream(newFile);
					os = new BufferedOutputStream(os);
				}
			} else {
				os = new FileOutputStream(fileCopy);
				os = new BufferedOutputStream(os);
			}

			new ExifRewriter().updateExifMetadataLossless(imagePath.toFile(), os, outputSet);
			succeeded = true;
		} catch (IOException ioe) {
			return succeeded;
		} catch (ImageReadException ire) {
			return succeeded;
		} catch (ImageWriteException iwe) {
			return succeeded;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ioe) {
					return succeeded;
				}
		}
		return succeeded;
	}

	public static boolean editTags(Path jpegImageFile, HashMap<String, TagInfo> editableTags) {
		OutputStream os = null;
		boolean succeeded = false;

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
			// first remove these tags, if they exist ....
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

			// and here's where those tags that removed are replace with new
			// tags
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

			String fileName = jpegImageFile.getFileName().toString();
			String copyPath = GlobalConstants.USRHOME;
			String fileCopy = copyPath + fileName;
			boolean dirExists = false;

			if (Paths.get(fileCopy).toFile().exists()) {
				dirExists = new File(copyPath + "image_copy").mkdir();

				if (dirExists) {
					String newDir = copyPath + "image_copy" + GlobalConstants.FILESEPARATOR;
					String newFile = newDir + fileName;
					os = new FileOutputStream(newFile);
					os = new BufferedOutputStream(os);
				}
			} else {
				os = new FileOutputStream(fileCopy);
				os = new BufferedOutputStream(os);
			}

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile.toFile(), os, outputSet);
			succeeded = true;
		} catch (IOException ioe) {
			return succeeded;
		} catch (ImageReadException ire) {
			return succeeded;
		} catch (ImageWriteException iwe) {
			return succeeded;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ioe) {
					return succeeded;
				}
		}
		return succeeded;
	}

	public static boolean editAndRemoveTags(Path jpegImageFile, HashMap<String, TagInfo> editableTags,
			ArrayList<TagInfo> removableTags) {
		OutputStream os = null;
		boolean succeeded = false;

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

			{
				TiffOutputField tof = null;
				for (TagInfo tagInfo : removableTags) {
					tof = outputSet.findField(tagInfo);
					if (null != tof) {
						outputSet.removeField(tagInfo);
					}
				}
			}

			if (null == outputSet)
				outputSet = new TiffOutputSet();
			// first remove these tags, if they exist ....
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

			// and here's where those tags that removed are replace with new
			// tags
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

			String fileName = jpegImageFile.getFileName().toString();
			String copyPath = GlobalConstants.USRHOME;
			String fileCopy = copyPath + fileName;
			boolean dirExists = false;

			if (Paths.get(fileCopy).toFile().exists()) {
				dirExists = new File(copyPath + "image_copy").mkdir();

				if (dirExists) {
					String newDir = copyPath + "image_copy" + GlobalConstants.FILESEPARATOR;
					String newFile = newDir + fileName;
					os = new FileOutputStream(newFile);
					os = new BufferedOutputStream(os);
				}
			} else {
				os = new FileOutputStream(fileCopy);
				os = new BufferedOutputStream(os);
			}

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile.toFile(), os, outputSet);
			succeeded = true;
		} catch (IOException ioe) {
			return succeeded;
		} catch (ImageReadException ire) {
			return succeeded;
		} catch (ImageWriteException iwe) {
			return succeeded;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ioe) {
					return succeeded;
				}
		}
		return succeeded;
	}

	public static boolean removeAllExifData(File jpegImageFile) {
		OutputStream os = null;
		boolean succeeded = false;

		try {
			String fileName = jpegImageFile.toPath().getFileName().toString();
			String copyPath = GlobalConstants.USRHOME;
			String fileCopy = copyPath + fileName;
			boolean dirExists = false;

			if (Paths.get(fileCopy).toFile().exists()) {
				dirExists = new File(copyPath + "image_copy").mkdir();

				if (dirExists) {
					String newDir = copyPath + "image_copy" + GlobalConstants.FILESEPARATOR;
					String newFile = newDir + fileName;
					os = new FileOutputStream(newFile);
					os = new BufferedOutputStream(os);
				}
			} else {
				os = new FileOutputStream(fileCopy);
				os = new BufferedOutputStream(os);
			}

			new ExifRewriter().removeExifMetadata(jpegImageFile, os);
			succeeded = true;
		} catch (IOException ioe) {
			return succeeded;
		} catch (ImageReadException ire) {
			return succeeded;
		} catch (ImageWriteException iwe) {
			return succeeded;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ioe) {
					return succeeded;
				}
			}
		}
		return succeeded;
	}

}
