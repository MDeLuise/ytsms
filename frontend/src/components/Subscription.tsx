import { AxiosInstance } from "axios";
import { useState } from "react";
import Chip from '@mui/material/Chip';
import DeleteIcon from '@mui/icons-material/Delete';
import CircularProgress from '@mui/material/CircularProgress';

export default function Subscription(props: {
    id: string,
    channelName: string,
    channelId: string,
    requestor: AxiosInstance
}) {
    const [removed, setRemoved] = useState<boolean>(false);

    const removeSubscription = () => {
        props.requestor.delete("subscription/" + props.id, {})
            .then((_data) => {
                setRemoved(true);
            })
            .catch((error) => console.error(error));
    };

    return (
        <Chip
            label={props.channelName ? props.channelName : props.channelId}
            onDelete={removeSubscription}
            deleteIcon={<DeleteIcon />}
            icon={props.channelName ? undefined : <CircularProgress size={"20px"}/>}
            variant="outlined"
            hidden={removed}
        />
    );
}
