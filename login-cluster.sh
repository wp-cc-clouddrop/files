#!/bin/bash

deploy_gcp() {
    gcloud --quiet components update
    cat ${GCP_CI_SERVICE_ACCOUNT} | gcloud auth activate-service-account --key-file=-
    gcloud --quiet config set project ${GCP_PROJECT_ID}
    gcloud container clusters get-credentials ${GCP_CLUSTER_NAME} --zone ${GCP_CLUSTER_ZONE}
}

deploy_azure() {
    az login --service-principal --username $AZ_USERNAME --password $AZ_PASSWORD --tenant $AZ_TENANT_ID
    az aks get-credentials --resource-group $AZ_RESOURCE_GROUP --name $AZ_CLUSTER_NAME
    kubectl config use-context $AZ_CLUSTER_NAME
}

if [ "$DEPLOY_TARGET" == "azure" ]; then
    echo "Login to Azure"
    deploy_azure
elif [ "$DEPLOY_TARGET" == "gcp" ]; then
    echo "Login to GCP"
    deploy_gcp
else
    echo "DEPLOY TARGET not defined. Exiting"
    exit 1
fi