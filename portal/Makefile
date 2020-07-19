NAME := portal
TAG := latest
HOST := gcr.io
PROJECT_ID := icfpc2020-bokuyaba
IMAGE := $(HOST)/$(PROJECT_ID)/servers/$(NAME):$(TAG)

clean:
	rm -rf _build
	rm -rf bin

setup:
	cd ../ && ./cloud_build_setup.sh

local-tool-build: setup
	mkdir -p bin
	(cd _build/modem && make build)
	cp _build/modem/bin/modem bin


build: setup
	docker build . -t $(NAME):$(TAG)

push: build
	docker tag $(NAME):$(TAG) $(IMAGE)
	docker push $(IMAGE)

deploy: push
	gcloud run deploy $(NAME) --image $(IMAGE) --platform managed --region us-west1 --allow-unauthenticated
