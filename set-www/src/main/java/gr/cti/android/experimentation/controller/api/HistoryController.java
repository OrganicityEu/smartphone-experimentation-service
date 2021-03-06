package gr.cti.android.experimentation.controller.api;

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

import gr.cti.android.experimentation.controller.BaseController;
import gr.cti.android.experimentation.model.HistoricDataDTO;
import gr.cti.android.experimentation.model.Measurement;
import gr.cti.android.experimentation.model.Result;
import gr.cti.android.experimentation.model.TempReading;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

@RestController
@RequestMapping(value = {"/api/v1", "/v1"})
public class HistoryController extends BaseController {

    /**
     * a log4j logger to print messages.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);
    private SimpleDateFormat df;
    private SimpleDateFormat df1;

    @PostConstruct
    public void init() {

        final TimeZone tz = TimeZone.getTimeZone("UTC");
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df1.setTimeZone(tz);

    }

    @ApiOperation(value = "experiment")
    @RequestMapping(value = {"/entities/{entity_id}/readings"}, method = RequestMethod.GET)
    public HistoricDataDTO experimentView(@PathVariable("entity_id") final String entityId,
                                          @RequestParam(value = "attribute_id") final String attributeId,
                                          @RequestParam(value = "from") final String from,
                                          @RequestParam(value = "to") final String to,
                                          @RequestParam(value = "all_intervals", required = false, defaultValue = "true") final boolean allIntervals,
                                          @RequestParam(value = "rollup", required = false, defaultValue = "") final String rollup,
                                          @RequestParam(value = "function", required = false, defaultValue = "avg") final String function) {

        final HistoricDataDTO historicDataDTO = new HistoricDataDTO();
        historicDataDTO.setEntity_id(entityId);
        historicDataDTO.setAttribute_id(attributeId);
        historicDataDTO.setFunction(function);
        historicDataDTO.setRollup(rollup);
        historicDataDTO.setFrom(from);
        historicDataDTO.setTo(to);
        historicDataDTO.setReadings(new ArrayList<>());

        final List<TempReading> tempReadings = new ArrayList<>();

        long fromLong = parseDateMillis(from);
        long toLong = parseDateMillis(to);

        final String[] parts = entityId.split(":");
        final String phoneId = parts[parts.length - 1];

        LOGGER.info("phoneId: " + phoneId + " from: " + from + " to: " + to);

        final Set<Measurement> results = measurementRepository.findByDeviceIdAndTimestampBetween(Integer.parseInt(phoneId), fromLong, toLong);

        for (final Measurement result : results) {
            try {
                if (result.getMeasurementKey().equals(attributeId)){
                    tempReadings.add(new TempReading(result.getTimestamp(),Double.parseDouble(result.getMeasurementValue())));
                }
            } catch (NumberFormatException e) {
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        List<TempReading> rolledUpTempReadings = new ArrayList<>();

        if ("".equals(rollup)) {
            rolledUpTempReadings = tempReadings;
        } else {
            final Map<Long, SummaryStatistics> dataMap = new HashMap<>();
            for (final TempReading tempReading : tempReadings) {
                Long millis = null;
                //TODO: make rollup understand the first integer part
                if (rollup.endsWith("m")) {
                    millis = new DateTime(tempReading.getTimestamp())
                            .withMillisOfSecond(0).withSecondOfMinute(0).getMillis();
                } else if (rollup.endsWith("h")) {
                    millis = new DateTime(tempReading.getTimestamp())
                            .withMillisOfSecond(0).withSecondOfMinute(0).withMinuteOfHour(0).getMillis();
                } else if (rollup.endsWith("d")) {
                    millis = new DateTime(tempReading.getTimestamp())
                            .withMillisOfDay(0).getMillis();
                }
                if (millis != null) {
                    if (!dataMap.containsKey(millis)) {
                        dataMap.put(millis, new SummaryStatistics());
                    }
                    dataMap.get(millis).addValue(tempReading.getValue());
                }
            }

            final TreeSet<Long> treeSet = new TreeSet<>();
            treeSet.addAll(dataMap.keySet());

            if (allIntervals) {
                fillMissingIntervals(treeSet, rollup, toLong);
            }

            for (final Long millis : treeSet) {
                if (dataMap.containsKey(millis)) {
                    rolledUpTempReadings.add(parse(millis, function, dataMap.get(millis)));
                } else {
                    rolledUpTempReadings.add(new TempReading(millis, 0));
                }
            }
        }

        for (final TempReading tempReading : rolledUpTempReadings) {
            List<Object> list = new ArrayList<>();
            list.add(df.format(tempReading.getTimestamp()));
            list.add(tempReading.getValue());
            historicDataDTO.getReadings().add(list);
        }
        return historicDataDTO;
    }


    private void fillMissingIntervals(TreeSet<Long> treeSet, String rollup, long toLong) {

        //TODO: add non existing intervals
        if (rollup.endsWith("d")) {
            DateTime firstDate = new DateTime(treeSet.iterator().next());

            while (firstDate.isBefore(toLong)) {
                firstDate = firstDate.plusDays(1);
                if (!treeSet.contains(firstDate.getMillis())) {
                    treeSet.add(firstDate.getMillis());
                }
            }
        } else if (rollup.endsWith("h")) {
            DateTime firstDate = new DateTime(treeSet.iterator().next());

            while (firstDate.isBefore(toLong)) {
                firstDate = firstDate.plusHours(1);
                if (!treeSet.contains(firstDate.getMillis())) {
                    treeSet.add(firstDate.getMillis());
                }
            }
        } else if (rollup.endsWith("m")) {
            DateTime firstDate = new DateTime(treeSet.iterator().next());

            while (firstDate.isBefore(toLong)) {
                firstDate = firstDate.plusMinutes(1);
                if (!treeSet.contains(firstDate.getMillis())) {
                    treeSet.add(firstDate.getMillis());
                }
            }
        }
    }

    /**
     * Parse a time instant and create a TempReading object.
     *
     * @param millis     the millis of the timestamp.
     * @param function   the function to aggregate.
     * @param statistics the data values
     * @return the aggregated TempReading for this time instant.
     */
    private TempReading parse(final long millis, final String function, SummaryStatistics statistics) {
        final Double value;
        switch (function) {
            case "avg":
                value = statistics.getMean();
                break;
            case "max":
                value = statistics.getMax();
                break;
            case "min":
                value = statistics.getMin();
                break;
            case "var":
                value = statistics.getVariance();
                break;
            case "sum":
                value = statistics.getSum();
                break;
            default:
                value = statistics.getMean();
        }
        return new TempReading(millis, value);
    }
}
