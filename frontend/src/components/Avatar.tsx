import { AutoTextSize } from "auto-text-size";
import { useSearchParams } from "react-router-dom";
import "../style/Channel.scss";

export default function Avatar(props: { id: string, src: string | undefined, text: string }) {
    const [searchParams, setSearchParams] = useSearchParams();


    const isSelected = (): boolean => {
        return searchParams.has("channels") && searchParams.get("channels")?.split(",").includes(props.id) as boolean;
    };


    const processText = (text: string): string => {
        return text.length > 7 ? text.substring(0, 7) + "..." : text;
    };


    const stringToColour = (str: string): string => {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = str.charCodeAt(i) + ((hash << 5) - hash);
        }
        let colour = '#';
        for (let i = 0; i < 3; i++) {
            let value = (hash >> (i * 8)) & 0xFF;
            colour += ('00' + value.toString(16)).substring(('00' + value.toString(16)).length - 2);
        }
        return colour;
    }

    return (
        <a href={isSelected() ? "/" : "?channels=" + props.id} className={"channel-link " + (isSelected() ? "selected" : "")}>
            <div className="channel" style={{backgroundColor: stringToColour(processText(props.text))}}>
                {props.src != undefined ? <img src={props.src} /> : <AutoTextSize><p style={{color: "black", margin: 0}}>{processText(props.text)}</p></AutoTextSize>}
            </div>
        </a>
    );
}