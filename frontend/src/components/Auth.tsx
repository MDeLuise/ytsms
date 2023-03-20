import { useEffect, useState } from "react";
import { AxiosInstance } from 'axios';
import "../style/Base.scss";
import "../style/Auth.scss";
import { NavigateFunction, useNavigate } from "react-router";

export default function (props: { requestor: AxiosInstance }) {
    let navigate: NavigateFunction = useNavigate();
    let [authMode, setAuthMode] = useState<string>("signin");
    const [username, setUsername] = useState<string>("admin");
    const [password, setPassword] = useState<string>("admin");
    const [error, setError] = useState(null);

    const doLogin = (event: React.SyntheticEvent) => {
        event.preventDefault();
        props.requestor.defaults.headers.common['Key'] = undefined;
        props.requestor.post("authentication/login", {
            username: username,
            password: password
        })
            .then((response) => {
                let jwt = response.data["jwt"]["value"];
                getOrCreateApiKey(jwt);
            })
            .catch(setError);
    }

    const getOrCreateApiKey = (jwt: string) => {
        const apiKeyName: string = "frontend-app_" + username;
        props.requestor.get("api-key/name/" + apiKeyName, {
            headers: {
                "Authorization": 'Bearer ' + jwt
            }
        })
            .then((response) => {
                localStorage.setItem("key", response.data.value);
                props.requestor.defaults.headers.common['Key'] = response.data.value;
                navigate('/');
            })
            .catch((_error) => {
                props.requestor.post("api-key/?name=" + apiKeyName, {}, {
                    headers: {
                        "Authorization": 'Bearer ' + jwt
                    }
                })
                    .then((response) => {
                        localStorage.setItem("key", response.data);
                        props.requestor.defaults.headers.common['Key'] = response.data;
                        navigate('/');
                    })
                    .catch(setError);
            });
    }


    const signUp = (event: React.SyntheticEvent) => {
        event.preventDefault();
        props.requestor.defaults.headers.common['Key'] = undefined;
        props.requestor.post("authentication/signup", {
            username: username,
            password: password
        })
            .then((_response) => {
                doLogin(event);
            })
            .catch(setError);
    }

    const changeAuthMode = () => {
        setAuthMode(authMode === "signin" ? "signup" : "signin");
        setError(null);
    }


    return (
        <div className="Auth-form-container">
            <form className="Auth-form" onSubmit={authMode === "signin" ? doLogin : signUp}>
                <div className="Auth-form-content">
                    <h3 className="Auth-form-title">{authMode == "signin" ? "Sign In" : "Sign Up"}</h3>
                    <div className="text-center">
                        {authMode == "signin" ? "Not registered yet? " : "Already registered? "}
                        <span className="link-primary" onClick={changeAuthMode}>
                            {authMode == "signin" ? "Sign Up" : "Sign In"}
                        </span>
                    </div>
                    <div className="form-group mt-3">
                        <label>Username</label>
                        <input
                            type="text"
                            className="form-control mt-1"
                            placeholder={authMode == "signin" ? "Enter username" : "Username"}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                    </div>
                    <div className="form-group mt-3">
                        <label>Password</label>
                        <input
                            type="password"
                            className="form-control mt-1"
                            placeholder={authMode == "signin" ? "Enter password" : "Password"}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>
                    <p className="error text-center mt-2">{error ? error["response"]["data"]["message"] : ""}</p>
                    <div className="d-grid gap-2 mt-3">
                        <button type="submit" className="btn btn-primary">
                            {authMode == "signin" ? "Login" : "Register"}
                        </button>
                    </div>
                    {/* <p className="text-center mt-2">
                            Forgot <a href="#">password?</a>
                        </p> */}
                </div>
            </form>
        </div>
    )

}