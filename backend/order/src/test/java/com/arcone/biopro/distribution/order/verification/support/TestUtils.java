package com.arcone.biopro.distribution.order.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

@Component
@Slf4j
public class TestUtils {

    public String getResource(String fileName) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return FileUtils.readFileToString(new File(resource.toURI()));

    }

}
