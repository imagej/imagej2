ImageJ2 uses several major components, each of which is released on its own
development cycle.

Component releases are done using the [SciJava
release-version.sh](https://github.com/scijava/scijava-scripts/blob/master/release-version.sh)
script, which uses the
[maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/)
to do some of the work. All releases are tagged in their respective Git
repositories, with binary builds deployed to the [ImageJ Maven
repository](http://maven.imagej.net/).

## POM-SCIJAVA VERSIONING

The
[Bump-POM-SciJava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/)
Jenkins job is used to automatically manage the version properties in
[pom-scijava](https://github.com/scijava/pom-scijava). If there are no breaking
changes in the ImageJ2 software stack components that will be released, the
procedure is simple:

1. Perform all necessary releases as indicated below.
2. Run Bump-POM-SciJava and select "UPDATE_XXXX" for all components that either:
  * Were newly released
  * Are downstream of a newly released component

This will update the version properties to the latest releases of all
components, and automatically update the pom-scijava usage to this newest
pom-scijava version for each component that "UPDATE_XXXX" was selected.

### BREAKING CHANGES

If some component releases will include breaking API changes, the procedure is
different - as these changes will typically propagate through the software
stack and will break builds if the downstream API use is not updated. In this
case, the procedure is as follows:

1.  Manually update [pom-scijava](https://github.com/scijava/pom-scijava)'s
    version properties to point to the *expected* (but not yet released) version of
    each component that will be released. As a part of this commit, the pom-scijava
    version number should be increased as well.
2.  For each component that will be released, starting from the lowest
    component in the stack (e.g. the component with the breaking changes that
    are being propagated):
    1.  Add a single commit that updates the pom version to the latest and
        performs any necessary API updates.
    2.  Release the component as indicated below.
3.  Run
    [Bump-POM-SciJava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/)
    with the `POM_SCIJAVA_BUMPED_MANUALLY` parameter enabled to deploy the
    latest pom-scijava. Other parameters should be disabled.
4.  (Optional) Run Bump-POM-SciJava for any remaining downstream components
    that need to be updated (e.g. those that were not manually updated).

## PREREQUISITES

All ImageJ2 prerequisites use the
[release-version.sh](https://github.com/scijava/scijava-scripts/blob/master/release-version.sh)
script. For each project:

    cd <project top-level>
    release-version.sh <new version>

The version numbering conventions for each project are as follows:

| Project        | Version syntax |
| -------------- |:--------------:|
| SciJava Common | 1.X.X          |
| ImgLib2        | 2.0.0-beta-X   |
| SCIFIO         | 0.X.X          |
| ImageJ Launcher| 3.X.X          |

## [IMAGEJ LAUNCHER](https://github.com/imagej/imagej-launcher)

After running the release-version.sh script, you will need to manually deploy:

    open http://jenkins.imagej.net/job/ImageJ-launcher/build

And build the newly pushed release tag; e.g., `imagej-launcher-3.14.159`.

## [IMAGEJ](https://github.com/imagej/imagej)

The following steps perform a release of ImageJ itself:

#### Use the latest version of ImageJ 1.x

Verify that the pom-scijava version used by ImageJ references the
[latest available version of ImageJ
1.x](http://maven.imagej.net/content/repositories/releases/net/imagej/ij/):

    cd imagej
    sj-version.sh | grep imagej1.version

If not, [tell Jenkins to update `imagej1.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).
Otherwise, uploading a new release to the update site will downgrade
all ImageJ2 and Fiji users to an obsolete ImageJ 1.x version.

#### Tag a release candidate

*First of all*, if you have an **Eclipse** instance running, quit it, or make sure
that *Project>Build automatically* is checked **off**.

    cd imagej
    release-version.sh --skip-push --skip-deploy --tag=temp 2.0.0-beta-7
    git push origin temp

- Where `2.0.0-beta-7` is the new release version.
- The `--tag=temp` argument creates a temporary tag named `temp`,
  from which we will build the release candidate in the next step.

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

#### Manual testing steps

At a minimum, the following should be tested:

* `File -> Open`
  * Ideally on all [SCIFIO-supported
    formats](https://github.com/scifio/scifio/tree/master/scifio/src/main/java/io/scif/formats).
  * On at least one dataset that will force caching, e.g.
    `test&axes=X,Y,Z&lengths=256,256,100000.fake`
* `File -> Save as...`
  * ideally for all [SCIFIO-supported output
    formats](https://github.com/scifio/scifio/blob/master/scifio/src/main/java/io/scif/Writer.java).
* `ImageJ -> Quit ImageJ`
  * Test this *before and after* opening data, especially data that causes
    caching.
* Plugins
  * Test as many IJ2 plugins (green puzzle piece in the menus) as possible.
  * Test several legacy plugins (IJ1 microscope in the menus) to verify
    compatibility layer.

#### Tag, build and deploy the actual release

    cd imagej
    release-version.sh 2.0.0-beta-7

- Where `2.0.0-beta-7` is the new release version.

#### Update pom-scijava

Optionally, after performing the release, [tell Jenkins to update
`imagej.version` in
pom-scijava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/build).

#### Upload artifacts to the ImageJ update site

- Unpack the [application
  ZIP](http://maven.imagej.net/content/repositories/releases/net/imagej/ij-app/)
- Add upload information for the ''ImageJ'' update site:

  ```
  ./ImageJ-linux64 --update edit-update-site ImageJ http://update.imagej.net/ \
          <user>@update.imagej.net /home/imagej/update-site/
  ```
- Simulate an upload (to make sure everything is correct):

  ```
  ./ImageJ-linux64 --update upload-complete-site --simulate ImageJ
  ```
- If the upload will render files obsolete that Fiji still relies on, upload them
  to the Fiji site using the ```--force-shadow``` option:

  ```
  ./ImageJ-linux64 --update upload --force-shadow --site Fiji <file>...
  ```
- When everything is alright, upload it for real:

  ```
  ./ImageJ-linux64 --update upload-complete-site ImageJ
  ```

#### Update web resources

1. [Create a blog post](http://developer.imagej.net/node/add/blog) on the
   ImageJ web site. It should be modeled after a [previous blog
   entry](http://developer.imagej.net/2013/06/12/imagej-v200-beta-7).

2. Rename the artifact `ij-app-XYZ-application.zip` to `imagej-XYZ.zip` where
   `XYZ` is the release version number. Add it as an attachment to the post.

3. Update the [Downloads page](http://developer.imagej.net/downloads).

4. Send a release announcement to the mailing lists (ImageJ and imagej-devel).
   It should be an abbreviated version of the blog post, modeled after a
   [previous release
   announcement](http://imagej.net/pipermail/imagej-devel/2012-May/000975.html).
