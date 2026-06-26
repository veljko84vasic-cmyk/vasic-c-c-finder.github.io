const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('launcher', {
  launchGame: (username) => ipcRenderer.invoke('launch-game', username),
  minimize: () => ipcRenderer.invoke('window-minimize'),
  maximize: () => ipcRenderer.invoke('window-maximize'),
  close: () => ipcRenderer.invoke('window-close'),
  onLog: (callback) => ipcRenderer.on('log-message', (e, msg) => callback(msg)),
  onError: (callback) => ipcRenderer.on('launch-error', (e, msg) => callback(msg)),
  onGameClosed: (callback) => ipcRenderer.on('game-closed', (e, code) => callback(code))
});
