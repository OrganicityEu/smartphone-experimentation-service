package gr.cti.android.experimentation.client;

/*-
 * #%L
 * Smartphone Experimentation Android Client
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

import eu.organicity.client.OrganicityServiceBaseClient;
import gr.cti.android.experimentation.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;

public class WebServiceAndroidClient extends OrganicityServiceBaseClient {

    private static final String BASE_URL = "https://api.smartphone-experimentation.eu/v1/";
    private static final String ACCOUNTS_TOKEN_ENDPOINT = "https://accounts.organicity.eu/realms/organicity/protocol/openid-connect/token";
    private String encodedToken;
    public WebServiceAndroidClient() {
        this("");
    }

    public WebServiceAndroidClient(final String token) {
        super(token);
    }

    public void setErrorHandler(final ResponseErrorHandler responseErrorHandler) {
        restTemplate.setErrorHandler(responseErrorHandler);
    }

    public void clearToken() {
        headers.remove(HttpHeaders.AUTHORIZATION);
    }

    public void setEncodedToken(final String encodedToken) {
        this.encodedToken = encodedToken;
    }

    public boolean updateAccessToken() {

        final MultiValueMap<String, String> codeMap = new LinkedMultiValueMap<>();
        codeMap.add("grant_type", "refresh_token");
        codeMap.add("refresh_token", token);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedToken);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        final HttpEntity<MultiValueMap<String, String>> internalRec = new HttpEntity<>(codeMap, headers);

        final ResponseEntity<OauthTokenResponse> response =
                restTemplate.exchange(ACCOUNTS_TOKEN_ENDPOINT, HttpMethod.POST, internalRec, OauthTokenResponse.class);
        if (response.hasBody()) {
            OauthTokenResponse credentials = response.getBody();

            headers.remove(HttpHeaders.AUTHORIZATION);
            headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", credentials.getAccess_token()));
            req = new HttpEntity<>("", headers);
            return true;
        }
        return false;
    }

    public ExperimentListDTO listExperiments() {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "experiment",
                HttpMethod.GET, req, ExperimentListDTO.class).getBody();
    }

    public ExperimentListDTO listLiveExperiments() {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "experiment/live",
                HttpMethod.GET, req, ExperimentListDTO.class).getBody();
    }

    public ExperimentListDTO listExperiments(final int smartphoneId) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "experiment?phoneId=" + smartphoneId,
                HttpMethod.GET, req, ExperimentListDTO.class).getBody();
    }

    public ExperimentDTO getExperiment(final String experimentId) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "experiment/" + experimentId,
                HttpMethod.GET, req, ExperimentDTO.class).getBody();
    }

    public PluginListDTO listPlugins() {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "plugin",
                HttpMethod.GET, req, PluginListDTO.class).getBody();
    }

    public SmartphoneStatisticsDTO getSmartphoneStatistics(final int smartphoneId) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "/smartphone/" + smartphoneId + "/statistics",
                HttpMethod.GET, req, SmartphoneStatisticsDTO.class).getBody();
    }

    public SmartphoneStatisticsDTO getSmartphoneStatistics(final int smartphoneId, final String experimentId) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "/smartphone/" + smartphoneId + "/statistics/" + experimentId,
                HttpMethod.GET, req, SmartphoneStatisticsDTO.class).getBody();
    }

    public SmartphoneDTO postSmartphone(final SmartphoneDTO smartphone) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "/smartphone",
                HttpMethod.POST, new HttpEntity<>(smartphone, headers), SmartphoneDTO.class).getBody();
    }

    public RegionListDTO getExperimentRegions(final String experimentId) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "/experiment/" + experimentId + "/region",
                HttpMethod.GET, req, RegionListDTO.class).getBody();
    }

    public ResponseDTO postExperimentResults(final ResultListDTO resultListDTO) {
        if (!"".equals(token)) {
            updateAccessToken();
        }
        return restTemplate.exchange(BASE_URL + "/data/multiple",
                HttpMethod.POST, new HttpEntity<>(resultListDTO, headers), ResponseDTO.class).getBody();
    }

}