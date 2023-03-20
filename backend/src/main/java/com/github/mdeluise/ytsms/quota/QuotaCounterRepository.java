package com.github.mdeluise.ytsms.quota;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotaCounterRepository extends JpaRepository<QuotaCounter, Long> {
    List<QuotaCounter> findByQuotaDayBetweenOrderByQuotaDayAsc(Date startOfDay, Date endOfDay);
}
