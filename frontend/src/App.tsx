import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Auth from "./components/Auth";
import Home from "./components/Home";
import axios from "axios";
import Settings from "./components/Settings";
import secureLocalStorage from "react-secure-storage";
import React from "react";
import { ThemeProvider, createTheme } from '@mui/material/styles';

const ColorModeContext = React.createContext({ toggleColorMode: () => { } });

export function App() {
  const colorMode = React.useContext(ColorModeContext);
  const isLoggedIn = () => secureLocalStorage.getItem("ytsms-key") != null;
  const backendURL = window._env_.API_URL != null ? window._env_.API_URL : "http://localhost:8085/api";
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

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/:channels?" element={<Home requestor={axiosReq} isLoggedIn={isLoggedIn} colorMode={colorMode}/>} />
        <Route path="/auth" element={<Auth requestor={axiosReq} />} />
        <Route path="/settings" element={<Settings requestor={axiosReq} isLoggedIn={isLoggedIn} colorMode={colorMode}/>} />
      </Routes>
    </BrowserRouter>
  );
}


export default function AppWithColorMode() {
  const [mode, setMode] = React.useState<'light' | 'dark'>(localStorage.getItem("ytsms-dark") != "false" ? "dark" : "light");
  const colorMode = React.useMemo(
    () => ({
      toggleColorMode: () => {
        setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
      },
    }),
    [],
  );

  const theme = React.useMemo(
    () =>
      createTheme({
        palette: {
          mode,
          ...(mode === 'light'
            ? {
                // palette values for light mode
                primary: {
                  main: '#3f51b5',
                },
                secondary: {
                  main: '#f50057',
                },
              }
            : {
                // palette values for dark mode
                primary: {
                  main: '#3f51b5',
                },
                secondary: {
                  main: '#f50057',
                },
              }),
        }}),
    [mode],
  );

  return (
    <ColorModeContext.Provider value={colorMode}>
      <ThemeProvider theme={theme}>
        <App></App>
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
}
