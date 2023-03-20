package com.github.mdeluise.ytsms.quota;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quota")
public class QuotaController {
    private final QuotaService quotaService;


    @Autowired
    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }


    @GetMapping
    public int getQuotaForToday() {
        return quotaService.getTodayQuota();
    }
}

