/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jmx.deployment;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.metadata.BpmPlatformXmlParser;
import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Deployment operation step responsible for parsing and attaching the bpm-platform.xml file.</p>
 * 
 * @author Daniel Meyer
 * @author Christian Lipphardt
 *
 */
public abstract class AbstractParseBpmPlatformXmlStep extends MBeanDeploymentOperationStep {

  protected final Logger LOGGER = Logger.getLogger(getClass().getName());

  public static final String BPM_PLATFORM_XML_FILE = "bpm-platform.xml";

  public static final String BPM_PLATFORM_XML_LOCATION = "bpmPlatformXmlLocation";
  public static final String BPM_PLATFORM_XML_ENVIRONMENT_VARIABLE = "BPM_PLATFORM_XML_LOCATION";
  public static final String BPM_PLATFORM_XML_SYSTEM_PROPERTY = "bpm.platform.xml.location";
  public static final String BPM_PLATFORM_XML_RESOURCE_LOCATION = "META-INF/" + BPM_PLATFORM_XML_FILE;

  public String getName() {
    return "Parsing bpm-platform.xml file";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    URL bpmPlatformXmlSource = getBpmPlatformXmlStream(operationContext);

    if (bpmPlatformXmlSource == null) {
      throw new ProcessEngineException("Unable to find bpm-platform.xml. This file is necessary for deploying the camunda BPM platform");
    }
    
    // parse the bpm platform xml
    BpmPlatformXml bpmPlatformXml = new BpmPlatformXmlParser().createParse()
      .sourceUrl(bpmPlatformXmlSource)
      .execute()
      .getBpmPlatformXml();
    
    // attach to operation context
    operationContext.addAttachment(Attachments.BPM_PLATFORM_XML, bpmPlatformXml);     
    
  }

  protected URL checkValidBpmPlatformXmlResourceLocation(String url) {
    url = autoCompleteUrl(url);

    URL fileLocation = null;

    try {
      fileLocation = checkValidUrlLocation(url);
      if (fileLocation == null) {
        fileLocation = checkValidFileLocation(url);
      }
    } catch (MalformedURLException e) {
      throw new ProcessEngineException("'" + url + "' is not a valid camunda bpm platform configuration resource location.", e);
    }

    return fileLocation;
  }

  protected String autoCompleteUrl(String url) {
    if (url != null) {
      LOGGER.log(Level.FINE, "before: [" + url + "]");

      if (!url.endsWith(BPM_PLATFORM_XML_FILE)) {
        String appender;
        if (url.contains("/")) {
          appender = "/";
        } else {
          appender = "\\";
        }

        if (!(url.endsWith("/") || url.endsWith("\\\\"))) {
          url += appender;
        }

        url += BPM_PLATFORM_XML_FILE;
      }

      LOGGER.log(Level.FINE, "after: [" + url + "]");
    }

    return url;
  }

  protected URL checkValidUrlLocation(String url) throws MalformedURLException {
    if (url == null || url.isEmpty()) {
      return null;
    }

    Pattern urlPattern = Pattern.compile("^(https?://).*/bpm-platform\\.xml$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Matcher urlMatcher = urlPattern.matcher(url);
    if (urlMatcher.matches()) {
      return new URL(url);
    }

    return null;
  }

  protected URL checkValidFileLocation(String url) throws MalformedURLException {
    if (url == null || url.isEmpty()) {
      return null;
    }

    Pattern filePattern = Pattern.compile("^(/|[A-z]://?|[A-z]:\\\\).*[/|\\\\]bpm-platform\\.xml$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Matcher fileMatcher = filePattern.matcher(url);
    if (fileMatcher.matches()) {
      File configurationLocation = new File(url);

      if (configurationLocation.isAbsolute() && configurationLocation.exists()) {
        return configurationLocation.toURI().toURL();
      }
    }

    return null;
  }

  protected URL lookupBpmPlatformXmlLocationFromJndi() {
    String jndi = "java:comp/env/" + BPM_PLATFORM_XML_LOCATION;

    try {
      String bpmPlatformXmlLocation = InitialContext.doLookup(jndi);

      URL fileLocation = checkValidBpmPlatformXmlResourceLocation(bpmPlatformXmlLocation);

      if (fileLocation != null) {
        LOGGER.log(Level.INFO, "Found camunda bpm platform configuration in JNDI [" + jndi + "] at " + fileLocation.toString());
      }

      return fileLocation;
    } catch (NamingException e) {
      LOGGER.log(Level.FINE, "Failed to look up camunda bpm platform configuration in JNDI [" + jndi + "].", e);
    }

    return null;
  }

  protected URL lookupBpmPlatformXmlLocationFromEnvironmentVariable() {
    String bpmPlatformXmlLocation = System.getenv(BPM_PLATFORM_XML_ENVIRONMENT_VARIABLE);
    String logStatement = "environment variable [" + BPM_PLATFORM_XML_ENVIRONMENT_VARIABLE + "]";

    if (bpmPlatformXmlLocation == null) {
      bpmPlatformXmlLocation = System.getProperty(BPM_PLATFORM_XML_SYSTEM_PROPERTY);
      logStatement = "system property [" + BPM_PLATFORM_XML_SYSTEM_PROPERTY + "]";
    }

    URL fileLocation = checkValidBpmPlatformXmlResourceLocation(bpmPlatformXmlLocation);

    if (fileLocation != null) {
      LOGGER.log(Level.INFO, "Found camunda bpm platform configuration through " + logStatement + " at " + fileLocation.toString());
    }

    return fileLocation;
  }

  protected URL lookupBpmPlatformXmlFromClassPath(String resourceLocation) {
    URL fileLocation = ClassLoaderUtil.getClassloader(getClass()).getResource(resourceLocation);

    if (fileLocation != null) {
      LOGGER.log(Level.INFO, "Found camunda bpm platform configuration in classpath [" + resourceLocation + "] at " + fileLocation.toString());
    }

    return fileLocation;
  }

  protected URL lookupBpmPlatformXmlFromClassPath() {
    return lookupBpmPlatformXmlFromClassPath(BPM_PLATFORM_XML_RESOURCE_LOCATION);
  }

  protected abstract URL getBpmPlatformXmlStream(MBeanDeploymentOperation operationContext);

}
