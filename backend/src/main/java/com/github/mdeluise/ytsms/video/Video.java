package com.github.mdeluise.ytsms.video;

import com.github.mdeluise.ytsms.channel.Channel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "video")
public class Video {
    @Id
    private String id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    @NotBlank
    private String title;
    @NotNull
    private Date publishedAt;
    @NotBlank
    private String thumbnailLink;
    private Long view;
    private Long secondsDuration;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public Channel getChannel() {
        return channel;
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public Date getPublishedAt() {
        return publishedAt;
    }


    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }


    public String getThumbnailLink() {
        return thumbnailLink;
    }


    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }


    public Long getView() {
        return view;
    }


    public void setView(Long view) {
        this.view = view;
    }


    public Long getSecondsDuration() {
        return secondsDuration;
    }


    public void setSecondsDuration(Long secondsDuration) {
        this.secondsDuration = secondsDuration;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Video video = (Video) o;
        return Objects.equals(id, video.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
