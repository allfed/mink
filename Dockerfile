FROM mundialis/grass-py3-pdal:8.0.2-ubuntu
ARG GRASS_PREFIX=/usr/local/grass

USER root

# RUN apt-get update \
#     && apt-get install gcc

RUN mkdir /GRASS_modules
COPY ./GRASS_modules /GRASS_modules

# RUN ln -s /usr/bin/gcc /usr/bin/CC
ENV GRASS_PREFIX=${GRASS_PREFIX}
WORKDIR /GRASS_modules/r.in.new
# RUN make



