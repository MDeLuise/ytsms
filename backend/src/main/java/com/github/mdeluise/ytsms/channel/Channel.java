package com.github.mdeluise.ytsms.channel;

import com.github.mdeluise.ytsms.common.IdentifiedEntity;
import com.github.mdeluise.ytsms.video.Video;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Set;

@Entity
@Table(name = "channels")
public class Channel implements IdentifiedEntity<String> {
    @Id
    private String id;
    private String name;
    @OneToMany(mappedBy = "channel")
    private Set<Video> video;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Set<Video> getVideo() {
        return video;
    }


    public void setVideo(Set<Video> video) {
        this.video = video;
    }
}
