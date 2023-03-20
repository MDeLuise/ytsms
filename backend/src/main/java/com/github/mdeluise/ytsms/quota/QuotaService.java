package com.github.mdeluise.ytsms.quota;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class QuotaService {
    private final QuotaCounterRepository quotaCounterRepository;

    public QuotaService(QuotaCounterRepository quotaCounterRepository) {
        this.quotaCounterRepository = quotaCounterRepository;
    }


    public int getTodayQuota() {
        final QuotaCounter todayQuotaCounter = getTodayQuotaCounter().orElse(new QuotaCounter());
        return todayQuotaCounter.getQuotaValue();
    }


    public void setTodayQuota(int quota) {
        final Optional<QuotaCounter> todayQuotaCounter = getTodayQuotaCounter();
        final QuotaCounter toSave = todayQuotaCounter.orElse(new QuotaCounter());

        toSave.setQuotaValue(quota);
        if (todayQuotaCounter.isEmpty()) {
            toSave.setQuotaDay(new Date());
        }

        quotaCounterRepository.save(toSave);
    }


    public void addToTodayQuota(int valueToAdd) {
        final Optional<QuotaCounter> todayQuotaCounter = getTodayQuotaCounter();
        final QuotaCounter toSave = todayQuotaCounter.orElse(new QuotaCounter());

        int updatedValue = toSave.getQuotaValue() + valueToAdd;
        toSave.setQuotaValue(updatedValue);

        if (todayQuotaCounter.isEmpty()) {
            toSave.setQuotaDay(new Date());
        }

        quotaCounterRepository.save(toSave);
    }


    private Optional<QuotaCounter> getTodayQuotaCounter() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final Date startOfDay = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        final Date endOfDay = calendar.getTime();

        final List<QuotaCounter> quotaCounters = quotaCounterRepository.findByQuotaDayBetweenOrderByQuotaDayAsc(startOfDay, endOfDay);

        if (!quotaCounters.isEmpty()) {
            return Optional.of(quotaCounters.get(0));
        } else {
            return Optional.empty();
        }
    }
}
