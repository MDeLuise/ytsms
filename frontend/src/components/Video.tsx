import { useState } from "react";
import "../style/Video.scss";

export default function Video(props: {
    title: string,
    channelName: string,
    channelId: string,
    id: string,
    thumbnailLink: string,
    view: Number,
    publishedAt: string,
    duration: number | null
}) {
    const [darkMode, _setDarkMode] = useState<boolean>(localStorage.getItem("ytsms-dark") != "false");

    const timeSince = (date: Date): string => {
        var seconds = Math.floor(((new Date()).valueOf() - date.valueOf()) / 1000);
        var interval = seconds / 31536000;
        if (interval > 1) {
            return Math.floor(interval) + " years";
        }
        interval = seconds / 2592000;
        if (interval > 1) {
            return Math.floor(interval) + " months";
        }
        interval = seconds / 86400;
        if (interval > 1) {
            return Math.floor(interval) + " days";
        }
        interval = seconds / 3600;
        if (interval > 1) {
            return Math.floor(interval) + " hours";
        }
        interval = seconds / 60;
        if (interval > 1) {
            return Math.floor(interval) + " minutes";
        }
        return Math.floor(seconds) + " seconds";
    };

    const formatDuration = (duration: number): string => {
        let hours: number = Math.floor(duration / 3600);
        duration %= 3600;
        let minutes: number = Math.floor(duration / 60);
        let seconds: number = Math.floor(duration % 60);
        let formatted: string = "";
        if (hours > 0) {
            formatted += `${hours}h`;
        }
        if (minutes > 0) {
            formatted += `${minutes}m`;
        }
        if (seconds > 0) {
            formatted += `${seconds}s`;
        }
        return formatted;
    };

    const createLink = (videoId: string) => {
        switch (localStorage.getItem("backend")) {
            case "invidious":
                let invidiousInstance = localStorage.getItem("invidiousInstance");
                return invidiousInstance + "/watch?v=" + videoId;
            default:
                return "https://www.youtube.com/watch?v=" + videoId;
        }
    };

    return (
        <div className="video">
            <a href={createLink(props.id)} target="_blank">
                <div>
                    <img className="thumbnail" src={props.thumbnailLink} />
                    {
                        props.duration &&
                        <p className="duration" style={{ color: darkMode ? "inherit" : "white" }}>{formatDuration(props.duration)}</p>
                    }
                </div>
                <h6 className="title">{props.title}</h6>
            </a>
            <div className="video-footer">
                <a href={`?channels=${props.channelId}`}>
                    <p className="channel-name">{props.channelName}</p>
                </a>
                <p className="published-at">{timeSince(new Date(props.publishedAt))} ago</p>
            </div>
        </div>
    );
}
