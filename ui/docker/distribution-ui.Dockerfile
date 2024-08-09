FROM artifactory.sha.ao.arc-one.com/docker/system/build-cicd/nginx:alpine

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

RUN ln -s /etc/nginx/sites-available /etc/nginx/sites-enabled && \
    # remove the default nginx config (hello I'm nginx) because it is
    # running on localhost:7001 as well and it would conflict with barista
    rm -rf /etc/nginx/conf.d/default.conf


EXPOSE 7001

COPY dist/distribution-ui /var/www/ui

CMD ["/bin/sh", "-c", "envsubst < /var/www/ui/browser/settings.template.json > /var/www/ui/browser/settings.json && nginx -g 'daemon off;'"]

## Run nginx
CMD ["/bin/sh", "-c", "envsubst < /var/www/ui/browser/settings.template.json > /var/www/ui/browser/settings.json && nginx -g 'daemon off;'"]
