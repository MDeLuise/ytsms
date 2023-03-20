import { AxiosInstance } from "axios";
import { useEffect, useRef, useState } from "react";
import { Link, NavigateFunction, useNavigate, useSearchParams } from "react-router-dom";
import Video from "./Video";
import Navbar from "./Navbar";
import Pagination from '@mui/material/Pagination';
import PaginationItem from '@mui/material/PaginationItem';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import Typography from '@mui/material/Typography';
import "../style/Home.scss";
import "../style/Base.scss";
import { ScraperStatus } from "../interfaces";

export default function Home(props: {
    isLoggedIn: () => boolean,
    requestor: AxiosInstance,
    colorMode: any;
}) {
    let navigate: NavigateFunction = useNavigate();
    const [videoRes, setVideoRes] = useState<any[]>([]);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [searchParams, _setSearchParams] = useSearchParams();
    const [channels, setChannels] = useState<{}[]>([]);
    const [scraperStatus, setScraperStatus] = useState<ScraperStatus>();
    const [quota, setQuota] = useState<number>(0);
    const pageSize = process.env.PAGE_SIZE != null ? process.env.PAGE_SIZE : 25;
    const windowSize = useRef([window.innerWidth, window.innerHeight]);

    const fetchAllVideo = (pageNum: number) => {
        props.requestor.get(`video?sortBy=publishedAt&sortDir=DESC&pageSize=${pageSize}&pageNo=${pageNum}`, {})
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
                window.scrollTo({
                    top: 0,
                    left: 0
                });
            })
            .catch();
    };

    const fetchVideoFromChannels = (pageNum: number, channelIds: string) => {
        props.requestor.get(`video/${channelIds}?sortBy=publishedAt&sortDir=DESC&pageSize=${pageSize}&pageNo=${pageNum}`, {})
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
                            publishedAt={vid.publishedAt} />);
                });
                setVideoRes(video);
                window.scrollTo({
                    top: 0,
                    left: 0
                });
            })
            .catch();
    };

    const fetchVideo = (pageNum: number) => {
        setCurrentPage(pageNum);
        if (searchParams.has("channels")) {
            fetchVideoFromChannels(pageNum, searchParams.get("channels") as string);
        } else {
            fetchAllVideo(pageNum);
        }
    };

    const getAllChannels = () => {
        props.requestor.get("subscription", {})
            .then((response) => {
                let channels: {}[] = [];
                response.data.forEach((sub: any) => {
                    channels.push(
                        {
                            "text": sub.channelName,
                            "id": sub.channelId,
                            "src": sub.channelThumbnailLink
                        }
                    );
                });
                setChannels(channels);
            })
            .catch();
    };

    const getPageRangeDisplayed = (): number => {
        return windowSize.current[0] > 760 ? 1 : 1;
    };

    const getMarginPagesDisplayed = (): number => {
        return windowSize.current[0] > 760 ? 2 : 1;
    };

    const refreshVideoFromBackend = (): void => {
        props.requestor.post("/video/_refresh");
    };

    const getScraperStatus = (): void => {
        props.requestor.get("/info/scraping-status")
            .then((res) => {
                setScraperStatus(res.data);
            });
    };

    const getQuotaUsage = (): void => {
        props.requestor.get("/quota")
            .then((res) => {
                setQuota(res.data);
            });
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
        getScraperStatus();
        getQuotaUsage();
    }, []);

    return (
        <>
            <Navbar
                channels={channels}
                colorMode={props.colorMode}
                getVideoFromChannel={(channelId: string) => { fetchVideoFromChannels(0, channelId); }}
                getAllVideo={() => fetchAllVideo(0)}
            />
            {
                channels.length !== 0 &&
                <Typography style={{
                    display: "flex",
                    justifyContent: "center",
                    gap: "5px",
                    marginBottom: "20px",
                    fontStyle: scraperStatus?.scraping && !scraperStatus.isLastFailed ? "italic" : "normal",
                }}>
                    {
                        scraperStatus?.isLastFailed ?
                            <span>Error: last refresh failed</span> :
                            <span>
                                {
                                    scraperStatus?.scraping ?
                                        "refreshing..." :
                                        scraperStatus?.lastScrape != undefined ?
                                            "Last refresh: " + new Date(scraperStatus.lastScrape).toLocaleString() :
                                            "n/a"
                                }
                            </span>
                    }
                    {
                        <span>
                            {
                                !scraperStatus?.scraping &&
                                <span>
                                    <span> | </span>
                                    <Link to={"#"} onClick={refreshVideoFromBackend}>
                                        refresh now
                                    </Link>
                                </span>
                            }
                        </span>
                    }
                    <Typography >Quota {quota} </Typography>
                </Typography>
            }
            <div id="video-wrapper">{videoRes}</div>
            {
                channels.length == 0 ?
                    <Typography variant="body1" gutterBottom sx={{ display: "flex", justifyContent: "center" }}>
                        You have no subscription :(<br />Go to the settings to add following channels.
                    </Typography> :
                    <Pagination
                        count={totalPages}
                        page={currentPage + 1}
                        renderItem={(item) => (
                            <PaginationItem
                                slots={{ previous: ArrowBackIcon, next: ArrowForwardIcon }}
                                {...item}
                            />
                        )}
                        color="primary"
                        siblingCount={getPageRangeDisplayed()}
                        boundaryCount={getMarginPagesDisplayed()}
                        onChange={(_event, value) => {
                            searchParams.set("page", value.toString());
                            fetchVideo(value - 1);
                            setCurrentPage(value - 1);
                        }}
                        sx={{
                            margin: "50px 0",
                            display: "flex",
                            justifyContent: "center",
                            gap: "40px",
                            marginTop: "50px"
                        }}
                    />
            }
        </>
    );
}
