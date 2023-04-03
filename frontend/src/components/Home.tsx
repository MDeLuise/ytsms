import { AxiosInstance } from "axios";
import { useEffect, useState } from "react";
import { NavigateFunction, useNavigate, useSearchParams } from "react-router-dom";
import Video from "./Video";
import "../style/Home.scss";
import "../style/Base.scss";
import Navbar from "./Navbar";
import ReactPaginate from "react-paginate";

export default function Home(props: { isLoggedIn: () => boolean, requestor: AxiosInstance }) {
    let navigate: NavigateFunction = useNavigate();
    const [videoRes, setVideoRes] = useState<any>([]);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [searchParams, setSearchParams] = useSearchParams();

    const fetchAllVideo = (pageNum: number) => {
        props.requestor.get(`video?sortBy=publishedAt&sortDir=DESC&pageSize=25&pageNo=${pageNum}`, {})
            .then((response) => {
                setTotalPages(response.data.totalPages);
                let video: any[] = [];
                response.data["content"].forEach((vid: any) => {
                    video.push(
                        <Video
                            id={vid.id}
                            title={vid.title}
                            channelName={vid.channelName}
                            channelId={vid.channelId}
                            thumbnailLink={vid.thumbnailLink}
                            view={vid.view}
                            duration={vid.secondsDuration}
                            publishedAt={vid.publishedAt} />)
                })
                setVideoRes(video);
            })
            .catch();
    }


    const fetchVideoFromChannels = (pageNum: number, channelIds: string) => {
        props.requestor.get(`video/${channelIds}?sortBy=publishedAt&sortDir=DESC&pageSize=25&pageNo=${pageNum}`, {})
            .then((response) => {
                setTotalPages(response.data.totalPages);
                let video: any[] = [];
                response.data["content"].forEach((vid: any) => {
                    video.push(
                        <Video
                            id={vid.id}
                            title={vid.title}
                            channelName={vid.channelName}
                            channelId={vid.channelId}
                            thumbnailLink={vid.thumbnailLink}
                            view={vid.view}
                            duration={vid.secondsDuration}
                            publishedAt={vid.publishedAt} />)
                })
                setVideoRes(video);
            })
            .catch();
    }


    const fetchVideo = (pageNum: number) => {
        if (searchParams.has("channels")) {
            fetchVideoFromChannels(pageNum, searchParams.get("channels") as string);
        } else {
            fetchAllVideo(pageNum);
        }
    }


    useEffect(() => {
        if (!props.isLoggedIn()) {
            navigate("/auth");
        }
        fetchVideo(0);
    }, []);


    return (
        <>
            <Navbar />
            <div id="video-wrapper">{videoRes}</div>
            <ReactPaginate
                breakLabel="..."
                nextLabel=">"
                onPageChange={(e) => {
                    fetchVideo(e.selected);
                    window.scrollTo({
                        top: 0,
                        left: 0
                    })
                }
                }
                pageRangeDisplayed={5}
                pageCount={totalPages}
                previousLabel="<"
            />
        </>
    );
}