ImageJ2 uses several major components, each of which is released on its own
development cycle.

Component releases are done using the [SciJava
release-version.sh](https://github.com/scijava/scijava-common/blob/master/bin/release-version.sh)
script, which uses the
[maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/)
to do some of the work. All releases are tagged in their respective Git
repositories, with binary builds deployed to the [ImageJ Maven
repository](http://maven.imagej.net/).

## [SCIJAVA COMMON](https://github.com/scijava/scijava-common)

SciJava Common provides the core plugin framework and application container.

    cd scijava-common
    release-version.sh 1.0.0

- Where `1.0.0` is the new release version.

Optionally, after performing the release, [tell Jenkins to update
`scijava-common.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

## [IMGLIB2](https://github.com/imagej/imglib)

ImgLib2 provides the core data model and image processing.

    cd imglib
    release-version.sh --tag=imglib2-2.0.0-beta-7 --dev-version=2.0.0-SNAPSHOT 2.0.0-beta-7

- Where `2.0.0-beta-7` is the new release version.
- The `--tag` argument is necessary since the default
  SCM release tag would be `pom-imglib-2.0.0-beta-7`.
- The `--dev-version` argument is required to set the new
  development version back to `2.0.0-SNAPSHOT` afterwards.
  Once ImgLib2 is out of beta, it will no longer be necessary.

Optionally, after performing the release, [tell Jenkins to update
`imglib2.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

## [SCIFIO](https://github.com/scifio/scifio)

SCIFIO provides core I/O functionality.

    cd scifio/scifio
    release-version.sh 0.1.0

- Where `0.1.0` is the new release version.

Optionally, after performing the release, [tell Jenkins to update
`scifio.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

## [CPPTASKS-PARALLEL](https://github.com/scijava/cpptasks-parallel)

CppTasks Parallel is used by the NAR plugin.
We deploy unofficial release builds for use with the ImageJ launcher.

    cd cpptasks-parellel
    release-version.sh --thirdparty=imagej 1.1.1-scijava-1
    git push scijava cpptasks-parallel-1.1.1-scijava-1

- Where `1.1.1-scijava-1` is the new release version.
- The `-scijava-X` qualifier indicates an unofficial release.
- Note that due to the `--thirdparty` argument,
  `release:prepare` will be called in interactive mode.

## [NAR-MAVEN-PLUGIN](https://github.com/scijava/maven-nar-plugin)

The NAR plugin is used to build the ImageJ launcher.
We deploy unofficial release builds.

    cd maven-nar-plugin
    release-version.sh --thirdparty=imagej 3.0.0-scijava-1
    git push scijava nar-maven-plugin-3.0.0-scijava-1

- Where `3.0.0-scijava-1` is the new release version.
- The `-scijava-X` qualifier indicates an unofficial release.
- Note that due to the `--thirdparty` argument,
  `release:prepare` will be called in interactive mode.

Optionally, after performing the release, [tell Jenkins to update
`nar.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

## [IMAGEJ LAUNCHER](https://github.com/imagej/imagej-launcher)

The ImageJ launcher is a native launcher for ImageJ.

    cd imagej-launcher
    release-version.sh --skip-deploy 2.0.0

- Where `2.0.0` is the new release version.

Then, to deploy:

    open http://jenkins.imagej.net/job/ImageJ-launcher/build

And build the newly pushed release tag; e.g., `ij-launcher-2.0.0`.

Optionally, after performing the release, [tell Jenkins to update
`imagej-launcher.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

## [IMAGEJ](https://github.com/imagej/imagej)

The following steps perform a release of ImageJ itself:

#### Tag a release candidate

    cd imagej
    release-version.sh --skip-push --skip-deploy --tag=temp --dev-version=2.0.0-SNAPSHOT 2.0.0-beta-7
    git push origin temp

- Where `2.0.0-beta-7` is the new release version.
- The `--tag=temp` argument creates a temporary tag named `temp`,
  from which we will build the release candidate in the next step.
- The `--dev-version` argument is required to set the new
  development version back to `2.0.0-SNAPSHOT` afterwards.
  Once ImageJ2 is out of beta, it will no longer be necessary.

#### Build the release candidate

    open http://jenkins.imagej.net/job/ImageJ-release-build/build

And specify the newly pushed `temp` tag.

#### Test the release candidate

- Download the resultant [application
  ZIP](http://jenkins.imagej.net/job/ImageJ-release-build/lastSuccessfulBuild/artifact/app/target/)
  from Jenkins and perform desired tests.
- Fix any critical bugs found on `master`
- Delete the `temp` tag locally and remotely
- Start the release process over again

#### Tag, build and deploy the actual release

    cd imagej
    release-version.sh --tag=imagej-2.0.0-beta-7 --dev-version=2.0.0-SNAPSHOT 2.0.0-beta-7

- Where `2.0.0-beta-7` is the new release version.
- The `--tag` argument is necessary since the default
  SCM release tag would be `pom-ij-base-2.0.0-beta-7`.
- The `--dev-version` argument is required to set the new
  development version back to `2.0.0-SNAPSHOT` afterwards.
  Once ImageJ2 is out of beta, it will no longer be necessary.

#### Update pom-scijava

Optionally, after performing the release, [tell Jenkins to update
`imagej.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

#### Upload artifacts to the ImageJ update site

Upload the new JARs to the ImageJ2 update site.
(Script for doing this is coming later.)

#### Update web resources

1. [Create a blog post](http://developer.imagej.net/node/add/blog) on the
   ImageJ web site. It should be modeled after a [previous blog
   entry](http://developer.imagej.net/2012/07/13/imagej-v200-beta3).

2. Rename the artifact `app/target/imagej-XYZ-application.zip` to delete the
   `-application` suffix. Add it as an attachment to the post.

3. Update the [Downloads page](http://developer.imagej.net/downloads).

4. Send a release announcement to the mailing lists (ImageJ and imagej-devel).
   It should be an abbreviated version of the blog post, modeled after a
   [previous release
   announcement](http://imagej.net/pipermail/imagej-devel/2012-May/000975.html).
