#!/usr/bin/env python3
"""
WebRTC Signaling Server for Campus Wave Podcast System
Handles real-time signaling for WebRTC audio streaming and admin control events
"""

import asyncio
import json
import logging
import time
from typing import Dict, Set
import websockets
from websockets.server import WebSocketServerProtocol

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Room management: room_id -> set of websocket connections
rooms: Dict[str, Set[WebSocketServerProtocol]] = {}

# Client metadata: websocket -> room_id
client_rooms: Dict[WebSocketServerProtocol, str] = {}


async def broadcast_to_room(room_id: str, message: dict, exclude_sender: WebSocketServerProtocol = None):
    """
    Broadcast a message to all clients in a room except the sender
    """
    if room_id not in rooms:
        logger.warning(f"Attempted to broadcast to non-existent room: {room_id}")
        return
    
    message_str = json.dumps(message)
    disconnected_clients = set()
    
    for client in rooms[room_id]:
        if client != exclude_sender:
            try:
                await client.send(message_str)
            except websockets.exceptions.ConnectionClosed:
                logger.info(f"Client disconnected, removing from room {room_id}")
                disconnected_clients.add(client)
    
    # Clean up disconnected clients
    for client in disconnected_clients:
        await remove_client_from_room(client, room_id)


async def add_client_to_room(websocket: WebSocketServerProtocol, room_id: str):
    """
    Add a client to a room
    """
    if room_id not in rooms:
        rooms[room_id] = set()
    
    rooms[room_id].add(websocket)
    client_rooms[websocket] = room_id
    
    logger.info(f"Client joined room {room_id}. Total clients: {len(rooms[room_id])}")
    
    # Notify others in the room
    await broadcast_to_room(room_id, {
        'type': 'peer_joined',
        'peer_id': id(websocket),
        'room_id': room_id
    }, exclude_sender=websocket)


async def remove_client_from_room(websocket: WebSocketServerProtocol, room_id: str):
    """
    Remove a client from a room
    """
    if room_id in rooms and websocket in rooms[room_id]:
        rooms[room_id].remove(websocket)
        
        # Clean up empty rooms
        if not rooms[room_id]:
            del rooms[room_id]
            logger.info(f"Room {room_id} is now empty and removed")
        else:
            logger.info(f"Client left room {room_id}. Remaining clients: {len(rooms[room_id])}")
            
            # Notify others in the room
            await broadcast_to_room(room_id, {
                'type': 'peer_left',
                'peer_id': id(websocket),
                'room_id': room_id
            })
    
    if websocket in client_rooms:
        del client_rooms[websocket]


async def handle_message(websocket: WebSocketServerProtocol, message: dict):
    """
    Handle incoming WebSocket messages
    """
    msg_type = message.get('type')
    room_id = message.get('room_id')
    
    logger.info(f"Received message: {msg_type} for room: {room_id}")
    
    if msg_type == 'join':
        # Client joining a room
        await add_client_to_room(websocket, room_id)
        await websocket.send(json.dumps({
            'type': 'joined',
            'room_id': room_id,
            'peer_id': id(websocket)
        }))
    
    elif msg_type == 'offer':
        # WebRTC offer from initiator
        await broadcast_to_room(room_id, {
            'type': 'offer',
            'from': id(websocket),
            'room_id': room_id,
            'payload': message.get('payload')
        }, exclude_sender=websocket)
    
    elif msg_type == 'answer':
        # WebRTC answer from responder
        await broadcast_to_room(room_id, {
            'type': 'answer',
            'from': id(websocket),
            'room_id': room_id,
            'payload': message.get('payload')
        }, exclude_sender=websocket)
    
    elif msg_type == 'ice_candidate':
        # ICE candidate exchange
        await broadcast_to_room(room_id, {
            'type': 'ice_candidate',
            'from': id(websocket),
            'room_id': room_id,
            'payload': message.get('payload')
        }, exclude_sender=websocket)
    
    elif msg_type == 'admin_pause':
        # CRITICAL: Admin pausing podcast
        logger.warning(f"🔴 ADMIN PAUSE - Broadcasting to room {room_id}")
        await broadcast_to_room(room_id, {
            'type': 'admin_paused',
            'room_id': room_id,
            'timestamp': int(time.time() * 1000)
        }, exclude_sender=websocket)
    
    elif msg_type == 'admin_resume':
        # CRITICAL: Admin resuming podcast
        logger.warning(f"🟢 ADMIN RESUME - Broadcasting to room {room_id}")
        await broadcast_to_room(room_id, {
            'type': 'admin_resumed',
            'room_id': room_id,
            'timestamp': int(time.time() * 1000)
        }, exclude_sender=websocket)
    
    elif msg_type == 'radio_pause':
        # CRITICAL: Admin pausing radio
        logger.warning(f"🔴 RADIO PAUSE - Broadcasting to room {room_id}")
        await broadcast_to_room(room_id, {
            'type': 'radio_paused',
            'room_id': room_id,
            'timestamp': int(time.time() * 1000)
        }, exclude_sender=websocket)
    
    elif msg_type == 'radio_resume':
        # CRITICAL: Admin resuming radio
        current_position = message.get('current_position', 0)
        logger.warning(f"🟢 RADIO RESUME - Broadcasting to room {room_id} at position {current_position}")
        await broadcast_to_room(room_id, {
            'type': 'radio_resumed',
            'room_id': room_id,
            'timestamp': int(time.time() * 1000),
            'current_position': current_position
        }, exclude_sender=websocket)
    
    elif msg_type == 'radio_stop':
        # CRITICAL: Admin stopping radio
        logger.warning(f"⛔ RADIO STOP - Broadcasting to room {room_id}")
        await broadcast_to_room(room_id, {
            'type': 'radio_stopped',
            'room_id': room_id,
            'timestamp': int(time.time() * 1000)
        }, exclude_sender=websocket)
    
    else:
        logger.warning(f"Unknown message type: {msg_type}")


async def websocket_handler(websocket: WebSocketServerProtocol):
    """
    Main WebSocket connection handler
    """
    logger.info(f"New connection from {websocket.remote_address}")
    
    try:
        async for message_str in websocket:
            try:
                message = json.loads(message_str)
                await handle_message(websocket, message)
            except json.JSONDecodeError:
                logger.error(f"Invalid JSON received: {message_str}")
                await websocket.send(json.dumps({
                    'type': 'error',
                    'message': 'Invalid JSON format'
                }))
            except Exception as e:
                logger.error(f"Error handling message: {e}", exc_info=True)
                await websocket.send(json.dumps({
                    'type': 'error',
                    'message': str(e)
                }))
    
    except websockets.exceptions.ConnectionClosed:
        logger.info(f"Connection closed for {websocket.remote_address}")
    
    finally:
        # Clean up on disconnect
        if websocket in client_rooms:
            room_id = client_rooms[websocket]
            await remove_client_from_room(websocket, room_id)


async def main():
    """
    Start the WebSocket signaling server
    """
    # Listen on all interfaces, port 8765
    host = '0.0.0.0'
    port = 8765
    
    logger.info(f"🚀 Starting WebRTC Signaling Server on {host}:{port}")
    
    async with websockets.serve(websocket_handler, host, port):
        logger.info("✅ Signaling server is running")
        logger.info(f"📡 Clients can connect to ws://<server-ip>:{port}")
        await asyncio.Future()  # Run forever


if __name__ == '__main__':
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("🛑 Signaling server stopped by user")
