package com.gmail.ichglauben.exifwriter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.junit.Test;

import com.gmail.ichglauben.exifreader.core.concretes.ExifReader;
import com.gmail.ichglauben.exifwriter.core.concretes.ExifWriter;
import com.gmail.ichglauben.exifwriter.core.utils.abstracts.CustomClass;
import com.gmail.ichglauben.exifwriter.core.utils.concretes.GlobalConstants;

public class ExifWriterTest extends CustomClass {
	ExifWriter ew = new ExifWriter();
	ExifReader er = new ExifReader();
	
	ClassLoader classLoader = getClass().getClassLoader();
	File img1 = new File(classLoader.getResource("lynx_2.jpg").getFile());
	File img2 = new File(classLoader.getResource("lynx_3.jpg").getFile());
	File img3 = new File(ClassLoader.getSystemResource("_lynx.jpg").getFile());
	File img4 = new File(ClassLoader.getSystemResource("lynx_5.jpg").getFile());
	File img5 = new File(ClassLoader.getSystemResource("lynx_4.jpg").getFile());
	
	TagInfo artist = TiffConstants.EXIF_TAG_ARTIST;
	TagInfo gps = TiffConstants.EXIF_TAG_GPSINFO;
	TagInfo xres = TiffConstants.EXIF_TAG_XRESOLUTION;
	TagInfo yres = TiffConstants.EXIF_TAG_YRESOLUTION;
	TagInfo dateTime = TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL;
	
	@Test
	public void testEditGPSTags() {
		er.read(img4);
		println(img4.toPath().getFileName() + "'s metadata before edit\n");
		er.printResults();
		
		println("\nWill add GPS data to " + img4.toPath().getFileName() + "\n");
		HashMap<TagInfo, String> tags = new HashMap<TagInfo, String>();
		tags.put(gps,"35.1453983,-15.041812");
		
		assertTrue(img4.toPath().getFileName() + " unedited", ew.editTags(img4.toPath(), tags));
		
		print(img4.toPath().getFileName() + "'s new data\n");
		
		er.read(GlobalConstants.USRHOME + GlobalConstants.EDITED_JPEGS + img4.toPath().getFileName());
		er.printResults();
	}
	
	@Test
	public void testRemoveTags() {
		er.read(img3);
		
		println(img3.toPath().getFileName() + "'s current metadata\nWill be removing the x & y resolution data\n");
		er.printResults();
		println("");
		
		ArrayList<TagInfo> tags = new ArrayList<TagInfo>();
		tags.add(xres);
		tags.add(yres);
		
		assertTrue(img3.toPath().getFileName() + " unedited", ew.removeTags(img3.toPath(), tags));
		
		println(img3.toPath().getFileName() + "s metadata after removing the x & y resolution");
		er.read(GlobalConstants.USRHOME + GlobalConstants.EDITED_JPEGS + img3.toPath().getFileName());
		er.printResults();
	}
	
	@Test
	public void testEditTags() {
		er.read(img1);
		println(img1.toPath().getFileName() + "'s metadata before editing\nWill be adding an artist\n");
		er.printResults();
		
		HashMap<TagInfo, String> tags = new HashMap<TagInfo, String>();
		tags.put(artist,"Rick Walker");
		
		assertTrue(img1.toPath().getFileName() + " unedited", ew.editTags(img1.toPath(), tags));
		
		println(img1.toPath().getFileName() + "'s metadata after adding an artist name\n");
		er.read(GlobalConstants.USRHOME + GlobalConstants.EDITED_JPEGS + img1.toPath().getFileName());
		er.printResults();
	}

	@Test
	public void testEditAndRemoveTags() {
		er.read(img5.toPath());
		println(img5.toPath().getFileName() + "'s metadata before editing\nWill change the artist's name" +
		" and remove the date and time\n");
		er.printResults();
		println("");
		
		ArrayList<TagInfo> remove = new ArrayList<TagInfo>();
		remove.add(dateTime);
		
		HashMap<TagInfo, String> edit = new HashMap<TagInfo, String>();
		edit.put(artist,"Anita Bathe");
		
		assertTrue(img5.toPath().getFileName() + " undedted", ew.editAndRemoveTags(img5.toPath(), edit, remove));
		
		er.read(GlobalConstants.USRHOME + GlobalConstants.EDITED_JPEGS + img5.toPath().getFileName());
		println(img5.toPath().getFileName() + "'s metadata after edit and remove\n");
		er.printResults();
	}

	@Test
	public void testRemoveAllExifData() {
		er.read(img2.toPath());
		println(img2.toPath().getFileName() + "'s metadata before editing\n" +
				"Will be removing all exchange information from this file\n");
		er.printResults();
		println("");
		
		assertTrue(img2.toPath().getFileName() + " unedited", ew.removeAllExifData(img2));
		
		println(img2.toPath().getFileName() + "'s metadata after edit\n");
		er.read(GlobalConstants.USRHOME + GlobalConstants.EDITED_JPEGS + img2.toPath().getFileName());
		er.printResults();
	}
}
