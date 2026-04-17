# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# Copy package files first (cached layer for npm install)
COPY package.json package-lock.json* ./
RUN npm ci --prefer-offline

# Copy source and build
COPY . .
RUN npm run build

# ── Stage 2: Runtime (nginx) ──────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist      /usr/share/nginx/html
COPY nginx.conf                  /etc/nginx/conf.d/default.conf

# Run as non-root (nginx user already exists in official image)
RUN chown -R nginx:nginx /usr/share/nginx/html \
 && chown -R nginx:nginx /var/cache/nginx \
 && chown -R nginx:nginx /var/log/nginx \
 && touch /var/run/nginx.pid \
 && chown nginx:nginx /var/run/nginx.pid

USER nginx

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
