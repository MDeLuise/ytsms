import { AxiosInstance } from "axios";
import { useState } from "react";

export default function Subscription(props: {id: string, channelName: string, channelId: string, requestor: AxiosInstance}) {
    const [removed, setRemoved] = useState<boolean>(false);
    const removeSubscription = () => {
        props.requestor.delete("subscription/" + props.id, {})
        .then((_data) => {
            setRemoved(true);
        })
        .catch((error) => console.error(error));
    }
    
    return (
        <span hidden={removed}>
            <i className="clickable fa fa-trash" aria-hidden="true" onClick={removeSubscription}></i>
            <span>{props.channelName != undefined ? props.channelName : props.channelId}</span>
        </span>
    )
}