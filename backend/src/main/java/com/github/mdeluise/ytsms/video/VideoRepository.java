package com.github.mdeluise.ytsms.video;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findByChannelIdIn(Collection<String> channelIds, Pageable pageable);
}
