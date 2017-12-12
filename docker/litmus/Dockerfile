FROM centos/systemd

RUN yum -y install httpd git less vim gcc make epel-release; yum clean all; systemctl enable httpd.service

EXPOSE 80

RUN mkdir /var/www/html/webdav
RUN chown -R apache:apache /var/www/html
RUN chmod -R 755 /var/www/html/webdav

COPY webdav.conf /etc/httpd/conf.d/webdav.conf

RUN yum -y install litmus

CMD ["/usr/sbin/init"]
