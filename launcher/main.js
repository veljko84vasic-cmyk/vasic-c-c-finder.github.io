const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process');

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1100,
    height: 700,
    minWidth: 900,
    minHeight: 600,
    frame: false,
    backgroundColor: '#07060f',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  mainWindow.loadFile('index.html');
}

app.whenReady().then(createWindow);
app.on('window-all-closed', () => app.quit());

function getProjectDir() {
  return path.resolve(__dirname, '..');
}

function getConfigPath() {
  const projectDir = getProjectDir();
  return path.join(projectDir, 'run', 'config', 'vasic-client.json');
}

function saveUsername(username) {
  const configPath = getConfigPath();
  const configDir = path.dirname(configPath);

  fs.mkdirSync(configDir, { recursive: true });

  let config = {};
  if (fs.existsSync(configPath)) {
    try {
      config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    } catch (e) {
      config = {};
    }
  }

  if (username && username.trim()) {
    config.username = username.trim();
  }

  fs.writeFileSync(configPath, JSON.stringify(config, null, 2));
}

ipcMain.handle('launch-game', async (event, username) => {
  try {
    const projectDir = getProjectDir();
    const gradlewPath = path.join(projectDir, 'gradlew.bat');

    if (!fs.existsSync(gradlewPath)) {
      const msg = 'gradlew.bat not found at: ' + gradlewPath;
      mainWindow.webContents.send('launch-error', msg);
      return { success: false, error: msg };
    }

    saveUsername(username);
    mainWindow.webContents.send('log-message', 'Project dir: ' + projectDir);
    mainWindow.webContents.send('log-message', 'Starting gradlew.bat runClient...');

    const child = spawn('cmd.exe', ['/c', 'gradlew.bat', 'runClient'], {
      cwd: projectDir,
      stdio: 'pipe',
      env: { ...process.env }
    });

    child.stdout.on('data', (data) => {
      const lines = data.toString().split('\n');
      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed) mainWindow.webContents.send('log-message', trimmed);
      }
    });

    child.stderr.on('data', (data) => {
      const lines = data.toString().split('\n');
      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed) mainWindow.webContents.send('log-message', trimmed);
      }
    });

    child.on('error', (err) => {
      mainWindow.webContents.send('launch-error', 'Process error: ' + err.message);
    });

    child.on('close', (code) => {
      mainWindow.webContents.send('game-closed', code);
    });

    return { success: true };
  } catch (err) {
    mainWindow.webContents.send('launch-error', err.message);
    return { success: false, error: err.message };
  }
});

ipcMain.handle('window-minimize', () => mainWindow.minimize());
ipcMain.handle('window-maximize', () => {
  if (mainWindow.isMaximized()) mainWindow.unmaximize();
  else mainWindow.maximize();
});
ipcMain.handle('window-close', () => mainWindow.close());
