docker pull ghcr.io/994450/ship-proxy:latest
docker pull ghcr.io/994450/offshore-proxy:latest


docker run -p 9090:9090 ghcr.io/994450/offshore-proxy:latest
docker run -p 8888:8888 ghcr.io/994450/ship-proxy:latest
