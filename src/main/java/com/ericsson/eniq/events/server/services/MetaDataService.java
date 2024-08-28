package com.ericsson.eniq.events.server.services;

/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

/** 
* @author edeccox
* @author estepdu
* @author eromsza
* 
* @since Mar 2010
*/
import java.io.IOException;
import javax.ejb.Local;

/**
 * This will return the metadata for the main ENIQ Events GUI.
 * The UI requires the metadata to be in JSON format.
 * 
 * @author estepdu
 *
 */
@Local
public interface MetaDataService {

    /**
     * Get meta data for Packet Switched and/or Circuit Switched UI (MSS)
     * @throws IOException
     */
    String getUIMetaData() throws IOException;
}
