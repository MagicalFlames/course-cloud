package com.zjgsu.wzy.catalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public class ScheduleSlot {
    @Column(name = "scheduleslot_dayofweek")
    private String dayOfWeek;
    @Column(name = "scheduleslot_starttime")
    private LocalTime startTime;
    @Column(name = "scheduleslot_endtime")
    private LocalTime endTime;
    @Column(name = "scheduleslot_expectedAttendance")
    private Long expectedAttendance;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Long getExpectedAttendance() {
        return expectedAttendance;
    }

    public void setExpectedAttendance(Long expectedAttendance) {
        this.expectedAttendance = expectedAttendance;
    }
}
