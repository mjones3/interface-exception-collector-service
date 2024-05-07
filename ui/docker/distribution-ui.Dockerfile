FROM public.ecr.aws/nginx/nginx:alpine

COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf
COPY docker/nginx/sites-available /etc/nginx/sites-available

RUN ln -s /etc/nginx/sites-available /etc/nginx/sites-enabled && \
    # remove the default nginx config (hello I'm nginx) because it is
    # running on localhost:7001 as well and it would conflict with barista
    rm -rf /etc/nginx/conf.d/default.conf

COPY dist/apps/distribution-ui /var/www/ui

EXPOSE 7001

## Run nginx
CMD ["/bin/sh", "-c", "envsubst < /var/www/ui/assets/settings.template.json > /var/www/ui/assets/settings.json && nginx -g 'daemon off;'"]
