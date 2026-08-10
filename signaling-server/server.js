const WebSocket = require("ws");

const PORT = process.env.PORT || 8080;

const server = new WebSocket.Server({
    port: PORT
});

const rooms = new Map();

console.log(`Signaling server started on port ${PORT}`);

function send(ws, message) {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(message));
    }
}

server.on("connection", (ws) => {

    console.log("Client connected");

    let currentRoom = null;

    ws.on("message", (data) => {

        let message;

        try {
            message = JSON.parse(data.toString());
        } catch (error) {
            send(ws, {
                type: "error",
                message: "Invalid JSON"
            });
            return;
        }

        switch (message.type) {

            case "join":
                joinRoom(ws, message.roomId);
                break;

            case "offer":
            case "answer":
            case "candidate":
                forwardToRoom(ws, message);
                break;

            case "leave":
                leaveRoom(ws);
                break;

            default:
                send(ws, {
                    type: "error",
                    message: "Unknown message type"
                });
        }
    });

    ws.on("close", () => {
        console.log("Client disconnected");
        leaveRoom(ws);
    });

    function joinRoom(socket, roomId) {

        if (!roomId) {
            send(socket, {
                type: "error",
                message: "roomId is required"
            });
            return;
        }

        currentRoom = roomId;

        if (!rooms.has(roomId)) {
            rooms.set(roomId, new Set());
        }

        const room = rooms.get(roomId);

        if (room.size >= 2) {
            send(socket, {
                type: "room-full"
            });
            return;
        }

        room.add(socket);

        send(socket, {
            type: "joined",
            roomId: roomId,
            peers: room.size
        });

        if (room.size === 2) {

            for (const peer of room) {

                send(peer, {
                    type: "peer-ready"
                });

            }
        }
    }

    function forwardToRoom(socket, message) {

        if (!currentRoom) {
            return;
        }

        const room = rooms.get(currentRoom);

        if (!room) {
            return;
        }

        for (const peer of room) {

            if (peer !== socket) {

                send(peer, message);

            }
        }
    }

    function leaveRoom(socket) {

        if (!currentRoom) {
            return;
        }

        const room = rooms.get(currentRoom);

        if (!room) {
            return;
        }

        room.delete(socket);

        for (const peer of room) {

            send(peer, {
                type: "peer-left"
            });
        }

        if (room.size === 0) {
            rooms.delete(currentRoom);
        }

        currentRoom = null;
    }
});

server.on("listening", () => {

    const address = server.address();

    console.log(
        `WebSocket server listening on ${address.port}`
    );
});
