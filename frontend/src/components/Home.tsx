import { AxiosInstance } from "axios";
import { useEffect, useRef, useState } from "react";
import { NavigateFunction, useNavigate, useSearchParams } from "react-router-dom";
import Video from "./Video";
import "../style/Home.scss";
import "../style/Base.scss";
import Navbar from "./Navbar";
import ReactPaginate from "react-paginate";
import Avatar from "./Avatar";

export default function Home(props: { isLoggedIn: () => boolean, requestor: AxiosInstance }) {
    let navigate: NavigateFunction = useNavigate();
    const [videoRes, setVideoRes] = useState<any>([]);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [searchParams, setSearchParams] = useSearchParams();
    const [channels, setChannels] = useState<string[]>([]);
    const windowSize = useRef([window.innerWidth, window.innerHeight]);

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
                            publishedAt={vid.publishedAt} />
                    );
                });
                setVideoRes(video);
            })
            .catch();
    };


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
    };


    const fetchVideo = (pageNum: number) => {
        if (searchParams.has("channels")) {
            fetchVideoFromChannels(pageNum, searchParams.get("channels") as string);
        } else {
            fetchAllVideo(pageNum);
        }
    };


    const getAllChannels = () => {
        props.requestor.get("subscription", {})
            .then((response) => {
                let channels: any[] = [];
                response.data.forEach((sub: any) => {
                    channels.push(
                        <Avatar
                            text={sub.channelName}
                            id={sub.channelId}
                            src={sub.channelThumbnailLink} />
                    );
                });
                setChannels(channels);
            })
            .catch();
    };


    const getPageRangeDisplayed = (): number => {
        return windowSize.current[0] > 400 ? 3 : 1;
    };


    const getMarginPagesDisplayed = (): number => {
        return windowSize.current[0] > 400 ? 3 : 1;
    };


    useEffect(() => {
        if (!props.isLoggedIn()) {
            navigate("/auth");
        }
        if (searchParams.has("page")) {
            setCurrentPage(Number(searchParams.get("page")));
        } else {
            setCurrentPage(0);
        }
        getAllChannels();
        fetchVideo(0);
    }, []);


    return (
        <>
            <Navbar />
            <div id="channel-filter" style={{
                height: "fit-content",
                display: "flex",
                gap: "20px",
                margin: "40px 20px",
                borderBottom: "1px solid var(--color)",
                paddingBottom: "10px",
                overflow: "scroll"
            }}>
                {channels}
            </div>

            <div id="video-wrapper">{videoRes}</div>
            <ReactPaginate
                breakLabel="..."
                nextLabel=">"
                onPageChange={(e) => {
                    fetchVideo(e.selected);
                    setCurrentPage(e.selected + 1);
                    searchParams.set("page", (e.selected != undefined ? e.selected.toString : "0") as string);
                    window.scrollTo({
                        top: 0,
                        left: 0
                    })
                }}
                pageRangeDisplayed={getPageRangeDisplayed()}
                marginPagesDisplayed={getMarginPagesDisplayed()}
                pageCount={totalPages}
                previousLabel="<"
                initialPage={currentPage}
            />
        </>
    );
}