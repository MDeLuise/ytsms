package com.github.mdeluise.ytsms.quota;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quotas")
public class QuotaCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date quotaDay;
    private int quotaValue;


    public QuotaCounter() {
        this.quotaValue = 0;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getId() {
        return id;
    }


    public Date getQuotaDay() {
        return quotaDay;
    }


    public void setQuotaDay(Date day) {
        this.quotaDay = day;
    }


    public int getQuotaValue() {
        return quotaValue;
    }


    public void setQuotaValue(int value) {
        this.quotaValue = value;
    }
}
