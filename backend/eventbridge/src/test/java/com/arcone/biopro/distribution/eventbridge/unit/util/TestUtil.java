package com.arcone.biopro.distribution.eventbridge.unit.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class TestUtil {

    public static String resource(String fileName) throws Exception {

        ClassLoader classLoader = TestUtil.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return FileUtils.readFileToString(new File(resource.toURI()));

    }
}
