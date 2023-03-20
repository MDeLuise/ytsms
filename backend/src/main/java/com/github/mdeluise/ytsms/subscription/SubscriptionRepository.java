package com.github.mdeluise.ytsms.subscription;


import com.github.mdeluise.ytsms.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUser(User user);
}
