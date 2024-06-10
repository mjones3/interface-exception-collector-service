package com.arcone.biopro.distribution.shippingservice.verification.config.scope;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.support.SimpleThreadScope;

import java.util.Objects;

@Slf4j
public class BrowserScope extends SimpleThreadScope {

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        try{
        Object o = super.get(name, objectFactory);
        SessionId sessionId = ((RemoteWebDriver) o).getSessionId();
        if (Objects.isNull(sessionId)) {
            super.remove(name);
            o = super.get(name, objectFactory);
        }
        return o;
        }
        catch (WebDriverException e){
            // There is a known issue with the RemoteWebDriver where the session is lost in the context.
            // This is a workaround to remove the object from the context and create a new one.
            log.error("Session is lost in the context. Creating a new one.");
            super.remove(name);
            return super.get(name, objectFactory);
        }
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
    }
}
