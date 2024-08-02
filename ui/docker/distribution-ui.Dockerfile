FROM public.ecr.aws/nginx/nginx:alpine

ARG BUILD_DATE
ARG VERSION
ARG VCS_REF
ARG VCS_URL
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url=$VCS_URL \
      org.label-schema.version=$VERSION

COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf
COPY docker/nginx/sites-available /etc/nginx/sites-available

COPY dist/distribution-ui /var/www/ui

EXPOSE 7001

## Run nginx
CMD ["/bin/sh", "-c", "envsubst < /var/www/ui/assets/settings.template.json > /var/www/ui/assets/settings.json && nginx -g 'daemon off;'"]