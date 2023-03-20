import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Auth from "./components/Auth";
import Home from "./components/Home";
import axios from "axios";
import Settings from "./components/Settings";
import secureLocalStorage from "react-secure-storage";


export default function App() {
  const isLoggedIn = () => secureLocalStorage.getItem("ytsms-key") !== null;
  const backendURL = process.env.REACT_APP_API_URL != null ? process.env.REACT_APP_API_URL : "http://localhost:8085/api";
  const axiosReq = axios.create({
    baseURL: backendURL,
    timeout: 1000
  });

  axiosReq.interceptors.request.use(
    (req) => {
      if (!req.url?.startsWith("authentication") && !req.url?.startsWith("api-key")) {
        req.headers['Key'] = secureLocalStorage.getItem("ytsms-key");
      }
      return req;
    },
    (err) => {
      return Promise.reject(err);
    }
  );

  if (localStorage.getItem("dark") != "false") {
    document.body.classList.add('dark');
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home requestor={axiosReq} isLoggedIn={isLoggedIn} />} />
        <Route path="/auth" element={<Auth requestor={axiosReq} />} />
        <Route path="/settings" element={<Settings requestor={axiosReq} isLoggedIn={isLoggedIn} />} />
      </Routes>
    </BrowserRouter>
  );
}