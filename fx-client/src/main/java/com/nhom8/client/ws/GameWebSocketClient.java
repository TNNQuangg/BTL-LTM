package com.nhom8.client.ws;

import com.nhom8.common.message.Envelope;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Lớp quản lý kết nối WebSocket phía Client.
 * Sử dụng java.net.http.WebSocket (có sẵn từ Java 11).
 */
public class GameWebSocketClient implements WebSocket.Listener {

    private final String url;
    private final Consumer<Envelope> messageHandler;
    private volatile WebSocket webSocket;
    private long generation;
    private java.util.concurrent.CompletableFuture<?> outbound = java.util.concurrent.CompletableFuture.completedFuture(null);
    private final long heartbeatMillis = Long.getLong("game.heartbeat.interval.ms", 5000);
    private final long heartbeatTimeout = Long.getLong("game.heartbeat.timeout.ms", 12000);
    private StringBuilder messageBuffer = new StringBuilder();
    private volatile Runnable disconnectHandler;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "game-websocket-heartbeat");
        thread.setDaemon(true);
        return thread;
    });
    private volatile ScheduledFuture<?> heartbeatTask;
    private volatile long lastPongMillis;

    public void setDisconnectHandler(Runnable disconnectHandler) {
        this.disconnectHandler = disconnectHandler;
    }

    public GameWebSocketClient(String url, Consumer<Envelope> messageHandler) {
        this.url = url;
        this.messageHandler = messageHandler;
    }

    private java.util.concurrent.CompletableFuture<WebSocket> connectFuture;

    public synchronized java.util.concurrent.CompletableFuture<WebSocket> connect() {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            return java.util.concurrent.CompletableFuture.completedFuture(webSocket);
        }
        if (connectFuture != null && !connectFuture.isDone()) {
            return connectFuture;
        }

        long attempt = ++generation;
        HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(3)).build();
        connectFuture = client.newWebSocketBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(3))
                .buildAsync(URI.create(url), this)
                .thenApply(ws -> {
                    synchronized (this) {
                    if (attempt != generation) { ws.abort(); return null; }
                    this.webSocket = ws;
                    messageBuffer.setLength(0);
                    outbound = java.util.concurrent.CompletableFuture.completedFuture(null);
                    startHeartbeat(ws);
                    System.out.println("[CLIENT] Connected to WebSocket at " + url);
                    return ws;
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[CLIENT] Lỗi kết nối WebSocket: " + ex.getMessage());
                    return null;
                });
        return connectFuture;
    }

    public synchronized void send(Envelope envelope) {
        WebSocket socket = webSocket;
        if (socket != null && !socket.isOutputClosed()) {
            String json = envelope.toJson();
            outbound = outbound.handle((ignored, error) -> null).thenCompose(ignored -> {
                if (socket != webSocket) return java.util.concurrent.CompletableFuture.completedFuture(null);
                return socket.sendText(json, true);
            }).exceptionally(error -> { failed(socket); return null; });
        }
    }

    public synchronized void close() {
        generation++;
        cancelHeartbeat();
        if (webSocket != null) {
            WebSocket old = webSocket;
            webSocket = null;
            old.abort();
        }
        connectFuture = null;
    }

    public boolean isConnected() {
        return webSocket != null && !webSocket.isOutputClosed();
    }

    // === WebSocket.Listener implementation ===

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public synchronized CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        if (this.webSocket != webSocket) return WebSocket.Listener.super.onText(webSocket, data, last);
        messageBuffer.append(data);
        if (last) {
            String fullMessage = messageBuffer.toString();
            messageBuffer.setLength(0); // Clear buffer
            try {
                Envelope envelope = Envelope.fromJson(fullMessage);
                messageHandler.accept(envelope);
            } catch (Exception e) {
                System.err.println("[CLIENT] JSON Parse Error: " + e.getMessage());
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("[CLIENT] WebSocket Closed: " + reason);
        if (this.webSocket == webSocket) {
            this.webSocket = null;
            notifyDisconnected();
        }
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        if (this.webSocket == webSocket) lastPongMillis = System.currentTimeMillis();
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("[CLIENT] WebSocket Error: " + error.getMessage());
        failed(webSocket);
    }

    private synchronized void failed(WebSocket socket) {
        if (socket != webSocket) return;
        webSocket = null; socket.abort(); notifyDisconnected();
    }

    private void startHeartbeat(WebSocket socket) {
        cancelHeartbeat();
        lastPongMillis = System.currentTimeMillis();
        heartbeatTask = heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (this.webSocket != socket || socket.isOutputClosed()) return;
            if (System.currentTimeMillis() - lastPongMillis > heartbeatTimeout) {
                socket.abort();
                if (this.webSocket == socket) {
                    this.webSocket = null;
                    notifyDisconnected();
                }
                return;
            }
            socket.sendPing(ByteBuffer.allocate(0)).exceptionally(error -> {
                if (this.webSocket == socket) {
                    this.webSocket = null;
                    notifyDisconnected();
                    socket.abort();
                }
                return null;
            });
        }, heartbeatMillis, heartbeatMillis, TimeUnit.MILLISECONDS);
    }

    private void notifyDisconnected() {
        cancelHeartbeat();
        Runnable handler = disconnectHandler;
        if (handler != null) handler.run();
    }

    private void cancelHeartbeat() {
        ScheduledFuture<?> task = heartbeatTask;
        if (task != null) task.cancel(false);
        heartbeatTask = null;
    }
}
