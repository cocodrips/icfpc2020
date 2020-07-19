NAME := portal
TAG := latest
HOST := gcr.io
PROJECT_ID := icfpc2020-bokuyaba
IMAGE := $(HOST)/$(PROJECT_ID)/servers/$(NAME):$(TAG)

clean:
	rm -rf _build
	rm -rf bin

setup:
	mkdir -p _build
	cp -r ../demodulator _build
	cp -r ../modem _build

local-tool-build: setup
	mkdir -p bin
	(cd _build/demodulator && make build)
	(cd _build/modem && make build)
	cp _build/demodulator/bin/demodulator bin
	cp _build/modem/bin/modem bin


build: setup
	docker build . -t $(NAME):$(TAG)

push: build
	docker tag $(NAME):$(TAG) $(IMAGE)
	docker push $(IMAGE)

deploy: push
	gcloud run deploy $(NAME) --image $(IMAGE) --platform managed --region us-west1 --allow-unauthenticated
