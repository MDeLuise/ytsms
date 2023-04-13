import { useRef } from "react";
import MobileNavbar from "./MobileNavbar";
import DesktopNavbar from "./DesktopNavbar";

export default function Navbar(props: {
    channels?: any[],
    colorMode: any,
    getVideoFromChannel?: (id: string) => void,
    getAllVideo?: () => void
}) {
    const windowSize = useRef([window.innerWidth, window.innerHeight]);

    return (
        windowSize.current[0] < 760 ?
            <MobileNavbar
                channels={props.channels}
                colorMode={props.colorMode}
                getVideoFromChannel={props.getVideoFromChannel != undefined ? props.getVideoFromChannel : (id: string) => { }}
                getAllVideo={props.getAllVideo != undefined ? props.getAllVideo : () => { }}
            ></MobileNavbar> :
            <DesktopNavbar
                channels={props.channels}
                colorMode={props.colorMode}
                getVideoFromChannel={props.getVideoFromChannel != undefined ? props.getVideoFromChannel : (id: string) => { }}
                getAllVideo={props.getAllVideo != undefined ? props.getAllVideo : () => { }}
            ></DesktopNavbar>
    );
}