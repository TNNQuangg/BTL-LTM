import { useRef, useState, useEffect } from 'react';
import wsService from '../../services/websocket';
import { useGame } from '../../context/GameContext';
import { motion } from 'framer-motion';
import { Pencil, Eraser, Trash2, PaintBucket, Undo2 } from 'lucide-react';
import { playSound } from '../../services/sound';

const COLORS = [
  '#000000', '#555555', '#1a53ff',
  '#FFFFFF', '#aaaaaa', '#00d4ff',
  '#008a00', '#aa0000', '#884400',
  '#00ff00', '#ff0000', '#ff8800',
  '#aa8800', '#aa0088', '#ffbbbb',
  '#ffee00', '#ff00ff', '#ffccee',
];

const STROKE_WIDTHS = [5, 10, 20, 30];

export default function DrawingCanvas() {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const { selectedTopic } = useGame();
  
  const [isDrawing, setIsDrawing] = useState(false);
  const [color, setColor] = useState('#000000');
  const [lineWidth, setLineWidth] = useState(5);
  const [isEraser, setIsEraser] = useState(false);
  const [currentDrawData, setCurrentDrawData] = useState({ xPoints: [], yPoints: [] });
  
  const drawDataQueue = useRef([]);
  const drawHistory = useRef([]);

  const redrawCanvas = () => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    drawHistory.current.forEach(stroke => {
      if (stroke.xPoints.length === 0) return;
      ctx.beginPath();
      ctx.moveTo(stroke.xPoints[0], stroke.yPoints[0]);
      for (let i = 1; i < stroke.xPoints.length; i++) {
        ctx.lineTo(stroke.xPoints[i], stroke.yPoints[i]);
      }
      ctx.strokeStyle = stroke.isEraser ? '#FFFFFF' : '#' + stroke.colorRGB.toString(16).padStart(6, '0');
      ctx.lineWidth = stroke.strokeWidth;
      ctx.stroke();
    });
  };

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (canvas && container) {
      canvas.width = container.clientWidth;
      canvas.height = container.clientHeight; 
      const ctx = canvas.getContext('2d');
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
    }
    const interval = setInterval(() => flushDrawData(), 100);
    
    // Handle resize
    const handleResize = () => {
      // NOTE: Resizing clears canvas in standard HTML5, we'd need to save/restore image data.
      // For simplicity in this game, we keep a fixed aspect ratio or let it scale via CSS.
    };
    window.addEventListener('resize', handleResize);
    
    return () => {
      clearInterval(interval);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  const flushDrawData = () => {
    if (drawDataQueue.current.length > 0) {
      wsService.send('DRAW_DATA', { drawData: drawDataQueue.current });
      drawDataQueue.current = [];
    }
  };

  const getCoordinates = (e) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    return {
      x: Math.floor((e.clientX - rect.left) * scaleX),
      y: Math.floor((e.clientY - rect.top) * scaleY)
    };
  };

  const startDrawing = (e) => {
    const { x, y } = getCoordinates(e);
    setIsDrawing(true);
    setCurrentDrawData({ xPoints: [x], yPoints: [y] });
    const ctx = canvasRef.current.getContext('2d');
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.strokeStyle = isEraser ? '#FFFFFF' : color;
    ctx.lineWidth = lineWidth;
  };

  const draw = (e) => {
    if (!isDrawing) return;
    const { x, y } = getCoordinates(e);
    const ctx = canvasRef.current.getContext('2d');
    ctx.lineTo(x, y);
    ctx.stroke();
    setCurrentDrawData(prev => ({ xPoints: [...prev.xPoints, x], yPoints: [...prev.yPoints, y] }));
  };

  const stopDrawing = () => {
    if (!isDrawing) return;
    setIsDrawing(false);
    const hexColor = color.replace('#', '');
    const colorRGB = parseInt(hexColor, 16);
    const newStroke = {
      xPoints: currentDrawData.xPoints,
      yPoints: currentDrawData.yPoints,
      colorRGB: isEraser ? 0xFFFFFF : colorRGB,
      strokeWidth: lineWidth,
      isEraser: isEraser
    };
    drawDataQueue.current.push(newStroke);
    drawHistory.current.push(newStroke);
    setCurrentDrawData({ xPoints: [], yPoints: [] });
    flushDrawData();
  };

  const undoLastStroke = () => {
    if (drawHistory.current.length === 0) return;
    drawHistory.current.pop();
    redrawCanvas();
    
    wsService.send('DRAW_DATA', {
      drawData: [{
        xPoints: [],
        yPoints: [],
        colorRGB: 0xFFFFFF,
        strokeWidth: 0,
        isEraser: false,
        undo: true
      }]
    });
    playSound('tick');
  };

  const clearCanvas = () => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    drawHistory.current = [];
    wsService.send('DRAW_DATA', {
      drawData: [{
        xPoints: [],
        yPoints: [],
        colorRGB: 0xFFFFFF,
        strokeWidth: 0,
        isEraser: false,
        clear: true
      }]
    });
    playSound('tick');
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
      
      {/* TOPIC HEADER */}
      <div style={{ textAlign: 'center', marginBottom: 15 }}>
        <h3 style={{ margin: 0, color: 'var(--text-muted)', fontSize: '1rem', fontFamily: "'Caveat', cursive", letterSpacing: 2 }}>TỪ KHÓA CỦA BẠN</h3>
        <div style={{ fontSize: '2rem', fontFamily: "'Bangers', cursive", color: 'var(--ink)', letterSpacing: 5, background: 'var(--bg-base)', display: 'inline-block', padding: '4px 20px', border: '3px solid var(--ink)', borderRadius: 8, boxShadow: 'var(--shadow-neo-sm)' }}>
          {selectedTopic || '???'}
        </div>
      </div>

      {/* CANVAS & TOOLBAR CONTAINER */}
      <div style={{ flex: 1, display: 'flex', gap: 20, minHeight: 0 }}>
        
        {/* TOOLBAR VERTICAL */}
        <div className="glass-panel" style={{ width: 80, padding: 10, display: 'flex', flexDirection: 'column', gap: 20, alignItems: 'center', borderRadius: 20 }}>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10, width: '100%' }}>
            <button 
              onClick={() => setIsEraser(false)}
              style={{ background: !isEraser ? 'var(--bg-base)' : 'transparent', border: `2px solid ${!isEraser ? 'var(--ink)' : 'transparent'}`, borderRadius: 12, padding: 10, cursor: 'pointer', color: 'var(--ink)', boxShadow: !isEraser ? 'var(--shadow-neo-sm)' : 'none' }}
              title="Cọ vẽ"
            >
              <Pencil size={24} style={{ margin: '0 auto' }} />
            </button>
            <button 
              onClick={() => setIsEraser(true)}
              style={{ background: isEraser ? 'var(--accent-red)' : 'transparent', border: `2px solid ${isEraser ? 'var(--ink)' : 'transparent'}`, borderRadius: 12, padding: 10, cursor: 'pointer', color: isEraser ? '#fff' : 'var(--ink)', boxShadow: isEraser ? 'var(--shadow-neo-sm)' : 'none' }}
              title="Tẩy"
            >
              <Eraser size={24} style={{ margin: '0 auto' }} />
            </button>
            <button 
              onClick={undoLastStroke}
              style={{ background: 'transparent', border: '2px solid transparent', borderRadius: 12, padding: 10, cursor: 'pointer', color: 'var(--ink)' }}
              title="Hoàn tác (Undo)"
            >
              <Undo2 size={24} style={{ margin: '0 auto' }} />
            </button>
            <button 
              onClick={clearCanvas}
              style={{ background: 'transparent', border: '2px solid transparent', borderRadius: 12, padding: 10, cursor: 'pointer', color: 'var(--text-muted)' }}
              title="Xóa toàn bộ"
            >
              <Trash2 size={24} style={{ margin: '0 auto' }} />
            </button>
          </div>

          <div style={{ width: '100%', height: 2, background: 'var(--ink)' }} />

          {/* BRUSH SIZES */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 15, alignItems: 'center' }}>
            {STROKE_WIDTHS.map(w => (
              <div 
                key={w} 
                onClick={() => setLineWidth(w)}
                style={{ 
                  width: 40, height: 40, borderRadius: '50%', cursor: 'pointer',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  background: lineWidth === w ? 'var(--bg-base)' : 'transparent',
                  border: lineWidth === w ? '2px solid var(--ink)' : '2px solid transparent'
                }}
              >
                <div style={{ width: w, height: w, borderRadius: '50%', background: 'var(--ink)', boxShadow: lineWidth === w ? 'var(--shadow-neo-sm)' : 'none' }} />
              </div>
            ))}
          </div>

        </div>

        {/* CANVAS WORKSPACE */}
        <div 
          ref={containerRef}
          style={{ 
            flex: 1, 
            background: '#ffffff', 
            borderRadius: 24, 
            overflow: 'hidden',
            boxShadow: 'var(--shadow-neo)',
            border: '4px solid var(--ink)',
            cursor: isEraser ? 'cell' : 'crosshair'
          }}
        >
          <canvas
            ref={canvasRef}
            onMouseDown={startDrawing}
            onMouseMove={draw}
            onMouseUp={stopDrawing}
            onMouseOut={stopDrawing}
            style={{ width: '100%', height: '100%', display: 'block' }}
          />
        </div>

        {/* COLORS VERTICAL */}
        <div className="glass-panel" style={{ width: 80, padding: '15px 10px', display: 'flex', flexDirection: 'column', alignItems: 'center', borderRadius: 20 }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, justifyContent: 'center' }}>
            {COLORS.map(c => (
              <div
                key={c}
                onClick={() => { setColor(c); setIsEraser(false); }}
                style={{
                  width: 26,
                  height: 26,
                  borderRadius: '50%',
                  backgroundColor: c,
                  cursor: 'pointer',
                  border: color === c && !isEraser ? '3px solid var(--ink)' : '2px solid rgba(0,0,0,0.3)',
                  boxShadow: color === c && !isEraser ? 'var(--shadow-neo-sm)' : 'none',
                  transform: color === c && !isEraser ? 'scale(1.2)' : 'scale(1)',
                  transition: 'all 0.2s'
                }}
              />
            ))}
          </div>
          
          <div style={{ width: '100%', height: 2, background: 'var(--ink)', margin: '15px 0' }} />
          
          {/* Nút Đổ Màu tương lai */}
          <button 
            style={{ background: 'transparent', border: '2px solid transparent', borderRadius: 12, padding: 10, cursor: 'not-allowed', color: 'rgba(0,0,0,0.2)' }}
            title="Đổ màu (Sắp ra mắt)"
          >
            <PaintBucket size={24} style={{ margin: '0 auto' }} />
          </button>
        </div>

      </div>
    </div>
  );
}
