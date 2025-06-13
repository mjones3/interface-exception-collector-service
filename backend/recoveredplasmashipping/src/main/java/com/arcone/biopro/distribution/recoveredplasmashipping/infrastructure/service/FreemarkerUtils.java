/*
 * © 2021 ARC-One Solutions, LLC
 */
package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service;


import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.exception.LabelServiceException;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LabelTemplateEntity;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Component
@Slf4j
public class FreemarkerUtils extends Configuration {

    private static final StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

    public FreemarkerUtils() {
        super(VERSION_2_3_30);

        setDefaultEncoding("UTF-8");
        setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        setTemplateLoader(stringTemplateLoader);
    }

    /**
     * Method that generates the ZPL from a freemarker template with the given values
     */
    public String zpl(LabelTemplateEntity template, Map<String, Object> values) {
        return processTemplate(
            template.getType(),
            template.getModificationDate().toEpochSecond(),
            template.getTemplate(),
            values
        );
    }

    private String processTemplate(String templateName,
                                   long version,
                                   String templateContents,
                                   Map<String, Object> dataMap) {
        assert templateName != null;
        if (StringUtils.isBlank(templateContents)) {
            return null;
        }
        var templateSource = stringTemplateLoader.findTemplateSource(templateName);
        if (templateSource == null) {
            log.debug("Init freemarker template: {}", templateName);
            stringTemplateLoader.putTemplate(templateName, templateContents, version);
        } else {
            long ver = stringTemplateLoader.getLastModified(templateSource);
            if (version > ver) {
                log.debug("Update freemarker template: {}", templateName);
                stringTemplateLoader.putTemplate(templateName, templateContents, version);
            }
        }
        return processTemplateByName(templateName, dataMap);
    }

    private String processTemplateByName(String templateName, Map<String, Object> dataMap) {
        var strWriter = new StringWriter();
        try {
            this.getTemplate(templateName).process(dataMap, strWriter);
            strWriter.flush();
        } catch (TemplateException | IOException e) {
            log.error("Error processing freemarker template", e);
            throw new LabelServiceException("error.freemarker.template.process");
        }
        return strWriter.toString();
    }
}
