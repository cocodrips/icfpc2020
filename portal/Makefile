NAME := portal
TAG := latest
HOST := gcr.io
PROJECT_ID := icfpc2020-bokuyaba
IMAGE := $(HOST)/$(PROJECT_ID)/servers/$(NAME):$(TAG)

setup:
	(cd ../demodulator && make build)
	mkdir -p bin
	cp ../demodulator/bin/* bin/.

build:
	mkdir -p bin
	mkdir -p _build
	cp -r ../demodulator _build
	docker build . -t $(NAME):$(TAG)

push: build
	docker tag $(NAME):$(TAG) $(IMAGE)
	docker push $(IMAGE)

deploy: push
	gcloud run deploy $(NAME) --image $(IMAGE) --platform managed --region us-west1 --allow-unauthenticated
