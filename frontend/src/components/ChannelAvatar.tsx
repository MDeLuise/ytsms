import { useSearchParams } from "react-router-dom";
import Avatar from '@mui/material/Avatar';

export default function ChannelAvatar(props: { id: string, src: string | undefined, text: string }) {
    const [searchParams, setSearchParams] = useSearchParams();

    const isSelected = (): boolean => {
        return searchParams.has("channels") && searchParams.get("channels")?.split(",").includes(props.id) as boolean;
    };

    const processText = (text: string): string => {
        return text.length > 7 ? text.substring(0, 7) + "..." : text;
    };

    const stringToColor = (str: string): string => {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = str.charCodeAt(i) + ((hash << 5) - hash);
        }
        let color = '#';
        for (let i = 0; i < 3; i++) {
            let value = (hash >> (i * 8)) & 0xFF;
            color += ('00' + value.toString(16)).substring(('00' + value.toString(16)).length - 2);
        }
        return color;
    };

    const getInitials = (name: string): string => {
        return name.split(" ").map(part => part.at(0)).join("").substring(0, 2).toUpperCase();
    };


    return (
        <Avatar
            sx={{bgcolor: stringToColor(props.text)}}
            alt={props.text}
            src={props.src != undefined ? props.src : "not-existing-url"}
        >{getInitials(props.text)}</Avatar>
    );
}
