package com.github.mdeluise.ytsms.channel;

import com.github.mdeluise.ytsms.video.Video;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Entity
@Table(name = "channels")
public class Channel {
    @Id
    private String id;
    @Length(max = 100)
    private String name;
    private String thumbnailLink;
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


    public String getThumbnailLink() {
        return thumbnailLink;
    }


    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }


    public Set<Video> getVideo() {
        return video;
    }


    public void setVideo(Set<Video> video) {
        this.video = video;
    }
}
