package com.gmail.ichglauben.exifwriter;

import static org.junit.Assert.assertTrue;

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
import com.gmail.ichglauben.exifwriter.core.utils.concretes.GlobalConstants;

public class ExifWriterTest extends CustomClass {
	ExifWriter ew = new ExifWriter();
	ExifReader er = new ExifReader();
	static String img1 = "K:\\media\\graphics\\jpegs\\cats\\lynxs\\lynx_2.jpg";
	TagInfo artist = TiffConstants.EXIF_TAG_ARTIST;
	TagInfo gps = TiffConstants.EXIF_TAG_GPSINFO;
	
	@Test
	public void testEditTags() throws URISyntaxException {		
		Path jpg1 = Paths.get(img1);
		List<String> tags = null;
				
		assertTrue("list is null",(null != (tags = er.getList(jpg1))));
		
		print(jpg1.getFileName() + " metadata");
		for (String tag:tags) {
			print(tag);
		}
		print("");
		
		HashMap<String,TagInfo> meta = new HashMap<String,TagInfo>();
		meta.put("rick walker", artist);
		meta.put("-34.0033,71.2",gps);
		
		assertTrue("Failed to edit " + jpg1.getFileName(),ew.editTags(jpg1, meta));
		print("Added artist and gps information to " + jpg1.getFileName());
		tags = er.getList(GlobalConstants.USRHOME + "lynx_2.jpg");
		
		print(jpg1.getFileName() + " metadata");
		for (String tag:tags) {
			print(tag);
		}
	}

}
