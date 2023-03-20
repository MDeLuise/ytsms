import { useState } from "react";
import 'font-awesome/css/font-awesome.min.css';
import '../style/Navbar.scss';
import { NavigateFunction, useNavigate } from "react-router-dom";

export default function Navbar(props: {}) {
    let navigate: NavigateFunction = useNavigate();
    const [darkMode, setDarkMode] = useState<boolean>(localStorage.getItem("dark") == "true" || localStorage.getItem("dark") == null);

    const toggleDarkMode = () => {
        localStorage.setItem("dark", (!darkMode).toString());
        setDarkMode(!darkMode);
        document.body.classList.toggle('dark');
    };

    const logOut = () => {
        localStorage.removeItem("key");
        navigate("/auth");
    };

    return (
        <div id="navbar">
            <span className="clickable" onClick={() => navigate("/")}>YTSMS</span>
            <div>
                <i className={"fa clickable " + (darkMode ? "fa-sun-o" : "fa-moon-o")} aria-hidden="true" onClick={toggleDarkMode}></i>
                <i className="clickable fa fa-cog" aria-hidden="true" onClick={() => navigate("/settings")}></i>
                <p className="clickable" onClick={logOut}>LOG OUT</p>
            </div>
        </div>
    )
}