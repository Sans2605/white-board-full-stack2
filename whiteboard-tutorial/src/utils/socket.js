import { io } from "socket.io-client";

const token = localStorage.getItem("whiteboard_user_token");

const socket = io("http://localhost:9090", {
  extraHeaders: token ? { Authorization: `Bearer ${token}` } : {}, // Only send if token exists
});

export default socket;
