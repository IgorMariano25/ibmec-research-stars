import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#002555",
      dark: "#001a3d",
      light: "#1245FF",
      contrastText: "#ffffff",
    },
    secondary: {
      main: "#F5AC00", 
      dark: "#c48a00",
      light: "#ffc233",
      contrastText: "#002555",
    },
    info: {
      main: "#1245FF", 
      contrastText: "#ffffff",
    },
    success: { main: "#2e7d32" },
    warning: { main: "#F5AC00", contrastText: "#002555" },
    error: { main: "#d32f2f" },
    background: {
      default: "#f5f7fb",
      paper: "#ffffff",
    },
    text: {
      primary: "#002555",
      secondary: "#37474f",
    },
  },
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: 'Roboto, "Helvetica Neue", Arial, sans-serif',
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: { textTransform: "none", fontWeight: 600 },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: "none" },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
    },
  },
});
