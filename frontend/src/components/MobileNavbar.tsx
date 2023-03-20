import * as React from 'react';
import { styled, useTheme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import CssBaseline from '@mui/material/CssBaseline';
import MuiAppBar, { AppBarProps as MuiAppBarProps } from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import { NavigateFunction, useNavigate, Link } from 'react-router-dom';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ChannelAvatar from './ChannelAvatar';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import secureLocalStorage from 'react-secure-storage';

const drawerWidth = 240;

const Main = styled('main', { shouldForwardProp: (prop) => prop !== 'open' })<{
    open?: boolean;
}>(({ theme, open }) => ({
    flexGrow: 1,
    padding: theme.spacing(3),
    transition: theme.transitions.create('margin', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    marginLeft: `-${drawerWidth}px`,
    ...(open && {
        transition: theme.transitions.create('margin', {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
        marginLeft: 0,
    }),
}));

interface AppBarProps extends MuiAppBarProps {
    open?: boolean;
}

const AppBar = styled(MuiAppBar, {
    shouldForwardProp: (prop) => prop !== 'open',
})<AppBarProps>(({ theme, open }) => ({
    transition: theme.transitions.create(['margin', 'width'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    ...(open && {
        width: `calc(100% - ${drawerWidth}px)`,
        marginLeft: `${drawerWidth}px`,
        transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
    }),
}));

const DrawerHeader = styled('div')(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
    justifyContent: 'flex-end',
}));

export default function MobileNavbar(props: {
    channels?: any[],
    colorMode: any,
    getVideoFromChannel: (id: string) => void
    getAllVideo?: () => void
}) {
    const theme = useTheme();
    const [open, setOpen] = React.useState(false);
    let navigate: NavigateFunction = useNavigate();
    const [darkMode, setDarkMode] = React.useState<boolean>(localStorage.getItem("dark") != "false");

    const toggleDarkMode = () => {
        localStorage.setItem("ytsms-dark", (!darkMode).toString());
        setDarkMode(!darkMode);
        props.colorMode.toggleColorMode();
    };

    const logOut = () => {
        secureLocalStorage.removeItem("ytsms-key");
        navigate("/auth");
    };

    const handleDrawerOpen = () => {
        setOpen(true);
    };

    const handleDrawerClose = () => {
        setOpen(false);
    };

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            <AppBar position="fixed" open={open}>
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={handleDrawerOpen}
                        edge="start"
                        sx={{ mr: 2, ...(open && { display: 'none' }) }}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" flexGrow={1} component="div">
                        <Link to={"/"} onClick={props.getAllVideo} style={{ textDecoration: "none", color: "inherit" }}>YTSMS</Link>
                    </Typography>
                    {darkMode ?
                        <DarkModeIcon style={{ marginRight: "15px" }} className="clickable" onClick={toggleDarkMode}></DarkModeIcon> :
                        <WbSunnyIcon style={{ marginRight: "15px" }} className="clickable" onClick={toggleDarkMode}></WbSunnyIcon>
                    }
                    <SettingsIcon style={{ marginRight: "15px" }} className="clickable" onClick={() => navigate("/settings")}></SettingsIcon>
                    <LogoutIcon className="clickable" onClick={logOut}></LogoutIcon>
                </Toolbar>
            </AppBar>
            <Drawer
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                    },
                }}
                variant="persistent"
                anchor="left"
                open={open}
            >
                <DrawerHeader>
                    <IconButton onClick={handleDrawerClose}>
                        {theme.direction === 'ltr' ? <ChevronLeftIcon /> : <ChevronRightIcon />}
                    </IconButton>
                </DrawerHeader>
                <Divider />
                <List style={{ flexDirection: "column" }}>
                    {props.channels?.map((ch) => {
                        return (
                            <ListItem key={ch.id} disablePadding sx={{ display: 'block' }} onClick={() => { handleDrawerClose(); navigate(`/?channels=${ch.id}`); props.getVideoFromChannel(ch.id) }}>
                                <ListItemButton
                                    sx={{
                                        justifyContent: 'center',
                                        padding: '5px 0'
                                    }}
                                >
                                    <ListItemAvatar sx={{ display: "flex", justifyContent: 'center' }}>
                                        <ChannelAvatar
                                            id={ch.id}
                                            text={ch.text != undefined ? ch.text : "-"}
                                            src={ch.src} />
                                    </ListItemAvatar>
                                    <ListItemText primary={ch.text} sx={{ opacity: open ? 1 : 0, overflow: "hidden" }} />
                                </ListItemButton>
                            </ListItem>
                        );
                    })}
                </List>
            </Drawer>
            <Main open={open}>
                <DrawerHeader />
            </Main>
        </Box>
    );
}
