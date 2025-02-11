# Konflux Tooling

This contains the tooling needed by PNC when running within a Konflux pipeline. The following 'mini-tools' are
provided, all of which are intended to be called from a Tekton pipeline e.g.

```
    - name: deploy
      image: quay.io/konflux-ci/pnc-konflux-tooling:latest
      securityContext:
        runAsUser: 0
      volumeMounts:
        - mountPath: /mnt/trusted-ca
          name: trusted-ca
          readOnly: true
      env:
        - name: ACCESS_TOKEN
          value: $(params.ACCESS_TOKEN)
      args:
        - deploy
        - --directory=/var/workdir/deployment
        - --mvn-repo=$(params.MVN_REPO)
        - --mvn-username=$(params.MVN_USERNAME)
        - --server-id=$(params.MVN_SERVER_ID)

```

The current tools are:

* copy-artifacts: used by Ant builds to move built artifacts to the correct location.
* deploy: used (with MavenRepositoryDeployer) to deploy files to a maven repository.
* notify: used to notify PNC components that a pipeline has finished.
* prepare : used to preprocess build sources to add a build script and Containerfile.


It is deployed as an image to https://quay.io/repository/redhat-user-workloads/konflux-jbs-pnc-tenant/konflux-tooling?tab=tags&tag=latest and then released to https://quay.io/repository/konflux-ci/pnc-konflux-tooling
