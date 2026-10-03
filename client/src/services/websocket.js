/**
 * WebSocket Service — quản lý kết nối WebSocket tới Java Server.
 * Hỗ trợ:
 * - Kết nối / tự động kết nối lại (auto-reconnect)
 * - Đăng ký lắng nghe theo MessageType
 * - Gửi thông điệp JSON
 */

const WS_URL = 'ws://localhost:9999/ws';

class WebSocketService {
  constructor() {
    this.ws = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
    this.reconnectDelay = 2000;
    this.connected = false;
    this.messageQueue = [];
  }

  connect(url = WS_URL) {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    this.maxReconnectAttempts = 10;
    const ws = new WebSocket(url);
    this.ws = ws;

    ws.onopen = () => {
      if (this.ws !== ws) return;
      console.log('[WS] Đã kết nối tới server');
      this.connected = true;
      this.reconnectAttempts = 0;
      this._notifyListeners('CONNECTION', { connected: true });
      
      // Gửi các message đang chờ trong queue
      while (this.messageQueue.length > 0) {
        const msg = this.messageQueue.shift();
        this.ws.send(msg);
      }
    };

    ws.onmessage = (event) => {
      if (this.ws !== ws) return;
      try {
        const message = JSON.parse(event.data);
        this._notifyListeners(message.type, message);
        this._notifyListeners('*', message);
      } catch (e) {
        console.error('[WS] Lỗi parse JSON:', e);
      }
    };

    ws.onclose = (event) => {
      if (this.ws !== ws) return;
      console.log('[WS] Mất kết nối (code:', event.code, ')');
      this.connected = false;
      this._notifyListeners('CONNECTION', { connected: false });

      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        const delay = this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts);
        console.log(`[WS] Thử kết nối lại sau ${Math.round(delay / 1000)}s...`);
        setTimeout(() => {
          this.reconnectAttempts++;
          this.connect(url);
        }, delay);
      }
    };

    ws.onerror = (error) => {
      if (this.ws !== ws) return;
      console.error('[WS] Lỗi WebSocket:', error);
    };
  }

  disconnect() {
    this.maxReconnectAttempts = 0;
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.connected = false;
  }

  /**
   * Gửi thông điệp tới server.
   * @param {string} type - MessageType (ví dụ: 'LOGIN_REQUEST')
   * @param {object} data - Dữ liệu đi kèm
   * @param {string} content - Nội dung text (tùy chọn)
   */
  send(type, data = {}, content = null) {
    const message = JSON.stringify({ type, data, content });
    
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(message);
    } else {
      this.messageQueue.push(message);
    }
  }

  /**
   * Đăng ký lắng nghe một loại message.
   * @param {string} messageType - Loại message hoặc '*' cho tất cả
   * @param {function} handler - Callback function(message)
   * @returns {function} Hàm hủy đăng ký
   */
  on(messageType, handler) {
    if (!this.listeners.has(messageType)) {
      this.listeners.set(messageType, new Set());
    }
    this.listeners.get(messageType).add(handler);
    
    // Trả về hàm cleanup
    return () => this.off(messageType, handler);
  }

  off(messageType, handler) {
    const handlers = this.listeners.get(messageType);
    if (handlers) {
      handlers.delete(handler);
    }
  }

  _notifyListeners(messageType, message) {
    const handlers = this.listeners.get(messageType);
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(message);
        } catch (e) {
          console.error('[WS] Lỗi handler:', e);
        }
      });
    }
  }

  isConnected() {
    return this.connected;
  }
}

// Singleton instance
const wsService = new WebSocketService();
export default wsService;
