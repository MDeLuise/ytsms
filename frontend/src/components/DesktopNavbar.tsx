import * as React from 'react';
import { styled, useTheme, Theme, CSSObject } from '@mui/material/styles';
import Box from '@mui/material/Box';
import MuiDrawer from '@mui/material/Drawer';
import MuiAppBar, { AppBarProps as MuiAppBarProps } from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import CssBaseline from '@mui/material/CssBaseline';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import { Link, NavigateFunction, useNavigate } from 'react-router-dom';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ChannelAvatar from './ChannelAvatar';
import secureLocalStorage from 'react-secure-storage';

const drawerWidth = 240;

const openedMixin = (theme: Theme): CSSObject => ({
    width: drawerWidth,
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen,
    }),
    overflowX: 'hidden',
});

const closedMixin = (theme: Theme): CSSObject => ({
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    overflowX: 'hidden',
    width: `calc(${theme.spacing(7)} + 1px)`,
    [theme.breakpoints.up('sm')]: {
        width: `calc(${theme.spacing(8)} + 1px)`,
    },
});

const DrawerHeader = styled('div')(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
}));

interface AppBarProps extends MuiAppBarProps {
    open?: boolean;
}

const AppBar = styled(MuiAppBar, {
    shouldForwardProp: (prop) => prop !== 'open',
})<AppBarProps>(({ theme, open }) => ({
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    ...(open && {
        marginLeft: drawerWidth,
        width: `calc(100% - ${drawerWidth}px)`,
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    }),
}));

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
    ({ theme, open }) => ({
        width: drawerWidth,
        flexShrink: 0,
        whiteSpace: 'nowrap',
        boxSizing: 'border-box',
        ...(open && {
            ...openedMixin(theme),
            '& .MuiDrawer-paper': openedMixin(theme),
        }),
        ...(!open && {
            ...closedMixin(theme),
            '& .MuiDrawer-paper': closedMixin(theme),
        }),
    }),
);

export default function DesktopNavbar(props: {
    channels?: any[],
    colorMode: any,
    getVideoFromChannel: (id: string) => void
    getAllVideo?: () => void
}) {
    const theme = useTheme();
    const [open, setOpen] = React.useState(false);
    let navigate: NavigateFunction = useNavigate();
    const [darkMode, setDarkMode] = React.useState<boolean>(localStorage.getItem("ytsms-dark") != "false");

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
        <Box sx={{ display: 'flex', marginBottom: "100px" }}>
            <CssBaseline />
            <AppBar position="fixed" open={open}>
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={handleDrawerOpen}
                        edge="start"
                        sx={{
                            marginRight: 5,
                            ...(open && { display: 'none' }),
                        }}
                        hidden={props.channels ? false : true}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" flexGrow={1} component="div">
                        <Link to={"/"} onClick={props.getAllVideo} style={{ textDecoration: "none", color: "inherit" }}>YTSMS</Link>
                    </Typography>
                    {darkMode ?
                        <DarkModeIcon style={{marginRight: "15px"}} className="clickable" onClick={toggleDarkMode}></DarkModeIcon> :
                        <WbSunnyIcon style={{marginRight: "15px"}} className="clickable" onClick={toggleDarkMode}></WbSunnyIcon>
                    }
                    <SettingsIcon style={{marginRight: "15px"}} className="clickable" onClick={() => navigate("/settings")}></SettingsIcon>
                    <LogoutIcon className="clickable" onClick={logOut}></LogoutIcon>
                </Toolbar>
            </AppBar>

            <Drawer variant="permanent" open={open} hidden={props.channels ? false : true}>
                <DrawerHeader>
                    <IconButton onClick={handleDrawerClose}>
                        {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
                    </IconButton>
                </DrawerHeader>
                <List style={{flexDirection: "column"}}>
                    {props.channels?.map((ch) => (
                        <ListItem key={ch.id} disablePadding sx={{ display: 'block' }} onClick={() => {handleDrawerClose(); navigate(`/?channels=${ch.id}`); props.getVideoFromChannel(ch.id)}}>
                            <ListItemButton
                                sx={{
                                    justifyContent: 'center',
                                    padding: '5px 0'
                                }}
                            >
                                <ListItemAvatar sx={{display: "flex", justifyContent: 'center'}}>
                                    <ChannelAvatar
                                        id={ch.id}
                                        text={ch.text != undefined ? ch.text : "-"}
                                        src={ch.src}
                                    />
                                </ListItemAvatar>
                                <ListItemText primary={ch.text} sx={{ opacity: open ? 1 : 0, overflow: "hidden" }} />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>
            </Drawer>
        </Box >
    );
}
