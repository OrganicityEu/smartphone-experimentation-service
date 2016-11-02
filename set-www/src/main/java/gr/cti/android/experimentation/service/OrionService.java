package gr.cti.android.experimentation.service;

/*-
 * #%L
 * Smartphone Experimentation Web Service
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 CTI - Computer Technology Institute and Press "Diophantus"
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.amaxilatis.orion.OrionClient;
import com.amaxilatis.orion.model.OrionContextElement;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.organicity.entities.handler.attributes.Attribute;
import eu.organicity.entities.handler.entities.SmartphoneDevice;
import eu.organicity.entities.handler.metadata.Datatype;
import eu.organicity.entities.namespace.OrganicityAttributeTypes;
import eu.organicity.entities.namespace.OrganicityDatatypes;
import gr.cti.android.experimentation.model.Experiment;
import gr.cti.android.experimentation.model.Result;
import gr.cti.android.experimentation.repository.ExperimentRepository;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Iterator;

import static org.apache.logging.log4j.LogManager.getLogger;


/**
 * Provides connection to the orion context broker for storing data.
 */
@Service
public class OrionService {

    /**
     * a log4j logger to print messages.
     */
    private static final Logger LOGGER = getLogger(OrionService.class);

    private static final String ORION_SMARTPHONE_EXPERIMENT_ID_FORMAT = "urn:oc:entity:experimenters:%s:%s:%s";

    private OrionClient orionClient;
    private ObjectMapper mapper;

    @Value("${w3w.key}")
    private String w3wApiKey;
    @Value("${orion.url}")
    private String orionUrl;
    @Value("${orion.token}")
    private String orionToken;
    @Value("${orion.service}")
    private String orionService;
    @Value("${orion.servicePath}")
    private String orionServicePath;
//    private What3Words w3w;

    @Autowired
    ExperimentRepository experimentRepository;
    @Autowired
    ExperimentPortalService experimentPortalService;

    @PostConstruct

    public void init() {
        orionClient = new OrionClient(orionUrl, orionToken, orionService, orionServicePath);
        mapper = new ObjectMapper();
//        w3w = new What3Words(w3wApiKey);
    }

    @Async
    public void store(Result newResult, Experiment experiment) {
        LOGGER.info("store-orion:" + newResult.getDeviceId());

        final SmartphoneDevice locationPhoneEntity = new SmartphoneDevice();

        locationPhoneEntity.setTimestamp(new Date());

        if (experiment.getOcExperimentId() == null) {
            experimentPortalService.getOCExperimentId(experiment.getExperimentId());
        }

        experiment = experimentRepository.findById(experiment.getId());
        if (experiment == null || experiment.getUserId() == null || experiment.getOcExperimentId() == null) {
            LOGGER.error("experiment == null");
            return;
        }
        final String uri = String.format(ORION_SMARTPHONE_EXPERIMENT_ID_FORMAT,
                experiment.getUserId(), experiment.getOcExperimentId(), newResult.getDeviceId());
        LOGGER.info("Orion URI: " + uri);

        try {
            LOGGER.info(newResult.getMessage());
            final JSONObject readingList = new JSONObject(newResult.getMessage());

            final Iterator keys = readingList.keys();
            String latitude = null;
            String longitude = null;
            while (keys.hasNext()) {
                final String key = (String) keys.next();
                if (key.contains("Latitude")) {
                    latitude = String.valueOf(readingList.get(key));
                } else if (key.contains("Longitude")) {
                    longitude = String.valueOf(readingList.get(key));
                } else if (key.startsWith(OrganicityAttributeTypes.Types.SOUND_PRESSURE_LEVEL.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.SOUND_PRESSURE_LEVEL, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.TEMPERATURE.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.TEMPERATURE, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.RELATIVE_HUMIDITY.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.RELATIVE_HUMIDITY, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.ATMOSPHERIC_PRESSURE.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.ATMOSPHERIC_PRESSURE, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.CARBON_MONOXIDE.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.CARBON_MONOXIDE, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.METHANE.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.METHANE, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.LPG.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.LPG, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.ILLUMINANCE.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.ILLUMINANCE, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.PARTICLES10.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.PARTICLES10, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.PARTICLES25.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.PARTICLES25, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.BATTERY_LEVEL.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.BATTERY_LEVEL, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                } else if (key.contains(OrganicityAttributeTypes.Types.BATTERY_VOLTAGE.getUrn())) {
                    Attribute a = new Attribute(OrganicityAttributeTypes.Types.BATTERY_VOLTAGE, String.valueOf(readingList.get(key)));
                    Datatype dm = new Datatype(OrganicityDatatypes.DATATYPES.NUMERIC);
                    a.addMetadata(dm);
                    locationPhoneEntity.addAttribute(a);
                }
            }


            if (latitude != null && longitude != null) {

                locationPhoneEntity.setId(uri);

                locationPhoneEntity.setPosition(Double.parseDouble(latitude), Double.parseDouble(longitude));
                locationPhoneEntity.setDatasource(false, "http://set.organicity.eu");
                try {
                    final OrionContextElement entity = locationPhoneEntity.getContextElement();
                    String string = mapper.writeValueAsString(entity);
                    LOGGER.info(string);
                    final String res = orionClient.postContextEntity(uri, entity);
                    LOGGER.info(res.replaceAll("\n", ""));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                LOGGER.warn("latitude:" + latitude + " longitude:" + longitude);
            }
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
