const port = process.env.TARGET_PORT || 8080;
const jarPath = process.env.JAR_PATH || '/home/ubuntu/daeng-ddang-be/app.jar';

module.exports = {
  apps: [{
    name: `daeng-backend-${port}`,
    script: 'java',
    args: `-jar -Dserver.port=${port} -Dspring.profiles.active=prod ${jarPath}`,
    cwd: '/home/ubuntu/daeng-ddang-be',
    interpreter: 'none',
    autorestart: false,
    watch: false,
    max_memory_restart: '1G',
  }]
}