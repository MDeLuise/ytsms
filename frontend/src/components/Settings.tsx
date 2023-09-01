import { AxiosInstance } from "axios";
import { useEffect, useState } from "react";
import { NavigateFunction, useNavigate } from "react-router-dom";
import Navbar from "./Navbar";
import SubscriptionComponent from "./Subscription";
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
import MenuItem from '@mui/material/MenuItem';
import "../style/Settings.scss";
import { Alert, Snackbar } from "@mui/material";
import { Subscription } from "../interfaces";

export default function Settings(props: { isLoggedIn: () => boolean, requestor: AxiosInstance, colorMode: any }) {
    let navigate: NavigateFunction = useNavigate();
    const [subscriptionComponents, setSubscriptionComponents] = useState<any[]>([]);
    const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
    const [channelId, setChannelId] = useState<string>();
    const [backend, setBackend] = useState<string>();
    const [invidiousInstance, setInvidiousInstance] = useState<string>("");
    const [fetchingMode, setFetchingMode] = useState<"YouTube_API" | "Scraping">("Scraping");
    const [addSubscriptionMode, setAddSubscriptionMode] = useState<"Channel ID" | "Channel Name">("Channel ID");
    const [channelName, setChannelName] = useState<string>();
    const [snackBarOpen, setSnackBarOpen] = useState<boolean>(false);
    const [snackBarMessage, setSnackBarMessage] = useState<string>();
    const [snackBarSeverity, setSnackBarSeverity] = useState<"error" | "warning" | "info" | "success">("success");

    const getAllSubscription = () => {
        props.requestor.get("subscription", {})
            .then((response) => {
                let subscriptions: any[] = [];
                response.data.forEach((sub: any) => {
                    subscriptions.push(
                        <SubscriptionComponent
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
        let payload: {} = {};
        if (addSubscriptionMode === "Channel ID") {
            payload = {
                "channelId": channelId
            };
        } else if (addSubscriptionMode === "Channel Name") {
            payload = {
                "channelName": channelName
            };
        }
        props.requestor.post("subscription", payload)
            .then((_res) => {
                getAllSubscription();
                setSnackBarMessage("Subscription added successfully");
                setSnackBarSeverity("success");
                setSnackBarOpen(true);
            })
            .catch((error) => {
                console.error(error);
                setSnackBarMessage("Error while adding subscription");
                setSnackBarSeverity("error");
                setSnackBarOpen(true);
            })
    };

    const changeBackend = (backendValue: string) => {
        setBackend(backendValue);
        localStorage.setItem("ytsms-backend", backendValue);
        if (backend === "invidious") {
            localStorage.setItem("ytsms-invidiousInstance", invidiousInstance);
        }
    };

    const getBackendFromPreferences = (): string => {
        return localStorage.getItem("ytsms-backend") != null ? localStorage.getItem("ytsms-backend")! : "youtube";
    };

    const setInvidiousInstanceString = () => {
        setInvidiousInstance(localStorage.getItem("ytsms-invidiousInstance") != null ? localStorage.getItem("ytsms-invidiousInstance")! : "");
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
                        let splittedChannelInfo: string[] = lines[line].split(",");
                        let importedChannelId = splittedChannelInfo[splittedChannelInfo.length - 1];
                        props.requestor.post("subscription", {
                            "channelId": importedChannelId
                        })
                            .catch((error) => {
                                console.error(error);
                                setSnackBarMessage("Error while importing subscription: " + importedChannelId);
                                setSnackBarSeverity("error");
                                setSnackBarOpen(true);
                            })
                    }
                }
            };
            fileReader.readAsText(file);
            getAllSubscription();
        }
    };

    const getFethingModeFromBackend = (): void => {
        props.requestor.get("info/scraping-mode")
            .then((res) => {
                setFetchingMode(res.data);
            })
            .catch((error) => {
                console.error(error);
                setSnackBarMessage("Error while retrieving backend fetching mode");
                setSnackBarSeverity("error");
                setSnackBarOpen(true);
            })
    };


    const closeSnackBar = (event: React.SyntheticEvent | Event, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setSnackBarOpen(false);
    };

    useEffect(() => {
        if (!props.isLoggedIn()) {
            navigate("/");
        }
        getFethingModeFromBackend();
        setBackend(getBackendFromPreferences());
        getAllSubscription();
        setInvidiousInstanceString();
    }, []);

    return (
        <>
            <Snackbar
                open={snackBarOpen}
                autoHideDuration={6000}
                onClose={closeSnackBar}
            >
                <Alert variant="filled" onClose={closeSnackBar} severity={snackBarSeverity} sx={{ width: '100%' }}>
                    {snackBarMessage}
                </Alert>
            </Snackbar>

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

                <form onSubmit={addSubscription} style={{
                    display: 'flex',
                    flexWrap: "wrap",
                    flexDirection: "column",
                    maxWidth: "500px",
                    gap: "10px",
                    margin: "50px 0"
                }}>
                    <TextField
                        id="outlined-select-currency"
                        select
                        disabled={fetchingMode === "Scraping"}
                        label="Mode"
                        defaultValue="Channel ID"
                        helperText="Please select the mode"
                        onChange={(e) => setAddSubscriptionMode(e.target.value as "Channel ID" | "Channel Name")}
                    >
                        <MenuItem key={"Channel ID"} value={"Channel ID"}>
                            Channel ID
                        </MenuItem>
                        <MenuItem key={"Channel Name"} value={"Channel Name"}>
                            Channel Name
                        </MenuItem>
                    </TextField>
                    <TextField
                        label={addSubscriptionMode === "Channel Name" ? "Name" : fetchingMode == "YouTube_API" ? "ID" : "Channel ID"}
                        variant="outlined"
                        onChange={(e) => {
                            if (addSubscriptionMode === "Channel ID") {
                                setChannelId(e.target.value);
                            } else if (addSubscriptionMode === "Channel Name") {
                                setChannelName(e.target.value);
                            }
                        }} />
                    <Button variant="contained" component="label" startIcon={<AddCircleOutlineOutlinedIcon />} onClick={addSubscription}>
                        Subscribe
                    </Button>
                </form>
                <p>All subscriptions ({subscriptions.length}):</p>
                <div id="subscription-wrapper">{
                    subscriptionComponents.sort(
                        ((sub1, sub2) => {
                            let first = sub1.props.channelName != null ? sub1.props.channelName : sub1.props.channelId;
                            let second = sub2.props.channelName != null ? sub2.props.channelName : sub2.props.channelId;
                            return first.localeCompare(second);
                        }))
                }</div>

                <h3 className="section">Backend</h3>

                <div className="d-flex flex-column">
                    <FormControl>
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
                        sx={{ marginTop: "20px", maxWidth: "400px" }}
                        onChange={(e) => setInvidiousInstance(e.target.value)} />
                </div>
            </div>
        </>
    );
}
