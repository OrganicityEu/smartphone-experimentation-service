package gr.cti.android.experimentation.controller.ui;

import gr.cti.android.experimentation.controller.BaseController;
import gr.cti.android.experimentation.model.Result;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Dimitrios Amaxilatis.
 */
@Controller
public class RestDataController extends BaseController {
    /**
     * a log4j logger to print messages.
     */
    private static final Logger LOGGER = Logger.getLogger(RestDataController.class);

    @ResponseBody
    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = "text/csv")
    public String dataCsv(@RequestParam(value = "type") final String type) throws JSONException {
        final StringBuilder response = new StringBuilder();
        for (final Result result : resultRepository.findAll()) {
            try {
                final JSONObject object = new JSONObject(result.getMessage());
                if (object.has(type)) {
                    response.append(object.get(type)).append("\n");
                }
            } catch (Exception ignore) {
            }
        }
        return response.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/data", method = RequestMethod.GET, produces = "application/json")
    public String getExperimentDataByExperimentId(@RequestParam(value = "deviceId", defaultValue = "0", required = false) final int deviceId, @RequestParam(value = "after", defaultValue = "0", required = false) final String after
            , @RequestParam(value = "to", defaultValue = "0", required = false) final String to
            , @RequestParam(value = "accuracy", required = false, defaultValue = "3") final int accuracy) {
        return getAllData(deviceId, after, to, accuracy).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/experiment/data/{experimentId}", method = RequestMethod.GET, produces = "application/json")
    public String getExperimentDataByExperimentId(@PathVariable("experimentId") final String experiment, @RequestParam(value = "deviceId", defaultValue = "0", required = false) final int deviceId, @RequestParam(value = "after", defaultValue = "0", required = false) final String after
            , @RequestParam(value = "to", defaultValue = "0", required = false) final String to
            , @RequestParam(value = "accuracy", required = false, defaultValue = "3") final int accuracy) {
        LOGGER.info("to:" + to);
        return getExperimentData(experiment, deviceId, after, to, accuracy).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/experiment/data/{experimentId}/hour", method = RequestMethod.GET, produces = "application/json")
    public String getExperimentDataHourlyByExperimentId(@PathVariable("experimentId") final String experiment, @RequestParam(value = "deviceId", defaultValue = "0", required = false) final int deviceId, @RequestParam(value = "after", defaultValue = "0", required = false) final String after
            , @RequestParam(value = "to", defaultValue = "0", required = false) final String to
            , @RequestParam(value = "accuracy", required = false, defaultValue = "3") final int accuracy) {
        JSONObject data = getExperimentHourlyData(experiment, deviceId, after, to, accuracy);
        LOGGER.info(data);
        return data.toString();
    }

//    @ResponseBody
//    @RequestMapping(value = "/api/v1/experiment/data/{experimentId}/rankings", method = RequestMethod.GET, produces = "application/json")
//    public String getExperimentDataHourlyByExperimentId(@PathVariable("experimentId") final String experiment, @RequestParam(value = "deviceId", defaultValue = "0", required = false) final int deviceId, @RequestParam(value = "after", defaultValue = "0", required = false) final String after) {
//        JSONObject data = getExperimentHourlyData(experiment, deviceId, after);
//        LOGGER.info(data);
//        return data.toString();
//    }

    private JSONArray getExperimentData(final String experiment, final int deviceId, final String after, final String to, final int accuracy) {
        final String format = getFormat(accuracy);
        final DecimalFormat df = new DecimalFormat(format);
        LOGGER.info("format:" + format);
        final long start = parseDateMillis(after);
        final long end = parseDateMillis(to);

        final Set<Result> results;
        if (deviceId == 0) {
            results = resultRepository.findByExperimentIdAndTimestampAfter(Integer.parseInt(experiment), start);
        } else {
            results = resultRepository.findByExperimentIdAndDeviceIdAndTimestampAfterOrderByTimestampAsc(Integer.parseInt(experiment), deviceId, start);
        }


        final JSONArray addressPoints = doCalculations(results, end, df);
        LOGGER.info(addressPoints.toString());
        return addressPoints;
    }

    private JSONArray getAllData(final int deviceId, final String after, final String to, final int accuracy) {
        final String format = getFormat(accuracy);

        DecimalFormat df = new DecimalFormat(format);
        long start = parseDateMillis(after);
        long end = parseDateMillis(to);

        final Set<Result> results;
        if (deviceId == 0) {
            results = new HashSet<>();
        } else {
            results = resultRepository.findByDeviceIdAndTimestampAfterOrderByTimestampAsc(deviceId, start);
        }

        final JSONArray addressPoints = doCalculations(results, end, df);
        LOGGER.info(addressPoints.toString());
        return addressPoints;
    }

    private JSONObject getExperimentHourlyData(final String experiment, final int deviceId, final String after, final String to, final int accuracy) {
        final String format = getFormat(accuracy);
        final DecimalFormat df = new DecimalFormat(format);
        final long start = parseDateMillis(after);
        final long end = parseDateMillis(to);

        final Set<Result> results;
        if (deviceId == 0) {
            results = resultRepository.findByExperimentIdAndTimestampAfter(Integer.parseInt(experiment), start);
        } else {
            results = resultRepository.findByExperimentIdAndDeviceIdAndTimestampAfterOrderByTimestampAsc(Integer.parseInt(experiment), deviceId, start);
        }

        try {
            final Map<Integer, Map<String, Map<String, Map<String, DescriptiveStatistics>>>> dataAggregates = new HashMap<>();
            String longitude;
            String latitude;
            final DescriptiveStatistics wholeDataStatistics = new DescriptiveStatistics();
            final Map<Integer, Map<String, Map<String, Long>>> locationsHeatMap = new HashMap<>();
            for (final Result result : results) {
                try {
                    if (!result.getMessage().startsWith("{")) {
                        continue;
                    }
                    if (end != 0 && result.getTimestamp() > end) {
                        continue;
                    }
                    final JSONObject message = new JSONObject(result.getMessage());

                    int hour = new DateTime(result.getTimestamp()).getHourOfDay();

                    if (message.has(LATITUDE) && message.has(LONGITUDE)) {
                        longitude = df.format(message.getDouble(LONGITUDE));
                        latitude = df.format(message.getDouble(LATITUDE));
                        if (!dataAggregates.containsKey(hour)) {
                            dataAggregates.put(hour, new HashMap<>());
                        }
                        if (!dataAggregates.get(hour).containsKey(longitude)) {
                            dataAggregates.get(hour).put(longitude, new HashMap<>());
                        }
                        if (!dataAggregates.get(hour).get(longitude).containsKey(latitude)) {
                            dataAggregates.get(hour).get(longitude).put(latitude, new HashMap<>());
                        }

                        //HeatMap
                        if (!locationsHeatMap.containsKey(hour)) {
                            locationsHeatMap.put(hour, new HashMap<>());
                        }
                        if (!locationsHeatMap.get(hour).containsKey(longitude)) {
                            locationsHeatMap.get(hour).put(longitude, new HashMap<>());
                        }
                        if (!locationsHeatMap.get(hour).get(longitude).containsKey(latitude)) {
                            locationsHeatMap.get(hour).get(longitude).put(latitude, 0L);
                        }

                        final Long val = locationsHeatMap.get(hour).get(longitude).get(latitude);
                        locationsHeatMap.get(hour).get(longitude).put(latitude, val + 1);


                        final Iterator iterator = message.keys();
                        if (longitude != null && latitude != null) {
                            while (iterator.hasNext()) {
                                final String key = (String) iterator.next();
                                if (key.equals(LATITUDE) || key.equals(LONGITUDE)) {
                                    continue;
                                }

                                if (!dataAggregates.get(hour).get(longitude).get(latitude).containsKey(key)) {
                                    dataAggregates.get(hour).get(longitude).get(latitude).put(key, new DescriptiveStatistics());
                                }
                                try {
                                    dataAggregates.get(hour).get(longitude).get(latitude).get(key).addValue(message.getDouble(key));
                                    wholeDataStatistics.addValue(message.getDouble(key));
                                } catch (Exception e) {
                                    LOGGER.error(e, e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e, e);
                }
            }
            final JSONObject hourlyPoints = new JSONObject();
            for (final Integer hour : dataAggregates.keySet()) {
                final JSONArray addressPoints = new JSONArray();
                for (final String longit : dataAggregates.get(hour).keySet()) {
                    for (final String latit : dataAggregates.get(hour).get(longit).keySet()) {
                        LOGGER.info("{" + longit + ":" + latit + "}");
                        final JSONArray measurement = new JSONArray();
                        try {
                            measurement.put(Double.parseDouble(latit));
                            measurement.put(Double.parseDouble(longit));
                            if (locationsHeatMap.containsKey(hour) && locationsHeatMap.get(hour).containsKey(longit) && locationsHeatMap.get(hour).get(longit).containsKey(latit)) {
                                measurement.put(locationsHeatMap.get(hour).get(longit).get(latit));
                            } else {
                                measurement.put(0);
                            }
                            final JSONObject data = new JSONObject();
                            measurement.put(data);
                            for (final Object key : dataAggregates.get(hour).get(longit).get(latit).keySet()) {
                                final String keyString = (String) key;
                                final String part = keyString.split("\\.")[keyString.split("\\.").length - 1];
                                data.put(part, dataAggregates.get(hour).get(longit).get(latit).get(keyString).getMean());
                            }
                            addressPoints.put(measurement);
                        } catch (JSONException e) {
                            LOGGER.error(e, e);
                        }
                    }
                }
                try {
                    hourlyPoints.put(String.valueOf(hour), addressPoints);
                } catch (JSONException e) {
                    LOGGER.error(e, e);
                }
            }
            LOGGER.info(hourlyPoints.toString());
            return hourlyPoints;
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return null;

    }


    private JSONArray doCalculations(final Set<Result> results, final long end, final DecimalFormat df) {
        final Map<String, Map<String, Map<String, DescriptiveStatistics>>> dataAggregates = new HashMap<>();
        final DescriptiveStatistics wholeDataStatistics = new DescriptiveStatistics();
        final Map<String, Map<String, Long>> locationsHeatMap = new HashMap<>();

        for (final Result result : results) {
            try {
                if (!result.getMessage().startsWith("{")) {
                    continue;
                }
                if (end != 0 && result.getTimestamp() > end) {
                    continue;
                }


                final JSONObject message = new JSONObject(result.getMessage());

                if (message.has(LATITUDE) && message.has(LONGITUDE)) {
                    final String longitude = df.format(message.getDouble(LONGITUDE));
                    final String latitude = df.format(message.getDouble(LATITUDE));
                    if (!dataAggregates.containsKey(longitude)) {
                        dataAggregates.put(longitude, new HashMap<>());
                    }
                    if (!dataAggregates.get(longitude).containsKey(latitude)) {
                        dataAggregates.get(longitude).put(latitude, new HashMap<>());
                    }

                    //HeatMap
                    if (!locationsHeatMap.containsKey(longitude)) {
                        locationsHeatMap.put(longitude, new HashMap<>());
                    }
                    if (!locationsHeatMap.get(longitude).containsKey(latitude)) {
                        locationsHeatMap.get(longitude).put(latitude, 0L);
                    }
                    final Long val = locationsHeatMap.get(longitude).get(latitude);
                    locationsHeatMap.get(longitude).put(latitude, val + 1);


                    final Iterator iterator = message.keys();
                    if (longitude != null && latitude != null) {
                        while (iterator.hasNext()) {
                            final String key = (String) iterator.next();
                            if (key.equals(LATITUDE) || key.equals(LONGITUDE)) {
                                continue;
                            }

                            if (!dataAggregates.get(longitude).get(latitude).containsKey(key)) {
                                dataAggregates.get(longitude).get(latitude).put(key, new DescriptiveStatistics());
                            }
                            try {
                                dataAggregates.get(longitude).get(latitude).get(key).addValue(
                                        message.getDouble(key)
                                );
                                wholeDataStatistics.addValue(message.getDouble(key));
                            } catch (Exception e) {
                                LOGGER.error(e, e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e, e);
            }
        }
        
        final JSONArray addressPoints = new JSONArray();
        for (final String longitude : dataAggregates.keySet()) {
            for (final String latitude : dataAggregates.get(longitude).keySet()) {
                LOGGER.info("{" + longitude + ":" + latitude + "}");
                final JSONArray measurement = new JSONArray();
                try {
                    measurement.put(Double.parseDouble(latitude));
                    measurement.put(Double.parseDouble(longitude));
                    if (locationsHeatMap.containsKey(longitude) && locationsHeatMap.get(longitude).containsKey(latitude)) {
                        measurement.put(String.valueOf(locationsHeatMap.get(longitude).get(latitude)));
                    } else {
                        measurement.put(1);
                    }
                    final JSONObject data = new JSONObject();
                    measurement.put(data);
                    for (final Object key : dataAggregates.get(longitude).get(latitude).keySet()) {
                        final String keyString = (String) key;
                        final String part = keyString.split("\\.")[keyString.split("\\.").length - 1];
                        data.put(part, dataAggregates.get(longitude).get(latitude).get(keyString).getMean());
                    }
                    addressPoints.put(measurement);
                } catch (JSONException e) {
                    LOGGER.error(e, e);
                }
            }
        }
        return addressPoints;
    }

    private String getFormat(int accuracy) {
        String format = "#";
        if (accuracy > 0) {
            format += ".";
            for (int i = 0; i < accuracy; i++) {
                format += "0";
            }
        }
        return format;
    }
}
