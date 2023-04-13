import { AxiosInstance } from "axios";
import { useEffect, useState } from "react";
import { NavigateFunction, useNavigate } from "react-router-dom";
import Navbar from "./Navbar";
import Subscription from "./Subscription";
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import UploadOutlinedIcon from '@mui/icons-material/UploadOutlined';
import AddCircleOutlineOutlinedIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import ButtonGroup from '@mui/material/ButtonGroup';
import "../style/Settings.scss";

export default function Settings(props: { isLoggedIn: () => boolean, requestor: AxiosInstance, colorMode: any }) {
    let navigate: NavigateFunction = useNavigate();
    const [subscriptionComponents, setSubscriptionComponents] = useState<any[]>([]);
    const [subscriptions, setSubscriptions] = useState<any[]>([]);
    const [channelId, setChannelId] = useState<string>();
    const [backend, setBackend] = useState<string>("");
    const [invidiousInstance, setInvidiousInstance] = useState<string>("");

    const getAllSubscription = () => {
        props.requestor.get("subscription", {})
            .then((response) => {
                let subscriptions: any[] = [];
                response.data.forEach((sub: any) => {
                    subscriptions.push(
                        <Subscription
                            channelName={sub.channelName}
                            channelId={sub.channelId}
                            requestor={props.requestor}
                            id={sub.id} />
                    );
                });
                setSubscriptionComponents(subscriptions);
                setSubscriptions(response.data);
            })
            .catch();
    };

    const addSubscription = (event: React.SyntheticEvent) => {
        event.preventDefault();
        props.requestor.post("subscription", {
            "channelId": channelId
        })
            .then((_res) => {
                getAllSubscription();
            })
            .catch((error) => {
                console.error(error);
            })
    };

    const changeBackend = (backendValue: string) => {
        setBackend(backendValue);
        localStorage.setItem("backend", backendValue);
        if (backend === "invidious") {
            localStorage.setItem("invidiousInstance", invidiousInstance);
        }
    };

    const getBackendFromPreferences = (): string => {
        return localStorage.getItem("backend") != null ? localStorage.getItem("backend")! : "youtube";
    };

    const setInvidiousInstanceString = () => {
        setInvidiousInstance(localStorage.getItem("invidiousInstance") != null ? localStorage.getItem("invidiousInstance")! : "");
    };

    const exportSubscription = () => {
        let subscriptionExportText = "";
        subscriptions.forEach(sub => {
            subscriptionExportText += `${sub.channelName},${sub.channelId}\n`;
        });
        var element = document.createElement('a');
        element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(subscriptionExportText));
        element.setAttribute('download', "subscriptions.txt");

        element.style.display = 'none';
        document.body.appendChild(element);

        element.click();

        document.body.removeChild(element);
    };

    const importSubscription = (files: FileList | null) => {
        if (files != null) {
            let file = files[0];
            let fileReader = new FileReader();
            fileReader.onload = (_e: ProgressEvent<FileReader>) => {
                if (fileReader.result != null) {
                    var lines = (fileReader.result as string).split(/\r\n|\n/);
                    for (var line = 0; line < lines.length - 1; line++) {
                        let importedChannelId = lines[line].split(",")[1];
                        props.requestor.post("subscription", {
                            "channelId": importedChannelId
                        })
                            .catch((error) => {
                                console.error(error);
                            })
                    }
                }
            };
            fileReader.readAsText(file);
            getAllSubscription();
        }
    };

    useEffect(() => {
        if (!props.isLoggedIn()) {
            navigate("/");
        }
        setBackend(getBackendFromPreferences());
        getAllSubscription();
        setInvidiousInstanceString();
    }, []);

    return (
        <>
            <Navbar colorMode={props.colorMode} />
            <div id="settings">
                <h3 className="section">Subscription</h3>

                <ButtonGroup variant="contained" aria-label="outlined primary button group">
                    <Button
                        variant="contained"
                        component="label"
                        startIcon={<UploadOutlinedIcon />}>
                        Import
                        <input hidden accept=".txt" multiple type="file" onChange={(e) => importSubscription(e.target.files)} />
                    </Button>
                    <Button
                        variant="contained"
                        component="label"
                        startIcon={<DownloadOutlinedIcon />}
                        disabled={subscriptions.length == 0}
                        onClick={exportSubscription}>
                        Export
                    </Button>
                </ButtonGroup>

                <form onSubmit={addSubscription} style={{ display: 'flex', alignItems: 'center', flexWrap: "wrap" }}>
                    <label style={{ color: "var(--color)" }} htmlFor="add-subscription">Add subscription</label>
                    <TextField id="add-subscription" label="Channel ID" variant="outlined" onChange={(e) => setChannelId(e.target.value)} />
                    <Button variant="contained" component="label" startIcon={<AddCircleOutlineOutlinedIcon />} onClick={addSubscription}>
                        Subscribe
                    </Button>
                </form>
                <div id="subscription-wrapper">{subscriptionComponents}</div>

                <h3 className="section">Backend</h3>

                <FormControl sx={{ width: "100%" }}>
                    <FormLabel id="demo-radio-buttons-group-label">Service</FormLabel>
                    <RadioGroup
                        aria-labelledby="demo-radio-buttons-group-label"
                        defaultValue="female"
                        name="radio-buttons-group"
                    >
                        <FormControlLabel value="youtube" control={<Radio />} label="YouTube" checked={backend == "youtube"} onChange={(e) => changeBackend((e.currentTarget as HTMLInputElement).value)} />
                        <FormControlLabel value="invidious" control={<Radio />} label="Invidious" checked={backend == "invidious"} onChange={(e) => changeBackend((e.currentTarget as HTMLInputElement).value)} />
                    </RadioGroup>
                </FormControl>
                <TextField
                    id="outlined-basic"
                    label="Invidious instance URL"
                    variant="outlined"
                    disabled={backend != "invidious"}
                    value={invidiousInstance}
                    sx={{ marginTop: "20px" }} />
            </div>
        </>
    );
}
