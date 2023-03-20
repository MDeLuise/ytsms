package com.github.mdeluise.ytsms.subscription;


import java.util.List;

import com.github.mdeluise.ytsms.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUser(User user);

    boolean existsByUserAndChannelId(User user, String channelId);
}
