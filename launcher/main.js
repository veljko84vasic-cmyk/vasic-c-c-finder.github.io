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
    icon: path.join(__dirname, 'icon.png'),
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

// Find the project directory (go up from launcher folder)
function getProjectDir() {
  return path.resolve(__dirname, '..');
}

// Find Fabric config directory
function getConfigPath() {
  const projectDir = getProjectDir();
  const runConfig = path.join(projectDir, 'run', 'config', 'vasic-client.json');
  return runConfig;
}

// Save username to mod config before launch
function saveUsername(username) {
  const configPath = getConfigPath();
  const configDir = path.dirname(configPath);

  // Create directories if needed
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

// Launch Minecraft via Gradle
ipcMain.handle('launch-game', async (event, username) => {
  try {
    saveUsername(username);

    const projectDir = getProjectDir();
    const isWindows = process.platform === 'win32';
    const gradlew = isWindows ? 'gradlew.bat' : './gradlew';

    const child = spawn(gradlew, ['runClient'], {
      cwd: projectDir,
      shell: true,
      detached: true,
      stdio: 'pipe'
    });

    child.stdout.on('data', (data) => {
      const line = data.toString().trim();
      if (line) mainWindow.webContents.send('log-message', line);
    });

    child.stderr.on('data', (data) => {
      const line = data.toString().trim();
      if (line) mainWindow.webContents.send('log-message', line);
    });

    child.on('error', (err) => {
      mainWindow.webContents.send('launch-error', err.message);
    });

    child.on('close', (code) => {
      mainWindow.webContents.send('game-closed', code);
    });

    return { success: true };
  } catch (err) {
    return { success: false, error: err.message };
  }
});

// Window controls
ipcMain.handle('window-minimize', () => mainWindow.minimize());
ipcMain.handle('window-maximize', () => {
  if (mainWindow.isMaximized()) mainWindow.unmaximize();
  else mainWindow.maximize();
});
ipcMain.handle('window-close', () => mainWindow.close());
