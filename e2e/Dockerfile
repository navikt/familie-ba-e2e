FROM docker.pkg.github.com/navikt/familie-ba-sak-frontend/familie-ba-sak-frontend:e04cf94fc9b44494f5bcc686827a5581da6ee89f

RUN echo "Fra dockercontainer"
RUN hostname
RUN hostname
RUN sudo echo "127.0.0.1 host.docker.internal" | sudo tee -a /etc/hosts
RUN ping -c4 host.docker.internal