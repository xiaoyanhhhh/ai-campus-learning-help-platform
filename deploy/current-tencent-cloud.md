# Current Tencent Cloud Deployment

Public URL:

```text
http://122.51.83.97/aihelp/login
```

Runtime layout:

```text
Nginx :80
  /aihelp/ -> 127.0.0.1:8081

systemd service: aihelp.service
Spring Boot jar: /opt/aihelp/aihelp.jar
Environment file: /opt/aihelp/aihelp.env
Application context path: /aihelp
```

The environment file stores the model provider configuration and API key on the
server only. Do not commit real keys or server passwords to Git.

Useful commands:

```bash
sudo systemctl status aihelp
sudo systemctl restart aihelp
sudo journalctl -u aihelp -n 100 --no-pager
sudo nginx -t
sudo systemctl reload nginx
```

Smoke checks:

```bash
curl -I http://127.0.0.1:8081/aihelp/login
curl -I http://127.0.0.1/aihelp/login
curl -I http://122.51.83.97/aihelp/login
```
