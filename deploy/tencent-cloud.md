# 腾讯云服务器部署说明

本文档用于将本项目部署到腾讯云 CVM。以下命令默认服务器为 Ubuntu/CentOS 系 Linux，并使用 `systemd` 管理服务。

## 1. 腾讯云准备

1. 在腾讯云控制台购买 CVM，建议选择 Ubuntu 22.04 LTS。
2. 安全组放行：
   - `22`：SSH 登录。
   - `8080`：直接访问 Spring Boot，调试用。
   - `80`：如果使用 Nginx 反向代理。
   - `443`：如果后续配置 HTTPS。
3. 服务器安装 JDK 17 或更高版本：

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless nginx
java -version
```

CentOS 可使用：

```bash
sudo yum install -y java-17-openjdk nginx
java -version
```

## 2. 本地打包

```powershell
mvn -s settings-local.xml clean package
```

生成文件：

```text
target/aihelp-1.0.0.jar
```

## 3. 上传和启动

把 JAR 上传到服务器：

```bash
sudo mkdir -p /opt/aihelp
sudo chown -R $USER:$USER /opt/aihelp
```

本地执行：

```powershell
scp target/aihelp-1.0.0.jar ubuntu@你的服务器公网IP:/opt/aihelp/aihelp.jar
scp deploy/aihelp.service ubuntu@你的服务器公网IP:/tmp/aihelp.service
```

服务器执行：

```bash
sudo mv /tmp/aihelp.service /etc/systemd/system/aihelp.service
sudo systemctl daemon-reload
sudo systemctl enable --now aihelp
sudo systemctl status aihelp
```

访问：

```text
http://你的服务器公网IP:8080
```

## 4. 真实大模型配置

默认服务使用 mock AI。接入真实大模型时，在服务器创建环境文件：

```bash
sudo tee /opt/aihelp/aihelp.env >/dev/null <<'EOF'
AI_MOCK_ENABLED=false
AI_PROVIDER=openai-compatible
AI_BASE_URL=https://你的模型服务商地址/v1
AI_MODEL=你的模型名称
AI_API_KEY=你的API Key
AI_TIMEOUT_MS=15000
EOF
sudo chmod 600 /opt/aihelp/aihelp.env
```

然后确认 `deploy/aihelp.service` 中包含：

```ini
EnvironmentFile=-/opt/aihelp/aihelp.env
```

重启：

```bash
sudo systemctl daemon-reload
sudo systemctl restart aihelp
sudo journalctl -u aihelp -f
```

## 5. Nginx 反向代理

如果有域名，把 `deploy/nginx-aihelp.conf` 中的 `server_name` 改成你的域名，然后上传：

```powershell
scp deploy/nginx-aihelp.conf ubuntu@你的服务器公网IP:/tmp/aihelp.conf
```

服务器执行：

```bash
sudo mv /tmp/aihelp.conf /etc/nginx/conf.d/aihelp.conf
sudo nginx -t
sudo systemctl reload nginx
```

访问：

```text
http://你的域名
```

## 6. 常用运维命令

```bash
sudo systemctl status aihelp
sudo systemctl restart aihelp
sudo journalctl -u aihelp -n 100 --no-pager
sudo journalctl -u aihelp -f
```

