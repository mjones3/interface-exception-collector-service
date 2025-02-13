package com.arcone.biopro.distribution.partnerorderprovider.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
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

    public void checkErrorMessage(String message , JSONObject payload) throws JSONException {
        var errorList = payload.getJSONArray("errors");
        var errorFound = false;
        for (int i = 0; i < errorList.length(); i++) {
            var error = errorList.getJSONObject(i);
            if (error.getString("description").equals(message)) {
                errorFound = true;
                break;
            }
        }
        Assert.assertTrue(errorFound);
    }

}
