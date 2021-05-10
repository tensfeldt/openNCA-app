/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.jcr.mimetype;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.io.InputStream;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
// import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.csv.TextAndCSVParser;
import org.apache.tika.sax.BodyContentHandler;

import org.modeshape.common.SystemFailureException;
import org.modeshape.common.annotation.Immutable;
import org.modeshape.common.annotation.ThreadSafe;
import org.modeshape.jcr.Environment;
import org.modeshape.jcr.JcrI18n;

import org.xml.sax.SAXException;


/**
 * {@link MimeTypeDetector} implementation which uses Apache Tika to determine the mimetype of a given binary, based on the 
 * content (binary) header. This involves reading at least the first X bytes from each binary and is more expensive than 
 * {@link NameOnlyDetector}
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 **/
@Immutable
@ThreadSafe
public final class ContentDetector extends TikaMimeTypeDetector {
    
    private DefaultDetector detector;
    private TextAndCSVParser parser;
    
    /**
     * Creates a new content detector
     * 
     * @param environment the {@link Environment} to use for class loading; may not be {@code null}
     */
    public ContentDetector( Environment environment ) {
        super(environment);
    }
    
    @Override
    protected void initDetector( ClassLoader loader ) {
        try {
            // this will also load ModeShape's custom-mimetypes.xml because it's placed in a org.apache.tika.mime package
            this.detector = new DefaultDetector(MimeTypes.getDefaultMimeTypes(loader));
            if (logger.isDebugEnabled()) {
                for (Detector detector : this.detector.getDetectors()) {
                    logger.debug(" - Found TIKA detector: " + detector.getClass().getName());
                }                
            }
            this.parser = new TextAndCSVParser();
        } catch (Throwable t) {
            throw new SystemFailureException(JcrI18n.unableToInitializeMimeTypeDetector.text(t.getMessage()), t);
        }
    }

    @Override
    protected String detect( InputStream inputStream, Metadata metadata ) {
        MediaType detectedMimeType = null;
        try {
            if (inputStream != null) {
                /***
                try (TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {
                    detectedMimeType = detector.detect(tikaInputStream, metadata);
                }
                ***/
                try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                    bis.mark(0x30000000);
                    detectedMimeType = detector.detect(bis, metadata);
                    if (logger.isDebugEnabled()) {
                        logger.debug("detectedMimeType: " + detectedMimeType.toString());
                    }
                    if ((detectedMimeType == null) || ((detectedMimeType != null) && (detectedMimeType.toString().equalsIgnoreCase("text/plain")))) {
                        bis.reset();
                        String mimeType = parse(bis, metadata);
                        if (logger.isDebugEnabled()) {
                            logger.debug("parsedMimeType: " + mimeType);
                        }
                        return mimeType;
                    }
                }                
            } else {
                detectedMimeType = detector.detect(null, metadata);
            } 
        } catch (Exception e) {
            logger.debug(e, "Unable to extract mime-type");
            return null;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("MIME type for '" + metadata.get(Metadata.RESOURCE_NAME_KEY) + "' ==> " + detectedMimeType);
        }
        String returnValue =  detectedMimeType.toString();
        if ((returnValue != null) && returnValue.contains(";")) {
            String[] parts = returnValue.split(";");
            metadata.set("Content-Type", parts[0]);
            returnValue = parts[0];
        }
        return returnValue;
    }

    private String parse(InputStream stream, Metadata metadata) throws IOException, SAXException, TikaException {
        BodyContentHandler handler = new BodyContentHandler(100 * 1024 * 1024);
        parser.parse(stream, handler, metadata);
        String returnValue = metadata.get("Content-Type");
        if ((returnValue != null) && returnValue.contains(";")) {
            String[] parts = returnValue.split(";");
            metadata.set("Content-Type", parts[0]);
            returnValue = parts[0];
        }
        return returnValue;
    }

}
