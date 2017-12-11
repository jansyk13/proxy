FROM ubuntu:14.04
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get -y install apache2 git

RUN a2enmod dav
RUN a2enmod dav_fs

RUN mkdir /var/www/webdav
RUN chown -R www-data:www-data /var/www/

RUN service apache2 stop
RUN service apache2 start

RUN git clone https://github.com/tolsen/litmus.git /root/litmus
