package com.gmail.ichglauben.exifwriter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.junit.Test;

import com.gmail.ichglauben.exifreader.core.concretes.ExifReader;
import com.gmail.ichglauben.exifwriter.core.concretes.ExifWriter;
import com.gmail.ichglauben.exifwriter.core.utils.abstracts.CustomClass;
import com.gmail.ichglauben.exifwriter.core.utils.concretes.FileExtensionExtractor;
import com.gmail.ichglauben.exifwriter.core.utils.concretes.FileNameExtractor;
import com.gmail.ichglauben.exifwriter.core.utils.concretes.GlobalConstants;

public class ExifWriterTest extends CustomClass {
	ExifWriter ew = new ExifWriter();
	ExifReader er = new ExifReader();
	
	ClassLoader classLoader = getClass().getClassLoader();
	File img1 = new File(classLoader.getResource("lynx_2.jpg").getFile());
	File img2 = new File(classLoader.getResource("lynx_3.jpg").getFile());
	
	TagInfo artist = TiffConstants.EXIF_TAG_ARTIST;
	TagInfo gps = TiffConstants.EXIF_TAG_GPSINFO;
	
	@Test
	public void testEditTags() throws URISyntaxException {		
		Path jpg1 = img1.toPath();
		Path jpg2 = img2.toPath();
		
		List<String> tags1 = null;
		List<String> tags2 = null;
				
		String name1 = FileNameExtractor.extract(jpg1) + FileExtensionExtractor.extractExtension(jpg1);
		String name2 = FileNameExtractor.extract(jpg1) + FileExtensionExtractor.extractExtension(jpg2);
				
		HashMap<String,TagInfo> meta = new HashMap<String,TagInfo>();
		meta.put("rick walker", artist);
		meta.put("-34.0033,71.2",gps);
		
		assertTrue("Failed to edit " + jpg1.getFileName(),ew.editTags(jpg1, meta));
		assertTrue("Failed to edit " + jpg2.getFileName(), ew.editTags(jpg2, meta));		
		assertTrue("List is null",(null != (tags1 = er.getList(GlobalConstants.USRHOME + name1))));
		assertTrue("List is null",(null != (tags2 = er.getList(GlobalConstants.USRHOME + name2))));
		
		print("\t\t" + jpg1.getFileName() + " metadata");
		for (String tag:tags1) {
			print(tag);
		}
		
		print("\n");		
		
		print("\t\t" + jpg2.getFileName() + " metadata");
		for (String tag:tags2) {
			print(tag);
		}
	}

}
