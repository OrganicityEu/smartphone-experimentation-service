package gr.cti.android.experimentation.model;

/*-
 * #%L
 * Smartphone Experimentation Model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Entity that describes an award badge given to users for gathering data.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Region implements Serializable {
    /**
     * Id of the entity.
     */
    @Id
    @GeneratedValue
    private int id;

    private String name;

    private String startDate;

    private String endDate;

    private String startTime;

    private String endTime;

    private Integer minMeasurements;

    private Integer maxMeasurements;

    private String weight;

    private String coordinates;

    private String experimentId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getMinMeasurements() {
        return minMeasurements;
    }

    public void setMinMeasurements(Integer minMeasurements) {
        this.minMeasurements = minMeasurements;
    }

    public Integer getMaxMeasurements() {
        return maxMeasurements;
    }

    public void setMaxMeasurements(Integer maxMeasurements) {
        this.maxMeasurements = maxMeasurements;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;

        if (id != region.id) return false;
        if (minMeasurements != region.minMeasurements) return false;
        if (maxMeasurements != region.maxMeasurements) return false;
        if (experimentId != region.experimentId) return false;
        if (name != null ? !name.equals(region.name) : region.name != null) return false;
        if (startDate != null ? !startDate.equals(region.startDate) : region.startDate != null) return false;
        if (endDate != null ? !endDate.equals(region.endDate) : region.endDate != null) return false;
        if (weight != null ? !weight.equals(region.weight) : region.weight != null) return false;
        return coordinates != null ? coordinates.equals(region.coordinates) : region.coordinates == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + minMeasurements;
        result = 31 * result + maxMeasurements;
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", minMeasurements=" + minMeasurements +
                ", maxMeasurements=" + maxMeasurements +
                ", weight='" + weight + '\'' +
                ", coordinates='" + coordinates + '\'' +
                ", experimentId=" + experimentId +
                '}';
    }
}
