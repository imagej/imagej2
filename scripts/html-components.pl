#!/usr/bin/perl

# Script to generate an HTML table describing ImageJ software components.
# Information is recursively scraped from Maven POM files.
# Requires XML::Simple to be installed.

# Usage: perl scripts/html-components.pl > out.html

use strict;
use XML::Simple;

my $dir = `dirname "$0"`;
chop $dir;
require "$dir/subs.pl";

# -- Constants --

my $nameColor = '#81c2ea';
my $descColor = '#d8ecf8';
my $columns = 4;

my $baseURL = 'http://dev.imagejdev.org';
my $jenkinsURL = "$baseURL:8080";
my $tracURL = "$baseURL/trac/imagej";

my $artifactPath = 'lastSuccessfulBuild/artifact/imagej';
my $sourcePath = 'src/main/java';

my $trunkBuildPrefix = "$jenkinsURL/job/ImageJ/$artifactPath";
my $dailyBuildPrefix = "$jenkinsURL/job/ImageJ-daily/$artifactPath";
my $sourcePrefix = "$tracURL/browser/trunk";

# -- Main --

{
  section('Core', 'core');
  section('UI', 'ui/app ui/platform-macosx ' .
    'ui/awt-swing/common ui/awt-swing/util');
  section('UI: Swing', 'ui/awt-swing/swing');
  section('UI: AWT', 'ui/awt-swing/awt');
  section('UI: Headless', 'ui/headless');
  section('UI: Pivot', 'ui/pivot');
  section('UI: SWT', 'ui/swt');
  section('Extra', 'extra');
}

# -- Subroutines --

sub section($$) {
  my $title = shift;
  my $dirs = shift;

  # print section header
  print "<h3>$title</h3>\n\n";

  print "<table border=\"0\">\n";
  print "<tbody><tr>\n";
  print "<th>Trunk build</th>\n";
  print "<th>Daily build</th>\n";
  print "<th>Maven site</th>\n";
  print "<th>Browse source</th>\n";
  print "</tr>\n";

  my @poms = `find $dirs -name 'pom.xml'`;
  foreach my $pom (@poms) {
    chop $pom;
    process($pom);
  }

  # print section footer
  print "</tbody></table>\n\n";
}

# Processes the given POM file.
sub process($) {
  my ($file) = @_;

  # read in POM file
  my $ref = XMLin($file);

  # skip multi-module POMs
  if ($ref->{modules}) {
    return;
  }

  # parse information from the XML structure
  my $artifactId = $ref->{artifactId};
  my $groupId = $ref->{parent}->{groupId};
  my $version = $ref->{parent}->{version};
  my $name = $ref->{name};
  my $description = $ref->{description};

  # synthesize information from known values
  my $componentPath = $file;
  $componentPath =~ s/\/pom.xml$//;
  my $groupPath = $groupId;
  $groupPath =~ s/\./\//g;

  # scan for Java files to determine package prefix
  my $packagePathPrefix = "$componentPath/$sourcePath";
  my $packagePath = $packagePathPrefix;
  while (1) {
    my @java = <$packagePath/*.java>;
    if (@java > 0) {
      # found source file(s)
      last;
    }
    my @files = <$packagePath/*>;
    if (@files != 1) {
      last;
    }
    $packagePath = $files[0];
  }
  my $package = $packagePath;
  $package =~ s/$packagePathPrefix\/?//;
  $package =~ s/\//./g;

  my $jarFile = "$artifactId-$version.jar";
  my $trunkBuild = "$trunkBuildPrefix/$componentPath/target/$jarFile";
  my $dailyBuild = "$dailyBuildPrefix/$componentPath/target/$jarFile";
  my $mavenSite = "$dailyBuildPrefix/$componentPath/target/site/index.html";
  my $sourceCode = "$sourcePrefix/$packagePath";

  print "<tr><td style=\"background-color: $nameColor;\" " .
    "colspan=\"$columns\">$name</td></tr>\n";
  print "<tr><td style=\"background-color: $descColor; font-size: smaller;\" " .
    "colspan=\"$columns\">$description</td></tr>\n";
  print "<tr>\n";
  print "<td><a href=\"$trunkBuild\">$jarFile</a></td>\n";
  print "<td><a href=\"$dailyBuild\">$jarFile</a></td>\n";
  print "<td><a href=\"$mavenSite\">$artifactId</a></td>\n";
  print "<td><a href=\"$sourceCode\">$package</a></td>\n";
  print "</tr>\n";

}
