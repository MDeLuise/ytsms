import { AxiosInstance } from "axios";
import { useEffect, useState } from "react";
import { NavigateFunction, useNavigate } from "react-router-dom";
import Navbar from "./Navbar";
import Subscription from "./Subscription";
import "../style/Settings.scss";

export default function Settings(props: { isLoggedIn: () => boolean, requestor: AxiosInstance }) {
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
                    )
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


    const changeBackend = (event: React.SyntheticEvent) => {
        event.preventDefault();
        localStorage.setItem("backend", backend);
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
            <Navbar />
            <div id="settings">
                <h3 className="section">Subscription</h3>
                <label htmlFor="import-from-file" style={{ color: "var(--color)" }}>Import from file</label>
                <input type="file" id="import-from-file-hidden"
                    onChange={(e) => importSubscription(e.target.files)}
                    accept=".txt"
                    style={{"display": "none"}}/>
                <input type="button" id="import-from-file" value={"Browse"} onClick={() => document.getElementById('import-from-file-hidden')!.click()}/>
                <form onSubmit={addSubscription}>
                    <label style={{ color: "var(--color)" }} htmlFor="add-subscription">Add subscription</label>
                    <input id="add-subscription" type="text" placeholder="Channel ID" onChange={(e) => setChannelId(e.target.value)} />
                    <button type="submit">Subscribe</button>
                </form>
                <div id="subscription-wrapper">{subscriptionComponents}</div>
                <button onClick={exportSubscription}>Export</button>


                <h3 className="section">Backend</h3>
                <form onSubmit={changeBackend} id="backend-form">
                    <div>
                        <input type="radio" name="backend" id="youtube" value="youtube" checked={backend == "youtube"}
                            onChange={(e) => setBackend(e.currentTarget.value)} />
                        <label htmlFor="youtube" style={{ color: "var(--color)" }}>Youtube</label>
                    </div>
                    <div>
                        <input type="radio" name="backend" id="invidious" value="invidious"
                            onChange={(e) => setBackend(e.currentTarget.value)} checked={backend == "invidious"} />
                        <label htmlFor="invidious" style={{ color: "var(--color)" }} id="invidious-label">Indivious</label>
                        <input type="text" placeholder="Instance URL" disabled={backend != "invidious"}
                            onChange={(e) => setInvidiousInstance(e.currentTarget.value)} value={invidiousInstance} />
                    </div>
                    <button type="submit">Change</button>
                </form>
            </div>
        </>
    )
}